package vn.com.vtcc.browser.api.model;

/**
 * Created by giang on 16/08/2017.
 */
public class ApiSearchRequest {
    int size;
    int from;
    String id;
    String source;
    String connectivity;
    int categoryId;
    String categoryName;
    String tags;
    int number;
    String title;
    String input;
    String userId;
    long searchAfter;

    public ApiSearchRequest () {
    }

    public String getTitle() {return this.title;}
    public void setTitle(String title) {this.title = title;}
    public String getSource() {return this.source;}
    public void setSource(String source) {this.source = source;}
    public String getConnectivity() {return this.connectivity;}
    public void setConnectivity(String connectivity) {this.connectivity = connectivity;}
    public String getId() {return this.id;}
    public void setId(String id) {this.id = id;}
    public int getSize() {return this.size;}
    public void setSize(int size) {this.size = size;}
    public int getFrom() {return this.from;}
    public void setFrom(int from) {this.from = from;}
    public int getCategoryId() {return this.categoryId;}
    public void setCategoryId(int categoryId) {this.categoryId = categoryId;}
    public String getCategoryName() {return this.categoryName;}
    public void setCategoryName(String categoryName) {this.categoryName = categoryName;}
    public String getTags() {return this.tags;}
    public void setTags(String tags) {this.tags = tags;}
    public String getInput() {return this.input;}
    public void setInput(String input) {this.input = input;}
    public String getUserId() {return this.userId;}
    public void setUserId(String userId) {this.userId = userId;}
    public int getNumber() {return this.number;}
    public void setNumber(int number) {this.number = number;}
    public long getSearchAfter() {return this.searchAfter;}
    public void setSearchAfter(long searchAfter) {this.searchAfter = searchAfter;}
}
