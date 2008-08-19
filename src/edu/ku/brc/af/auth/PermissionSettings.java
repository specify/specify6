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
package edu.ku.brc.af.auth;

import edu.ku.brc.af.core.PermissionIFace;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Aug 19, 2008
 *
 */
public class PermissionSettings implements PermissionIFace 
{
    public static final int NO_PERM          =   0; // Indicates there are no permissions
    public static final int CAN_VIEW         =   1; // Indicates the user can view the form
    public static final int CAN_MODIFY       =   2; // Indicates the user can modify data
    public static final int CAN_DELETE       =   4; // Indicates the user can delete items
    public static final int CAN_ADD          =   8; // Indicates the user can add new items
    
    public static final int ALL_PERM         = CAN_VIEW | CAN_MODIFY | CAN_DELETE | CAN_ADD;
    
    private int permissions;
    
    public PermissionSettings(final int permissions)
    {
        this.permissions = permissions;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionIFace#canModify()
     */
    public boolean canModify()
    {
        return isOn(permissions, CAN_MODIFY);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionIFace#canView()
     */
    public boolean canView()
    {
        return isOn(permissions, CAN_VIEW);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionIFace#canAdd()
     */
    public boolean canAdd()
    {
        return isOn(permissions, CAN_ADD);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionIFace#canDelete()
     */
    public boolean canDelete()
    {
        return isOn(permissions, CAN_DELETE);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionIFace#isViewOnly()
     */
    public boolean isViewOnly()
    {
        return permissions == CAN_VIEW;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionIFace#getOptions()
     */
    public int getOptions()
    {
        return permissions;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionIFace#hasNoPerm()
     */
    public boolean hasNoPerm()
    {
        return permissions == 0;
    }
    
    /**
     * Helper method to see if an option is turned on.
     * @param options the range of options that can be turned on
     * @param opt the actual option that may be turned on
     * @return true if the opt bit is on
     */
    public static boolean isOn(final int options, final int opt)
    {
        return (options & opt) == opt;
    }
    
    /**
     * @param options
     * @return
     */
    public static boolean canModify(final int options)
    {
        return isOn(options, CAN_MODIFY);
    }
    
    /**
     * @param options
     * @return
     */
    public static boolean canView(final int options)
    {
        return isOn(options, CAN_VIEW);
    }
    
    /**
     * @param options
     * @return
     */
    public static boolean canAdd(final int options)
    {
        return isOn(options, CAN_ADD);
    }
    
    /**
     * @param options
     * @return
     */
    public static boolean canDelete(final int options)
    {
        return isOn(options, CAN_DELETE);
    }
    
    /**
     * @param options
     */
    public static void dumpPermissions(final String title, final int options)
    {
        System.err.print(title + " - ");
        System.err.print("  View: "   + (canView(options)   ? "Y" : "N"));
        System.err.print("Modify: " + (canModify(options) ? "Y" : "N"));
        System.err.print("  Delete: " + (canDelete(options) ? "Y" : "N"));
        System.err.println("  Add: "  + (canAdd(options)    ? "Y" : "N"));
    }
}
