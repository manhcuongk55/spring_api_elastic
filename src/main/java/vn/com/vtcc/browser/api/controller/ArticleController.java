package vn.com.vtcc.browser.api.controller;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.DevicePlatform;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import sun.misc.BASE64Decoder;
import vn.com.vtcc.browser.api.service.ArticleService;
import vn.com.vtcc.browser.api.service.CategoryService;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import org.springframework.mobile.device.Device;
import vn.com.vtcc.browser.api.utils.UrlUtils;

@Component
@RestController
public class ArticleController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	ArticleService ArticleService = new ArticleService();
	CategoryService CategoryService = new CategoryService();
	private static final String WHITELIST_SOURCE = "tiin.vn,netnews.vn,moison.vn,songkhoe.vn";

	@CrossOrigin
	@RequestMapping(value = "/get_article_id", method = RequestMethod.GET, produces = "application/json")
	//@Cacheable(cacheNames="getArticleById", key="#id")
	public String getArticleById(
			@RequestParam(value = "id", defaultValue = "596b8412b2c5fad54a4ee565a37e7baa") String id)
			throws org.json.simple.parser.ParseException {
		//return ArticleService.getArticleById(id);
		return ArticleService.getArticleByID(id);
	}

	@RequestMapping(value = "/get_list_hot_article", method = RequestMethod.GET, produces = "application/json")
	public String getListHotNews(@RequestParam(value = "from", defaultValue = "0") String from,
								 @RequestParam(value = "size", defaultValue = "20") String size,
								 @RequestParam(value = "timestamp", defaultValue = "0") String timestamp,
								 @RequestParam(value = "source", defaultValue = WHITELIST_SOURCE) String source,
								 @RequestParam(value = "connectivity", defaultValue = "wifi") String connectivity,
								 @RequestHeader(value="User-Agent") String userAgent)
			throws org.json.simple.parser.ParseException, UnknownHostException {

		if (userAgent.indexOf("Darwin") >= 0) {
			source = source == null | source == "*" ? WHITELIST_SOURCE : source;
		}

		//return ArticleService.getListHotArticle(from, size, timestamp, source, connectivity);
		return ArticleService.getListHotArticles(from, size, timestamp, source, connectivity);
	}

	@RequestMapping(value = "/list_hot_article", method = RequestMethod.POST, produces = "application/json")
	public String postListHotNews(@RequestBody JSONObject input, @RequestHeader(value="User-Agent") String userAgent)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		String from = input.get("from") == null ? "0" : input.get("from").toString();
		String size = input.get("size") == null ? "20" : input.get("size").toString();
		String timestamp = input.get("timestamp") == null ? "0" : input.get("size").toString();
		String source = input.get("source") == null ? "*" : input.get("source").toString();
		if (userAgent.indexOf("Darwin") >= 0) {
			source = input.get("source") == null | source == "*" ? WHITELIST_SOURCE : input.get("source").toString();
		}
		String connectivity = input.get("connectivity") == null ? "wifi" : input.get("connectivity").toString();

		//return ArticleService.getListHotArticle(from, size, timestamp, source, connectivity);
		return ArticleService.getListHotArticles(from, size, timestamp, source, connectivity);
	}

	/* Get articles by IDs */
	@CrossOrigin
	@RequestMapping(value = "/get_list_article_categoryId", method = RequestMethod.GET, produces = "application/json")
	public String getListArticlesByCategor(@RequestParam(value = "from", defaultValue = "0") String from,
										   @RequestParam(value = "size", defaultValue = "20") String size,
										   @RequestParam(value = "categoryId", defaultValue = "0") String categoryId,
										   @RequestParam(value = "timestamp", defaultValue = "0") String timestamp,
										   @RequestParam(value = "source", defaultValue = WHITELIST_SOURCE) String source,
										   @RequestParam(value = "connectivity", defaultValue = "wifi") String connectivity)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		//return ArticleService.getListArticleByCategoryId(from, size, categoryId,timestamp, source, connectivity);
		return ArticleService.getListArticleByCatId(from, size, categoryId,timestamp, source, connectivity);
	}
	@CrossOrigin
	@RequestMapping(value = "/list_article_categoryId", method = RequestMethod.POST, produces = "application/json")
	public String postListArticlesByCategor(@RequestBody JSONObject input , @RequestHeader(value="User-Agent") String userAgent)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		String categoryId = input.get("categoryId") == null ? "1" : input.get("categoryId").toString();
		String from = input.get("from") == null ? "0" : input.get("from").toString();
		String size = input.get("size") == null ? "20" : input.get("size").toString();
		String timestamp = input.get("timestamp") == null ? "0" : input.get("size").toString();
		String source = input.get("source") == null ? "*" : input.get("source").toString();
		if (userAgent.indexOf("Darwin") >= 0) {
			source = input.get("source") == null | source == "*" ? WHITELIST_SOURCE : input.get("source").toString();
		}
		String connectivity = input.get("connectivity") == null ? "wifi" : input.get("size").toString();

		//return ArticleService.getListArticleByCategoryId(from, size, categoryId,timestamp, source, connectivity);
		return ArticleService.getListArticleByCatId(from, size, categoryId,timestamp, source, connectivity);
	}

	/* Get articles by category name */
	@RequestMapping(value = "/list_article_categoryName", method = RequestMethod.POST, produces = "application/json")
	public String postListArticlesByCategoryName(@RequestBody JSONObject input , @RequestHeader(value="User-Agent") String userAgent)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		String categoryName = input.get("categoryName") == null ? "thoi_su" : input.get("categoryName").toString();
		String from = input.get("from") == null ? "0" : input.get("from").toString();
		String size = input.get("size") == null ? "20" : input.get("size").toString();
		String timestamp = input.get("timestamp") == null ? WHITELIST_SOURCE : input.get("size").toString();
		String source = input.get("source") == null ? "*" : input.get("source").toString();
		if (userAgent.indexOf("Darwin") >= 0) {
			source = input.get("source") == null | source == "*" ? WHITELIST_SOURCE : input.get("source").toString();
		}
		String connectivity = input.get("connectivity") == null ? "wifi" : input.get("size").toString();

		//return ArticleService.getListArticleByCategoryName(from, size, categoryName, timestamp,source, connectivity);
		return ArticleService.getListArticleByCatName(from, size, categoryName, timestamp,source, connectivity);
	}

	/* Get articles by tags */
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
	@RequestMapping(value = "/list_article_tags", method = RequestMethod.POST, produces = "application/json")
	public String postListArticlesByTags(@RequestBody JSONObject input)
			throws org.json.simple.parser.ParseException, UnknownHostException {

		String tags = input.get("tags") == null ? "Việt Nam" : input.get("tags").toString();
		String from = input.get("from") == null ? "0" : input.get("from").toString();
		String size = input.get("size") == null ? "20" : input.get("size").toString();
		String timestamp = input.get("timestamp") == null ? "*" : input.get("size").toString();
		String source = input.get("source") == null ? "*" : input.get("source").toString();
		String connectivity = input.get("connectivity") == null ? "wifi" : input.get("size").toString();
		return ArticleService.getListArticleByTags(from, size, tags,timestamp,source, connectivity);
	}

	@RequestMapping(value = "/list_article_related_tags", method = RequestMethod.POST, produces = "application/json")
	public String postListArticlReleatedTags(@RequestBody JSONObject input)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		String tags = input.get("tags") == null ? "Việt Nam" : input.get("tags").toString();
		String number = input.get("number") == null ? "5" : input.get("number").toString();
		String from = input.get("from") == null ? "0" : input.get("from").toString();
		String size = input.get("size") == null ? "20" : input.get("size").toString();
		String timestamp = input.get("timestamp") == null ? "*" : input.get("size").toString();
		String source = input.get("source") == null ? "*" : input.get("source").toString();
		String connectivity = input.get("connectivity") == null ? "wifi" : input.get("size").toString();
		return ArticleService.getListArticleByTags(from, size, tags,timestamp,source, connectivity);
	}

	/* Get articles by title */
	@CrossOrigin
	@RequestMapping(value = "/get_list_article_tittle", method = RequestMethod.GET, produces = "application/json")
	public String getListArticlSearchByTitle(@RequestParam(value = "from", defaultValue = "0") String from,
											 @RequestParam(value = "size", defaultValue = "20") String size,
											 @RequestParam(value = "title", defaultValue = "title") String title,
											 @RequestParam(value = "source", defaultValue = WHITELIST_SOURCE) String source,
											 @RequestParam(value = "connectivity", defaultValue = "wifi") String connectivity)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		return ArticleService.getListArticleByStringInTitle(from, size, title, source, connectivity);
	}
	@RequestMapping(value = "/list_article_tittle", method = RequestMethod.GET, produces = "application/json")
	public String postListArticlSearchByTitle(@RequestBody JSONObject input)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		String title = input.get("title") == null ? "*" : input.get("title").toString();
		String from = input.get("from") == null ? "0" : input.get("from").toString();
		String size = input.get("size") == null ? "20" : input.get("size").toString();
		String source = input.get("source") == null ? WHITELIST_SOURCE : input.get("size").toString();
		String connectivity = input.get("connectivity") == null ? "wifi" : input.get("size").toString();
		return ArticleService.getListArticleByStringInTitle(from, size, title, source, connectivity);
	}

	/* Get article by source */
	@CrossOrigin
	@RequestMapping(value = "/get_list_article_source", method = RequestMethod.GET, produces = "application/json")
	public String getListArticlSearchBySource(@RequestParam(value = "from", defaultValue = "0") String from,
											  @RequestParam(value = "size", defaultValue = "20") String size,
											  @RequestParam(value = "source", defaultValue = WHITELIST_SOURCE) String source,@RequestParam(value = "timestamp", defaultValue = "0") String timestamp,
											  @RequestParam(value = "connectivity", defaultValue = "wifi") String connectivity)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		return ArticleService.getListArticleByStringInSource(from, size, source,timestamp, connectivity);
	}
	@RequestMapping(value = "/list_article_source", method = RequestMethod.GET, produces = "application/json")
	public String postListArticlSearchBySource(@RequestBody JSONObject input)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		String timestamp = input.get("timestamp") == null ? "0" : input.get("timestamp").toString();
		String from = input.get("from") == null ? "0" : input.get("from").toString();
		String size = input.get("size") == null ? "20" : input.get("size").toString();
		String source = input.get("source") == null ? "*" : input.get("size").toString();
		String connectivity = input.get("connectivity") == null ? "wifi" : input.get("size").toString();
		return ArticleService.getListArticleByStringInSource(from, size, source,timestamp, connectivity);
	}

	/* GET LIST OF RELATED ARTICLES */
	@RequestMapping(value = "/related_articles", method = RequestMethod.POST, produces = "application/json")
	public String postListArticlReleated(@RequestBody JSONObject input, @RequestHeader(value="User-Agent") String userAgent)
			throws org.json.simple.parser.ParseException, UnknownHostException {
		String id = input.get("id") == null ? "" : input.get("id").toString();
		String size = input.get("size") == null ? "10" : input.get("size").toString();
		String timestamp = input.get("timestamp") == null ? "*" : input.get("size").toString();
		String connectivity = input.get("connectivity") == null ? "wifi" : input.get("size").toString();
		String source = input.get("source") == null ? "*" : input.get("source").toString();
		if (userAgent.indexOf("Darwin") >= 0) {
			source = input.get("source") == null | source == "*" ? WHITELIST_SOURCE : input.get("source").toString();
		}
		return ArticleService.getRelatedArticles(id, size, timestamp, source, connectivity);
	}

	@CrossOrigin
	@RequestMapping(value = "/get_list_categories", method = RequestMethod.GET, produces = "application/json")
	public String getListCategories()
			throws org.json.simple.parser.ParseException {
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
		DevicePlatform platform = device.getDevicePlatform();
		if (platform.equals(DevicePlatform.IOS)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
		} else {
			return ArticleService.getHotTags();
		}
	}

	@CrossOrigin
	@RequestMapping(value = "/fallback_image", method = RequestMethod.GET)
	public void getImageFromByteArray(@RequestParam String input, HttpServletResponse response) {
		try {
			String parsedUrl = UrlUtils.convertToURLEscapingIllegalCharacters(input);
			if (parsedUrl != null) {
				logger.info("===================> Displaying: " +parsedUrl);
				URLConnection conn = new URL(parsedUrl).openConnection();
				conn.setConnectTimeout(5000);
				conn.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
				conn.setReadTimeout(5000);

				InputStream in = conn.getInputStream();
				response.setContentType(MediaType.IMAGE_JPEG_VALUE);
				IOUtils.copy(in, response.getOutputStream());
			}
		} catch (IOException e) {
			logger.error("================> Their server not returning image: " +input);
		}
	}

	@RequestMapping(value = "/fallback_image", method = RequestMethod.POST)
	public void postImageFromByteArray(@RequestBody String input, HttpServletResponse response) throws IOException {
		System.out.println("===================> Displaying: " +input);
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
	@RequestMapping(value = "/uploadFile", method = RequestMethod.POST, consumes="multipart/form-data")
	public @ResponseBody
	ResponseEntity<Object> uploadFileHandler(@RequestParam("type") String type,
											@RequestParam("name") String name,
											@RequestParam("image") MultipartFile image) {

		if (!image.isEmpty()) {
			try {
				// Creating the directory to store file
				String rootPath = "/media" + File.separator + type + File.separator;
				File dir = new File(rootPath);
				if (!dir.exists()) dir.mkdirs();

				// Create the file on server
				File serverFile = new File(dir.getAbsolutePath() + File.separator + name);
				logger.info("Start storing image: " + serverFile);
				InputStream inputStream = new BufferedInputStream(image.getInputStream());
				FileOutputStream outputStream = new FileOutputStream(serverFile);

				// reads input image from file
				BufferedImage inputImage = ImageIO.read(inputStream);

				// writes to the output image in specified format
				boolean result = ImageIO.write(inputImage, "png", outputStream);
				outputStream.close();
				inputStream.close();

				if (result) {
					return ResponseEntity.status(HttpStatus.OK).body("Upload image success!");
				}
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image!");
			} catch (Exception e) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
			}
		} else {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing image!");
		}
	}

}