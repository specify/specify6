/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.af.auth.specify.permission;

import java.security.Permission;
import java.security.PrivilegedAction;

import javax.security.auth.Subject;

import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.JaasContext;

/**
 * @author megkumin
 *
 * @code_status Alpha
 *
 */
public class PermissionHelper
{
    private static final Logger          log            = Logger.getLogger(PermissionHelper.class);
    
    /**
     * 
     */
    public PermissionHelper()
    {
        // TODO Auto-generated constructor stub
    }

    public static void invokeSubjectPermCall(final Permission myPerm)
    {
        Subject subject = JaasContext.getGlobalSubject();

        boolean allowed = true;
        try
        {
            Subject.doAsPrivileged(subject, new PrivilegedAction<Object>()
            {

                public Object run()
                {
                    final SecurityManager sm = System.getSecurityManager();
                    if (sm != null)
                    {
                        sm.checkPermission(myPerm);
                    }
                    return null;
                }

            }, null);
        } catch (SecurityException e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PermissionHelper.class, e);
            allowed = false;
        }

        if (allowed)
        {
            log.debug("permission was granted"); //$NON-NLS-1$
        } 
        else
        {
            log.error("permission was denied" + myPerm.toString()); //$NON-NLS-1$
            System.exit(0);
        }
    }
}
