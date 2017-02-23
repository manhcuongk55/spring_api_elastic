package vn.com.vtcc.browser.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan
@EnableAutoConfiguration
@SpringBootApplication
@EnableCaching
public class Application {
	public static final int RESPONE_STATAUS_OK = 200;
	public static final String URL_ELASTICSEARCH = "http://192.168.10.34:9200/br_article_v4/article/_search?";
	public static final String URL_GOOGLE = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=";
	public static final String STATUS_DISPLAY = "1";

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
}