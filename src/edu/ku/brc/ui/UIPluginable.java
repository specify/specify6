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
package edu.ku.brc.ui;

import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

/*
 * @code_status Beta
 **
 * @author rods
 *
 */
public interface UIPluginable
{
    /**
     * Sets the property list into the plugin control
     * @param properties the map of properties
     * @param isViewMode indicates whether the plugin is being created to be viewed or is in edit mode.
     */
    public abstract void initialize(Properties properties, boolean isViewMode);
    
    /**
     * Sets the name of the cell that this represents.
     * @param cellName the name
     */
    public abstract void setCellName(String cellName);
    
     /**
     * Sets a single ChangeListener.
     * @param listener the listener
     */
    public abstract void setChangeListener(ChangeListener listener);
    
    /**
     * @return the UI component for the plugin
     */
    public abstract JComponent getUIComponent();
    
    
    /**
     * Tells the plugin to cleanup because the form is going away. 
     */
    public abstract void shutdown();

}
