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
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.util.HashSet;
import java.util.Set;

import edu.ku.brc.af.core.db.DBFieldInfo;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 13, 2012
 *
 */
public class DisplayFindItemInfo extends FindItemInfo
{
    private Set<DBFieldInfo> columns       = null;
    private boolean          isSingleCol   = true;
    private int              indexForTitle = 0;
    
    /**
     * @param id
     * @param value
     */
    public DisplayFindItemInfo(final FindItemInfo fii, 
                               final Set<DBFieldInfo> columns)
    {
        super(fii.id, fii.value);
        
        this.title        = fii.title; 
        this.duplicateIds = new HashSet<Integer>(fii.duplicateIds);
        this.columns      = columns;
    }

    /**
     * @param fii
     * @param column
     */
    public DisplayFindItemInfo(final FindItemInfo fii, 
                               final DBFieldInfo column)
    {
        super(fii.id, fii.value);
        
        this.title        = fii.title; 
        this.duplicateIds = new HashSet<Integer>(fii.duplicateIds);
        this.columns      = new HashSet<DBFieldInfo>();
        this.columns.add(column);
    }

    /**
     * @return columns to include in the display whether they have same data or not.
     */
    public Set<DBFieldInfo> getColsToInclude()
    {
        return columns;
    }
    
    /**
     * @return
     */
    public String getFormattedTitle(Object[] rowData)
    {
        if (isSingleCol && rowData[indexForTitle] != null)
        {
            return rowData[indexForTitle].toString();
        }
        return null;
    }
}
