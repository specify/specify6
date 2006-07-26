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
package edu.ku.brc.af.core;

import java.util.List;

import javax.swing.JComponent;

/**
 * Describes a Navigation Box (container) of objects. The objects can be commands, queries, 
 * recordsets, anything that can be shown and acts or acts upon.
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
public interface NavBoxIFace
{
    /**
     * Global   - All Users can see items in a 'global' tab. <br>
     * Local    - Local to current User
     * SubPanel - Owned and managed by a SubPanel
     */
    public enum Scope {Global, Local, SubPanel}
    
    /**
     * Returns the scope of the tab.
     * @return returns the scope of the tab
     */
    public Scope getScope();
    
    /**
     *  Sets the scrope.
     * @param scope the new cope
     */
    public void setScope(final Scope scope);
    
    /**
     * Returns the name of the tab (localized).
     * @return the localized name of the tab
     */
    public String getName();
    
    /**
     * Sets the name
     * @param name the new name
     */
    public void setName(final String name);
    
    /**
     * Returns the UI component for this tab.
     * @return the UI component for this tab
     */
    public JComponent getUIComponent();
    
    /**
     * Sets the manager into the object.
     * @param mgr the NavBox Manager
     */
    public void setMgr(NavBoxMgr mgr);
    
    /**
     * Returns the list of NavBoxItemIfaces.
     * @return the list of NavBoxItemIfaces
     */
    public List<NavBoxItemIFace> getItems();
    
}
