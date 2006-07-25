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

package edu.ku.brc.dbsupport;
import java.util.List;
import java.util.Vector;

/**
 * This class contains a collection of QueryResultsContainers (each has their own SQL statement) and it creates
 * SQLExecutionProcessors for each Container and has them execute all the Queries in parallel. If one returns an error
 * then the listener is notified immediately, otherwise the listener is notified when they all complete.
 *
 * @code_status Unknown (auto-generated)
 * 
 * @author rods
 *
 */
public class QueryResultsGetter
{
    // Static Data Members
    //private static final Logger log = Logger.getLogger(QueryResultsGetter.class);
    
    // Data Members
    protected Vector<SQLExec>               sqlExecList = new Vector<SQLExec>();
    protected Vector<QueryResultsContainer> qrcs        = new Vector<QueryResultsContainer>();
    protected boolean                       hasFailed   = false;
    protected QueryResultsListener          listener;
    
    /**
     * Creates a getter to go get (in parallel) all the containers and their values
     * @param listener the object that will be notified when all the queries are done
     */
    public QueryResultsGetter(final QueryResultsListener listener)
    {
       this.listener = listener;
       
    }
    
    /**
     * Adds a QueryResultsContainer to be processed
     * @param qrc the QueryResultsContainer to be processed
     */
    public void add(QueryResultsContainer qrc)
    {
        qrcs.addElement(qrc);
        
        SQLExec sqle = new SQLExec(qrc, this);
        sqlExecList.addElement(sqle);
        sqle.init();
    }
    
    /**
     * Adds a QueryResultsContainer and starts its execution on a separate thread 
     * @param qrcs the collection of containers to be executed
     */
    public void add(final List<QueryResultsContainer> qrcs)
    {
        for (QueryResultsContainer qrc : qrcs)
        {
            add(qrc); // this needs to be done after everything has been added to the qrc
        }
    }
   
   /**
     * Returns the collection of QueryResultsContainer
     * @return Returns the collection of QueryResultsContainer
     */
    public List<QueryResultsContainer> getQueryResultsContainers()
    {
        return qrcs;
    }
    
    /**
     * Checks to see of all of the paraellel queries are done
     * @return true if all the parallel queries are done.
     */
    public boolean isDoneProcessing()
    {
        for (SQLExec sqle : sqlExecList)
        {
            if (!sqle.isProcessed())
            {
                 return false;
            }
        }
        return true;
    }
    
    /**
     * Cleans up all the data structures
     *
     */
    public void clear()
    {
        for (SQLExec sqle : sqlExecList)
        {
            sqle.clear();
        }
        sqlExecList.clear();
        
        for (QueryResultsContainer qrc : qrcs)
        {
            qrc.clear();
        }
        qrcs.clear();
        
    }
    
    /**
     * Called when a container is done processing
     *
     */
    protected synchronized void containerIsDone(QueryResultsContainer qrc)
    {
        if (isDoneProcessing())
        {
            if (!hasFailed)
            {
                listener.allResultsBack();
            }
        }
    }
    
    /**
     * Called when a container has an error
     *
     */
    protected synchronized void containerIsInError(QueryResultsContainer qrc)
    {
        hasFailed = true;
        listener.resultsInError(qrc);
        
        // XXX Maybe here we tell all remain queries to stop ?
    }
    
    
    
    //----------------------------------------------------------
    //-- Inner Classes
    //----------------------------------------------------------
    /**
     * This is a class that associated the SQLExecutionProcessor and the Container and 
     * provides the needed parallelism
     */
    class SQLExec implements SQLExecutionListener
    {
        protected QueryResultsContainer  qrc;
        protected SQLExecutionProcessor  sqlProc;
        protected boolean                isProcessed = false;
        protected QueryResultsGetter     getter      = null;
        
        /**
         * Creates a mini-processor that knows how to 
         * @param qrc the container to be processed
         * @param getter the getter that own this guy
         */
        public SQLExec(final QueryResultsContainer qrc, final QueryResultsGetter getter)
        {
            this.qrc    = qrc;
            this.getter = getter;
        }
        
        /**
         * Starts up the query
         *
         */
        public void init()
        {
            sqlProc = new SQLExecutionProcessor(this, qrc.getSql());
            sqlProc.start();
        }

        /**
         * @return return the container
         */
        public QueryResultsContainer getQueryResultsContainer()
        {
            return qrc;
        }
        
        /**
         * @return returns whether it has been processed or not
         */
        public boolean isProcessed()
        {
            return
            isProcessed;
        }

        
        /**
         * Clean up all data 
         */
        public void clear()
        {
            qrc     = null;
            sqlProc = null;
            getter  = null;
        }
        
        //-----------------------------------------------------
        //-- SQLExecutionListener
        //-----------------------------------------------------
        
        /* (non-Javadoc)
         * @see edu.ku.brc.specify.dbsupport.SQLExecutionListener#exectionDone(edu.ku.brc.specify.dbsupport.SQLExecutionProcessor, java.sql.ResultSet)
         */
        public synchronized void exectionDone(final SQLExecutionProcessor processor, final java.sql.ResultSet resultSet)
        {
            isProcessed = true;
            qrc.processResultSet(resultSet);
            getter.containerIsDone(qrc);
         }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.specify.dbsupport.SQLExecutionListener#executionError(edu.ku.brc.specify.dbsupport.SQLExecutionProcessor, java.lang.Exception)
         */
        public synchronized void executionError(final SQLExecutionProcessor processor, final Exception ex)
        {
            hasFailed = true;
            getter.containerIsInError(qrc);
        }
       
    }


}
