package vn.com.vtcc.browser.api.controller;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import vn.com.vtcc.browser.api.model.User;
import vn.com.vtcc.browser.api.service.UserService;

@RestController
public class UserController {

	/*UserService userService = new UserService();
	@CrossOrigin
	@RequestMapping(value = "/log_in", method = RequestMethod.GET, produces = "application/json")
	@Cacheable("books")
	public User logInByGoogle(@RequestParam(value = "access_token", defaultValue = "100463254787083192751") String access_token) throws org.json.simple.parser.ParseException, JsonProcessingException {
		User user = userService.loginByGoogle(access_token);
		return user;
	}*/
}
