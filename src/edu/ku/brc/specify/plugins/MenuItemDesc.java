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

package edu.ku.brc.specify.plugins;

import javax.swing.MenuElement;

public class MenuItemDesc
{
    protected MenuElement menuItem;
    protected String      menuPath;
    
    public MenuItemDesc(final MenuElement menuItem, final String menuPath)
    {
        this.menuItem = menuItem;
        this.menuPath = menuPath;
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
