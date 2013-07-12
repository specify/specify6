/* Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PickListDBAdapterFactory.class, e);
                InternalError error = new InternalError("Can't instantiate PickListAdapterFactory factory " + factoryName); //$NON-NLS-1$
                error.initCause(e);
                throw error;
            }
        }
        
        throw new InternalError("Can't instantiate PickListAdapterFactory factory becase " + propName + " has not been set."); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
