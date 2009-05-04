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
package edu.ku.brc.ui;

import javax.swing.ImageIcon;

/**
 * Simple class for collecting the information neccessary for creating the drop down menus for the UI Switcher.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Nov 17, 2006
 *
 */
public class DropDownMenuInfo
{
    protected String    label;
    protected ImageIcon imageIcon;
    protected String    tooltip;
    
    public DropDownMenuInfo(final String    label, 
                            final ImageIcon imageIcon, 
                            final String    tooltip)
    {
        super();
        this.label = label;
        this.imageIcon = imageIcon;
        this.tooltip = tooltip;
    }
    
    public String getLabel()
    {
        return label;
    }
    public ImageIcon getImageIcon()
    {
        return imageIcon;
    }
    public String getTooltip()
    {
        return tooltip;
    }
}
