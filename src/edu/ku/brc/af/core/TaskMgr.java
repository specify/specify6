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

import static edu.ku.brc.helpers.XMLHelper.getAttr;
import static edu.ku.brc.helpers.XMLHelper.readDOMFromConfigDir;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static org.apache.commons.lang.StringUtils.split;

import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
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

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.UIPluginable;
import edu.ku.brc.af.ui.forms.persist.FormDevHelper;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ToolbarLayoutManager;
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
    private static final String  APP_SHUTDOWN_ACT  = "Shutdown"; //$NON-NLS-1$


    // Data Members
    protected Vector<Taskable>               toolbarTasks   = new Vector<Taskable>();
    protected Hashtable<String, MenuElement> menuHash       = new Hashtable<String, MenuElement>();
    protected Element                        commandDOMRoot = null;
    protected Taskable                       defaultTask    = null;
    protected Taskable                       currentTask    = null;
    
    protected Hashtable<String, Taskable>    tasks          = new Hashtable<String, Taskable>();
    protected Hashtable<String, Class<?>>    uiPluginHash   = new Hashtable<String,  Class<?>>();
    
    protected Vector<Taskable>               disabledTasks  = new Vector<Taskable>();

    /**
     * Protected Default Constructor for Singleton
     *
     */
    protected TaskMgr()
    {
        CommandDispatcher.register("App", this);
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
     * @return the currentTask
     */
    public Taskable getCurrentTask()
    {
        return currentTask;
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
            instance.currentTask = instance.defaultTask;
            
        } else
        {
            Taskable startUpTask = ContextMgr.getTaskByName(getResourceString("TaskMgr.STARTUP")); //$NON-NLS-1$
            if (startUpTask != null)
            {
                startUpTask.requestContext();
                instance.currentTask = startUpTask;
                
            } else if (instance.tasks.values().size() > 0)
            {
                Taskable arbitraryTaskable = instance.tasks.values().iterator().next();
                if (arbitraryTaskable != null)
                {
                    arbitraryTaskable.requestContext();
                    instance.currentTask = startUpTask;
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
                //throw new RuntimeException("Registering a plugin with an existing name["+plugin.getName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
                FormDevHelper.appendFormDevError("Registering a plugin with an existing name["+plugin.getName()+"]");//$NON-NLS-1$ //$NON-NLS-2$
            }
        } else
        {
            //throw new NullPointerException("Trying to register a null plugin!"); //$NON-NLS-1$
            FormDevHelper.appendFormDevError("Trying to register a null plugin!"); //$NON-NLS-1$
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
     * Registers the plugin's UI components with the various parts of the UI. If the requested position
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
                for (MenuItemDesc menuItem : menuItems)
                {
                    instance.menuHash.put(menuItem.getMenuPath(), menuItem.getMenuItem());
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
    
    /**
     * @param path
     * @return
     */
    public static MenuElement getMenuElementByPath(final String path)
    {
        return getInstance().menuHash.get(path);
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
                int     insertPos = menuComp.getComponentCount(); // last position
                JMenu   menu      = (JMenu)parent;
                
                if (menuItemDesc.getPosition() == MenuItemDesc.Position.Top)
                {
                    insertPos = 0;
                    //log.debug(String.format("0 Inserted: %s - %d", ((JMenuItem)me).getText(), insertPos));
                    
                } else if (menuItemDesc.getPosition() == MenuItemDesc.Position.Bottom)
                {
                    //log.debug(String.format("1 Inserted: %s - %d", ((JMenuItem)me).getText(),insertPos));
                }
                
                
                if (menuItemDesc.getPosition() == MenuItemDesc.Position.Before || 
                    menuItemDesc.getPosition() == MenuItemDesc.Position.After)
                {
                    int inx = 0;
                    for (int i=0;i<menuComp.getComponentCount();i++)
                    {
                        Component c = menuComp.getComponent(i);
                        if (c instanceof JMenuItem && ((JMenuItem)c).getText().equals(menuItemDesc.getPosMenuItemName()))
                        {
                            insertPos = inx+1;
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
                    menu.add((JMenuItem)me, insertPos);
                    //log.debug(String.format("2 Inserted: %s - %d", ((JMenuItem)me).getText(),insertPos));
                    found = true;
                }
                
                insertPos++;
                
                if (menuItemDesc.getSepPosition() == MenuItemDesc.Position.After)
                {
                    if (found)
                    {
                        menu.add(new JPopupMenu.Separator(), insertPos);
                        //log.debug(String.format("3 Inserted: Sep - %d", insertPos));

                    }
                }
            } else if (parent instanceof JMenuItem)
            {
                JMenuItem   mi   = (JMenuItem)parent;
                JPopupMenu  menu = (JPopupMenu)mi.getParent();
                
                int pos = 0;
                for (int i=0;i<menu.getComponentCount();i++)
                {
                    if (mi == menu.getComponent(i))
                    {
                        pos = i;
                        break;
                    }
                }
                //log.debug(String.format("4 Inserted: %s - %d", ((JMenuItem)me).getText(), menuItemDesc.getPosition() == MenuItemDesc.Position.After ? pos + 1 : pos));
                menu.insert((JMenuItem)me, menuItemDesc.getPosition() == MenuItemDesc.Position.After ? pos + 1 : pos);
            }
            
        } else
        {
            String label = getResourceString(menuPath[currIndex]);

            MenuElement menuElement = getMenuByName(parent, label);
            
            /*log.debug(menuPath[currIndex]+" -> "+label+ " "+menuElement);
            if (parent instanceof JMenuItem) log.debug(((JMenuItem)parent).getText());
            else if (parent instanceof JMenu) log.debug(((JMenu)parent).getText());
            else if (parent instanceof JMenuBar) log.debug("MenuBar");
            */
            
            if (menuElement == null)
            {
                log.error("Couldn't find menu element ["+label+"]");//$NON-NLS-1$ //$NON-NLS-2$
                //UIRegistry.showError("Couldn't find menu element ["+label+"]"); //$NON-NLS-1$ //$NON-NLS-2$
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
     * @param isMobile whether it is mobile
     */
    public void readRegistry(final boolean isMobile)
    {
        if (instance.tasks.size() == 0)
        {
            String fileName = "plugin_registry.xml";
            
            HashMap<String, PluginInfo> plugins = new HashMap<String, PluginInfo>();
            HashMap<String, PluginInfo> uiPlugins = new HashMap<String, PluginInfo>();
            
            String path = XMLHelper.getConfigDirPath(fileName);
            readRegistry(path, plugins, uiPlugins, isMobile);
            
            path = AppPreferences.getLocalPrefs().getDirPath() + File.separator + fileName;
            readRegistry(path, plugins, uiPlugins, isMobile);

            readRegistry(plugins, uiPlugins);
        }
    }

    /**
     * @param path
     * @param plugins
     * @param isMobile
     */
    private void readRegistry(final String path, 
                              final HashMap<String, PluginInfo> plugins,
                              final HashMap<String, PluginInfo> uiPlugins,
                              final boolean isMobile)
    {
        try
        {
            File file = new File(path);
            if (file.exists())
            {
                Element root  = XMLHelper.readFileToDOM4J(file);
    
                int order = 0;
                List<?> boxes = root.selectNodes("/plugins/"+(isMobile ? "mobilecore" : "core") + "/plugin"); //$NON-NLS-1$
                for ( Iterator<?> iter = boxes.iterator(); iter.hasNext(); )
                {
                    org.dom4j.Element pluginElement = (org.dom4j.Element)iter.next();
    
                    String  pluginName = pluginElement.attributeValue("name"); //$NON-NLS-1$
                    String  className  = pluginElement.attributeValue("class"); //$NON-NLS-1$
                    Boolean addToUI    = XMLHelper.getAttr(pluginElement, "addui", false); //$NON-NLS-1$
                    Boolean isDefault  = XMLHelper.getAttr(pluginElement, "default", false); //$NON-NLS-1$
                    String  prefName   = pluginElement.attributeValue("prefname"); //$NON-NLS-1$
                    
                    if (StringUtils.isNotEmpty(pluginName) && StringUtils.isNotEmpty(className))
                    {
                        PluginInfo newPI = new PluginInfo(order, pluginName, className, addToUI, isDefault, prefName);
                        PluginInfo pi    = plugins.get(pluginName);
                        if (pi != null)
                        {
                            newPI.setOrder(newPI.getOrder());
                        }
                        plugins.put(pluginName, newPI);
                        order++;
                    }
                }
                
                for ( Iterator<?> iter = root.selectNodes("/plugins/uiplugins/plugin").iterator(); iter.hasNext(); ) //$NON-NLS-1$
                {
                    Element pluginElement = (Element)iter.next();

                    String pluginName = XMLHelper.getAttr(pluginElement, "name", null); //$NON-NLS-1$
                    String className  = XMLHelper.getAttr(pluginElement, "class", null); //$NON-NLS-1$
                    if (StringUtils.isNotEmpty(pluginName) && StringUtils.isNotEmpty(className))
                    {
                        uiPlugins.put(pluginName, new PluginInfo(pluginName, className));
                    }
                }
            }
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TaskMgr.class, ex);
            ex.printStackTrace();
            log.error(ex);
        }
    }

    /**
     * @param path
     * @param plugins
     * @param isMobile
     */
    private void readRegistry(final HashMap<String, PluginInfo> plugins,
                              final HashMap<String, PluginInfo> uiPlugins)
    {
        try
        {
            Vector<PluginInfo> list = new Vector<TaskMgr.PluginInfo>(plugins.values());
            Collections.sort(list);
            
            for (PluginInfo pi : list)
            {
                String prefName = pi.getPrefName();
                if (prefName != null)
                {
                    if (!AppPreferences.getLocalPrefs().getBoolean(prefName, false))
                    {
                        continue;
                    }
                }
                
                Object newObj = null;
                try
                {
                    newObj = Class.forName(pi.getClassName()).asSubclass(Taskable.class).newInstance();

                } catch (Exception ex)
                {
                    if (StringUtils.isEmpty(prefName))
                    {
                        log.error(ex);
                        UIRegistry.showError(String.format("The plugin '%s' could not be loaded.\nPlease contact cutomer suppoer.", pi.getPluginName()));
                        //ex.printStackTrace();
                        //edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TaskMgr.class, ex);
                    }
                    
                    // go to the next plugin
                    continue;
                    // XXX Do we need a dialog here ???
                }

                if (newObj instanceof Taskable)
                {
                    Taskable task = (Taskable)newObj;
                    
                    boolean shouldAddToUI = pi.getIsAddToUI();
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
                    
                    boolean isTaskDefault = pi.getIsDefault();
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
            
            for ( PluginInfo uiPI : uiPlugins.values())
            {
                try
                {
                    Class<?> cls = Class.forName(uiPI.getClassName()).asSubclass(UIPluginable.class);
                    //log.debug("Registering ["+name+"] Class["+cls.getName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    uiPluginHash.put(uiPI.getPluginName(), cls);

                } catch (Exception ex)
                {
                    log.error(ex);
                    ex.printStackTrace();
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TaskMgr.class, ex);
                }
            }

        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TaskMgr.class, ex);
            ex.printStackTrace();
            log.error(ex);
        }
    }
    
    /**
     * Looks up a plugin's class by name.
     * @param devName the name of the plugin
     * @return the class of the plgin.
     */
    public static Class<?> getUIPluginClassForName(final String pluginName)
    {
        //log.debug("Looking up["+pluginName+"]["+instance.uiPluginHash.get(pluginName)+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
    
    /**
     * Tells all tasks they are shutting down.
     */
    private void shutdownTasks()
    {
        for (Taskable task : instance.tasks.values())
        {
            task.shutdown();
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CommandListener#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isAction(APP_RESTART_ACT))
        {
            shutdownTasks();
            
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
        } else if (cmdAction.isAction(APP_SHUTDOWN_ACT))
        {
            shutdownTasks();
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TaskMgr.class, ex);
            ex.printStackTrace();
            log.error(ex);
        }
        return list;
    }
    
    /**
     * Checks each task to see if it is currently enabled and then disables it.
     * When 'reenableAllDisabledTasks' is called it sets each task  to be abled that 
     * had been set disabled by this call.
     *  
     */
    public static void disableAllEnabledTasks()
    {
        TaskMgr tm = getInstance();
        if (tm.disabledTasks.size() == 0)
        {
            for (Taskable task : tm.getAllTasks())
            {
                if (task.isEnabled())
                {
                    tm.disabledTasks.add(task);
                    task.setEnabled(false);
                }
            }
        }
    }
    
    /**
     * @return true if there are one or more taks disabled.
     */
    public static boolean areTasksDisabled()
    {
        return getInstance().disabledTasks.size() > 0;
    }

    /**
     * Re-Enables all the tasks that had been disabled by a call to 'disableAllEnabledTasks'
     * This does not enable any tasks that had not be disabled by a call to 'disableAllEnabledTasks'.
     */
    public static void reenableAllDisabledTasks()
    {
        for (Taskable task : getInstance().disabledTasks)
        {
            task.setEnabled(true);
        }
        getInstance().disabledTasks.clear();
    }
    
    //-----------------------------------------------------------------------
    //
    //-----------------------------------------------------------------------
    class PluginInfo implements Comparable<PluginInfo>
    {
        private Integer order;
        private String  pluginName;
        private String  className;
        private Boolean isAddToUI;
        private Boolean isDefault;
        private String  prefName;
        
        /**
         * @param pluginName
         * @param className
         */
        public PluginInfo(String pluginName, String className)
        {
            this(null, pluginName, className, null, null, null);
        }
        
        /**
         * @param pluginName
         * @param className
         * @param isAddToUI
         * @param isDefault
         */
        public PluginInfo(Integer order,
                          String pluginName, 
                          String className, 
                          Boolean isAddToUI, 
                          Boolean isDefault,
                          String  prefName)
        {
            super();
            this.order = order;
            this.pluginName = pluginName;
            this.className = className;
            this.isAddToUI = isAddToUI;
            this.isDefault = isDefault;
            this.prefName  = prefName;
        }
        /**
         * @return the pluginName
         */
        public String getPluginName()
        {
            return pluginName;
        }
        /**
         * @return the className
         */
        public String getClassName()
        {
            return className;
        }
        /**
         * @return the isAddToUI
         */
        public Boolean getIsAddToUI()
        {
            return isAddToUI;
        }
        /**
         * @return the isDefault
         */
        public Boolean getIsDefault()
        {
            return isDefault;
        }
        /**
         * @return the order
         */
        public Integer getOrder()
        {
            return order;
        }
        /**
         * @param order the order to set
         */
        public void setOrder(Integer order)
        {
            this.order = order;
        }
        
        /**
         * @return the prefName
         */
        public String getPrefName()
        {
            return prefName;
        }
        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(PluginInfo o)
        {
            return order.compareTo(o.order);
        }
        
    }
}
