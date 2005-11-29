/* Filename:    $RCSfile: PairsMultipleQueryResultsHandler.java,v $
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
    
    /**
     * 
     */
    public void init(final QueryResultsListener listener, final java.util.List<QueryResultsContainer> list)
    {
        this.listener = listener;
        qrcs          = list; // XXX should we copy to the list instead of just wacking it??
    }
    
    /**
     * 
     */
    public void init(final QueryResultsListener listener, final QueryResultsContainer qrc)
    {
        this.listener = listener;
        if (qrcs == null)
        {
            qrcs = new Vector<QueryResultsContainer>();
        } else
        {
            qrcs.clear(); // XXX do more clean up
        }
        qrcs.add(qrc);
    }

    public synchronized void startUp()
    {
        getter = new QueryResultsGetter(listener);
        for (QueryResultsContainer qrc : qrcs)
        {
            getter.add(qrc); // this needs to be done after everything has been added to the qrc
        }

    }
     
    /**
     * 
     */
    public void cleanUp()
    {
        getter = null;
        qrcs.clear(); 
    }
    
    /**
     * 
     */
    public List<QueryResultsContainer> getContainers()
    {
        return qrcs;
    }
    
    public List<Object> getDataObjects()
    {
        Vector<Object> list = new Vector<Object>();
        for (QueryResultsContainer qrc : qrcs)
        {
            java.util.List<QueryResultsDataObj> qrdos = qrc.getQueryResultsDataObjs();
            for (int i=0;i<qrdos.size();i++)
            {         
                list.add(qrdos.get(i++).getResult());
                list.add(qrdos.get(i).getResult());
            }

        }        
        return list;
    }
    
    /**
     * 
     */
    public boolean isPairs()
    {
        return true;
    }

    
}
