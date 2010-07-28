/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.dbsupport;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.StaleStateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.JDBCConnectionException;

import edu.ku.brc.af.prefs.AppPreferences;
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
    protected static final Logger log = Logger.getLogger(HibernateDataProviderSession.class);
    
    // Used for checking to see if we have any dangling creates without closes
    protected static int     createsCounts = 0;
    protected static int     closesCounts  = 0;
    protected static boolean SHOW_COUNTS   = AppPreferences.getLocalPrefs().getBoolean("CONN_COUNTS", false);
    
    protected Session     session         = null;
    protected Exception   recentException = null;
    protected Transaction transaction     = null;
    
    protected List<Object> deleteList     = new Vector<Object>();
    
    private static HashSet<Session> sessions = new HashSet<Session>();
    
    /**
     * Creates a new Hibernate Session.
     */
    public HibernateDataProviderSession()
    {
        this.session = HibernateUtil.getNewSession();
        if (SHOW_COUNTS)
        {
            createsCounts++;
            System.err.println("Create - Creates: "+createsCounts+"  Closes: "+closesCounts+" Dif: "+(createsCounts-closesCounts)+"  "+session.hashCode());
            sessions.add(this.session);
            
            if (sessions.size() < 4)
            {
                int cn = 0;
                for (Session ses : sessions)
                {
                    if (cn > 0) System.err.print(",");
                    cn++;
                    System.err.print(ses.hashCode());
                    if (cn %20 == 0) System.err.println();
                }
                System.err.println();
            }
        }
    }
    
    public HibernateDataProviderSession(final Session session)
    {
        if (session != null)
        {
            // this constructor doesn't track opens and closes since they are done outside of this class, in this case
            this.session = session;
            return;
        }
        
        // dupilicate of HibernateDataProviderSession() code
        this.session = HibernateUtil.getNewSession();
        if (SHOW_COUNTS)
        {
            createsCounts++;
            //System.err.println("Aquire - Creates: "+createsCounts+"  Closes: "+closesCounts+" Dif: "+(createsCounts-closesCounts)+"  "+session.hashCode());
            sessions.add(this.session);
        }
    }
    
    /**
     * Returns a native hibernate session.
     * @return a native hibernate session.
     */
    public Session getSession()
    {
        return session;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#isOpen()
     */
    public boolean isOpen()
    {
        return session != null && session.isOpen();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#refresh(java.lang.Object)
     */
    public boolean refresh(final Object dataObj)
    {
        if (session != null)
        {
            log.debug(session.hashCode());
            session.refresh(dataObj);
 
            return true;
        }
        
        log.error("Session was null.", new NullPointerException("Session was null"));

        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#delete(java.lang.Object)
     */
    public boolean delete(final Object dataObj) throws Exception
    {
        if (session != null)
        {
            //log.debug(session.hashCode());
            session.delete(dataObj);
 
            return true;
        }
        
        log.error("Session was null.", new NullPointerException("Session was null"));

        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#delete(java.lang.String)
     */
    @Override
    public boolean deleteHQL(String hql) throws Exception
    {
        if (session != null)
        {
            Query query = session.createQuery(hql);
            int row = query.executeUpdate();
            if (row == 0)
            {
              log.info("no rows deleted");
              return false;
            }
            //else
            log.info("Deleted Rows: " + row);
            
            return true;
        }
        
        log.error("Session was null.", new NullPointerException("Session was null"));

        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#deleteOnSaveOrUpdate(java.lang.Object)
     */
    public void deleteOnSaveOrUpdate(final Object dataObj)
    {
        deleteList.add(dataObj);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#merge(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public <T> T merge(final T dataObj) throws StaleObjectException
    {
        if (session != null)
        {
            T mergedObj = null;
            try
            {
                mergedObj = (T)session.merge(dataObj);
            }
            catch (StaleStateException sse)
            {
                throw new StaleObjectException(sse);
            }
            return mergedObj;
        }
        log.error("Session was null.", new NullPointerException("Session was null"));
        return null;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#getDataList(java.lang.String)
     */
    public List<?> getDataList(final String sqlStr)
    {
        if (session != null)
        {
            Query query = session.createQuery(sqlStr);
            return query.list();
        }
        
        log.error("Session was null.", new NullPointerException("Session was null"));

        return null;

    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#getDataList(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getDataList(final Class<T> clazz)
    {
        if (session != null)
        {
            Query q = session.createQuery("FROM " + clazz.getName());
            return q.list();

            // this is the old method, which returns multiple copies of any result that
            // has any EAGER loaded relationships
            // Criteria criteria = session.createCriteria(clazz);
            // return criteria.list();
        }
        
        log.error("Session was null.", new NullPointerException("Session was null"));

        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#getDataList(java.lang.Class, java.lang.String, boolean)
     */
    @SuppressWarnings("unchecked")
    public <T> List<T>  getDataList(final Class<T> clsObject, final String fieldName, final boolean isDistinct)
    {
        Query query = session.createQuery("SELECT DISTINCT " + fieldName + " FROM " + clsObject.getName() + " WHERE " + fieldName + " <> NULL");
        return query.list();
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#getDataList(java.lang.Class, java.lang.String, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getDataList(final Class<T> clsObject, final String fieldName, final Object value, final DataProviderSessionIFace.CompareType compareType)
    {
        if (session != null)
        {
            Criteria criteria = session.createCriteria(clsObject);
            criteria.add(compareType == DataProviderSessionIFace.CompareType.Equals ? Restrictions.eq(fieldName, value) : Restrictions.eq(fieldName, value));
            return criteria.list();           
        }
        
        log.error("Session was null.", new NullPointerException("Session was null"));

        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#getData(java.lang.Class, java.lang.String, java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace.CompareType)
     */
    @SuppressWarnings("unchecked")
    public <T> T getData(final Class<T> clsObject, final String fieldName, final Object value, final DataProviderSessionIFace.CompareType compareType)
    {
        if (session != null)
        {
            Criteria criteria = session.createCriteria(clsObject);
            criteria.add(compareType == DataProviderSessionIFace.CompareType.Equals ? Restrictions.eq(fieldName, value) : Restrictions.eq(fieldName, value));
            List<T> list = criteria.list();
            return list == null || list.size() == 0 ? null : list.get(0);
        }
        
        log.error("Session was null.", new NullPointerException("Session was null"));

        return null;
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#getDataList(java.lang.Class, java.lang.String, java.lang.Object)
     */
    public <T> List<T> getDataList(final Class<T> clsObject, final String fieldName, final Object value)
    {
        if (session != null)
        {
            return getDataList(clsObject, fieldName, value, DataProviderSessionIFace.CompareType.Equals);
        }
        
        log.error("Session was null.", new NullPointerException("Session was null"));

        return null;
    }
    
    @SuppressWarnings("deprecation")
    protected void checkAndReconnect()
    {
        boolean reconnect = false;
        Statement  stmt = null;
        ResultSet  rs   = null;
       try
        {
            Connection conn = session.connection(); // not sure ehat method to use
            stmt = conn.createStatement();
            if (stmt != null)
            {
                rs   = stmt.executeQuery("select * from specifyuser");
                if (rs != null)
                {
                    rs.next();
                }
            }
             
        } catch (SQLException ex)
        {
            reconnect = true;
            
        } catch (JDBCConnectionException ex)
        {
            reconnect = true;
        } finally
        {
            try
            {
                if (rs != null)
                {
                    rs.close();
                }
                if (stmt != null)
                {
                    stmt.close();
                }
                
            } catch (Exception ex)
            {
                //ignore
            }
        }
        
        if (reconnect)
        {
            log.debug("Reconnecting and rebuilding session factory....");
            if (session != null && session.isOpen())
            {
                session.close();
            }
            HibernateUtil.rebuildSessionFactory();
            session = HibernateUtil.getNewSession();
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#get(java.lang.Class, java.lang.Integer)
     */
    @SuppressWarnings("unchecked")
    public <T> T get(final Class<T> clsObj, final Integer id)
    {
        if (session != null)
        {
            return (T)session.get(clsObj, id);
        }
        
        log.error("Session was null.", new NullPointerException("Session was null"));

        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#getDataCount(java.lang.Class, java.lang.String, java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace.CompareType)
     */
    public <T> Integer getDataCount(final Class<T> clsObject, final String fieldName, final Object value, final DataProviderSessionIFace.CompareType compareType)
    {
        if (session != null)
        {
            Criteria criteria = session.createCriteria(clsObject);
            criteria.add(compareType == DataProviderSessionIFace.CompareType.Equals ? Restrictions.eq(fieldName, value) : Restrictions.eq(fieldName, value));
            criteria.setProjection(Projections.rowCount());
            List<?> countList = criteria.list();
            
            if (countList == null || countList.size() == 0)
            {
                return 0;
                
            }
            // else
            Object countObj = countList.get(0);
            if (countObj instanceof Integer)
            {
                return (Integer)countObj;
            }
        }
        
        log.error("Session was null.", new NullPointerException("Session was null"));

        return 0;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#load(java.lang.Class, java.lang.Integer)
     */
    @SuppressWarnings("unchecked")
    public <T> T load(final Class<T> clsObj, final Integer id)
    {
        if (session != null)
        {
            return (T)session.load(clsObj, id);
        }
        
        log.error("Session was null.", new NullPointerException("Session was null"));

        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#getData(java.lang.String)
     */
    public Object getData(final String sqlStr)
    {
        if (session != null)
        {
            List<?> list = getDataList(sqlStr);
            return list != null && list.size() > 0 ? list.get(0) : null;
        }
        
        log.error("Session was null.", new NullPointerException("Session was null"));

        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#contains(java.lang.Object)
     */
    public boolean contains(final Object obj)
    {
        return session.contains(obj);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#evict(java.lang.Class)
     */
    public void evict(final Class<?> clsObject)
    {
        if (session != null)
        {
            session.evict(clsObject);
            HibernateUtil.getSessionFactory().evict(clsObject);
            
        } else
        {
            log.error("Session was null.", new NullPointerException("Session was null"));
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#evict(java.lang.Object)
     */
    public void evict(final Object dataObj)
    {
        if (session != null)
        {
            session.evict(dataObj);
        } else
        {
            log.error("Session was null.", new NullPointerException("Session was null"));
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#placeIntoSession(java.lang.Object)
     */
    public void attach(final Object dataObj)
    {
        if (session != null)
        {
            session.lock(dataObj, LockMode.NONE);
            
        } else
        {
            log.error("Session was null.", new NullPointerException("Session was null"));
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#save(java.lang.Object)
     */
    public boolean save(final Object dataObj) throws Exception
    {
        if (session != null)
        {
            deleteObjectFromList();
            session.save(dataObj);
            return true;
        }
        
        log.error("Session was null.", new NullPointerException("Session was null"));
        
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#saveOrUpdate(java.lang.Object)
     */
    public boolean saveOrUpdate(final Object dataObj) throws Exception
    {
        if (session != null)
        {
            deleteObjectFromList();
            session.saveOrUpdate(dataObj);
            return true;
        }
        
        log.error("Session was null.", new NullPointerException("Session was null"));
        
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#update(java.lang.Object)
     */
    public boolean update(final Object dataObj) throws Exception
    {
        if (session != null)
        {
            deleteObjectFromList();
            session.update(dataObj);
            return true;
        }
        
        log.error("Session was null.", new NullPointerException("Session was null"));
        
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#createQuery(java.lang.String)
     */
    public QueryIFace createQuery(final String query, boolean isSql)
    {
        if (isSql)
        {
            return new HibernateSQLQuery(query);
        }
        return new HibernateQuery(query);
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#createCriteria(java.lang.Class)
     */
    public CriteriaIFace createCriteria(final Class<?> cls)
    {
        if (session != null) 
        { 
            return new HibernateCriteria(cls); 
        }
        log.error("Session was null.", new NullPointerException("Session was null"));
        return null;
    }
    
    /*
     * (non-Javadoc)
     * 
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
            log.error("Session was null.", new NullPointerException("Session was null"));
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
            transaction = null;
            
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
            log.error("Session was null.", new NullPointerException("Session was null"));
        }
    }
    
    
    /* (non-Javadoc)
	 * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#clear()
	 */
	@Override
	public void clear()
	{
        if (session != null)
        {
            session.clear();
            
        } else
        {
            log.error("Session was null.", new NullPointerException("Session was null"));
        }
	}

	/* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderSessionIFace#close()
     */
    public void close()
    {
        if (SHOW_COUNTS)
        {
            closesCounts++;
            System.err.println("Close - Creates: "+createsCounts+"  Closes: "+closesCounts+" Dif: "+(createsCounts-closesCounts)+"  "+session.hashCode());
            
            sessions.remove(session);
            
            if (sessions.size() == 1)
            {
                int cn = 0;
                for (Session ses : sessions)
                {
                    if (cn > 0) System.err.print(",");
                    cn++;
                    System.err.print(ses.hashCode());
                    if (cn %20 == 0) System.err.println();
                }
                System.err.println();
            }
        }
        
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
            log.error("Session was null.", new NullPointerException("Session was null"));
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
    
    public class HibernateQuery implements QueryIFace
    {
        protected Query queryDelegate;
        
        protected HibernateQuery()
        {
            //nuthin
        }
        
        public HibernateQuery(String hql)
        {
            //log.debug("hql["+hql+"]");
            queryDelegate = session.createQuery(hql);
            if (queryDelegate == null)
            {
                log.error("queryDelegate is null for query for hql["+hql+"]");
            }
        }

        public int executeUpdate()
        {
            return queryDelegate != null ? queryDelegate.executeUpdate() : 0;
        }

        public List<?> list()
        {
            return queryDelegate != null ? queryDelegate.list() : null;
        }

        public void setParameter(String name, Object value)
        {
            if (queryDelegate != null)
            {
                queryDelegate.setParameter(name, value);
            }
        }

        public Object uniqueResult()
        {
            return queryDelegate != null ? queryDelegate.uniqueResult() : null;
        }
    }
    
    public class HibernateSQLQuery extends HibernateQuery
    {
        public HibernateSQLQuery(String sql)
        {
            queryDelegate = session.createSQLQuery(sql);
            if (queryDelegate == null)
            {
                log.error("queryDelegate is null for query for sql["+sql+"]");
            }
            
        }
    }

    
    public class HibernateCriteria implements CriteriaIFace
    {
        protected Criteria criteriaDelegate;
        
        /**
         * @param cls
         */
        public HibernateCriteria(final Class<?> cls)
        {
            criteriaDelegate = session.createCriteria(cls);
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.dbsupport.DataProviderSessionIFace.CriteriaIFace#add(java.lang.Object)
         */
        @Override
        public void add(final Object criterion)
        {
            if (criterion instanceof Criterion)
            {
                criteriaDelegate.add((Criterion)criterion);
            }
            else
            {
                log.error("invalid criterion.");
            }
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.dbsupport.DataProviderSessionIFace.CriteriaIFace#uniqueResult()
         */
        @Override
        public Object uniqueResult()
        {
            return criteriaDelegate.uniqueResult();
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.dbsupport.DataProviderSessionIFace.CriteriaIFace#list()
         */
        @Override
        public List<?> list()
        {
            return criteriaDelegate.list();
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.dbsupport.DataProviderSessionIFace.CriteriaIFace#addSubCriterion(java.lang.String, java.lang.Object)
         */
        @Override
        public void addSubCriterion(String name, Object criterion)
        {
            if (criterion instanceof Criterion)
            {
                criteriaDelegate.createCriteria(name).add((Criterion )criterion);
            }
            else
            {
                log.error("invalid criterion.");
            }
        }
    }
}
