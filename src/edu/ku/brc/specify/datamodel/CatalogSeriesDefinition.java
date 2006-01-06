package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="catalogseriesdefinition"
 *     
 */
public class CatalogSeriesDefinition  implements java.io.Serializable {

    // Fields    

     protected Integer catalogSeriesDefinitionId;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     private CatalogSeries catalogSeries;
     private CollectionObjectType collectionobjecttypes;


    // Constructors

    /** default constructor */
    public CatalogSeriesDefinition() {
    }
    
    /** constructor with id */
    public CatalogSeriesDefinition(Integer catalogSeriesDefinitionId) {
        this.catalogSeriesDefinitionId = catalogSeriesDefinitionId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="CatalogSeriesDefinitionID"
     *         
     */
    public Integer getCatalogSeriesDefinitionId() {
        return this.catalogSeriesDefinitionId;
    }
    
    public void setCatalogSeriesDefinitionId(Integer catalogSeriesDefinitionId) {
        this.catalogSeriesDefinitionId = catalogSeriesDefinitionId;
    }

    /**
     *      *            @hibernate.property
     *             column="Remarks"
     *             length="1073741823"
     *         
     */
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
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
     *            @hibernate.column name="CatalogSeriesID" 
     * 		   cascade="delete"        
     *         
     */
    public CatalogSeries getCatalogSeries() {
        return this.catalogSeries;
    }
    
    public void setCatalogSeries(CatalogSeries catalogSeries) {
        this.catalogSeries = catalogSeries;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="ObjectTypeID" 
     * 		   cascade="none"        
     *         
     */
    public CollectionObjectType getCollectionobjecttypes() {
        return this.collectionobjecttypes;
    }
    
    public void setCollectionobjecttypes(CollectionObjectType collectionobjecttypes) {
        this.collectionobjecttypes = collectionobjecttypes;
    }




}