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
package edu.ku.brc.specify.tasks.subpane.security;

import java.util.Iterator;

import edu.ku.brc.af.auth.specify.principal.GroupPrincipal;
import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.ui.UIRegistry;

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
    public GeneralPermissionTableCellValueWrapper(final SpPermission permission,
                                                  final SpPermission overrulingPermission,
                                                  final String       permissionAction,
                                                  final boolean      adminPrincipal)
    {
        this.admin                 = adminPrincipal;
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
    public void setOverrulingPermissionActionValue(final boolean overrulingPermissionActionValue)
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
    public void setOverrulingPermissionText(final String overrulingPermissionText)
    {
        this.overrulingPermissionText = overrulingPermissionText;
    }
    
    /**
     * Indicates whether the current permission is overriden by any other. 
     * @return flag indicating whether the current permission is overriden by any other
     */
    public boolean isOverriden() 
    {
        return overrulingPermissionActionValue;
    }

    /**
     * @return
     */
    public boolean isAdmin()
    {
        return admin;
    }
    
    /**
     * Prepare an instance of our custom JCheckBox to be returned by the table cell renderer and editor.
     *  
     * @param customCheckbox
     */
    public void prepareComponent(final GeneralPermissionTableCheckBox customCheckbox)
    {
        // finally, put itself inside the checkbox
        customCheckbox.setCellValue(this);

        if (isAdmin()) 
        {
            customCheckbox.setText(UIRegistry.getResourceString("SEC_ALWAYS_ADM"));
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
