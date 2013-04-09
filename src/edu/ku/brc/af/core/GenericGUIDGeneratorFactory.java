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

import java.beans.PropertyChangeListener;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.UUID;

/**
 * Base Factory class (not Abstract) for generating GUID for Collection Objects. 
 *
 * @code_status Beta
 *
 * @author rods
 *
 */
public class GenericGUIDGeneratorFactory
{
    public static final String factoryName = "edu.ku.brc.af.core.CollectionObjGUIDGenFactory"; //$NON-NLS-1$
    
    public enum CATEGORY_TYPE {Attachment, Specimen, CollectingEvent, LithoStrat, 
                              Locality, Person, ReferenceWork, Journal, GeologicTimePeriod,
                              Collection, Institution, Determination, }
    // Note including Taxonomy, Geography,  at this time
    
    protected static GenericGUIDGeneratorFactory instance = null;
    
    /**
     * 
     */
    public GenericGUIDGeneratorFactory()
    {
        super();
    }

    /**
     * @return the message when isReady is false.
     */
    public String getErrorMsg()
    {
        return null;
    }
    
    /**
     * Default implementation to be overridden for an internal implementation. 
     * Returns null if not overridden.
     * @param category the GUID category
     * @param id the unique identifier
     * @return the GUID
     */
    public String createGUID()
    {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
    
    /**
     * This builds (fills in) all the empty GUID fields in any table that requires it.
     * This is usually for the the tables represented by the enum CATEGORY_TYPE.
     * @param pcl PropertyChangeListener for progress
     */
    public void buildGUIDs(PropertyChangeListener pcl)
    {
        
    }
    
    /**
     * Returns the instance of the CollectionObjGUIDGenFactory.
     * @return the instance of the CollectionObjGUIDGenFactory.
     */
    public static GenericGUIDGeneratorFactory getInstance()
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
                instance = (GenericGUIDGeneratorFactory)Class.forName(factoryNameStr).newInstance();
                return instance;
                 
            } catch (Exception e) 
            {
                InternalError error = new InternalError("Can't instantiate CollectionObjGUIDGenFactory factory " + factoryNameStr); //$NON-NLS-1$
                error.initCause(e);
                throw error;
            }
        }
        return null;
    }
}
