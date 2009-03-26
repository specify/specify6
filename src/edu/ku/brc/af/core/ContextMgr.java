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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;

/**
 * Manages the task context of the UI. The task context is controlled by what tab is visible in the main pane
 * When tasks are registered they are asked for the NavBoxes and those are placed in the NavBox Manager. when
 * they are unregistered the NavBoxes are removed
 *
 * Status: Finished
 
 * @code_status Complete
 **
 * @author rods
 *
 */
public class ContextMgr implements CommandListener
{
    // Static Data Members
    private static final Logger      log      = Logger.getLogger(ContextMgr.class);
    private static final ContextMgr  instance = new ContextMgr();
    private static final String  APP_RESTART_ACT = "AppRestart"; //$NON-NLS-1$

    // Data Members
    protected Taskable         currentContext         = null;
    protected Vector<Taskable> tasks                  = new Vector<Taskable>();

    protected Hashtable<String, ServiceInfo>        services        = new Hashtable<String, ServiceInfo>();
    protected Hashtable<Integer, List<ServiceInfo>> servicesByTable = new Hashtable<Integer, List<ServiceInfo>>();

    protected Vector<ServiceInfo>                   genericService  = new Vector<ServiceInfo>();
    
    /**
     * Protected Constructor of Singleton
     */
    protected ContextMgr()
    {
        CommandDispatcher.register("App", this);
    }

    /**
     * Returns the current Context (Taskable)
     * @return the current Context (Taskable)
     */
    public static Taskable getCurrentContext()
    {
        return instance.currentContext;
    }

    /**
     * Request for a change in context.
     * @param task the task requesting the context
     */
    public static void requestContext(final Taskable task)
    {
        if (task != instance.currentContext)
        {
            if (instance.currentContext != null)
            {
                NavBoxMgr.unregister();
            }

            if (task != null)
            {
                NavBoxMgr.register(task);
             }
            instance.currentContext = task;
        }
    }

    /**
     * Registers a task.
     * @param task the task to be register
     */
    public static void register(final Taskable task)
    {
        instance.tasks.addElement(task);
    }

    /**
     *  Unregisters a task. Checks to see if it is the current task, if so then it unregisters the NavBoxes
     * @param task the task to be unregistered
     */
    public static void unregister(final Taskable task)
    {
        /*if (currentContext == task)
        {
            NavBoxMgr.getInstance().unregister(currentContext);
            currentContext = null;
        }*/
        instance.tasks.removeElement(task);
    }

    /**
     * Returns a task by a given name.
     * @param name name of task to be returned
     * @return Returns a task by a given name
     */
    public static Taskable getTaskByName(final String name)
    {
        if (name == null)
        {
            throw new NullPointerException("Name arg is null in getTaskByName"); //$NON-NLS-1$
        }

        // Sequential search (Could use binary but list is short)
        for (Taskable task : instance.tasks)
        {
            if (name.equals(task.getName()))
            {
                return task;
            }
        }
        log.error("Couldn't find task by name["+name+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        return null;
    }

    /**
     * Returns a task by a given Class.
     * @param theClass the class of task to be returned
     * @return Returns a task by a given Class
     */
    public static Taskable getTaskByClass(final Class<?> theClass)
    {
        if (theClass == null)
        {
            throw new NullPointerException("Class arg is null in getTaskByClass"); //$NON-NLS-1$
        }

        // Sequential search (Could use binary but list is short)
        for (Taskable task : instance.tasks)
        {
            if (task.getClass() == theClass)
            {
                return task;
            }
        }
        log.error("Couldn't find task by class ["+theClass.getSimpleName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        return null;
    }

    /**
     * Register a service for other UI components to use.
     * @param priority the priority of the service used for ordering them
     * @param serviceName the name of the service
     * @param tableId the table ID that the service is provided for
     * @param command the command to be sent
     * @param task the task that provides the service
     * @param iconName the name of the icon to be used
     * @param tooltip the tooltip text for any UI
     * @return a service info object that provide the service
     */
    public static ServiceInfo registerService(final Integer       priority,
                                              final String        serviceName, 
                                              final int           tableId, 
                                              final CommandAction command, 
                                              final Taskable      task, 
                                              final String        iconName, 
                                              final String        tooltip)
    {
        return registerService(priority, serviceName, tableId, command, task, iconName, tooltip, false);
    }

    /**
     * Register a service for other UI components to use.
     * @param priority the priority of the service used for ordering them
     * @param serviceName the name of the service
     * @param tableId the table ID that the service is provided for
     * @param command the command to be sent
     * @param task the task that provides the service
     * @param iconName the name of the icon to be used
     * @param tooltip the tooltip text for any UI
     * @return a service info object that provide the service
     */
    public static ServiceInfo registerService(final Integer       priority,
                                              final String        serviceName, 
                                              final int           tableId, 
                                              final CommandAction command, 
                                              final Taskable      task, 
                                              final String        iconName, 
                                              final String        tooltip,
                                              final boolean       isDefault)
    {
        String hashName = ServiceInfo.getHashKey(serviceName, task, tableId);
        if (instance.services.get(hashName) != null)
        {
            return null;
        }
        
        ServiceInfo serviceInfo = new ServiceInfo(priority, serviceName, tableId, command, task, iconName, tooltip, isDefault);
        //log.debug("REG: "+serviceInfo.getHashKey());
        
        return registerService(serviceInfo);
    }
    
    public static ServiceInfo registerService(final ServiceInfo serviceInfo)
    {
        String hashName = ServiceInfo.getHashKey(serviceInfo.getName(), serviceInfo.getTask(), serviceInfo.getTableId());
        if (serviceInfo.getTableId() == -1)
        {
            instance.genericService.add(serviceInfo);
            instance.services.put(hashName, serviceInfo);
            
        } else 
        {
            instance.services.put(hashName, serviceInfo);
            List<ServiceInfo> serviceList = instance.servicesByTable.get(serviceInfo.getTableId() );
            if (serviceList == null)
            {
                serviceList = new ArrayList<ServiceInfo>();
                instance.servicesByTable.put(serviceInfo.getTableId() , serviceList);
            }
            serviceList.add(serviceInfo);
            return serviceInfo;
        }
        return null;
    	
    }
    /**
     * @param hashName the hashedName
     */
    public static void unregisterService(final String hashName)
    {
        ServiceInfo srvInfo = instance.services.get(hashName);
        if (srvInfo != null)
        {
            instance.services.remove(hashName);
            if (srvInfo.getTableId() > -1)
            {
                List<ServiceInfo> serviceList = instance.servicesByTable.get(srvInfo.getTableId());
                if (serviceList != null)
                {
                    serviceList.remove(srvInfo);
                }
            } else
            {
                instance.genericService.remove(srvInfo);
            }
        } else
        {
            log.error("Couldn't find service ["+hashName+"]");
        }
    }
    
    /**
     * @return the default service or null if there is none.
     */
    public static ServiceInfo getDefaultService()
    {
        for (ServiceInfo si : instance.services.values())
        {
            if (si.isDefault())
            {
                if (AppContextMgr.isSecurityOn() && !si.isPermissionOK())
                {
                    return null;
                }
                return si;
            }
        }
        return null;
    }
    
    /**
     * Removes all the ServiceInfo object that are "owned" by a task.
     * @param task the task that owns the services
     */
    public static void removeServicesByTask(final Taskable task)
    {
        // Create a mutable list so we can remove items from the actual list
        Vector<ServiceInfo>  list = new Vector<ServiceInfo>(instance.services.values());
        
        for (ServiceInfo service : list)
        {
            if (service.getTask() == task)
            {
                instance.services.remove(service.getName());
            }
        }
        
        for (Collection<ServiceInfo> srvList : instance.servicesByTable.values())
        {
            for (ServiceInfo srv : new Vector<ServiceInfo>(srvList))
            {
                if (srv.getTask() == task)
                {
                    srvList.remove(srv);
                }
            }
        }
        
        // Remove from generic List
        for (ServiceInfo srvInfo : new Vector<ServiceInfo>(instance.genericService))
        {
            if (srvInfo.getTask() == task)
            {
                instance.genericService.remove(srvInfo);
            }
        }
    }

    /**
     * Removes all the ServiceInfo object that are "owned" by a task.
     * @param task the task that owns the services
     */
    public static void removeServicesByTaskAndTable(final Taskable task,
                                                    final int      tableId)
    {
        Vector<ServiceInfo> list = new Vector<ServiceInfo>(instance.services.values());
        
        for (ServiceInfo service : list)
        {
            if (service.getTask() == task && service.getTableId() == tableId)
            {
                instance.services.remove(service.getName());
            }
        }
        
        for (Collection<ServiceInfo> srvList : instance.servicesByTable.values())
        {
            for (ServiceInfo srv : new Vector<ServiceInfo>(srvList))
            {
                if (srv.getTask() == task)
                {
                    srvList.remove(srv);
                }
            }
        }
    }

    /**
     * Returns the ServiceInfo object for a given service and the table it is to act upon.
     * @param serviceName name of service to be provided
     * @param tableId the table ID of the data to be serviced
     * @return the ServiceInfo object for a given service and the table it is to act upon.
     */
    /*public static ServiceInfo checkForService(final String serviceName, final int tableId)
    {
        return instance.services.get(ServiceInfo.getHashKey(serviceName, tableId));
    }*/

    /**
     * Returns a list of services for a table id, this will always return a list (empty list, never null).
     * @param tableId the table id of the list of services
     * @return Returns a list of services for a table id
     */
    public static List<ServiceInfo> checkForServices(final int tableId)
    {
        List<ServiceInfo> serviceList = instance.servicesByTable.get(tableId);
        if (serviceList == null)
        {
            serviceList = new ArrayList<ServiceInfo>();
        }
        
        for (ServiceInfo srvInfo : new ArrayList<ServiceInfo>(serviceList))
        {
            if (AppContextMgr.isSecurityOn() && !srvInfo.isPermissionOK())
            {
                serviceList.remove(srvInfo);
            }
        }
        
        for (ServiceInfo srvInfo : instance.genericService)
        {
            if (!serviceList.contains(srvInfo))
            {
                if (AppContextMgr.isSecurityOn() && !srvInfo.isPermissionOK())
                {
                    continue;
                }
                if (!srvInfo.isAvailable(tableId))
                {
                	continue;
                }
                serviceList.add(srvInfo);
            }
        }
        Collections.sort(serviceList);
        return serviceList;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CommandListener#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isAction(APP_RESTART_ACT))
        {
            for (ServiceInfo srvInfo : instance.genericService)
            {
                if (AppContextMgr.isSecurityOn())
                {
                    srvInfo.resetPermissions();
                }
            }
        }
    }
    
    /**
     * 
     */
    public static void dump()
    {
        for (Integer tableId : instance.servicesByTable.keySet())
        {
            DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(tableId);
            System.out.println("------------------"+tableInfo.getTitle()+"------------------");
            for (ServiceInfo s : instance.servicesByTable.get(tableId))
            {
                System.out.println("Name:      "+s.getName());
                System.out.println("Task Name: "+s.getTask().getName());
                //System.out.println("Key:       "+s.getHashKey());
                System.out.println("\n");
            }
        }
    }

}
