package vn.com.vtcc.browser.api.service;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import vn.com.vtcc.browser.api.Application;
import vn.com.vtcc.browser.api.exception.DataNotFoundException;
import vn.com.vtcc.browser.api.utils.TextUtils;

public class ArticleService {
	private static final int TIMESTAMP_DAY_BEFORE = 86400000;
	private static final int CONNECTION_TIMEOUT = 1000;
	Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
	JedisCluster jc = new JedisCluster(jedisClusterNodes);


	public ArticleService() {
		this.jedisClusterNodes.add(new HostAndPort("192.168.107.201", 3001));
		this.jedisClusterNodes.add(new HostAndPort("192.168.107.202", 3001));
		this.jedisClusterNodes.add(new HostAndPort("192.168.107.203", 3001));
		this.jedisClusterNodes.add(new HostAndPort("192.168.107.204", 3001));
		this.jedisClusterNodes.add(new HostAndPort("192.168.107.205", 3001));
		this.jedisClusterNodes.add(new HostAndPort("192.168.107.206", 3001));
		this.jc = new JedisCluster(this.jedisClusterNodes);
	}

	public static Timestamp getTimeStampNow() {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		return now;
	}

	public String getListHotArticle(String from, String size, String timestamp,String source, String connectivity) throws ParseException, UnknownHostException {
		if (timestamp.equals("0")) {
			Timestamp now = getTimeStampNow();
			timestamp = String.valueOf(now.getTime());
		}
		String ES_FIELDS = "&_source_exclude=raw_content,canonical";
		if (!connectivity.equals("wifi")) { ES_FIELDS = "&_source=title,time_post,images,source,url,tags"; }
		Client client = ClientBuilder.newClient().property(ClientProperties.CONNECT_TIMEOUT,CONNECTION_TIMEOUT).register(JacksonJsonProvider.class);
		WebTarget rootTarget = client
				.target(Application.URL_ELASTICSEARCH  + "q=display:" + Application.STATUS_DISPLAY
						+ " AND timestamp:[* TO " + timestamp + "]" + " AND source:" + source + "&from=" + from + "&size=" + size
						+  "&sort=time_post:desc" + ES_FIELDS);
		Response response = rootTarget.request().get(); // Call get method

		if (response.getStatus() == Application.RESPONE_STATAUS_OK) {
			JSONParser parser = new JSONParser();
			JSONObject json = new JSONObject();
			JSONArray msg = new JSONArray();
			json = (JSONObject) parser.parse(response.readEntity(JSONObject.class).toString());
			json = (JSONObject) parser.parse(json.get("hits").toString());
			msg = this.getSourceImage(json);
			//msg = (JSONArray) json.get("hits");
			client.close();
			if (msg == null) {
				throw new DataNotFoundException("Articles not found");
			} else{
				return msg.toString();
			}
		} else {
			client.close();
			throw new DataNotFoundException("Articles not found");
		}
	}

	public JSONArray getSourceImage(JSONObject input) throws UnknownHostException {
		JSONArray results = (JSONArray) input.get("hits");
		//String host = InetAddress.getLocalHost().getHostAddress();
		if (results != null) {
			for (int i =0; i < results.size(); i++) {
				JSONObject hit = (JSONObject) results.get(i);
				if (hit != null){
					JSONObject _source = (JSONObject) hit.get("_source");
					String source = (String) _source.get("source");
					if (source != null) {
						_source.put("source_favicon", Application.MEDIA_HOST_NAME + "/images/" + source + ".png");
					}
				}
			}
		}
		return results;
	}

	public String getArticleById(String id) throws ParseException {
		Client client = ClientBuilder.newClient().property(ClientProperties.CONNECT_TIMEOUT, CONNECTION_TIMEOUT)
				.property(ClientProperties.READ_TIMEOUT, 1000)
				.register(JacksonJsonProvider.class);
		String ES_FIELDS = "&_source_exclude=raw_content,canonical";
		WebTarget rootTarget = client.target(Application.URL_ELASTICSEARCH + "q=" + id + ES_FIELDS);
		Response response = rootTarget.request().get();

		if (response.getStatus() == Application.RESPONE_STATAUS_OK) {
			JSONParser parser = new JSONParser();
			JSONObject json = new JSONObject();
			JSONArray msg = new JSONArray();
			json = (JSONObject) parser.parse(response.readEntity(JSONObject.class).toString());
			json = (JSONObject) parser.parse(json.get("hits").toString());
			msg = (JSONArray) json.get("hits");
			client.close();
			if (msg != null) {
				return msg.toString().toString();
			} else {
				throw new DataNotFoundException("Article with id " + id + " not found");
			}
		} else {
			client.close();
			throw new DataNotFoundException("Article with id " + id + " not found");
		}

	}

	public String getListArticleByCategoryId(String from, String size, String categoryId, String timestamp, String source, String connectivity) throws ParseException, UnknownHostException {
		//System.out.println("Test for cache redis:" + System.currentTimeMillis()/1000);
		String path = "";
		try {
			if (timestamp.equals("0")) {
				Timestamp now = getTimeStampNow();
				timestamp = String.valueOf(now.getTime());
			}
			String ES_FIELDS = "&_source_exclude=raw_content,canonical";
			if (!connectivity.equals("wifi")) { ES_FIELDS = "&_source=title,time_post,images,source,url"; }
			path = Application.URL_ELASTICSEARCH + "&size=" + size + "&from=" + from + "&sort=time_post:desc"
					+ "&q=display: " + Application.STATUS_DISPLAY +  " AND category.id:"
					+ URLEncoder.encode(categoryId, "UTF-8") + " AND timestamp:[* TO " + timestamp + "] AND source:" + source
					+ ES_FIELDS;
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Client client = ClientBuilder.newClient().property(ClientProperties.CONNECT_TIMEOUT, CONNECTION_TIMEOUT)
				.property(ClientProperties.READ_TIMEOUT, 1000)
				.register(JacksonJsonProvider.class);
		WebTarget rootTarget = client.target(path);
		Response response = rootTarget.request().get(); // Call get method

		if (response.getStatus() == Application.RESPONE_STATAUS_OK) {
			JSONParser parser = new JSONParser();
			JSONObject json = new JSONObject();
			JSONArray msg = new JSONArray();
			json = (JSONObject) parser.parse(response.readEntity(JSONObject.class).toString());
			json = (JSONObject) parser.parse(json.get("hits").toString());
			msg = this.getSourceImage(json);
			//msg = (JSONArray) json.get("hits");
			client.close();
			if (msg == null) {
				throw new DataNotFoundException("Articles not found");
			} else {
				return msg.toString().toString();
			}
		} else {
			client.close();
			throw new DataNotFoundException("Articles not found");
		}

	}

	public String getListArticleByCategoryName(String from, String size, String categoryName, String timestamp, String source, String connectivity) throws ParseException, UnknownHostException {
		String path = "";
		try {
			if (timestamp.equals("0")) {
				Timestamp now = getTimeStampNow();
				timestamp = String.valueOf(now.getTime());
			}
			String ES_FIELDS = "&_source_exclude=raw_content,canonical";
			if (!connectivity.equals("wifi")) { ES_FIELDS = "&_source=title,time_post,images,source,url,tags"; }
			path = Application.URL_ELASTICSEARCH + "&size=" + size + "&from=" + from + "&sort=time_post:desc"
					+ "&q=display:" + Application.STATUS_DISPLAY + " AND category.name:" +
					URLEncoder.encode("\"" + categoryName + "\"", "UTF-8") + " AND timestamp:[* TO " + timestamp + "] AND source:" + source
					+ ES_FIELDS;
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Client client = ClientBuilder.newClient().property(ClientProperties.CONNECT_TIMEOUT, CONNECTION_TIMEOUT)
				.property(ClientProperties.READ_TIMEOUT, 1000)
				.register(JacksonJsonProvider.class);
		WebTarget rootTarget = client.target(path);
		Response response = rootTarget.request().get(); // Call get method

		if (response.getStatus() == Application.RESPONE_STATAUS_OK) {
			JSONParser parser = new JSONParser();
			JSONObject json = new JSONObject();
			JSONArray msg = new JSONArray();
			json = (JSONObject) parser.parse(response.readEntity(JSONObject.class).toString());
			json = (JSONObject) parser.parse(json.get("hits").toString());
			msg = this.getSourceImage(json);
			//msg = (JSONArray) json.get("hits");
			client.close();
			if (msg == null) {
				throw new DataNotFoundException("Articles not found");
			} else {
				return msg.toString().toString();
			}
		} else {
			client.close();
			throw new DataNotFoundException("Articles not found");
		}

	}

	public String getListArticleByTags(String from, String size, String tags, String timestamp, String source, String connectivity) throws ParseException, UnknownHostException {
		Client client = ClientBuilder.newClient().property(ClientProperties.CONNECT_TIMEOUT, CONNECTION_TIMEOUT)
				.property(ClientProperties.READ_TIMEOUT, 1000)
				.register(JacksonJsonProvider.class);
		String ES_FIELDS = "&_source_exclude=raw_content,canonical";
		if (!connectivity.equals("wifi")) { ES_FIELDS = "&_source=title,time_post,images,source,url,tags"; }
		WebTarget rootTarget = client
				.target(Application.URL_ELASTICSEARCH + "&size=" + size + "&from=" + from + "&sort=time_post:desc" + ES_FIELDS);
		String jsonObject = "{\"query\" : {\"constant_score\" : { \"filter\" : {\"bool\" : { \"must\" : [" +
				" {\"terms\" : {\"tags\" : [\"" + tags + "\"]}}," +
				" {\"term\": {\"display\" :"+ Application.STATUS_DISPLAY  +"}} ] } } } } }";

		if (!source.equals("*")) {
			String[] sources = source.split(",");
			String sources_concated = TextUtils.concat_strings(sources);
			jsonObject = "{\"query\" : {\"constant_score\" : { \"filter\" : {\"bool\" : { \"must\" : [" +
					" {\"terms\" : {\"tags\" : [\"" + tags + "\"]}}," +
					" {\"terms\" : {\"source\" : [" + sources_concated + "]}}," +
					" {\"term\": {\"display\" :"+ Application.STATUS_DISPLAY  +"}} ] } } } } }";
		}
		Response response = rootTarget.request().post(Entity.json(jsonObject));

		if (response.getStatus() == Application.RESPONE_STATAUS_OK) {
			JSONParser parser = new JSONParser();
			JSONObject json = new JSONObject();
			JSONArray msg = new JSONArray();
			json = (JSONObject) parser.parse(response.readEntity(JSONObject.class).toString());
			json = (JSONObject) parser.parse(json.get("hits").toString());
			msg = this.getSourceImage(json);
			//msg = (JSONArray) json.get("hits");
			client.close();
			if (msg == null) {
				throw new DataNotFoundException("Articles not found");
			} else {
				return msg.toString().toString();
			}
		} else {
			client.close();
			throw new DataNotFoundException("Articles not found");
		}

	}

	public String getListArticlReleatedTags(String tags, String number, String timestamp, String source, String connectivity) throws ParseException, UnknownHostException {
		Client client = ClientBuilder.newClient().property(ClientProperties.CONNECT_TIMEOUT, CONNECTION_TIMEOUT)
				.property(ClientProperties.READ_TIMEOUT, 1000)
				.register(JacksonJsonProvider.class);
		String ES_FIELDS = "&_source_exclude=raw_content,canonical";
		if (!connectivity.equals("wifi")) { ES_FIELDS = "&_source=title,time_post,images,source,url,tags"; }
		WebTarget rootTarget = client.target(Application.URL_ELASTICSEARCH + "&size=" + number + "&sort=time_post:desc" + ES_FIELDS);
		String jsonObject = "{\"query\" : {\"constant_score\" : { \"filter\" : {\"bool\" : { \"must\" : [ {\"terms\" : {\"tags\" : [\"" + tags + "\"]}}, {\"term\": {\"display\" :"+ Application.STATUS_DISPLAY  +"}} ] } } } } }";
		if (!source.equals("*")) {
			String[] sources = source.split(",");
			String sources_concated = TextUtils.concat_strings(sources);
			jsonObject = "{\"query\" : {\"constant_score\" : { \"filter\" : {\"bool\" : { \"must\" : [" +
					" {\"terms\" : {\"tags\" : [\"" + tags + "\"]}}," +
					" {\"terms\" : {\"source\" : [" + sources_concated + "]}}," +
					" {\"term\": {\"display\" :"+ Application.STATUS_DISPLAY  +"}} ] } } } } }";
		}

		Response response = rootTarget.request().post(Entity.json(jsonObject));

			if (response.getStatus() == Application.RESPONE_STATAUS_OK) {
				JSONParser parser = new JSONParser();
				JSONObject json = new JSONObject();
				JSONArray msg = new JSONArray();
				json = (JSONObject) parser.parse(response.readEntity(JSONObject.class).toString());
				json = (JSONObject) parser.parse(json.get("hits").toString());
				//msg = (JSONArray) json.get("hits");
				msg = this.getSourceImage(json);
				client.close();
				if (msg == null) {
					throw new DataNotFoundException("Articles not found");
				} else {
					return msg.toString().toString();
				}
			} else {
				client.close();
				throw new DataNotFoundException("Articles not found");
			}

	}

	public String getListArticleByStringInTitle(String from, String size, String value, String source, String connectivity) throws ParseException, UnknownHostException {
		String path = "";
		String ES_FIELDS = "&_source_exclude=raw_content,canonical";
		if (!connectivity.equals("wifi")) { ES_FIELDS = "&_source=title,time_post,images,source,url,tags"; }
		try {
			path = Application.URL_ELASTICSEARCH + "&size=" + size + "&from=" + from + "&sort=time_post:desc" + "&q=display:"+Application.STATUS_DISPLAY+" AND title:"
					+ URLEncoder.encode("\"" + value + "\"", "UTF-8") + " AND source:" + source + ES_FIELDS;
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Client client = ClientBuilder.newClient().property(ClientProperties.CONNECT_TIMEOUT, CONNECTION_TIMEOUT)
				.property(ClientProperties.READ_TIMEOUT, 1000)
				.register(JacksonJsonProvider.class);
		WebTarget rootTarget = client.target(path);
		Response response = rootTarget.request().get();
		if (response.getStatus() == Application.RESPONE_STATAUS_OK) {
			JSONParser parser = new JSONParser();
			JSONObject json = new JSONObject();
			JSONArray msg = new JSONArray();
			json = (JSONObject) parser.parse(response.readEntity(JSONObject.class).toString());
			json = (JSONObject) parser.parse(json.get("hits").toString());
			msg = this.getSourceImage(json);
			//msg = (JSONArray) json.get("hits");
			client.close();
			if (msg == null) {
				throw new DataNotFoundException("Articles not found");
			} else {
				return msg.toString().toString();
			}
		} else {
			client.close();
			throw new DataNotFoundException("Articles not found");
		}
	}

	public String getListArticleByStringInSource(String from, String size, String value, String timestamp, String connectivity) throws ParseException, UnknownHostException {
		String path = "";
		try {
			if (timestamp.equals("0")) {
				Timestamp now = getTimeStampNow();
				timestamp = String.valueOf(now.getTime());
			}
			String ES_FIELDS = "&_source_exclude=raw_content,canonical";
			if (!connectivity.equals("wifi")) { ES_FIELDS = "&_source=title,time_post,images,source,url,tags"; }
			path = Application.URL_ELASTICSEARCH + "&size=" + size + "&from=" + from + "&sort=time_post:desc" + "&q=display:"+Application.STATUS_DISPLAY+" AND source:"
					+ URLEncoder.encode("\"" + value + "\"", "UTF-8") + " AND timestamp:[* TO " + timestamp + "]" + ES_FIELDS;
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Client client = ClientBuilder.newClient().property(ClientProperties.CONNECT_TIMEOUT, CONNECTION_TIMEOUT)
				.property(ClientProperties.READ_TIMEOUT, 1000)
				.register(JacksonJsonProvider.class);
		WebTarget rootTarget = client.target(path);
		Response response = rootTarget.request() 
				.get();
		if (response.getStatus() == Application.RESPONE_STATAUS_OK) {
			JSONParser parser = new JSONParser();
			JSONObject json = new JSONObject();
			JSONArray msg = new JSONArray();
			json = (JSONObject) parser.parse(response.readEntity(JSONObject.class).toString());
			json = (JSONObject) parser.parse(json.get("hits").toString());
			msg = this.getSourceImage(json);
			//msg = (JSONArray) json.get("hits");
			client.close();
			if (msg == null) {
				throw new DataNotFoundException("Articles not found");
			} else {
				return msg.toString().toString();
			}
		} else {
			client.close();
			throw new DataNotFoundException("Articles not found");
		}
	}

	public String getListHotTags() throws ParseException {
		Timestamp now = getTimeStampNow();
		Client client = ClientBuilder.newClient().property(ClientProperties.CONNECT_TIMEOUT, CONNECTION_TIMEOUT)
				.property(ClientProperties.READ_TIMEOUT, 1000) 				.register(JacksonJsonProvider.class);
		String timestamp_before = String.valueOf((now.getTime() - TIMESTAMP_DAY_BEFORE) / 1000);
		String timestamp = String.valueOf(now.getTime());
		WebTarget rootTarget = client.target(Application.URL_ELASTICSEARCH);
		String jsonObject = "{\"query\": { \"bool\": { \"must\": [{ \"range\": {\"time_post\" : {\"gte\" : \""+timestamp_before+"\"}}}]}},\"size\": 0, \"aggregations\": {\"hot_tags\": {\"terms\": { \"field\": \"tags\"} }}}";
		Response response = rootTarget.request()
				.post(Entity.json(jsonObject));
		if (response.getStatus() == Application.RESPONE_STATAUS_OK) {
			JSONParser parser = new JSONParser();
			JSONObject json = new JSONObject();
			JSONArray msg = new JSONArray();
			json = (JSONObject) parser.parse(response.readEntity(JSONObject.class).toString());
			json = (JSONObject) parser.parse(json.get("aggregations").toString());
			json = (JSONObject) json.get("hot_tags");
			msg = (JSONArray) json.get("buckets");
			client.close();
			if (msg == null) {
				throw new DataNotFoundException("Tags not found");
			} else {
				return msg.toString().toString();
			}
		} else {
			client.close();
			throw new DataNotFoundException("Tags not found");
		}
	}

	public ResponseEntity<Object> updateRedisHotTags(String input) {
		if (input != "") {
			try {
				this.jc.set(Application.REDIS_KEY, input);
				return ResponseEntity.ok("Update success");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	}

	public ResponseEntity<Object> getHotTags() {
		try {
			String tags = this.jc.get(Application.REDIS_KEY);
			if (tags != null) {
				return ResponseEntity.ok(tags);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	}
}
