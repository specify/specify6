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

import java.sql.Timestamp;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cascade;

import edu.ku.brc.af.auth.specify.principal.AdminPrincipal;
import edu.ku.brc.af.auth.specify.principal.GroupPrincipal;
import edu.ku.brc.af.auth.specify.principal.UserPrincipal;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.specify.SpecifyUserTypes;

/**
 * 
 */
@SuppressWarnings("serial")
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "specifyuser")
public class SpecifyUser extends DataModelObjBase implements java.io.Serializable, Comparable<SpecifyUser>
{

    // Fields
    protected Integer                   specifyUserId;
    protected String                    name;
    protected String                    email;
    protected String                    password;
    protected String                    userType;
    protected Boolean                   isLoggedIn;
    protected String				    loginCollectionName;
    protected String				    loginDisciplineName;
    protected Boolean                   isLoggedInReport;
    protected Timestamp                 loginOutTime;
    protected Long                      accumMinLoggedIn;
    
    //protected Short                     privLevel;
    //protected Set<RecordSet>            recordSets;
    protected Set<Workbench>            workbenches;
    protected Set<WorkbenchTemplate>    workbenchTemplates;
    protected Set<SpAppResource>        spAppResources;
    protected Set<SpPrincipal>          spPrincipals;
    protected Set<SpAppResourceDir>     spAppResourceDirs;
    protected Set<SpQuery>              spQuerys;
    protected Set<Agent>              	agents;
    protected Set<SpTaskSemaphore>      taskSemaphores;

    //protected SpPrincipal             principal;

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

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        
        specifyUserId = null;
		name = null;
		email = null;
		isLoggedIn = false;
		loginCollectionName = null;
		loginDisciplineName = null;
		isLoggedInReport = false;
		loginOutTime = null;
        
        //privLevel          = null;
        //recordSets         = new HashSet<RecordSet>();
        workbenches        = new HashSet<Workbench>();
        workbenchTemplates = new HashSet<WorkbenchTemplate>();
        spAppResources     = new HashSet<SpAppResource>();
        spPrincipals       = new HashSet<SpPrincipal>();
        spAppResourceDirs  = new HashSet<SpAppResourceDir>();
        spQuerys           = new HashSet<SpQuery>();
        agents             = new HashSet<Agent>();
        taskSemaphores     = new HashSet<SpTaskSemaphore>();

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
     * 
     */
    @Column(name = "Password", unique = false, nullable = false, insertable = true, updatable = true, length = 255)
    public String getPassword()
    {
        return this.password;
    }

    /**
     * @param password - 
     * void
     */
    public void setPassword(String password)
    {
        this.password = password;
    }
    /**
     * @return the userType
     */
    @Column(name = "UserType", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
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

//    /**
//     * @return - 
//     * Short
//     */
//    @Column(name = "PrivLevel", unique = false, nullable = false, insertable = true, updatable = true)
//    public Short getPrivLevel()
//    {
//        return this.privLevel;
//    }

//    /**
//     * @param privLevel - 
//     * void
//     */
//    public void setPrivLevel(Short privLevel)
//    {
//        this.privLevel = privLevel;
//    }

    /**
     * @return the accumMinLoggedIn
     */
    @Column(name = "AccumMinLoggedIn", unique = false, nullable = true, insertable = true, updatable = true)
    public Long getAccumMinLoggedIn()
    {
        return accumMinLoggedIn;
    }

    /**
     * @param accumMinLoggedIn the accumMinLoggedIn to set
     */
    public void setAccumMinLoggedIn(Long accumMinLoggedIn)
    {
        this.accumMinLoggedIn = accumMinLoggedIn;
    }

    /**
     * @return the loginOutTime
     */
    @Column(name = "LoginOutTime", unique = false, nullable = true, insertable = true, updatable = true)
    public Timestamp getLoginOutTime()
    {
        return loginOutTime;
    }

    /**
     * @param loginOutTime the loginOutTime to set
     */
    public void setLoginOutTime(Timestamp loginOutTime)
    {
        if (loginOutTime != null && loginOutTime.getTime() > 0)
        {
            this.loginOutTime = loginOutTime;
        }
    }

    /**
     * @return the isLoggedIn
     */
    @Column(name = "IsLoggedIn", unique = false, nullable = false, insertable = true, updatable = true)
    public Boolean getIsLoggedIn()
    {
        return isLoggedIn != null ? isLoggedIn : false;
    }

    /**
     * @param isLoggedIn the isLoggedIn to set
     */
    public void setIsLoggedIn(Boolean isLoggedIn)
    {
        this.isLoggedIn = isLoggedIn;
    }
    
    
    
    /**
	 * @return the loginCollectionName
	 */
    @Column(name="LoginCollectionName", nullable = true, length=64)
	public String getLoginCollectionName()
	{
		return loginCollectionName;
	}

	/**
	 * @param loginCollectionName the loginCollectionName to set
	 */
	public void setLoginCollectionName(String loginCollectionName)
	{
		this.loginCollectionName = loginCollectionName;
	}

	/**
	 * @return the loginDisciplineName
	 */
    @Column(name="LoginDisciplineName", nullable = true, length=64)
	public String getLoginDisciplineName()
	{
		return loginDisciplineName;
	}

	/**
	 * @param loginDisciplineName the loginDisciplineName to set
	 */
	public void setLoginDisciplineName(String loginDisciplineName)
	{
		this.loginDisciplineName = loginDisciplineName;
	}

	/**
     * @return the isLoggedInReport
     */
    @Column(name = "IsLoggedInReport", unique = false, nullable = false, insertable = true, updatable = true)
    public Boolean getIsLoggedInReport()
    {
        return isLoggedInReport;
    }

    /**
     * @param isLoggedInReport the isLoggedInReport to set
     */
    public void setIsLoggedInReport(Boolean isLoggedInReport)
    {
        this.isLoggedInReport = isLoggedInReport;
    }

    /**
     * 
     */
//    @OneToMany(cascade = { CascadeType.REMOVE }, fetch = FetchType.LAZY, mappedBy = "specifyUser")
//    //@org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
//    public Set<RecordSet> getRecordSets()
//    {
//        return this.recordSets;
//    }
//
//    /**
//     * @param recordSets - 
//     * void
//     */
//    public void setRecordSets(Set<RecordSet> recordSets)
//    {
//        this.recordSets = recordSets;
//    }

    /**
     * 
     */
    /**
     * @return - 
     * Set<SpPrincipal>
     */
    @ManyToMany(cascade = {}, fetch = FetchType.LAZY)
    @JoinTable(name = "specifyuser_spprincipal", 
            joinColumns = 
            { 
                @JoinColumn(name = "SpecifyUserID", unique = false, nullable = false, insertable = true, updatable = false) 
            }, 
            inverseJoinColumns = 
            { 
                @JoinColumn(name = "SpPrincipalID", unique = false, nullable = false, insertable = true, updatable = false) 
            }
    )
    
    @Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE })
    public Set<SpPrincipal> getSpPrincipals()
    {
        return this.spPrincipals;
    }

    /**
     * @param spUserGroup - 
     * void
     */
    public void setSpPrincipals(Set<SpPrincipal> spPrincipals)
    {
        this.spPrincipals = spPrincipals;
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
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "specifyUser")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.SAVE_UPDATE, 
                                          org.hibernate.annotations.CascadeType.MERGE, 
                                          org.hibernate.annotations.CascadeType.LOCK })
    public Set<Agent> getAgents()
    {
        return this.agents;
    }
    
    /**
     * @return the taskSemaphores
     */
    @OneToMany(mappedBy = "owner")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<SpTaskSemaphore> getTaskSemaphores()
    {
        return taskSemaphores;
    }

    /**
     * @param taskSemaphores the taskSemaphores to set
     */
    public void setTaskSemaphores(Set<SpTaskSemaphore> taskSemaphores)
    {
        this.taskSemaphores = taskSemaphores;
    }

//    /**
//     * @param agent - 
//     * void
//     */
//    public void setPrincipal(SpPrincipal principal)
//    {
//        this.principal = principal;
//    }
//    /**
//     * @return - 
//     * Agent
//     */
//    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
//    @Cascade( { org.hibernate.annotations.CascadeType.ALL })
//    @JoinColumn(name = "SpPrincipalID", unique = false, nullable = true, insertable = true, updatable = true)
//    public SpPrincipal getPrincipal()
//    {
//        return this.principal;
//    }

    /**
     * @param agenta
     * 
     */
    public void setAgents(Set<Agent> agents)
    {
        this.agents = agents;
    }

    // Add Methods
    /**
     * @param userGroupArg - 
     * void
     */
    public void addUserToSpPrincipalGroup(final SpPrincipal userGroupArg)
    {
        this.spPrincipals.add(userGroupArg);
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


    public void removeUserGroups(final SpPrincipal userGroupArg)
    {
        this.spPrincipals.remove(userGroupArg);
        userGroupArg.getSpecifyUsers().remove(this);
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
        // debug
        //if (name != null) return name  + " - HC: " + this.hashCode();
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
    
    /**
     * Counts the actual number of user groups this user belongs to.
     * That's not the same thing as counting the principals, because in JAAS a principal may be
     * a single user or a group. 
     * @return the actual number of user groups this user belongs to.
     */
    @Transient
    public int getUserGroupCount() 
    {
        int count = 0;
        for (SpPrincipal principal : getSpPrincipals())
        {
            if (GroupPrincipal.class.getCanonicalName().equals(principal.getGroupSubClass()) ||
                AdminPrincipal.class.getCanonicalName().equals(principal.getGroupSubClass()))
            {
                ++count;
            }
        }
        return count;
    }
    
    /**
     * @return true if the user is in the admin group
     */
    @Transient
    public boolean isInAdminGroup() 
    {
        for (SpPrincipal principal : getSpPrincipals())
        {
            if (AdminPrincipal.class.getCanonicalName().equals(principal.getGroupSubClass()))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the user principal, that is, that principal that is from the sub-class UserPrincipal.
     * This principal represents the user (as opposed as a user group) in JAAS 
     * @return the user principal
     */
    @Transient
    public SpPrincipal getUserPrincipal()
    {
        for (SpPrincipal principal : getSpPrincipals())
        {
            if (UserPrincipal.class.getCanonicalName().equals(principal.getGroupSubClass()))
            {
                return principal;
            }
        }
        
        // Data inconsistency: we shouldn't really get here 
        // because every user must have its own UserPrincipal
        return null;
    }

    /**
     * @param group
     * @return true if current user is of type userType. 
     */
    @Transient
    public static boolean isCurrentUserType(final SpecifyUserTypes.UserType userType)
    {
    	SpecifyUser currentUser = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);  
    	if (currentUser != null)
    	{
    		return currentUser.getUserType().equals(userType.toString());
    	}
    	//else (this is probably impossible)
    	return false; 
    }
        
    //----------------------------------------------------------------------
    //-- Comparable Interface
    //----------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(SpecifyUser obj)
    {
        if (name != null && obj != null && StringUtils.isNotEmpty(obj.name))
        {
            return name.compareTo(obj.name);
        }

        if (this.email != null && obj != null && StringUtils.isNotEmpty(obj.email))
        {
            return email.compareTo(obj.email);
        }
        
        // else
        return timestampCreated != null && obj != null && obj.timestampCreated != null ? timestampCreated.compareTo(obj.timestampCreated) : 0;
    }
}
