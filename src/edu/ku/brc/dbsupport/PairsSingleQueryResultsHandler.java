/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
    private QueryResultsGetter         getter    = null;
    private QueryResultsContainerIFace container = null;
    private QueryResultsListener       listener  = null;

    /**
     * Default Constructor
     *
     */
    public PairsSingleQueryResultsHandler()
    {
        // no-op
    }
    
    //-------------------------------------------
    // QueryResultsHandlerIFace
    //-------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.QueryResultsHandlerIFace#init(edu.ku.brc.dbsupport.QueryResultsListener, java.util.List)
     */
    public void init(final QueryResultsListener listenerArg, final java.util.List<QueryResultsContainerIFace> list)
    {
        throw new RuntimeException("PairsSingleQueryResultsHandler can't handle more than one QueryResultsContainer!"); //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.QueryResultsHandlerIFace#init(edu.ku.brc.dbsupport.QueryResultsListener, edu.ku.brc.dbsupport.QueryResultsContainerIFace)
     */
    public void init(final QueryResultsListener listenerArg, final QueryResultsContainerIFace containerArg)
    {
        this.listener  = listenerArg;
        this.container = containerArg;   
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
