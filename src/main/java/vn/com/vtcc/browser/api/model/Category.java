package vn.com.vtcc.browser.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "category")
public class Category {

	@Id
	@Column(name = "id")
	private int id;
	@Column(name = "display_name")
	private String display_name;
	@Column(name = "name")
	private String name;
	@Column(name = "image")
	private String image;
	@Column(name = "status")
	private String status;
	
	
	public Category() {
		super();
	}


	public Category(int id, String display_name, String name, String image, String status) {
		super();
		this.id = id;
		this.display_name = display_name;
		this.name = name;
		this.image = image;
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


	public String getImage() {
		return image;
	}


	public void setImage(String image) {
		this.image = image;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}
	
	
	
	
	

}
