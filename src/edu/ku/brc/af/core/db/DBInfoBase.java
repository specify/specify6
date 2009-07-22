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
package edu.ku.brc.af.core.db;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.auth.SecurityMgr;

/**
 * Hold the Schema Information, this is used for for L10N/I18N and whether the item is visible.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Oct 3, 2007
 *
 */
public class DBInfoBase implements Comparable<DBInfoBase>
{
    protected String  name;
    protected String  title;
    protected String  description;
    protected boolean isHidden = false;
    
    // Transient
    protected PermissionSettings permissions = null;
    
    /**
     * Default Constructor.
     */
    public DBInfoBase()
    {
        this(null, null, null);
    }

    /**
     * Constructor with name.
     * @param name the name of the item
     */
    public DBInfoBase(final String name)
    {
        this(name, null, null);
    }

    /**
     * COnstructor with name and localized title and description
     * @param name
     * @param title
     * @param description
     */
    protected DBInfoBase(final String name, final String title, final String description)
    {
        super();
        this.name        = name;
        this.title       = title;
        this.description = description;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(final String description)
    {
        this.description = description;
    }

    /**
     * @return the name
     */
    public String getTitle()
    {
        if (StringUtils.isNotEmpty(title))
        {
            return title;
        }
        return name;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param title the name to set
     */
    public void setTitle(final String title)
    {
        this.title = title;
    }

    /**
     * @return the isHidden
     */
    public boolean isHidden()
    {
        return isHidden;
    }

    /**
     * @param isHidden the isHidden to set
     */
    public void setHidden(final boolean isHidden)
    {
        this.isHidden = isHidden;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getTitle();
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(final DBInfoBase o)
    {
        return getTitle().compareTo(o.getTitle());
    }
    
    /**
     * @return
     */
    protected String getSecurityName()
    {
        throw new RuntimeException("Must be implemented.");
    }

    /**
     * @return the permissions
     */
    public PermissionSettings getPermissions()
    {
        if (permissions == null)
        {
            permissions = SecurityMgr.getInstance().getPermission("DO."+getSecurityName());
        }
        return permissions;
    }

    /**
     * @param permissions the permissions to set
     */
    public void setPermissions(PermissionSettings permissions)
    {
        this.permissions = permissions;
    }
}
