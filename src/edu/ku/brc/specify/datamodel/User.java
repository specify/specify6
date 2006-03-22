package edu.ku.brc.specify.datamodel;

import java.util.Set;




/**
 *        @hibernate.class
 *         table="user"
 *     
 */
public class User  implements java.io.Serializable {

    // Fields    

     protected Integer userId;
     protected String name;
     protected String password;
     protected Short privLevel;
     private Set collectionObjDef;
     protected Set recordSets;
     private UserGroup userGroup;


    // Constructors

    /** default constructor */
    public User() {
    }
    
    /** constructor with id */
    public User(Integer userId) {
        this.userId = userId;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Integer getUserId() {
        return this.userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
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
     *             column="Password"
     *             length="64"
     *         
     */
    public String getPassword() {
        return this.password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     *      *            @hibernate.property
     *             column="PrivLevel"
     *         
     */
    public Short getPrivLevel() {
        return this.privLevel;
    }
    
    public void setPrivLevel(Short privLevel) {
        this.privLevel = privLevel;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="UserID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.CollectionObjDef"
     *         
     */
    public Set getCollectionObjDef() {
        return this.collectionObjDef;
    }
    
    public void setCollectionObjDef(Set collectionObjDef) {
        this.collectionObjDef = collectionObjDef;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="CollectionObjectID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.RecordSet"
     *         
     */
    public Set getRecordSets() {
        return this.recordSets;
    }
    
    public void setRecordSets(Set recordSets) {
        this.recordSets = recordSets;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="UserGroupID"         
     *         
     */
    public UserGroup getUserGroup() {
        return this.userGroup;
    }
    
    public void setUserGroup(UserGroup userGroup) {
        this.userGroup = userGroup;
    }




}