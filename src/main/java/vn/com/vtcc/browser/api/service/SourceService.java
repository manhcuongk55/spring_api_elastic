package vn.com.vtcc.browser.api.service;

import com.google.gson.Gson;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import vn.com.vtcc.browser.api.model.Category;
import vn.com.vtcc.browser.api.model.Source;
import vn.com.vtcc.browser.api.utils.HibernateUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by giang on 10/03/2017.
 */
public class SourceService {
    Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
    JedisCluster jc = new JedisCluster(jedisClusterNodes);

    public SourceService() {
        this.jedisClusterNodes.add(new HostAndPort("192.168.107.201", 3001));
        this.jedisClusterNodes.add(new HostAndPort("192.168.107.202", 3001));
        this.jedisClusterNodes.add(new HostAndPort("192.168.107.203", 3001));
        this.jedisClusterNodes.add(new HostAndPort("192.168.107.204", 3001));
        this.jedisClusterNodes.add(new HostAndPort("192.168.107.205", 3001));
        this.jedisClusterNodes.add(new HostAndPort("192.168.107.206", 3001));
        this.jc = new JedisCluster(this.jedisClusterNodes);
    }

    public String getSourcesFromDatabase() {
        String strSources = this.jc.get("SOURCES");
        Gson gson = new Gson();
        if (strSources == null) {
            List<Source> sources = new ArrayList<>();
            SessionFactory factory = HibernateUtils.getSessionFactory();
            Session session = factory.getCurrentSession();
            try {
                session.getTransaction().begin();
                String sql = "Select e from " + Source.class.getName() + " e " + " where e.status='1' order by e.id";
                @SuppressWarnings("unchecked")
                Query<Source> query = session.createQuery(sql);
                sources = query.getResultList();
                strSources = gson.toJson(sources);
                this.jc.set("SOURCES", strSources);
                this.jc.expire("SOURCES", 300);
                session.getTransaction().commit();
            } catch (Exception e) {
                e.printStackTrace();
                session.getTransaction().rollback();
            } finally {
                session.close();
            }
        }
        return strSources;
    }
}
