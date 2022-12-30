/* Copyright (C) 2022, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.dbsupport;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.JDBCConnectionException;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
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
    private static long lastErrorTime = 0;
    
    protected QueryExecutor             queryExecutor;
    protected String                    sqlStr;
    protected boolean                   inError     = false;
    protected boolean					quiet       = false; //don't dispatch errors when quiet - for use by background tasks 
    protected List<?>                   resultsList = null;
    protected boolean                   isUnique    = false;
    protected boolean					isCount = false;        
    protected int						maxResults  = 0;
    protected int						firstResult = 0;
    protected boolean                   doDebug     = AppPreferences.getLocalPrefs().getBoolean("esdebug", false);
    
    /**
     * A list of <Name, Value> pairs for each parameter in the query.
     */
    protected List<Pair<String, Object>> params = null;
    
    protected CustomQueryListener       cql         = null;
    protected Object                    data        = null;
    protected Query                     query       = null;
    
    protected final AtomicBoolean       cancelled   = new AtomicBoolean(false);
	private boolean 					isModified 	= true;
    
    /**
     * Constructor.
     * @param sqlStr the query string
     */
    public JPAQuery(final String sqlStr)
    {
        this(null, sqlStr, null);
    }
    
    /**
     * Constructor to be used as a Runnable.
     * @param sqlStr the query string
     * @param cql the listener
     */
    public JPAQuery(final String sqlStr,
                    final CustomQueryListener cql)
    {
        this(null, sqlStr, cql);
    }
    
    /**
     * Constructor to be used as a Runnable.
     * @param sqlStr the query string
     * @param cql the listener
     */
    public JPAQuery(final QueryExecutor queryExecutor,
                    final String sqlStr,
                    final CustomQueryListener cql)
    {
        this.queryExecutor = queryExecutor == null ? QueryExecutor.getInstance() : queryExecutor;
        this.sqlStr        = sqlStr;
        this.cql           = cql;
    }
    
    /**
     * Constructor to be used as a Runnable.
     * @param query the query string
     * @param cql the listener
     */
    public JPAQuery(final Query query,
                    final CustomQueryListener cql)
    {
        this.query         = query;
        this.cql           = cql;
        this.queryExecutor = QueryExecutor.getInstance();

    }
    
    /**
     * @param isUnique the isUnique to set
     */
    public void setUnique(boolean isUnique)
    {
        this.isUnique = isUnique;
    }

    
    /**
	 * @return the isCount
	 */
	public boolean isCount() 
	{
		return isCount;
	}

	/**
	 * @param isCount the isCount to set
	 */
	public void setCount(boolean isCount) 
	{
		this.isCount = isCount;
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
        return queryExecutor.executeQuery(this);
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
    
    public void clearCql() 
    {
    	cql = null;
    }
    /**
     * @return
     */
    private boolean dispatchError()
    {
        long    now       = System.currentTimeMillis(); 
        boolean showError = !quiet && now - lastErrorTime > 2000;
        lastErrorTime = now;
        if (showError)
        {
            inError = true;
            CommandDispatcher.dispatch(new CommandAction("ERRMSG", "DISPLAY", this, null, "BAD_CONNECTION"));
            return true;
        }
        return false;
    }

    
    /**
	 * @return the quiet
	 */
	public boolean isQuiet() 
	{
		return quiet;
	}

	/**
	 * @param quiet the quiet to set
	 */
	public void setQuiet(boolean quiet)
	{
		this.quiet = quiet;
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
                if (maxResults > 0)
                {
                	qry.setMaxResults(maxResults);
                }
                if (firstResult > 0)
                {
                	qry.setFirstResult(firstResult);
                }
                if (params != null)
                {
                    for (Pair<String, Object> param : params)
                    {
                        qry.setParameter(param.getFirst(), param.getSecond());
                    }
                }
                
                if (resultsList != null)
                {
                	resultsList.clear();
                	resultsList = null;
                }
                
                if (isUnique)
                {
                    List<Object> objArray = new ArrayList<Object>(1);
                    objArray.add(qry.uniqueResult());
                    resultsList = objArray;
                    
                } else if (isCount) {
                	List<Object> objArray = new ArrayList<Object>(1);
                	objArray.add(qry.list().size());
                	resultsList = objArray;
                } else if (isModified)
                {
                    resultsList = modifyQueryResults(qry.list());
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
                if (dispatchError())
                {
                    inError = true;
                    return false;
                }
                
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(JPAQuery.class, ex);
                try
                {
                    HibernateUtil.rebuildSessionFactory();
                    Query localQuery = session.createQuery(sqlStr);
                    resultsList = localQuery.list();
                    
                } catch (JDBCConnectionException ex2)
                {
                    dispatchError();
                }
            }
            
            inError = resultsList == null;
            
            /*for (Object data : resultsList)
            {
              log.debug(data);
            }*/
            
        } catch (Exception ex)
        {
            log.error(ex);
            dispatchError();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(JPAQuery.class, ex);
            log.error("** In Exception ["+sqlStr+"]"); //$NON-NLS-1$ //$NON-NLS-2$
            ex.printStackTrace();
            inError = true;
            
        } finally
        {
            if (session != null)
            {
                try
                {
                    session.close();
                } catch (HibernateException he) {}
            }
        }
        
        return !inError;
    }
    
    /**
     * Processes and modifies data from the Query Output of a Hibernate SQL command
     * @param rawQueryResults -> A query output from Hibernate
     * @return newList -> the modified list
     */
    public static List<?> modifyQueryResults(List<?> rawQueryResults)
    {
    	/* 
    	 * Hibernate returns data from the database to Java from Query.list() in the form of a List<?> containing the results.
    	 * If there are multiple results per row, Hibernate instead returns a List of Object Arrays, each containing results per row.
    	 *   
    	 * 
    	 * See https://docs.jboss.org/hibernate/orm/3.2/api/org/hibernate/Query.html#list()
    	 */
    	List<Object> modifiedQueryResults = (List<Object>) rawQueryResults;
    	
    	for (int row =0 ; row < modifiedQueryResults.size(); row++)
    	{
    		if (modifiedQueryResults.get(row) instanceof Object[])
    		{
    			Object[] cols = (Object[]) modifiedQueryResults.get(row);
    			for (int col=0; col < cols.length; col++)
    			{
    				if (cols[col] != null)
    				{
    					/* Strip Bigdecimals of their trailing zeros */
    					if (cols[col] instanceof BigDecimal)
    					{
    						BigDecimal rawData = (BigDecimal) cols[col];
    						BigDecimal newData = new BigDecimal(rawData.stripTrailingZeros().toPlainString());
    						cols[col] = newData;
    						modifiedQueryResults.set(row, cols);
    					}
    				}
    			}
    		}
    	}
    	return modifiedQueryResults;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#execute(edu.ku.brc.dbsupport.CustomQueryListener)
     */
    public void execute(final CustomQueryListener cqlArg)
    {
        runQuery();
     
        try
        {
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
            
            if (cql != null && cqlArg != null && cql != cqlArg)
            {
                if (inError)
                {
                    cqlArg.executionError(this);
                    
                } else
                {
                    cqlArg.exectionDone(this);
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    

    /**
     * Dumps the results to the log file.
     */
    protected void dumpResults()
    {
        //log.debug("-- Results Start -- Size: "+resultsList.size());
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
                    //log.debug(" --- " + sb.toString()+" --- ");
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
                //log.debug(rowNum+" - " + sb.toString());
            } else
            {
                //log.debug(rowNum+" - " + row);
            }
            rowNum++;
        }
        //log.debug("-- Results End --");
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

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryIFace#getTableIds()
     */
    @Override
    public List<Integer> getTableIds()
    {
        return null;
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.dbsupport.CustomQueryIFace#getMaxResults()
	 */
	@Override
	public int getMaxResults() 
	{
		return maxResults;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.dbsupport.CustomQueryIFace#setMaxResults(int)
	 */
	@Override
	public void setMaxResults(int maxResults) 
	{
		this.maxResults = maxResults;
	}

	/**
	 * @return the firstResult
	 */
	public int getFirstResult() 
	{
		return firstResult;
	}

	/**
	 * @param firstResult the firstResult to set
	 */
	public void setFirstResult(int firstResult) 
	{
		this.firstResult = firstResult;
	}
	
	/**
	 * Tell the query whether it should be modified or not (default is true)
	 * See modifyQueryResults() to see how the query can be modified
	 * 
	 * @param isModified
	 */
	public void setIsModified(boolean isModified)
	{
		this.isModified = isModified;
	}
 
}
