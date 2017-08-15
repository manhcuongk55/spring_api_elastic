package vn.com.vtcc.browser.api.controller;

import org.elasticsearch.action.search.SearchResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import vn.com.vtcc.browser.api.model.MessageBoxLogRequest;
import vn.com.vtcc.browser.api.service.LoggingService;
import vn.com.vtcc.browser.api.service.SourceService;

import java.net.UnknownHostException;

/**
 * Created by giang on 20/05/2017.
 */
@RestController
public class LoggingController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    LoggingService loggingService = new LoggingService();

    @CrossOrigin
    @RequestMapping(value = "/list_deviceId_with_categories", method = RequestMethod.GET, produces = "application/json")
    public String getListDeviceId(@RequestParam(value = "from" , defaultValue = "0") String from,
                                  @RequestParam (value = "size" , defaultValue = "20") String size)
            throws org.json.simple.parser.ParseException {
        return loggingService.getListDeviceIdsFromAllCategories().toString();
    }

    @CrossOrigin
    @RequestMapping(value = "/list_deviceId_by_category", method = RequestMethod.GET, produces = "application/json")
    public String getListDeviceIdByCategory(@RequestParam (value = "id", defaultValue = "2") String id,
                                            @RequestParam (value = "from" , defaultValue = "0") String from,
                                            @RequestParam (value = "size" , defaultValue = "20") String size)
            throws org.json.simple.parser.ParseException {
        return loggingService.getListDeviceIdsByCategoryId(id, from, size).toString();
    }

    @CrossOrigin
    @RequestMapping(value = "/top_category_of_device", method = RequestMethod.GET)
    public String getListDeviceIdByCategory(@RequestParam (value = "id", defaultValue = "2") String id)
            throws org.json.simple.parser.ParseException, JSONException {
        return loggingService.getTopCategoryOfDevice(id);
    }

    @CrossOrigin
    @RequestMapping(value = "/notification_clicks", method = RequestMethod.GET)
    public String getTotalnotificationclicks(@RequestParam (value = "from", defaultValue = "") String from,
                                                 @RequestParam (value = "to", defaultValue = "") String to,
                                             @RequestParam (value = "device", defaultValue = "") String device)
            throws org.json.simple.parser.ParseException, JSONException {
        return loggingService.getTotalNotificationClicks(from,to,device);
    }

    @CrossOrigin
    @RequestMapping(value = "/notification_clicks_of_article", method = RequestMethod.POST, produces = "application/json")
    public String getTotalnotificationclicksOfArticle(@RequestBody JSONObject data)
            throws org.json.simple.parser.ParseException, JSONException {

        String id = data.get("id") == null ? "6fc88a4c8cc4436880827f90d3047803-20170712144707000"
                : data.get("id").toString();
        String device = data.get("device") == null ? "*" : data.get("device").toString();
        return loggingService.getTotalNotificationClicksOfArticle(id,device);
    }

    @CrossOrigin
    @RequestMapping(value = "/set_log", method = RequestMethod.POST, produces = "application/json")
    public String setLogForMessageBox(@RequestBody MessageBoxLogRequest data)
            throws org.json.simple.parser.ParseException, UnknownHostException, JSONException {
        return loggingService.saveLogForMessageBox(data).toString();
    }

    @RequestMapping(value = "/get_log_by_jobid", method = RequestMethod.GET)
    public String listLogByJobID(@RequestParam(value = "jobId", defaultValue = "*") String jobId,
                                 @RequestParam (value = "device", defaultValue = "*") String device,
                                 @RequestParam (value = "appVersion", defaultValue = "*") String appVersion) {
        SearchResponse results = loggingService.findByJobId(jobId,device,appVersion);
        if (results != null) {
            return results.toString();
        }
        return "";
    }
}
