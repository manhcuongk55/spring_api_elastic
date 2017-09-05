package vn.com.vtcc.browser.api.service;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder.Item;
import org.elasticsearch.index.query.QueryBuilders;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import vn.com.vtcc.browser.api.Application;
import vn.com.vtcc.browser.api.config.ProductionConfig;
import vn.com.vtcc.browser.api.elasticsearch.ESClient;
import vn.com.vtcc.browser.api.utils.DateTimeUtils;
import vn.com.vtcc.browser.api.utils.ElasticsearchUtils;

@Service
public class ArticleService {
	private static final int TIMESTAMP_DAY_BEFORE = 86400000;
	private static final int CONNECTION_TIMEOUT = 1000;
	private static final String[] BLACKLIST_FIELDS = {"raw_content", "canonical"};
	private static final String[] WHITELIST_FIELDS = {"title","time_post","images","source",
						"url","tags", "id","content","snippet","category","sort"};
	private static final String[] MORE_LIKE_THIS_FIELDS = {"tags", "title", "category"};
	private String[] redisHosts = {""};
	private String[] esHosts = {""};
	Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
	JedisCluster jc = new JedisCluster(jedisClusterNodes);
	TransportClient esClient;

	@Autowired
	public ArticleService(ESClient es_client) {
		try {
			if (Application.PRODUCTION_ENV == true) {
				this.redisHosts = ProductionConfig.REDIS_HOST_PRODUCTION;
			} else {
				this.redisHosts = ProductionConfig.REDIS_HOST_STAGING;
			}
			for (String redisHost : this.redisHosts) {
				this.jedisClusterNodes.add(new HostAndPort(redisHost, ProductionConfig.REDIS_PORT));
			}
			this.esClient = es_client.getClient();
			this.jc = new JedisCluster(this.jedisClusterNodes);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getListHotArticles(int from, int size, Object[] searchAfter, String source, String connectivity)
			throws UnknownHostException, JSONException {
		List<String> sources = new ArrayList<>();
		sources.addAll(Arrays.asList(source.split(",")));
		if (sources.contains("kenh14.vn")) {
			sources.remove("kenh14.vn");
			sources.add("kenh14");
		}
		SearchRequestBuilder req = this.esClient.prepareSearch(ProductionConfig.ES_INDEX_NAME)
							.setTypes(ProductionConfig.ES_INDEX_TYPE).setSearchType(SearchType.QUERY_THEN_FETCH)
							.setQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("display", 1)));
		if (!sources.contains("*")) {
			req.setPostFilter(QueryBuilders.termsQuery("source", sources));
		}
		if (!connectivity.equals("wifi")) {
			req.setFetchSource(WHITELIST_FIELDS,null);
		} else {
			req.setFetchSource(null,BLACKLIST_FIELDS);
		}
		if (searchAfter != null) {
			req.searchAfter(searchAfter);
		} else {
			req.setFrom(from);
		}
		req.addSort("time_post", SortOrder.DESC);

		SearchResponse response = req.setSize(size).execute().actionGet();
		return ElasticsearchUtils.convertEsResultToString(response);
	}

	public String getListArticleByCatId(int from, int size, int categoryId, Object[] searchAfter, String source,
										String connectivity) throws UnknownHostException, JSONException {
		List<String> sources = new ArrayList<>();
		sources.addAll(Arrays.asList(source.split(",")));
		if (sources.contains("kenh14.vn")) {
			sources.remove("kenh14.vn");
			sources.add("kenh14");
		}
		SearchRequestBuilder req = this.esClient.prepareSearch(ProductionConfig.ES_INDEX_NAME).setTypes(ProductionConfig.ES_INDEX_TYPE)
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
		if (searchAfter != null) {
			req.searchAfter(searchAfter);
		} else {
			req.setFrom(from);
		}
		req.addSort("time_post", SortOrder.DESC);

		SearchResponse response = req.setSize(size).execute().actionGet();

		return ElasticsearchUtils.convertEsResultToString(response);
	}


	public String getListArticleByCatName(int from, int size, String categoryName, Object[] searchAfter, String source,
										  String connectivity) throws UnknownHostException, JSONException {
		List<String> sources = new ArrayList<>();
		sources.addAll(Arrays.asList(source.split(",")));
		if (sources.contains("kenh14.vn")) {
			sources.remove("kenh14.vn");
			sources.add("kenh14");
		}

		SearchRequestBuilder req = this.esClient.prepareSearch(ProductionConfig.ES_INDEX_NAME)
				.setTypes(ProductionConfig.ES_INDEX_TYPE)
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
		if (searchAfter != null) {
			req.searchAfter(searchAfter);
		} else {
			req.setFrom(from);
		}
		req.addSort("time_post", SortOrder.DESC);

		SearchResponse response = req.setSize(size).execute().actionGet();
		return ElasticsearchUtils.convertEsResultToString(response);
	}

	public JSONArray getSourceImage(JSONObject input) throws UnknownHostException, JSONException {
		JSONArray results = (JSONArray) input.get("hits");
		if (results != null) {
			for (int i =0; i < results.length(); i++) {
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

	public String getRelatedArticles(String id, int size, String source, String connectivity) throws JSONException {
		if (source.equals("*") || source == null) {
			source = ProductionConfig.WHITELIST_SOURCE_ES;
		}
		List<String> sources = new ArrayList<>();
		sources.addAll(Arrays.asList(source.split(",")));
		if (sources.contains("kenh14.vn")) {
			sources.remove("kenh14.vn");
			sources.add("kenh14");
		}

		Item itemLikeThis = new Item(ProductionConfig.ES_INDEX_NAME, ProductionConfig.ES_INDEX_TYPE, id);
		SearchRequestBuilder req = this.esClient.prepareSearch(ProductionConfig.ES_INDEX_NAME)
				.setTypes(ProductionConfig.ES_INDEX_TYPE)
				.setQuery(QueryBuilders.boolQuery()
						.must(QueryBuilders.moreLikeThisQuery(MORE_LIKE_THIS_FIELDS, new Item[]{itemLikeThis})
								.minTermFreq(ProductionConfig.MIN_TERM_FREQ))
						.filter(QueryBuilders.termQuery("display",ProductionConfig.STATUS_DISPLAY)));

		req.setPostFilter(QueryBuilders.termsQuery("source", sources));

		if (!connectivity.equals("wifi")) {
			req.setFetchSource(WHITELIST_FIELDS,null);
		} else {
			req.setFetchSource(null, BLACKLIST_FIELDS);
		}
		SearchResponse response = req.setFrom(1).setSize(size).execute().actionGet();
		return ElasticsearchUtils.convertEsResultToString(response);
	}

	public String getArticleByID(String id) throws JSONException {
		SearchRequestBuilder req = this.esClient.prepareSearch(ProductionConfig.ES_INDEX_NAME)
				.setTypes(ProductionConfig.ES_INDEX_TYPE)
				.setSearchType(SearchType.QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("id", id)))
				.setFetchSource(new String[] {"content", "title", "images", "snippet", "time_post","source", "tags", "author","url"},
						new String[] {"raw_content", "canonical"});
		SearchResponse response = req.execute().actionGet();
		return ElasticsearchUtils.convertEsResultToString(response);
	}

	public String getArticleFromNotification(String id) throws JSONException {
		if (id.contains("-")) {
			id = id.split("-")[0];
		}
		SearchRequestBuilder req = this.esClient.prepareSearch(ProductionConfig.ES_INDEX_NAME)
				.setTypes(ProductionConfig.ES_INDEX_TYPE)
				.setSearchType(SearchType.QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("id", id)))
				.setFetchSource(new String[] {"id", "category","content", "title", "images", "snippet",
								"time_post","source", "tags", "author", "url"},
						new String[] {"raw_content", "canonical"});
		SearchResponse response = req.execute().actionGet();
		return ElasticsearchUtils.convertEsResultToString(response);
	}

	public String getListArticleByTags(int from, int size, String inputTags, Object[] searchAfter, String source, String connectivity) throws JSONException {
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

		boolQuery.filter(QueryBuilders.termsQuery("source",sources));

		SearchRequestBuilder query = this.esClient.prepareSearch(ProductionConfig.ES_INDEX_NAME)
				.setTypes(ProductionConfig.ES_INDEX_TYPE).setQuery(boolQuery);

		if (!connectivity.equals("wifi")) {
			query.setFetchSource(WHITELIST_FIELDS,null);
		}  else {
			query.setFetchSource(null,BLACKLIST_FIELDS);
		}
		if (searchAfter != null) {
			query.searchAfter(searchAfter);
		} else {
			query.setFrom(from);
		}
		query.addSort("time_post", SortOrder.DESC);
		SearchResponse response = query.setSize(size).execute().actionGet();

		return ElasticsearchUtils.convertEsResultToString(response);
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

	public String getListVideoArticles(int from, int size, String connectivity) throws UnknownHostException, JSONException {
		SearchRequestBuilder req = this.esClient.prepareSearch(ProductionConfig.ES_INDEX_NAME).setTypes(ProductionConfig.ES_INDEX_TYPE)
				.setSearchType(SearchType.QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("display", 1))
						.must(QueryBuilders.matchQuery("category.id", 16)));

		if (!connectivity.equals("wifi")) {
			req.setFetchSource(WHITELIST_FIELDS,null);
		}  else {
			req.setFetchSource(null,BLACKLIST_FIELDS);
		}
		SearchResponse response = req.addSort("time_post", SortOrder.DESC)
				.setFrom(from).setSize(size).execute().actionGet();

		return ElasticsearchUtils.convertEsResultToString(response);
	}

	public String[] parseHtml(String content) {
		String[] result = new String[0];
		if (content != "" && content.length() > 0) {
			result = content.split("(?<=/p>|/div>|/br>|tr>|td>|thead>|tbody> )");
		}
		return result;
	}

	public org.json.JSONArray getTopHotArticles() throws JSONException {
		SearchResponse response = new SearchResponse();
		org.json.JSONArray result = new org.json.JSONArray();
		org.json.JSONArray data = new org.json.JSONArray();
		DateTime dateFrom = DateTimeUtils.getPreviousTime("hour", 1);
		if (dateFrom != null) {
			long from = DateTimeUtils.convertDateTimeToUnixTimestamp(dateFrom);

			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery().must(QueryBuilders.termsQuery("display", "1"))
					.must(QueryBuilders.rangeQuery("time_post").from(from / 1000));
			SearchRequestBuilder query = esClient.prepareSearch(ProductionConfig.ES_INDEX_NAME)
					.setTypes(ProductionConfig.ES_INDEX_TYPE).setQuery(boolQuery)
					.addAggregation(AggregationBuilders.terms("hot_tags").field("tags")
							.subAggregation(AggregationBuilders.topHits("top_article_of_tags").size(10)));
			response = query.setSize(0).execute().actionGet();
			if (response != null) {
				org.json.JSONObject response1 = new org.json.JSONObject(response.toString());
				org.json.JSONObject aggregations = (org.json.JSONObject) response1.get("aggregations");
				org.json.JSONObject results = (org.json.JSONObject) aggregations.get("hot_tags");
				result = results.getJSONArray("buckets");
				for (int i = 0; i < result.length(); i++) {
					org.json.JSONObject obj = result.getJSONObject(i);
					org.json.JSONArray arr = obj.getJSONObject("top_article_of_tags").getJSONObject("hits").getJSONArray("hits");
					for (int j = 0; j < arr.length(); j++) {
						org.json.JSONObject o = new org.json.JSONObject();
						o.put("_source", arr.getJSONObject(j).get("_source"));
						o.put("_id", arr.getJSONObject(j).get("_id"));
						data.put(j, o);
					}
				}
			}

		}
		return data;
	}

	public JSONObject updateLikeCount(String id, String userId) throws ExecutionException, InterruptedException, JSONException {
		JSONObject result = new JSONObject();
		if (!"*".equals(id)) {
			UpdateRequest updateRequest = new UpdateRequest(ProductionConfig.ES_INDEX_NAME,ProductionConfig.ES_INDEX_TYPE,id)
					.script(new Script("ctx._source.likeCount++"));
			if (this.esClient.update(updateRequest).get() != null) {
				result.put("status",200);
				result.put("message","Update like success");
			}
			else {
				result.put("status",400);
				result.put("message","Update like failed!");
			}
		} else {
			result.put("status",400);
			result.put("message","Update like failed!");
		}
		return result;
	}
}
