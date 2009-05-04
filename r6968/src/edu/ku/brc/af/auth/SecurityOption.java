/* Copyright (C) 2009, University of Kansas Center for Research
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

import java.util.Hashtable;
import java.util.List;

import javax.swing.ImageIcon;

import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.ui.IconManager;

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
    protected String                iconName     = null;
    protected PermissionEditorIFace editor;
    protected PermissionIFace       permissions  = null;
    
    protected Hashtable<String, PermissionIFace> defaultPermissionsHash = new Hashtable<String, PermissionIFace>();

    /**
     * @param name   // the unique name of the Security Option
     * @param title  // already localized
     * @param prefix // prefix (not including '.')
     */
    public SecurityOption(final String name, 
                          final String title, 
                          final String prefix)
    {
        this(name, title, prefix, (PermissionEditorIFace)null);
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
     * @param name   // the unique name of the Security Option
     * @param title  // alrady localized
     * @param prefix // prefix (not including '.')
     * @param editor // the editor for the Security Panel editor
     */
    public SecurityOption(final String name, 
                          final String title, 
                          final String prefix, 
                          final String iconName)
    {
        super();
        this.name     = name;
        this.title    = title;
        this.prefix   = prefix;
        this.iconName = iconName;
        this.editor   = null;

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
        if (iconName != null)
        {
            for (IconManager.IconSize iconSize : IconManager.IconSize.values())
            {
                if (iconSize.size() == size)
                {
                    return IconManager.getIcon(iconName, iconSize);
                }
            }
        }
        return icon;
    }

    /**
     * @return the iconName
     */
    public String getIconName()
    {
        return iconName;
    }


    /**
     * @param iconName the iconName to set
     */
    public void setIconName(String iconName)
    {
        this.iconName = iconName;
    }


    /**
     * @param userType
     * @param defaultPerms
     */
    public void addDefaultPerm(final String userType, final PermissionIFace defaultPerms)
    {
        defaultPermissionsHash.put(userType, defaultPerms);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.auth.SecurityOptionIFace#getDefaultPermissions(java.lang.String)
     */
    @Override
    public PermissionIFace getDefaultPermissions(final String userType)
    {
        return defaultPermissionsHash.get(userType);
    }

}
