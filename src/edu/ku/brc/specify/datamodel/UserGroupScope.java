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
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
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
    @Column(name = "UserGroupScopeId", /*unique = true,*/ nullable = false, insertable = true, updatable = true)
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
