/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.dbsupport;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Mar 2, 2007
 *
 */
public abstract class CustomQueryFactory
{
    public static final String factoryName = "edu.ku.brc.dbsupport.CustomQueryFactory"; //$NON-NLS-1$
    
    protected static CustomQueryFactory instance = null;
    
    /**
     * Returns a custom query by name.
     * @param queryName the name of the query
     * @return the CustomQuery
     */
    public abstract CustomQueryIFace getQuery(final String queryName);
    
    
    /**
     * Returns the instance of the CustomQueryFactory.
     * @return the instance of the CustomQueryFactory.
     */
    public static CustomQueryFactory getInstance()
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
                instance = (CustomQueryFactory)Class.forName(factoryNameStr).newInstance();
                return instance;
                 
            } catch (Exception e) 
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(CustomQueryFactory.class, e);
                InternalError error = new InternalError("Can't instantiate CustomQueryFactory factory " + factoryNameStr); //$NON-NLS-1$
                error.initCause(e);
                throw error;
            }
        }
        return null;
    }

}
