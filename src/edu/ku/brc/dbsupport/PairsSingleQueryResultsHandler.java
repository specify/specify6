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
 * This class processes a single container and places all the results in a collection
 * (This class morphed so it is missed named) 
 * (This class needs to be explained better - thus Code Freeze).
 *
 * @code_status Code Freeze
 * 
 * @author rods
 *
 */
public class PairsSingleQueryResultsHandler implements QueryResultsHandlerIFace
{
    private QueryResultsGetter    getter    = null;
    private QueryResultsContainer container = null;
    private QueryResultsListener  listener  = null;

    /**
     * Default Constructor
     *
     */
    public PairsSingleQueryResultsHandler()
    {
    }
    
    //-------------------------------------------
    // QueryResultsHandlerIFace
    //-------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.QueryResultsHandlerIFace#init(edu.ku.brc.specify.dbsupport.QueryResultsListener, java.util.List)
     */
    public void init(final QueryResultsListener listener, final java.util.List<QueryResultsContainer> list)
    {
        throw new RuntimeException("PairsSingleQueryResultsHandler can't handle more than one QueryResultsContainer!");
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.QueryResultsHandlerIFace#init(edu.ku.brc.specify.dbsupport.QueryResultsListener, edu.ku.brc.specify.dbsupport.QueryResultsContainer)
     */
    public void init(final QueryResultsListener listener, final QueryResultsContainer container)
    {
        this.listener = listener;
        this.container = container;   
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.QueryResultsHandlerIFace#startUp()
     */
    public void startUp()
    {
       getter = new QueryResultsGetter(listener);   
       getter.add(container); // this needs to be done after everything has been added to the container
                              // by adding it, it starts the processing
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.QueryResultsHandlerIFace#cleanUp()
     */
    public void cleanUp()
    {
        listener = null;
        container.clear();
        container = null;
    }
   
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.QueryResultsHandlerIFace#getDataObjects()
     */
    public List<Object> getDataObjects()
    {
        Vector<Object> list = new Vector<Object>();
        java.util.List<QueryResultsDataObj> qrdos = container.getQueryResultsDataObjs();
        for (int i=0;i<qrdos.size();i++)
        {         
            list.add(qrdos.get(i).getResult());
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
