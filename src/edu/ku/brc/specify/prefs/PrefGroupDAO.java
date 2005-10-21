package edu.ku.brc.specify.prefs;

import edu.ku.brc.specify.dbsupport.*;
import edu.ku.brc.specify.exceptions.*;
import org.hibernate.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Projections;
import org.hibernate.FetchMode;
import org.hibernate.cache.*;
import java.util.*;

public class PrefGroupDAO
{

    public PrefGroupDAO()
    {
        HibernateUtil.beginTransaction();
    }
    
    /**
     * 
     * @param itemId
     * @param lock
     * @return
     * @throws ConfigurationException
     */
    public PrefGroup getPrefGroupById(Long itemId, boolean lock) throws ConfigurationException
    {

        Session session = HibernateUtil.getCurrentSession();
        PrefGroup prefPage = null;
        try
        {
            if (lock)
            {
                prefPage = (PrefGroup) session.load(PrefGroup.class, itemId, LockMode.UPGRADE);
            } else
            {
                prefPage = (PrefGroup) session.load(PrefGroup.class, itemId);
            }
        } catch (HibernateException ex)
        {
            throw new ConfigurationException(ex);
        }
        return prefPage;
    }
    
    /**
     * 
     * @return All the PrefGroup items
     * @throws ConfigurationException
     */
    public Collection findAll() throws ConfigurationException
    {

        Collection items = null;
        try
        {
            items = HibernateUtil.getCurrentSession().createCriteria(PrefGroup.class).list();
        } catch (HibernateException ex)
        {
            throw new ConfigurationException(ex);
        }
        return items;
    }

    // ********************************************************** //

    public void makePersistent(PrefGroup item)
            throws ConfigurationException {

        try {
            HibernateUtil.getCurrentSession().saveOrUpdate(item);
        } catch (HibernateException ex) {
            throw new ConfigurationException(ex);
        }
    }

    // ********************************************************** //

    public void makeTransient(PrefGroup item)
            throws ConfigurationException {

        try {
            HibernateUtil.getCurrentSession().delete(item);
        } catch (HibernateException ex) {
            throw new ConfigurationException(ex);
        }
    }

}
