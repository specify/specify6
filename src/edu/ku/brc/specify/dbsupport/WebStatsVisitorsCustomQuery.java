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

package edu.ku.brc.specify.dbsupport;

import java.util.Vector;

import edu.ku.brc.dbsupport.CustomQuery;
import edu.ku.brc.dbsupport.QueryResultsContainer;
import edu.ku.brc.dbsupport.QueryResultsDataObj;

/**
 * Custom Query for calculating web stats for Visitors (SPNHC Demo)
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
public class WebStatsVisitorsCustomQuery implements CustomQuery
{

    public WebStatsVisitorsCustomQuery()
    {

    }

    //-------------------------------------------
    // CustomQuery Interface
    //-------------------------------------------
    public java.util.List<QueryResultsContainer> getQueryDefinition()
    {
        Vector<QueryResultsContainer> list = new Vector<QueryResultsContainer>();
        for (int i=1;i<7;i++)
        {
            QueryResultsContainer ndbrc = new QueryResultsContainer("select UniqueVisitorsMon"+i+", UniqueVisitors"+i+" from webstats");
            ndbrc.add(new QueryResultsDataObj(1, 1));
            ndbrc.add(new QueryResultsDataObj(1, 2));
            list.addElement(ndbrc);
        }
        return list;
    }

}
