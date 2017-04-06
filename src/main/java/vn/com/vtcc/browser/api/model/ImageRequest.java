package vn.com.vtcc.browser.api.model;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by giang on 05/04/2017.
 */
public class ImageRequest {
    private String[] names;
    private String[] types;
    private MultipartFile[] images;

    public ImageRequest() {

    }

    public String[] getNames() { return this.names;}
    public String[] getTypes() { return this.types;}
    public MultipartFile[] getImages() { return this.images;}
}
