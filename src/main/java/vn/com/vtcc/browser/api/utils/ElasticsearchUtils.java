package vn.com.vtcc.browser.api.utils;

import com.google.gson.Gson;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import org.json.JSONException;
import org.json.simple.JSONObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by giang on 03/04/2017.
 */
public class ElasticsearchUtils {
    public static String convertEsResultToString(SearchResponse response) {
        Gson gson = new Gson();
        ArrayList<Object> results = new ArrayList<Object>();
        SearchHit[] hits = response.getHits().getHits();
        for (int i = 0; i < hits.length; i++) {
            JSONObject obj = new JSONObject();
            obj.put("_source",hits[i].getSource());
            obj.put("_id", hits[i].getId());
            results.add(obj);
        }

        return gson.toJson(results);
    }

    public static String convertEsResultAggrsToString(SearchResponse response) throws JSONException {
        Gson gson = new Gson();
        ArrayList<Object> results = new ArrayList<Object>();
        Terms terms = response.getAggregations().get("tags");
        Collection<Terms.Bucket> buckets = terms.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            JSONObject obj = new JSONObject();
            obj.put(bucket.getKeyAsString(), bucket.getDocCount());
            results.add(obj);
        }
        return gson.toJson(results);
    }

    public static org.json.JSONObject convertEsResultAggrsToArray(SearchResponse response, String key, String subKey) throws JSONException {
        Terms agg = response.getAggregations().get(key);
        org.json.JSONObject result = new org.json.JSONObject();
        Collection<Terms.Bucket> buckets = agg.getBuckets();

        for (Terms.Bucket bucket : buckets) {
            if (bucket.getDocCount() != 0) {
                Terms terms = bucket.getAggregations().get(subKey);
                Collection<Terms.Bucket> bkts = terms.getBuckets();
                for (Terms.Bucket b : bkts) {
                    if (b.getDocCount() != 0 && !b.getKeyAsString().equals("undefined")) {
                        org.json.JSONObject obj = new org.json.JSONObject();
                        String categoryName = bucket.getKeyAsString().split(",")[2].replace("}","")
                                .replace("\"","");
                        if (!categoryName.equals("categoryId:1")) {
                            obj.put(categoryName, b.getDocCount());
                        }
                        result.append(b.getKeyAsString(),obj);
                    }
                }
            }
        }
        return result;
    }

}
