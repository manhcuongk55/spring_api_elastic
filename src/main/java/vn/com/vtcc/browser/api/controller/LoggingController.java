package vn.com.vtcc.browser.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import vn.com.vtcc.browser.api.service.LoggingService;
import vn.com.vtcc.browser.api.service.SourceService;

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
}
