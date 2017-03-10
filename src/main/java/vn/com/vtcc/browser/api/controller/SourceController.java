package vn.com.vtcc.browser.api.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import vn.com.vtcc.browser.api.model.Source;
import vn.com.vtcc.browser.api.service.SourceService;

import java.util.List;

/**
 * Created by giang on 10/03/2017.
 */
@RestController
public class SourceController {
    SourceService sourceService = new SourceService();
    @CrossOrigin
    @RequestMapping(value = "/get_sources", method = RequestMethod.GET, produces = "application/json")
    public List<Source> getListSources()
            throws org.json.simple.parser.ParseException {
        return sourceService.getSourcesFromDatabase();
    }
}
