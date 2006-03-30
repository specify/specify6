package edu.ku.brc.specify.datamodel;

import java.util.Set;




/**

 */
public class SpecifyUser  implements java.io.Serializable {

    // Fields    

     protected Integer specifyUserId;
     protected String name;
     protected String password;
     protected Short privLevel;
     private Set collectionObjDef;
     protected Set recordSets;
     private UserGroup userGroup;


    // Constructors

    /** default constructor */
    public SpecifyUser() {
    }
    
    /** constructor with id */
    public SpecifyUser(Integer specifyUserId) {
        this.specifyUserId = specifyUserId;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Integer getSpecifyUserId() {
        return this.specifyUserId;
    }
    
    public void setSpecifyUserId(Integer specifyUserId) {
        this.specifyUserId = specifyUserId;
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
    public String getPassword() {
        return this.password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 
     */
    public Short getPrivLevel() {
        return this.privLevel;
    }
    
    public void setPrivLevel(Short privLevel) {
        this.privLevel = privLevel;
    }

    /**
     * 
     */
    public Set getCollectionObjDef() {
        return this.collectionObjDef;
    }
    
    public void setCollectionObjDef(Set collectionObjDef) {
        this.collectionObjDef = collectionObjDef;
    }

    /**
     * 
     */
    public Set getRecordSets() {
        return this.recordSets;
    }
    
    public void setRecordSets(Set recordSets) {
        this.recordSets = recordSets;
    }

    /**
     * 
     */
    public UserGroup getUserGroup() {
        return this.userGroup;
    }
    
    public void setUserGroup(UserGroup userGroup) {
        this.userGroup = userGroup;
    }




}