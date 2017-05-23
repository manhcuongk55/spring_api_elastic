package vn.com.vtcc.browser.api.service;

import com.google.gson.Gson;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import vn.com.vtcc.browser.api.model.Site;
import vn.com.vtcc.browser.api.model.Source;
import vn.com.vtcc.browser.api.utils.HibernateUtils;

import java.util.*;

/**
 * Created by giang on 10/03/2017.
 */
public class SourceService {
    Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
    JedisCluster jc = new JedisCluster(jedisClusterNodes);
    private static String REDIS_SITES = "sites_by_topic";
    private static String REDIS_SITES_BY_CATEGORY = "sites_by_category";

    public SourceService() {
        this.jedisClusterNodes.add(new HostAndPort("192.168.107.201", 3001));
        this.jedisClusterNodes.add(new HostAndPort("192.168.107.202", 3001));
        this.jedisClusterNodes.add(new HostAndPort("192.168.107.203", 3001));
        this.jedisClusterNodes.add(new HostAndPort("192.168.107.204", 3001));
        this.jedisClusterNodes.add(new HostAndPort("192.168.107.205", 3001));
        this.jedisClusterNodes.add(new HostAndPort("192.168.107.206", 3001));
        this.jc = new JedisCluster(this.jedisClusterNodes);

    }

    public String getSourcesFromDatabase(String whitelist_source) {
        String redisName = "SOURCES";
        String sql = "Select e from " + Source.class.getName() + " e " + " where e.status='1' order by e.id";
        if (!whitelist_source.equals("*")) {
            redisName = "SOURCES_IOS";
            sql = "Select e from " + Source.class.getName() + " e " + " where e.status='1' and e.name in (" + whitelist_source + ") order by e.id";
        }

        String strSources = this.jc.get(redisName);
        Gson gson = new Gson();
        if (strSources == null) {
            List<Source> sources = new ArrayList<>();
            SessionFactory factory = HibernateUtils.getSessionFactory();
            Session session = factory.getCurrentSession();
            try {
                session.getTransaction().begin();
                Query<Source> query = session.createQuery(sql);
                sources = query.getResultList();
                strSources = gson.toJson(sources);
                this.jc.set(redisName, strSources);
                this.jc.expire(redisName, 600);
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


    public String suggestSources(String input, String size) {
        String sql = "Select e from " + Site.class.getName() + " e where e.link like '%" + input + "%' order by e.priority ASC ";

        SessionFactory factory = HibernateUtils.getSessionFactory();
        Session session = factory.getCurrentSession();
        String strSites = "";
        Gson gson = new Gson();
        try {
            List<Site> sites = new ArrayList<>();
            session.getTransaction().begin();
            Query<Site> query = session.createQuery(sql);
            query.setMaxResults(Integer.parseInt(size));
            sites = query.getResultList();
            strSites = gson.toJson(sites);
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            session.getTransaction().rollback();
        } finally {
            session.close();
        }
        return strSites;
    }

    public String suggestSourcesByCategory(String categoryId, String size) {
        String sql = "Select e from " + Site.class.getName() +" e where e.id_special_topic = '" + categoryId + "' order by e.priority asc ";
        SessionFactory factory = HibernateUtils.getSessionFactory();
        Session session = factory.getCurrentSession();
        String redisName = REDIS_SITES_BY_CATEGORY + "_" + categoryId;
        String strSites = this.jc.get(redisName);
        Gson gson = new Gson();
        if (strSites == null) {
            try {
                List<Site> sites = new ArrayList<>();
                session.getTransaction().begin();
                Query<Site> query = session.createQuery(sql);
                query.setMaxResults(Integer.parseInt(size));
                sites = query.getResultList();
                strSites = gson.toJson(sites);
                this.jc.set(redisName, strSites);
                this.jc.expire(redisName, 3600);
                session.getTransaction().commit();
            } catch (Exception e) {
                e.printStackTrace();
                session.getTransaction().rollback();
            } finally {
                session.close();
            }
        }
        return strSites;
    }
}
