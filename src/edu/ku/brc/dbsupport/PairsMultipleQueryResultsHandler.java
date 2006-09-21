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
 * This class processes a collection container and places all the results in a collection. It makes it easy to process them in a single collection
 * instead of having to understand how to traverse multiple QRCs and QRCDOs.
 * (This class morphed so it is missed named)
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class PairsMultipleQueryResultsHandler implements QueryResultsHandlerIFace
{
    private QueryResultsGetter                    getter   = null;
    private java.util.List<QueryResultsContainer> qrcs     = null;
    private QueryResultsListener                  listener = null;
    
    /**
     * 
     * Default Constructor
     */
    public PairsMultipleQueryResultsHandler()
    {
    }
    
    
    //-------------------------------------------
    // QueryResultsHandlerIFace
    //-------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.QueryResultsHandlerIFace#init(edu.ku.brc.specify.dbsupport.QueryResultsListener, java.util.List)
     */
    public void init(final QueryResultsListener listenerArg, final java.util.List<QueryResultsContainer> list)
    {
        this.listener = listenerArg;
        qrcs          = list; // XXX should we copy to the list instead of just wacking it??
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.QueryResultsHandlerIFace#init(edu.ku.brc.specify.dbsupport.QueryResultsListener, edu.ku.brc.specify.dbsupport.QueryResultsContainer)
     */
    public void init(final QueryResultsListener dataArg, final QueryResultsContainer qrc)
    {
        this.listener = dataArg;
        if (qrcs == null)
        {
            qrcs = new Vector<QueryResultsContainer>();
        } else
        {
            qrcs.clear(); // XXX do more clean up
        }
        qrcs.add(qrc);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.QueryResultsHandlerIFace#startUp()
     */
    public synchronized void startUp()
    {
        getter = new QueryResultsGetter(listener);
        getter.add(qrcs);// this needs to be done after everything has been added to the qrc in the qrcs
    }
     
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.QueryResultsHandlerIFace#cleanUp()
     */
    public void cleanUp()
    {
        getter = null;
        qrcs.clear(); 
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.QueryResultsHandlerIFace#getDataObjects()
     */
    public List<Object> getDataObjects()
    {
        Vector<Object> list = new Vector<Object>();
        for (QueryResultsContainer qrc : qrcs)
        {
            java.util.List<QueryResultsDataObj> qrdos = qrc.getQueryResultsDataObjs();
            for (int i=0;i<qrdos.size();i++)
            {         
                list.add(qrdos.get(i).getResult());
            }

        }        
        return list;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.QueryResultsHandlerIFace#isPairs()
     */
    public boolean isPairs()
    {
        return true;
    }

    
}
