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
 * Helper class for Security Options. This is used mainly for additional options.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 27, 2008
 *
 */
public class SecurityOption implements SecurityOptionIFace
{
    protected String                name;
    protected String                title;
    protected String                prefix;
    protected String                shortDesc    = "";
    protected ImageIcon             icon         = null;
    protected PermissionEditorIFace editor;
    protected PermissionIFace       permissions  = null;
    protected PermissionIFace       defaultPerms = null;
    
    
    /**
     * @param name   // the unique name of the Security Option
     * @param title  // already localized
     * @param prefix // prefix (not including '.')
     */
    public SecurityOption(final String name, 
                          final String title, 
                          final String prefix)
    {
        this(name, title, prefix, null);
    }


    /**
     * @param name   // the unique name of the Security Option
     * @param title  // alrady localized
     * @param prefix // prefix (not including '.')
     * @param editor // the editor for the Security Panel editor
     */
    public SecurityOption(final String name, 
                          final String title, 
                          final String prefix, 
                          final PermissionEditorIFace editor)
    {
        super();
        this.name   = name;
        this.title  = title;
        this.prefix = prefix;
        this.editor = editor;
    }

    /**
     * @param editor the editor to set
     */
    public void setEditor(PermissionEditorIFace editor)
    {
        this.editor = editor;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.SecurityOptionIFace#getPermEditorPanel()
     */
    @Override
    public PermissionEditorIFace getPermEditorPanel()
    {
        return editor;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.SecurityOptionIFace#getPermissionName()
     */
    @Override
    public String getPermissionName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.SecurityOptionIFace#getPermissions()
     */
    @Override
    public PermissionIFace getPermissions()
    {
        if (permissions == null)
        {
            permissions = SecurityMgr.getInstance().getPermission(prefix + "." + getPermissionName());
        }
        return permissions;
    }

    /**
     * @param shortDesc the shortDesc to set
     */
    public void setShortDesc(final String shortDesc)
    {
        this.shortDesc = shortDesc;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.SecurityOptionIFace#getShortDesc()
     */
    @Override
    public String getShortDesc()
    {
        return shortDesc ==  null ? title : shortDesc;
    }

    /**
     * @param title the title to set
     */
    public void setSecurityTitle(final String title)
    {
        this.title = title;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.SecurityOptionIFace#getTitle()
     */
    @Override
    public String getPermissionTitle()
    {
        return title;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.SecurityOptionIFace#setPermissions(edu.ku.brc.af.core.PermissionIFace)
     */
    @Override
    public void setPermissions(PermissionIFace permissions)
    {
        this.permissions = permissions;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.SecurityOptionIFace#getAdditionalSecurityOptions()
     */
    @Override
    public List<SecurityOptionIFace> getAdditionalSecurityOptions()
    {
        return null;
    }

    /**
     * @return the icon
     */
    public ImageIcon getIcon()
    {
        return icon;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(ImageIcon icon)
    {
        this.icon = icon;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.SecurityOptionIFace#getIcon(int)
     */
    @Override
    public ImageIcon getIcon(int size)
    {
        return icon;
    }


    /**
     * @param defaultPerms the defaultPerms to set
     */
    public void setDefaultPerms(final PermissionIFace defaultPerms)
    {
        this.defaultPerms = defaultPerms;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.SecurityOptionIFace#getDefaultPermissions(java.lang.String)
     */
    @Override
    public PermissionIFace getDefaultPermissions(final String userType)
    {
        return defaultPerms;
    }

}
