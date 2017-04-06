package vn.com.vtcc.browser.api.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.json.simple.JSONObject;

import java.util.ArrayList;

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
}
