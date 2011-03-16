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
package edu.ku.brc.specify.plugins.sgr;

import javax.swing.ImageIcon;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Mar 15, 2011
 *
 */
public class DataResultsRow
{
    protected GroupingColObjData grpRawData;
    protected GroupingColObjData grpSNIBData;
    protected RawData            rawData;
    
    protected ImageIcon          imgIcon = null;
    
    /**
     * @param grpRawData
     * @param rawData
     */
    public DataResultsRow(GroupingColObjData grpRawData, RawData rawData)
    {
        super();
        this.grpRawData = grpRawData;
        this.rawData = rawData;
    }
    
    /**
     * @param grpRawData
     * @param rawData
     * @param snibData
     */
    public DataResultsRow(GroupingColObjData grpRawData, GroupingColObjData grpSNIBData, RawData rawData)
    {
        super();
        this.grpRawData = grpRawData;
        this.grpSNIBData = grpSNIBData;
        this.rawData = rawData;
    }

    /**
     * @return the imgIcon
     */
    public ImageIcon getImgIcon()
    {
        return imgIcon;
    }

    /**
     * @param imgIcon the imgIcon to set
     */
    public void setImgIcon(ImageIcon imgIcon)
    {
        this.imgIcon = imgIcon;
    }

    /**
     * @return the grpRawData
     */
    public GroupingColObjData getGrpRawData()
    {
        return grpRawData;
    }
    /**
     * @param grpRawData the grpRawData to set
     */
    public void setGrpRawData(GroupingColObjData grpRawData)
    {
        this.grpRawData = grpRawData;
    }
    /**
     * @return the rawData
     */
    public RawData getRawData()
    {
        return rawData;
    }
    /**
     * @param rawData the rawData to set
     */
    public void setRawData(RawData rawData)
    {
        this.rawData = rawData;
    }
    /**
     * @return the grpSNIBData
     */
    public GroupingColObjData getGrpSNIBData()
    {
        return grpSNIBData;
    }
    /**
     * @param grpSNIBData the grpSNIBData to set
     */
    public void setGrpSNIBData(GroupingColObjData grpSNIBData)
    {
        this.grpSNIBData = grpSNIBData;
    }
}
