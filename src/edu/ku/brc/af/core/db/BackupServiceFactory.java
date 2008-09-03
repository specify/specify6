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
/**
 * 
 */
package edu.ku.brc.af.core.db;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.beans.PropertyChangeListener;
import java.security.AccessController;

import edu.ku.brc.af.core.Taskable;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 2, 2008
 *
 */
public abstract class BackupServiceFactory
{
    public static final String factoryName = "edu.ku.brc.af.core.db.BackupServiceFactory"; //$NON-NLS-1$
    
    
    //private static final Logger log = Logger.getLogger(SecurityMgr.class);
    
    protected static BackupServiceFactory instance = null;
    
    /**
     * Protected Constructor
     */
    protected BackupServiceFactory()
    {
        
    }
    
    /**
     * Returns the instance to the singleton
     * @return  the instance to the singleton
     */
    public static BackupServiceFactory getInstance()
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
                return instance = (BackupServiceFactory)Class.forName(factoryNameStr).newInstance();
                 
            } catch (Exception e) 
            {
                InternalError error = new InternalError("Can't instantiate RecordSet factory " + factoryNameStr); //$NON-NLS-1$
                error.initCause(e);
                throw error;
            }
        }
        return null;
    }
    
    public abstract int getNumberofTables();
   
    /**
     * @return
     */
    public abstract void doBackUp(PropertyChangeListener listener);

    /**
     * @return
     */
    public abstract void doRestore(PropertyChangeListener listener);
    
}


