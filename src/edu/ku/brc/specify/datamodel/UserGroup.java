package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;




/**

 */
public class UserGroup  implements java.io.Serializable {

    // Fields

     protected Integer userGroupId;
     protected String name;
     protected String remarks;
     protected Set<SpecifyUser> specifyUsers;


    // Constructors

    /** default constructor */
    public UserGroup() {
    }

    /** constructor with id */
    public UserGroup(Integer userGroupId) {
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
    public Integer getUserGroupId() {
        return this.userGroupId;
    }

    public void setUserGroupId(Integer userGroupId) {
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
}
