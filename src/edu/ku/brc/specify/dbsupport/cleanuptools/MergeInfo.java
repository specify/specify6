/* Copyright (C) 2012, University of Kansas Center for Research
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
import java.util.List;

import edu.ku.brc.af.core.db.DBTableInfo;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 14, 2012
 *
 */
public class MergeInfo
{
    private boolean            isParent;
    private boolean            isSingle;
    private DBTableInfo        tblInfo;
    private List<MergeInfo>    items = null;
    
    private List<MergeInfoItem> mergeItems;

    /**
     * @param isParent
     * @param tblInfo
     * @param mergeItems
     */
    public MergeInfo(final boolean isParent, 
                     final boolean isSingle, 
                     final DBTableInfo tblInfo, 
                     final List<MergeInfoItem> mergeItems)
    {
        super();
        this.isParent   = isParent;
        this.isSingle   = isSingle;
        this.tblInfo    = tblInfo;
        this.mergeItems = mergeItems;
    }
    
    /**
     * @param mi
     */
    public void add(final MergeInfo mi)
    {
        if (items == null)
        {
            items = new ArrayList<MergeInfo>();
        }
        items.add(mi);
    }
    
    /**
     * @return
     */
    public MergeInfoItem getMergeInto()
    {
        for (MergeInfoItem mi : mergeItems)
        {
            if (mi.isMergedInto())
            {
                return mi;
            }
        }
        return null;
    }
    
    /**
     * @return
     */
    public List<MergeInfoItem> getMergeFrom()
    {
        ArrayList<MergeInfoItem> list = new ArrayList<MergeInfoItem>();
        for (MergeInfoItem mi : mergeItems)
        {
            if (mi.isMergedFrom())
            {
                list.add(mi);
            }
        }
        return list;
    }
    
    /**
     * @return
     */
    public List<MergeInfoItem> getMergeIncludedInternal(final boolean isIncluded)
    {
        ArrayList<MergeInfoItem> list = new ArrayList<MergeInfoItem>();
        for (MergeInfoItem mi : mergeItems)
        {
            if (mi.isIncluded() == isIncluded)
            {
                list.add(mi);
            }
        }
        return list;
    }
    
    /**
     * @return
     */
    public List<MergeInfoItem> getMergeIncluded()
    {
        return getMergeIncludedInternal(true);
    }
    
    /**
     * @return
     */
    public List<MergeInfoItem> getMergeNotIncluded()
    {
        return getMergeIncludedInternal(false);
    }
    
    /**
     * @return
     */
    public Iterable<MergeInfo> kidItems()
    {
        return items;
    }
    
    /**
     * @return
     */
    public Iterable<MergeInfoItem> items()
    {
        return mergeItems;
    }

    /**
     * @return the isParent
     */
    public boolean isParent()
    {
        return isParent;
    }

    /**
     * @param isParent the isParent to set
     */
    public void setParent(boolean isParent)
    {
        this.isParent = isParent;
    }

    /**
     * @return the tblInfo
     */
    public DBTableInfo getTblInfo()
    {
        return tblInfo;
    }

    /**
     * @param tblInfo the tblInfo to set
     */
    public void setTblInfo(DBTableInfo tblInfo)
    {
        this.tblInfo = tblInfo;
    }

    /**
     * @return the isSingle
     */
    public boolean isSingle()
    {
        return isSingle;
    }

    /**
     * @param mergeItems the mergeItems to set
     */
    public void setMergeItems(List<MergeInfoItem> mergeItems)
    {
        this.mergeItems = mergeItems;
    }
    
    
}
