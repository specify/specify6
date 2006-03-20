package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="collectionobjdef"
 *     
 */
public class CollectionObjDef  implements java.io.Serializable {

    // Fields    

     protected Integer collectionObjDefId;
     protected String name;
     protected DataType dataType;
     private Set catalogSeries;
     private Set taxonTreeDef;
     protected User user;
     private Set AttributeDefs;


    // Constructors

    /** default constructor */
    public CollectionObjDef() {
    }
    
    /** constructor with id */
    public CollectionObjDef(Integer collectionObjDefId) {
        this.collectionObjDefId = collectionObjDefId;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Integer getCollectionObjDefId() {
        return this.collectionObjDefId;
    }
    
    public void setCollectionObjDefId(Integer collectionObjDefId) {
        this.collectionObjDefId = collectionObjDefId;
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
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="DataTypeID"
     *         
     */
    public DataType getDataType() {
        return this.dataType;
    }
    
    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    /**
     * 
     */
    public Set getCatalogSeries() {
        return this.catalogSeries;
    }
    
    public void setCatalogSeries(Set catalogSeries) {
        this.catalogSeries = catalogSeries;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="TreeDefID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.TaxonTreeDef"
     *         
     */
    public Set getTaxonTreeDef() {
        return this.taxonTreeDef;
    }
    
    public void setTaxonTreeDef(Set taxonTreeDef) {
        this.taxonTreeDef = taxonTreeDef;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="UserID"
     *         
     */
    public User getUser() {
        return this.user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="CollectionObjDefID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.AttributeDef"
     *         
     */
    public Set getAttributeDefs() {
        return this.AttributeDefs;
    }
    
    public void setAttributeDefs(Set AttributeDefs) {
        this.AttributeDefs = AttributeDefs;
    }




}