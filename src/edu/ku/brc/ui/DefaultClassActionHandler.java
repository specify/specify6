/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.ui;

import java.awt.event.ActionListener;
import java.util.Hashtable;

/**
 * A class that provides mappings from classes to ActionListeners
 * that should fire when objects of those classes are 'activated'
 * in some manner, most likely by a double-click action from a user.
 *
 * @code_status Complete
 * @author jstewart
 */
public class DefaultClassActionHandler
{
    /** The singleton instance of the class. */
    protected static DefaultClassActionHandler instance;
    
    /** The mapping from classes to the action handlers. */
    protected Hashtable<Class<?>, ActionListener> handlers;
    
    /**
     * Creates an instance.
     */
    protected DefaultClassActionHandler()
    {
        handlers = new Hashtable<Class<?>, ActionListener>();
    }
    
    /**
     * Returns the singleton instance of the class, creating one
     * if needed.
     *
     * @return the singleton instance
     */
    public static synchronized DefaultClassActionHandler getInstance()
    {
        if (instance==null)
        {
            instance = new DefaultClassActionHandler();
        }
        
        return instance;
    }
    
    /**
     * Registers the given {@link ActionListener} to handle actions on instances
     * of the given class.
     *
     * @param clazz the class handled by the given ActionListener
     * @param handler the handler
     */
    public void registerActionHandler(Class<?> clazz, ActionListener handler)
    {
        handlers.put(clazz, handler);
    }
    
    /**
     * Returns the handler registered for the given class, or null if one
     * is not registered.
     *
     * @param clazz the class in question
     * @return the registered handler, or null if none is registered
     */
    public ActionListener getDefaultClassActionHandler(Class<?> clazz)
    {
        return handlers.get(clazz);
    }
}
