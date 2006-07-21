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
package edu.ku.brc.specify.core;

import java.util.Hashtable;

import javax.swing.ImageIcon;

import edu.ku.brc.specify.ui.CommandAction;
import edu.ku.brc.specify.ui.IconManager;

/**
 * This class desrcibes a service that can be provided by a task for a specific type of data. 
 * The data is specified by the "tableId" number which also corresponds to a Hibernate Java POJO.
 * Services are mapped to a table ID because not all service can act on all tables.<br><br>
 * The service info MUST be able to provide icons in the standard sizes of 32, 24, and 16.<br>
 * 
 * @author rods
 *
 */
public class ServiceInfo
{
    protected String         name;
    protected int            tableId;
    protected Taskable       task;
    protected String         tooltip;
    protected CommandAction  command;
    
    
    protected Hashtable<String, ImageIcon> icons = new Hashtable<String, ImageIcon>();
    
    /**
     * Constructs a service info object describing the service for UI components to use
     * @param name the name of the service
     * @param tableId the table ID that the service is provided for
     * @param command the command to be sent
     * @param task the task that provides the service
     * @param tooltip the tooltip text for any UI
     */
    public ServiceInfo(final String name, final int tableId, final CommandAction command, final Taskable task, final String tooltip)
    {
        this.name    = name;
        this.tableId = tableId;
        this.command = command;
        this.task    = task;
        this.tooltip = tooltip;
    }
    
 
    /**
     * @param icon the icon to be registered with the service
     * @param iconSize the size of the icon
     */
    public void addIcon(final ImageIcon icon, final IconManager.IconSize iconSize)
    {
        icons.put(iconSize.toString(), icon);
    }

    /**
     * returns an icon of a specific size
     * @param iconSize the sie of the icon to be returned
     * @return returns an icon of a specific size
     */
    public ImageIcon getIcon(final IconManager.IconSize iconSize)
    {
        return icons.get(iconSize.toString());
    }
    
    /**
     * Returns the a string that includes the name and the table ID
     * @return Returns the a string that includes the name and the table ID
     */
    public String getHashKey()
    {
        return getHashKey(this.name, this.tableId);
    }

    public String getName()
    {
        return name;
    }


    public int getTableId()
    {
        return tableId;
    }


    public Taskable getTask()
    {
        return task;
    }


    public String getTooltip()
    {
        return tooltip;
    }

    public CommandAction getCommandAction()
    {
        return command;
    }

    //------------------------------------------
    //-- Static Methods
    //------------------------------------------
    

    /**
     * Returns the a string that includes the name and the table ID
     * @return Returns the a string that includes the name and the table ID
     */
    public static String getHashKey(final String name, final int tableId)
    {
        return name + "_" + Integer.toString(tableId);
    }

    
}
