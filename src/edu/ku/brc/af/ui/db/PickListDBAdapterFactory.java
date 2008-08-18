/**
* Copyright (C) 2006  The University of Kansas
*
* [INSERT KU-APPROVED LICENSE TEXT HERE]
*
*/
package edu.ku.brc.af.ui.db;

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
    protected static final String propName = "edu.ku.brc.ui.db.PickListDBAdapterFactory"; //$NON-NLS-1$
    
    private static PickListDBAdapterFactory instance = null;
   
    /**
     * @param name
     * @return
     */
    public PickListDBAdapterIFace create(@SuppressWarnings("unused") final String name,  //$NON-NLS-1$
                                         @SuppressWarnings("unused") final boolean createWhenNotFound) //$NON-NLS-1$
    {
        throw new RuntimeException("You must override this factory with your own."); //$NON-NLS-1$
    }
    
    /**
     * @return
     */
    public PickListIFace getPickList(@SuppressWarnings("unused") final String name) //$NON-NLS-1$
    {
        throw new RuntimeException("You must override this factory with your own."); //$NON-NLS-1$
    }
    
    /**
     * @return
     */
    public PickListIFace createPickList()
    {
        throw new RuntimeException("You must override this factory with your own."); //$NON-NLS-1$
    }
    
    /**
     * @return
     */
    public PickListItemIFace createPickListItem()
    {
        throw new RuntimeException("You must override this factory with your own."); //$NON-NLS-1$
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
                InternalError error = new InternalError("Can't instantiate PickListAdapterFactory factory " + factoryName); //$NON-NLS-1$
                error.initCause(e);
                throw error;
            }
        }
        
        throw new InternalError("Can't instantiate PickListAdapterFactory factory becase " + propName + " has not been set."); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
