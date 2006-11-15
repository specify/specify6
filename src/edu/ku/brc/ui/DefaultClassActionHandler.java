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
 *
 * @code_status Alpha
 * @author jstewart
 */
public class DefaultClassActionHandler
{
    protected static DefaultClassActionHandler instance;
    protected Hashtable<Class<?>, ActionListener> handlers;
    
    protected DefaultClassActionHandler()
    {
        handlers = new Hashtable<Class<?>, ActionListener>();
    }
    
    public static synchronized DefaultClassActionHandler getInstance()
    {
        if (instance==null)
        {
            instance = new DefaultClassActionHandler();
        }
        
        return instance;
    }
    
    public void registerActionHandler(Class<?> clazz, ActionListener handler)
    {
        handlers.put(clazz, handler);
    }
    
    public ActionListener getDefaultClassActionHandler(Class<?> clazz)
    {
        return handlers.get(clazz);
    }
}