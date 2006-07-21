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
package edu.ku.brc.specify.helpers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JMenuItem;

/**
 * 
 * @author rods
 *
 */
public class MenuItemPropertyChangeListener implements PropertyChangeListener
{
    JMenuItem menuItem;
    
    public MenuItemPropertyChangeListener(JMenuItem mi)
    {
        super();
        this.menuItem = mi;
    }
    
    public void propertyChange(PropertyChangeEvent e)
    {
        String propertyName = e.getPropertyName();
        if (e.getPropertyName().equals(Action.NAME))
        {
            String text = (String) e.getNewValue();
            menuItem.setText(text);
            
        } else if (propertyName.equals("enabled"))
        {
            Boolean enabledState = (Boolean) e.getNewValue();
            menuItem.setEnabled(enabledState.booleanValue());
        }
    }
}

