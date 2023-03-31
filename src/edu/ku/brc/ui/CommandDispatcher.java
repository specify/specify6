/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.ui;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;


/**
 * Dispatches a command to a "set" of listeners. Typically, each Task registered itself as a "type"
 * of listener that listens for "types" of commands. Consumers can set a command to have been "consumed"
 * but they are not required to do so. Meaning any listener and listen for command and choose to act or not, but
 * it doesn't mean they have to consume them.<br><BR>
 * Note: All primary tasks get first try at consuming the commands.
 *
 * @code_status UComplete
 * 
 * @author rods
 * 
 */
public class CommandDispatcher
{
    // Static Data Members
   protected static final Logger log = Logger.getLogger(CommandDispatcher.class);
    private static final CommandDispatcher instance = new CommandDispatcher();
    
    // Data Members
    protected Hashtable<String, Vector<CommandListener>> listeners = new Hashtable<String, Vector<CommandListener>>();
    
    /**
     * Protected Constructor of Singleton.
     *
     */
    protected CommandDispatcher()
    {
        // do nothing
    }
    
    /**
     * Returns singleton.
     * @return returns singleton of Context Manager
     */ 
    public static CommandDispatcher getInstance()
    {
        return instance;
    }
    
    /**
     * Registers a listener.
     * @param type the type of command
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
            log.error("Listener has already been registered. "+listener);
        }
    }
    
    /**
     *  Unregisters a listener. Checks to see if it is the current listener.
     * @param type the type of command
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
            //throw new RuntimeException("Listener is not registered."+listener);
            log.error("Listener is not registered."+listener);
        }
    }
    
    /**
     * Requests a command to be dispatched.
     * @param cmdAction the command to be dispatched
     * @return true is consumed, false if not
     */
    public static boolean dispatch(final CommandAction cmdAction)
    {
        //log.debug(cmdAction);
        Vector<CommandListener> list = instance.listeners.get(cmdAction.getType());
        if (list != null)
        {
            for (CommandListener l : new Vector<CommandListener>(list))
            {
                l.doCommand(cmdAction);
                if (cmdAction.isConsumed)
                {
                    return true;
                }
            }
            return false;
        }
        // else
        if (!cmdAction.getType().endsWith("Database"))
        {
            log.warn("Type of Listeners couldn't be found["+cmdAction.getType()+"]");
        }
        return false;
    }

}
