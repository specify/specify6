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
package edu.ku.brc.helpers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JMenuItem;

/**
 *
 * @code_status Unknown (auto-generated)
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
            
        } else if (propertyName.equals("enabled")) //$NON-NLS-1$
        {
            Boolean enabledState = (Boolean) e.getNewValue();
            menuItem.setEnabled(enabledState.booleanValue());
        }
    }
}

