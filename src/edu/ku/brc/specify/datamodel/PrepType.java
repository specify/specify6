package edu.ku.brc.specify.datamodel;

import java.util.Set;




/**
 *        @hibernate.class
 *         table="preptype"
 *     
 */
public class PrepType  implements java.io.Serializable {

    // Fields    

     protected Integer prepTypeId;
     protected String name;
     protected Set preparations;
     protected Set attributeDefs;


    // Constructors

    /** default constructor */
    public PrepType() {
    }
    
    /** constructor with id */
    public PrepType(Integer prepTypeId) {
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
     *             length="32"
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
    public Set getPreparations() {
        return this.preparations;
    }
    
    public void setPreparations(Set preparations) {
        this.preparations = preparations;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="AttributeDefID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.AttributeDef"
     *         
     */
    public Set getAttributeDefs() {
        return this.attributeDefs;
    }
    
    public void setAttributeDefs(Set attributeDefs) {
        this.attributeDefs = attributeDefs;
    }




}