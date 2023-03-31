/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.af.auth;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.security.AccessController;
import java.security.Permission;
import java.security.Principal;

import javax.security.auth.Subject;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jul 31, 2008
 *
 */
public class SecurityMgr
{
    public static final String factoryName = "edu.ku.brc.af.auth.SecurityMgr"; //$NON-NLS-1$
    
    public static final String VIEW_PERM     = "view";
    public static final String MODIFY_PERM   = "modify";
    public static final String DELETE_PERM   = "delete";
    public static final String ADD_PERM      = "add";
    
    //private static final Logger log = Logger.getLogger(SecurityMgr.class);
    
    protected static SecurityMgr instance = null;
    
    /**
     * Protected Constructor
     */
    protected SecurityMgr()
    {
        
    }

    /**
     * Returns the instance to the singleton
     * @return  the instance to the singleton
     */
    public static SecurityMgr getInstance()
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
                return instance = (SecurityMgr)Class.forName(factoryNameStr).newInstance();
                 
            } catch (Exception e) 
            {
                e.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SecurityMgr.class, e);
                InternalError error = new InternalError("Can't instantiate SecurityMgr factory " + factoryNameStr); //$NON-NLS-1$
                error.initCause(e);
                throw error;
            }
        }
        return null;
    }
   
    
    /** 
     * 
     * Validates the given user and password against the JDBC datasource (using JDBC directly, not hibernate).
     * 
     * @see edu.ku.brc.af.auth.specify.SecurityMgr#authenticate()
     * 
     * @param user - the username to be authenticated.
     * @param pass - the password to be authenticated.
     * @param driverClass - the driver to be used during authentication.
     * @param url - the url of the jdbc datasource.
     * @param dbUserName
     * @param dbPwd
     * @exception Exception - if the validation fails.
     */
    public boolean authenticateDB(String user, 
                                  String pass, 
                                  String driverClass, 
                                  String url,
                                  String dbUserName,
                                  String dbPwd) throws Exception
    {
        return false;
    }
    
    
    // XXX should be moved to PermissionService class
    /**
     * @param currentSubject
     * @param principalToMatchTo
     * @param perm
     */
    public void grantPermission(Subject currentSubject, Principal principalToMatchTo, Permission perm)
    {
        
    }
    
    /**
     * Checks whether current user has the permission defined by BasicSpPermission 
     * and the name and actions provided.
     *  
     * @param name Type and target of permission 
     * @param actions Actions (any combination of view, add, modify, delete actions) 
     * @return Whether the user has the permission or not
     */
    public boolean checkPermission(String name, String actions)
    {
        return true;
    }
    
    /**
     * Returns a Permission Object with the permission bits flipped accordingly
     * @param name the name of the permission
     * @return the options
     */
    public PermissionSettings getPermission(final String name)
    {
        return new PermissionSettings(PermissionSettings.ALL_PERM);
    }

}


