/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
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
    
    public static final int NO_PERM          =   0; // Indicates there are no permissions
    public static final int CAN_VIEW         =   1; // Indicates the user can view the form
    public static final int CAN_MODIFY       =   2; // Indicates the user can modify data
    public static final int CAN_DELETE       =   4; // Indicates the user can delete items
    public static final int CAN_ADD          =   8; // Indicates the user can add new items
    
    public static final int ALL_PERM         = CAN_VIEW | CAN_MODIFY | CAN_DELETE | CAN_ADD;
    
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
     * @return the embeddedSpecifyAppRootUser
     */
    public String getEmbeddedUserName()
    {
        return null;
    }

    /**
     * @return the embeddedSpecifyAppRootPwd
     */
    public String getEmbeddedPwd()
    {
        return null;
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
                InternalError error = new InternalError("Can't instantiate RecordSet factory " + factoryNameStr); //$NON-NLS-1$
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
     * @exception Exception - if the validation fails.
     */
    public boolean authenticateDB(String user, String pass, String driverClass, String url) throws Exception
    {
        return true;
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
    public PermissionBits getPermission(final String name)
    {
        return new PermissionBits(ALL_PERM);
    }

    /**
     * Returns a integer mask with the permission bits flipped accordingly
     * @param name the name of the permission
     * @return the options
     */
    public int getPermissionOptions(String name)
    {
        return ALL_PERM;
    }

    /**
     * Helper method to see if an option is turned on.
     * @param options the range of options that can be turned on
     * @param opt the actual option that may be turned on
     * @return true if the opt bit is on
     */
    public static boolean isOn(final int options, final int opt)
    {
        return (options & opt) == opt;
    }
    
    /**
     * @param options
     * @return
     */
    public static boolean canModify(final int options)
    {
        return isOn(options, CAN_MODIFY);
    }
    
    /**
     * @param options
     * @return
     */
    public static boolean canView(final int options)
    {
        return isOn(options, CAN_VIEW);
    }
    
    /**
     * @param options
     * @return
     */
    public static boolean canAdd(final int options)
    {
        return isOn(options, CAN_ADD);
    }
    
    /**
     * @param options
     * @return
     */
    public static boolean canDelete(final int options)
    {
        return isOn(options, CAN_DELETE);
    }
    
    /**
     * @param options
     */
    public static void dumpPermissions(final String title, final int options)
    {
        System.err.print(title + " - ");
        System.err.print("Modify: " + (canModify(options) ? "Y" : "N"));
        System.err.print("  View: "   + (canView(options)   ? "Y" : "N"));
        System.err.print("  Delete: " + (canDelete(options) ? "Y" : "N"));
        System.err.println("  Add: "  + (canAdd(options)    ? "Y" : "N"));
    }
    
    /**
     * Creates a PermissionBits object.
     * @param options the options
     * @return the obj
     */
    private PermissionBits createPermissionBitsInternal(final int options)
    {
        return new PermissionBits(options);
    }
    
    /**
     * Creates a PermissionBits object.
     * @param options the options
     * @return the obj
     */
    public static PermissionBits createPermissionBits(final int options)
    {
        return getInstance().createPermissionBitsInternal(options);
    }
    
    //------------------------------------------------------------
    // Simple Class for making Permissions easy.
    //------------------------------------------------------------
    public class PermissionBits 
    {
        private int permissions;
        
        public PermissionBits(final int permissions)
        {
            this.permissions = permissions;
        }
        
        public boolean canModify()
        {
            return isOn(permissions, CAN_MODIFY);
        }
        
        public boolean canView()
        {
            return isOn(permissions, CAN_VIEW);
        }

        public boolean canAdd()
        {
            return isOn(permissions, CAN_ADD);
        }

        public boolean canDelete()
        {
            return isOn(permissions, CAN_DELETE);
        }

        public int getOptions()
        {
            return permissions;
        }
    }
}


