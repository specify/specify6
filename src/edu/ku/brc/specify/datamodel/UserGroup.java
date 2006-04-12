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
     protected Set<SpecifyUser> users;


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
        users = new HashSet<SpecifyUser>();
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
    public Set<SpecifyUser> getUsers() {
        return this.users;
    }

    public void setUsers(Set<SpecifyUser> users) {
        this.users = users;
    }




    // Add Methods

    public void addUser(final SpecifyUser user)
    {
        this.users.add(user);
    }

    // Done Add Methods
}
