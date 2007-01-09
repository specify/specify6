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




/**

 */
public class UserGroup extends DataModelObjBase implements java.io.Serializable {

    // Fields

     protected Long userGroupId;
     protected String name;
     protected String remarks;
     protected Set<SpecifyUser> specifyUsers;
     protected Set<RecordSet> recordsets;
     private Set<Workbench> workbenches;
     protected Set<AppResource> appResources;

    // Constructors

    /** default constructor */
    public UserGroup() {
    }

    /** constructor with id */
    public UserGroup(Long userGroupId) {
        this.userGroupId = userGroupId;
    }




    // Initializer
    public void initialize()
    {
        userGroupId = null;
        name = null;
        remarks = null;
        specifyUsers = new HashSet<SpecifyUser>();
        recordsets = new HashSet<RecordSet>();
        workbenches = new HashSet<Workbench>();
        appResources = new HashSet<AppResource>();
    }
    // End Initializer

    // Property accessors

    /**
     *
     */
    public Long getUserGroupId() {
        return this.userGroupId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.userGroupId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    public Class<?> getDataClass()
    {
        return UserGroup.class;
    }

    public void setUserGroupId(Long userGroupId) {
        this.userGroupId = userGroupId;
    }

    /**
     *
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     */
    public String getRemarks() {
        return this.remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *
     */
    public Set<SpecifyUser> getSpecifyUsers() {
        return this.specifyUsers;
    }

    public void setSpecifyUsers(Set<SpecifyUser> specifyUsers) {
        this.specifyUsers = specifyUsers;
    }

    /**
    *
    */
   public Set<RecordSet> getRecordsets() {
       return this.recordsets;
   }

   public void setRecordsets(Set<RecordSet> recordSets) {
       this.recordsets = recordSets;
   }

   /**
    * 
    */
   public Set<AppResource> getAppResources() {
       return this.appResources;
   }
   
   public void setAppResources(Set<AppResource> appResource) {
       this.appResources = appResource;
   } 
   /**
    * 
    */
   public Set getWorkbenches() {
       return this.workbenches;
   }
   
   public void setWorkbenches(Set workbench) {
       this.workbenches = workbench;
   }
    // Add Methods

    public void addSpecifyUser(final SpecifyUser specifyUserArg)
    {
        this.specifyUsers.add(specifyUserArg);
        specifyUserArg.getUserGroup().add(this);
    }

    public void addWorkbench(final Workbench workbench)
    {
        this.workbenches.add(workbench);
        workbench.setGroup(this);
    }
    
    public void addRecordSet(RecordSet recordSet)
    {
        this.recordsets.add(recordSet);
        recordSet.setGroup(this);
    }
    
    public void addAppResource(AppResource appResource)
    {
        this.appResources.add(appResource);
        appResource.setGroup(this);
    }
    // Done Add Methods

    // Delete Methods

    public void removeSpecifyUser(final SpecifyUser specifyUserArg)
    {
        this.specifyUsers.remove(specifyUserArg);
        specifyUserArg.getUserGroup().remove(this);
        //specifyUserArg.setUserGroup(null);
    }
    
    public void removeWorkbench(Workbench workbench)
    {
        this.workbenches.remove(workbench);
        workbench.setGroup(null);
    }
    
    public void removeRecordSet(RecordSet recordSet)
    {
        this.recordsets.remove(recordSet);
        recordSet.setGroup(null);
    }
    public void removeAppResource(AppResource appResource)
    {
        this.appResources.remove(appResource);
        appResource.setGroup(null);
    }

    // Delete Add Methods
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    public Integer getTableId()
    {
        return 78;
    }
    public String getIdentityTitle()
    { 
        if(name!=null)return name;
        return super.getIdentityTitle();
    }
}
