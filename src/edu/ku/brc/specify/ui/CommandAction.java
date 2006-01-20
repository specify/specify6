/* Filename:    $RCSfile: CommandAction.java,v $
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
package edu.ku.brc.specify.ui;

/**
 * Represents a single command action typically between the UI and a task, sometimes between tasks
 * The "type" of command determines who will be listening for it.
 * 
 * @author rods
 *
 */
public class CommandAction
{

    protected final String type;
    protected final String action;
    protected final int    tableId;
    
    protected Object data;
    
    protected boolean isConsumed = false;
    
    /**
     * Constructs a command
     * @param type the type of command determines who listens for it
     * @param action the name of the action to be performed (contract between producer and consumer)
     * @param data the data to be passed
     */
    public CommandAction(final String type, final String action, final Object data)
    {
        this.type    = type;
        this.action  = action;
        this.tableId = -1;
        this.data    = data;
    }
    
    /**
     * Constructs a command
     * @param type the type of command determines who listens for it
     * @param action the name of the action to be performed (contract between producer and consumer)
     * @param tableId the table id that the command is associated with
     */
    public CommandAction(final String type, final String action, final int tableId)
    {
        this.type    = type;
        this.action  = action;
        this.tableId = tableId;
        this.data    = null;
    }
    
    /**
     * Constructs a command
     * @param type the type of command determines who listens for it
     * @param action the name of the action to be performed (contract between producer and consumer)
     */
    public CommandAction(final String type, final String action)
    {
        this.type   = type;
        this.action = action;
        this.tableId = -1;
        this.data   = null;
    }
    
    public String getAction()
    {
        return action;
    }

    public void setData(final Object data)
    {
        this.data = data;
    }

    public Object getData()
    {
        return data;
    }

    public String getType()
    {
        return type;
    }

    public boolean isConsumed()
    {
        return isConsumed;
    }

    public void setConsumed(boolean isConsumed)
    {
        this.isConsumed = isConsumed;
    }

    public int getTableId()
    {
        return tableId;
    }
}
