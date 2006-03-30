package edu.ku.brc.specify.datamodel;

import java.util.Set;




/**

 */
public class UserGroup  implements java.io.Serializable {

    // Fields    

     protected Integer userGroupId;
     protected String name;
     protected String remarks;
     private Set users;


    // Constructors

    /** default constructor */
    public UserGroup() {
    }
    
    /** constructor with id */
    public UserGroup(Integer userGroupId) {
        this.userGroupId = userGroupId;
    }
   
    
    

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
    public Set getUsers() {
        return this.users;
    }
    
    public void setUsers(Set users) {
        this.users = users;
    }




}