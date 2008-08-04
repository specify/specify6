package edu.ku.brc.af.auth.specify.policy;

import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author megkumin
 *
 * @code_status Alpha
 *
 * Created Date: Aug 16, 2007
 *
 */
public class CompositePolicy extends Policy
{
    protected static final Logger log = Logger.getLogger(CompositePolicy.class);
    private List<Policy> policies =  new ArrayList<Policy>();
    
    /**
     * @param policies
     */
    public CompositePolicy(final List<Policy> policies)
    {
        this.policies = policies;
    }

    /* (non-Javadoc)
     * @see java.security.Policy#getPermissions(java.security.ProtectionDomain)
     */
    public PermissionCollection getPermissions(final ProtectionDomain domain)
    {
        Permissions perms = new Permissions();
        for (Iterator<Policy> itr = policies.iterator(); itr.hasNext();)
        {
            Policy               policy  = itr.next();
            PermissionCollection permCol = policy.getPermissions(domain);
            for (Enumeration<Permission> en = permCol.elements(); en.hasMoreElements();)
            {
                perms.add(en.nextElement());
            }
        }
        return perms;
    }

    /* (non-Javadoc)
     * @see java.security.Policy#implies(java.security.ProtectionDomain, java.security.Permission)
     */
    public boolean implies(final ProtectionDomain domain, final Permission permission)
    {
        
       //log.debug("ProtectionDomain=" + JaasStringHelper.principalsToString(domain));
       //log.debug("Permission=" + permission);
        for (Iterator<Policy> itr = policies.iterator(); itr.hasNext();)
        {
            Policy p = itr.next();
            if (p.implies(domain, permission)) { return true; }
        }

        return false;
    }

    /* (non-Javadoc)
     * @see java.security.Policy#getPermissions(java.security.CodeSource)
     */
    public PermissionCollection getPermissions(CodeSource codesource)
    {
        Permissions perms = new Permissions();
        for (Iterator<Policy> itr = policies.iterator(); itr.hasNext();)
        {
            Policy p = itr.next();
            PermissionCollection permsCol = p.getPermissions(codesource);
            for (Enumeration<Permission> en = permsCol.elements(); en.hasMoreElements();)
            {
                perms.add(en.nextElement());
            }
        }
        return perms;
    }

    /* (non-Javadoc)
     * @see java.security.Policy#refresh()
     */
    public void refresh()
    {
        for (Iterator<Policy> itr = policies.iterator(); itr.hasNext();)
        {
            Policy p = itr.next();
            p.refresh();
        }
    }


}