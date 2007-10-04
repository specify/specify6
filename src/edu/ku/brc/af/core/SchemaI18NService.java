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
import java.util.Locale;

/**
 *
 * @code_status Beta
 *
 * Created Date: Oct 3, 2007
 *
 */
public abstract class SchemaI18NService
{
    public static final String factoryName = "edu.ku.brc.af.core.SchemaI18NService";
    
    protected static SchemaI18NService instance = null;
    
    public abstract void loadWithLocale(Locale locale);
    
    /**
     * Returns the instance of the AppContextMgr.
     * @return the instance of the AppContextMgr.
     */
    public static SchemaI18NService getInstance()
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
                instance = (SchemaI18NService)Class.forName(factoryNameStr).newInstance();
                return instance;
                 
            } catch (Exception e) 
            {
                InternalError error = new InternalError("Can't instantiate AppContextMgr factory " + factoryNameStr);
                error.initCause(e);
                throw error;
            }
        }
        return null;
    }

}
