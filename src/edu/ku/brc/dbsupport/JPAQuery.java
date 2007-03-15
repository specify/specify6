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

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import edu.ku.brc.helpers.SwingWorker;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Mar 5, 2007
 *
 */
public class JPAQuery implements CustomQuery
{
    private static final Logger log = Logger.getLogger(JPAQuery.class);
    
    protected String                    sqlStr;
    protected List<QueryResultsDataObj> qrdoResults = null;
    protected boolean                   inError     = false;
    protected List<?>                   resultsList = null;
    
    /**
     * Constructor.
     * @param sqlStr the query string
     */
    public JPAQuery(final String sqlStr)
    {
        this.sqlStr = sqlStr;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#execute()
     */
    public boolean execute()
    {
        // NOTE: This return true if it executed correctly
        inError = false;
        
        Session session = null;
        
        try
        {
            session = HibernateUtil.getNewSession();
            
            log.error(sqlStr);
            Query query = session.createQuery(sqlStr);
            
            resultsList = query.list();
            
            inError = resultsList == null;
            
            for (Object data : resultsList)
            {
              log.debug(data);
            }
            
        } catch (Exception ex)
        {
            log.error(ex);
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
    public void execute(final CustomQueryListener cql)
    {
        final JPAQuery thisItem = this;

        final SwingWorker worker = new SwingWorker()
        {
             public Object construct()
            {
                inError = execute();
                return null;
            }

            //Runs on the event-dispatching thread.
            public void finished()
            {
                if (inError)
                {
                    cql.executionError(thisItem);
                    
                } else
                {
                    cql.exectionDone(thisItem);
                }
            }
        };
        worker.start();

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
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#getResults()
     */
    public List<QueryResultsDataObj> getResults()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#isExecutable()
     */
    public boolean isExecutable()
    {
        return true;
    }

}
