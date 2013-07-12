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
package edu.ku.brc.specify.tasks.subpane.security;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.ImageIcon;

import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.auth.SecurityOptionIFace;
import edu.ku.brc.af.auth.specify.permission.BasicSpPermission;
import edu.ku.brc.af.auth.specify.principal.UserPrincipalSQLService;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.ui.UIRegistry;

/**
 * Abstract class that defines the basic methods used to enumerate potential and existing permissions
 * of a principal. It's used to build the permission table used by instances of PermissionEditor class.  
 *  
 * @author Ricardo
 *
 */
public abstract class PermissionEnumerator 
{
    private String permBaseName = null;
    private String descKey      = null;
    
    /**
     * @param permBaseName
     * @param descKey
     */
    public PermissionEnumerator(String permBaseName, String descKey)
    {
        super();
        this.permBaseName = permBaseName;
        this.descKey      = descKey;
    }

    /**
     * Returns the permissions of a given principal
     * @param principal
     * @param overrulingPrincipal
     * @return
     */
    /*public List<PermissionEditorRowIFace> getPermissions(final SpPrincipal principal, 
                                                         final SpPrincipal overrulingPrincipal,
                                                         final String      userType)
    {
        Hashtable<String, SpPermission> existingPerms = PermissionService.getExistingPermissions(principal.getId());
        
        Hashtable<String, SpPermission> overrulingPerms = null;
        if (overrulingPrincipal != null)
        {
            overrulingPerms = PermissionService.getExistingPermissions(overrulingPrincipal.getId());
        }
        return getPermissions(principal, existingPerms, overrulingPerms, userType);
    }*/

    /**
     * @param perms
     * @param securityOption
     * @param icon
     * @param principal
     * @param existingPerms
     * @param overrulingPerms
     * @param userType
     * @param admin
     */
    protected void checkAndAddPermission(final List<PermissionEditorRowIFace>  perms,
                                         final SecurityOptionIFace             securityOption,
                                         final ImageIcon                       icon,
                                         final SpPrincipal                     principal, 
                                         final Hashtable<String, SpPermission> existingPerms,
                                         final Hashtable<String, SpPermission> overrulingPerms,
                                         final String                          userType,
                                         final boolean                         admin)
    {
        // first check if there is a permission with this name
        String       secName = permBaseName + "." + securityOption.getPermissionName();
        SpPermission perm    = existingPerms.get(secName);
        SpPermission oPerm   = (overrulingPerms != null)? overrulingPerms.get(secName) : null;

        if (perm == null)
        {
            perm = new SpPermission();
            perm.initialize();
            perm.setName(secName);
            perm.setActions("");
            perm.setPermissionClass(BasicSpPermission.class.getCanonicalName());
            
//            if (userType != null)
//            {
//                PermissionIFace defPerms = securityOption.getDefaultPermissions(userType);
//                if (defPerms != null)
//                {
//                    perm.setActions(defPerms.canView(), defPerms.canAdd(), defPerms.canModify(), defPerms.canDelete());
//                }
//            }
        }
        
        String desc = UIRegistry.getLocalizedMessage(descKey, securityOption.getShortDesc());

        PermissionEditorIFace editPanel = securityOption.getPermEditorPanel();
        
        // add newly created permission to the bag that will be returned
        perms.add(new GeneralPermissionEditorRow(perm, oPerm, permBaseName, securityOption.getPermissionTitle(), 
                                                 desc, icon, editPanel, admin));
    }
    
    /**
     * @param principal
     * @param existingPerms
     * @param overrulingPerms
     * @param userType
     * @return
     */
    public List<PermissionEditorRowIFace> getPermissions(final SpPrincipal                     principal, 
                                                         final Hashtable<String, SpPermission> existingPerms,
                                                         final Hashtable<String, SpPermission> overrulingPerms,
                                                         final String                          userType) 
    {
        List<SecurityOptionIFace> list = getSecurityOptions();

        if (list == null)
        {
            return null;
        }
        
        boolean admin = UserPrincipalSQLService.isPrincipalAdmin(principal.getId());

        List<PermissionEditorRowIFace> perms = new ArrayList<PermissionEditorRowIFace>(list.size());

        for (SecurityOptionIFace securityOption : list)
        {
            ImageIcon taskIcon = securityOption.getIcon(Taskable.StdIcon20);

            checkAndAddPermission(perms, securityOption, taskIcon, principal, existingPerms, overrulingPerms, userType, admin);

            List<SecurityOptionIFace> additionalOptions = securityOption.getAdditionalSecurityOptions();
            if (additionalOptions != null)
            {
                for (SecurityOptionIFace sOpt : additionalOptions)
                {
                    ImageIcon icon = sOpt.getIcon(Taskable.StdIcon20);
                    if (icon == null)
                    {
                        icon = taskIcon;
                    }
                    checkAndAddPermission(perms, sOpt, icon, principal, existingPerms, overrulingPerms, userType, admin);
                }
            }
        }

        return perms;
    }
    
    /**
     * @return
     */
    protected abstract List<SecurityOptionIFace> getSecurityOptions();
}
