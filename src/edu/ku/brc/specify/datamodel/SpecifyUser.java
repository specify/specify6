/*
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */
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
package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;

/**
 * 
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "specifyuser")
public class SpecifyUser extends DataModelObjBase implements java.io.Serializable
{

    protected static SpecifyUser      currentUser = null;

    // Fields

    protected Integer                   specifyUserId;
    protected String                    name;
    protected String                    email;
    protected String                    userType;
    protected Short                     privLevel;
    protected Set<CollectionType>       collectionTypes;
    protected Set<RecordSet>            recordSets;
    protected Set<Workbench>            workbenches;
    protected Set<WorkbenchTemplate>    workbenchTemplates;
    protected Set<SpAppResource>        spAppResources;
    protected Set<UserGroup>            userGroups;
    protected Set<SpAppResourceDir>     spAppResourceDirs;
    protected Set<UserPermission>       userPermissions;
    protected Set<SpQuery>              spQuerys;
    protected Agent                     agent;

    // Constructors

    /** default constructor */
    public SpecifyUser()
    {
        //
    }

    /** constructor with id */
    public SpecifyUser(Integer specifyUserId)
    {
        this.specifyUserId = specifyUserId;
    }

    /**
     * Return the current Specify User.
     * 
     * @return the current Specify User
     */
    public static SpecifyUser getCurrentUser()
    {
        return currentUser;
    }

    /**
     * Sets the Current Specify User.
     * 
     * @param currentUser
     *            the current specify user
     */
    public static void setCurrentUser(final SpecifyUser currentUser)
    {
        SpecifyUser.currentUser = currentUser;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        
        specifyUserId      = null;
        name               = null;
        email              = null;
        privLevel          = null;
        collectionTypes    = new HashSet<CollectionType>();
        recordSets         = new HashSet<RecordSet>();
        workbenches        = new HashSet<Workbench>();
        workbenchTemplates = new HashSet<WorkbenchTemplate>();
        spAppResources     = new HashSet<SpAppResource>();
        userGroups         = new HashSet<UserGroup>();
        spAppResourceDirs  = new HashSet<SpAppResourceDir>();
        userPermissions    = new HashSet<UserPermission>();
        spQuerys           = new HashSet<SpQuery>();

    }

    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "SpecifyUserID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpecifyUserId()
    {
        return this.specifyUserId;
    }

    /**
     * Generic Getter for the ID Property.
     * 
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.specifyUserId;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return SpecifyUser.class;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#isChangeNotifier()
     */
    @Transient
    @Override
    public boolean isChangeNotifier()
    {
        return false;
    }
    
    public void setSpecifyUserId(Integer specifyUserId) {
        this.specifyUserId = specifyUserId;
    }

    /**
     * 
     */
    @Column(name = "Name", unique = true, nullable = false, insertable = true, updatable = true, length = 64)
    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * 
     */
    @Column(name = "EMail", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getEmail()
    {
        return this.email;
    }

    /**
     * @param email - 
     * void
     */
    public void setEmail(String email)
    {
        this.email = email;
    }

    /**
     * @return the userType
     */
    @Column(name = "UserType", unique = false, nullable = false, insertable = true, updatable = true, length = 32)
    public String getUserType()
    {
        return this.userType;
    }

    /**
     * @param userType the userType to set
     */
    public void setUserType(String userType)
    {
        this.userType = userType;
    }

    /**
     * @return - 
     * Short
     */
    @Column(name = "PrivLevel", unique = false, nullable = false, insertable = true, updatable = true)
    public Short getPrivLevel()
    {
        return this.privLevel;
    }

    /**
     * @param privLevel - 
     * void
     */
    public void setPrivLevel(Short privLevel)
    {
        this.privLevel = privLevel;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "specifyUser")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<CollectionType> getCollectionTypes()
    {
        return this.collectionTypes;
    }

    /**
     * @param collectionType - 
     * void
     */
    public void setCollectionTypes(Set<CollectionType> collectionType)
    {
        this.collectionTypes = collectionType;
    }

    /**
     * 
     */
    @OneToMany(cascade = { CascadeType.REMOVE }, fetch = FetchType.LAZY, mappedBy = "specifyUser")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<RecordSet> getRecordSets()
    {
        return this.recordSets;
    }

    /**
     * @param recordSets - 
     * void
     */
    public void setRecordSets(Set<RecordSet> recordSets)
    {
        this.recordSets = recordSets;
    }

    /**
     * 
     */
    /**
     * @return - 
     * Set<UserGroup>
     */
    @ManyToMany(cascade = {}, fetch = FetchType.LAZY)
    @JoinTable(name = "sp_usergroup", joinColumns = { @JoinColumn(name = "SpecifyUserID", unique = false, nullable = false, insertable = true, updatable = false) }, inverseJoinColumns = { @JoinColumn(name = "UserGroupID", unique = false, nullable = false, insertable = true, updatable = false) })
    @Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE })
    public Set<UserGroup> getUserGroup()
    {
        return this.userGroups;
    }

    /**
     * @param userGroup - 
     * void
     */
    public void setUserGroup(Set<UserGroup> userGroup)
    {
        this.userGroups = userGroup;
    }

    /**
     * @return - 
     * Set<SpAppResourceDir>
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "specifyUser")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<SpAppResourceDir> getSpAppResourceDirs()
    {
        return spAppResourceDirs;
    }

    /**
     * @param spAppResourceDirs - 
     * void
     */
    public void setSpAppResourceDirs(Set<SpAppResourceDir> spAppResourceDirs)
    {
        this.spAppResourceDirs = spAppResourceDirs;
    }

    /**
     * @return - 
     * Set<UserPermission>
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "specifyUser")
    @Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<UserPermission> getUserPermissions()
    {
        return this.userPermissions;
    }

    /**
     * @param userPermissions - 
     * void
     */
    public void setUserPermissions(Set<UserPermission> userPermissions)
    {
        this.userPermissions = userPermissions;
    }

    /**
     * @return - 
     * Set<AppResource>
     */
    @OneToMany(cascade = { CascadeType.REMOVE }, fetch = FetchType.LAZY, mappedBy = "specifyUser")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<SpAppResource> getSpAppResources()
    {
        return this.spAppResources;
    }

    /**
     * @param spAppResource - 
     * void
     */
    public void setSpAppResources(Set<SpAppResource> spAppResources)
    {
        this.spAppResources = spAppResources;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "specifyUser")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<Workbench> getWorkbenches()
    {
        return this.workbenches;
    }

    /**
     * @param workbench - 
     * void
     */
    public void setWorkbenches(Set<Workbench> workbench)
    {
        this.workbenches = workbench;
    }

    /**
     * @param workbench - 
     * void
     */
    public void setWorkbenchTemplates(Set<WorkbenchTemplate> workbenchTemplates)
    {
        this.workbenchTemplates = workbenchTemplates;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "specifyUser")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<WorkbenchTemplate> getWorkbenchTemplates()
    {
        return this.workbenchTemplates;
    }

    /**
     * @return - 
     * Agent
     */
    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @JoinColumn(name = "AgentID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent()
    {
        // XXX - RELEASE (nullable should be false in final release)
        return this.agent;
    }

    /**
     * @param agent - 
     * void
     */
    public void setAgent(Agent agent)
    {
        this.agent = agent;
    }

    // Add Methods
    /**
     * @param userGroupArg - 
     * void
     */
    public void addUserGroups(final UserGroup userGroupArg)
    {
        this.userGroups.add(userGroupArg);
        userGroupArg.getSpecifyUsers().add(this);
    }
    
    /**
     * @return the spQuerys
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "specifyUser")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<SpQuery> getSpQuerys()
    {
        return spQuerys;
    }

    /**
     * @param spQuerys the spQuerys to set
     */
    public void setSpQuerys(Set<SpQuery> spQuerys)
    {
        this.spQuerys = spQuerys;
    }
    
    //-------------------------------------------------------------------------
    //-- FormDataObjIFace
    //-------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
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
        return 72;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    { 
        if (name != null) return name;
        return super.getIdentityTitle();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        if (name != null) return name;
        return super.getIdentityTitle();       
    }
}
