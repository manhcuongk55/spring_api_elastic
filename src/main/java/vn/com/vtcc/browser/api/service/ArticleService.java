package vn.com.vtcc.browser.api.service;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder.Item;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryBuilders.*;
import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.*;
import javax.imageio.ImageIO;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
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
import vn.com.vtcc.browser.api.utils.ElasticsearchUtils;
import vn.com.vtcc.browser.api.utils.TextUtils;

public class ArticleService {
	private static final int TIMESTAMP_DAY_BEFORE = 86400000;
	private static final int CONNECTION_TIMEOUT = 1000;
	private static final String[] BLACKLIST_FIELDS = {"raw_content", "canonical"};
	private static final String[] WHITELIST_FIELDS = {"title","time_post","images","source","url","tags", "id"};
	private static final String[] MORE_LIKE_THIS_FIELDS = {"tags", "title", "category"};
	Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
	JedisCluster jc = new JedisCluster(jedisClusterNodes);
	Settings settings = Settings.builder().put("cluster.name", "vbrowser")
						.put("client.transport.sniff", true).build();
	TransportClient esClient = new PreBuiltTransportClient(settings);

	public ArticleService() {
		try {
			this.esClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.107.233"), 9300))
                    	.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.107.232"), 9300))
						.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.107.233"), 9300))
						.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.107.234"), 9300))
						.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.107.235"), 9300))
						.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.107.236"), 9300));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

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

	public String getListHotArticles(String from, String size, String timestamp,String source, String connectivity) throws ParseException, UnknownHostException {
		List<String> sources = Arrays.asList(source.split(","));
		SearchRequestBuilder req = this.esClient.prepareSearch("br_article_v4").setTypes("article").setSearchType(SearchType.QUERY_THEN_FETCH)
									.setQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("display", 1)));
		if (!sources.contains("*")) {
			req.setPostFilter(QueryBuilders.termsQuery("source", sources));
		}
		if (!connectivity.equals("wifi")) {
			req.setFetchSource(WHITELIST_FIELDS,null);
		} else {
			req.setFetchSource(null,BLACKLIST_FIELDS);
		}
		SearchResponse response = req.addSort("time_post", SortOrder.DESC)
				.setFrom(Integer.parseInt(from)).setSize(Integer.parseInt(size)).execute().actionGet();

		return ElasticsearchUtils.convertEsResultToString(response);
	}

	public String getListArticleByCatId(String from, String size, String categoryId, String timestamp, String source, String connectivity) throws ParseException, UnknownHostException {
		List<String> sources = Arrays.asList(source.split(","));

		SearchRequestBuilder req = this.esClient.prepareSearch("br_article_v4").setTypes("article")
				.setSearchType(SearchType.QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("display", 1))
						.must(QueryBuilders.matchQuery("category.id", categoryId)));
		if (!sources.contains("*")) {
			req.setPostFilter(QueryBuilders.termsQuery("source", sources));
		}
		if (!connectivity.equals("wifi")) {
			req.setFetchSource(WHITELIST_FIELDS,null);
		} else {
			req.setFetchSource(null,BLACKLIST_FIELDS);
		}
		SearchResponse response = req.addSort("time_post", SortOrder.DESC)
				.setFrom(Integer.parseInt(from)).setSize(Integer.parseInt(size)).execute().actionGet();

		return ElasticsearchUtils.convertEsResultToString(response);
	}

	public String getRelatedArticles(String id, String size, String timestamp, String source, String connectivity) {
		List<String> sources = Arrays.asList(source.split(","));
		Item itemLikeThis = new Item(Application.ES_INDEX_NAME, Application.ES_INDEX_TYPE, id);
		SearchRequestBuilder req = this.esClient.prepareSearch("br_article_v4").setTypes("article")
				.setQuery(QueryBuilders.boolQuery()
						.must(QueryBuilders.moreLikeThisQuery(MORE_LIKE_THIS_FIELDS, new Item[]{itemLikeThis}).minTermFreq(1))
						.filter(QueryBuilders.termQuery("display","1")));
		/*SearchRequestBuilder req = this.esClient.prepareSearch("br_article_v4").setTypes("article")
				.setQuery(QueryBuilders.moreLikeThisQuery(MORE_LIKE_THIS_FIELDS, new Item[]{itemLikeThis}).maxQueryTerms(10).minTermFreq(1));*/
		if (!sources.contains("*")) {
			req.setPostFilter(QueryBuilders.termsQuery("source", sources));
		}
		if (!connectivity.equals("wifi")) {
			req.setFetchSource(WHITELIST_FIELDS,null);
		} else {
			req.setFetchSource(null, new String[]{"raw_content", "content", "canonical","tags","category"});
		}
		SearchResponse response = req.setFrom(1).setSize(Integer.parseInt(size)).execute().actionGet();
		return ElasticsearchUtils.convertEsResultToString(response);
	}

	public String getListArticleByCatName(String from, String size, String categoryName, String timestamp, String source, String connectivity) throws ParseException, UnknownHostException {
		List<String> sources = Arrays.asList(source.split(","));

		SearchRequestBuilder req = this.esClient.prepareSearch("br_article_v4").setTypes("article")
				.setSearchType(SearchType.QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("display", 1))
						.must(QueryBuilders.matchQuery("category.name", categoryName)));
		if (!sources.contains("*")) {
			req.setPostFilter(QueryBuilders.termsQuery("source", sources));
		}
		if (!connectivity.equals("wifi")) {
			req.setFetchSource(WHITELIST_FIELDS,null);
		} else {
			req.setFetchSource(null,BLACKLIST_FIELDS);
		}
		SearchResponse response = req.addSort("time_post", SortOrder.DESC)
				.setFrom(Integer.parseInt(from)).setSize(Integer.parseInt(size)).execute().actionGet();

		return ElasticsearchUtils.convertEsResultToString(response);
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

	public String getArticleByID(String id) {
		SearchRequestBuilder req = this.esClient.prepareSearch("br_article_v4").setTypes("article")
				.setSearchType(SearchType.QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("id", id)))
				.setFetchSource(new String[] {"content", "title", "images", "snippet", "time_post","source", "tags", "author"},
						new String[] {"raw_content", "canonical"});
		SearchResponse response = req.execute().actionGet();
		return ElasticsearchUtils.convertEsResultToString(response);
	}

	public String getListArticleByTags(String from, String size, String inputTags, String timestamp, String source, String connectivity) {
		List<String> sources = Arrays.asList(source.split(","));
		List<String> tags = Arrays.asList(inputTags.split(","));
		SearchRequestBuilder req = this.esClient.prepareSearch("br_article_v4").setTypes("article")
				.setSearchType(SearchType.QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("display", 1)))
				.setPostFilter(QueryBuilders.termsQuery("tags", tags));
		if (!sources.contains("*")) {
			req.setPostFilter(QueryBuilders.termsQuery("source", sources));
		}

		if (!connectivity.equals("wifi")) {
			req.setFetchSource(WHITELIST_FIELDS,null);
		}  else {
			req.setFetchSource(null,BLACKLIST_FIELDS);
		}
		SearchResponse response = req.addSort("time_post", SortOrder.DESC)
				.setFrom(Integer.parseInt(from)).setSize(Integer.parseInt(size)).execute().actionGet();

		return ElasticsearchUtils.convertEsResultToString(response);
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

	public ResponseEntity<Object> updateRedisHotTagsIOS(String input) {
		if (input != "") {
			try {
				this.jc.set(Application.REDIS_KEY_IOS, input);
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

	public ResponseEntity<Object> getHotTagsIOS() {
		try {
			String tags = this.jc.get(Application.REDIS_KEY_IOS);
			if (tags != null) {
				return ResponseEntity.ok(tags);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	}
}
