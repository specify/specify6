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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;


/**
 * Processes a list of QueryResultsContainer objects (Note: Each QueryResultsContainer contains an SQL statement)
 * in serialized fashion;
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class QueryResultsSerializedGetter implements QRCProcessorListener
{
    private static final Logger log = Logger.getLogger(QueryResultsSerializedGetter.class);
    
    // Data Members
    protected Vector<QueryResultsContainerIFace> qrcs        = new Vector<QueryResultsContainerIFace>();
    protected boolean                            hasFailed   = false;
    protected QueryResultsListener               listener;
    
    // For Serialized Processing
    protected boolean                            hasProcessingStarted   = false;
    protected int                                currentProcessingIndex;
    protected Connection                         connection             = null;

    /**
     * Constructs a QueryResultsSerializedGetter for parallel processing.
     * @param listener the listener to be notified
     */
    public QueryResultsSerializedGetter(final QueryResultsListener listener)
    {
       this.listener = listener;
       
    }
    
    /**
     * Constructs a QueryResultsSerializedGetter with option to specify what type of processing.
     * @param listener the listener to be notified
     * @param isSerialized indicates the type of processing to be done false - parallel, false - serialized
     */
    public QueryResultsSerializedGetter(final QueryResultsListener listener, final boolean isSerialized)
    {
       this.listener = listener;
    }
    
    /**
     * Adds a QueryResultsContainer and starts its execution on a separate thread.
     * @param qrc the container to be executed
     */
    public void add(final QueryResultsContainerIFace qrc)
    {
        qrcs.addElement(qrc);
    }
    
    /**
     * Adds a QueryResultsContainer and starts its execution on a separate thread.
     * @param qrcsArg the collection of containers to be executed
     */
    public void add(final List<QueryResultsContainerIFace> qrcsArg)
    {
        this.qrcs.addAll(qrcsArg);
        
        currentProcessingIndex = 0;
        startContainer(qrcs.get(currentProcessingIndex));
    }
   
    /**
     * Creates a SQLExec for the QueryResultsContainer and starts it up.
     * @param qrc the container to be executed
     */
    protected void startContainer(final QueryResultsContainerIFace qrc)
    {
        if (hasProcessingStarted)
        {
            throw new RuntimeException("Processing has already started!.");
        }
        
        if (connection == null)
        {
            connection = DBConnection.getInstance().createConnection();
        }
        
        qrc.start(this, connection);     
    }
    
    /**
     * Adds a QueryResultsContainer and starts its execution on a separate thread when startProcessing is true
     * this will throw a runtime exception if the process is started twice.
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
     * Returns a list of QueryResultsContainers.
     * @return Returns a list of QueryResultsContainers
     */
    public List<QueryResultsContainerIFace> getQueryResultsContainers()
    {
        return qrcs;
    }
    
    /**
     * 
     * Clears all data structures.
     */
    public void clear()
    {
        for (QueryResultsContainerIFace qrc : qrcs)
        {
            qrc.clear();
        }
        qrcs.clear();
        
        if (connection != null)
        {
            try
            {
                connection.close();
                connection = null;
                
            } catch (SQLException ex)
            {
                log.error(ex);
            }
        }
    }
    
    //-----------------------------------------------------
    //-- QRCProcessorListener
    //-----------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.QRCProcessorListener#exectionDone(edu.ku.brc.dbsupport.QueryResultsContainerIFace)
     */
    public synchronized void exectionDone(final QueryResultsContainerIFace qrc)
    { 
        currentProcessingIndex++;
        if (currentProcessingIndex == qrcs.size())
        {
            listener.allResultsBack();
            
            if (connection != null)
            {
                try
                {
                    connection.close();
                    connection = null;
                    
                } catch (SQLException ex)
                {
                    log.error(ex);
                }
            }
            
        } else
        {
            QueryResultsContainerIFace nxtQRC = qrcs.elementAt(currentProcessingIndex);
            nxtQRC.start(this, connection);
        } 
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.QRCProcessorListener#executionError(edu.ku.brc.dbsupport.QueryResultsContainerIFace)
     */
    public synchronized void executionError(final QueryResultsContainerIFace qrc)
    {
        hasFailed = true;
    }
    
}
