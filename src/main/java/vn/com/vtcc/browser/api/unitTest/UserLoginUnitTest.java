package vn.com.vtcc.browser.api.unitTest;


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.hibernate.query.Query;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import vn.com.vtcc.browser.api.Application;
import vn.com.vtcc.browser.api.exception.DataNotFoundException;
import vn.com.vtcc.browser.api.model.User;
import vn.com.vtcc.browser.api.service.UserService;
import vn.com.vtcc.browser.api.utils.HibernateUtils;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class UserLoginUnitTest {
	static Client client = ClientBuilder.newClient().register(JacksonJsonProvider.class);
	static UserService userService = new UserService();

	public static User getUser(Session session, String string) {
		String sql = "Select e from " + User.class.getName() + " e "//
				+ " where e.google_id= :google_id ";
		Query<User> query = session.createQuery(sql);
		query.setParameter("google_id", string);
		return (User) query.getSingleResult();
	}

	public static void main(String[] args) throws ParseException {
		User user = LoginByGoogle(
				"ya29.Glz0A1fJG4KfR35X681Be8VJuf55pOLlDFN8UZg81jSFoIcU3MprRwEFdI9zD4ZgvsSRTzg1zByo62PZn6CM64u7YDHqgcm5fQv0YmFiDwpYgM_P-A1t2fn4LlV2MA");
		System.out.println("ccccccccc" + user.getUsername());

	}

	public static User LoginByGoogle(String access_token) throws ParseException {
		User userResponse = new User();
		User userUpdate = new User();
		WebTarget rootTarget = client.target(Application.URL_GOOGLE + access_token);
		Response response = rootTarget.request().get(); // Call get method
		if (response.getStatus() == Application.RESPONE_STATAUS_OK) {
			JSONParser parser = new JSONParser();
			JSONObject json = new JSONObject();
			json = (JSONObject) parser.parse(response.readEntity(JSONObject.class).toString());
			if (json != null) {
				userUpdate.setGoogle_id(json.get("id").toString());
				userUpdate.setEmail(json.get("email").toString());
				userUpdate.setToken(access_token);
				userUpdate.setUsername(json.get("name").toString());
				userUpdate.setAvatar_url(json.get("picture").toString());
				userUpdate.setCreated_at(System.currentTimeMillis() / 1000);
				userUpdate.setLast_logged_in_at(System.currentTimeMillis() / 1000);
				SessionFactory factory = HibernateUtils.getSessionFactory();
				Session session = factory.getCurrentSession();
				try {
					if (!session.beginTransaction().isActive()) {
						session.beginTransaction().begin();
					}
					userResponse = getUserFromDatabase(session, json.get("id").toString(), userUpdate);
					session.getTransaction().commit();
				} catch (Exception e) {
					e.printStackTrace();
					session.getTransaction().rollback();
				}
			}
		} else {
			throw new DataNotFoundException("user not found");
		}
		return userResponse;

	}

	@SuppressWarnings("deprecation")
	public static User getUserFromDatabase(Session session, String googleID, User userUpdate) {
		User user = new User();
		try {
			String sql = "Select e from " + User.class.getName() + " e " + " where e.google_id= :google_id ";
			@SuppressWarnings("unchecked")
			Query<User> query = session.createQuery(sql);
			query.setParameter("google_id", googleID);
			user = (User)query.uniqueResult();
			if (user == null) {
				session.save(userUpdate);
				return userUpdate;
			} else {
				user.setLast_logged_in_at(System.currentTimeMillis() / 1000);
				session.update(user);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return user;
	}

}
