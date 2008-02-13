/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.ui.forms;

import java.security.AccessController;
import java.security.PrivilegedAction;

import edu.ku.brc.dbsupport.DBFieldInfo;

/**
 * A factory for serving up default value for a data member in a data object.
 * 
 * @author rod
 *
 * @code_status Complete
 *
 * Feb 12, 2008
 *
 */
public abstract class DefaultFormFieldValueFactory
{
    public static final String factoryName = "edu.ku.brc.ui.forms.DefaultFormFieldValueFactory";
    
    protected static DefaultFormFieldValueFactory instance = null;
    
    /**
     * Returns a default value for a field.
     * 
     * @param fieldInfo the field infomation.
     * @return the default value
     */
    public abstract Object getValueFor(final DBFieldInfo fieldInfo);
    
    /**
     * Returns the instance of the DefaultFormFieldValueFactory.
     * @return the instance of the DefaultFormFieldValueFactory.
     */
    public static DefaultFormFieldValueFactory getInstance()
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
                instance = (DefaultFormFieldValueFactory)Class.forName(factoryNameStr).newInstance();
                return instance;
                 
            } catch (Exception e) 
            {
                InternalError error = new InternalError("Can't instantiate DefaultFormFieldValueFactory factory " + factoryNameStr);
                error.initCause(e);
                throw error;
            }
        }
        return null;
    }

}