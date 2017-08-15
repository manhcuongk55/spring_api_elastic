package vn.com.vtcc.browser.api.controller;

import org.apache.commons.io.IOUtils;
import org.hibernate.validator.internal.util.logging.Log;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.DevicePlatform;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import sun.misc.BASE64Decoder;
import vn.com.vtcc.browser.api.Application;
import vn.com.vtcc.browser.api.config.ProductionConfig;
import vn.com.vtcc.browser.api.service.ArticleService;
import vn.com.vtcc.browser.api.service.CategoryService;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.*;

import org.springframework.mobile.device.Device;
import vn.com.vtcc.browser.api.utils.UrlUtils;

@RestController
public class ArticleController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	ArticleService ArticleService = new ArticleService();
	CategoryService CategoryService = new CategoryService();

	@CrossOrigin
	@RequestMapping(value = "/get_article_id", method = RequestMethod.GET, produces = "application/json")
	//@Cacheable(cacheNames="getArticleById", key="#id")
	public String getArticleById(
			@RequestParam(value = "id", defaultValue = "596b8412b2c5fad54a4ee565a37e7baa") String id)
			throws org.json.simple.parser.ParseException {
		//return ArticleService.getArticleById(id);
		return ArticleService.getArticleByID(id);
	}

	@CrossOrigin
	@RequestMapping(value = "/notification_detail", method = RequestMethod.GET, produces = "application/json")
	//@Cacheable(cacheNames="getArticleById", key="#id")
	public String getArticleByNotification(
			@RequestParam(value = "id", defaultValue = "596b8412b2c5fad54a4ee565a37e7baa") String id)
			throws org.json.simple.parser.ParseException {
		//return ArticleService.getArticleById(id);
		return ArticleService.getArticleFromNotification(id);
	}

	@CrossOrigin
	@RequestMapping(value = "/list_hot_article", method = RequestMethod.POST, produces = "application/json")
	public String postListHotNews(@RequestBody JSONObject input, @RequestHeader(value="User-Agent") String userAgent)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		String from = input.get("from") == null ? "0" : input.get("from").toString();
		String size = input.get("size") == null ? "20" : input.get("size").toString();
		Object[] searchAfter = input.get("searchAfter") == null ? null : new Object[] {input.get("searchAfter")};
		String source = input.get("source") == null ? "*" : input.get("source").toString();
		source = source == "*" ? ProductionConfig.WHITELIST_SOURCE_ES : input.get("source").toString();
		//String source = Application.WHITELIST_SOURCE_ES;
		String connectivity = input.get("connectivity") == null ? "wifi" : input.get("connectivity").toString();

		//return ArticleService.getListHotArticle(from, size, timestamp, source, connectivity);
		return ArticleService.getListHotArticles(from, size, searchAfter, source, connectivity);
	}

	/* Get articles by IDs */
	/*@CrossOrigin
	@RequestMapping(value = "/get_list_article_categoryId", method = RequestMethod.GET, produces = "application/json")
	public String getListArticlesByCategor(@RequestParam(value = "from", defaultValue = "0") String from,
										   @RequestParam(value = "size", defaultValue = "20") String size,
										   @RequestParam(value = "categoryId", defaultValue = "0") String categoryId,
										   @RequestParam(value = "timestamp", defaultValue = "0") String timestamp,
										   @RequestParam(value = "source", defaultValue = ProductionConfig.WHITELIST_SOURCE_ES) String source,
										   @RequestParam(value = "connectivity", defaultValue = "wifi") String connectivity)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		return ArticleService.getListArticleByCatId(from, size, categoryId,Integer.parseInt(timestamp), source, connectivity);
	}*/

	@CrossOrigin
	@RequestMapping(value = "/list_article_categoryId", method = RequestMethod.POST, produces = "application/json")
	public String postListArticlesByCategor(@RequestBody JSONObject input , @RequestHeader(value="User-Agent") String userAgent)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		String categoryId = input.get("categoryId") == null ? "1" : input.get("categoryId").toString();
		String from = input.get("from") == null ? "0" : input.get("from").toString();
		String size = input.get("size") == null ? "20" : input.get("size").toString();
		Object[] searchAfter = input.get("searchAfter") == null ? null : new Object[] {input.get("searchAfter")};
		String source = input.get("source") == null ? "*" : input.get("source").toString();
		source = input.get("source") == null | source == "*" ? ProductionConfig.WHITELIST_SOURCE_ES : input.get("source").toString();
		String connectivity = input.get("connectivity") == null ? "wifi" : input.get("size").toString();

		//return ArticleService.getListArticleByCategoryId(from, size, categoryId,timestamp, source, connectivity);
		return ArticleService.getListArticleByCatId(from, size, categoryId,searchAfter, source, connectivity);
	}

	/* Get articles by category name */
	@CrossOrigin
	@RequestMapping(value = "/list_article_categoryName", method = RequestMethod.POST, produces = "application/json")
	public String postListArticlesByCategoryName(@RequestBody JSONObject input , @RequestHeader(value="User-Agent") String userAgent)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		String categoryName = input.get("categoryName") == null ? "thoi_su" : input.get("categoryName").toString();
		String from = input.get("from") == null ? "0" : input.get("from").toString();
		String size = input.get("size") == null ? "20" : input.get("size").toString();
		Object[] searchAfter = input.get("searchAfter") == null ? null : new Object[] {input.get("searchAfter")};
		String source = input.get("source") == null ? "*" : input.get("source").toString();
		source = input.get("source") == null | source == "*" ? ProductionConfig.WHITELIST_SOURCE_ES : input.get("source").toString();
		String connectivity = input.get("connectivity") == null ? "wifi" : input.get("size").toString();

		//return ArticleService.getListArticleByCategoryName(from, size, categoryName, timestamp,source, connectivity);
		return ArticleService.getListArticleByCatName(from, size, categoryName, searchAfter,source, connectivity);
	}

	@CrossOrigin
	@RequestMapping(value = "/list_article_tags", method = RequestMethod.POST, produces = "application/json")
	public String postListArticlesByTags(@RequestBody JSONObject input)
			throws org.json.simple.parser.ParseException, UnknownHostException {

		String tags = input.get("tags") == null ? "Việt Nam" : input.get("tags").toString();
		String from = input.get("from") == null ? "0" : input.get("from").toString();
		String size = input.get("size") == null ? "20" : input.get("size").toString();
		Object[] searchAfter = input.get("searchAfter") == null ? null : new Object[] {input.get("searchAfter")};
		String source = input.get("source") == null ? "*" : input.get("source").toString();
		String connectivity = input.get("connectivity") == null ? "wifi" : input.get("size").toString();
		return ArticleService.getListArticleByTags(from, size, tags,searchAfter,source, connectivity);
	}

	@CrossOrigin
	@RequestMapping(value = "/list_article_related_tags", method = RequestMethod.POST, produces = "application/json")
	public String postListArticleReleatedTags(@RequestBody JSONObject input)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		String tags = input.get("tags") == null ? "Việt Nam" : input.get("tags").toString();
		String number = input.get("number") == null ? "5" : input.get("number").toString();
		String from = input.get("from") == null ? "0" : input.get("from").toString();
		String size = input.get("size") == null ? "20" : input.get("size").toString();
		Object[] searchAfter = input.get("searchAfter") == null ? null : new Object[] {input.get("searchAfter")};
		String source = input.get("source") == null ? "*" : input.get("source").toString();
		String connectivity = input.get("connectivity") == null ? "wifi" : input.get("size").toString();
		return ArticleService.getListArticleByTags(from, size, tags,searchAfter,source, connectivity);
	}

	/* GET LIST OF RELATED ARTICLES */
	@RequestMapping(value = "/related_articles", method = RequestMethod.POST, produces = "application/json")
	public String postListArticleReleated(@RequestBody JSONObject input, @RequestHeader(value="User-Agent") String userAgent)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		String id = input.get("id") == null ? "" : input.get("id").toString();
		String size = input.get("size") == null ? "10" : input.get("size").toString();
		String timestamp = input.get("timestamp") == null ? "*" : input.get("size").toString();
		String connectivity = input.get("connectivity") == null ? "wifi" : input.get("size").toString();
		String source = input.get("source") == null ? "*" : input.get("source").toString();
		source = input.get("source") == null | source == "*" ? ProductionConfig.WHITELIST_SOURCE_ES : input.get("source").toString();

		return ArticleService.getRelatedArticles(id, size, timestamp, source, connectivity);
	}

	/* Get articles by title */
	@CrossOrigin
	@RequestMapping(value = "/get_list_article_tittle", method = RequestMethod.GET, produces = "application/json")
	public String getListArticleSearchByTitle(@RequestParam(value = "from", defaultValue = "0") String from,
											 @RequestParam(value = "size", defaultValue = "20") String size,
											 @RequestParam(value = "title", defaultValue = "title") String title,
											 @RequestParam(value = "source", defaultValue = ProductionConfig.WHITELIST_SOURCE_ES) String source,
											 @RequestParam(value = "connectivity", defaultValue = "wifi") String connectivity)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		return ArticleService.getListArticleByStringInTitle(from, size, title, source, connectivity);
	}

	@CrossOrigin
	@RequestMapping(value = "/list_article_tittle", method = RequestMethod.GET, produces = "application/json")
	public String postListArticleSearchByTitle(@RequestBody JSONObject input)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		String title = input.get("title") == null ? "*" : input.get("title").toString();
		String from = input.get("from") == null ? "0" : input.get("from").toString();
		String size = input.get("size") == null ? "20" : input.get("size").toString();
		String source = input.get("source") == null ? ProductionConfig.WHITELIST_SOURCE_ES : input.get("size").toString();
		String connectivity = input.get("connectivity") == null ? "wifi" : input.get("size").toString();
		return ArticleService.getListArticleByStringInTitle(from, size, title, source, connectivity);
	}

	@CrossOrigin
	@RequestMapping(value = "/get_list_categories", method = RequestMethod.GET, produces = "application/json")
	public String getListCategories(@RequestHeader HttpHeaders headers)
			throws org.json.simple.parser.ParseException {
		//System.out.println(headers);
		logger.info(headers.toString());
		return CategoryService.getCategoryFromDatabase();
	}

	@CrossOrigin
	@RequestMapping(value = "/update_hot_tags", method = RequestMethod.POST)
	public ResponseEntity<Object> updateRedisHotTags(@RequestBody String input)
			throws org.json.simple.parser.ParseException {
		return ArticleService.updateRedisHotTags(input);
	}

	@CrossOrigin
	@RequestMapping(value = "/update_hot_tags_ios", method = RequestMethod.POST)
	public ResponseEntity<Object> updateRedisHotTagsIOS(@RequestBody String input)
			throws org.json.simple.parser.ParseException {
		return ArticleService.updateRedisHotTagsIOS(input);
	}

	@CrossOrigin
	@RequestMapping(value = "/get_hot_tags", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Object> getHotTags(Device device)
			throws org.json.simple.parser.ParseException {

		return ArticleService.getHotTags();

	}

	@CrossOrigin
	@RequestMapping(value = "/fallback_image", method = RequestMethod.GET)
	public void getImageFromByteArray(@RequestParam String input, HttpServletResponse response) {
		try {
			/*System.setProperty("http.proxyHost", "10.240.152.56");
			System.setProperty("http.proxyPort", "1988");*/
			String parsedUrl = UrlUtils.convertToURLEscapingIllegalCharacters(input);
			if (parsedUrl != null) {
				URLConnection conn = new URL(parsedUrl).openConnection();
				conn.setConnectTimeout(5000);
				conn.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
				conn.setReadTimeout(5000);

				InputStream in = conn.getInputStream();
				response.setContentType(MediaType.IMAGE_JPEG_VALUE);
				IOUtils.copy(in, response.getOutputStream());
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("================> Their server not returning image: " +input);
		}
	}

	@CrossOrigin
	@RequestMapping(value = "/fallback_image", method = RequestMethod.POST)
	public void postImageFromByteArray(@RequestBody String input, HttpServletResponse response) throws IOException {
		try {
			response.setContentType(MediaType.IMAGE_JPEG_VALUE);
			if (input.contains("base64")) {
				String[] image_str = input.split(",");
				if (image_str[1] != null) {
					byte[] imageByte;
					BASE64Decoder decoder = new BASE64Decoder();
					imageByte = decoder.decodeBuffer(image_str[1]);
					ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
					IOUtils.copy(bis, response.getOutputStream());
				}
			} else {
				URLConnection conn = new URL(input).openConnection();
				conn.setConnectTimeout(5000);
				conn.setReadTimeout(5000);
				InputStream in = conn.getInputStream();
				IOUtils.copy(in, response.getOutputStream());
			}
		} catch (SocketTimeoutException e) {
			System.out.println("================> Their server not returning image: " +input);
		}
	}

	@CrossOrigin
	@RequestMapping(value = "/list_video_articles", method = RequestMethod.POST, produces = "application/json")
	public String postListVideoArticles(@RequestBody JSONObject input)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		String from = input.get("from") == null ? "0" : input.get("from").toString();
		String size = input.get("size") == null ? "20" : input.get("size").toString();
		String timestamp = input.get("timestamp") == null ? "0" : input.get("size").toString();
		//String source = input.get("source") == null ? "*" : input.get("source").toString();
		String connectivity = input.get("connectivity") == null ? "wifi" : input.get("size").toString();
		//return ArticleService.getListArticleByCategoryId(from, size, categoryId,timestamp, source, connectivity);
		return ArticleService.getListVideoArticles(from, size, connectivity);
	}

	@CrossOrigin
	@RequestMapping(value = "/parse_html", method = RequestMethod.POST)
	public String[] parseHtml(@RequestBody String content)
			throws org.json.simple.parser.ParseException {
		return ArticleService.parseHtml(content);
	}

	@CrossOrigin
	@RequestMapping(value = "/top_hot_articles", method = RequestMethod.GET, produces = "application/json")
	public String getTopHotArticles()
			throws org.json.simple.parser.ParseException, UnknownHostException, JSONException {

		return ArticleService.getTopHotArticles().toString();
	}
}