package vn.com.vtcc.browser.api.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;

/**
 * Created by giang on 10/03/2017.
 */
public class TextUtils {
    public static String concat_strings(String[] input) {
        String result = "";
        for (int i = 0; i < input.length; i++) {
            if (i < input.length -1) {
                result += "\"" + input[i] + "\",";
            } else {
                result += "\"" + input[i] + "\"";
            }
        }
        return result;
    }

    public static String findValueInJsonArrayFromKey(JSONArray input, String key) throws JSONException {
        String result = "";
        Gson gson = new Gson();
        for (int i=0; i< input.length(); i++) {
            String json = input.get(i).toString();
            JsonObject jsonObject = gson.fromJson( json, JsonObject.class);
            if (jsonObject.get(key) != null) {
                result = jsonObject.get(key).toString();
            }
        }
        return result;
    }
}
