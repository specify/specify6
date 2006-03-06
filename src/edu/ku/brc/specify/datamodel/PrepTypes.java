package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="preptypes"
 *     
 */
public class PrepTypes  implements java.io.Serializable {

    // Fields    

     protected Integer prepTypeId;
     protected String name;
     protected Set preparation;


    // Constructors

    /** default constructor */
    public PrepTypes() {
    }
    
    /** constructor with id */
    public PrepTypes(Integer prepTypeId) {
        this.prepTypeId = prepTypeId;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Integer getPrepTypeId() {
        return this.prepTypeId;
    }
    
    public void setPrepTypeId(Integer prepTypeId) {
        this.prepTypeId = prepTypeId;
    }

    /**
     *      *            @hibernate.property
     *             column="Name"
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
     *             column="PreparationID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Preparation"
     *         
     */
    public Set getPreparation() {
        return this.preparation;
    }
    
    public void setPreparation(Set preparation) {
        this.preparation = preparation;
    }




}