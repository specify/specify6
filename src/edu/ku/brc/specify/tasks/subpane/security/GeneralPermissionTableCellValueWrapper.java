/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.security;

import java.util.Iterator;

import edu.ku.brc.af.auth.specify.principal.GroupPrincipal;
import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;

/**
 * Wraps in a single object the values of a permission action (whether it's on
 * or off), its overruling permission value and the name of the principal the
 * overruling permission comes from. It's used on the table where permission
 * actions are viewed and edited, to provide a merged view of a user's
 * permissions.
 * 
 * @author Ricardo
 * 
 */
public class GeneralPermissionTableCellValueWrapper
{
    private boolean permissionActionValue;
    private boolean overrulingPermissionActionValue;
    private String  overrulingPermissionText;
    private boolean admin;

    /**
     * Constructor that uses permissions to extract field values.
     */
    public GeneralPermissionTableCellValueWrapper(
            final SpPermission permission,
            final SpPermission overrulingPermission,
            final String permissionAction,
            final boolean adminPrincipal)
    {
        this.admin = adminPrincipal;
        this.permissionActionValue = permission.hasAction(permissionAction);
        if (adminPrincipal)
        {
            // it's an admininstrator, so it has permissions to do anything
            overrulingPermissionActionValue = true;
            overrulingPermissionText = "Admin";
        }
        else if (overrulingPermission != null) 
        {
            this.overrulingPermissionActionValue = overrulingPermission.hasAction(permissionAction);
            // get name of first user group principal associated with this permission
            Iterator<SpPrincipal> it = overrulingPermission.getPrincipals().iterator();
            while (it.hasNext()) 
            {
                SpPrincipal principal = it.next();
                if (GroupPrincipal.class.getCanonicalName().equals(principal.getGroupSubClass())) 
                {
                    this.overrulingPermissionText = principal.getName();
                }
            }
        }
    }

    /**
     * Whether this permission action is on or off
     */
    public boolean getPermissionActionValue()
    {
        return permissionActionValue;
    }

    /**
     * See comments on property getter
     */
    public void setPermissionActionValue(boolean permissionActionValue)
    {
        this.permissionActionValue = permissionActionValue;
    }

    /**
     * Flag indicating the value of the overruling permission. That is, if the
     * permission is "shadowed" by another from the user's group, this flag
     * indicates whether the shadowing permission is on or off
     */
    public boolean getOverrulingPermissionActionValue()
    {
        return overrulingPermissionActionValue;
    }

    /**
     * See comments on property getter
     */
    public void setOverrulingPermissionActionValue(
            boolean overrulingPermissionActionValue)
    {
        this.overrulingPermissionActionValue = overrulingPermissionActionValue;
    }

    /**
     * What to display next to the permission checkbox to indicate that the
     * permission is overruled by another permission
     */
    public String getOverrulingPermissionText()
    {
        return overrulingPermissionText;
    }

    /**
     * See comments on property getter
     */
    public void setOverrulingPermissionText(String overrulingPermissionText)
    {
        this.overrulingPermissionText = overrulingPermissionText;
    }
    
    /**
     * Indicates whether the current permission is overriden by any other. 
     * @return flag indicating whether the current permission is overriden by any other
     */
    public boolean isOverriden() {
        return overrulingPermissionActionValue;
    }

    public boolean isAdmin()
    {
        return admin;
    }
    
    /**
     * Prepare an instance of our custom JCheckBox to be returned by the table cell renderer and editor.
     *  
     * @param customCheckbox
     */
    public void prepareComponent(GeneralPermissionTableCheckBox customCheckbox)
    {
        // finally, put itself inside the checkbox
        customCheckbox.setCellValue(this);

        if (isAdmin()) 
        {
            customCheckbox.setText("Always (Admin)");
            customCheckbox.setEnabled(false);
            customCheckbox.setSelected(true);
        }
        else if (isOverriden())
        {
            customCheckbox.setText("(" + getOverrulingPermissionText() + ")");
            customCheckbox.setEnabled(false);
            customCheckbox.setSelected(true);
        }
        else
        {
            customCheckbox.setText("");
            customCheckbox.setEnabled(true);
        }
    }
}
