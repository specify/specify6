package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="CollectionTaxonomyTypes"
 *     
 */
public class CollectionTaxonomyType  implements java.io.Serializable {

    // Fields    

     protected Integer collectionTaxonomyTypesId;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     private CollectionObjectType collectionObjectType;
     private TaxonomyType taxonomyType;
     private Collection collection;


    // Constructors

    /** default constructor */
    public CollectionTaxonomyType() {
    }
    
    /** constructor with id */
    public CollectionTaxonomyType(Integer collectionTaxonomyTypesId) {
        this.collectionTaxonomyTypesId = collectionTaxonomyTypesId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="CollectionTaxonomyTypesID"
     *         
     */
    public Integer getCollectionTaxonomyTypesId() {
        return this.collectionTaxonomyTypesId;
    }
    
    public void setCollectionTaxonomyTypesId(Integer collectionTaxonomyTypesId) {
        this.collectionTaxonomyTypesId = collectionTaxonomyTypesId;
    }

    /**
     *      *            @hibernate.property
     *             column="TimestampModified"
     *             length="23"
     *         
     */
    public Date getTimestampModified() {
        return this.timestampModified;
    }
    
    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
    }

    /**
     *      *            @hibernate.property
     *             column="TimestampCreated"
     *             length="23"
     *         
     */
    public Date getTimestampCreated() {
        return this.timestampCreated;
    }
    
    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    /**
     *      *            @hibernate.property
     *             column="LastEditedBy"
     *             length="50"
     *         
     */
    public String getLastEditedBy() {
        return this.lastEditedBy;
    }
    
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="BiologicalObjectTypeID"         
     *         
     */
    public CollectionObjectType getCollectionObjectType() {
        return this.collectionObjectType;
    }
    
    public void setCollectionObjectType(CollectionObjectType collectionObjectType) {
        this.collectionObjectType = collectionObjectType;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="TaxonomyTypeID"         
     *         
     */
    public TaxonomyType getTaxonomyType() {
        return this.taxonomyType;
    }
    
    public void setTaxonomyType(TaxonomyType taxonomyType) {
        this.taxonomyType = taxonomyType;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="CollectionID"  
     * 		   cascade="delete"       
     *         
     */
    public Collection getCollection() {
        return this.collection;
    }
    
    public void setCollection(Collection collection) {
        this.collection = collection;
    }




}