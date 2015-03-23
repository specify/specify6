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
        
       //log.debug("ProtectionDomain=" + domain.toString());//JaasStringHelper.principalsToString(domain));
       log.debug("Permission=" + permission);
        for (Iterator<Policy> itr = policies.iterator(); itr.hasNext();)
        {
            Policy p = itr.next();
            if (p.implies(domain, permission)) 
            { 
                return true; 
            }
        }

        return false;
    }

    /* (non-Javadoc)
     * @see java.security.Policy#getPermissions(java.security.CodeSource)
     */
    public PermissionCollection getPermissions(final CodeSource codesource)
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
