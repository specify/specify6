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





    // Add Methods

    public void addSpecifyUsers(final SpecifyUser specifyUser)
    {
        this.specifyUsers.add(specifyUser);
        specifyUser.setUserGroup(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeSpecifyUsers(final SpecifyUser specifyUser)
    {
        this.specifyUsers.remove(specifyUser);
        specifyUser.setUserGroup(null);
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

}
