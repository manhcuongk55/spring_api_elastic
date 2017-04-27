package vn.com.vtcc.browser.api.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

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
            results.add(obj);
        }

        return gson.toJson(results);
    }

    public static String convertEsResultAggrsToString(SearchResponse response) {
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
}
