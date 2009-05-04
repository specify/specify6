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
package edu.ku.brc.dbsupport;

import java.sql.Connection;
import java.util.List;
import java.util.Vector;

import edu.ku.brc.helpers.SwingWorker;


/**
 * A class that conatains a collection of QueryResultsDatObjects and a single CustomQuery to be executed.
 * Once the results are back this clas is asked to process the results and fill the collection of QueryResultsDataObjects.
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class CustomQueryResultsContainer implements QueryResultsContainerIFace
{
    // Static Data Members
    //private static final Logger log = Logger.getLogger(CustomQueryResultsContainer.class);
    
    // Data Members
    protected CustomQueryIFace                 customQuery;
    protected List<QueryResultsDataObj>   qrdos         = new Vector<QueryResultsDataObj>();
    
    protected QRCProcessorListener        listener      = null;
    protected boolean                     hasFailed     = false;
    
    /**
     * Constructs it with the name of the Custom Query to be executed.
     * @param customQueryName the Sname of the Custom Query to be executed
     */
    public CustomQueryResultsContainer(final String customQueryName)
    {
        customQuery = CustomQueryFactory.getInstance().getQuery(customQueryName);
        //log.debug(customQuery.getName());
    }

    /**
     * Constructs it with the Custom Query to be executed.
     * @param customQuery the Custom Query to be executed
     */
    public CustomQueryResultsContainer(final CustomQueryIFace customQuery)
    {
        this.customQuery = customQuery;
    }

    /**
     * Adds a QueryResultsDataObj to be processed.
     * @param qrdo
     */
    public void add(final QueryResultsDataObj qrdo)
    {
        qrdos.add(qrdo);
    }
    
    /**
     * Returns a list of QueryResultsDataObj objects.
     * @return Returns a list of QueryResultsDataObj objects
     */
    public List<QueryResultsDataObj> getQueryResultsDataObjs()
    {
        return qrdos;
    }
    
    /**
     * Clears all the data structures.
     *
     */
    public void clear()
    {
        for (QueryResultsDataObj qrdo : qrdos) 
        {
            qrdo.clear();
        }
        qrdos.clear();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.QueryResultsContainerIFace#start(edu.ku.brc.dbsupport.QRCProcessorListener, java.sql.Connection)
     */
    public synchronized void start(final QRCProcessorListener listenerArg, final Connection connection)
    {
        // NOTE: We ignore the connection
        
        this.listener = listenerArg;
        
        final CustomQueryResultsContainer thisItem = this;

        final SwingWorker worker = new SwingWorker()
        {
            protected boolean inError = false;
            public Object construct()
            {
                //log.debug("Executing Query: "+customQuery.getName());
                inError = !customQuery.execute();
                //log.debug("Executing Query: "+customQuery.getName()+" inError: "+inError);
                return null;
            }

            //Runs on the event-dispatching thread.
            public void finished()
            {
                if (inError)
                {
                    listenerArg.executionError(thisItem);
                    hasFailed = true;
                    
                } else
                {
                    //log.debug("Processing Results: "+customQuery.getName());

                    List<?> list = customQuery.getDataObjects();
                    for (QueryResultsDataObj qrdo : qrdos) 
                    {
                        if (qrdo.isProcessable())
                        {
                            int col = qrdo.getCol();
                            qrdo.setResult(list.get(col-1));
                            //log.debug("Processing Results Data: "+list.get(col-1));
                        }
                    }
                    listenerArg.exectionDone(thisItem);
                    hasFailed = false;
                }
            }
        };
        worker.start();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.QueryResultsContainerIFace#hasFailed()
     */
    public synchronized boolean hasFailed()
    {
        return hasFailed;
    }
}
