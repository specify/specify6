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
package edu.ku.brc.exceptions;

import java.security.AccessController;
import java.security.PrivilegedAction;

import edu.ku.brc.ui.FeedBackSender;
import edu.ku.brc.ui.UIRegistry;


/**
 * Used to tracker and send Handled Exceptions.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 10, 2009
 *
 */
public class ExceptionTracker extends FeedBackSender
{
    public static final String factoryName = "edu.ku.brc.exceptions.ExceptionTracker"; //$NON-NLS-1$

    protected static ExceptionTracker instance = null;
    
    /**
     * 
     */
    public ExceptionTracker()
    {
        
    }
    
    /**
     * @return the url that the info should be sent to
     */
    protected String getSenderURL()
    {
        return UIRegistry.getResourceString("CGI_BASE_URL") + "/exception.php";
    }
    
    /**
     * Returns the instance of the ExceptionTracker.
     * @return the instance of the ExceptionTracker.
     */
    public static ExceptionTracker getInstance()
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
                instance = (ExceptionTracker)Class.forName(factoryNameStr).newInstance();
                return instance;
                 
            } catch (Exception e) 
            {
                InternalError error = new InternalError("Can't instantiate ExceptionTracker factory " + factoryNameStr); //$NON-NLS-1$
                error.initCause(e);
                throw error;
            }
        }
        return instance = new ExceptionTracker();
    }

}
