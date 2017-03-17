package vn.com.vtcc.browser.api.service;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import vn.com.vtcc.browser.api.model.Category;
import vn.com.vtcc.browser.api.utils.HibernateUtils;

public class CategoryService {
	Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
	JedisCluster jc = new JedisCluster(jedisClusterNodes);

	public CategoryService() {
		this.jedisClusterNodes.add(new HostAndPort("192.168.107.201", 3001));
		this.jedisClusterNodes.add(new HostAndPort("192.168.107.202", 3001));
		this.jedisClusterNodes.add(new HostAndPort("192.168.107.203", 3001));
		this.jedisClusterNodes.add(new HostAndPort("192.168.107.204", 3001));
		this.jedisClusterNodes.add(new HostAndPort("192.168.107.205", 3001));
		this.jedisClusterNodes.add(new HostAndPort("192.168.107.206", 3001));
		this.jc = new JedisCluster(this.jedisClusterNodes);
	}

	public String getCategoryFromDatabase() {
		String strCate = this.jc.get("CATEGORIES");
		Gson gson = new Gson();
		if (strCate == null) {
			List<Category> categories = new ArrayList<>();
			SessionFactory factory = HibernateUtils.getSessionFactory();
			Session session = factory.getCurrentSession();
			try {
				session.getTransaction().begin();
				String sql = "Select e from " + Category.class.getName() + " e " + " order by e.id";
				@SuppressWarnings("unchecked")
				Query<Category> query = session.createQuery(sql);
				categories = query.getResultList();;
				strCate = gson.toJson(categories);
				this.jc.set("CATEGORIES", strCate);
				this.jc.expire("CATEGORIES", 300);
				session.getTransaction().commit();
			} catch (Exception e) {
				e.printStackTrace();
				session.getTransaction().rollback();
			} finally {
				session.close();
			}
		}

		return strCate;
	}
}
