package edu.ku.brc.specify.prefs;

import java.util.Collection;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;

import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.exceptions.ConfigurationException;

public class PreferenceDAO
{

    public PreferenceDAO()
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
    public Preference getPreferenceById(Long itemId, boolean lock) throws ConfigurationException
    {

        Session session = HibernateUtil.getCurrentSession();
        Preference prefPage = null;
        try
        {
            if (lock)
            {
                prefPage = (Preference) session.load(Preference.class, itemId, LockMode.UPGRADE);
            } else
            {
                prefPage = (Preference) session.load(Preference.class, itemId);
            }
        } catch (HibernateException ex)
        {
            throw new ConfigurationException(ex);
        }
        return prefPage;
    }
    
    /**
     * 
     * @return All the Preference items
     * @throws ConfigurationException
     */
    public Collection findAll() throws ConfigurationException
    {

        Collection items = null;
        try
        {
            items = HibernateUtil.getCurrentSession().createCriteria(Preference.class).list();
        } catch (HibernateException ex)
        {
            throw new ConfigurationException(ex);
        }
        return items;
    }

    /**
     * 
     * @param item
     * @throws ConfigurationException
     */
    public void makePersistent(Preference item) throws ConfigurationException 
    {

        try {
            HibernateUtil.getCurrentSession().saveOrUpdate(item);
        } catch (HibernateException ex) {
            throw new ConfigurationException(ex);
        }
    }

    /**
     * 
     * @param item
     * @throws ConfigurationException
     */
    public void makeTransient(Preference item) throws ConfigurationException 
    {

        try {
            HibernateUtil.getCurrentSession().delete(item);
        } catch (HibernateException ex) {
            throw new ConfigurationException(ex);
        }
    }

}
