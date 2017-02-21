package vn.com.vtcc.browser.api.model;

import java.util.List;

public class SortArticle {
	private String id;
    private String title;
    private String source;
    private String url_thumbnail;
    private String category;
    private String time_post;
    private List<String> tags;
    
	public SortArticle() {
		super();
	}
	public SortArticle(String id, String title, String source, String url_thumbnail, String category, String time_post,
			List<String> tags) {
		super();
		this.id = id;
		this.title = title;
		this.source = source;
		this.url_thumbnail = url_thumbnail;
		this.category = category;
		this.time_post = time_post;
		this.tags = tags;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
	public String getUrl_thumbnail() {
		return url_thumbnail;
	}
	public void setUrl_thumbnail(String url_thumbnail) {
		this.url_thumbnail = url_thumbnail;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getTime_post() {
		return time_post;
	}
	public void setTime_post(String time_post) {
		this.time_post = time_post;
	}
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
    
}
