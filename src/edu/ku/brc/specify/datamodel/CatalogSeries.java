package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.Set;




/**

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
     *      * Primary key
     */
    public Integer getCatalogSeriesId() {
        return this.catalogSeriesId;
    }
    
    public void setCatalogSeriesId(Integer catalogSeriesId) {
        this.catalogSeriesId = catalogSeriesId;
    }

    /**
     * 
     */
    public Boolean getIsTissueSeries() {
        return this.isTissueSeries;
    }
    
    public void setIsTissueSeries(Boolean isTissueSeries) {
        this.isTissueSeries = isTissueSeries;
    }

    /**
     *      * Textual name for Catalog series. E.g. Main specimen collection
     */
    public String getSeriesName() {
        return this.seriesName;
    }
    
    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    /**
     *      * Text Displayed with Catalog numbers. E.g. 'KU'
     */
    public String getCatalogSeriesPrefix() {
        return this.catalogSeriesPrefix;
    }
    
    public void setCatalogSeriesPrefix(String catalogSeriesPrefix) {
        this.catalogSeriesPrefix = catalogSeriesPrefix;
    }

    /**
     * 
     */
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * 
     */
    public Date getTimestampModified() {
        return this.timestampModified;
    }
    
    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
    }

    /**
     * 
     */
    public Date getTimestampCreated() {
        return this.timestampCreated;
    }
    
    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    /**
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
     * 
     */
    public CatalogSeries getTissue() {
        return this.tissue;
    }
    
    public void setTissue(CatalogSeries tissue) {
        this.tissue = tissue;
    }




}