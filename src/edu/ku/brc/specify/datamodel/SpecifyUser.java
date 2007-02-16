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


import java.util.HashSet;
import java.util.Set;

/**
 * 
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@Table(name = "specifyuser")
public class SpecifyUser extends DataModelObjBase implements java.io.Serializable
{

    protected static SpecifyUser      currentUser = null;

    // Fields

    protected Long                    specifyUserId;
    protected String                  name;
    protected String                  email;
    protected String                  userType;
    protected Short                   privLevel;
    protected Set<CollectionObjDef>   collectionObjDefs;//
    protected Set<RecordSet>          recordSets;//
    private Set<Workbench>            workbenches;
    private Set<WorkbenchTemplate>    workbenchTemplates;
    protected Set<AppResource>        appResources;
    protected Set<UserGroup>          userGroups;//
    protected Set<AppResourceDefault> appResourceDefaults;//
    protected Set<UserPermission>     userPermissions;
    protected Agent                   agent;

    // Constructors

    /** default constructor */
    public SpecifyUser()
    {
        //
    }

    /** constructor with id */
    public SpecifyUser(Long specifyUserId)
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
        specifyUserId = null;
        name = null;
        email = null;
        privLevel = null;
        collectionObjDefs = new HashSet<CollectionObjDef>();
        recordSets = new HashSet<RecordSet>();
        workbenches = new HashSet<Workbench>();
        workbenchTemplates = new HashSet<WorkbenchTemplate>();
        appResources = new HashSet<AppResource>();
        userGroups = new HashSet<UserGroup>();
        appResourceDefaults = new HashSet<AppResourceDefault>();
        userPermissions = new HashSet<UserPermission>();
        agent = null;
    }

    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "SpecifyUserID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getSpecifyUserId()
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
    public Long getId()
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
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#isIndexable()
     */
    @Transient
    @Override
    public boolean isIndexable()
    {
        return false;
    }
    
    public void setSpecifyUserId(Long specifyUserId) {
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
    public Set<CollectionObjDef> getCollectionObjDefs()
    {
        return this.collectionObjDefs;
    }

    /**
     * @param collectionObjDef - 
     * void
     */
    public void setCollectionObjDefs(Set<CollectionObjDef> collectionObjDef)
    {
        this.collectionObjDefs = collectionObjDef;
    }

    /**
     * 
     */
    @OneToMany(cascade = { CascadeType.REMOVE }, fetch = FetchType.LAZY, mappedBy = "specifyUser")
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
     * Set<AppResourceDefault>
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "specifyUser")
    public Set<AppResourceDefault> getAppResourceDefaults()
    {
        return appResourceDefaults;
    }

    /**
     * @param appResourceDefaults - 
     * void
     */
    public void setAppResourceDefaults(Set<AppResourceDefault> appResourceDefaults)
    {
        this.appResourceDefaults = appResourceDefaults;
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
    public Set<AppResource> getAppResources()
    {
        return this.appResources;
    }

    /**
     * @param appResource - 
     * void
     */
    public void setAppResources(Set<AppResource> appResource)
    {
        this.appResources = appResource;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "specifyUser")
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
    public Set<WorkbenchTemplate> getWorkbenchTemplates()
    {
        return this.workbenchTemplates;
    }

    /**
     * @return - 
     * Agent
     */
    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.MERGE, org.hibernate.annotations.CascadeType.LOCK })
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
     * @param collectionObjDefArg - 
     * void
     */
    public void addCollectionObjDefs(final CollectionObjDef collectionObjDefArg)
    {
        this.collectionObjDefs.add(collectionObjDefArg);
        collectionObjDefArg.setSpecifyUser(this);
    }

    /**
     * @param recordSet - 
     * void
     */
    public void addRecordSets(final RecordSet recordSet)
    {
        this.recordSets.add(recordSet);
        recordSet.setSpecifyUser(this);
    }

    /**
     * @param userPermission - 
     * void
     */
    public void addUserPermissions(final UserPermission userPermission)
    {
        this.userPermissions.add(userPermission);
        userPermission.setSpecifyUser(this);
    }
    
    public void addWorkbenches(final Workbench workbench)
    {
        this.workbenches.add(workbench);
        workbench.setSpecifyUser(this);
    }
    /**
     * @param appResource - 
     * void
     */
    public void addAppResource(final AppResource appResource)
    {
        this.appResources.add(appResource);
        appResource.setSpecifyUser(this);
    }
    
    /**
     * @param appResourceDefault - 
     * void
     */
    public void addAppResourceDefaults(final AppResourceDefault appResourceDefault)
    {
        this.appResourceDefaults.add(appResourceDefault);
        appResourceDefault.setSpecifyUser(this);
    }

    // Done Add Methods

    // Delete Methods

    /**
     * @param userGroupArg - 
     * void
     */
    public void removeUserGroups(final UserGroup userGroupArg)
    {
        this.userGroups.remove(userGroupArg);
        userGroupArg.getSpecifyUsers().remove(this);
    }

    /**
     * @param collectionObjDefArg - 
     * void
     */
    public void removeCollectionObjDefs(final CollectionObjDef collectionObjDefArg)
    {
        this.collectionObjDefs.remove(collectionObjDefArg);
        collectionObjDefArg.setSpecifyUser(null);
    }

    /**
     * @param recordSet - 
     * void
     */
    public void removeRecordSets(final RecordSet recordSet)
    {
        this.recordSets.remove(recordSet);
        recordSet.setSpecifyUser(null);
    }
    
    /**
     * @param workbench - 
     * void
     */
    public void removeWorkbench(final Workbench workbench)
    {
        this.workbenches.remove(workbench);
        workbench.setSpecifyUser(null);
    }
    /**
     * @param appResource - 
     * void
     */
    public void removeAppResource(final AppResource appResource)
    {
        this.appResources.remove(appResource);
        appResource.setSpecifyUser(null);
    }    
    /**
     * @param userPermission - 
     * void
     */
    public void removeUserPermission(final UserPermission userPermission)
    {
        this.userPermissions.remove(userPermission);
        userPermission.setSpecifyUser(null);
    }    
    /**
     * @param appResourceDefault - 
     * void
     */
    public void removeAppResourceDefaults(final AppResourceDefault appResourceDefault)
    {
        this.appResourceDefaults.remove(appResourceDefault);
        appResourceDefault.setSpecifyUser(null);
    }

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
    
    @Override
    public String toString()
    {
        if (name != null) return name;
        return super.getIdentityTitle();       
    }
}
