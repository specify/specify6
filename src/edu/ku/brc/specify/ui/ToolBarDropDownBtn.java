/* Filename:    $RCSfile: ToolBarDropDownBtn.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.ui;

import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;

/**
 * Toolbar button derived from DropDownBtn, this provides a way to set menu items
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ToolBarDropDownBtn extends DropDownButton
{
     /**
     * Creates a toolbar item with label and icon and their positions.
     * @param label label of the toolbar item
     * @param icon the icon
     * @param textPosition the position of the text as related to the icon
     */
    public ToolBarDropDownBtn(final String label, final Icon icon, final int textPosition)
    {
        super(label, icon, textPosition);
    }

    /**
     * Creates a toolbar item with label and icon and their positions and menu items to be added.
     * The Items MUST be of class JSeparator or JMenuItem.
     * @param label label of the toolbar item
     * @param icon the icon
     * @param textPosition the position of the text as related to the icon
     * @param menus the list of menu items and separators
     */
    public ToolBarDropDownBtn(final String label, final Icon icon, final int textPosition, final List<JComponent> menus)
    {
        super(label, icon, textPosition, menus);      
     }

    /**
     * @param icon the icon for the button
     */
    public ToolBarDropDownBtn(final Icon icon)
    {
        super(icon);

    }
 }
