package vn.com.vtcc.browser.api.controller;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import vn.com.vtcc.browser.api.service.ArticleService;

@RestController
public class ArticleController {

	ArticleService ArticleService = new ArticleService();
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
			@RequestParam(value = "size", defaultValue = "20") String size)
			throws org.json.simple.parser.ParseException {
		return ArticleService.getListHotArticle(from, size);
	}
	@CrossOrigin
	@RequestMapping(value = "/get_list_article_categoryId", method = RequestMethod.GET, produces = "application/json")
	public String getListArticlesByCategor(@RequestParam(value = "from", defaultValue = "0") String from,
			@RequestParam(value = "size", defaultValue = "20") String size,
			@RequestParam(value = "categoryId", defaultValue = "0") String categoryId)
			throws org.json.simple.parser.ParseException {
		return ArticleService.getListArticleByCategoryId(from, size, categoryId);
	}
	@CrossOrigin
	@RequestMapping(value = "/get_list_article_categoryName", method = RequestMethod.GET, produces = "application/json")
	public String getListArticlesByCategoryName(@RequestParam(value = "from", defaultValue = "0") String from,
			@RequestParam(value = "size", defaultValue = "20") String size,
			@RequestParam(value = "categoryName", defaultValue = "a") String categoryName)
			throws org.json.simple.parser.ParseException {
		return ArticleService.getListArticleByCategoryName(from, size, categoryName);
	}
	@CrossOrigin
	@RequestMapping(value = "/get_list_article_tags", method = RequestMethod.GET, produces = "application/json")
	public String getListArticlesByTags(@RequestParam(value = "from", defaultValue = "0") String from,
			@RequestParam(value = "size", defaultValue = "20") String size,
			@RequestParam(value = "tags", defaultValue = "a") String tags)
			throws org.json.simple.parser.ParseException {
		return ArticleService.getListArticleByTags(from, size, tags);
	}
	@CrossOrigin
	@RequestMapping(value = "/get_list_article_related_tags", method = RequestMethod.GET, produces = "application/json")
	public String getListArticlReleatedTags(@RequestParam(value = "tags", defaultValue = "a") String tags,
			@RequestParam(value = "number", defaultValue = "4") String number)
			throws org.json.simple.parser.ParseException {
		return ArticleService.getListArticlReleatedTags(tags, number);
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
			@RequestParam(value = "source", defaultValue = "source") String source)
			throws org.json.simple.parser.ParseException {
		return ArticleService.getListArticleByStringInSource(from, size, source);
	}
	
}