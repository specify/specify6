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

import java.util.Calendar;
import java.util.Vector;

import edu.ku.brc.dbsupport.CustomQuery;
import edu.ku.brc.dbsupport.QueryResultsContainer;
import edu.ku.brc.dbsupport.QueryResultsDataObj;

/**
 * Custom Query for calculating the how many items were calculated for each year
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
public class CatByPrev10YearsCustomQuery implements CustomQuery
{

    public CatByPrev10YearsCustomQuery()
    {
    }

    protected String createTitle(final int year, final int inc)
    {
        return year + " - " + (year - inc);
    }

    //-------------------------------------------
    // CustomQuery Interface
    //-------------------------------------------
    public java.util.List<QueryResultsContainer> getQueryDefinition()
    {
        int inc = 5;
        int year = Calendar.getInstance().get(Calendar.YEAR) - 1;

        Vector<QueryResultsContainer> list = new Vector<QueryResultsContainer>();
        QueryResultsContainer ndbrc;

        for (int i=0;i<10;i++)
        {
            ndbrc = new QueryResultsContainer("select count(*) from collectionobject where CatalogedDate < '"+(year)+"-01-01' and CatalogedDate > '"+(year-10)+"-12-31'; ");
            ndbrc.add(new QueryResultsDataObj(createTitle(year, inc)));
            ndbrc.add(new QueryResultsDataObj(1, 1));
            list.addElement(ndbrc);
            year -= inc;
        }
        return list;
    }

}
