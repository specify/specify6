/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.af.core;

import javax.swing.MenuElement;

/**
 * Describes a menu item for a plugin.<br> The path is a string with "/" separator describes the menuitem path from the root.
 * 
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class MenuItemDesc
{
    public enum Position {None, Top, Before, After, Bottom}
    
    protected MenuElement menuItem;
    protected String      menuPath;
    
    protected Position    position = Position.None;
    protected String      posMenuItemName;
    
    protected Position    sepPosition = Position.None;
    
    /**
     * Construct the info oject with the menuitem component
     * @param menuItem the menuitem
     * @param menuPath the path to the item
     */
    public MenuItemDesc(final MenuElement menuItem, final String menuPath)
    {
        this(menuItem, menuPath, Position.None);
    }
    
    /**
     * Construct the info object with the menuitem component
     * @param menuItem the menuitem
     * @param menuPath the path to the item
     * @param position the position
     */
    public MenuItemDesc(final MenuElement menuItem, final String menuPath, final Position position)
    {
        this.menuItem = menuItem;
        this.menuPath = menuPath;
        this.position = position;
    }
    
    /**
     * @param position
     * @param posMenuItemName
     */
    public void setPosition(final Position position, final String posMenuItemName)
    {
        this.position        = position;
        this.posMenuItemName = posMenuItemName;
    }

    /**
     * @return the sepPosition
     */
    public Position getSepPosition()
    {
        return sepPosition;
    }

    /**
     * @param sepPosition the sepPosition to set
     */
    public void setSepPosition(Position sepPosition)
    {
        this.sepPosition = sepPosition;
    }

    /**
     * @return the position
     */
    public Position getPosition()
    {
        return position;
    }

    public void setPosition(Position position)
    {
        this.position = position;
    }

    /**
     * @return the posMenuItemName
     */
    public String getPosMenuItemName()
    {
        return posMenuItemName;
    }

    public MenuElement getMenuItem()
    {
        return menuItem;
    }

    public void setMenuItem(MenuElement menuItem)
    {
        this.menuItem = menuItem;
    }

    public String getMenuPath()
    {
        return menuPath;
    }

    public void setMenuPath(String menuPath)
    {
        this.menuPath = menuPath;
    }
    
}
