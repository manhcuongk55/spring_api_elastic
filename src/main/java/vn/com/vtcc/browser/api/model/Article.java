package vn.com.vtcc.browser.api.model;

import java.util.ArrayList;
import java.util.List;
 
public class Article {
    private long id;
    private String snippet;
    private String author;
    private String title;
    private String source;
    private String url_article;
    private String content;
    private String category_id;
    private String category_name;
    private String time_post;
    private String timestamp;
    private String  url_images;
    private String tags;
    
    
	public Article(long l, String content) {
		super();
		this.id = l;
		this.content = content;
	}
	public Article() {
		super();
	}
	public Article(long id, String snippet, String author, String title, String source, String url_article,
			String content, String category_id, String category_name, String time_post, String timestamp,
			String url_images, String tags) {
		super();
		this.id = id;
		this.snippet = snippet;
		this.author = author;
		this.title = title;
		this.source = source;
		this.url_article = url_article;
		this.content = content;
		this.category_id = category_id;
		this.category_name = category_name;
		this.time_post = time_post;
		this.timestamp = timestamp;
		this.url_images = url_images;
		this.tags = tags;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getSnippet() {
		return snippet;
	}
	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getUrl_article() {
		return url_article;
	}
	public void setUrl_article(String url_article) {
		this.url_article = url_article;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getCategory_id() {
		return category_id;
	}
	public void setCategory_id(String category_id) {
		this.category_id = category_id;
	}
	public String getCategory_name() {
		return category_name;
	}
	public void setCategory_name(String category_name) {
		this.category_name = category_name;
	}
	public String getTime_post() {
		return time_post;
	}
	public void setTime_post(String time_post) {
		this.time_post = time_post;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getUrl_images() {
		return url_images;
	}
	public void setUrl_images(String url_images) {
		this.url_images = url_images;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
    
	
}
