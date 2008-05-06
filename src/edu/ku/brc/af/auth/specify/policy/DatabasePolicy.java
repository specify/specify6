/**
 * 
 */
package edu.ku.brc.af.auth.specify.policy;

import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.specify.permission.BasicSpPermission;
import edu.ku.brc.af.auth.specify.permission.PermissionService;
import edu.ku.brc.af.auth.specify.principal.AdminPrincipal;
import edu.ku.brc.af.auth.specify.principal.BasicPrincipal;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author megkumin
 *
 * @code_status Alpha
 *
 */
public class DatabasePolicy extends java.security.Policy
{
    protected static final Logger log   = Logger.getLogger(DatabasePolicy.class);
    
    /*
     * (non-Javadoc)
     * @see java.security.Policy#getPermissions(java.security.ProtectionDomain)
     */
    @SuppressWarnings("unchecked") //$NON-NLS-1$
    public PermissionCollection getPermissions(final ProtectionDomain domain)
    {
    	// TODO: Cache permissions by principals to avoid retrieving them every time
        final Permissions permissions = new Permissions();
        Principal[] principals = domain.getPrincipals();
        if (principals != null && principals.length > 0)
        {
            for (Principal principal : principals)
            {
            	if (!(principal instanceof BasicPrincipal))
            		continue;
            	
            	BasicPrincipal basicPrincipal = (BasicPrincipal) principal; 
            	List perms = PermissionService.findPrincipalBasedPermissions(basicPrincipal.getId());
                for (Iterator itr = perms.iterator(); itr.hasNext();)
                {
                	Permission perm = (Permission)itr.next();
                	permissions.add(perm);
                }
            }
        } 
        return permissions;
    }

    /* (non-Javadoc)
     * @see java.security.Policy#getPermissions(java.security.CodeSource)
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
    	}
    	
        PermissionCollection perms = getPermissions(domain);
        return perms.implies(permission);
    }

    /**
     * @param principals
     * @return
     */
    @SuppressWarnings("unused") //$NON-NLS-1$
    private String toString(Principal[] principals)
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
	 */
	@Override
	public void refresh()
	{
		// TODO Auto-generated method stub
		
	}
}
