/* Copyright (C) 2009, University of Kansas Center for Research
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

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.specify.dbsupport.cleanuptools.BaseFindCleanupItems.ItemStatusType;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 3, 2010
 *
 */
public class FindItemInfo
{
    private ItemStatusType status;
    private int            id;
    private Object         value;
    private String         title;
    
    /**
     * @param id
     * @param value
     */
    public FindItemInfo(int id, Object value)
    {
        super();
        this.id    = id;
        this.value  = value;
        this.status = ItemStatusType.eOK;
    }

    /**
     * @param id
     * @param value
     */
    public FindItemInfo(int id, Object value, String title)
    {
        this(id, value);
        this.title = title;
    }

    /**
     * @return the status
     */
    public ItemStatusType getStatus()
    {
        return status;
    }

    /**
     * @return the id
     */
    public int getId()
    {
        return id;
    }

    /**
     * @return the value
     */
    public Object getValue()
    {
        return value;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return StringUtils.isNotEmpty(title) ? title : (value != null ? value.toString() : "N/A");
    }
}
