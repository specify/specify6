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

package edu.ku.brc.specify.dbsupport.customqueries;

import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.NotImplementedException;

import edu.ku.brc.dbsupport.CustomQuery;
import edu.ku.brc.dbsupport.CustomQueryListener;
import edu.ku.brc.dbsupport.QueryResultsContainer;
import edu.ku.brc.dbsupport.QueryResultsContainerIFace;
import edu.ku.brc.dbsupport.QueryResultsDataObj;

/**
 * Custom Query for calculating web stats for Taxon searched (SPNHC Demo)
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
public class WebStatsTaxonCustomQuery implements CustomQuery
{

    public WebStatsTaxonCustomQuery()
    {

    }

    //-------------------------------------------
    // CustomQuery Interface
    //-------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#isExecutable()
     */
    public boolean isExecutable()
    {
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#getResults()
     */
    public List<QueryResultsDataObj> getResults()
    {
        throw new NotImplementedException("getResults is not implemented!");
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#execute()
     */
    public boolean execute()
    {
        throw new NotImplementedException("execute is not implemented!");
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#execute(edu.ku.brc.dbsupport.CustomQueryListener)
     */
    public void execute(final CustomQueryListener cql)
    {
        throw new NotImplementedException();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#getDataObjects()
     */
    public List<?> getDataObjects()
    {
        throw new NotImplementedException();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#getQueryDefinition()
     */
    public List<QueryResultsContainerIFace> getQueryDefinition()
    {
        Vector<QueryResultsContainerIFace> list = new Vector<QueryResultsContainerIFace>();
        for (int i=1;i<6;i++)
        {
            QueryResultsContainer ndbrc = new QueryResultsContainer("select Taxon"+i+", TaxonCnt"+i+" from webstats");
            ndbrc.add(new QueryResultsDataObj(1, 1));
            ndbrc.add(new QueryResultsDataObj(1, 2));
            list.addElement(ndbrc);
        }
        return list;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#getName()
     */
    public String getName()
    {
        // TODO Auto-generated method stub
        return getClass().getSimpleName();
    }
}
