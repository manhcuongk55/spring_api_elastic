package vn.com.vtcc.browser.api.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
	public String getListHotArticle(String from, String size) throws ParseException {
		System.out.println("Test for cache redis");
		WebTarget rootTarget = client
				.target(Application.URL_ELASTICSEARCH + "from=" + from + "&size=" + size + "&sort=time_post:desc");
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
		WebTarget rootTarget = client.target(Application.URL_ELASTICSEARCH + "q=_id:" + id);
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

	public String getListArticleByCategoryId(String from, String size, String categoryId) throws ParseException {
		String path = "";
		try {
			path = Application.URL_ELASTICSEARCH + "&size=" + size + "&from=" + from + "&sort=time_post:desc"
					+ "&q=category.id:" + URLEncoder.encode(categoryId, "UTF-8");
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

	public String getListArticleByCategoryName(String from, String size, String categoryName) throws ParseException {
		String path = "";
		try {
			path = Application.URL_ELASTICSEARCH + "&size=" + size + "&from=" + from + "&sort=time_post:desc"
					+ "&q=category.name:" + URLEncoder.encode("\"" + categoryName + "\"", "UTF-8");
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

	public String getListArticleByTags(String from, String size, String tags) throws ParseException {
		WebTarget rootTarget = client
				.target(Application.URL_ELASTICSEARCH + "&size=" + size + "&from=" + from + "&sort=time_post:desc");
		Response response = rootTarget.request() 
				.post(Entity.json("{ \"query\" : { \"constant_score\" : { \"filter\" : { \"terms\" : { \"tags\" : [\""
						+ tags + "\"] } } } } }"));
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

	public String getListArticlReleatedTags(String tags, String number) throws ParseException {
		WebTarget rootTarget = client.target(Application.URL_ELASTICSEARCH + "&size=" + number + "&sort=time_post:desc");
		Response response = rootTarget.request()
				.post(Entity.json("{ \"query\" : { \"constant_score\" : { \"filter\" : { \"terms\" : { \"tags\" : [\""
						+ tags + "\"] } } } } }"));
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
			path = Application.URL_ELASTICSEARCH + "&size=" + size + "&from=" + from + "&sort=time_post:desc" + "&q=title:"
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

	public String getListArticleByStringInSource(String from, String size, String value) throws ParseException {
		String path = "";
		try {
			path = Application.URL_ELASTICSEARCH + "&size=" + size + "&from=" + from + "&sort=time_post:desc" + "&q=source:"
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

}
