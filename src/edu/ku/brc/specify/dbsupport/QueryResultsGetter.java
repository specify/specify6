/* Filename:    $RCSfile: QueryResultsGetter.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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

package edu.ku.brc.specify.dbsupport;
import java.util.List;
import java.util.Vector;

/**
 * 
 * @author rods
 *
 */
public class QueryResultsGetter implements SQLExecutionListener
{
    // Static Data Members
    //private static Log log = LogFactory.getLog(QueryResultsGetter.class);
    
    // Data Members
    protected Vector<SQLExec>               sqlExecList = new Vector<SQLExec>();
    protected Vector<QueryResultsContainer> qrcs        = new Vector<QueryResultsContainer>();
    protected boolean                       hasFailed   = false;
    protected QueryResultsListener          listener;
    
    /**
     * 
     * @param listener
     */
    public QueryResultsGetter(final QueryResultsListener listener)
    {
       this.listener = listener;
       
    }
    
    /**
     * 
     * @param ndbrc
     */
    public void add(QueryResultsContainer ndbrc)
    {
        qrcs.addElement(ndbrc);
        
        SQLExec sqle = new SQLExec(ndbrc, this);
        sqlExecList.addElement(sqle);
        sqle.init();
    }
    
    /**
     * 
     * @return
     */
    public List<QueryResultsContainer> getQueryResultsContainers()
    {
        return qrcs;
    }
    
    /**
     * 
     * @return
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
     * 
     * @param processor
     * @return
     */
    protected SQLExec getSQLExecByProcessor(final SQLExecutionProcessor processor)
    {
        for (SQLExec sqle : sqlExecList)
        {
            if (sqle.getSqlProc() == processor)
            {
                return sqle;
            }
        }
        return null;
    }
    
    /**
     * 
     *
     */
    public void clear()
    {
        for (SQLExec sqle : sqlExecList)
        {
            sqle.clear();
        }
        sqlExecList.clear();
        
        for (QueryResultsContainer ndbrc : qrcs)
        {
            ndbrc.clear();
        }
        qrcs.clear();
        
    }
    
    //-----------------------------------------------------
    //-- SQLExecutionListener
    //-----------------------------------------------------
    
    public synchronized void exectionDone(final SQLExecutionProcessor processor, final java.sql.ResultSet resultSet)
    {
        SQLExec sqle = getSQLExecByProcessor(processor);
        sqle.setProcessed(true);
        sqle.getNdbrc().processResultSet(resultSet);
        if (isDoneProcessing())
        {
            listener.allResultsBack();
        }
     }
    
    public synchronized void executionError(final SQLExecutionProcessor processor, final Exception ex)
    {
        //SQLExec sqle = getSQLExecByProcessor(processor);
        hasFailed = true;
    }
    
    
    //----------------------------------------------------------
    //-- Inner Classes
    //----------------------------------------------------------
    class SQLExec
    {
        protected QueryResultsContainer ndbrc;
        protected SQLExecutionProcessor  sqlProc;
        protected boolean                isProcessed = false;
        protected SQLExecutionListener   listener    = null;
        
        public SQLExec(final QueryResultsContainer ndbrc, final SQLExecutionListener listener)
        {
            this.ndbrc = ndbrc;
            this.listener = listener;
        }
        
        public void init()
        {
            sqlProc = new SQLExecutionProcessor(listener, ndbrc.getSql());
            sqlProc.start();
        }

        public QueryResultsContainer getNdbrc()
        {
            return ndbrc;
        }
        
        public SQLExecutionProcessor getSqlProc()
        {
            return sqlProc;
        }

        public boolean isProcessed()
        {
            return isProcessed;
        }

        public void setProcessed(boolean isProcessed)
        {
            this.isProcessed = isProcessed;
        }
        
        public void clear()
        {
            ndbrc   = null;
            sqlProc = null;
        }
        
    }


}
