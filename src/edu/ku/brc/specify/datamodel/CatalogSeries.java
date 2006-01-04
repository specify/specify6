package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="CatalogSeries"
 *     
 */
public class CatalogSeries  implements java.io.Serializable {

    // Fields    

     protected Integer catalogSeriesId;
     protected String seriesName;
     protected String catalogSeriesPrefix;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     private Set catalogSeriesDefinitions;
     private Set collectionObjectCatalogs;
     private Collection collection;


    // Constructors

    /** default constructor */
    public CatalogSeries() {
    }
    
    /** constructor with id */
    public CatalogSeries(Integer catalogSeriesId) {
        this.catalogSeriesId = catalogSeriesId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="CatalogSeriesID"
     *         
     */
    public Integer getCatalogSeriesId() {
        return this.catalogSeriesId;
    }
    
    public void setCatalogSeriesId(Integer catalogSeriesId) {
        this.catalogSeriesId = catalogSeriesId;
    }

    /**
     *      *            @hibernate.property
     *             column="SeriesName"
     *             length="50"
     *         
     */
    public String getSeriesName() {
        return this.seriesName;
    }
    
    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    /**
     *      *            @hibernate.property
     *             column="CatalogSeriesPrefix"
     *             length="50"
     *         
     */
    public String getCatalogSeriesPrefix() {
        return this.catalogSeriesPrefix;
    }
    
    public void setCatalogSeriesPrefix(String catalogSeriesPrefix) {
        this.catalogSeriesPrefix = catalogSeriesPrefix;
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
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="CatalogSeriesID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.CatalogSeriesDefinition"
     *         
     */
    public Set getCatalogSeriesDefinitions() {
        return this.catalogSeriesDefinitions;
    }
    
    public void setCatalogSeriesDefinitions(Set catalogSeriesDefinitions) {
        this.catalogSeriesDefinitions = catalogSeriesDefinitions;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="CatalogSeriesID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.CollectionObj"
     *         
     */
    public Set getCollectionObjectCatalogs() {
        return this.collectionObjectCatalogs;
    }
    
    public void setCollectionObjectCatalogs(Set collectionObjectCatalogs) {
        this.collectionObjectCatalogs = collectionObjectCatalogs;
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