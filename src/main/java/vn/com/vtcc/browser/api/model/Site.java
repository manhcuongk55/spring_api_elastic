package vn.com.vtcc.browser.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by giang on 11/04/2017.
 */
@Entity
@Table(name = "topic_link")
public class Site {

    @Id
    @Column(name = "id")
    private int id;
    @Column(name = "link")
    private String link;
    @Column(name = "name")
    private String name;
    @Column(name = "id_special_topic")
    private int id_special_topic;
    @Column(name = "priority")
    private int priority;
    @Column(name = "logo")
    private String logo;


    public Site() {
        super();
    }


    public Site(int id, String link, String name, String logo, int id_special_topic, int priority) {
        super();
        this.id = id;
        this.link = link;
        this.name = name;
        this.logo = logo;
        this.id_special_topic = id_special_topic;
        this.priority = priority;
    }


    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }


    public String getLink() {
        return link;
    }


    public void setLink(String link) {
        this.link = link;
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

    public int getIdSpecialTopic() {
        return id_special_topic;
    }


    public void setIdSpecialTopic(int id_special_topic) {
        this.id_special_topic = id_special_topic;
    }


    public int getPriority() {
        return priority;
    }


    public void setPriority(int status) {
        this.priority = status;
    }
}
