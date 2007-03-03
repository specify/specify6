/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.dbsupport;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Mar 2, 2007
 *
 */
public abstract class CustomQueryFactory
{
    public static final String factoryName = "edu.ku.brc.dbsupport.CustomQueryFactory";
    
    protected static CustomQueryFactory instance = null;
    
    /**
     * Returns a custom query by name.
     * @param queryName the name of the query
     * @return the CustomQuery
     */
    public abstract CustomQuery getQuery(final String queryName);
    
    
    /**
     * Returns the instance of the CustomQueryFactory.
     * @return the instance of the CustomQueryFactory.
     */
    public static CustomQueryFactory getInstance()
    {
        if (instance != null)
        {
            return instance;
            
        }
        // else
        String factoryNameStr = AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(factoryName);
                    }
                });
            
        if (factoryNameStr != null) 
        {
            try 
            {
                instance = (CustomQueryFactory)Class.forName(factoryNameStr).newInstance();
                return instance;
                 
            } catch (Exception e) 
            {
                InternalError error = new InternalError("Can't instantiate CustomQueryFactory factory " + factoryNameStr);
                error.initCause(e);
                throw error;
            }
        }
        return null;
    }

}
