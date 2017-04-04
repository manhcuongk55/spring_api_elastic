package vn.com.vtcc.browser.api.utils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by giang on 29/03/2017.
 */
public class UrlUtils {
    public static String convertToURLEscapingIllegalCharacters(String string) throws UnsupportedEncodingException {
        if (string.contains("/")) {
            String[] tokens = string.split("/");
            tokens[tokens.length -1] = URLEncoder.encode(tokens[tokens.length -1].split("\\?")[0], "UTF-8");
            tokens[tokens.length -1] = tokens[tokens.length -1].replace("+","%20");
            string = String.join("/",tokens);
        }
        return string;
    }
}
