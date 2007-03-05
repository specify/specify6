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
 * A custom query for creating percentage of growth for each year
 *
 * @code_status Unknown (auto-generated)
 * 
 * @author rods
 *
 */
public class PercentageGrowthCustomQuery implements CustomQuery
{
    public PercentageGrowthCustomQuery()
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
     * @see edu.ku.brc.dbsupport.CustomQuery#execute()
     */
    public boolean execute()
    {
        throw new NotImplementedException("execute is not implemented!");
    }
   
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQuery#getResults()
     */
    public List<QueryResultsDataObj> getResults()
    {
        throw new NotImplementedException("getResults is not implemented!");
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
        QueryResultsContainer ndbrc = new QueryResultsContainer("select count(*) from collectionobject where TimestampCreated < '2001-01-01' and TimestampCreated > '1999-12-31'; ");
        ndbrc.add(new QueryResultsDataObj("2002"));
        ndbrc.add(new QueryResultsDataObj(1, 1));
        list.addElement(ndbrc);
        
        ndbrc = new QueryResultsContainer("select count(*) from collectionobject where TimestampCreated < '2002-01-01' and TimestampCreated > '2000-12-31'; ");
        ndbrc.add(new QueryResultsDataObj("2003"));
        ndbrc.add(new QueryResultsDataObj(1, 1));
        list.addElement(ndbrc);
        
        ndbrc = new QueryResultsContainer("select count(*) from collectionobject where TimestampCreated < '2003-01-01' and TimestampCreated > '2001-12-31'; ");
        ndbrc.add(new QueryResultsDataObj("2004"));
        ndbrc.add(new QueryResultsDataObj(1, 1));
        list.addElement(ndbrc);
        
        ndbrc = new QueryResultsContainer("select count(*) from collectionobject where TimestampCreated < '2004-01-01' and TimestampCreated > '2002-12-31'; ");
        ndbrc.add(new QueryResultsDataObj("2005"));
        ndbrc.add(new QueryResultsDataObj(1, 1));
        list.addElement(ndbrc);
        
        ndbrc = new QueryResultsContainer("select count(*) from collectionobject where TimestampCreated < '2005-01-01' and TimestampCreated > '2003-12-31'; ");
        ndbrc.add(new QueryResultsDataObj("2006"));
        ndbrc.add(new QueryResultsDataObj(1, 1));
        list.addElement(ndbrc);
        
        /*ndbrc = new QueryResultsContainer("2005", "select count(*) from collectionobject where TimestampCreated < '2006-01-01' and TimestampCreated > '2004-12-31'; ");
        ndbrc.add(new QueryResultsDataObj("2005"));
        ndbrc.add(new QueryResultsDataObj(1, 1));
        list.addElement(ndbrc);
        */
        
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
