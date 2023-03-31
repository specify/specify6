/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.af.core;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

/**
 * Interface of an item to be organized by a NavBox
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public interface NavBoxItemIFace extends Comparable<NavBoxItemIFace>
{
    /**
     * Returns the localized title of the item (it can be used for look ups by others).
     * @return the localized title
     */
    public String getTitle();

    /**
     * Return the UI Component that is used to display the contents of the navigation box.
     * @return the UI Component that is used to display the contents of the navigation box
     */
    public JComponent getUIComponent();

    /**
     * Sets data into the Item.
     * @param data the data (anything really)
     */
    public void setData(Object data);

    /**
     * Returns the data object.
     * @return the data object
     */
    public Object getData();
    
    /**
     * Sest a tooltip string on the item (if appropriate).
     * @param toolTip the already localized tool tip string
     */
    public void setToolTip(String toolTip);
    
    /**
     * Sets a new and/or differet icon for item.
     * @param icon the new icon
     */
    public void setIcon(ImageIcon icon);
    
    /**
     * Returns the Title.
     * @return the title.
     */
    public String toString();
    
    /**
     * Sets whether the item is enabled or not.
     * @param enabled true- enabled, false not
     */
    public void setEnabled(boolean enabled);

    /**
     * Determines if the {@link NavBoxItemIFace} is enabled or not.
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled();
}
