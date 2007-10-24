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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;




/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "usergroup")
public class UserGroup extends DataModelObjBase implements java.io.Serializable {

    // Fields

     protected Integer            userGroupId;
     protected String             name;
     protected String             remarks;
     protected Set<SpecifyUser>   specifyUsers;
     protected Set<RecordSet>     recordsets;
     private Set<Workbench>       workbenches;
     protected Set<SpAppResource> spAppResources;

    // Constructors

    /** default constructor */
    public UserGroup() 
    {
        //
    }

    /** constructor with id */
    public UserGroup(Integer userGroupId) 
    {
        this.userGroupId = userGroupId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        userGroupId = null;
        name = null;
        remarks = null;
        specifyUsers = new HashSet<SpecifyUser>();
        recordsets = new HashSet<RecordSet>();
        workbenches = new HashSet<Workbench>();
        spAppResources = new HashSet<SpAppResource>();
    }
    // End Initializer

    // Property accessors

    /**
     *
     */
    @Id
    @GeneratedValue
    @Column(name = "UserGroupID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getUserGroupId() {
        return this.userGroupId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.userGroupId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return UserGroup.class;
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
    
    /**
     * @param userGroupId
     */
    public void setUserGroupId(Integer userGroupId) {
        this.userGroupId = userGroupId;
    }

    /**
     *
     */
    @Column(name = "Name", unique = true, nullable = false, insertable = true, updatable = true, length = 64)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     */
    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks() {
        return this.remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *
     */
    @ManyToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "userGroup")
    public Set<SpecifyUser> getSpecifyUsers() {
        return this.specifyUsers;
    }

    public void setSpecifyUsers(Set<SpecifyUser> specifyUsers) {
        this.specifyUsers = specifyUsers;
    }

    /**
    *
    */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "group")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
   public Set<RecordSet> getRecordsets() {
       return this.recordsets;
   }

   public void setRecordsets(Set<RecordSet> recordSets) {
       this.recordsets = recordSets;
   }

   /**
    * 
    */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "group")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
   public Set<SpAppResource> getSpAppResources() 
   {
       return this.spAppResources;
   }
   
   public void setSpAppResources(Set<SpAppResource> spAppResource) 
   {
       this.spAppResources = spAppResource;
   } 
   /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "group")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<Workbench> getWorkbenches()
    {
        return this.workbenches;
    }

    @SuppressWarnings("unchecked")
    public void setWorkbenches(Set<Workbench> workbench)
    {
        this.workbenches = workbench;
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
        return 78;
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
}
