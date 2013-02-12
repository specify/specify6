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

import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.Journal;
import edu.ku.brc.specify.datamodel.LithoStrat;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.ReferenceWork;
import edu.ku.brc.specify.datamodel.Taxon;

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
    
    public enum CATEGORY_TYPE {Attachment, Specimen, Taxonomy, Geography, CollectingEvent, LithoStrat, 
                              Locality, Person, ReferenceWork, Journal, GeologicTimePeriod}
    
    protected static GenericGUIDGeneratorFactory instance = null;
    
    /**
     * 
     */
    public GenericGUIDGeneratorFactory()
    {
        super();
    }

    /**
     * @return true if the Factory is ready to generate GUIDs. (Default is true).
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
     * Generic GBIF GUID.
     * 
     * @param GUIDAuthority
     * @param institutionCode
     * @param collectionCode
     * @param catalogNumer
     * @return
     */
    public String createGUID(final String GUIDAuthority, 
                             final String institutionCode, 
                             final String collectionCode, 
                             final String catalogNumer)
    {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
    
    /**
     * Default implementation to be overridden for an internal implementation. 
     * Returns null if not overridden.
     * @param category the GUID category
     * @param id the unique identifier
     * @return the GUID
     */
    public String createGUID(final CATEGORY_TYPE category, final String id)
    {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
    
    /**
     * This sets a GUID on a data object
     * @param data the data object
     * @param doVersioning whether to add the versioning to the end
     * @param formatter the field formatter if it has or needs one.
     * @return the GUID it assigned
     */
    public String setGUIDOnId(final FormDataObjIFace      data,
                              final boolean               doVersioning,
                              final UIFieldFormatterIFace formatter)
    {
        return null;
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
     * Default implementation to be overridden for an internal implementation. 
     * Returns null if not overridden.
     * @param category the GUID category
     * @param id the unique identifier
     * @param version the version
     * @return the GUID
     */
//    public String createGUID(final CATEGORY_TYPE category, final String id, final int version)
//    {
//        return null;
//    }
    
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
