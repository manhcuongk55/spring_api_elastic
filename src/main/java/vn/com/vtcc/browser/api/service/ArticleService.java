package vn.com.vtcc.browser.api.service;

import org.aspectj.lang.annotation.Aspect;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder.Item;
import org.elasticsearch.index.query.QueryBuilder;
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

import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.glassfish.jersey.client.ClientProperties;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import vn.com.vtcc.browser.api.Application;
import vn.com.vtcc.browser.api.config.ProductionConfig;
import vn.com.vtcc.browser.api.exception.DataNotFoundException;
import vn.com.vtcc.browser.api.utils.ElasticsearchUtils;
import vn.com.vtcc.browser.api.utils.TextUtils;

public class ArticleService {
	private static final int TIMESTAMP_DAY_BEFORE = 86400000;
	private static final int CONNECTION_TIMEOUT = 1000;
	private static final String[] BLACKLIST_FIELDS = {"raw_content", "canonical"};
	private static final String[] WHITELIST_FIELDS = {"title","time_post","images","source",
						"url","tags", "id","content","snippet","category"};
	private static final String[] MORE_LIKE_THIS_FIELDS = {"tags", "title", "category"};
	private String[] redisHosts = {""};
	private String[] esHosts = {""};

	Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
	JedisCluster jc = new JedisCluster(jedisClusterNodes);
	Settings settings = Settings.builder().put("cluster.name", Application.ES_CLUSTER_NAME)
						.put("client.transport.sniff", true).build();
	TransportClient esClient = new PreBuiltTransportClient(settings);

	public ArticleService() {

		try {
			if (Application.PRODUCTION_ENV == true) {
				this.redisHosts = ProductionConfig.REDIS_HOST_PRODUCTION;
				this.esHosts = ProductionConfig.ES_HOST_PRODUCTION;
			} else {
				this.redisHosts = ProductionConfig.REDIS_HOST_STAGING;
				this.esHosts = ProductionConfig.ES_HOST_STAGING;
			}
			for (String esHost : this.esHosts) {
				this.esClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esHost),
						ProductionConfig.ES_TRANSPORT_PORT));
			}
			for (String redisHost : this.redisHosts) {
				this.jedisClusterNodes.add(new HostAndPort(redisHost, ProductionConfig.REDIS_PORT));
			}
			this.jc = new JedisCluster(this.jedisClusterNodes);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}


	}

	public static Timestamp getTimeStampNow() {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		return now;
	}

	public String getListHotArticles(String from, String size, String timestamp,String source, String connectivity)
			throws ParseException, UnknownHostException {
		List<String> sources = new ArrayList<>();
		sources.addAll(Arrays.asList(source.split(",")));
		if (sources.contains("kenh14.vn")) {
			sources.remove("kenh14.vn");
			sources.add("kenh14");
		}
		SearchRequestBuilder req = this.esClient.prepareSearch("br_article_v4")
							.setTypes("article").setSearchType(SearchType.QUERY_THEN_FETCH)
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

	public String getListArticleByCatId(String from, String size, String categoryId, String timestamp, String source,
										String connectivity) throws ParseException, UnknownHostException {
		List<String> sources = new ArrayList<>();
		sources.addAll(Arrays.asList(source.split(",")));
		if (sources.contains("kenh14.vn")) {
			sources.remove("kenh14.vn");
			sources.add("kenh14");
		}
		SearchRequestBuilder req = this.esClient.prepareSearch("br_article_v4").setTypes("article")
				.setSearchType(SearchType.QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("display", 1))
						.must(QueryBuilders.matchQuery("category.id", categoryId)));
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


	public String getListArticleByCatName(String from, String size, String categoryName, String timestamp, String source,
										  String connectivity) throws ParseException, UnknownHostException {
		List<String> sources = new ArrayList<>();
		sources.addAll(Arrays.asList(source.split(",")));
		if (sources.contains("kenh14.vn")) {
			sources.remove("kenh14.vn");
			sources.add("kenh14");
		}

		SearchRequestBuilder req = this.esClient.prepareSearch("br_article_v4").setTypes("article")
				.setSearchType(SearchType.QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("display", 1))
						.must(QueryBuilders.matchQuery("category.name", categoryName)));
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

	public JSONArray getSourceImage(JSONObject input) throws UnknownHostException {
		JSONArray results = (JSONArray) input.get("hits");
		if (results != null) {
			for (int i =0; i < results.size(); i++) {
				JSONObject hit = (JSONObject) results.get(i);
				if (hit != null){
					JSONObject _source = (JSONObject) hit.get("_source");
					String source = (String) _source.get("source");
					if (source != null) {
						_source.put("source_favicon", ProductionConfig.MEDIA_HOST_NAME + "/images/" + source + ".png");
					}
				}
			}
		}
		return results;
	}

	public String getRelatedArticles(String id, String size, String timestamp, String source, String connectivity) {
		List<String> sources = new ArrayList<>();
		sources.addAll(Arrays.asList(source.split(",")));
		if (sources.contains("kenh14.vn")) {
			sources.remove("kenh14.vn");
			sources.add("kenh14");
		}

		Item itemLikeThis = new Item(ProductionConfig.ES_INDEX_NAME, ProductionConfig.ES_INDEX_TYPE, id);
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
			req.setFetchSource(null, BLACKLIST_FIELDS);
		}
		SearchResponse response = req.setFrom(1).setSize(Integer.parseInt(size)).execute().actionGet();

		return ElasticsearchUtils.convertEsResultToString(response);
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
		List<String> sources = new ArrayList<>();
		sources.addAll(Arrays.asList(source.split(",")));
		if (sources.contains("kenh14.vn")) {
			sources.remove("kenh14.vn");
			sources.add("kenh14");
		}
		List<String> tags = Arrays.asList(inputTags.split(","));
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
				.must(QueryBuilders.termQuery("display",1))
				.filter(QueryBuilders.termsQuery("tags", tags));
		if (!sources.contains("*")) {
			boolQuery.filter(QueryBuilders.termsQuery("source",sources));
		}
		SearchRequestBuilder query = this.esClient.prepareSearch("br_article_v4")
				.setTypes("article").setQuery(boolQuery);

		if (!connectivity.equals("wifi")) {
			query.setFetchSource(WHITELIST_FIELDS,null);
		}  else {
			query.setFetchSource(null,BLACKLIST_FIELDS);
		}
		SearchResponse response = query.addSort("time_post", SortOrder.DESC)
				.setFrom(Integer.parseInt(from)).setSize(Integer.parseInt(size)).execute().actionGet();

		return ElasticsearchUtils.convertEsResultToString(response);
	}

	public String getListArticleByStringInTitle(String from, String size, String value, String source, String connectivity)
			throws ParseException, UnknownHostException {
		String path = "";
		String ES_FIELDS = "&_source_exclude=raw_content,canonical";
		if (!connectivity.equals("wifi")) { ES_FIELDS = "&_source=title,time_post,images,source,url,tags"; }
		try {
			path = ProductionConfig.URL_ELASTICSEARCH + "&size=" + size + "&from=" + from + "&sort=time_post:desc"
					+ "&q=display:"+ProductionConfig.STATUS_DISPLAY+" AND title:"
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
		if (response.getStatus() == ProductionConfig.RESPONE_STATAUS_OK) {
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
				this.jc.set(ProductionConfig.REDIS_KEY, input);
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
				this.jc.set(ProductionConfig.REDIS_KEY_IOS, input);
				return ResponseEntity.ok("Update success");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	}

	public ResponseEntity<Object> getHotTags() {
		try {
			String tags = this.jc.get(ProductionConfig.REDIS_KEY);
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
			String tags = this.jc.get(ProductionConfig.REDIS_KEY_IOS);
			if (tags != null) {
				return ResponseEntity.ok(tags);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	}

	public ResponseEntity<Object> getTagsOfEducationCategory(String size, String category_id) throws JSONException {

		SearchRequestBuilder req = this.esClient.prepareSearch("br_article_v4").setTypes("article")
				.setSearchType(SearchType.QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("category.id",category_id)))
				.addAggregation(AggregationBuilders.terms("tags").field("tags").size(Integer.parseInt(size)));

		SearchResponse response = req.execute().actionGet();
		String result = ElasticsearchUtils.convertEsResultAggrsToString(response);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	public String getListVideoArticles(String from, String size, String connectivity) throws ParseException, UnknownHostException {
		SearchRequestBuilder req = this.esClient.prepareSearch("br_article_v4").setTypes("article")
				.setSearchType(SearchType.QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("display", 1))
						.must(QueryBuilders.matchQuery("category.id", 16)));

		if (!connectivity.equals("wifi")) {
			req.setFetchSource(WHITELIST_FIELDS,null);
		}  else {
			req.setFetchSource(null,BLACKLIST_FIELDS);
		}
		SearchResponse response = req.addSort("time_post", SortOrder.DESC)
				.setFrom(Integer.parseInt(from)).setSize(Integer.parseInt(size)).execute().actionGet();

		return ElasticsearchUtils.convertEsResultToString(response);
	}

	public String[] parseHtml(String content) {
		String[] result = new String[0];
		if (content != "" && content.length() > 0) {
			result = content.split("(?<=/p>|/div>|/br>|tr>|td>|thead>|tbody> )");
		}
		return result;
	}
}
