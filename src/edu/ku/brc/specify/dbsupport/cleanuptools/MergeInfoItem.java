/* Copyright (C) 2013, University of Kansas Center for Research
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


/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 14, 2012
 *
 */
public class MergeInfoItem
{
    protected Integer     id;
    protected boolean     isMergedInto;
    protected boolean     isMergedFrom;
    protected boolean     isIncluded;
    
    /**
     * @param id
     * @param isMergedInto
     * @param isMergedFrom
     * @param isIncluded
     */
    public MergeInfoItem(Integer id, boolean isMergedInto, boolean isMergedFrom, boolean isIncluded)
    {
        super();
        this.id = id;
        this.isMergedInto = isMergedInto;
        this.isMergedFrom = isMergedFrom;
        this.isIncluded   = isIncluded;
    }

    /**
     * @return the id
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * @return the isMergedInto
     */
    public boolean isMergedInto()
    {
        return isMergedInto;
    }

    /**
     * @return the isMergedFrom
     */
    public boolean isMergedFrom()
    {
        return isMergedFrom;
    }

    /**
     * @return the isIncluded
     */
    public boolean isIncluded()
    {
        return isIncluded;
    }
}
