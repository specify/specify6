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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComponent;

/**
 *
 * @code_status Beta
 * 
 * @author rods
 *
 * Currently Not Used
 */
public class ActionChangedListener implements PropertyChangeListener
{
   private Vector<JComponent> items = new Vector<JComponent>(); 
    
    /**
     * Constructor
     * @param uiComp the component
     */
    public ActionChangedListener(JComponent uiComp)
    {
        super();
        add(uiComp);
    }
    
    /**
     * Add the component
     * @param uiComp the component
     */
    public void add(JComponent uiComp)
    {
        items.addElement(uiComp);
    }
    
    /**
     * Remove the component
     * @param uiComp the component
     */
    public void remove(JComponent uiComp)
    {
        items.removeElement(uiComp);
    }
    
    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent e)
    {
        for (JComponent comp : items)
        {
            String propertyName = e.getPropertyName();
            if (comp instanceof AbstractButton && e.getPropertyName().equals(Action.NAME))
            {
                String text = (String) e.getNewValue();
                ((AbstractButton)comp).setText(text);
                
            } else if (propertyName.equals("enabled"))
            {
                Boolean enabledState = (Boolean) e.getNewValue();
                comp.setEnabled(enabledState.booleanValue());
            }
        }
    }
}
