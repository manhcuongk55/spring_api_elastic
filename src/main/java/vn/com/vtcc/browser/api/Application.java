package vn.com.vtcc.browser.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.HashMap;
import java.util.Map;

@ComponentScan
@EnableAutoConfiguration
@SpringBootApplication
@EnableCaching
@EnableWebMvc
public class Application extends WebMvcConfigurerAdapter {
	public static final int RESPONE_STATAUS_OK = 200;
	public static String ES_SERVER = System.getProperty("es_server");
	public static final String URL_ELASTICSEARCH =  ES_SERVER + "/_search?";
	//public static final String URL_ELASTICSEARCH = "http://192.168.107.231:9200/br_article_v4/article/_search?";
	public static final String URL_GOOGLE = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=";
	public static final String STATUS_DISPLAY = "1";

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/images/**")
				.addResourceLocations("classpath:/images/");
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
}