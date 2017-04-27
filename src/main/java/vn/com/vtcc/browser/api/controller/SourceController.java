package vn.com.vtcc.browser.api.controller;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DevicePlatform;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;
import vn.com.vtcc.browser.api.Application;
import vn.com.vtcc.browser.api.service.SourceService;
import vn.com.vtcc.browser.api.utils.TextUtils;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

/**
 * Created by giang on 10/03/2017.
 */
@RestController
public class SourceController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
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
        //JSONParser parser = new JSONParser();
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
            //JSONArray json = (JSONArray) parser.parse(data);
            //JSONObject obj = new JSONObject(data);
            JSONArray json = new JSONArray(data);

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

    @CrossOrigin
    @RequestMapping(value = "/suggest_sites", method = RequestMethod.GET)
    public String suggestSources(@RequestParam String input, @RequestParam(value = "size", defaultValue = "5") String size) {
        return sourceService.suggestSources(input,size);
    }

    @CrossOrigin
    @RequestMapping(value = "/suggest_sites_by_category", method = RequestMethod.GET)
    public String suggestSourcesByCategory(@RequestParam(value="size", defaultValue = "6") String size, @RequestParam(value="categoryId", defaultValue = "1") String categoryId ) {
        return sourceService.suggestSourcesByCategory(categoryId,size);
    }

    @CrossOrigin
    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST, consumes="multipart/form-data")
    public @ResponseBody
    ResponseEntity<Object> uploadFileHandler(@RequestParam("type") String type,
                                             @RequestParam("name") String name,
                                             @RequestParam("image") MultipartFile image) throws IOException {

        if (!image.isEmpty()) {
            InputStream inputStream = null;
            FileOutputStream outputStream = null;
            try {
                // Creating the directory to store file
                String rootPath = "/media" + File.separator + type + File.separator;
                File dir = new File(rootPath);
                if (!dir.exists()) dir.mkdirs();

                // Create the file on server
                File serverFile = new File(dir.getAbsolutePath() + File.separator + name);
                logger.info("Start storing image: " + serverFile);
                inputStream = new BufferedInputStream(image.getInputStream());
                outputStream = new FileOutputStream(serverFile);

                // reads input image from file
                BufferedImage inputImage = ImageIO.read(inputStream);

                // writes to the output image in specified format
                boolean result = ImageIO.write(inputImage, "png", outputStream);
                outputStream.close();
                inputStream.close();
                if (result) {
                    return ResponseEntity.status(HttpStatus.OK).body("Upload image success!");
                }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image!");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
            } finally {
                if (inputStream != null) {inputStream.close();}
                if (outputStream != null) {outputStream.close();}
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing image!");
        }
    }

    @CrossOrigin
    @RequestMapping(value = "/uploadMultipleFiles", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity<Object> uploadMultipleFilesHandler(@RequestParam("names") String[] names,
                                                      @RequestParam("types") String[] types,
                                                      @RequestParam("images") MultipartFile[] images) throws IOException {

        FileOutputStream outputStream = null;
        InputStream inputStream = null;
        if (images.length != names.length)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Data not match!");
        for (int i = 0; i < images.length; i++) {
            MultipartFile image = images[i];
            String name = names[i];
            String type = types[i];
            try {
                String rootPath = "/media" + File.separator + type + File.separator;
                File dir = new File(rootPath);
                if (!dir.exists()) dir.mkdirs();
                File serverFile = new File(dir.getAbsolutePath() + File.separator + name);
                logger.info("===================> Start storing image: " + serverFile);
                inputStream = new BufferedInputStream(image.getInputStream());
                outputStream = new FileOutputStream(serverFile);
                // reads input image from file
                BufferedImage inputImage = ImageIO.read(inputStream);
                // writes to the output image in specified format
                boolean result = ImageIO.write(inputImage, "png", outputStream);

            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload images!");
            } finally {
                if (inputStream != null) {inputStream.close();}
                if (outputStream != null) {outputStream.close();}
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body("Upload image success!");
    }
}
