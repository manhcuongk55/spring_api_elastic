package vn.com.vtcc.browser.api;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.HashMap;
import java.util.Map;


@SpringBootApplication
@ComponentScan(basePackages = {"vn.com"})
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@Configuration
public class Application extends WebMvcConfigurerAdapter {
	public static boolean PRODUCTION_ENV = false;
	public static String ES_CLUSTER_NAME = "vbrowser";
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