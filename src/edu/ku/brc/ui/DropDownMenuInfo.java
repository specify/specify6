/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
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
    
    public DropDownMenuInfo(String label, 
                    ImageIcon imageIcon, 
                    String tooltip)
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
