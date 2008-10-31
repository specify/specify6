/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.security;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.ImageIcon;

import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.auth.SecurityOptionIFace;
import edu.ku.brc.af.auth.specify.permission.BasicSpPermission;
import edu.ku.brc.af.core.PermissionIFace;
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
     * Returns a special permission that allows user to see all tasks
     * @return
     */
    protected PermissionEditorRowIFace getStarPermission(final String permissionBaseName,
                                                         final String type,
                                                         final String title,
                                                         final String description,
                                                         final Hashtable<String, SpPermission> existingPerms,
                                                         final Hashtable<String, SpPermission> overrulingPerms)
    {
        String       permName = permissionBaseName + ".*";
        SpPermission perm     = existingPerms.get(permName);
        SpPermission oPerm    = (overrulingPerms != null)? overrulingPerms.get(permName) : null;

        if (perm == null)
        {
            // no permission with this name, create new one
            perm = new SpPermission();
            perm.initialize();
            perm.setName(permName);
            perm.setActions("");
            perm.setPermissionClass(BasicSpPermission.class.getCanonicalName());
        }
        return new GeneralPermissionEditorRow(perm, oPerm, type, title, description, null, null);
    }
    
    /**
     * @param perms
     * @param securityOption
     * @param icon
     * @param principal
     * @param existingPerms
     * @param overrulingPerms
     */
    protected void checkAndAddPermission(final List<PermissionEditorRowIFace>  perms,
                                         final SecurityOptionIFace             securityOption,
                                         final ImageIcon                       icon,
                                         final SpPrincipal                     principal, 
                                         final Hashtable<String, SpPermission> existingPerms,
                                         final Hashtable<String, SpPermission> overrulingPerms,
                                         final String                          userType)
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
            
            if (userType != null)
            {
                PermissionIFace defPerms = securityOption.getDefaultPermissions(userType);
                if (defPerms != null)
                {
                    perm.setActions(defPerms.canView(), defPerms.canAdd(), defPerms.canModify(), defPerms.canDelete());
                }
            }
        }
        
        String desc = UIRegistry.getLocalizedMessage(descKey, securityOption.getShortDesc());

        PermissionEditorIFace editPanel = null;
        editPanel = securityOption.getPermEditorPanel();
        
        // add newly created permission to the bag that will be returned
        perms.add(new GeneralPermissionEditorRow(perm, oPerm, permBaseName, securityOption.getPermissionTitle(), 
                                                 desc, icon, editPanel));
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

        if (list != null)
        {
            List<PermissionEditorRowIFace> perms = new ArrayList<PermissionEditorRowIFace>(list.size());
            
            for (SecurityOptionIFace securityOption : list)
            {
                ImageIcon taskIcon = securityOption.getIcon(Taskable.StdIcon20);
                
                checkAndAddPermission(perms, securityOption, taskIcon, principal, existingPerms, overrulingPerms, userType);
                
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
                        checkAndAddPermission(perms, sOpt, icon, principal, existingPerms, overrulingPerms, userType);
                    }
                }
            }
            return perms;
        }
        return null;
    }
    
    /**
     * @return
     */
    protected abstract List<SecurityOptionIFace> getSecurityOptions();
}
