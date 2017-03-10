package vn.com.vtcc.browser.api.utils;

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
}
