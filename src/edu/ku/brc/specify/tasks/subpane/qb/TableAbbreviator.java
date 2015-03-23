/* Copyright (C) 2015, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.HashMap;
import java.util.Map;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *Assigns unique abbreviations/aliases for tables during the construction of
 *hql/sql for a query.
 */
class TableAbbreviator
{
    
    /**
     * Maps tableIds with the number of occurrences in the query.
     */
    protected Map<Integer, Integer>  idCountMap;
    /**
     * Maps table trees with their assigned abbreviations.
     */
    protected Map<String, String> ttAbbrevMap;
    
    public TableAbbreviator()
    {
        idCountMap = new HashMap<Integer, Integer>();
        ttAbbrevMap = new HashMap<String, String>();
    }
    
    /**
     * @param tt
     * @return a unique (within the current query) sql/hql abbreviation or alias
     * for the table represented by tt.
     */
    public String getAbbreviation(final TableTree tt)
    {
        if (ttAbbrevMap.containsKey(getTableTreeKey(tt)))
        {
            return ttAbbrevMap.get(getTableTreeKey(tt));
        }
        //else
        if (idCountMap.containsKey(tt.getTableInfo().getTableId()))
        {
            return newAbbrev(tt, idCountMap.get(tt.getTableInfo().getTableId()) + 1);
        }
        //else
        return newAbbrev(tt, 0);
    }
    
    protected String getTableTreeKey(final TableTree tt)
    {
        return tt.getPathFromRoot() + tt.getField();
        
    }
    protected String newAbbrev(final TableTree tt, final Integer count)
    {
        idCountMap.put(tt.getTableInfo().getTableId(), count);
        String result = tt.getAbbrev() + count;
        ttAbbrevMap.put(getTableTreeKey(tt), result);
        return result;
    }
    
    public void clear()
    {
        idCountMap.clear();
        ttAbbrevMap.clear();
    }
    
}
