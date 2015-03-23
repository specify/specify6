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

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.lang.StringUtils;

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
    protected boolean        isIncluded;
    protected int            id;
    protected Object         value;
    protected String         title;
    protected HashSet<Integer> duplicateIds = new HashSet<Integer>();
    
    /**
     * @param id
     * @param value
     */
    public FindItemInfo(final int id, final Object value)
    {
        super();
        this.isIncluded = true;
        this.id         = id;
        this.value      = value;
    }

    /**
     * @param id
     * @param value
     */
    public FindItemInfo(final int id, final Object value, final String title)
    {
        this(id, value);
        this.title = title;
    }
    
    public int getCount()
    {
        return duplicateIds.size();
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

    public void addDuplicate(final int id)
    {
        duplicateIds.add(id);
    }
    
    /**
     * @param includePrimaryId
     * @return
     */
    /*public String getInClause(final boolean includePrimaryId)
    {
        return getInClause(includePrimaryId, null);
    }*/
    
    /**
     * @return the isIncluded
     */
    public boolean isIncluded()
    {
        return isIncluded;
    }

    /**
     * @param isIncluded the isIncluded to set
     */
    public void setIncluded(boolean isIncluded)
    {
        this.isIncluded = isIncluded;
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * @param includePrimaryId
     * @param usedIds
     * @return
     */
    public String getInClause(final boolean includePrimaryId)
    {
        StringBuilder sb = new StringBuilder();
        for (Integer id : duplicateIds)
        {
            if (sb.length() > 0) sb.append(',');
            sb.append(id);
        }
        
        if (includePrimaryId)
        {
            if (sb.length() > 0) sb.append(',');
            sb.append(id);
        }
        return "(" + sb.toString() +")";
    }
    
    /**
     * @param value the value to set
     */
    public void setValue(Object value)
    {
        this.value = value;
    }

    /**
     * @return the duplicateIds
     */
    public int cleanDuplicateIds(final HashSet<Integer> usedIds)
    {
        ArrayList<Integer> ids = new ArrayList<Integer>(duplicateIds);
        duplicateIds.clear();
        for (Integer dupId : ids)
        {
            if (usedIds == null || !usedIds.contains(dupId))
            {
                duplicateIds.add(dupId);
            }
        }
        return duplicateIds.size();
    }
    
    /**
     * @return the duplicateIds
     */
    public HashSet<Integer> getDuplicateIds()
    {
        return duplicateIds;
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
