package vn.com.vtcc.browser.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by giang on 10/03/2017.
 */
@Entity
@Table(name = "source")
public class Source implements Serializable {

    @Id
    @Column(name = "id")
    private int id;
    @Column(name = "display_name")
    private String display_name;
    @Column(name = "name")
    private String name;
    @Column(name = "logo")
    private String logo;
    @Column(name = "favicon")
    private String favicon;
    @Column(name = "status")
    private String status;


    public Source() {
        super();
    }


    public Source(int id, String display_name, String name, String logo, String favicon, String status) {
        super();
        this.id = id;
        this.display_name = display_name;
        this.name = name;
        this.logo = logo;
        this.favicon = favicon;
        this.status = status;
    }


    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }


    public String getDisplay_name() {
        return display_name;
    }


    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getLogo() {
        return logo;
    }


    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getFavicon() {
        return favicon;
    }


    public void setFavicon(String favicon) {
        this.favicon = favicon;
    }


    public String getStatus() {
        return status;
    }


    public void setStatus(String status) {
        this.status = status;
    }
}
