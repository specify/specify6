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
     * @param isViewMode indicates whether the plugin is being created to be vewed or is in edit mode.
     */
    public void initialize(Properties properties, boolean isViewMode);
    
    /**
     * Sets the name of the cell that this represents.
     * @param cellName the name
     */
    public void setCellName(String cellName);
    
    /**
     * Indicates this is for display and NOT editting, or it is for editting.
     * @param isDisplayOnly true - display, false - editting
     */
    public void setIsDisplayOnly(boolean isDisplayOnly);
    
    /**
     * Sets a single ChangeListener.
     * @param listener the listener
     */
    public void setChangeListener(ChangeListener listener);
    
    public JComponent getUIComponent();

}
