/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.af.core;

import java.security.AccessController;
import java.security.PrivilegedAction;

import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;

/**
 * Base Factory class (not Abstract) for generating LSID for Collection Objects. 
 *
 * @code_status Beta
 *
 * @author rods
 *
 */
public class GenericLSIDGeneratorFactory
{
    public static final String factoryName = "edu.ku.brc.af.core.CollectionObjLSIDGenFactory"; //$NON-NLS-1$
    
    public enum CATEGORY_TYPE {Specimen, Taxonomy, Geography, LithoStrat, Locality, Person, ReferenceWork, Journal}
    
    protected static GenericLSIDGeneratorFactory instance = null;
    
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
     * Generic GBIF LSID.
     * 
     * @param lsidAuthority
     * @param institutionCode
     * @param collectionCode
     * @param catalogNumer
     * @return
     */
    public String createLSID(final String lsidAuthority, 
                             final String institutionCode, 
                             final String collectionCode, 
                             final String catalogNumer)
    {
        return String.format("urn:lsid:%s:%s:%s:%s", lsidAuthority, institutionCode, collectionCode, catalogNumer);
    }
    
    /**
     * Default implementation to be overridden for an internal implementation. 
     * Returns null if not overridden.
     * @param category the LSID category
     * @param id the unique identifier
     * @return the LSID
     */
    public String createLSID(final CATEGORY_TYPE category, final String id)
    {
        return null;
    }
    
    /**
     * This sets a LSID on a data object
     * @param data the data object
     * @param doVersioning whether to add the versioning to the end
     * @param formatter the field formatter if it has or needs one.
     * @return the LSID it assigned
     */
    public String setLSIDOnId(final FormDataObjIFace      data,
                              final boolean               doVersioning,
                              final UIFieldFormatterIFace formatter)
    {
        return null;
    }
    
    /**
     * This builds (fills in) all the empty GUID fields in any table that requires it.
     * This is usually for the the tables represented by the enum CATEGORY_TYPE.
     */
    public void buildLSIDs()
    {
        
    }
    
    /**
     * Default implementation to be overridden for an internal implementation. 
     * Returns null if not overridden.
     * @param category the LSID category
     * @param id the unique identifier
     * @param version the version
     * @return the LSID
     */
    public String createLSID(final CATEGORY_TYPE category, final String id, final int version)
    {
        return null;
    }
    
    /**
     * Returns the instance of the CollectionObjLSIDGenFactory.
     * @return the instance of the CollectionObjLSIDGenFactory.
     */
    public static GenericLSIDGeneratorFactory getInstance()
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
                instance = (GenericLSIDGeneratorFactory)Class.forName(factoryNameStr).newInstance();
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
