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

package edu.ku.brc.specify.plugins;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;
import static org.apache.commons.lang.StringUtils.split;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.MenuElement;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.specify.core.ContextMgr;
import edu.ku.brc.specify.core.ServiceInfo;
import edu.ku.brc.specify.core.TaskCommandDef;
import edu.ku.brc.specify.helpers.UIHelper;
import edu.ku.brc.specify.helpers.XMLHelper;
import edu.ku.brc.specify.ui.CommandAction;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.UICacheManager;

public class PluginMgr
{

    // Static Data Members
    private static final Logger    log      = Logger.getLogger(PluginMgr.class);
    private static final PluginMgr instance = new PluginMgr();

    // Data Members
    protected Hashtable<String, TaskPluginable> plugins        = new Hashtable<String, TaskPluginable>();
    protected Element                           commandDOMRoot = null;

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
        JToolBar toolBar = (JToolBar)UICacheManager.get(UICacheManager.TOOLBAR);
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

        JMenuBar menuBar = (JMenuBar)UICacheManager.get(UICacheManager.MENUBAR);
        if (menuBar != null)
        {
            for (MenuItemDesc menuItem : plugin.getMenuItems())
            {
                String[] menuPath = split(menuItem.getMenuPath(), "/");
                buildMenuTree(menuBar, menuItem, menuPath, 0);
            }
        } else
        {
            throw new NullPointerException("The MenuBar component cannot be null!");
        }
    }

    /**
     * @param parent
     * @param menuItemDesc
     * @param menuPath
     * @param currIndex
     */
    public static void buildMenuTree(final MenuElement  parent,
                                     final MenuItemDesc menuItemDesc,
                                     final String[]     menuPath,
                                     final int          currIndex)
    {


        if (currIndex == menuPath.length)
        {
            MenuElement me = menuItemDesc.getMenuItem();
            if (parent instanceof JMenuBar)
            {
                ((JMenuBar)parent).add((JMenu)me);

            } else if (parent instanceof JMenu)
            {
                ((JMenu)parent).add((JMenuItem)me);
            }
        } else
        {
            String label = getResourceString(menuPath[currIndex]);

            MenuElement menuElement = getMenuByName(parent, label);
            if (menuElement == null)
            {
                throw new RuntimeException("Couldn't find menu element ["+label+"]");
            }
            buildMenuTree(menuElement, menuItemDesc, menuPath, currIndex+1);
        }
    }

    /**
     * Create a menu by name
     * @param parent the parent menu item
     * @param name the new name
     * @return the menu element
     */
    public static MenuElement getMenuByName(final MenuElement parent, final String name)
    {
        for (MenuElement mi : parent.getSubElements())
        {
            if (mi instanceof AbstractButton)
            {
                //System.out.println("["+((AbstractButton)mi).getText()+"]["+name+"]");
                if (((AbstractButton)mi).getText().equals(name))
                {
                    return mi;
                }
            } else if (mi instanceof JPopupMenu)
            {
                return getMenuByName(mi, name);

                /*System.out.println("["+((JPopupMenu)mi).getLabel()+"]["+name+"]");
                if (((JPopupMenu)mi).getLabel().equals(name))
                {
                    return mi;
                }*/
            }
        }
        return null;
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
            taskablePlugin.initialize(getCommandDefinitions(taskablePlugin.getTaskClass()));
        }
    }

    /**
     * Helper method that loads the standard icon sizes needed for services.
     * @param name the name of the icon
     * @param info the service info object to be loaded with the icons
     */
    protected static void loadServiceIcons(final String name, final ServiceInfo info)
    {
        info.addIcon(IconManager.getIcon(name, IconManager.IconSize.Std16), IconManager.IconSize.Std16);
        info.addIcon(IconManager.getIcon(name, IconManager.IconSize.Std24), IconManager.IconSize.Std24);
        info.addIcon(IconManager.getIcon(name, IconManager.IconSize.Std32), IconManager.IconSize.Std32);
    }

    /**
     * Reads the plugins registry and loads them
     *
     */
    public static void readRegistry()
    {

        try
        {
            Element root  = XMLHelper.readDOMFromConfigDir("plugin_registry.xml");

            List boxes = root.selectNodes("/plugins/core/plugin");
            for ( Iterator iter = boxes.iterator(); iter.hasNext(); )
            {
                org.dom4j.Element pluginElement = (org.dom4j.Element)iter.next();

                Object newObj = null;
                String name   = pluginElement.attributeValue("class");
                try
                {

                    Class cls = Class.forName(name);
                    newObj = cls.newInstance();

                } catch (ClassNotFoundException ex)
                {
                    log.error(ex);
                    // XXX Do we need a dialog here ???
                }

                if (newObj instanceof TaskPluginable)
                {
                    TaskPluginable tp = (TaskPluginable)newObj;
                    register(tp);

                    List servicesList = pluginElement.selectNodes("service");
                    for ( Iterator iterServices = servicesList.iterator(); iterServices.hasNext(); )
                    {
                        Element       serviceElement = (Element)iterServices.next();
                        int           tableId        = Integer.parseInt(serviceElement.attributeValue("tableid"));
                        CommandAction cmd            = new CommandAction(tp.getName(), serviceElement.attributeValue("command"), tableId);
                        ServiceInfo   serviceInfo    = ContextMgr.registerService(tp.getName(), tableId, cmd, null, serviceElement.attributeValue("tooltip"));
                        loadServiceIcons(tp.getName(), serviceInfo);
                    }
                } else
                {
                    log.error("Oops, the plugin is not instance of TaskPluginable ["+newObj+"]");
                    // XXX Need to display an error
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.error(ex);
        }
    }

    /**
     * Get the command definitions for a class
     * @param classObj a class object
     * @return the lst of commands for the class
     */
    public static List<TaskCommandDef> getCommandDefinitions(final Class classObj)
    {
        List<TaskCommandDef> list = new ArrayList<TaskCommandDef>();
        try
        {
            if (instance.commandDOMRoot == null)
            {
                instance.commandDOMRoot = XMLHelper.readDOMFromConfigDir("command_registry.xml");
            }

            List cmds = instance.commandDOMRoot.selectNodes("/commands/command[@class='"+classObj.getName()+"']");

            for ( Iterator iter = cmds.iterator(); iter.hasNext(); )
            {
                Element cmdElement = (Element)iter.next();

                String cmdName     = XMLHelper.getAttr(cmdElement, "name", null);
                String cmdIconName = XMLHelper.getAttr(cmdElement, "icon", null);
                if (StringUtils.isNotEmpty(cmdName) && StringUtils.isNotEmpty(cmdIconName))
                {
                    Map<String, String> params = null;
                    List paramsList = cmdElement.selectNodes("param");
                    for ( Iterator iterServices = paramsList.iterator(); iterServices.hasNext(); )
                    {
                        Element paramElement = (Element)iterServices.next();
                        String name  = XMLHelper.getAttr(paramElement, "name", null);
                        String value = paramElement.getTextTrim();
                        if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(value))
                        {
                            if (params == null)
                            {
                                params = UIHelper.createMap();
                            }
                            params.put(name, value);
                        }
                    }
                    TaskCommandDef tcd = new TaskCommandDef(cmdName, cmdIconName, params);
                    list.add(tcd);

                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.error(ex);
        }
        return list;
    }


    //---------------------------------------------------------------
    //-- Inner Classes
    //---------------------------------------------------------------


}
