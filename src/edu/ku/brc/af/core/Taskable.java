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

import javax.swing.ImageIcon;

/**
 * Interface for any task in the system, most of the methods are "getters" that
 * enable the Task to play nice within the system.
 * 
 * The initialize and pre-initialize do not assume any order this this is
 * why we do it in two steps.
 * 
 * @code_status Complete
 *
 * @author rods
 *
 */
public interface Taskable
{
    /**
     * Returns all a collection of Nav Boxes for the NavBox Pane.
     * @return a collection of Nav Boxes
     */
    public List<NavBoxIFace> getNavBoxes();

    /**
     * Return the icon that represents the task.
     * @return the icon that represents the task
     */
    public ImageIcon getImageIcon();

    /**
     * Returns the name of the task (NOT Localized).
     * @return the name of the task (NOT Localized)
     */
    public String getName();

    /**
     * Returns the title of the task (Localized).
     * @return the title of the task (Localized)
     */
    public String getTitle();

    /**
     * Initializes the task. The Taskable is responsible for making sure this method
     * can be called mulitple times with no ill effects.
     *
     */
    //public void initialize();

    /**
     * Requests the context for this task.
     *
     */
    public void requestContext();
    
    /**
     * Returns the toolbar items (usually only one item).
     * @return the toolbar items (usually only one item)
     */
    public List<ToolBarItemDesc> getToolBarItems();

    /**
     * Returns the menu item to be registered.
     * @return the menu item to be registered
     */
    public List<MenuItemDesc> getMenuItems();
    
    /**
     * Pre-Initializes the task. This is called after all the tasks are created and registered, 
     * but before Initialize is called.
     */
    public void preInitialize();

    /**
     * Initializes the task. The Taskable is responsible for making sure this method
     * can be called mulitple times with no ill effects.
     *
     * @param cmds the list of commands for the task
     */
    public void initialize(List<TaskCommandDef> cmds, boolean isVisible);

    /**
     * Returns the implementing Class type.
     * @return the implementing Class type
     */
    public abstract Class<?> getTaskClass();
    
    /**
     * Returns a the start pane for the task.
     * @return a the start pane for the task.
     */
    public abstract SubPaneIFace getStarterPane();
    
    /**
     * @return returns whether the current pane for the task is the starter pane.
     */
    public abstract boolean isStarterPane();
    
    /**
     * @return returns whether it should show the default starter pane when the last SubPane is closed.
     */
    public abstract boolean isShowDefault();

}
