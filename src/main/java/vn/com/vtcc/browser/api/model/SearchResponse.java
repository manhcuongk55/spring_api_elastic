package vn.com.vtcc.browser.api.model;

/**
 * Created by giang on 20/06/2017.
 */
public class SearchResponse {
    private String title;
    private String url;
    private long timestamp;
    private int id;


    public SearchResponse () {

    }

    public String getTitle() {return this.title;}
    public void setTitle(String title) {this.title = title;}
    public String getUrl() {return this.url;}
    public void setUrl(String url) {this.url = url;}
    public long getTimestamp() {return this.timestamp;}
    public void setTimestamp(long timestamp) {this.timestamp = timestamp;}
    public int getId() {return this.id;}
    public void setId(int id) {this.id = id;}
}
