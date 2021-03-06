package vn.com.vtcc.browser.api.controller;

import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import vn.com.vtcc.browser.api.Application;
import vn.com.vtcc.browser.api.model.Category;
import vn.com.vtcc.browser.api.service.CategoryService;
@RestController
public class CategoryController {
	CategoryService categoryService = new CategoryService();
	@CrossOrigin
	@RequestMapping(value = "/get_category", method = RequestMethod.GET, produces = "application/json")
	public String getListCategories(){
		return categoryService.getCategoryFromDatabase();
	}
}