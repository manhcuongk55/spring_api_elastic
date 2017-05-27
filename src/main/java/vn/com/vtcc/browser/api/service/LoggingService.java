package vn.com.vtcc.browser.api.service;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import vn.com.vtcc.browser.api.Application;
import vn.com.vtcc.browser.api.config.ProductionConfig;
import vn.com.vtcc.browser.api.utils.ElasticsearchUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by giang on 20/05/2017.
 */
public class LoggingService {
    private static final String DEVICE_NOTIFICATION_CAT_KEY = "categories";
    private static final String DEVICE_NOTIFICATION_KEY = "device_id";
    private static final String FILTER_TERM = "parameters:\"size:20,from:0\"";
    private static final String START_DATE = "2017-05-17T00:00:00";
    Settings settings = Settings.builder().put("cluster.name", "sfive")
            .put("client.transport.sniff", true).build();
    TransportClient esClient = new PreBuiltTransportClient(settings);
    private String[] esHosts = {""};

    public LoggingService() {
        try {
            if (Application.PRODUCTION_ENV == true) {
                this.esHosts = ProductionConfig.ES_HOST_PRODUCTION;
            } else {
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
                .must(QueryBuilders.termsQuery("function.keyword","postListArticlesByCategor"))
                .must(QueryBuilders.queryStringQuery(FILTER_TERM))
                .must(QueryBuilders.rangeQuery("@timestamp").from(START_DATE));

        SearchRequestBuilder query = this.esClient.prepareSearch("browser_logging_v2")
                .setTypes("logs").setQuery(boolQuery)
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
}
