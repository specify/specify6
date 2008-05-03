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
package edu.ku.brc.af.auth.specify;

import java.lang.reflect.Constructor;
import java.security.Permission;
import java.security.Principal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.specify.permission.BasicSpPermission;
import edu.ku.brc.af.auth.specify.permission.PermissionService;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.datamodel.SpecifyUser;

/**
 * 
 * @code_status Unknown
 * 
 * @author megkumin
 *
 */
public class SpecifySecurityMgr
{
    private static final Logger  log = Logger.getLogger(SpecifySecurityMgr.class);

    protected static final SpecifySecurityMgr instance = new SpecifySecurityMgr();
    
    // XXX TODO SECURITY- make secure Specify Admin user and pwd
    public static final String embeddedSpecifyAppRootUser = "rods";
    public static final String embeddedSpecifyAppRootPwd = "rods";

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
     * @exception Exception - if the validation fails.
     */
    public static boolean authenticateDB(String user, String pass, String driverClass, String url) throws Exception
    {
        Connection conn = null;        
        Statement stmt = null;
        boolean passwordMatch = false;
        
        try
        {
            Class.forName(driverClass);
            conn = DriverManager.getConnection(url, embeddedSpecifyAppRootUser, embeddedSpecifyAppRootPwd);
            String query = "SELECT * FROM specifyuser where name='" + user + "'";
            stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);
            String dbPassword = null;

            while (result.next())
            {
                if (!result.isFirst())
                {
                    throw new LoginException("authenticateDB - Ambiguous user (located more than once): " + user);
                }                    
                dbPassword = result.getString(result.findColumn("Password"));
            }

            if (dbPassword == null)
            {
                throw new LoginException("authenticateDB - Password for User " + user + " undefined.");
            }
            if (pass != null && pass.equals(dbPassword))
            {
                passwordMatch = true;
            } 

            // else: passwords do NOT match, user will not be authenticated
        } 
        catch (java.lang.ClassNotFoundException e)
        {
            log.error("authenticateDB - Could not connect to database, driverclass - ClassNotFoundException: ");
            log.error(e.getMessage());
            e.printStackTrace();
            throw new LoginException("authenticateDB -  Database driver class not found: " + driverClass);
        }
        catch (SQLException ex)
        {
            log.error("authenticateDB - SQLException: " + ex.toString());
            log.error("authenticateDB - " + ex.getMessage());
            throw new LoginException("authenticateDB - SQLException: " + ex.getMessage());
        }
        finally
        {
            try
            {
                if (conn != null)  conn.close();
                if (stmt != null)  stmt.close(); 
            } catch (SQLException e)
            {
                log.error("Exception caught: " + e.toString());
                e.printStackTrace();
            }
        }
        return passwordMatch;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.specify.AppContextMgr#getInstance()
     */
    public static SpecifySecurityMgr getInstance()
    {
        return instance;
    }
    
    // XXX should be moved to PermissionService class
    public static void grantPermission(Subject currentSubject, Principal principalToMatchTo, Permission perm)
    {
        log.debug("grantPermission");
        if (currentSubject == null)
        {
            log.error("grantPermission - subject is null - cannot grant permission");
            return;
        }
        if (perm == null)
        {
            log.error("grantPermission - permission is null - cannot grant permission");
            return;
        }
        Set<Principal> p = currentSubject.getPrincipals();
        if (p == null)
        {
            log
                    .error("grantPermission - there are no principals associated with this user - cannot grant permission");
            return;
        }
        
        Iterator<Principal> it = p.iterator();
        while (it.hasNext())
        {
            Principal principal = (Principal)it.next();
            String principalClassName = principal.getClass().getCanonicalName();
            if (principalClassName.equals(SpPrincipal.class.getCanonicalName()))
            {
                SpPrincipal spp = (SpPrincipal)principal;
                String principalType = spp.getGroupSubClass();
                String principalName = spp.getName();
                SpPrincipal mySpPrincipal = PermissionService.getSpPrincipalByName(principalName);
                if (principalToMatchTo == null)
                {
                    PermissionService.giveSpPrincipalPermission(mySpPrincipal, perm);
                } else if (principalType.equals(principalToMatchTo.getClass().getCanonicalName()))
                {
                    PermissionService.giveSpPrincipalPermission(mySpPrincipal, perm);
                }
            }
        }
    }
    
    /**
     * Checks whether current user has the permission defined by BasicSpPermission 
     * and the name and actions provided.
     *  
     * @param name Type and target of permission 
     * @param actions Actions (any combination of view, add, modify, delete actions) 
     * @return Whether the user has the permission or not
     */
    public static boolean checkPermission(String name, String actions)
    {
    	final Class<?> permissionClass = BasicSpPermission.class;
    	return checkPermission(permissionClass, name, actions);
    }

    /**
     * Checks whether current user has the permission defined by the permission class, 
     * the name and actions provided. This method is protected because there is only
     * one class of permissions being used so far: BasicSpPermission. When new permissions
     * classes are created, this method can be made public
     *  
     * @param permissionClass Class of the permission being tested 
     * @param name Type and target of permission 
     * @param actions Actions (any combination of view, add, modify, delete actions) 
     * @return Whether the user has the permission or not
     */
    protected static boolean checkPermission(final Class<?> permissionClass, String name, String actions)
    {
    	if (!(BasicSpPermission.class.isAssignableFrom(permissionClass)))
    		throw new SecurityException(permissionClass.getName() + " class is not part of Specify permission hierarchy.");

    	Constructor<?> constructor = null;
    	BasicSpPermission perm = null;
    	try
    	{
    		constructor = permissionClass.getConstructor(String.class, String.class);
        	perm = (BasicSpPermission) constructor.newInstance(name, actions);
    	}
    	catch (Exception e)
    	{
    		throw new RuntimeException(e);
    	}
    	
    	
        return checkPermission(perm);
    }
    
    /**
     * Checks whether current user has the permission provided. 
     *  
     * @param myPerm Permission being tested 
     * @return Whether the user has the permission or not
     */
    public static boolean checkPermission(final BasicSpPermission myPerm)
    {
        Subject currentSubject = SpecifyUser.getCurrentSubject();
        
        if ((currentSubject != null) && (myPerm != null))
        {
            return PermissionService.runCheckPermssion(currentSubject, myPerm);
        }
        else
        {
            log.error("doesCurrentUserHavePermission - either current subject or permission passed is null, should not happen");
        }

        return false;
    }

}
