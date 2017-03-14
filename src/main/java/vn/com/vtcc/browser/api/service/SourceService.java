package vn.com.vtcc.browser.api.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import vn.com.vtcc.browser.api.model.Category;
import vn.com.vtcc.browser.api.model.Source;
import vn.com.vtcc.browser.api.utils.HibernateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by giang on 10/03/2017.
 */
public class SourceService {
    public List<Source> getSourcesFromDatabase() {
        List<Source> sources = new ArrayList<>();
        SessionFactory factory = HibernateUtils.getSessionFactory();
        Session session = factory.getCurrentSession();
        try {
            session.getTransaction().begin();
            String sql = "Select e from " + Source.class.getName() + " e " + " where e.status='1' order by e.id";
            @SuppressWarnings("unchecked")
            Query<Source> query = session.createQuery(sql);
            sources = query.getResultList();
            session.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
            session.getTransaction().rollback();
        } finally {
            session.close();
        }
        return sources;
    }
}
