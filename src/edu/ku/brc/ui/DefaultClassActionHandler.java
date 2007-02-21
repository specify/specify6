/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.ui;

import java.awt.event.ActionListener;
import java.util.Hashtable;

/**
 * A class that provides mappings from classes to ActionListeners
 * that should fire when objects of those classes are 'activated'
 * in some manner, most likely by a double-click action from a user.
 *
 * @code_status Beta
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