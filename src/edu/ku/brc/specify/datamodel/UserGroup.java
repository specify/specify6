package edu.ku.brc.specify.datamodel;

import java.util.Set;




/**
 *        @hibernate.class
 *         table="usergroup"
 *     
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
     *      *            @hibernate.property
     *             column="Name"
     *             length="64"
     *         
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     *      *            @hibernate.property
     *             column="Remarks"
     *         
     */
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="UserGroupID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.SpecifyUser"
     *         
     */
    public Set getUsers() {
        return this.users;
    }
    
    public void setUsers(Set users) {
        this.users = users;
    }




}