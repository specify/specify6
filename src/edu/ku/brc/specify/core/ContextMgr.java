/* Filename:    $RCSfile: MainPanel.java,v $
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
package edu.ku.brc.specify.core;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.ui.CommandAction;

/**
 * Manages the task context of the UI. The task context is controlled by what tab is visible in the main pane
 * When tasks are registered they are asked for the NavBoxes and those are placed in the NavBox Manager. when
 * they are unregistered the NavBoxes are removed
 *
 * Status: Finished
 *
 * @author rods
 *
 */
public class ContextMgr
{
    // Static Data Members
    private static final Logger      log      = Logger.getLogger(ContextMgr.class);
    private static final ContextMgr  instance = new ContextMgr();

    // Data Members
    protected Taskable         currentContext         = null;
    protected Vector<Taskable> tasks                  = new Vector<Taskable>();

    protected Hashtable<String, ServiceInfo>        services        = new Hashtable<String, ServiceInfo>();
    protected Hashtable<Integer, List<ServiceInfo>> servicesByTable = new Hashtable<Integer, List<ServiceInfo>>();

    /**
     * Protected Constructor of Singleton
     *
     */
    protected ContextMgr()
    {

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
     * Request for a change in context
     * @param task the task requesting the context
     */
    public static void requestContext(Taskable task)
    {
        if (task != instance.currentContext)
        {
            if (instance.currentContext != null)
            {
                NavBoxMgr.unregister(instance.currentContext);
            }

            if (task != null)
            {
                NavBoxMgr.register(task);
             }

            instance.currentContext = task;
        }

    }

    /**
     * Registers a task
     * @param task the task to be register
     */
    public static void register(Taskable task)
    {
        instance.tasks.addElement(task);
    }

    /**
     *  Unregisters a task. Checks to see if it is the current task, if so then it unregisters the NavBoxes
     * @param task the task to be unregistered
     */
    public static void unregister(Taskable task)
    {
        /*if (currentContext == task)
        {
            NavBoxMgr.getInstance().unregister(currentContext);
            currentContext = null;
        }*/
        instance.tasks.removeElement(task);
    }

    /**
     * Returns a task by a given name
     * @param name name of task to be returned
     * @return Returns a task by a given name
     */
    public static Taskable getTaskByName(final String name)
    {
        if (name == null)
        {
            throw new NullPointerException("Name arg is null in getTaskByName");
        }

        // Sequential search (Could use binary but list is short)
        for (Taskable task : instance.tasks)
        {
            if (name.equals(task.getName()))
            {
                return task;
            }
        }
        log.error("Couldn't find task by name["+name+"]");
        return null;
    }

    /**
     * Returns a task by a given Class
     * @param theClass the class of task to be returned
     * @return Returns a task by a given Class
     */
    public static Taskable getTaskByClass(final Class theClass)
    {
        if (theClass == null)
        {
            throw new NullPointerException("Class arg is null in getTaskByClass");
        }

        // Sequential search (Could use binary but list is short)
        for (Taskable task : instance.tasks)
        {
            if (task.getClass() == theClass)
            {
                return task;
            }
        }
        log.error("Couldn't find task by class ["+theClass.getSimpleName()+"]");
        return null;
    }

    /**
     * Register a service for other UI components to use
     * @param name the name of the service
     * @param tableId the table ID that the service is provided for
     * @param command the command to be sent
     * @param task the task that provides the service
     * @param tooltip the tooltip text for any UI
     * @return a service info object that provide the service
     */
    public static ServiceInfo registerService(final String name, final int tableId, final CommandAction command, final Taskable task, final String tooltip)
    {
        ServiceInfo serviceInfo = new ServiceInfo(name, tableId, command, task, tooltip);
        instance.services.put(serviceInfo.getHashKey(), serviceInfo);
        List<ServiceInfo> serviceList = instance.servicesByTable.get(tableId);
        if (serviceList == null)
        {
            serviceList = new ArrayList<ServiceInfo>();
            instance.servicesByTable.put(tableId, serviceList);
        }
        serviceList.add(serviceInfo);
        return serviceInfo;
    }

    /**
     * Returns the ServiceInfo object for a given service and the table it is to act upon.
     * @param name name of service to be provided
     * @param tableId the table ID of the data to be serviced
     * @return the ServiceInfo object for a given service and the table it is to act upon.
     */
    public static ServiceInfo checkForService(final String name, final int tableId)
    {
        return instance.services.get(ServiceInfo.getHashKey(name, tableId));
    }

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
            return new ArrayList<ServiceInfo>();
        }
        return serviceList;
    }

}
