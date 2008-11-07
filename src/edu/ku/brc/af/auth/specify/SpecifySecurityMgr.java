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

import java.io.File;
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

import edu.ku.brc.af.auth.JaasContext;
import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.auth.specify.permission.BasicSpPermission;
import edu.ku.brc.af.auth.specify.permission.PermissionService;
import edu.ku.brc.specify.datamodel.SpPrincipal;

/**
 * 
 * @code_status Alpha
 * 
 * @author megkumin
 * @author ricardo
 * @author rods
 *
 */
public class SpecifySecurityMgr extends SecurityMgr
{
    private static final Logger log = Logger.getLogger(SpecifySecurityMgr.class);
    
    protected boolean        domFound       = false;
    protected String         localFileName  = null;
    protected static boolean doingLocal     = false;
    
    /**
     * 
     */
    public SpecifySecurityMgr()
    {
        localFileName = "backstop" + File.separator + "security.xml";
    }
    
    /**
     * @param localFileName the localFileName to set
     */
    public static void setLocalFileName(String localFileName)
    {
        ((SpecifySecurityMgr)getInstance()).localFileName = localFileName;
    }

    /**
     * @return the localFileName
     */
    public static String getLocalFileName()
    {
        return ((SpecifySecurityMgr)getInstance()).localFileName;
    }

    /**
     * @param doLocal the doLocal to set
     */
    public static void setDoingLocal(boolean doLocal)
    {
        doingLocal = doLocal;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.SecurityMgr#authenticateDB(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean authenticateDB(final String user, 
                                  final String pass, 
                                  final String driverClass, 
                                  final String url,
                                  final String dbUserName,
                                  final String dbPwd) throws Exception
    {
        Connection conn          = null;        
        Statement  stmt          = null;
        boolean    passwordMatch = false;
        
        try
        {
            Class.forName(driverClass);
            
            conn = DriverManager.getConnection(url, dbUserName, dbPwd);
            
            String query = "SELECT * FROM specifyuser where name='" + user + "'"; //$NON-NLS-1$ //$NON-NLS-2$
            stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);
            String dbPassword = null;

            while (result.next())
            {
                if (!result.isFirst())
                {
                    throw new LoginException("authenticateDB - Ambiguous user (located more than once): " + user); //$NON-NLS-1$
                }                    
                dbPassword = result.getString(result.findColumn("Password")); //$NON-NLS-1$
            }

            /*if (dbPassword == null)
            {
                throw new LoginException("authenticateDB - Password for User " + user + " undefined."); //$NON-NLS-1$ //$NON-NLS-2$
            }*/
            if (pass != null && dbPassword != null && pass.equals(dbPassword))
            {
                passwordMatch = true;
            } 

            // else: passwords do NOT match, user will not be authenticated
        } 
        catch (java.lang.ClassNotFoundException e)
        {
            log.error("authenticateDB - Could not connect to database, driverclass - ClassNotFoundException: "); //$NON-NLS-1$
            log.error(e.getMessage());
            e.printStackTrace();
            throw new LoginException("authenticateDB -  Database driver class not found: " + driverClass); //$NON-NLS-1$
        }
        catch (SQLException ex)
        {
            log.error("authenticateDB - SQLException: " + ex.toString()); //$NON-NLS-1$
            log.error("authenticateDB - " + ex.getMessage()); //$NON-NLS-1$
            throw new LoginException("authenticateDB - SQLException: " + ex.getMessage()); //$NON-NLS-1$
        }
        finally
        {
            try
            {
                if (conn != null)  conn.close();
                if (stmt != null)  stmt.close(); 
            } catch (SQLException e)
            {
                log.error("Exception caught: " + e.toString()); //$NON-NLS-1$
                e.printStackTrace();
            }
        }
        return passwordMatch;
    }
    
    // XXX should be moved to PermissionService class
    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.SecurityMgr#grantPermission(javax.security.auth.Subject, java.security.Principal, java.security.Permission)
     */
    public void grantPermission(Subject currentSubject, Principal principalToMatchTo, Permission perm)
    {
        log.debug("grantPermission"); //$NON-NLS-1$
        if (currentSubject == null)
        {
            log.error("grantPermission - subject is null - cannot grant permission"); //$NON-NLS-1$
            return;
        }
        if (perm == null)
        {
            log.error("grantPermission - permission is null - cannot grant permission"); //$NON-NLS-1$
            return;
        }
        Set<Principal> p = currentSubject.getPrincipals();
        if (p == null)
        {
            log.error("grantPermission - there are no principals associated with this user - cannot grant permission"); //$NON-NLS-1$
            return;
        }
        
        Iterator<Principal> it = p.iterator();
        while (it.hasNext())
        {
            Principal principal = it.next();
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.SecurityMgr#checkPermission(java.lang.String, java.lang.String)
     */
    public boolean checkPermission(String name, String actions)
    {
        final Class<?> permissionClass = BasicSpPermission.class;
        return checkPermission(permissionClass, name, actions);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.SecurityMgr#getPermissionOptions(java.lang.String)
     */
    public int getPermissionOptions(final String name)
    {
        final Class<?> permissionClass = BasicSpPermission.class;
        return getPermissionOptions(permissionClass, name);
    }
    
    /**
     * Returns a Permission Object with the permission bits flipped accordingly
     * @param name the name of the permission
     * @return the options
     */
    public PermissionSettings getPermission(final String name)
    {
        return new PermissionSettings(getPermissionOptions(name));
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
    protected boolean checkPermission(final Class<?> permissionClass, final String name, final String actions)
    {
        if (!(BasicSpPermission.class.isAssignableFrom(permissionClass)))
        {
            throw new SecurityException(permissionClass.getName() + " class is not part of Specify permission hierarchy."); //$NON-NLS-1$
        }

        Constructor<?> constructor = null;
        BasicSpPermission perm = null;
        try
        {
            constructor = permissionClass.getConstructor(String.class, String.class);
            perm        = (BasicSpPermission) constructor.newInstance(name, actions);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        
        return checkPermission(perm);
    }
    
    /**
     * @param permissionClass
     * @param name
     * @return
     */
    protected int getPermissionOptions(final Class<?> permissionClass, final String name)
    {
        if (!(BasicSpPermission.class.isAssignableFrom(permissionClass)))
        {
            throw new SecurityException(permissionClass.getName() + " class is not part of Specify permission hierarchy."); //$NON-NLS-1$
        }

        int options = PermissionSettings.NO_PERM;
        
        Constructor<?> constructor = null;
        try
        {
            constructor = permissionClass.getConstructor(String.class, String.class);
            
            log.debug("******************* Can View: "+name+" - "+ checkPermission((BasicSpPermission)constructor.newInstance(name, VIEW_PERM)));
            log.debug("******************* Can Mod : "+name+" - "+ checkPermission((BasicSpPermission)constructor.newInstance(name, MODIFY_PERM)));
            log.debug("******************* Can Del : "+name+" - "+ checkPermission((BasicSpPermission)constructor.newInstance(name, DELETE_PERM)));
            log.debug("******************* Can Add : "+name+" - "+ checkPermission((BasicSpPermission)constructor.newInstance(name, ADD_PERM)));
            
            options |= checkPermission((BasicSpPermission)constructor.newInstance(name, MODIFY_PERM)) ? PermissionSettings.CAN_MODIFY : 0;
            options |= checkPermission((BasicSpPermission)constructor.newInstance(name, VIEW_PERM)) ?   PermissionSettings.CAN_VIEW : 0;
            options |= checkPermission((BasicSpPermission)constructor.newInstance(name, ADD_PERM)) ?    PermissionSettings.CAN_ADD : 0;
            options |= checkPermission((BasicSpPermission)constructor.newInstance(name, DELETE_PERM)) ? PermissionSettings.CAN_DELETE : 0;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        
        return options;
    }
    
    /**
     * Checks whether current user has the permission provided. 
     *  
     * @param myPerm Permission being tested 
     * @return Whether the user has the permission or not
     */
    public boolean checkPermission(final BasicSpPermission myPerm)
    {
        Subject currentSubject = JaasContext.getGlobalSubject();
        
        if ((currentSubject != null) && (myPerm != null))
        {
            return PermissionService.runCheckPermssion(currentSubject, myPerm);
        }
        // else
        log.error("doesCurrentUserHavePermission - either current subject or permission passed is null, should not happen"); //$NON-NLS-1$

        return false;
    }
    
}
