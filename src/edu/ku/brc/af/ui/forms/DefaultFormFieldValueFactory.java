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
package edu.ku.brc.af.ui.forms;

import java.security.AccessController;
import java.security.PrivilegedAction;

import edu.ku.brc.af.core.db.DBFieldInfo;

/**
 * A factory for serving up default value for a data member in a data object.
 * 
 * @author rod
 *
 * @code_status Complete
 *
 * Feb 12, 2008
 *
 */
public abstract class DefaultFormFieldValueFactory
{
    public static final String factoryName = "edu.ku.brc.ui.forms.DefaultFormFieldValueFactory";
    
    protected static DefaultFormFieldValueFactory instance = null;
    
    /**
     * Returns a default value for a field.
     * 
     * @param fieldInfo the field infomation.
     * @return the default value
     */
    public abstract Object getValueFor(final DBFieldInfo fieldInfo);
    
    /**
     * Returns the instance of the DefaultFormFieldValueFactory.
     * @return the instance of the DefaultFormFieldValueFactory.
     */
    public static DefaultFormFieldValueFactory getInstance()
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
                instance = (DefaultFormFieldValueFactory)Class.forName(factoryNameStr).newInstance();
                return instance;
                 
            } catch (Exception e) 
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DefaultFormFieldValueFactory.class, e);
                InternalError error = new InternalError("Can't instantiate DefaultFormFieldValueFactory factory " + factoryNameStr);
                error.initCause(e);
                throw error;
            }
        }
        return null;
    }

}
