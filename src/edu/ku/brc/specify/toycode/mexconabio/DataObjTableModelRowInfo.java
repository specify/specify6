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
package edu.ku.brc.specify.toycode.mexconabio;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 3, 2010
 *
 */
public class DataObjTableModelRowInfo
{
    int     id;
    boolean isMainRecord;
    boolean isIncluded;

    
    /**
     * @param id
     * @param isMainRecord
     * @param isIncluded
     */
    public DataObjTableModelRowInfo(int id, boolean isMainRecord, boolean isIncluded)
    {
        super();
        this.id = id;
        this.isMainRecord = isMainRecord;
        this.isIncluded = isIncluded;
    }
    
    /**
     * @return the id
     */
    public int getId()
    {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id)
    {
        this.id = id;
    }

    /**
     * @return the isMainRecord
     */
    public boolean isMainRecord()
    {
        return isMainRecord;
    }
    /**
     * @param isMainRecord the isMainRecord to set
     */
    public void setMainRecord(boolean isMainRecord)
    {
        this.isMainRecord = isMainRecord;
    }
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
}