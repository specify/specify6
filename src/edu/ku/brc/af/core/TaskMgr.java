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

import static edu.ku.brc.helpers.XMLHelper.getAttr;
import static edu.ku.brc.helpers.XMLHelper.readDOMFromConfigDir;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static org.apache.commons.lang.StringUtils.split;

import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

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

import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ToolbarLayoutManager;
import edu.ku.brc.ui.UIPluginable;
import edu.ku.brc.ui.UIRegistry;

/**
 * Manages all the tasks as described in the plugin registry. 
 * The Tasks (Plugins) are read in and then created. Their toolbar items and menu items areinserted into the UI.<br>
 * Tasks can indicate via the plugin_registry.xml file whether there UI should be added or not. The UI consists of 
 * toolbar items or menu items. The TaskMgr tracks whether the task offers up toolbar items, because in some scenarios 
 * an application may be configured for a single "visible" task and there wants to hide the toolbar, but it also means that
 * there always needs to be a pne showing in order to get the left side nav Panel UI.
 * 
 * @code_status Beta
 * 
 * @author rods
 *
 */
public class TaskMgr implements CommandListener
{
    // Static Data Members
    private static final Logger  log               = Logger.getLogger(TaskMgr.class);
    private static final TaskMgr instance          = new TaskMgr();
    private static final String  APP_RESTART_ACT   = "AppRestart"; //$NON-NLS-1$

    // Data Members
    protected Vector<Taskable>            toolbarTasks   = new Vector<Taskable>();
    protected Element                     commandDOMRoot = null;
    protected Taskable                    defaultTask    = null;
    
    protected Hashtable<String, Taskable>   tasks          = new Hashtable<String, Taskable>();
    protected Hashtable<String, Class<?>>   uiPluginHash   = new Hashtable<String,  Class<?>>();
    //protected Hashtable<String, Taskable>   visTaskHash    = new Hashtable<String,  Taskable>();

    /**
     * Protected Default Constructor for Singleton
     *
     */
    protected TaskMgr()
    {
        CommandDispatcher.register(APP_RESTART_ACT, this);
    }

    /**
     * Returns a singleton of the plugin manager
     * @return a singleton of the plugin manager
     */
    public static TaskMgr getInstance()
    {
        return instance;
    }
    
    /**
     * Returns the default taskable.
     * @return the default taskable.
     */
    public static Taskable getDefaultTaskable()
    {
        return instance.defaultTask;
    }
    
    /**
     * Returns the number of tasks that provide UI.
     * @return Returns the number of tasks that provide UI.
     */
    public static int getToolbarTaskCount()
    {
        return instance.toolbarTasks.size();
    }
    
    /**
     * Requests an initial context. First it chooses the default, then the Startup, and then some arbitrary task.
     */
    public static void requestInitalContext()
    {
        if (instance.defaultTask != null)
        {
            instance.defaultTask.requestContext();
        } else
        {
            Taskable startUpTask = ContextMgr.getTaskByName(getResourceString("TaskMgr.STARTUP")); //$NON-NLS-1$
            if (startUpTask != null)
            {
                startUpTask.requestContext();
                
            } else
            {
                Taskable arbitraryTaskable = instance.tasks.values().iterator().next();
                if (arbitraryTaskable != null)
                {
                    arbitraryTaskable.requestContext();
                }
            }
            
        }
    }

    /**
     * Registers a plugin into the applications.
     * @param plugin the plugin to be registered
     * @param shouldAddUI true if plugin should add UI components, false if the UI should be "hidden"
     */
    public static void register(final Taskable plugin, final boolean shouldAddUI)
    {
        if (plugin != null)
        {
            if (instance.tasks.get(plugin.getName()) == null)
            {
                instance.tasks.put(plugin.getName(), plugin);

                if (shouldAddUI)
                {
                    registerWithUI(plugin);
                }

            } else
            {
                throw new RuntimeException("Registering a plugin with an existing name["+plugin.getName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } else
        {
            throw new NullPointerException("Trying to register a null plugin!"); //$NON-NLS-1$
        }
    }

    /**
     * Unregisters a plugin from the application.
     * @param taskable the plugin to be installed
     */
    public static void unregister(final Taskable taskable)
    {
        if (taskable != null)
        {
            Taskable tp = instance.tasks.get(taskable.getName());
            if (tp != null)
            {
                instance.tasks.remove(taskable.getName());
                if (instance.toolbarTasks.indexOf(tp) > -1)
                {
                    instance.toolbarTasks.remove(tp);
                }
                if (taskable == instance.defaultTask)
                {
                    instance.defaultTask = null;
                }
            } else
            {
                throw new RuntimeException("Unregistering a plugin that has been registered ["+taskable.getName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } else
        {
            throw new NullPointerException("Trying to unregister a null plugin!"); //$NON-NLS-1$
        }
    }
    
    /**
     * Returns a Task instance by name
     * @param name the name of the plugin task
     * @return the name of the plugin task
     */
    public static Taskable getTask(final String name)
    {
        return instance.tasks.get(name);
    }

    /**
     * Registers the plugin's UI compontents with the various parts of the UI. If the requested poxition
     * is 'Position.AppendNextToLast' then it is appended and the ToolBar is set to adjust the last item to
     * the right. Note: If two items request Position.AppendNextToLast then the last one to do so is 
     * is adjusted right since they are appended.
     * @param plugin the plugin that will register it's UI
     */
    protected static void registerWithUI(final Taskable plugin)
    {
        boolean isVisible = false;
        
        JToolBar toolBar = (JToolBar)UIRegistry.get(UIRegistry.TOOLBAR);
        if (toolBar != null)
        {
            List<ToolBarItemDesc> toolBarItems = plugin.getToolBarItems();
            if (toolBarItems != null && toolBarItems.size() > 0)
            {
                isVisible = true;
                for (ToolBarItemDesc tbItem : toolBarItems)
                {
                    Component toolBarComp = tbItem.getComp();
                    if (tbItem.getPos() == ToolBarItemDesc.Position.Insert)
                    {
                        toolBar.add(toolBarComp, tbItem.getIndex());

                    } else if (tbItem.getPos() == ToolBarItemDesc.Position.Append)
                    {
                        toolBar.add(toolBarComp);

                    } else if (tbItem.getPos() == ToolBarItemDesc.Position.AdjustRightLastComp)
                    {
                        toolBar.add(toolBarComp);
                        LayoutManager layout = toolBar.getLayout();
                        if (layout instanceof ToolbarLayoutManager)
                        {
                            ((ToolbarLayoutManager)layout).setAdjustRightLastComp(true);
                        }

                    } else if (tbItem.getPos() == ToolBarItemDesc.Position.AppendNextToLast)
                    {
                        int inx = toolBar.getComponentCount();
                        if (inx > 0)
                        {
                            inx -= 1;
                        }
                        toolBar.add(toolBarComp, inx);
                    }
                } // for
            }
        } else
        {
            throw new NullPointerException("The Toolbar component cannot be null!"); //$NON-NLS-1$
        }
        
        // Load all the menu Items
        JMenuBar menuBar = (JMenuBar)UIRegistry.get(UIRegistry.MENUBAR);
        if (menuBar != null)
        {
            List<MenuItemDesc> menuItems = plugin.getMenuItems();
            if (menuItems != null)
            {
                for (MenuItemDesc menuItem : plugin.getMenuItems())
                {
                    String[] menuPath = split(menuItem.getMenuPath(), "/"); //$NON-NLS-1$
                    buildMenuTree(menuBar, menuItem, menuPath, 0);
                }
            }
        } else
        {
            throw new NullPointerException("The MenuBar component cannot be null!"); //$NON-NLS-1$
        }
        
        if (isVisible)
        {
            instance.toolbarTasks.add(plugin);
        }
    }
    
    public static void addToolbarBtn(final Component toolBarComp, final int pos)
    {
        JToolBar toolBar = (JToolBar)UIRegistry.get(UIRegistry.TOOLBAR);
        if (toolBar != null)
        {
            toolBar.add(toolBarComp, pos);
        }
    }

    public static void removeToolbarBtn(final Component toolBarComp)
    {
        JToolBar toolBar = (JToolBar)UIRegistry.get(UIRegistry.TOOLBAR);
        if (toolBar != null)
        {
            toolBar.remove(toolBarComp);
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
            MenuElement me   = menuItemDesc.getMenuItem();
            if (parent instanceof JMenuBar)
            {
                ((JMenuBar)parent).add((JMenu)me);

            } else if (parent instanceof JMenu)
            {
                Container menuComp = ((JMenu)parent).getPopupMenu();
                boolean found     = false;
                int     insertPos = -1;
                
                JMenu menu = (JMenu)parent;
                if (menuItemDesc.getPosition() != MenuItemDesc.Position.None)
                {
                    int inx = 0;
                    for (int i=0;i<menuComp.getComponentCount();i++)
                    {
                        Component c = menuComp.getComponent(i);
                        if (c instanceof JMenuItem && ((JMenuItem)c).getText().equals(menuItemDesc.getPosMenuItemName()))
                        {
                            insertPos = inx;
                            found     = true;
                            break;
                        }
                        inx++;
                    }
                }
                
                if (menuItemDesc.getSepPosition() == MenuItemDesc.Position.Before)
                {
                    menu.add(new JPopupMenu.Separator(), insertPos);
                    insertPos++;
                }
                
                if (insertPos == -1)
                {
                    menu.add((JMenuItem)me);
                } else
                {
                    menu.add((JMenuItem)me, menuItemDesc.getPosition() == MenuItemDesc.Position.Before ? insertPos : insertPos + 1);    
                }
                
                insertPos++;
                
                if (menuItemDesc.getSepPosition() == MenuItemDesc.Position.After)
                {
                    if (found)
                    {
                        menu.add(new JPopupMenu.Separator(), insertPos);
                    }
                }
            }
            
        } else
        {
            String label = getResourceString(menuPath[currIndex]);

            MenuElement menuElement = getMenuByName(parent, label);
            if (menuElement == null)
            {
                throw new RuntimeException("Couldn't find menu element ["+label+"]"); //$NON-NLS-1$ //$NON-NLS-2$
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

//    /**
//     * Unregisters the plugin's UI components from the various different pasts of the application
//     * @param plugin the plugin that is being unregistered
//     */
//    protected static void unregisterWithUI(final Taskable plugin)
//    {
//    }

    /**
     * Forces an initialization of all the plugins. Can be called mulitple times because plugins are responsible
     * for making sure they only get initialized one time.
     */
    public static void initializePlugins()
    {
        // The initialize and pre-initialize do not assume any order this this is
        // why we do it in two steps.
        
        // Pre Initialze Step
        for (Enumeration<Taskable> e=instance.tasks.elements();e.hasMoreElements();)
        {
            e.nextElement().preInitialize();
        }
        
        // Now call initialize.
        for (Taskable taskablePlugin : instance.tasks.values())
        {
            taskablePlugin.initialize(getCommandDefinitions(taskablePlugin.getTaskClass()), true);
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
        // Only read in the XML if there are no tasks
        if (instance.tasks.size() == 0)
        {
            try
            {
                Element root  = readDOMFromConfigDir("plugin_registry.xml"); //$NON-NLS-1$
    
                List<?> boxes = root.selectNodes("/plugins/core/plugin"); //$NON-NLS-1$
                for ( Iterator<?> iter = boxes.iterator(); iter.hasNext(); )
                {
                    org.dom4j.Element pluginElement = (org.dom4j.Element)iter.next();
    
                    Object newObj = null;
                    String name   = pluginElement.attributeValue("class"); //$NON-NLS-1$
                    try
                    {
    
                        newObj = Class.forName(name).asSubclass(Taskable.class).newInstance();
    
                    } catch (Exception ex)
                    {
                        log.error(ex);
                        ex.printStackTrace();
                        
                        // go to the next plugin
                        continue;
                        // XXX Do we need a dialog here ???
                    }
    
                    if (newObj instanceof Taskable)
                    {
                        Taskable task = (Taskable)newObj;
                        
                        boolean shouldAddToUI =  getAttr(pluginElement, "addui", false);
                        if (AppContextMgr.isSecurityOn())
                        {
                            PermissionIFace perm = task.getPermissions();
                            if (perm != null)
                            {
                                if (!perm.canView())
                                {
                                    shouldAddToUI = false;
                                    task.setEnabled(false);
                                }
                            }
                        }
                        
                        boolean isTaskDefault = getAttr(pluginElement, "default", false); //$NON-NLS-1$
                        if (isTaskDefault)
                        {
                            if (instance.defaultTask == null)
                            {
                                instance.defaultTask = task;
                            } else
                            {
                                log.error("More than one plugin thinks it is the default["+task.getName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                        
                        register(task, shouldAddToUI); //$NON-NLS-1$

                    } else
                    {
                        log.error("Oops, the plugin is not instance of Taskable ["+newObj+"]"); //$NON-NLS-1$ //$NON-NLS-2$
                        // XXX Need to display an error
                    }
                }
                
                for ( Iterator<?> iter = root.selectNodes("/plugins/uiplugins/plugin").iterator(); iter.hasNext(); ) //$NON-NLS-1$
                {
                    Element pluginElement = (Element)iter.next();
    
                    String name      = XMLHelper.getAttr(pluginElement, "name", null); //$NON-NLS-1$
                    String className = XMLHelper.getAttr(pluginElement, "class", null); //$NON-NLS-1$
                    if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(className))
                    {
                        try
                        {
                            Class<?> cls = Class.forName(className).asSubclass(UIPluginable.class);
                            log.debug("Registering ["+name+"] Class["+cls.getName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            instance.uiPluginHash.put(name, cls);
        
                        } catch (Exception ex)
                        {
                            log.error(ex);
                            ex.printStackTrace();
                            
                            // go to the next plugin
                            continue;
                            // XXX Do we need a dialog here ???
                        }
                    }
                }

            } catch (Exception ex)
            {
                ex.printStackTrace();
                log.error(ex);
            }
        }
    }
    
    /**
     * Looks up a plugin's class by name.
     * @param devName the name of the plugin
     * @return the class of the plgin.
     */
    public static Class<?> getUIPluginClassForName(final String pluginName)
    {
        log.debug("Looing up["+pluginName+"]["+instance.uiPluginHash.get(pluginName)+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return instance.uiPluginHash.get(pluginName);
    }
    
    /**
     * @return a sorted list of the names of the Plugins
     */
    public List<String> getUIPluginList()
    {
        List<String> list = new Vector<String>(uiPluginHash.keySet().size());
        list.addAll(uiPluginHash.keySet());
        return list;
    }
    
    /**
     * @return all the tasks
     */
    public Collection<Taskable> getAllTasks()
    {
        return tasks.values();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CommandListener#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isAction(APP_RESTART_ACT))
        {
            for (Taskable task : instance.tasks.values())
            {
                if (AppContextMgr.isSecurityOn())
                {
                    task.setPermissions(null); // for relo0ad of permissions
                    PermissionIFace perm = task.getPermissions();
                    if (perm != null)
                    {
                        task.setEnabled(perm.canView());
                    }
                }
            }
        }
    }
    
    /**
     * Get the command definitions for a class
     * @param classObj a class object
     * @return the lst of commands for the class
     */
    public static List<TaskCommandDef> getCommandDefinitions(final Class<?> classObj)
    {
        List<TaskCommandDef> list = new ArrayList<TaskCommandDef>();
        try
        {
            if (instance.commandDOMRoot == null)
            {
                instance.commandDOMRoot = readDOMFromConfigDir("command_registry.xml"); //$NON-NLS-1$
            }

            List<?> cmds = instance.commandDOMRoot.selectNodes("/commands/command[@class='"+classObj.getName()+"']"); //$NON-NLS-1$ //$NON-NLS-2$

            for ( Iterator<?> iter = cmds.iterator(); iter.hasNext(); )
            {
                Element cmdElement = (Element)iter.next();

                String cmdName     = getAttr(cmdElement, "name", null); //$NON-NLS-1$
                String cmdIconName = getAttr(cmdElement, "icon", null); //$NON-NLS-1$
                if (StringUtils.isNotEmpty(cmdName) && StringUtils.isNotEmpty(cmdIconName))
                {
                    Properties params = null;
                    List<?> paramsList = cmdElement.selectNodes("param"); //$NON-NLS-1$
                    for ( Iterator<?> iterServices = paramsList.iterator(); iterServices.hasNext(); )
                    {
                        Element paramElement = (Element)iterServices.next();
                        String name  = getAttr(paramElement, UIRegistry.getResourceString("TaskMgr.0"), null); //$NON-NLS-1$
                        String value = paramElement.getTextTrim();
                        if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(value))
                        {
                            if (params == null)
                            {
                                params = new Properties();
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

}
