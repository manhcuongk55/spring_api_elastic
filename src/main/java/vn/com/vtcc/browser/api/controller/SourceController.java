package vn.com.vtcc.browser.api.controller;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.MediaType;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DevicePlatform;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Decoder;
import vn.com.vtcc.browser.api.Application;
import vn.com.vtcc.browser.api.model.Source;
import vn.com.vtcc.browser.api.service.SourceService;
import vn.com.vtcc.browser.api.utils.ImageUtils;
import vn.com.vtcc.browser.api.utils.TextUtils;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by giang on 10/03/2017.
 */
@RestController
public class SourceController {
    SourceService sourceService = new SourceService();
    public static String GOOGLE_API_LOGO = "https://www.google.com/search?tbm=isch&q=logo ";
    public static String GOOGLE_API_FAVICON = "https://www.google.com/s2/favicons?domain=";
    public static String LOCAL_FAVICON_API = "https://icons.better-idea.org/allicons.json?url=";
    //public static String LOCAL_FAVICON_API = "http://192.168.107.227:8181/allicons.json?url=";

    @CrossOrigin
    @RequestMapping(value = "/get_sources", method = RequestMethod.GET, produces = "application/json")
    public String getListSources(Device device)
            throws org.json.simple.parser.ParseException {
        String whitelist_sources = "*";
        DevicePlatform platform = device.getDevicePlatform();
        if (platform.equals(DevicePlatform.IOS)) {
            whitelist_sources = "'tiin.vn','netnews.vn','moison.vn','songkhoe.vn'";
        }
        return sourceService.getSourcesFromDatabase(whitelist_sources);
    }

    @CrossOrigin
    @RequestMapping(value = "/find_logo", method = RequestMethod.GET)
    public void findLogo(@RequestParam String input, HttpServletResponse response) {
        String result = "";
        JSONParser parser = new JSONParser();
        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        try {
            String googleUrl = GOOGLE_API_LOGO + input.replace("google.com.vn", "google.com");
            Document doc1 = Jsoup.connect(googleUrl).userAgent(Application.USER_AGENT).timeout(10 * 1000).get();
            String media = doc1.select("div#rg_s div a img").first().attr("name");

            String script = doc1.select("script:containsData(var data=)").first().toString();
            String data = script.substring(script.indexOf('=')+2, script.lastIndexOf("\n,") + 1);
            data = data.replaceAll("(?!^)\\[(?!$)","{");
            data = data.replaceAll("(?!^)\\](?!$)","}");
            data = data.replaceAll("\",\"","\":\"");
            JSONArray json = (JSONArray) parser.parse(data);

            result = TextUtils.findValueInJsonArrayFromKey(json,media);
            if (result != null && result.contains(",")) {
                BASE64Decoder decoder = new BASE64Decoder();
                byte[] decodedBytes = decoder.decodeBuffer(result.substring(result.indexOf(",")+1));
                InputStream in = new ByteArrayInputStream(decodedBytes);
                IOUtils.copy(in, response.getOutputStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*@CrossOrigin
    @RequestMapping(value = "/find_favicon", method = RequestMethod.GET)
    public void findFavicon(@RequestParam String input, HttpServletResponse response) {
        String result = "";
        JSONParser parser = new JSONParser();
        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        try {
            String googleUrl = GOOGLE_API_FAVICON + input;
            URLConnection conn = new URL(googleUrl).openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
            conn.setReadTimeout(5000);

            InputStream in = conn.getInputStream();
            response.setContentType(MediaType.IMAGE_JPEG_VALUE);
            BufferedImage sourceImage = ImageIO.read(in);
            //Image thumbnail = sourceImage.getScaledInstance(100, -1, Image.SCALE_SMOOTH);
            BufferedImage bufferedThumbnail = ImageUtils.getScaledInstance(sourceImage,100,100 ,false);
            //bufferedThumbnail.getGraphics().drawImage(thumbnail, 0, 0, null);
            ImageIO.write(bufferedThumbnail, "png", response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    @CrossOrigin
    @RequestMapping(value = "/find_favicon", method = RequestMethod.GET)
    public void findFavicon(@RequestParam String input, HttpServletResponse response) {
        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        try {
            String googleUrl = LOCAL_FAVICON_API + input;
            JSONObject json = new JSONObject(IOUtils.toString(new URL(googleUrl), Charset.forName("UTF-8")));
            JSONArray array = json.getJSONArray("icons");

            if (array != null && array.length() > 0) {

                JSONObject obj = (JSONObject) array.get(0);
                URLConnection conn = new URL(obj.getString("url")).openConnection();
                conn.setConnectTimeout(3000);
                conn.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
                conn.setReadTimeout(5000);
                InputStream in = conn.getInputStream();
                //BufferedImage sourceImage = ImageIO.read(in);
                IOUtils.copy(in, response.getOutputStream());

                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
