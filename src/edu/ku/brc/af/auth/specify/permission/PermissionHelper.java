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
/**
 * 
 */
package edu.ku.brc.af.auth.specify.permission;

import java.security.Permission;
import java.security.PrivilegedAction;

import javax.security.auth.Subject;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.datamodel.SpecifyUser;

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
        Subject subject = SpecifyUser.getCurrentSubject();

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
            allowed = false;
        }

        if (allowed)
        {
            log.debug("permission was granted");
        } 
        else
        {
            log.error("permission was denied" + myPerm.toString());
            System.exit(0);
        }
    }
}
