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

import java.util.Hashtable;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.IconManager;

/**
 * This class desrcibes a service that can be provided by a task for a specific type of data. 
 * The data is specified by the "tableId" number which also corresponds to a Hibernate Java POJO.
 * Services are mapped to a table ID because not all service can act on all tables.<br><br>
 * <b>The service info MUST be able to provide icons in the standard sizes of 32, 24, and 16.</b><br>
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class ServiceInfo
{
    private static final Logger log = Logger.getLogger(ServiceInfo.class);
            
    protected String         name;
    protected int            tableId;
    protected Taskable       task;
    protected String         tooltip;
    protected CommandAction  command;
    
    
    protected Hashtable<String, ImageIcon> icons = new Hashtable<String, ImageIcon>();
    
    /**
     * Constructs a service info object describing the service for UI components to use; also looks up the iconName in the IconCache
     * and creates icons for sizes 16, 24, and 32.
     * @param serviceName the name of the service
     * @param tableId the table ID that the service is provided for
     * @param command the command to be sent
     * @param task the task that provides the service
     * @param iconName the name of the icon to be used
     * @param tooltip the tooltip text for any UI
     */
    public ServiceInfo(final String serviceName, 
                       final int tableId, 
                       final CommandAction command, 
                       final Taskable task, 
                       final String iconName,
                       final String tooltip)
    {
        this.name    = serviceName;
        this.tableId = tableId;
        this.command = command;
        this.task    = task;
        this.tooltip = tooltip;
        
        addIcon(IconManager.getIcon(iconName, IconManager.IconSize.Std16), IconManager.IconSize.Std16);
        addIcon(IconManager.getIcon(iconName, IconManager.IconSize.Std24), IconManager.IconSize.Std24);
        addIcon(IconManager.getIcon(iconName, IconManager.IconSize.Std32), IconManager.IconSize.Std32);
    }
    
 
    /**
     * Adds an icon to be associated with the service.
     * @param icon the icon to be registered with the service
     * @param iconSize the size of the icon
     */
    public void addIcon(final ImageIcon icon, final IconManager.IconSize iconSize)
    {
        if (icon != null)
        {
            icons.put(iconSize.toString(), icon);
        } else
        {
            log.error("Couldn't load icon for size ["+iconSize+"]");
        }
    }

    /**
     * Returns an icon of a specific size.
     * @param iconSize the sie of the icon to be returned
     * @return an icon of a specific size
     */
    public ImageIcon getIcon(final IconManager.IconSize iconSize)
    {
        return icons.get(iconSize.toString());
    }
    
    /**
     * Returns the a string that includes the name and the table ID.
     * @return the a string that includes the name and the table ID
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
