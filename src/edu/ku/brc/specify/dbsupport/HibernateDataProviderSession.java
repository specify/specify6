/**
* Copyright (C) 2006  The University of Kansas
*
* [INSERT KU-APPROVED LICENSE TEXT HERE]
*
*/
/**
 * 
 */
package edu.ku.brc.specify.dbsupport;

import java.util.List;

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
 * @author rods
 *
 * @code_status Alpha
 *
 */
public class HibernateDataProviderSession implements DataProviderSessionIFace
{

    protected Session     session         = null;
    protected Exception   recentException = null;
    protected Transaction transaction     = null;
    /**
     * 
     */
    public HibernateDataProviderSession()
    {
        session = HibernateUtil.getNewSession();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#delete(java.lang.Object)
     */
    public boolean delete(Object dataObj) throws Exception
    {

        session.delete(dataObj);
            

        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#getDataList(java.lang.String)
     */
    public List getDataList(String sqlStr)
    {
        Query query = session.createQuery(sqlStr);
        return query.list();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#getDataList(java.lang.Class)
     */
    public List getDataList(Class clsObject)
    {
        Criteria criteria = session.createCriteria(clsObject);
        return criteria.list();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#getDataList(java.lang.Class, java.lang.String, java.lang.Object)
     */
    public List getDataList(Class clsObject, String fieldName, Object value, DataProviderSessionIFace.CompareType compareType)
    {
        Criteria criteria = session.createCriteria(clsObject);
        criteria.add(compareType == DataProviderSessionIFace.CompareType.Equals ? Expression.eq(fieldName, value) : Restrictions.eq(fieldName, value));
        return criteria.list();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#getDataList(java.lang.Class, java.lang.String, java.lang.Object)
     */
    public List getDataList(Class clsObject, String fieldName, Object value)
    {
        return getDataList(clsObject, fieldName, value, DataProviderSessionIFace.CompareType.Equals);
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
        return session.load(clsObj, id);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#getData(java.lang.String)
     */
    public Object getData(String sqlStr)
    {
        List list = getDataList(sqlStr);
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#evict(java.lang.Class)
     */
    public void evict(Class clsObject)
    {
        session.evict(clsObject);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#evict(java.lang.Object)
     */
    public void evict(Object dataObj)
    {
        session.evict(dataObj);
       
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#placeIntoSession(java.lang.Object)
     */
    public void attach(Object dataObj)
    {
        session.lock(dataObj, LockMode.NONE);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#save(java.lang.Object)
     */
    public boolean save(Object dataObj) throws Exception
    {
        session.save(dataObj);
        
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#saveOrUpdate(java.lang.Object)
     */
    public boolean saveOrUpdate(Object dataObj) throws Exception
    {
        session.saveOrUpdate(dataObj);
        
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#update(java.lang.Object)
     */
    public boolean update(Object dataObj) throws Exception
    {
        session.saveOrUpdate(dataObj);
        
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#beginTransaction()
     */
    public void beginTransaction() throws Exception
    {
        transaction = session.beginTransaction();
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
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#flush()
     */
    public void flush()
    {
        session.flush();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#close()
     */
    public void close()
    {
        session.close();
        session     = null;
        transaction = null;
    }
}
