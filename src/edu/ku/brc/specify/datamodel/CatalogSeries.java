package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**

 */
public class CatalogSeries  implements java.io.Serializable, Comparable<CatalogSeries>
{
    protected static CatalogSeries currentCatalogSeries = null;
    
    // Fields

     protected Integer catalogSeriesId;
     protected Boolean isTissueSeries;
     protected String seriesName;
     protected String catalogSeriesPrefix;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     protected Set<CollectionObjDef> collectionObjDefItems;
     protected CatalogSeries tissue;


    // Constructors

    /** default constructor */
    public CatalogSeries() {
    }

    /** constructor with id */
    public CatalogSeries(Integer catalogSeriesId) {
        this.catalogSeriesId = catalogSeriesId;
    }

    public static CatalogSeries getCurrentCatalogSeries()
    {
        return currentCatalogSeries;
    }

    public static void setCurrentCatalogSeries(CatalogSeries currentCatalogSeries)
    {
        CatalogSeries.currentCatalogSeries = currentCatalogSeries;
    }

    // Initializer
    public void initialize()
    {
        catalogSeriesId = null;
        isTissueSeries = null;
        seriesName = null;
        catalogSeriesPrefix = null;
        remarks = null;
        timestampModified = new Date();
        timestampCreated = new Date();
        lastEditedBy = null;
        collectionObjDefItems = new HashSet<CollectionObjDef>();
        tissue = null;
    }
    // End Initializer

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
    public Set<CollectionObjDef> getCollectionObjDefItems() {
        return this.collectionObjDefItems;
    }

    public void setCollectionObjDefItems(Set<CollectionObjDef> collectionObjDefItems) {
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


    
    public String toString()
    {
        return seriesName;
    }
    
    public int compareTo(CatalogSeries obj)
    {
        return seriesName.compareTo(obj.seriesName);
    }


    // Add Methods

    public void addCollectionObjDefItems(final CollectionObjDef collectionObjDefItem)
    {
        this.collectionObjDefItems.add(collectionObjDefItem);
        collectionObjDefItem.getCatalogSeries().add(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeCollectionObjDefItems(final CollectionObjDef collectionObjDefItem)
    {
        this.collectionObjDefItems.remove(collectionObjDefItem);
        collectionObjDefItem.getCatalogSeries().remove(this);
    }

    // Delete Add Methods

}
