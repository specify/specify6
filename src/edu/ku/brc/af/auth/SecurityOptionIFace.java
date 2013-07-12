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

import java.util.List;

import javax.swing.ImageIcon;

import edu.ku.brc.af.core.PermissionIFace;


/**
 * This interface enables the implementer to participate in the Security/Permission system.
 * 
 * @author rod
 *
 * @code_status Complete
 *
 * Oct 21, 2008
 *
 */
public interface SecurityOptionIFace
{
    /**
     * @return a unique name (without a security prefix) used by the security system, 
     * doesn't need to be localized and is not intended to be human readable.
     */
    public abstract String getPermissionName();
    
    /**
     * @return the localized human readable title of the permission.
     */
    public abstract String getPermissionTitle();
    
    /**
     * @return a short localized description of the security option to help explain what it is.
     */
    public abstract String getShortDesc();
    
    /**
     * Return the icon that represents the task.
     * @param size use standard size (i.e. 32, 24, 20, 26)
     * @return the icon that represents the task
     */
    public abstract ImageIcon getIcon(int size);
    
    /**
     * @return a PermissionEditorIFace object that is used to set the permissions for the
     * the task.
     */
    public abstract PermissionEditorIFace getPermEditorPanel();
    
    /**
     * @return returns a permissions object
     */
    public abstract PermissionIFace getPermissions();
    
    /**
     * Sets a permission object.
     * @param permissions the object
     */
    public abstract void setPermissions(PermissionIFace permissions);
    
    /**
     * @return a list of addition Security options. These can be thought as 'sub-options'.
     */
    public abstract List<SecurityOptionIFace> getAdditionalSecurityOptions(); 
    
    /**
     * @param userType the type of use, this value is implementation dependent, it can be null
     * @return the default permissions for a user type
     */
    public abstract PermissionIFace getDefaultPermissions(String userType);
     
}
