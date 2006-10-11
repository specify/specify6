/**
* Copyright (C) 2006  The University of Kansas
*
* [INSERT KU-APPROVED LICENSE TEXT HERE]
*
*/
package edu.ku.brc.dbsupport;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 */
public class DataProviderFactory
{
    protected static final String propName = "edu.ku.brc.dbsupport.DataProvider";
    
    private static DataProviderIFace instance = null;
   
    
    /**
     * Returns the instance of the AuditInterceptor.
     * @return the instance of the AuditInterceptor.
     */
    public static DataProviderIFace getInstance()
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
                instance = Class.forName(factoryName).asSubclass(DataProviderIFace.class).newInstance();
                return instance;
                 
            } catch (Exception e) 
            {
                InternalError error = new InternalError("Can't instantiate DataProviderFactory factory " + factoryName);
                error.initCause(e);
                throw error;
            }
        }
        
        throw new InternalError("Can't instantiate DataProviderFactory factory becase " + propName + " has not been set.");
    }
}
