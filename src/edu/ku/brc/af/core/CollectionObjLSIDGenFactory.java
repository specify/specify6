/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.ku.brc.af.core;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Base Factory class (not Abstract) for generating LSID for Collection Objects. 
 *
 * @code_status Beta
 *
 * @author rods
 *
 */
public class CollectionObjLSIDGenFactory
{
    public static final String factoryName = "edu.ku.brc.af.core.CollectionObjLSIDGenFactory"; //$NON-NLS-1$
    
    protected static CollectionObjLSIDGenFactory instance = null;
    
    /**
     * @return true if the Factory is ready to generate LSIDs. (Default is true).
     */
    public boolean isReady()
    {
        return true;
    }
    
    /**
     * @return the message when isReady is false.
     */
    public String getErrorMsg()
    {
        return null;
    }
    
    /**
     * Resets the Factory so it can be checked again with a call to isReady.
     */
    public void reset()
    {
        
    }
    
    /**
     * @param uriStr
     * @param institutionCode
     * @param collectionCode
     * @param catalogNumer
     * @return
     */
    public String getLSID(final String uriStr, 
                          final String institutionCode, 
                          final String collectionCode, 
                          final String catalogNumer)
    {
        return String.format("urn:lsid:%s:%s:%s:%s", uriStr, institutionCode, collectionCode, catalogNumer);
    }
    
    /**
     * Default implementation to be overridden for an internal implementation. 
     * Returns null if not overridden.
     * @param catalogNumer the catalog number
     * @return the LSID
     */
    public String getLSID(final String catalogNumer)
    {
        return null;
    }
    
    /**
     * Returns the instance of the CollectionObjLSIDGenFactory.
     * @return the instance of the CollectionObjLSIDGenFactory.
     */
    public static CollectionObjLSIDGenFactory getInstance()
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
                instance = (CollectionObjLSIDGenFactory)Class.forName(factoryNameStr).newInstance();
                return instance;
                 
            } catch (Exception e) 
            {
                InternalError error = new InternalError("Can't instantiate CollectionObjLSIDGenFactory factory " + factoryNameStr); //$NON-NLS-1$
                error.initCause(e);
                throw error;
            }
        }
        return null;
    }
}
