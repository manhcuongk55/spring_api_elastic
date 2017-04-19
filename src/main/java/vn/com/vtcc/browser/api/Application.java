package vn.com.vtcc.browser.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.*;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


@SpringBootApplication
@Configuration
public class Application extends WebMvcConfigurerAdapter {
	public static final int RESPONE_STATAUS_OK = 200;
	public static String ES_SERVER = System.getProperty("es_server");
	public static final String URL_ELASTICSEARCH =  ES_SERVER + "/_search?";
	//public static final String URL_ELASTICSEARCH = "http://192.168.107.231:9200/br_article_v4/article/_search?";
	public static final String ES_INDEX_NAME = "br_article_v4";
	public static final String ES_INDEX_TYPE = "article";
	public static final String URL_GOOGLE = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=";
	public static final String STATUS_DISPLAY = "1";
	//public static final String HOST_NAME = "http://news.safenet.vn/";
	public static final String MEDIA_HOST_NAME = "http://media.sfive.vn/";
	public static final String REDIS_KEY = "HOT_TAGS";
	public static final String REDIS_KEY_IOS = "HOT_TAGS_IOS";
	public static final String USER_AGENT = "Mozilla/5.0 (X11; CrOS x86_64 8172.45.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.64 Safari/537.36";

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
		SpringApplication springApplication = new SpringApplication();
		springApplication.run(Application.class,args);
	}
	
}