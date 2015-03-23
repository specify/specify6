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
package edu.ku.brc.af.auth.specify.policy;

import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.specify.permission.BasicSpPermission;
import edu.ku.brc.af.auth.specify.permission.PermissionService;
import edu.ku.brc.af.auth.specify.principal.AdminPrincipal;
import edu.ku.brc.af.auth.specify.principal.BasicPrincipal;

/**
 * @author megkumin
 *
 * @code_status Alpha
 *
 */
public class DatabasePolicy extends java.security.Policy
{
    protected static final Logger log   = Logger.getLogger(DatabasePolicy.class);
    
    /** Timestamp when the cache will expire */
    protected Date cacheExpirationTime;
    
    /** Cache of permissions per principal */
    protected Map<Principal, Permissions> cachedPermissions = new HashMap<Principal, Permissions>();
    
    /** Time in seconds to keep cache */
    final protected int cacheExpirationInSeconds = 1 * 60; 
    
    /**
     * Constructor.
     */
    public DatabasePolicy()
    {
    	// force cache to expire now (but so that cacheExpirationTime is not null)
    	cacheExpirationTime = new Date();
    }
    
    /**
     * Indicates whether the current cached permissions are still valid (fresh) or not.
     * Note that we should really cache permissions for each different ProtectionDomain
     * provided in method getPermissions, but we all know it will always be the same 
     * (the current user) for our custom policy.
     * 
     * @return Boolean indicating whether the current cached permissions are still valid (fresh) or not.
     */
    protected boolean isCacheValid(final Principal principal) {
    	// cache is invalid if it hasn't been set yet, or if expiration time is past
        boolean expired = cacheExpirationTime.before(new Date());
        if (expired)
        {
            // cache time expired, so clear all data in cache
            cachedPermissions.clear();
            // cache time expired, so it's invalid: return false
            return false;
        }

        // cache hasn't expired, so check if it has data for this principal 
        return (cachedPermissions.get(principal) != null);
    }
    
    /**
     * Resets the cache timer to the pre-defined amount of seconds
     */
    protected void resetCacheTimer() {
    	Calendar cal = new GregorianCalendar();
    	cal.setTime(new Date());
    	cal.add(Calendar.SECOND, cacheExpirationInSeconds);
    	cacheExpirationTime = cal.getTime();
    }
    
    /*
     * (non-Javadoc)
     * @see java.security.Policy#getPermissions(java.security.ProtectionDomain)
     */
    @SuppressWarnings("unchecked") //$NON-NLS-1$
    public PermissionCollection getPermissions(final Principal principal)
    {
        Permissions principalPermissions = cachedPermissions.get(principal); 
    	if (isCacheValid(principal) && principalPermissions != null) 
    	{
    		// cache is still valid, so there's no need to get permissions all over again
    	    // Note that we should really cache permissions for each different ProtectionDomain
    	    // provided in method getPermissions, but we all know it will always be the same 
    	    // (the current user) for our custom policy.
    		return principalPermissions;
    	}
    	
    	resetCacheTimer();
    	
    	principalPermissions = new Permissions();

    	if (!(principal instanceof BasicPrincipal))
    	{
    	    // It's not the kind of principal we care about
    	    // so just return an empty set
    	    // there's also no need to store this in cache
    	    return principalPermissions;
    	}
    	
	    BasicPrincipal basicPrincipal = (BasicPrincipal) principal; 
	    List<Permission> perms = PermissionService.findPrincipalBasedPermissions(basicPrincipal.getId());
	    if (perms != null)
	    {
    	    for (Permission perm : perms)
    	    {
    	        principalPermissions.add(perm);
    	    }
	    }

	    // store principal permissions in cache for later
    	cachedPermissions.put(principal, principalPermissions);
    	// and also return them
    	return principalPermissions;
    }

    /* (non-Javadoc)
     * @see java.security.Policy#getPermissions(java.security.CodeSource)
     * 
     * Note: no permissions are granted to code running without a principal in Specify
     */
    @Override
    public PermissionCollection getPermissions(CodeSource arg0)
    {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see java.security.Policy#implies(java.security.ProtectionDomain, java.security.Permission)
     */
    public boolean implies(final ProtectionDomain domain, final Permission permission)
    {
    	if (!(permission instanceof BasicSpPermission))
    	{
    		// built-in Java permissions (file, socket, etc) pass through this method 
    		// i.e., if this is not a permission we care about, then it means that according to this policy, 
    		// the user has permission to do it regardless what the permission is
    		
    		// if we end up using JAAS for plug-in security, we need to exclude non-brc code from 
    		// the pass-through 
    		return true;
    	}
    	
    	for (Principal principal : domain.getPrincipals())
    	{
    		// not using instanceof operator to avoid matching against subclasses of admin principal class 
    		if (principal.getClass().equals(AdminPrincipal.class))
    		{
            	// administrator has all permissions by default
    			return true;
    		}
    		else
    		{
    		    // check if we can imply the permission from the principal permissions  
    	        PermissionCollection perms = getPermissions(principal);
    	        if (perms.implies(permission)) {
    	            return true;
    	        }
    		}
    	}

    	// permission can't be implied from permissions of all principals 
        return false;
    }

    /**
     * @param principals
     * @return
     */
    @SuppressWarnings("unused") //$NON-NLS-1$
    private String toString(final Principal[] principals)
    {
        if (principals == null || principals.length == 0) { return "DatabasePolicy <empty principals>"; } //$NON-NLS-1$
        StringBuffer buf = new StringBuffer();
        buf.append("DatabasePolicy <"); //$NON-NLS-1$
        for (int i = 0; i < principals.length; i++)
        {
            Principal p = principals[i];
            buf.append("(class="); //$NON-NLS-1$
            buf.append(p.getClass().getName());
            buf.append(", name="); //$NON-NLS-1$
            buf.append(p.getName());
            buf.append(")"); //$NON-NLS-1$
            if (i < principals.length - 1)
            {
                buf.append(", "); //$NON-NLS-1$
            }
        }
        buf.append(">"); //$NON-NLS-1$
        return buf.toString();
    }

	/* (non-Javadoc)
	 * @see java.security.Policy#refresh()
	 * 
	 * Note: force permission cache expiration
	 */
	@Override
	public void refresh()
	{
		// sets cache to expire now
		cachedPermissions = null;
	}
}
