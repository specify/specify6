package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="preptypes"
 *     
 */
public class PrepTypes  implements java.io.Serializable {

    // Fields    

     protected Integer prepTypeID;
     protected String name;
     protected Set prepsObjs;


    // Constructors

    /** default constructor */
    public PrepTypes() {
    }
    
    /** constructor with id */
    public PrepTypes(Integer prepTypeID) {
        this.prepTypeID = prepTypeID;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="PrepTypeID"
     *         
     */
    public Integer getPrepTypeID() {
        return this.prepTypeID;
    }
    
    public void setPrepTypeID(Integer prepTypeID) {
        this.prepTypeID = prepTypeID;
    }

    /**
     *      *            @hibernate.property
     *             column="name"
     *             length="50"
     *         
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="preparationId"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.PrepsObj"
     *         
     */
    public Set getPrepsObjs() {
        return this.prepsObjs;
    }
    
    public void setPrepsObjs(Set prepsObjs) {
        this.prepsObjs = prepsObjs;
    }




}