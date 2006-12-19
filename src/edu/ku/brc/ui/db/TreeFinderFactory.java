/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.ui.db;

import java.security.AccessController;
import java.security.PrivilegedAction;

import edu.ku.brc.specify.datamodel.TreeDefIface;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public abstract class TreeFinderFactory
{
    protected static final String propName = "edu.ku.brc.ui.db.TreeFinderFactory";
    
    private static TreeFinderFactory instance;
    
    public abstract TreeDefIface<?,?,?> findTreeDefinition(String type);
    
    /**
     * Returns the instance of the TreeFinderFactory.
     * @return the instance of the TreeFinderFactory.
     */
    public static TreeFinderFactory getInstance()
    {
        if (instance != null)
        {
            return instance;
            
        }
        // else
        String factoryName = AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(
                    propName);}});
            
        if (factoryName != null) 
        {
            try 
            {
                instance = Class.forName(factoryName).asSubclass(TreeFinderFactory.class).newInstance();
                return instance;
                 
            } catch (Exception e) 
            {
                InternalError error = new InternalError("Can't instantiate TreeFinderFactory factory " + factoryName);
                error.initCause(e);
                throw error;
            }
        }
        
        throw new InternalError("Can't instantiate TreeFinderFactory factory becase " + propName + " has not been set.");
    }
}
