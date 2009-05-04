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

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.security.AccessController;

import edu.ku.brc.dbsupport.RecordSetIFace;

/**
 * A factory for creating RecordSet objects.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Jul 14, 2008
 *
 */
public class RecordSetFactory
{
    public static final String factoryName = "RecordSetFactory"; //$NON-NLS-1$
    
    //private static final Logger log = Logger.getLogger(RecordSetFactory.class);
    
    protected static RecordSetFactory instance = null;
    
    /**
     * Protected Constructor
     */
    protected RecordSetFactory()
    {
    }

    /**
     * Returns the instance to the singleton
     * @return  the instance to the singleton
     */
    public static RecordSetFactory getInstance()
    {
        if (instance != null)
        {
            return instance;
        }
        
        // else
        String factoryNameStr = AccessController.doPrivileged(new java.security.PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(factoryName);
                    }
                });
            
        if (isNotEmpty(factoryNameStr)) 
        {
            try 
            {
                return instance = (RecordSetFactory)Class.forName(factoryNameStr).newInstance();
                 
            } catch (Exception e) 
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(RecordSetFactory.class, e);
                InternalError error = new InternalError("Can't instantiate RecordSet factory " + factoryNameStr); //$NON-NLS-1$
                error.initCause(e);
                throw error;
            }
        }
        return null;
    }
    
    /**
     * Creates a RecordSet object from a factory in the domain of the application.
     * @return a recordset object
     */
    public RecordSetIFace createRecordSet()
    {
        throw new RuntimeException("Must create your own implementation.");
    }
    
    /**
     * Creates a RecordSet object from a factory in the domain of the application.
     * @param name the name (title) of the record set
     * @param dbTableId the Table Id of the data it represents
     * @param type the type (Global, etc)
     * @return a recordset object
     */
    public RecordSetIFace createRecordSet(final String name, final int dbTableId, final Byte type)
    {
        throw new RuntimeException("Must create your own implementation.");
    }
}

