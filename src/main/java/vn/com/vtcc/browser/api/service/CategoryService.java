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
import vn.com.vtcc.browser.api.Application;
import vn.com.vtcc.browser.api.config.ProductionConfig;
import vn.com.vtcc.browser.api.model.Category;
import vn.com.vtcc.browser.api.utils.HibernateUtils;

public class CategoryService {
	Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
	JedisCluster jc = new JedisCluster(jedisClusterNodes);
	private String[] hosts = {""};

	public CategoryService() {
		if (Application.PRODUCTION_ENV == true) {
			this.hosts = ProductionConfig.REDIS_HOST_PRODUCTION;
		} else {
			this.hosts = ProductionConfig.REDIS_HOST_STAGING;
		}

		for (String host : this.hosts) {
			this.jedisClusterNodes.add(new HostAndPort(host, ProductionConfig.REDIS_PORT));
		}
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
				String sql = "Select e from " + Category.class.getName() + " e " + " where e.status='1' order by e.id";
				@SuppressWarnings("unchecked")
				Query<Category> query = session.createQuery(sql);
				categories = query.getResultList();;
				strCate = gson.toJson(categories);
				this.jc.set("CATEGORIES", strCate);
				this.jc.expire("CATEGORIES", 600);
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
