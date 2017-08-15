package vn.com.vtcc.browser.api.service;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import vn.com.vtcc.browser.api.Application;
import vn.com.vtcc.browser.api.config.ProductionConfig;
import vn.com.vtcc.browser.api.model.MessageBoxLogRequest;
import vn.com.vtcc.browser.api.utils.DateTimeUtils;
import vn.com.vtcc.browser.api.utils.ElasticsearchUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Created by giang on 20/05/2017.
 */
public class LoggingService {
    private static final String DEVICE_NOTIFICATION_CAT_KEY = "categories";
    private static final String DEVICE_NOTIFICATION_KEY = "device_id";
    public static final String LIST_ARTICLE_BY_CATEGORY_FUNCTION = "postListArticlesByCategor";
    public static final String NOTIFICATION_CLICK_FUNCTION = "getArticleByNotification";
    private static final String FILTER_TERM = "parameters:\"size:20,from:0\"";
    private static final String START_DATE = "2017-05-17T00:00:00";
    private static String LOGGING_INDEX_NAME = "browser_logging_v2";
    private static String LOGGING_INDEX_TYPE = "logs";

    private static final String MESSAGE_BOX_INDEX_NAME = "mobile_app_log";
    private static final String MESSAGE_BOX_INDEX_TYPE = "message_box";

    Settings settings = Settings.builder().put("cluster.name", "sfive")
            .put("client.transport.sniff", true).build();
    TransportClient esClient = new PreBuiltTransportClient(settings);
    private String[] esHosts = {""};

    public LoggingService() {
        try {
            if (Application.PRODUCTION_ENV == true) {
                this.LOGGING_INDEX_NAME = "browser_logging_v2";
                this.esHosts = ProductionConfig.ES_HOST_PRODUCTION;
            } else {
                this.LOGGING_INDEX_NAME = "browser_logging_dev";
                this.esHosts = ProductionConfig.ES_HOST_STAGING;
            }
            for (String esHost : this.esHosts) {
                this.esClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esHost),
                        ProductionConfig.ES_TRANSPORT_PORT));
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getListDeviceIdsFromAllCategories() {
        org.json.JSONObject data    = new org.json.JSONObject();
        JSONObject results          = new JSONObject();
        JSONObject rows             = new JSONObject();
        JSONObject metadata         = new JSONObject();
        DateFormat dateFormat       = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.termsQuery("function.keyword",LIST_ARTICLE_BY_CATEGORY_FUNCTION))
                .must(QueryBuilders.queryStringQuery(FILTER_TERM))
                .must(QueryBuilders.rangeQuery("@timestamp").from(START_DATE));

        SearchRequestBuilder query = this.esClient.prepareSearch(LOGGING_INDEX_NAME)
                .setTypes(LOGGING_INDEX_TYPE).setQuery(boolQuery)
                .addAggregation(AggregationBuilders.terms(DEVICE_NOTIFICATION_CAT_KEY).field("parameters.keyword").size(20)
                        .subAggregation(AggregationBuilders.terms(DEVICE_NOTIFICATION_KEY).field("notificationId.keyword").size(1000)));

        SearchResponse response = query.setSize(0).execute().actionGet();
        try {
            data = ElasticsearchUtils.convertEsResultAggrsToArray
                    (response,DEVICE_NOTIFICATION_CAT_KEY,DEVICE_NOTIFICATION_KEY);
            Iterator<?> keys = data.keys();

            /* Process to get max categoryId of each firebase Id */
            while( keys.hasNext() ) {
                String k = (String)keys.next();
                JSONArray arr = (JSONArray) data.get(k);
                long maxValue = 0;
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject val = (JSONObject) arr.get(i);
                    for (Iterator it = val.keys(); it.hasNext(); ) {
                        String keyName = (String) it.next();
                        if (val.getLong(keyName) > maxValue) {
                            JSONObject obj = new JSONObject();
                            obj.put(keyName,val.getLong(keyName));
                            rows.put(k,obj);
                            maxValue = val.getLong(keyName);
                        }
                    }
                }
            }

            metadata.put("date", dateFormat.format(date));
            metadata.put("total", rows.length());
            results.put("metadata",metadata);
            results.put("data",rows);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return results;
    }

    public String getTopCategoryOfDevice(String deviceId) throws JSONException {
        String result = "";
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.termsQuery("function.keyword",LIST_ARTICLE_BY_CATEGORY_FUNCTION))
                .must(QueryBuilders.queryStringQuery(FILTER_TERM))
                .must(QueryBuilders.rangeQuery("@timestamp").from(START_DATE));

        SearchRequestBuilder query = this.esClient.prepareSearch(LOGGING_INDEX_NAME)
                .setTypes(LOGGING_INDEX_TYPE).setQuery(boolQuery)
                .addAggregation(AggregationBuilders.terms(DEVICE_NOTIFICATION_CAT_KEY).field("parameters.keyword").size(1));
        SearchResponse response = query.setSize(0).execute().actionGet();
        result = ElasticsearchUtils.convertEsResultAggrsToString(response,DEVICE_NOTIFICATION_CAT_KEY);
        return result;
    }

    public JSONObject getListDeviceIdsByCategoryId(String id, String from, String size) {
        JSONObject results      = new JSONObject();
        JSONObject metadata     = new JSONObject();
        ArrayList<String> data  = new ArrayList<>();
        String categoryId       = "categoryId:" + id;
        DateFormat dateFormat   = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();

        try {
            JSONObject input = (JSONObject) this.getListDeviceIdsFromAllCategories().get("data");
            int total = 0;
            Iterator<?> keys = input.keys();

            /* Process to group firebase Id to category */
            while (keys.hasNext()){
                String key = (String) keys.next();
                JSONObject obj = (JSONObject) input.get(key);
                if (obj.has(categoryId)) {
                    data.add(key);
                    total++;
                }
            }
            ArrayList<String> res = data.stream()
                    .skip(Integer.parseInt(from))
                    .limit(Integer.parseInt(size))
                    .collect(Collectors.toCollection(ArrayList::new));

            metadata.put("date", dateFormat.format(date));
            metadata.put("name", categoryId);
            metadata.put("total", total);
            metadata.put("size", Integer.parseInt(size));
            metadata.put("from", Integer.parseInt(from));
            results.put("data",res);
            results.put("metadata", metadata);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    public String getTotalNotificationClicks(String from, String to, String device) {
        if (from.equals("")) { from = DateTimeUtils.getPreviousDate(7);}
        if (to.equals("")) { to = DateTimeUtils.getTimeNow("yyyy-MM-dd HH:mm:ss");}

        JSONObject result = new JSONObject();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.termsQuery("function.keyword",NOTIFICATION_CLICK_FUNCTION))
                .must(QueryBuilders.rangeQuery("@timestamp").from(from).to(to));

        if (device.equals("ios")) {
            boolQuery.must(QueryBuilders.wildcardQuery("notificationId.keyword","ios*"));
        } else if (device.equals("android")) {
            boolQuery.mustNot(QueryBuilders.wildcardQuery("notificationId.keyword","ios*"));
        }
        SearchRequestBuilder query = this.esClient.prepareSearch(LOGGING_INDEX_NAME)
                .setTypes(LOGGING_INDEX_TYPE).setQuery(boolQuery)
                .addAggregation(AggregationBuilders.terms("top_devices").field("notificationId.keyword"));
        SearchResponse response = query.setSize(10).execute().actionGet();
        return response.toString();
    }

    public String getTotalNotificationClicksOfArticle(String id, String device) throws JSONException {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.termsQuery("function.keyword",NOTIFICATION_CLICK_FUNCTION))
                .must(QueryBuilders.termsQuery("parameters.keyword","[" + id + "]"));

        if (device.equals("ios")) {
            boolQuery.must(QueryBuilders.wildcardQuery("notificationId.keyword","ios*"));
        } else if (device.equals("android")) {
            boolQuery.mustNot(QueryBuilders.wildcardQuery("notificationId.keyword","ios*"));
        }
        SearchRequestBuilder query = this.esClient.prepareSearch(LOGGING_INDEX_NAME)
                .setTypes(LOGGING_INDEX_TYPE).setQuery(boolQuery)
                .addAggregation(AggregationBuilders.terms("top_devices").field("notificationId.keyword"));
        SearchResponse response = query.setSize(10).execute().actionGet();
        return response.toString();
    }

    public JSONObject saveLogForMessageBox(MessageBoxLogRequest sentMessJobs) {
        JSONObject result = new JSONObject();
        try {
            IndexResponse response = this.esClient.prepareIndex(MESSAGE_BOX_INDEX_NAME, MESSAGE_BOX_INDEX_TYPE)
                    .setSource(jsonBuilder()
                            .startObject()
                            .field("jobId", sentMessJobs.getJobId())
                            .field("idFireBase", sentMessJobs.getIdFireBase())
                            .field("timestamp", new java.util.Date())
                            .field("deviceType", sentMessJobs.getDeviceType())
                            .field("appVersion", sentMessJobs.getAppVersion())
                            .endObject()
                    )
                    .get();
            if (response != null) {
                result.put("status" , "success");
            } else {
                result.put("status" , "failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public SearchResponse findByJobId(String jobId, String device, String appVersion) {
        SearchRequestBuilder req = this.esClient.prepareSearch(MESSAGE_BOX_INDEX_NAME)
                .setTypes(MESSAGE_BOX_INDEX_TYPE)
                .setSearchType(SearchType.QUERY_THEN_FETCH)              // Query
                .setPostFilter(QueryBuilders.termQuery("jobId", jobId));
        if (!device.equals("*")) {
            req.setPostFilter(QueryBuilders.termQuery("deviceType", device));
        }
        if (!appVersion.equals("*")) {
            req.setPostFilter(QueryBuilders.termQuery("appVersion", appVersion));
        }

        SearchResponse response = req.addSort("timestamp", SortOrder.DESC).execute().actionGet();
        return response;
    }
}
