/* Filename:    $RCSfile: PairsSingleQueryResultsHandler.java,v $
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

public class PairsSingleQueryResultsHandler implements QueryResultsHandlerIFace
{
    private QueryResultsGetter    getter    = null;
    private QueryResultsContainer container = null;
    private QueryResultsListener  listener  = null;

    public PairsSingleQueryResultsHandler()
    {
    }
    
    //-------------------------------------------
    // QueryResultsHandlerIFace
    //-------------------------------------------
    public void init(final QueryResultsListener listener, final java.util.List<QueryResultsContainer> list)
    {
        throw new RuntimeException("PairsSingleQueryResultsHandler can't handle more than one QueryResultsContainer!");
    }
    
    public void init(final QueryResultsListener listener, final QueryResultsContainer container)
    {
        this.listener = listener;
        this.container = container;   
    }
    
    public void startUp()
    {
       getter = new QueryResultsGetter(listener);   
       getter.add(container); // this needs to be done after everything has been added to the container
                           // by adding it, it starts the processing
    }

    public void cleanUp()
    {

        listener = null;
        container.clear();
        container = null;
    }
   
    public List<QueryResultsContainer> getContainers()
    {
        Vector<QueryResultsContainer> list = new Vector<QueryResultsContainer>();
        list.addElement(container);
        return list;
    }
    
    public List<Object> getDataObjects()
    {
        Vector<Object> list = new Vector<Object>();
        java.util.List<QueryResultsDataObj> qrdos = container.getQueryResultsDataObjs();
        for (int i=0;i<qrdos.size();i++)
        {         
            list.add(qrdos.get(i++).getResult());
            list.add(qrdos.get(i).getResult());
        }
        return list;
    }
    
    public boolean isPairs()
    {
        return true;
    }

    
}
