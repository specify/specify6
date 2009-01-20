/*
 * Copyright (C) 2009  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.exceptions;

import java.security.AccessController;
import java.security.PrivilegedAction;

import edu.ku.brc.ui.FeedBackSender;


/**
 * Used to tracker and send Handled Exceptions.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 10, 2009
 *
 */
public class ExceptionTracker extends FeedBackSender
{
    public static final String factoryName = "edu.ku.brc.exceptions.ExceptionTracker"; //$NON-NLS-1$

    protected static ExceptionTracker instance = null;
    
    /**
     * 
     */
    public ExceptionTracker()
    {
        
    }
    
    /**
     * @return the url that the info should be sent to
     */
    protected String getSenderURL()
    {
        return "http://specify6-test.nhm.ku.edu/exception.php";
    }
    
    /**
     * Returns the instance of the ExceptionTracker.
     * @return the instance of the ExceptionTracker.
     */
    public static ExceptionTracker getInstance()
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
                instance = (ExceptionTracker)Class.forName(factoryNameStr).newInstance();
                return instance;
                 
            } catch (Exception e) 
            {
                InternalError error = new InternalError("Can't instantiate ExceptionTracker factory " + factoryNameStr); //$NON-NLS-1$
                error.initCause(e);
                throw error;
            }
        }
        return instance = new ExceptionTracker();
    }

}
