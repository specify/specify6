/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
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
     
}
