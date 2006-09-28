/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.datamodel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**

 */
public class CatalogSeries extends DataModelObjBase implements java.io.Serializable, Comparable<CatalogSeries>
{
    protected static List<CatalogSeries> currentCatalogSeries = new ArrayList<CatalogSeries>();
    
    // Fields

     protected Long catalogSeriesId;
     protected Boolean isTissueSeries;
     protected String seriesName;
     protected String catalogSeriesPrefix;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     protected Set<CollectionObjDef> collectionObjDefItems;
     protected CatalogSeries tissue;
     protected Set<AppResourceDefault> appResourceDefaults;
     

    // Constructors

    /** default constructor */
    public CatalogSeries() {
    }

    /** constructor with id */
    public CatalogSeries(Long catalogSeriesId) {
        this.catalogSeriesId = catalogSeriesId;
    }

    public static List<CatalogSeries> getCurrentCatalogSeries()
    {
        return currentCatalogSeries;
    }

    public static void setCurrentCatalogSeries(List<CatalogSeries> currentCatalogSeries)
    {
        if (currentCatalogSeries != CatalogSeries.currentCatalogSeries)
        {
            CatalogSeries.currentCatalogSeries.clear();
        
            if (currentCatalogSeries != null)
            {
                CatalogSeries.currentCatalogSeries.addAll(currentCatalogSeries);
            }
        }
    }

    // Initializer
    public void initialize()
    {
        catalogSeriesId = null;
        isTissueSeries = null;
        seriesName = null;
        catalogSeriesPrefix = null;
        remarks = null;
        timestampModified = null;
        timestampCreated = new Date();
        lastEditedBy = null;
        collectionObjDefItems = new HashSet<CollectionObjDef>();
        tissue = null;
        appResourceDefaults = new HashSet<AppResourceDefault>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    public Long getCatalogSeriesId() {
        return this.catalogSeriesId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.catalogSeriesId;
    }

    public void setCatalogSeriesId(Long catalogSeriesId) {
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
    
    public Set<AppResourceDefault> getAppResourceDefaults()
    {
        return appResourceDefaults;
    }

    public void setAppResourceDefaults(Set<AppResourceDefault> appResourceDefaults)
    {
        this.appResourceDefaults = appResourceDefaults;
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
