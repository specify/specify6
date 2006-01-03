/* Filename:    $RCSfile: PluginMgr.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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

import java.awt.Component;
import java.util.Hashtable;
import java.util.Enumeration;

import javax.swing.JMenuBar;
import javax.swing.JToolBar;

import edu.ku.brc.specify.ui.UICacheManager;

public class PluginMgr
{

    // Static Data Members
    private static PluginMgr instance = new PluginMgr();
    
    // Data Members
    protected Hashtable<String, TaskPluginable> plugins = new Hashtable<String, TaskPluginable>();
    
    /**
     * Protected Default Constructor for Singleton
     *
     */
    protected PluginMgr()
    {
        
    }
    
    /**
     * Returns a singleton of the plugin manager
     * @return a singleton of the plugin manager
     */
    public static PluginMgr getInstance()
    {
        return instance;
    }
    
    /**
     * Registers a plugin into the applications
     * @param plugin the plugin to be registered
     */
    public static void register(final TaskPluginable plugin)
    {
        if (plugin != null)
        {
            if (instance.plugins.get(plugin.getName()) == null)
            {
                instance.plugins.put(plugin.getName(), plugin);
                
                registerWithUI(plugin);
                
            } else
            {
                throw new RuntimeException("Registering a plugin with an existing name["+plugin.getName()+"]");
            }
        } else
        {
            throw new NullPointerException("Trying to register a null plugin!");
        }
    }
    
    /**
     * Unregisters a plugin from the application
     * @param plugin
     */
    public static void unregister(final TaskPluginable plugin)
    {
        if (plugin != null)
        {
            if (instance.plugins.get(plugin.getName()) != null)
            {
                instance.plugins.remove(plugin.getName());
            } else
            {
                throw new RuntimeException("Unregistering a plugin that has been registered ["+plugin.getName()+"]");
            }
        } else
        {
            throw new NullPointerException("Trying to unregister a null plugin!");
        }
    }

    /**
     * Registers the plugin's UI compontents with the various parts of the UI
     * @param plugin the plugin that will register it's UI
     */
    protected static void registerWithUI(final TaskPluginable plugin)
    {
        JToolBar toolBar = (JToolBar)UICacheManager.getInstance().get(UICacheManager.TOOLBAR);
        if (toolBar != null)
        {
            for (ToolBarItemDesc tbItem : plugin.getToolBarItems())
            {
                Component toolBarComp = tbItem.getComp();
                if (tbItem.getPos() == ToolBarItemDesc.Position.Insert)
                {
                    toolBar.add(toolBarComp, tbItem.getIndex());
                    
                } else if (tbItem.getPos() == ToolBarItemDesc.Position.Append)
                {
                    toolBar.add(toolBarComp);
                    
                } else if (tbItem.getPos() == ToolBarItemDesc.Position.AppendNextToLast)
                {
                    int inx = toolBar.getComponentCount();
                    if (inx > 0)
                    {
                        inx -= 1;
                    }
                    toolBar.add(toolBarComp, inx);
                }
            }
        } else
        {
            throw new NullPointerException("The Toolbar component cannot be null!");
        }
        
        JMenuBar menuBar = (JMenuBar)UICacheManager.getInstance().get(UICacheManager.MENUBAR);
        if (menuBar != null)
        {
            for (MenuItemDesc menuItem : plugin.getMenuItems())
            {
                Component toolBarComp = menuItem.getMenuItem();
                if (toolBarComp != null)
                {
                    toolBar.add(toolBarComp);
                }
            }
        } else
        {
            throw new NullPointerException("The MenuBar component cannot be null!");
        }
    }

    /**
     * Unregisters the plugin's UI components from the various different pasts of the application
     * @param plugin the plugin that is being unregistered
     */
    protected static void unregisterWithUI(final TaskPluginable plugin)
    {
        
    }
    
    /**
     * Forces an initialization of all the plugins. Can be called mulitple times because plugins are responsible
     * for making sure they only get initialized one time.
     */
    public static void initializePlugins()
    {
        for (Enumeration<TaskPluginable> e=instance.plugins.elements();e.hasMoreElements();)
        {
            TaskPluginable taskablePlugin = e.nextElement();
            taskablePlugin.initialize();
        }
    }

}
