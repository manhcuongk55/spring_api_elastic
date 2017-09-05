package vn.com.vtcc.browser.api.controller;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import sun.misc.BASE64Decoder;
import vn.com.vtcc.browser.api.config.ProductionConfig;
import vn.com.vtcc.browser.api.model.ApiSearchRequest;
import vn.com.vtcc.browser.api.protobuf.NewsApiProtos;
import vn.com.vtcc.browser.api.service.ArticleService;
import vn.com.vtcc.browser.api.service.CategoryService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.HttpHeaders;
import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import org.springframework.mobile.device.Device;
import vn.com.vtcc.browser.api.service.MediaService;
import vn.com.vtcc.browser.api.service.MultipartFileSender;
import vn.com.vtcc.browser.api.utils.UrlUtils;

@RestController
public class ArticleController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	ArticleService ArticleService;

	@Autowired
	MediaService MediaService;

	CategoryService CategoryService = new CategoryService();

	@CrossOrigin
	@RequestMapping(value = "/get_article_id", method = RequestMethod.GET, produces = "application/json")
	//@Cacheable(cacheNames="getArticleById", key="#id")
	public String getArticleById(
			@RequestParam(value = "id", defaultValue = "596b8412b2c5fad54a4ee565a37e7baa") String id) throws JSONException {
		//return ArticleService.getArticleById(id);
		return ArticleService.getArticleByID(id);
	}

	@CrossOrigin
	@RequestMapping(value = "/notification_detail", method = RequestMethod.GET, produces = "application/json")
	//@Cacheable(cacheNames="getArticleById", key="#id")
	public String getArticleByNotification(
			@RequestParam(value = "id", defaultValue = "596b8412b2c5fad54a4ee565a37e7baa") String id) throws JSONException {
		//return ArticleService.getArticleById(id);
		return ArticleService.getArticleFromNotification(id);
	}

	@CrossOrigin
	@RequestMapping(value = "/list_hot_article", method = RequestMethod.POST, produces = "application/json")
	public String postListHotNews(@RequestBody NewsApiProtos.ApiSearchRequest input)
			throws UnknownHostException, JSONException {
		int from = input.hasFrom() ? input.getFrom() : 0;
		int size = input.hasSize() ? input.getSize() : ProductionConfig.DEFAULT_RESPONSE_SIZE;

		Object[] searchAfter = input.hasSearchAfter() ? new Object[] {input.getSearchAfter()} : null;
		String source = input.hasSource() ? input.getSource() : "*";
		String connectivity = input.hasConnectivity() ? input.getConnectivity() : "wifi";
		source = source == "*" ? ProductionConfig.WHITELIST_SOURCE_ES : source;
		return ArticleService.getListHotArticles(from, size, searchAfter, source, connectivity);
	}

	@CrossOrigin
	@RequestMapping(value = "/list_article_categoryId", method = RequestMethod.POST, produces = "application/json")
	public String postListArticlesByCategor(@RequestBody NewsApiProtos.ApiSearchRequest input)
			throws UnknownHostException, JSONException {
		int categoryId = input.getCategoryId() == 0 ? 1 : input.getCategoryId();
		int from = input.hasFrom() ? input.getFrom() : 0;
		int size = input.hasSize() ? input.getSize() : ProductionConfig.DEFAULT_RESPONSE_SIZE;
		//Object[] searchAfter = input.getSearchAfter() == 0 ? null : new Object[] {input.getSearchAfter()};
		Object[] searchAfter = input.hasSearchAfter() ? new Object[] {input.getSearchAfter()} : null;
		String source = input.hasSource() ? input.getSource() : "*";
		String connectivity = input.hasConnectivity() ? input.getConnectivity() : "wifi";
		source = source == "*" ? ProductionConfig.WHITELIST_SOURCE_ES : source;
		//return ArticleService.getListArticleByCategoryId(from, size, categoryId,timestamp, source, connectivity);
		return ArticleService.getListArticleByCatId(from, size, categoryId,searchAfter, source, connectivity);
	}

	/* Get articles by category name */
	@CrossOrigin
	@RequestMapping(value = "/list_article_categoryName", method = RequestMethod.POST, produces = "application/json")
	public String postListArticlesByCategoryName(@RequestBody NewsApiProtos.ApiSearchRequest input)
			throws UnknownHostException, JSONException {
		String categoryName = input.getCategoryName() == null ? "thoi_su" : input.getCategoryName();
		int from = input.hasFrom() ? input.getFrom() : 0;
		int size = input.hasSize() ? input.getSize() : ProductionConfig.DEFAULT_RESPONSE_SIZE;
		Object[] searchAfter = input.hasSearchAfter() ? new Object[] {input.getSearchAfter()} : null;
		String source = input.hasSource() ? input.getSource() : "*";
		String connectivity = input.hasConnectivity() ? input.getConnectivity() : "wifi";
		source = source == "*" ? ProductionConfig.WHITELIST_SOURCE_ES : source;
		//return ArticleService.getListArticleByCategoryName(from, size, categoryName, timestamp,source, connectivity);
		return ArticleService.getListArticleByCatName(from, size, categoryName, searchAfter,source, connectivity);
	}

	@CrossOrigin
	@RequestMapping(value = "/list_article_tags", method = RequestMethod.POST, produces = "application/json")
	public String postListArticlesByTags(@RequestBody NewsApiProtos.ApiSearchRequest input)
			throws UnknownHostException, JSONException {
		String tags = input.getTags() == null ? "Việt Nam" : input.getTags();
		int from = input.hasFrom() ? input.getFrom() : 0;
		int size = input.hasSize() ? input.getSize() : ProductionConfig.DEFAULT_RESPONSE_SIZE;
		Object[] searchAfter = input.hasSearchAfter() ? new Object[] {input.getSearchAfter()} : null;
		String source = input.hasSource() ? input.getSource() : ProductionConfig.WHITELIST_SOURCE_ES;
		String connectivity = input.hasConnectivity() ? input.getConnectivity() : "wifi";
		return ArticleService.getListArticleByTags(from, size, tags,searchAfter,source, connectivity);
	}

	@CrossOrigin
	@RequestMapping(value = "/list_article_related_tags", method = RequestMethod.POST, produces = "application/json")
	public String postListArticleReleatedTags(@RequestBody NewsApiProtos.ApiSearchRequest input)
			throws UnknownHostException, JSONException {
		String tags = input.getTags() == null ? "Việt Nam" : input.getTags();
		int from = input.hasFrom() ? input.getFrom() : 0;
		int size = input.hasSize() ? input.getSize() : ProductionConfig.DEFAULT_RESPONSE_SIZE;
		Object[] searchAfter = input.hasSearchAfter() ? new Object[] {input.getSearchAfter()} : null;
		String source = input.hasSource() ? input.getSource() : ProductionConfig.WHITELIST_SOURCE_ES;
		String connectivity = input.hasConnectivity() ? input.getConnectivity() : "wifi";
		return ArticleService.getListArticleByTags(from, size, tags,searchAfter,source, connectivity);
	}

	/* GET LIST OF RELATED ARTICLES */
	@RequestMapping(value = "/related_articles", method = RequestMethod.POST, produces = "application/json")
	public String postListArticleReleated(@RequestBody NewsApiProtos.ApiSearchRequest input)
			throws UnknownHostException, JSONException {
		String id = input.getId() == null ? "" : input.getId();
		int size = input.hasSize() ? input.getSize() : ProductionConfig.DEFAULT_RESPONSE_SIZE;
		String connectivity = input.hasConnectivity() ? input.getConnectivity() : "wifi";
		String source = input.hasSource() ? input.getSource() : "*";
		source = source.equals("*") ? ProductionConfig.WHITELIST_SOURCE_ES : source;
		return ArticleService.getRelatedArticles(id, size, source, connectivity);
	}

	@CrossOrigin
	@RequestMapping(value = "/get_list_categories", method = RequestMethod.GET, produces = "application/json")
	public String getListCategories(){
		return CategoryService.getCategoryFromDatabase();
	}

	@CrossOrigin
	@RequestMapping(value = "/update_hot_tags", method = RequestMethod.POST)
	public ResponseEntity<Object> updateRedisHotTags(@RequestBody String input){
		return ArticleService.updateRedisHotTags(input);
	}

	@CrossOrigin
	@RequestMapping(value = "/update_hot_tags_ios", method = RequestMethod.POST)
	public ResponseEntity<Object> updateRedisHotTagsIOS(@RequestBody String input) {
		return ArticleService.updateRedisHotTagsIOS(input);
	}

	@CrossOrigin
	@RequestMapping(value = "/get_hot_tags", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Object> getHotTags(Device device)
	{
		return ArticleService.getHotTags();
	}

	@CrossOrigin
	@RequestMapping(value = "/fallback_image", method = RequestMethod.GET)
	public void getImageFromByteArray(@RequestParam String input, HttpServletResponse response) {
		try {
			/*System.setProperty("http.proxyHost", "10.240.152.56");
			System.setProperty("http.proxyPort", "1988");
			System.setProperty("https.proxyHost", "10.240.152.56");
			System.setProperty("https.proxyPort", "1988");*/
			String parsedUrl = UrlUtils.convertToURLEscapingIllegalCharacters(input);
			if (parsedUrl != null) {
				URLConnection conn = new URL(parsedUrl).openConnection();
				conn.setConnectTimeout(ProductionConfig.REQUEST_TIMEOUT);
				conn.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
				conn.setReadTimeout(ProductionConfig.REQUEST_TIMEOUT);

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
				conn.setConnectTimeout(ProductionConfig.REQUEST_TIMEOUT);
				conn.setReadTimeout(ProductionConfig.REQUEST_TIMEOUT);
				InputStream in = conn.getInputStream();
				IOUtils.copy(in, response.getOutputStream());
			}
		} catch (SocketTimeoutException e) {
			System.out.println("================> Their server not returning image: " +input);
		}
	}

	@CrossOrigin
	@RequestMapping(value = "/list_video_articles", method = RequestMethod.POST, produces = "application/json")
	public String postListVideoArticles(@RequestBody ApiSearchRequest input)
			throws UnknownHostException, JSONException {
		int from = input.getFrom() == 0 ? 0 : input.getFrom();
		int size = input.getSize() == 0 ? 20 : input.getSize();
		String connectivity = input.getConnectivity() == null ? "wifi" : input.getConnectivity();;
		return ArticleService.getListVideoArticles(from, size, connectivity);
	}

	@CrossOrigin
	@RequestMapping(value = "/parse_html", method = RequestMethod.POST)
	public String[] parseHtml(@RequestBody String content) {
		return ArticleService.parseHtml(content);
	}

	@CrossOrigin
	@RequestMapping(value = "/top_hot_articles", method = RequestMethod.GET, produces = "application/json")
	public String getTopHotArticles()
			throws UnknownHostException, JSONException {

		return ArticleService.getTopHotArticles().toString();
	}

	@CrossOrigin
	@RequestMapping(value = "/update_like_count", method = RequestMethod.POST, produces = "application/json")
	public String postUpdateLikesOfArticle(@RequestBody ApiSearchRequest input)
			throws UnknownHostException, JSONException, ExecutionException, InterruptedException {

		String id = input.getId();
		String userId = input.getUserId();
		return ArticleService.updateLikeCount(id,userId).toString();
	}

	@CrossOrigin
	@RequestMapping(value = "/streaming_video", method = RequestMethod.GET)
	public void streamingVideo(@RequestParam String input, HttpServletRequest request, HttpServletResponse response) throws Exception {
		input = input.equals("") || input.equals("1") ? ProductionConfig.EMPTY_STRING : input;
		MediaService.streamingVideo(input,request,response);
	}
}