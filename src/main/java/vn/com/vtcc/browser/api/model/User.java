package vn.com.vtcc.browser.api.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "tb_user")
public class User {
	
	
	private int id;
	private String token;
	private String username;
	private String avatar_url;
	private String email;
	private String google_id;
	private long last_logged_in_at;
	private long created_at;

	public User() {
		super();
	}

	public User(String token, String avatar_url, String email, String google_id) {
		super();
		this.token = token;
		this.avatar_url = avatar_url;
		this.email = email;
		this.google_id = google_id;
	}

	public User(int id, String token, String username, String avatar_url, String email, String google_id,
			Long last_logged_in_at, Long created_at) {
		super();
		this.id = id;
		this.token = token;
		this.username = username;
		this.avatar_url = avatar_url;
		this.email = email;
		this.google_id = google_id;
		this.last_logged_in_at = last_logged_in_at;
		this.created_at = created_at;
	}
	@Id
	@Column(name = "id")
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(name = "token")
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Column(name = "username", nullable = false)
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Column(name = "avatar_url", nullable = false)
	public String getAvatar_url() {
		return avatar_url;
	}

	public void setAvatar_url(String avatar_url) {
		this.avatar_url = avatar_url;
	}

	@Column(name = "email")
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(name = "google_id")
	public String getGoogle_id() {
		return google_id;
	}

	public void setGoogle_id(String google_id) {
		this.google_id = google_id;
	}

	@Column(name = "last_logged_in_at", nullable = false)
	public long getLast_logged_in_at() {
		return last_logged_in_at;
	}

	public void setLast_logged_in_at(long last_logged_in_at) {
		this.last_logged_in_at = last_logged_in_at;
	}

	@Column(name = "created_at", nullable = false)
	public long getCreated_at() {
		return created_at;
	}

	public void setCreated_at(long created_at) {
		this.created_at = created_at;
	}

}
