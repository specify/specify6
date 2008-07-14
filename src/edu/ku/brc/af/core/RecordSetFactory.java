/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.af.core;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.security.AccessController;

import edu.ku.brc.dbsupport.RecordSetIFace;

/**
 * A factory for creating RecordSet objects.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Jul 14, 2008
 *
 */
public class RecordSetFactory
{
    public static final String factoryName = "RecordSetFactory"; //$NON-NLS-1$
    
    //private static final Logger log = Logger.getLogger(WebLinkMgr.class);
    
    protected static RecordSetFactory instance = null;
    
    /**
     * Protected Constructor
     */
    protected RecordSetFactory()
    {
    }

    /**
     * Returns the instance to the singleton
     * @return  the instance to the singleton
     */
    public static RecordSetFactory getInstance()
    {
        if (instance != null)
        {
            return instance;
        }
        
        // else
        String factoryNameStr = AccessController.doPrivileged(new java.security.PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(factoryName);
                    }
                });
            
        if (isNotEmpty(factoryNameStr)) 
        {
            try 
            {
                return instance = (RecordSetFactory)Class.forName(factoryNameStr).newInstance();
                 
            } catch (Exception e) 
            {
                InternalError error = new InternalError("Can't instantiate RecordSet factory " + factoryNameStr); //$NON-NLS-1$
                error.initCause(e);
                throw error;
            }
        }
        return null;
    }
    
    /**
     * @return a recordset object
     */
    public RecordSetIFace createRecordSet()
    {
        throw new RuntimeException("Must create your own implementation.");
    }
    
}

