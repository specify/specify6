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

package edu.ku.brc.af.plugins;

import java.util.List;

import edu.ku.brc.af.core.TaskCommandDef;
/**
 * An interface for describing how a plugin can be registered into the UI and begin to provide service.
 *
 * External plugins should make sure the call <i>initialize</i> after they install themselves, or they can call
 * PluginMgr.initilize();
 *
 * @author rods
 *
 */
public interface TaskPluginable
{
    /**
     * Initializes the task. The Taskable is responsible for making sure this method
     * can be called mulitple times with no ill effects.
     *
     * @param cmds the list of commands for the task
     */
    public void initialize(List<TaskCommandDef> cmds);


    /**
     * Returns the name of the task (NOT Localized)
     * @return Returns the name of the task (NOT Localized)
     */
    public String getName();


    /**
     * Returns the implementing Class type
     * @return Returns the implementing Class type
     */
    public abstract Class getTaskClass();

    /**
     * Returns the toolbar items (usually only one item)
     * @return Returns the toolbar items (usually only one item)
     */
    public List<ToolBarItemDesc> getToolBarItems();

    /**
     * Returns the menu item to be registered
     * @return Returns the menu item to be registered
     */
    public List<MenuItemDesc>    getMenuItems();

    /**
     * Install all preferences with default values
     */
    public void installPrefs();

    /**
     * Remove all preferences from the system
     */
    public void removePrefs();


}
