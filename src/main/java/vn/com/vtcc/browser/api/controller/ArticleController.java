package vn.com.vtcc.browser.api.controller;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vn.com.vtcc.browser.api.model.Category;
import vn.com.vtcc.browser.api.service.ArticleService;
import vn.com.vtcc.browser.api.service.CategoryService;

import java.sql.Timestamp;
import java.util.List;

@RestController
public class ArticleController {

	ArticleService ArticleService = new ArticleService();
	CategoryService CategoryService = new CategoryService();
	@CrossOrigin
	@RequestMapping(value = "/get_article_id", method = RequestMethod.GET, produces = "application/json")
	@Cacheable(cacheNames="getArticleById", key="#id")
	public String getArticleById(
			@RequestParam(value = "id", defaultValue = "596b8412b2c5fad54a4ee565a37e7baa") String id)
			throws org.json.simple.parser.ParseException {
		return ArticleService.getArticleById(id);
	}
	@CrossOrigin
	@RequestMapping(value = "/get_list_hot_article", method = RequestMethod.GET, produces = "application/json")
	public String getListHotNews(@RequestParam(value = "from", defaultValue = "0") String from,
			@RequestParam(value = "size", defaultValue = "20") String size,
								 @RequestParam(value = "timestamp", defaultValue = "0") String timestamp)
			throws org.json.simple.parser.ParseException {
		return ArticleService.getListHotArticle(from, size, timestamp);
	}
	@CrossOrigin
	@RequestMapping(value = "/get_list_article_categoryId", method = RequestMethod.GET, produces = "application/json")
	public String getListArticlesByCategor(@RequestParam(value = "from", defaultValue = "0") String from,
			@RequestParam(value = "size", defaultValue = "20") String size,
			@RequestParam(value = "categoryId", defaultValue = "0") String categoryId,
										   @RequestParam(value = "timestamp", defaultValue = "0") String timestamp)
			throws org.json.simple.parser.ParseException {
		return ArticleService.getListArticleByCategoryId(from, size, categoryId,timestamp);
	}
	@CrossOrigin
	@RequestMapping(value = "/get_list_article_categoryName", method = RequestMethod.GET, produces = "application/json")
	public String getListArticlesByCategoryName(@RequestParam(value = "from", defaultValue = "0") String from,
			@RequestParam(value = "size", defaultValue = "20") String size,
			@RequestParam(value = "categoryName", defaultValue = "a") String categoryName,
												@RequestParam(value = "timestamp", defaultValue = "0") String timestamp)
			throws org.json.simple.parser.ParseException {
		return ArticleService.getListArticleByCategoryName(from, size, categoryName, timestamp);
	}
	@CrossOrigin
	@RequestMapping(value = "/get_list_article_tags", method = RequestMethod.GET, produces = "application/json")
	public String getListArticlesByTags(@RequestParam(value = "from", defaultValue = "0") String from,
			@RequestParam(value = "size", defaultValue = "20") String size,
			@RequestParam(value = "tags", defaultValue = "a") String tags,
										@RequestParam(value = "timestamp", defaultValue = "0") String timestamp)
			throws org.json.simple.parser.ParseException {
		return ArticleService.getListArticleByTags(from, size, tags,timestamp);
	}
	@CrossOrigin
	@RequestMapping(value = "/get_list_article_related_tags", method = RequestMethod.GET, produces = "application/json")
	public String getListArticlReleatedTags(@RequestParam(value = "tags", defaultValue = "a") String tags,
			@RequestParam(value = "number", defaultValue = "4") String number,@RequestParam(value = "timestamp", defaultValue = "0") String timestamp)
			throws org.json.simple.parser.ParseException {
		return ArticleService.getListArticlReleatedTags(tags, number, timestamp);
	}
	@CrossOrigin
	@RequestMapping(value = "/get_list_article_tittle", method = RequestMethod.GET, produces = "application/json")
	public String getListArticlSearchByTitle(@RequestParam(value = "from", defaultValue = "0") String from,
			@RequestParam(value = "size", defaultValue = "20") String size,
			@RequestParam(value = "title", defaultValue = "title") String title)
			throws org.json.simple.parser.ParseException {
		return ArticleService.getListArticleByStringInTitle(from, size, title);
	}
	@CrossOrigin
	@RequestMapping(value = "/get_list_article_source", method = RequestMethod.GET, produces = "application/json")
	public String getListArticlSearchBySource(@RequestParam(value = "from", defaultValue = "0") String from,
			@RequestParam(value = "size", defaultValue = "20") String size,
			@RequestParam(value = "source", defaultValue = "source") String source,@RequestParam(value = "timestamp", defaultValue = "0") String timestamp)
			throws org.json.simple.parser.ParseException {
		return ArticleService.getListArticleByStringInSource(from, size, source,timestamp);
	}

	@CrossOrigin
	@RequestMapping(value = "/get_list_categories", method = RequestMethod.GET, produces = "application/json")
	public List<Category> getListCategories()
			throws org.json.simple.parser.ParseException {
		return CategoryService.getCategoryFromDatabase();
	}
}