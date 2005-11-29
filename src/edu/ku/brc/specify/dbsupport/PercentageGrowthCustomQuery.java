/* Filename:    $RCSfile: PercentageGrowthCustomQuery.java,v $
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

import java.util.Vector;

public class PercentageGrowthCustomQuery implements CustomQuery
{
    public PercentageGrowthCustomQuery()
    {
        
    }
    
    //-------------------------------------------
    // CustomQuery Interface
    //-------------------------------------------
    public java.util.List<QueryResultsContainer> getQueryDefinition()
    {
        Vector<QueryResultsContainer> list = new Vector<QueryResultsContainer>();
        QueryResultsContainer ndbrc = new QueryResultsContainer("select count(*) from collectionobject where TimestampCreated < '2001-01-01' and TimestampCreated > '1999-12-31'; ");
        ndbrc.add(new QueryResultsDataObj("2000"));
        ndbrc.add(new QueryResultsDataObj(1, 1));
        list.addElement(ndbrc);
        
        ndbrc = new QueryResultsContainer("select count(*) from collectionobject where TimestampCreated < '2002-01-01' and TimestampCreated > '2000-12-31'; ");
        ndbrc.add(new QueryResultsDataObj("2001"));
        ndbrc.add(new QueryResultsDataObj(1, 1));
        list.addElement(ndbrc);
        
        ndbrc = new QueryResultsContainer("select count(*) from collectionobject where TimestampCreated < '2003-01-01' and TimestampCreated > '2001-12-31'; ");
        ndbrc.add(new QueryResultsDataObj("2002"));
        ndbrc.add(new QueryResultsDataObj(1, 1));
        list.addElement(ndbrc);
        
        ndbrc = new QueryResultsContainer("select count(*) from collectionobject where TimestampCreated < '2004-01-01' and TimestampCreated > '2002-12-31'; ");
        ndbrc.add(new QueryResultsDataObj("2003"));
        ndbrc.add(new QueryResultsDataObj(1, 1));
        list.addElement(ndbrc);
        
        ndbrc = new QueryResultsContainer("select count(*) from collectionobject where TimestampCreated < '2005-01-01' and TimestampCreated > '2003-12-31'; ");
        ndbrc.add(new QueryResultsDataObj("2004"));
        ndbrc.add(new QueryResultsDataObj(1, 1));
        list.addElement(ndbrc);
        
        /*ndbrc = new QueryResultsContainer("2005", "select count(*) from collectionobject where TimestampCreated < '2006-01-01' and TimestampCreated > '2004-12-31'; ");
        ndbrc.add(new QueryResultsDataObj("2005"));
        ndbrc.add(new QueryResultsDataObj(1, 1));
        list.addElement(ndbrc);
        */
        
        return list;
    }

}
