package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.Set;




/**
 *        @hibernate.class
 *         table="catalogseries"
 *     
 */
public class CatalogSeries  implements java.io.Serializable {

    // Fields    

     protected Integer catalogSeriesId;
     protected Boolean isTissueSeries;
     protected String seriesName;
     protected String catalogSeriesPrefix;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     private Set collectionObjDefItems;
     private CatalogSeries tissue;


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
     *             column="IsTissueSeries"
     *         
     */
    public Boolean getIsTissueSeries() {
        return this.isTissueSeries;
    }
    
    public void setIsTissueSeries(Boolean isTissueSeries) {
        this.isTissueSeries = isTissueSeries;
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
     *             not-null="true"
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
     *             update="false"
     *             not-null="true"
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
     * 
     */
    public Set getCollectionObjDefItems() {
        return this.collectionObjDefItems;
    }
    
    public void setCollectionObjDefItems(Set collectionObjDefItems) {
        this.collectionObjDefItems = collectionObjDefItems;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="TissueID"         
     *         
     */
    public CatalogSeries getTissue() {
        return this.tissue;
    }
    
    public void setTissue(CatalogSeries tissue) {
        this.tissue = tissue;
    }




}