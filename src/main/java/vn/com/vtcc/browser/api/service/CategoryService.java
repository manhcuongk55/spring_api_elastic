package vn.com.vtcc.browser.api.service;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import vn.com.vtcc.browser.api.model.Category;
import vn.com.vtcc.browser.api.utils.HibernateUtils;

public class CategoryService {
	
	public  List<Category> getCategoryFromDatabase() {
		List<Category> categories = new ArrayList<>();
		SessionFactory factory = HibernateUtils.getSessionFactory();
		Session session = factory.getCurrentSession();
		try {
			session.getTransaction().begin();
			String sql = "Select e from " + Category.class.getName() + " e " + " order by e.id";
			@SuppressWarnings("unchecked")
			Query<Category> query = session.createQuery(sql);
			categories = query.getResultList();
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			session.getTransaction().rollback();
		}
		return categories;
	}
}
