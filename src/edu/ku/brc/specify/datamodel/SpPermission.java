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
package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

@SuppressWarnings("serial")
@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "sppermission")
public class SpPermission /*extends DataModelObjBase*/implements java.io.Serializable
{
	protected Integer permissionId;
	protected String permissionClass;
	protected String name;
	protected String actions;
	protected Integer targetId;
	protected Set<SpPrincipal> principals;
	
	// Transient 
	protected boolean hasChanged = false;

	// Constructors

	/** default constructor */
	public SpPermission()
	{
	}

	/** constructor with id */
	public SpPermission(Integer permissionId)
	{
		this.permissionId = permissionId;
	}

	// Property accessors

	/**
	 * 
	 */
	@Id
	@GeneratedValue
	@Column(name = "SpPermissionID", unique = false, nullable = false, insertable = true, updatable = true)
	public Integer getPermissionId()
	{
		return this.permissionId;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
	 */
	// @Override
	public void initialize()
	{
		//super.init();
		permissionId    = null;
		permissionClass = null;
		name            = null;
		actions         = null;
		principals      = new HashSet<SpPrincipal>();
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
	 */
	@Transient
	public Class<?> getDataClass()
	{
		return SpPermission.class;
	}

	/**
	 *
	 */
	@Column(name = "Actions", unique = false, nullable = true, insertable = true, updatable = true, length = 256)
	public String getActions()
	{
		return actions;
	}

	/**
	 * @param actions the actions to set
	 */
	public void setActions(String actions)
	{
	    //hasChanged = this.actions == null || !actions.equals(this.actions);
		this.actions = actions;
	}

	/**
	 *
	 */
	@Column(name = "Name", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
	public String getName()
	{
		return this.name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	@Column(name = "TargetId", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getTargetId()
	{
		return this.targetId;
	}

	public void setTargetId(Integer targetId)
	{
		this.targetId = targetId;
	}

	/**
	 *
	 */
	@Column(name = "PermissionClass", unique = false, nullable = false, insertable = true, updatable = true, length = 256)
	public String getPermissionClass()
	{
		return permissionClass;
	}

	/**
	 * @param permissionClass the permissionClass to set
	 */
	public void setPermissionClass(String permissionClass)
	{
		this.permissionClass = permissionClass;
	}

	/**
     * @return the hasChanged
     */
    public boolean hasChanged()
    {
        return hasChanged;
    }

    /**
     * @param hasChanged the hasChanged to set
     */
    public void setHasChanged(boolean hasChanged)
    {
        this.hasChanged = hasChanged;
    }

    /**
	 * Generic Getter for the ID Property.
	 * @returns ID Property.
	 */
	@Transient
	//@Override
	public Integer getId()
	{
		return permissionId;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
	 */
	//@Override
	@Transient
	public int getTableId()
	{
		return getClassTableId();
	}

	/**
	 * @return the Table ID for the class.
	 */
	public static int getClassTableId()
	{
		return 103;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
	 */
	//@Override
	@Transient
	public String getIdentityTitle()
	{
		return "Undefined";
		//return super.getIdentityTitle();
	}

	/**
	 * @param permissionId the permissionId to set
	 */
	public void setPermissionId(Integer permissionId)
	{
		this.permissionId = permissionId;
	}

	/**
	 *
	 */
    @ManyToMany(cascade = {}, fetch = FetchType.LAZY)
    @JoinTable(name = "spprincipal_sppermission", 
            joinColumns = { 
                @JoinColumn(name = "SpPermissionID", 
                            unique = false, nullable = false, insertable = true, updatable = false) }, 
            inverseJoinColumns = { 
                @JoinColumn(name = "SpPrincipalID", 
                            unique = false, nullable = false, insertable = true, updatable = false) 
            }
    )	
    public Set<SpPrincipal> getPrincipals()
	{
		return this.principals;
	}

	/**
	 * @param principals
	 */
	public void setPrincipals(Set<SpPrincipal> principals)
	{
		this.principals = principals;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return name; 
	}
	
	/**
	 * @param other
	 * @return
	 */
	public boolean equals(SpPermission other)
	{
		if (permissionId == null)
			return super.equals(other);
		
		return permissionId.equals(other.permissionId);
	}
	
	// helper methods to parse actions
	/**
	 * @return
	 */
    public boolean hasAction(final String action)
    {
        return (actions != null) && (actions.indexOf(action) >= 0);
    }

	public boolean canView()
	{
		return (actions != null) && (actions.indexOf("view") >= 0);
	}

	/**
	 * @return
	 */
	public boolean canAdd()
	{
		return (actions != null) && (actions.indexOf("add") >= 0);
	}

	/**
	 * @return
	 */
	public boolean canModify()
	{
		return (actions != null) && (actions.indexOf("modify") >= 0);
	}

	/**
	 * @return
	 */
	public boolean canDelete()
	{
		return (actions != null) && (actions.indexOf("delete") >= 0);
	}
	
	/**
	 * @param canView
	 * @param canAdd
	 * @param canModify
	 * @param canDelete
	 * @return
	 */
	public boolean hasSameFlags(boolean canView, boolean canAdd, boolean canModify, boolean canDelete)
	{
		return 	(canView   == canView())   &&
				(canAdd    == canAdd())    &&
				(canModify == canModify()) &&
				(canDelete == canDelete());
	}
	
	/**
	 * @param canView
	 * @param canAdd
	 * @param canModify
	 * @param canDelete
	 */
	public void setActions(boolean canView, boolean canAdd, boolean canModify, boolean canDelete)
	{
		String newActions = "";
		String sep        = "";
		if (canView)
		{
		    newActions += "view";
			sep = ",";
		}
		
		if (canAdd)
		{
		    newActions += sep + "add";
			sep = ",";
		}

		if (canModify)
		{
		    newActions += sep + "modify";
			sep = ",";
		}

		if (canDelete)
		{
		    newActions += sep + "delete";
			sep = ",";
		}
		hasChanged = actions == null || !newActions.equals(actions);
		actions = newActions;
	}
}
