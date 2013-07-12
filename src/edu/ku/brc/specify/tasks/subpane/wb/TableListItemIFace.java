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
package edu.ku.brc.specify.tasks.subpane.wb;

import javax.swing.ImageIcon;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 6, 2007
 *
 */
public interface TableListItemIFace
{

    /**
     * @return
     */
    public abstract ImageIcon getIcon();
    
    /**
     * @return
     */
    public abstract String getText();
    
    //public abstract String getTitle();
    
    
    //------------------------------
    // For Tables
    //------------------------------
    /**
     * @return
     */
    public abstract boolean isExpandable();
    
    /**
     * @return
     */
    public abstract boolean isExpanded();
    
    /**
     * @param expand
     */
    public abstract void setExpanded(boolean expand);
    
    //------------------------------
    // For Fields
    //------------------------------
    
    /**
     * @return
     */
    public abstract boolean isChecked();
    
    /**
     * @param checked
     */
    public abstract void setChecked(boolean checked);
    
    
}
