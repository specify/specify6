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
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * This class contains a collection of QueryResultsContainers (each has their own SQL statement) and it creates
 * SQLExecutionProcessors for each Container and has them execute all the Queries in parallel. If one returns an error
 * then the listener is notified immediately, otherwise the listener is notified when they all complete.
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class QueryResultsGetter implements QRCProcessorListener
{
    // Data Members
    protected Hashtable<QueryResultsContainerIFace, Boolean> doneHash= new Hashtable<QueryResultsContainerIFace, Boolean>();
    protected Vector<QueryResultsContainerIFace> qrcs      = new Vector<QueryResultsContainerIFace>();
    protected int                                doneCount = 0;
    protected boolean                            hasFailed = false;
    protected QueryResultsListener               listener;
    
    /**
     * Creates a getter to go get (in parallel) all the containers and their values.
     * @param listener the object that will be notified when all the queries are done
     */
    public QueryResultsGetter(final QueryResultsListener listener)
    {
       this.listener = listener;
    }
    
    /**
     * Adds a QueryResultsContainer to be processed.
     * @param qrc the QueryResultsContainer to be processed
     */
    public void add(final QueryResultsContainerIFace qrc)
    {
        doneHash.put(qrc, false);
        qrcs.addElement(qrc);
        qrc.start(this, null);
    }
    
    /**
     * Adds a QueryResultsContainer and starts its execution on a separate thread .
     * @param qrcsArg the collection of containers to be executed
     */
    public void add(final List<QueryResultsContainerIFace> qrcsArg)
    {
        for (QueryResultsContainerIFace qrc : qrcsArg)
        {
            add(qrc); // this needs to be done after everything has been added to the qrc
        }
    }
   
   /**
     * Returns the collection of QueryResultsContainer.
     * @return Returns the collection of QueryResultsContainer
     */
    public List<QueryResultsContainerIFace> getQueryResultsContainers()
    {
        return qrcs;
    }
    
    /**
     * Checks to see of all of the paraellel queries are done.
     * @return true if all the parallel queries are done.
     */
    public boolean isDoneProcessing()
    {
        for (Boolean bool : doneHash.values())
        {
            if (!bool.booleanValue())
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Cleans up all the data structures.
     *
     */
    public void clear()
    {
        for (QueryResultsContainerIFace qrci : qrcs)
        {
            qrci.clear();
        }

        qrcs.clear();
        doneHash.clear();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.QRCProcessorListener#exectionDone(edu.ku.brc.dbsupport.QueryResultsContainerIFace)
     */
    public synchronized void exectionDone(final QueryResultsContainerIFace qrc)
    {
        doneHash.put(qrc, true);
        if (isDoneProcessing())
        {
            if (!hasFailed)
            {
                listener.allResultsBack();
            }
        } 
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.QRCProcessorListener#executionError(edu.ku.brc.dbsupport.QueryResultsContainerIFace)
     */
    public synchronized void executionError(final QueryResultsContainerIFace qrc)
    {
        doneHash.put(qrc, true);
        hasFailed = true;
        listener.resultsInError(qrc);
    }

}
