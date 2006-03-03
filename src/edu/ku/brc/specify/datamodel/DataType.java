package edu.ku.brc.specify.datamodel;

import java.util.Set;




/**
 *        @hibernate.class
 *         table="datatype"
 *     
 */
public class DataType  implements java.io.Serializable {

    // Fields    

     protected Integer dataTypeId;
     protected String name;
     private Set collectionObjDef;


    // Constructors

    /** default constructor */
    public DataType() {
    }
    
    /** constructor with id */
    public DataType(Integer dataTypeId) {
        this.dataTypeId = dataTypeId;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Integer getDataTypeId() {
        return this.dataTypeId;
    }
    
    public void setDataTypeId(Integer dataTypeId) {
        this.dataTypeId = dataTypeId;
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