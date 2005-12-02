/* Filename:    $RCSfile: QueryResultsSerializedGetter.java,v $
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
 * Processes a list of QueryResultsContainer objects (Note: Each QueryResultsContainer contains an SQL statement)
 * in serialized fashion;
 * 
 * @author rods
 *
 */
public class QueryResultsSerializedGetter implements SQLExecutionListener
{
    // Static Data Members
    // private static Log log = LogFactory.getLog(QueryResultsSerializedGetter.class);
    
    // Data Members
    protected SQLExecutionProcessor         sqlProc;
    protected Vector<QueryResultsContainer> qrcs        = new Vector<QueryResultsContainer>();
    protected boolean                       hasFailed   = false;
    protected QueryResultsListener          listener;
    
    // For Serialized Processing
    protected boolean                       hasProcessingStarted   = false;
    protected int                           currentProcessingIndex;

    /**
     * Constructs a QueryResultsSerializedGetter for parallel processing
     * @param listener the listener to be notified
     */
    public QueryResultsSerializedGetter(final QueryResultsListener listener)
    {
       this.listener = listener;
       
    }
    
    /**
     * Constructs a QueryResultsSerializedGetter with option to specify what type of processing
     * @param listener the listener to be notified
     * @param isSerialized indicates the type of processing to be done false - parallel, false - serialized
     */
    public QueryResultsSerializedGetter(final QueryResultsListener listener, final boolean isSerialized)
    {
       this.listener = listener;
    }
    
    /**
     * Adds a QueryResultsContainer and starts its execution on a separate thread 
     * @param qrc the container to be executed
     */
    public void add(final QueryResultsContainer qrc)
    {
        qrcs.addElement(qrc);
    }
    
    /**
     * Adds a QueryResultsContainer and starts its execution on a separate thread 
     * @param qrcs the collection of containers to be executed
     */
    public void add(final List<QueryResultsContainer> qrcs)
    {
        this.qrcs.addAll(qrcs);
        currentProcessingIndex = 0;
        startContainer(qrcs.get(currentProcessingIndex));
    }
   
    /**
     * Creates a SQLExec for the QueryResultsContainer and starts it up
     * @param qrc the container to be executed
     */
    protected void startContainer(final QueryResultsContainer qrc)
    {
        if (hasProcessingStarted)
        {
            throw new RuntimeException("Processing has already started!.");
        }
        
        sqlProc = new SQLExecutionProcessor(this, qrc.getSql());
        sqlProc.setAutoCloseConnection(false);
        sqlProc.start();        
    }
    
    /**
     * Adds a QueryResultsContainer and starts its execution on a separate thread when startProcessing is true
     * this will throw a runtime exception if the process is started twice
     * @param qrc the container to be executed
     * @param startProcessing true - starts processing, false - don't start processing yet
     */
    public void add(final QueryResultsContainer qrc, final boolean startProcessing)
    {
        
        qrcs.addElement(qrc);
        
        if (startProcessing)
        {
            if (hasProcessingStarted)
            {
                throw new RuntimeException("Processing has already been started!");
            }
            hasProcessingStarted   = true;
            currentProcessingIndex = 0;
            
            startContainer(qrcs.firstElement());    
        }
    }
    
    /**
     * Returns a list of QueryResultsContainers
     * @return Returns a list of QueryResultsContainers
     */
    public List<QueryResultsContainer> getQueryResultsContainers()
    {
        return qrcs;
    }
    
    /**
     * 
     * Clears all data structures
     */
    public void clear()
    {
        for (QueryResultsContainer qrc : qrcs)
        {
            qrc.clear();
        }
        qrcs.clear();
        
    }
    
     //-----------------------------------------------------
    //-- SQLExecutionListener
    //-----------------------------------------------------


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.SQLExecutionListener#exectionDone(edu.ku.brc.specify.dbsupport.SQLExecutionProcessor, java.sql.ResultSet)
     */
    public synchronized void exectionDone(final SQLExecutionProcessor processor, final java.sql.ResultSet resultSet)
    {
        
        QueryResultsContainer qrc = qrcs.elementAt(currentProcessingIndex);
        if (resultSet != null)
        {
            qrc.processResultSet(resultSet);
        }
        
        currentProcessingIndex++;
        if (currentProcessingIndex == qrcs.size())
        {
            listener.allResultsBack();
            sqlProc.close();
            
        } else
        {
            qrc = qrcs.elementAt(currentProcessingIndex);
            
            SQLExecutionProcessor newSqlProc = new SQLExecutionProcessor(sqlProc.getDbConnection(), this, qrc.getSql());
            newSqlProc.setAutoCloseConnection(false);
            newSqlProc.start();  
            
            sqlProc.closeStatement();
            sqlProc = newSqlProc;
        }
     }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.SQLExecutionListener#executionError(edu.ku.brc.specify.dbsupport.SQLExecutionProcessor, java.lang.Exception)
     */
    public synchronized void executionError(final SQLExecutionProcessor processor, final Exception ex)
    {
        listener.allResultsBack();
        
        hasFailed = true;   
    }
    
    
}
