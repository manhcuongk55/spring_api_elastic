package vn.com.vtcc.browser.api.controller;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
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
import vn.com.vtcc.browser.api.utils.TextUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Created by giang on 10/03/2017.
 */
@RestController
public class SourceController {
    SourceService sourceService = new SourceService();
    String GOOGLE_API = "https://www.google.com/search?tbm=isch&q=";

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
        System.setProperty("https.proxyHost", "192.168.10.34");
        System.setProperty("https.proxyPort", "3128");
        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        try {
            String googleUrl = GOOGLE_API + input.replace(",", "") + " logo";
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
}
