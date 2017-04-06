package vn.com.vtcc.browser.api;

import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import vn.com.vtcc.browser.api.model.Category;
import vn.com.vtcc.browser.api.model.Source;
import vn.com.vtcc.browser.api.service.CategoryService;
import vn.com.vtcc.browser.api.service.SourceService;

import javax.servlet.MultipartConfigElement;
import java.util.List;

@SpringBootApplication
@ComponentScan
@Configuration
@EnableAutoConfiguration
@EnableCaching
public class Application extends WebMvcConfigurerAdapter {
	public static final int RESPONE_STATAUS_OK = 200;
	public static String ES_SERVER = System.getProperty("es_server");
	public static final String URL_ELASTICSEARCH =  ES_SERVER + "/_search?";
	//public static final String URL_ELASTICSEARCH = "http://192.168.107.231:9200/br_article_v4/article/_search?";
	public static final String URL_GOOGLE = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=";
	public static final String STATUS_DISPLAY = "1";
	//public static final String HOST_NAME = "http://news.safenet.vn/";
	public static final String MEDIA_HOST_NAME = "http://media.sfive.vn/";
	public static final String REDIS_KEY = "HOT_TAGS";
	public static final String REDIS_KEY_IOS = "HOT_TAGS_IOS";

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/site_logos/**")
				.addResourceLocations("/resources/","classpath:/site_logos/");
		registry.addResourceHandler("/source_logos/**")
				.addResourceLocations("/resources/","classpath:/source_logos/");
		registry.addResourceHandler("/source_logos_favicon/**")
				.addResourceLocations("/resources/","classpath:/source_logos_favicon/");
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
}