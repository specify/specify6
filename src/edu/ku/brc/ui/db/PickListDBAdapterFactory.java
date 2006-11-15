/**
* Copyright (C) 2006  The University of Kansas
*
* [INSERT KU-APPROVED LICENSE TEXT HERE]
*
*/
package edu.ku.brc.ui.db;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Nov 10, 2006
 *
 */
public class PickListDBAdapterFactory
{
    protected static final String propName = "edu.ku.brc.ui.db.PickListDBAdapterFactory";
    
    private static PickListDBAdapterFactory instance = null;
   
    /**
     * @param name
     * @return
     */
    public PickListDBAdapterIFace create(final String name, final boolean createWhenNotFound)
    {
        throw new RuntimeException("You must override this factory with your own.");
    }
    
    /**
     * @return
     */
    public PickListIFace getPickList(final String name)
    {
        throw new RuntimeException("You must override this factory with your own.");
    }
    
    /**
     * @return
     */
    public PickListIFace createPickList()
    {
        throw new RuntimeException("You must override this factory with your own.");
    }
    
    /**
     * @return
     */
    public PickListItemIFace createPickListItem()
    {
        throw new RuntimeException("You must override this factory with your own.");
    }
    
    /**
     * Returns the instance of the DataProviderIFace.
     * @return the instance of the DataProviderIFace.
     */
    public static PickListDBAdapterFactory getInstance()
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
                instance = Class.forName(factoryName).asSubclass(PickListDBAdapterFactory.class).newInstance();
                return instance;
                 
            } catch (Exception e) 
            {
                InternalError error = new InternalError("Can't instantiate PickListAdapterFactory factory " + factoryName);
                error.initCause(e);
                throw error;
            }
        }
        
        throw new InternalError("Can't instantiate PickListAdapterFactory factory becase " + propName + " has not been set.");
    }
}
