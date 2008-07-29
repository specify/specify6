/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.dbsupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.JDBCConnectionException;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.util.Pair;

/**
 * This class is used to execute JPA or Hibernate queries and notify the listener when done.
 *  <br>The start method asks a thread pool service
 * to execute the query. (It used to execute on its own thread).
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Mar 5, 2007
 *
 */
public class JPAQuery implements CustomQueryIFace
{
    private static final Logger log = Logger.getLogger(JPAQuery.class);
    
    protected String                    sqlStr;
    protected boolean                   inError     = false;
    protected List<?>                   resultsList = null;
    protected boolean                   isUnique    = false;
    protected boolean                   doDebug     = AppPreferences.getLocalPrefs().getBoolean("esdebug", false);
    
    /**
     * A list of <Name, Value> pairs for each parameter in the query.
     */
    protected List<Pair<String, Object>> params = null;
    
    protected CustomQueryListener       cql         = null;
    protected Object                    data        = null;
    protected Query                     query       = null;
    
    protected final AtomicBoolean       cancelled   = new AtomicBoolean(false);
    /**
     * Constructor.
     * @param sqlStr the query string
     */
    public JPAQuery(final String sqlStr)
    {
        this(sqlStr, null);
    }
    
    /**
     * Constructor to be used as a Runnable.
     * @param sqlStr the query string
     * @param cql the listener
     */
    public JPAQuery(final String sqlStr,
                    final CustomQueryListener cql)
    {
        this.sqlStr = sqlStr;
        this.cql    = cql;
    }
    
    /**
     * Constructor to be used as a Runnable.
     * @param query the query string
     * @param cql the listener
     */
    public JPAQuery(final Query query,
                    final CustomQueryListener cql)
    {
        this.query  = query;
        this.cql    = cql;
    }
    
    /**
     * @param isUnique the isUnique to set
     */
    public void setUnique(boolean isUnique)
    {
        this.isUnique = isUnique;
    }

    /**
     * @return the inError
     */
    public boolean isInError()
    {
        return inError;
    }

    /**
     * @return the data
     */
    public Object getData()
    {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(final Object data)
    {
        this.data = data;
    }
    
    /**
     * Starts the thread to make the SQL call.
     *
     */
    //public void start()
    public Future<CustomQueryIFace> start()
    {
        return QueryExecutor.executeQuery(this);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#getListener()
     */
    public CustomQueryListener getListener()
    {
        return cql;
    }
    
   /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#execute()
     */
    public boolean execute()
    {
        execute(cql); // sets inError
        
        return !inError;
    }

    /**
     * @return
     */
    private boolean runQuery()
    {
        // NOTE: This return true if it executed correctly
        inError = false;
        
        Session session = HibernateUtil.getNewSession();
        
        try
        {
            try
            {
                //log.debug(sqlStr);
                Query qry = query != null ? query : session.createQuery(sqlStr);
                
                if (params != null)
                {
                    for (Pair<String, Object> param : params)
                    {
                        qry.setParameter(param.getFirst(), param.getSecond());
                    }
                }
                
                if (isUnique)
                {
                    List<Object> objArray = new ArrayList<Object>(1);
                    objArray.add(qry.uniqueResult());
                    resultsList = objArray;
                    
                } else
                {
                    resultsList = qry.list();
                }
                
               if (doDebug)
               {
                   dumpResults();
               }
                
            } catch (JDBCConnectionException ex)
            {
                HibernateUtil.rebuildSessionFactory();
                Query localQuery = session.createQuery(sqlStr);
                resultsList = localQuery.list();
            }
            
            inError = resultsList == null;
            
            /*for (Object data : resultsList)
            {
              log.debug(data);
            }*/
            
        } catch (Exception ex)
        {
            log.error("** In Exception ["+sqlStr+"]"); //$NON-NLS-1$ //$NON-NLS-2$
            log.error(ex);
            ex.printStackTrace();
            inError = true;
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        
        return !inError;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#execute(edu.ku.brc.dbsupport.CustomQueryListener)
     */
    public void execute(final CustomQueryListener cqlArg)
    {
        runQuery();
        
        if (cqlArg != null)
        {
            if (inError)
            {
                cqlArg.executionError(this);
                
            } else
            {
                cqlArg.exectionDone(this);
            }
        }
        
        if (cql != null && cql != cqlArg)
        {
            if (inError)
            {
                cqlArg.executionError(this);
                
            } else
            {
                cqlArg.exectionDone(this);
            }
        }
    }

    /**
     * Dumps the results to the log file.
     */
    protected void dumpResults()
    {
        log.debug("-- Results Start -- Size: "+resultsList.size());
        StringBuilder sb       = new StringBuilder();
        int           rowNum   = 0;
        boolean       firstRow = true;
        for (Object row : resultsList)
        {
            if (row instanceof Object[])
            {
                if (firstRow)
                {
                    firstRow = false;
                    sb.setLength(0);
                    Object[] cols = (Object[])row;
                    for (Object colData : cols)
                    {
                        sb.append('|');
                        if (colData != null && colData.getClass() != null)
                        {
                            sb.append(colData.getClass().getSimpleName());
                        }
                    }
                    sb.append('|');
                    log.debug(" --- " + sb.toString()+" --- ");
                }
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                
                sb.setLength(0);
                Object[] cols = (Object[])row;
                for (Object colData : cols)
                {
                    sb.append('|');
                    if (colData instanceof Calendar)
                    {
                        sb.append(sdf.format(((Calendar)colData).getTime()));
                    } else
                    {
                        sb.append(colData);
                    }
                    
                }
                sb.append('|');
                log.debug(rowNum+" - " + sb.toString());
            } else
            {
                log.debug(rowNum+" - " + row);
            }
            rowNum++;
        }
        log.debug("-- Results End --");
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#getDataObjects()
     */
    public List<?> getDataObjects()
    {
        return resultsList;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#getName()
     */
    public String getName()
    {
        return getClass().getSimpleName();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#getQueryDefinition()
     */
    public List<QueryResultsContainerIFace> getQueryDefinition()
    {
        throw new RuntimeException("Not Implemented"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#getResults()
     */
    public List<QueryResultsDataObj> getResults()
    {
        throw new RuntimeException("Not Implemented"); //$NON-NLS-1$
    }

    /**
     * @return the params
     */
    public List<Pair<String, Object>> getParams()
    {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(List<Pair<String, Object>> params)
    {
        this.params = params;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryIFace#isCancelled()
     */
    //@Override
    public boolean isCancelled()
    {
        return cancelled.get();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryIFace#cancel()
     */
    //@Override
    public void cancel()
    {
        cancelled.set(true);
    }

}
