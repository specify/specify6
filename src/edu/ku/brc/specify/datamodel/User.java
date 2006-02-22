package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="usysusers"
 *     
 */
public class User  implements java.io.Serializable {

    // Fields    

     protected Integer userId;
     protected String name;
     protected String password;
     protected Integer privLevel;
     private Set collectionObjDef;


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
     *             length="5"
     *         
     */
    public Integer getPrivLevel() {
        return this.privLevel;
    }
    
    public void setPrivLevel(Integer privLevel) {
        this.privLevel = privLevel;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="CollectionObjDefID"
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




}