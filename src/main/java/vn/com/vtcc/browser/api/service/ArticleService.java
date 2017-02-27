package vn.com.vtcc.browser.api.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import vn.com.vtcc.browser.api.Application;
import vn.com.vtcc.browser.api.exception.DataNotFoundException;

public class ArticleService {
	Client client = ClientBuilder.newClient().register(JacksonJsonProvider.class);

	public static Timestamp getTimeStampNow() {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		return now;
	}

	public String getListHotArticle(String from, String size, String timestamp) throws ParseException {
		if (timestamp.equals("0")) {
			Timestamp now = getTimeStampNow();
			timestamp = String.valueOf(now.getTime());
		}
		WebTarget rootTarget = client
				.target(Application.URL_ELASTICSEARCH  + "q=display:" + Application.STATUS_DISPLAY + " AND timestamp:[* TO " + timestamp + "]&from=" + from + "&size=" + size +  "&sort=time_post:desc");
		Response response = rootTarget.request() 
				.get(); // Call get method
		if (response.getStatus() == Application.RESPONE_STATAUS_OK) {
			JSONParser parser = new JSONParser();
			JSONObject json = new JSONObject();
			JSONArray msg = new JSONArray();
			json = (JSONObject) parser.parse(response.readEntity(JSONObject.class).toString());
			json = (JSONObject) parser.parse(json.get("hits").toString());
			msg = (JSONArray) json.get("hits");
			if (msg == null) {
				throw new DataNotFoundException("Articles not found");
			} else{
				return msg.toString();
			}
		} else {
			throw new DataNotFoundException("Articles not found");
		}

	}
	public String getArticleById(String id) throws ParseException {
		System.out.println("Test for cache redis:" + System.currentTimeMillis()/10000000);
		WebTarget rootTarget = client.target(Application.URL_ELASTICSEARCH + "q=" + id);
		Response response = rootTarget.request().get();
		if (response.getStatus() == Application.RESPONE_STATAUS_OK) {
			JSONParser parser = new JSONParser();
			JSONObject json = new JSONObject();
			JSONArray msg = new JSONArray();
			json = (JSONObject) parser.parse(response.readEntity(JSONObject.class).toString());
			json = (JSONObject) parser.parse(json.get("hits").toString());
			msg = (JSONArray) json.get("hits");
			if (msg != null) {
				return msg.toString().toString();
			} else {
				throw new DataNotFoundException("Article with id " + id + " not found");
			}
		} else {
			throw new DataNotFoundException("Article with id " + id + " not found");
		}
	}

	public String getListArticleByCategoryId(String from, String size, String categoryId, String timestamp) throws ParseException {
		//System.out.println("Test for cache redis:" + System.currentTimeMillis()/1000);
		String path = "";
		try {
			if (timestamp.equals("0")) {
				Timestamp now = getTimeStampNow();
				timestamp = String.valueOf(now.getTime());
			}
			path = Application.URL_ELASTICSEARCH + "&size=" + size + "&from=" + from + "&sort=time_post:desc"
					+ "&q=display: " + Application.STATUS_DISPLAY +  " AND category.id:" + URLEncoder.encode(categoryId, "UTF-8") + " AND timestamp:[* TO " + timestamp + "]";
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		WebTarget rootTarget = client.target(path);
		Response response = rootTarget.request() 
				.get(); // Call get method
		if (response.getStatus() == Application.RESPONE_STATAUS_OK) {
			JSONParser parser = new JSONParser();
			JSONObject json = new JSONObject();
			JSONArray msg = new JSONArray();
			json = (JSONObject) parser.parse(response.readEntity(JSONObject.class).toString());
			json = (JSONObject) parser.parse(json.get("hits").toString());
			msg = (JSONArray) json.get("hits");
			if (msg == null) {
				throw new DataNotFoundException("Articles not found");
			} else {
				return msg.toString().toString();
			}
		} else {
			throw new DataNotFoundException("Articles not found");
		}
	}

	public String getListArticleByCategoryName(String from, String size, String categoryName, String timestamp) throws ParseException {
		String path = "";
		try {
			if (timestamp.equals("0")) {
				Timestamp now = getTimeStampNow();
				timestamp = String.valueOf(now.getTime());
			}
			path = Application.URL_ELASTICSEARCH + "&size=" + size + "&from=" + from + "&sort=time_post:desc"
					+ "&q=display:" + Application.STATUS_DISPLAY + " AND category.name:" +
					URLEncoder.encode("\"" + categoryName + "\"", "UTF-8") + " AND timestamp:[* TO " + timestamp + "]";
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		WebTarget rootTarget = client.target(path);
		Response response = rootTarget.request() 
				.get(); // Call get method
		if (response.getStatus() == Application.RESPONE_STATAUS_OK) {
			JSONParser parser = new JSONParser();
			JSONObject json = new JSONObject();
			JSONArray msg = new JSONArray();
			json = (JSONObject) parser.parse(response.readEntity(JSONObject.class).toString());
			json = (JSONObject) parser.parse(json.get("hits").toString());
			msg = (JSONArray) json.get("hits");
			if (msg == null) {
				throw new DataNotFoundException("Articles not found");
			} else {
				return msg.toString().toString();
			}
		} else {
			throw new DataNotFoundException("Articles not found");
		}
	}

	public String getListArticleByTags(String from, String size, String tags, String timestamp) throws ParseException {
		WebTarget rootTarget = client
				.target(Application.URL_ELASTICSEARCH + "&size=" + size + "&from=" + from + "&sort=time_post:desc");
		String jsonObject = "{\"query\" : {\"constant_score\" : { \"filter\" : {\"bool\" : { \"must\" : [ {\"terms\" : {\"tags\" : [\"" + tags + "\"]}}, {\"term\": {\"display\" :"+ Application.STATUS_DISPLAY  +"}} ] } } } } }";
		Response response = rootTarget.request() 
				.post(Entity.json(jsonObject));
		if (response.getStatus() == Application.RESPONE_STATAUS_OK) {
			JSONParser parser = new JSONParser();
			JSONObject json = new JSONObject();
			JSONArray msg = new JSONArray(); 
			json = (JSONObject) parser.parse(response.readEntity(JSONObject.class).toString());
			json = (JSONObject) parser.parse(json.get("hits").toString());
			msg = (JSONArray) json.get("hits");
			if (msg == null) {
				throw new DataNotFoundException("Articles not found");
			} else {
				return msg.toString().toString();
			}
		} else {
			throw new DataNotFoundException("Articles not found");
		}
	}

	public String getListArticlReleatedTags(String tags, String number, String timestamp) throws ParseException {
		WebTarget rootTarget = client.target(Application.URL_ELASTICSEARCH + "&size=" + number + "&sort=time_post:desc");
		String jsonObject = "{\"query\" : {\"constant_score\" : { \"filter\" : {\"bool\" : { \"must\" : [ {\"terms\" : {\"tags\" : [\"" + tags + "\"]}}, {\"term\": {\"display\" :"+ Application.STATUS_DISPLAY  +"}} ] } } } } }";
		Response response = rootTarget.request()
				.post(Entity.json(jsonObject));
		if (response.getStatus() == Application.RESPONE_STATAUS_OK) {
			JSONParser parser = new JSONParser();
			JSONObject json = new JSONObject();
			JSONArray msg = new JSONArray();
			json = (JSONObject) parser.parse(response.readEntity(JSONObject.class).toString());
			json = (JSONObject) parser.parse(json.get("hits").toString());
			msg = (JSONArray) json.get("hits");
			if (msg == null) {
				throw new DataNotFoundException("Articles not found");
			} else {
				return msg.toString().toString();
			}
		} else {
			throw new DataNotFoundException("Articles not found");
		}
	}

	public String getListArticleByStringInTitle(String from, String size, String value) throws ParseException {
		String path = "";
		try {
			path = Application.URL_ELASTICSEARCH + "&size=" + size + "&from=" + from + "&sort=time_post:desc" + "&q=display:"+Application.STATUS_DISPLAY+" AND title:"
					+ URLEncoder.encode("\"" + value + "\"", "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		WebTarget rootTarget = client.target(path);
		Response response = rootTarget.request() 
				.get();
		if (response.getStatus() == Application.RESPONE_STATAUS_OK) {
			JSONParser parser = new JSONParser();
			JSONObject json = new JSONObject();
			JSONArray msg = new JSONArray();
			json = (JSONObject) parser.parse(response.readEntity(JSONObject.class).toString());
			json = (JSONObject) parser.parse(json.get("hits").toString());
			msg = (JSONArray) json.get("hits");
			if (msg == null) {
				throw new DataNotFoundException("Articles not found");
			} else {
				return msg.toString().toString();
			}
		} else {
			throw new DataNotFoundException("Articles not found");
		}
	}

	public String getListArticleByStringInSource(String from, String size, String value, String timestamp) throws ParseException {
		String path = "";
		try {
			if (timestamp.equals("0")) {
				Timestamp now = getTimeStampNow();
				timestamp = String.valueOf(now.getTime());
			}
			path = Application.URL_ELASTICSEARCH + "&size=" + size + "&from=" + from + "&sort=time_post:desc" + "&q=display:"+Application.STATUS_DISPLAY+" AND source:"
					+ URLEncoder.encode("\"" + value + "\"", "UTF-8") + " AND timestamp:[* TO " + timestamp + "]";
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		WebTarget rootTarget = client.target(path);
		Response response = rootTarget.request() 
				.get();
		if (response.getStatus() == Application.RESPONE_STATAUS_OK) {
			JSONParser parser = new JSONParser();
			JSONObject json = new JSONObject();
			JSONArray msg = new JSONArray();
			json = (JSONObject) parser.parse(response.readEntity(JSONObject.class).toString());
			json = (JSONObject) parser.parse(json.get("hits").toString());
			msg = (JSONArray) json.get("hits");
			if (msg == null) {
				throw new DataNotFoundException("Articles not found");
			} else {
				return msg.toString().toString();
			}
		} else {
			throw new DataNotFoundException("Articles not found");
		}
	}

}
