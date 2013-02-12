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
package edu.ku.brc.specify.tasks.subpane.collab;

import javax.swing.Icon;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Nov 29, 2011
 *
 */
public class NotiLogInfo
{
    private Icon    icon;
    private String  desc;
    private boolean isNewData;
    private String  action;
    private boolean isBatch;
    private int     numRecords;
    private String  source;
    /**
     * @param icon
     * @param desc
     * @param isNewData
     * @param action
     * @param isBatch
     * @param numRecords
     * @param source
     */
    public NotiLogInfo(Icon icon, String desc, boolean isNewData, String action, boolean isBatch,
            int numRecords, String source)
    {
        super();
        this.icon = icon;
        this.desc = desc;
        this.isNewData = isNewData;
        this.action = action;
        this.isBatch = isBatch;
        this.numRecords = numRecords;
        this.source = source;
    }
    /**
     * @return the icon
     */
    public Icon getIcon()
    {
        return icon;
    }
    /**
     * @return the desc
     */
    public String getDesc()
    {
        return desc;
    }
    /**
     * @return the isNewData
     */
    public boolean isNewData()
    {
        return isNewData;
    }
    /**
     * @return the action
     */
    public String getAction()
    {
        return action;
    }
    /**
     * @return the isBatch
     */
    public boolean isBatch()
    {
        return isBatch;
    }
    /**
     * @return the numRecords
     */
    public int getNumRecords()
    {
        return numRecords;
    }
    /**
     * @return the source
     */
    public String getSource()
    {
        return source;
    }
    
    
}
