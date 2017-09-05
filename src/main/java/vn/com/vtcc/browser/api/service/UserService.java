package vn.com.vtcc.browser.api.service;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import vn.com.vtcc.browser.api.Application;
import vn.com.vtcc.browser.api.config.ProductionConfig;
import vn.com.vtcc.browser.api.exception.DataNotFoundException;
import vn.com.vtcc.browser.api.model.User;
import vn.com.vtcc.browser.api.utils.HibernateUtils;

public class UserService {
	Client client = ClientBuilder.newClient().register(JacksonJsonProvider.class);

	/*public  User loginByGoogle(String access_token) {
		User userResponse = new User();
		User userUpdate = new User();
		WebTarget rootTarget = client.target(ProductionConfig.URL_GOOGLE + access_token);
		Response response = rootTarget.request().get(); // Call get method
		if (response.getStatus() == ProductionConfig.RESPONE_STATAUS_OK) {
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

	}*/

	@SuppressWarnings("deprecation")
	public  User getUserFromDatabase(Session session, String googleID, User userUpdate) {
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
