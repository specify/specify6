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
package edu.ku.brc.specify.ui;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Dispatches a command to a "set" of listeners. Typically, each Task registered itself as a "type"
 * of listener that listens for "types" of commands. Consumers can set a command to have been "consumed"
 * but they are not required to do so. Meaning any listener and listen for command and choose to act or not, but
 * it doesn't mean they have to consume them.<br><BR>
 * Note: All primary tasks get first try at consuming the commands.
 * 
 * @author rods
 * 
 */
public class CommandDispatcher
{
    // Static Data Members
    private static Log log  = LogFactory.getLog(CommandDispatcher.class);
    
    private static CommandDispatcher instance = new CommandDispatcher();
    
    // Data Members
    protected Hashtable<String, Vector<CommandListener>> listeners = new Hashtable<String, Vector<CommandListener>>();
    
    /**
     * Protected Constructor of Singleton
     *
     */
    protected CommandDispatcher()
    { 
    }
    
    /**
     * Returns singleton
     * @return returns singleton of Context Manager
     */ 
    public static CommandDispatcher getInstance()
    {
        return instance;
    }
    
    /**
     * Registers a listener
     * @param listener the listener to be register
     */
    public static void register(final String type, final CommandListener listener)
    {
        Vector<CommandListener> list = instance.listeners.get(type);
        if (list == null)
        {
            list = new Vector<CommandListener>();
            instance.listeners.put(type, list);
            
        }
        if (!list.contains(listener))
        {
            
            list.addElement(listener);
        } else
        {
            throw new RuntimeException("Listener has already been registered. "+listener);
        }
    }
    
    /**
     *  Unregisters a listener. Checks to see if it is the current listener
     * @param listener the listener to be unregistered
     */
    public static void unregister(final String type, final CommandListener listener)
    {
        Vector<CommandListener> list = instance.listeners.get(type);
        if (list.contains(listener))
        {
            list.removeElement(listener);
        } else
        {
            throw new RuntimeException("Listener is not registered."+listener);
        }
    }
    
    /**
     * Requests a command to be dispatched
     * @param cmdAction the command to be dispatched
     * @return true is consumed, false if not
     */
    public static boolean dispatch(final CommandAction cmdAction)
    {
        Vector<CommandListener> list = instance.listeners.get(cmdAction.getType());
        if (list != null)
        {
            for (CommandListener l : list)
            {
                l.doCommand(cmdAction);
                if (cmdAction.isConsumed)
                {
                    return true;
                }
            }
            return false;
        } else
        {
            throw new RuntimeException("Type of Listeners couldn't be found["+cmdAction.getType()+"]");
        } 
    }

}
