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

package edu.ku.brc.af.core;

import java.util.Vector;

/**
 * This class is used for collecting all the record primary keys for a given Express Definition.
 *
 * @code_status Beta
 *
 * @author rods
 *
 */
public class ExpressSearchResults
{
    protected String                  searchId;
    protected Integer                 joinColTableId;
    protected ExpressResultsTableInfo tableInfo;
    protected Vector<Long>            recIds   = new Vector<Long>();
    protected Vector<Integer>         indexes  = new Vector<Integer>();

    /**
     * Constructs the Results.
     * @param searchId The Express Search definition ID
     * @param joinColTableId the Join's Table ID (may be null)
     * @param tableInfo the table info 
     */
    public ExpressSearchResults(final String                  searchId, 
                                final Integer                 joinColTableId, 
                                final ExpressResultsTableInfo tableInfo)
    {
        super();
        this.searchId    = searchId;
        this.joinColTableId = joinColTableId;
        this.tableInfo   = tableInfo;
    }
    
    /**
     * Cleans up references to other objects.
     */
    public void cleanUp()
    {
        tableInfo = null;
        
        recIds.clear();
        recIds = null;
    }
    
    public void add(long id)
    {
        recIds.add(id);
    }

    public void add(String idStr)
    {
        recIds.add(Long.parseLong(idStr)); 
    }
    

    /**
     * Returns the number of indexes.
     * @return the number of indexes
     */
    public int getNumIndexes()
    {
        return indexes.size();
    }

    /**
     * Adds an index.
     * @param index the index to add
     */
    public void addIndex(int index)
    {
        indexes.add(index);
    }

    /**
     * Returns the array of indexes.
     * @return the array of indexes
     */
    public int[] getIndexes()
    {
        int[] inxs = new int[indexes.size()];
        int inx = 0;
        for (Integer i : indexes)
        {
            inxs[inx++] = i;
        }
        indexes.clear();
        return inxs;
    }

    public String getSearchId()
    {
        return searchId;
    }

    public Integer getJoinColTableId()
    {
        return joinColTableId;
    }

    public Vector<Long> getRecIds()
    {
        return recIds;
    }

    public ExpressResultsTableInfo getTableInfo()
    {
        return tableInfo;
    }
    

    public String getRecIdList()
    {
        StringBuffer idsStr = new StringBuffer(recIds.size()*8);
        for (int i=0;i<recIds.size();i++)
        {
            if (i > 0) idsStr.append(',');
            idsStr.append(recIds.elementAt(i).toString());
        }
        return idsStr.toString();
    }
}
