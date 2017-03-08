package vn.com.vtcc.browser.api.service;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import javax.annotation.Resource;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import redis.clients.jedis.Jedis;
import vn.com.vtcc.browser.api.Application;
import vn.com.vtcc.browser.api.exception.DataNotFoundException;
import vn.com.vtcc.browser.api.unitTest.UpdateRedisUnitTest;

public class ArticleService {
	private static final int TIMESTAMP_DAY_BEFORE = 86400000;
	private static final int CONNECTION_TIMEOUT = 1000;
	private Jedis jedis = new Jedis("localhost");


	public static Timestamp getTimeStampNow() {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		return now;
	}

	public String getListHotArticle(String from, String size, String timestamp, String connectivity) throws ParseException, UnknownHostException {
		if (timestamp.equals("0")) {
			Timestamp now = getTimeStampNow();
			timestamp = String.valueOf(now.getTime());
		}
		String ES_FIELDS = "&_source_exclude=raw_content,canonical";
		if (!connectivity.equals("wifi")) { ES_FIELDS = "&_source=title,time_post,images,source"; }

		Client client = ClientBuilder.newClient().property(ClientProperties.CONNECT_TIMEOUT, CONNECTION_TIMEOUT)
				.property(ClientProperties.READ_TIMEOUT, 1000)
				.register(JacksonJsonProvider.class);
		WebTarget rootTarget = client
				.target(Application.URL_ELASTICSEARCH  + "q=display:" + Application.STATUS_DISPLAY
						+ " AND timestamp:[* TO " + timestamp + "]&from=" + from + "&size=" + size
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
		String host = InetAddress.getLocalHost().getHostAddress();
		if (results != null) {
			for (int i =0; i < results.size(); i++) {
				JSONObject hit = (JSONObject) results.get(i);
				if (hit != null){
					JSONObject _source = (JSONObject) hit.get("_source");
					String source = (String) _source.get("source");
					if (source != null) {
						_source.put("source_favicon", "http://" + host + "/images/" + source + ".png");
					}
				}
			}
		}
		return results;
	}

	public String getArticleById(String id) throws ParseException {
		//System.out.println("Test for cache redis:" + System.currentTimeMillis()/10000000);
		Client client = ClientBuilder.newClient().property(ClientProperties.CONNECT_TIMEOUT, CONNECTION_TIMEOUT)
				.property(ClientProperties.READ_TIMEOUT, 1000)
				.register(JacksonJsonProvider.class);
		WebTarget rootTarget = client.target(Application.URL_ELASTICSEARCH + "q=" + id);
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

	public String getListArticleByCategoryId(String from, String size, String categoryId, String timestamp, String connectivity) throws ParseException, UnknownHostException {
		//System.out.println("Test for cache redis:" + System.currentTimeMillis()/1000);
		String path = "";
		try {
			if (timestamp.equals("0")) {
				Timestamp now = getTimeStampNow();
				timestamp = String.valueOf(now.getTime());
			}
			String ES_FIELDS = "&_source_exclude=raw_content,canonical";
			if (!connectivity.equals("wifi")) { ES_FIELDS = "&_source=title,time_post,images,source"; }
			path = Application.URL_ELASTICSEARCH + "&size=" + size + "&from=" + from + "&sort=time_post:desc"
					+ "&q=display: " + Application.STATUS_DISPLAY +  " AND category.id:"
					+ URLEncoder.encode(categoryId, "UTF-8") + " AND timestamp:[* TO " + timestamp + "]"
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

	public String getListArticleByCategoryName(String from, String size, String categoryName, String timestamp, String connectivity) throws ParseException, UnknownHostException {
		String path = "";
		try {
			if (timestamp.equals("0")) {
				Timestamp now = getTimeStampNow();
				timestamp = String.valueOf(now.getTime());
			}
			String ES_FIELDS = "&_source_exclude=raw_content,canonical";
			if (!connectivity.equals("wifi")) { ES_FIELDS = "&_source=title,time_post,images,source"; }
			path = Application.URL_ELASTICSEARCH + "&size=" + size + "&from=" + from + "&sort=time_post:desc"
					+ "&q=display:" + Application.STATUS_DISPLAY + " AND category.name:" +
					URLEncoder.encode("\"" + categoryName + "\"", "UTF-8") + " AND timestamp:[* TO " + timestamp + "]"
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

	public String getListArticleByTags(String from, String size, String tags, String timestamp, String connectivity) throws ParseException, UnknownHostException {
		Client client = ClientBuilder.newClient().property(ClientProperties.CONNECT_TIMEOUT, CONNECTION_TIMEOUT)
				.property(ClientProperties.READ_TIMEOUT, 1000)
				.register(JacksonJsonProvider.class);
		String ES_FIELDS = "&_source_exclude=raw_content,canonical";
		if (!connectivity.equals("wifi")) { ES_FIELDS = "&_source=title,time_post,images,source"; }
		WebTarget rootTarget = client
				.target(Application.URL_ELASTICSEARCH + "&size=" + size + "&from=" + from + "&sort=time_post:desc" + ES_FIELDS);
		String jsonObject = "{\"query\" : {\"constant_score\" : { \"filter\" : {\"bool\" : { \"must\" : [ {\"terms\" : {\"tags\" : [\"" + tags + "\"]}}, {\"term\": {\"display\" :"+ Application.STATUS_DISPLAY  +"}} ] } } } } }";
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

	public String getListArticlReleatedTags(String tags, String number, String timestamp, String connectivity) throws ParseException, UnknownHostException {
		Client client = ClientBuilder.newClient().property(ClientProperties.CONNECT_TIMEOUT, CONNECTION_TIMEOUT)
				.property(ClientProperties.READ_TIMEOUT, 1000)
				.register(JacksonJsonProvider.class);
		String ES_FIELDS = "&_source_exclude=raw_content,canonical";
		if (!connectivity.equals("wifi")) { ES_FIELDS = "&_source=title,time_post,images,source"; }
		WebTarget rootTarget = client.target(Application.URL_ELASTICSEARCH + "&size=" + number + "&sort=time_post:desc" + ES_FIELDS);
		String jsonObject = "{\"query\" : {\"constant_score\" : { \"filter\" : {\"bool\" : { \"must\" : [ {\"terms\" : {\"tags\" : [\"" + tags + "\"]}}, {\"term\": {\"display\" :"+ Application.STATUS_DISPLAY  +"}} ] } } } } }";
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

	public String getListArticleByStringInTitle(String from, String size, String value, String connectivity) throws ParseException, UnknownHostException {
		String path = "";
		String ES_FIELDS = "&_source_exclude=raw_content,canonical";
		if (!connectivity.equals("wifi")) { ES_FIELDS = "&_source=title,time_post,images,source"; }
		try {
			path = Application.URL_ELASTICSEARCH + "&size=" + size + "&from=" + from + "&sort=time_post:desc" + "&q=display:"+Application.STATUS_DISPLAY+" AND title:"
					+ URLEncoder.encode("\"" + value + "\"", "UTF-8") + ES_FIELDS;
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
			if (!connectivity.equals("wifi")) { ES_FIELDS = "&_source=title,time_post,images,source"; }
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

	public ResponseEntity<Object> getListHotTags() throws ParseException {
		String result = jedis.get(Application.REDIS_KEY);
		if (result != "" && result != null) {
			return ResponseEntity.ok(result);
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		}
	}

	public ResponseEntity<Object> updateRedisHotTags(String input) {
		if (input != "") {
			try {
				this.jedis.set(Application.REDIS_KEY, input);
				return ResponseEntity.ok("Update success");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	}

}
