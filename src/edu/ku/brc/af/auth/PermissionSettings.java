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
package edu.ku.brc.af.auth;

import org.apache.log4j.Logger;

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
    private static final Logger log = Logger.getLogger(PermissionSettings.class);
    
    public static final int NO_PERM          =   0; // Indicates there are no permissions
    public static final int CAN_VIEW         =   1; // Indicates the user can view the form
    public static final int CAN_MODIFY       =   2; // Indicates the user can modify data
    public static final int CAN_DELETE       =   4; // Indicates the user can delete items
    public static final int CAN_ADD          =   8; // Indicates the user can add new items
    
    public static final int ALL_PERM         = CAN_VIEW | CAN_MODIFY | CAN_DELETE | CAN_ADD;
    
    private int permissions;
    
    /**
     * @param permissions
     */
    public PermissionSettings(final int permissions)
    {
        this.permissions = permissions;
    }
    
    /**
     * @param canView
     * @param canModify
     * @param canDel
     * @param canAdd
     */
    public PermissionSettings(final boolean canView, 
                              final boolean canModify, 
                              final boolean canDel, 
                              final boolean canAdd)
    {
        setCanView(canView);
        setCanModify(canModify);
        setCanDelete(canDel);
        setCanAdd(canAdd);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionIFace#canModify()
     */
    @Override
    public boolean canModify()
    {
        return isOn(permissions, CAN_MODIFY);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionIFace#canView()
     */
    @Override
    public boolean canView()
    {
        return isOn(permissions, CAN_VIEW);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionIFace#canAdd()
     */
    @Override
    public boolean canAdd()
    {
        return isOn(permissions, CAN_ADD);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionIFace#canDelete()
     */
    @Override
    public boolean canDelete()
    {
        return isOn(permissions, CAN_DELETE);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.PermissionIFace#setCanAdd()
     */
    @Override
    public void setCanAdd(final boolean value)
    {
        if (value)
        {
            permissions |= CAN_ADD;
        } else
        {
            permissions &= ~CAN_ADD;
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.PermissionIFace#setCanDelete()
     */
    @Override
    public void setCanDelete(final boolean value)
    {
        if (value)
        {
            permissions |= CAN_DELETE;
        } else
        {
            permissions &= ~CAN_DELETE;
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.PermissionIFace#setCanModify()
     */
    @Override
    public void setCanModify(final boolean value)
    {
        if (value)
        {
            permissions |= CAN_MODIFY;
        } else
        {
            permissions &= ~CAN_MODIFY;
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.PermissionIFace#setCanView()
     */
    @Override
    public void setCanView(final boolean value)
    {
        if (value)
        {
            permissions |= CAN_VIEW;
        } else
        {
            permissions &= ~CAN_VIEW;
        }
   }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionIFace#isViewOnly()
     */
    @Override
    public boolean isViewOnly()
    {
        return permissions == CAN_VIEW;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionIFace#getOptions()
     */
    @Override
    public int getOptions()
    {
        return permissions;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.PermissionIFace#clear()
     */
    @Override
    public void clear()
    {
        permissions = NO_PERM;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.PermissionIFace#hasNoPerm()
     */
    @Override
    public boolean hasNoPerm()
    {
        return permissions == NO_PERM;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.PermissionIFace#setOptions(int)
     */
    @Override
    public void setOptions(int options)
    {
        permissions = options;
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
        log.debug(title + " - View: " + (canView(options)   ? "Y" : "N") + " Modify: " + (canModify(options) ? "Y" : "N") +
                  " Delete: " + (canDelete(options) ? "Y" : "N") + " Add: "  + (canAdd(options)    ? "Y" : "N"));
    }
}
