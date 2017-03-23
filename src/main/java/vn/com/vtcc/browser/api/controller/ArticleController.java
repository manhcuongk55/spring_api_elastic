package vn.com.vtcc.browser.api.controller;

import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.com.vtcc.browser.api.config.SiteConfig;
import vn.com.vtcc.browser.api.model.Category;
import vn.com.vtcc.browser.api.service.ArticleService;
import vn.com.vtcc.browser.api.service.CategoryService;

import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.List;

import static com.sun.corba.se.spi.activation.IIOP_CLEAR_TEXT.value;

@RestController
public class ArticleController {

	ArticleService ArticleService = new ArticleService();
	CategoryService CategoryService = new CategoryService();
	@CrossOrigin
	@RequestMapping(value = "/get_article_id", method = RequestMethod.GET, produces = "application/json")
	//@Cacheable(cacheNames="getArticleById", key="#id")
	public String getArticleById(
			@RequestParam(value = "id", defaultValue = "596b8412b2c5fad54a4ee565a37e7baa") String id)
			throws org.json.simple.parser.ParseException {
		return ArticleService.getArticleById(id);
	}
	@CrossOrigin
	@RequestMapping(value = "/get_list_hot_article", method = RequestMethod.GET, produces = "application/json")
	public String getListHotNews(@RequestParam(value = "from", defaultValue = "0") String from,
			@RequestParam(value = "size", defaultValue = "20") String size,
								 @RequestParam(value = "timestamp", defaultValue = "0") String timestamp,
								 @RequestParam(value = "source", defaultValue = "*") String source,
								 @RequestParam(value = "connectivity", defaultValue = "wifi") String connectivity)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		return ArticleService.getListHotArticle(from, size, timestamp, source, connectivity);
	}
	@CrossOrigin
	@RequestMapping(value = "/get_list_article_categoryId", method = RequestMethod.GET, produces = "application/json")
	public String getListArticlesByCategor(@RequestParam(value = "from", defaultValue = "0") String from,
			@RequestParam(value = "size", defaultValue = "20") String size,
			@RequestParam(value = "categoryId", defaultValue = "0") String categoryId,
										   @RequestParam(value = "timestamp", defaultValue = "0") String timestamp,
										   @RequestParam(value = "source", defaultValue = "*") String source,
										   @RequestParam(value = "connectivity", defaultValue = "wifi") String connectivity)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		return ArticleService.getListArticleByCategoryId(from, size, categoryId,timestamp, source, connectivity);
	}
	@CrossOrigin
	@RequestMapping(value = "/get_list_article_categoryName", method = RequestMethod.GET, produces = "application/json")
	public String getListArticlesByCategoryName(@RequestParam(value = "from", defaultValue = "0") String from,
			@RequestParam(value = "size", defaultValue = "20") String size,
			@RequestParam(value = "categoryName", defaultValue = "a") String categoryName,
												@RequestParam(value = "timestamp", defaultValue = "0") String timestamp,
												@RequestParam(value = "source", defaultValue = "*") String source,
												@RequestParam(value = "connectivity", defaultValue = "wifi") String connectivity)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		return ArticleService.getListArticleByCategoryName(from, size, categoryName, timestamp,source, connectivity);
	}
	@CrossOrigin
	@RequestMapping(value = "/get_list_article_tags", method = RequestMethod.GET, produces = "application/json")
	public String getListArticlesByTags(@RequestParam(value = "from", defaultValue = "0") String from,
			@RequestParam(value = "size", defaultValue = "20") String size,
			@RequestParam(value = "tags", defaultValue = "a") String tags,
										@RequestParam(value = "timestamp", defaultValue = "0") String timestamp,
										@RequestParam(value = "source", defaultValue = "*") String source,
										@RequestParam(value = "connectivity", defaultValue = "wifi") String connectivity)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		return ArticleService.getListArticleByTags(from, size, tags,timestamp,source, connectivity);
	}
	@CrossOrigin
	@RequestMapping(value = "/get_list_article_related_tags", method = RequestMethod.GET, produces = "application/json")
	public String getListArticlReleatedTags(@RequestParam(value = "tags", defaultValue = "a") String tags,
			@RequestParam(value = "number", defaultValue = "4") String number,@RequestParam(value = "timestamp", defaultValue = "0") String timestamp,
											@RequestParam(value = "source", defaultValue = "*") String source,
											@RequestParam(value = "connectivity", defaultValue = "wifi") String connectivity)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		return ArticleService.getListArticlReleatedTags(tags, number, timestamp, source, connectivity);
	}
	@CrossOrigin
	@RequestMapping(value = "/get_list_article_tittle", method = RequestMethod.GET, produces = "application/json")
	public String getListArticlSearchByTitle(@RequestParam(value = "from", defaultValue = "0") String from,
			@RequestParam(value = "size", defaultValue = "20") String size,
			@RequestParam(value = "title", defaultValue = "title") String title,
											 @RequestParam(value = "source", defaultValue = "*") String source,
											 @RequestParam(value = "connectivity", defaultValue = "wifi") String connectivity)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		return ArticleService.getListArticleByStringInTitle(from, size, title, source, connectivity);
	}
	@CrossOrigin
	@RequestMapping(value = "/get_list_article_source", method = RequestMethod.GET, produces = "application/json")
	public String getListArticlSearchBySource(@RequestParam(value = "from", defaultValue = "0") String from,
			@RequestParam(value = "size", defaultValue = "20") String size,
			@RequestParam(value = "source", defaultValue = "source") String source,@RequestParam(value = "timestamp", defaultValue = "0") String timestamp,
											  @RequestParam(value = "connectivity", defaultValue = "wifi") String connectivity)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		return ArticleService.getListArticleByStringInSource(from, size, source,timestamp, connectivity);
	}

	@CrossOrigin
	@RequestMapping(value = "/get_list_categories", method = RequestMethod.GET, produces = "application/json")
	public String getListCategories()
			throws org.json.simple.parser.ParseException {
		return CategoryService.getCategoryFromDatabase();
	}
	@CrossOrigin
	@RequestMapping(value = "/get_list_hot_tags", method = RequestMethod.GET, produces = "application/json")
	public String getListHotTags()
			throws org.json.simple.parser.ParseException {
		return ArticleService.getListHotTags();
	}

	@CrossOrigin
	@RequestMapping(value = "/update_hot_tags", method = RequestMethod.POST)
	public ResponseEntity<Object> updateRedisHotTags(@RequestBody String input)
			throws org.json.simple.parser.ParseException {
		return ArticleService.updateRedisHotTags(input);
	}

	@CrossOrigin
	@RequestMapping(value = "/get_hot_tags", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Object> getHotTags()
			throws org.json.simple.parser.ParseException {
		return ArticleService.getHotTags();
	}

	@CrossOrigin
	@RequestMapping(value = "/fallback_image", method = RequestMethod.GET)
	public void getImageFromByteArray(@RequestParam String input, HttpServletResponse response) throws IOException {
		System.out.println("===================> Displaying: " +input);
		URLConnection conn = new URL(input).openConnection();
		conn.setConnectTimeout(5000);
		conn.setReadTimeout(5000);

		InputStream in = conn.getInputStream();
		response.setContentType(MediaType.IMAGE_JPEG_VALUE);
		IOUtils.copy(in, response.getOutputStream());
	}
}