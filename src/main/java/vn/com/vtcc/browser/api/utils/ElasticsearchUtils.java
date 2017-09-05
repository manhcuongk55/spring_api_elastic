package vn.com.vtcc.browser.api.utils;

import com.google.gson.Gson;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import org.json.JSONException;
import org.json.simple.JSONObject;
import vn.com.vtcc.browser.api.model.Source;
import vn.com.vtcc.browser.api.protobuf.NewsApiProtos;

import java.util.*;

/**
 * Created by giang on 03/04/2017.
 */
public class ElasticsearchUtils {
    public static String convertEsResultToString(SearchResponse response) throws JSONException {
        Gson gson = new Gson();
        ArrayList<Object> results = new ArrayList<>();
        SearchHit[] hits = response.getHits().getHits();
        for (int i = 0; i < hits.length; i++) {
            JSONObject obj = new JSONObject();
            obj.put("_source",hits[i].getSource());
            obj.put("_id", hits[i].getId());
            obj.put("sort", hits[i].getSortValues());
            //results.add(convertESHitsToProto(hits[i].getSource()));
            results.add(obj);
        }
        return gson.toJson(results);
    }

    public static NewsApiProtos.ApiSearchResponse convertESHitsToProto(Map<String,Object> hit) {
        NewsApiProtos.ApiSearchResponse searchResponse = NewsApiProtos.ApiSearchResponse.newBuilder()
                .addSource(
                        NewsApiProtos.ApiSearchResponse.Source.newBuilder()
                                .addTitle(hit.get("title").toString())
                                .addSnippet(hit.get("snippet") == null ? "" : hit.get("snippet").toString())
                                .setRawContent(hit.get("raw_content") == null ? "" : hit.get("raw_content").toString())
                                .setSource(hit.get("source").toString())
                                .setUrl(hit.get("url").toString())
                                .addAuthor(hit.get("author") == null ? "" : hit.get("author").toString())
                                .setContent(hit.get("content").toString())
                                .setDisplay((Integer) hit.get("display"))
                                .setDuplicated((Integer) hit.get("duplicated"))
                                .addImages(hit.get("images").toString())
                                .addTags(hit.get("tags").toString())
                                .setId(hit.get("id").toString())
                                .setTimePost(Float.parseFloat(hit.get("time_post").toString()))
                                .setTimestamp(Float.parseFloat((hit.get("timestamp").toString())))
                                .setViewCount( Integer.parseInt(hit.get("viewCount").toString())))
                .setId(hit.get("id").toString()).build();
        return searchResponse;
    }

    public static String convertEsResultAggrsToString(SearchResponse response, String key) throws JSONException {
        Gson gson = new Gson();
        ArrayList<Object> results = new ArrayList<Object>();
        Terms terms = response.getAggregations().get(key);
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
