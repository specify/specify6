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
package edu.ku.brc.af.core;

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

import edu.ku.brc.af.auth.SecurityOptionIFace;

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
public interface Taskable extends SecurityOptionIFace
{
    public final static int StdIcon32 = 32;
    public final static int StdIcon24 = 24;
    public final static int StdIcon20 = 20;
    public final static int StdIcon16 = 16;
    
    /**
     * @return whether this Taskable should only ever have a single pane.
     */
    public abstract boolean isSingletonPane();
    
    
    /**
     * Returns all a collection of Nav Boxes for the NavBox Pane.
     * @return a collection of Nav Boxes
     */
    public abstract List<NavBoxIFace> getNavBoxes();

    /**
     * Return the icon that represents the task.
     * @param size use standard size (i.e. 32, 24, 20, 26)
     * @return the icon that represents the task
     */
    public abstract ImageIcon getIcon(int size);

    /**
     * Returns the name of the task (NOT Localized).
     * @return the name of the task (NOT Localized)
     */
    public abstract String getName();

    /**
     * Returns the title of the task (Localized).
     * @return the title of the task (Localized)
     */
    public abstract String getTitle();

    /**
     * Initializes the task. The Taskable is responsible for making sure this method
     * can be called mulitple times with no ill effects.
     *
     */
    //public abstract void initialize();

    /**
     * Requests the context for this task.
     *
     */
    public abstract void requestContext();
    
    /**
     * Returns the toolbar items (usually only one item).
     * @return the toolbar items (usually only one item)
     */
    public abstract List<ToolBarItemDesc> getToolBarItems();

    /**
     * Returns the menu item to be registered.
     * @return the menu item to be registered
     */
    public abstract List<MenuItemDesc> getMenuItems();
    
    /**
     * Pre-Initializes the task. This is called after all the tasks are created and registered, 
     * but before Initialize is called.
     */
    public abstract void preInitialize();

    /**
     * Initializes the task. The Taskable is responsible for making sure this method
     * can be called mulitple times with no ill effects.
     *
     * @param cmds the list of commands for the task
     */
    public abstract void initialize(List<TaskCommandDef> cmds, boolean isVisible);

    /**
     * Returns the implementing Class type.
     * @return the implementing Class type
     */
    public abstract Class<?> getTaskClass();
    
    /**
     * Returns the initial pane for this task, may be a blank (empty) pane, but shouldn't null.
     * @return Returns the initial pane for this task, may be a blank (empty) pane, but shouldn't null
     */
    public abstract SubPaneIFace getStarterPane();
    
    /**
     * @param pane
     */
    public abstract void setStarterPane(SubPaneIFace pane);
    
    /**
     * @return returns whether the current pane for the task is the starter pane.
     */
    public abstract boolean isStarterPane();
    
    /**
     * @return returns whether it should show the default starter pane when the last SubPane is closed.
     */
    public abstract boolean isShowDefault();
    
    /**
     * @return whether the task supports being configured. Meaning the getPopupMenu menu
     * will return a menu. (If false, getPopupMenu will return null).
     */
    public abstract boolean isConfigurable();
    
    /**
     * @return a popup menu for the left side pane or null, that is used to configure the task.
     */
    public abstract JPopupMenu getPopupMenu();
    
    /**
     * Displays any UI necessary to configure the task.
     */
    public abstract void doConfigure();
    
    /**
     * @return whether it should have it's UI put into the menus or Toolbar
     */
    public abstract boolean isVisible();
    
    /**
     * Sets the icon name for the task
     * @param iconName the the name
     */
    public abstract void setIconName(String iconName); 

    /**
     * Sets whether the Task is available. Disabling a task also disables
     * the UI associated with the task.
     * @param enabled true/false
     */
    public abstract void setEnabled(boolean enabled);
    
    /**
     * @return whether the task is enabled our not.
     */
    public abstract boolean isEnabled();
    
    /**
     * @return whether the task should be configurable for Permissions
     */
    public abstract boolean isPermissionsSettable();
    
    /**
     * @return the help context
     */
    public abstract String getHelpTarget();
    
    /**
     * Tells the task that the app is shutting down,
     * for good or for a restart.
     */
    public abstract void shutdown();

    public abstract void closing(SubPaneIFace workbenchPaneSS);
    
}
