package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class UserGroupScope extends DataModelObjBase 
{
    //private static final Logger log = Logger.getLogger(UserGroupScope.class);
	
    protected Integer userGroupScopeId;
    protected Set<SpPrincipal> userGroups;

    public UserGroupScope() {}
    
    public UserGroupScope(Integer userGroupScopeId)
    {
    	this.userGroupScopeId = userGroupScopeId;
    }

    public void init()
    {
    	super.init();

    	userGroupScopeId = null;
    	userGroups = new HashSet<SpPrincipal>();
    }
    
    @Id  
    @GeneratedValue(generator = "hilo")  
    @GenericGenerator(name = "hilo", strategy = "hilo")  
    @Column(name = "UserGroupScopeId", unique = true, nullable = false, insertable = true, updatable = true)
	public Integer getUserGroupScopeId() {
		return userGroupScopeId;
	}
	
	public void setUserGroupScopeId(Integer userGroupScopeId) {
		this.userGroupScopeId = userGroupScopeId;
	}

	@OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "scope")
	public Set<SpPrincipal> getUserGroups() {
		return userGroups;
	}
	
	public void setUserGroups(Set<SpPrincipal> userGroups) {
		this.userGroups = userGroups;
	}
}
