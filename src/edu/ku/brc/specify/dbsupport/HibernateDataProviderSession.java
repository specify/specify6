/**
* Copyright (C) 2006  The University of Kansas
*
* [INSERT KU-APPROVED LICENSE TEXT HERE]
*
*/
package edu.ku.brc.specify.dbsupport;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Restrictions;

import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.dbsupport.StaleObjectException;

/**
 * This is a wrapper around Hibernate Session so we don't have to pollute our class with Hibernate and we could switch it
 * out for a networked version later.
 * 
 * @author rods
 *
 * @code_status Complete
 *
 */
public class HibernateDataProviderSession implements DataProviderSessionIFace
{
    private static final Logger log = Logger.getLogger(HibernateDataProviderSession.class);
    
    // Used for checking to see if we have any dangling creates without closes
    //protected static      int creates = 0;
    //protected static      int closes  = 0;
    
    protected Session     session         = null;
    protected Exception   recentException = null;
    protected Transaction transaction     = null;
    
    protected List<Object> deleteList     = new Vector<Object>();
    
    /**
     * Creates a new Hibernate Session
     */
    public HibernateDataProviderSession()
    {
        session = HibernateUtil.getNewSession();
        //creates++;
        //log.info(" Creates: "+creates+"  Closes: "+closes+" Dif: "+(creates-closes));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#delete(java.lang.Object)
     */
    public boolean delete(Object dataObj) throws Exception
    {
        if (session != null)
        {
            session.delete(dataObj);
            return false;
        }
        
        log.error("Session was null.");

        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#deleteOnSaveOrUpdate(java.lang.Object)
     */
    public void deleteOnSaveOrUpdate(Object dataObj) throws Exception
    {
        deleteList.add(dataObj);
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#getDataList(java.lang.String)
     */
    public List getDataList(String sqlStr)
    {
        if (session != null)
        {
            Query query = session.createQuery(sqlStr);
            return query.list();
        }
        
        log.error("Session was null.");

        return null;

    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#getDataList(java.lang.Class)
     */
    public List getDataList(Class clsObject)
    {
        if (session != null)
        {
            Criteria criteria = session.createCriteria(clsObject);
            return criteria.list();
        }
        
        log.error("Session was null.");

        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#getDataList(java.lang.Class, java.lang.String, java.lang.Object)
     */
    public List getDataList(Class clsObject, String fieldName, Object value, DataProviderSessionIFace.CompareType compareType)
    {
        if (session != null)
        {
            Criteria criteria = session.createCriteria(clsObject);
            criteria.add(compareType == DataProviderSessionIFace.CompareType.Equals ? Expression.eq(fieldName, value) : Restrictions.eq(fieldName, value));
            return criteria.list();           
        }
        
        log.error("Session was null.");

        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#getDataList(java.lang.Class, java.lang.String, java.lang.Object)
     */
    public List getDataList(Class clsObject, String fieldName, Object value)
    {
        if (session != null)
        {
            return getDataList(clsObject, fieldName, value, DataProviderSessionIFace.CompareType.Equals);
        }
        
        log.error("Session was null.");

        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#getDataList(edu.ku.brc.dbsupport.RecordSetIFace)
     */
    //public List getDataList(RecordSetIFace recordSet) throws Exception
    //{
    //    
    //}
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#load(java.lang.Class, java.lang.Long)
     */
    public Object load(Class clsObj, Long id)
    {
        if (session != null)
        {
            return session.load(clsObj, id);
        }
        
        log.error("Session was null.");

        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#getData(java.lang.String)
     */
    public Object getData(String sqlStr)
    {
        if (session != null)
        {
            List list = getDataList(sqlStr);
            return list != null && list.size() > 0 ? list.get(0) : null;
        }
        
        log.error("Session was null.");

        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#evict(java.lang.Class)
     */
    public void evict(Class clsObject)
    {
        if (session != null)
        {
            session.evict(clsObject);
        } else
        {
            log.error("Session was null.");
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#evict(java.lang.Object)
     */
    public void evict(Object dataObj)
    {
        if (session != null)
        {
            session.evict(dataObj);
        } else
        {
            log.error("Session was null.");
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#placeIntoSession(java.lang.Object)
     */
    public void attach(Object dataObj)
    {
        if (session != null)
        {
            session.lock(dataObj, LockMode.NONE);
            
        } else
        {
            log.error("Session was null.");
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#save(java.lang.Object)
     */
    public boolean save(Object dataObj) throws Exception
    {
        if (session != null)
        {
            deleteObjectFromList();
            session.save(dataObj);
            return true;
        }
        
        log.error("Session was null.");
        
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#saveOrUpdate(java.lang.Object)
     */
    public boolean saveOrUpdate(Object dataObj) throws Exception
    {
        if (session != null)
        {
            deleteObjectFromList();
            session.saveOrUpdate(dataObj);
            return true;
        }
        
        log.error("Session was null.");
        
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#update(java.lang.Object)
     */
    public boolean update(Object dataObj) throws Exception
    {
        if (session != null)
        {
            deleteObjectFromList();
            session.update(dataObj);
            return true;
        }
        
        log.error("Session was null.");
        
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#beginTransaction()
     */
    public void beginTransaction() throws Exception
    {
        if (session != null)
        {
            transaction = session.beginTransaction();
            if (transaction == null)
            {
                log.error("Transaction was null!"); // Throw Exception?
            }
        } else
        {
            log.error("Session was null.");
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#commit()
     */
    public void commit() throws Exception
    {
        if (transaction != null)
        {
            try
            {
                transaction.commit();
                transaction = null;
                
            } catch (StaleObjectStateException soe)
            {
                throw new StaleObjectException(soe);
            }
        } else
        {
            throw new RuntimeException("Transaction was null and shouldn't been.");
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#rollback()
     */
    public void rollback()
    {
        if (transaction != null)
        {
            transaction.rollback();
            
        } else
        {
            throw new RuntimeException("Transaction was null and shouldn't been.");
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#flush()
     */
    public void flush()
    {
        if (session != null)
        {
            session.flush();
            
        } else
        {
            log.error("Session was null.");
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#close()
     */
    public void close()
    {
        //closes++;
        //log.info("*Creates: "+creates+"  Closes: "+closes+" Dif: "+(creates-closes));
        
        if (session != null)
        {
            if (transaction != null)
            {
                transaction.rollback();
                transaction = null;
                throw new RuntimeException("Closing Session with open transaction - rolling it back");
            }
            
            session.close();
            session = null;
        } else
        {
            log.error("Session was null.");
        }
     }
    
    /**
     * Deletes all the object that were marked for delayed deletion.
     */
    protected void deleteObjectFromList()
    {
        for (Object obj : deleteList)
        {
            if (session.contains(obj))
            {
                session.delete(obj);
            }
        }
        deleteList.clear();
    }
}
