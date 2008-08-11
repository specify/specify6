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
    public static final String factoryName = "edu.ku.brc.dbsupport.DataProvider"; //$NON-NLS-1$
    
    private static DataProviderIFace instance = null;
   
    
    /**
     * Returns the instance of the DataProviderIFace.
     * @return the instance of the DataProviderIFace.
     */
    public static DataProviderIFace getInstance()
    {
        if (instance != null)
        {
            return instance;
            
        }
        // else
        String factoryNameStr = AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(
                            factoryName);}});
            
        if (factoryNameStr != null) 
        {
            try 
            {
                instance = Class.forName(factoryNameStr).asSubclass(DataProviderIFace.class).newInstance();
                return instance;
                 
            } catch (Exception e) 
            {
                InternalError error = new InternalError("Can't instantiate DataProviderFactory factory " + factoryNameStr); //$NON-NLS-1$
                error.initCause(e);
                throw error;
            }
        }
        
        throw new InternalError("Can't instantiate DataProviderFactory factory becase " + factoryName + " has not been set."); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
