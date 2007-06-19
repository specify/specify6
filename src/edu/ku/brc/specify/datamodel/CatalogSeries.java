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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@Table(name = "catalogseries")
public class CatalogSeries extends DataModelObjBase implements java.io.Serializable, Comparable<CatalogSeries>
{
    protected static CatalogSeries currentCatalogSeries = null;
    
    // Fields
    protected Long                       catalogSeriesId;
    protected String                     seriesName;
    protected String                     catalogSeriesPrefix;
    protected String                     remarks;
    protected CollectionObjDef           collectionObjDef;
    protected Set<AppResourceDefault>    appResourceDefaults;

    // Constructors

    /** default constructor */
    public CatalogSeries() {
        //
    }

    /** constructor with id */
    public CatalogSeries(Long catalogSeriesId) {
        this.catalogSeriesId = catalogSeriesId;
    }

    public static CatalogSeries getCurrentCatalogSeries()
    {
        return currentCatalogSeries;
    }

    public static void setCurrentCatalogSeries(final CatalogSeries currentCatalogSeries)
    {
        CatalogSeries.currentCatalogSeries = currentCatalogSeries;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        catalogSeriesId       = null;
        seriesName            = null;
        catalogSeriesPrefix   = null;
        remarks               = null;
        collectionObjDef      = null;
        appResourceDefaults   = new HashSet<AppResourceDefault>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "CatalogSeriesID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getCatalogSeriesId() {
        return this.catalogSeriesId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.catalogSeriesId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return CatalogSeries.class;
    }

    public void setCatalogSeriesId(Long catalogSeriesId) {
        this.catalogSeriesId = catalogSeriesId;
    }

//    /**
//     *
//     */
//    public Boolean getIsTissueSeries() {
//        return this.isTissueSeries;
//    }
//
//    public void setIsTissueSeries(Boolean isTissueSeries) {
//        this.isTissueSeries = isTissueSeries;
//    }

    /**
     *      * Textual name for Catalog series. E.g. Main specimen collection
     */
    @Column(name = "SeriesName", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getSeriesName() {
        return this.seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    /**
     *      * Text Displayed with Catalog numbers. E.g. 'KU'
     */
    @Column(name = "CatalogSeriesPrefix", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getCatalogSeriesPrefix() {
        return this.catalogSeriesPrefix;
    }

    public void setCatalogSeriesPrefix(String catalogSeriesPrefix) {
        this.catalogSeriesPrefix = catalogSeriesPrefix;
    }

    /**
     *
     */
    @Lob
    @Column(name="Remarks", unique=false, nullable=true, updatable=true, insertable=true)
    public String getRemarks() {
        return this.remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }


    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CollectionObjDefID", unique = false, nullable = true, insertable = true, updatable = true)
    public CollectionObjDef getCollectionObjDef() {
        return this.collectionObjDef;
    }

    public void setCollectionObjDef(CollectionObjDef collectionObjDef) {
        this.collectionObjDef = collectionObjDef;
    }
    
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "catalogSeries")
    public Set<AppResourceDefault> getAppResourceDefaults()
    {
        return appResourceDefaults;
    }

    public void setAppResourceDefaults(Set<AppResourceDefault> appResourceDefaults)
    {
        this.appResourceDefaults = appResourceDefaults;
    }

    @Override
    public String toString()
    {
        return seriesName;
    }
    
    public int compareTo(CatalogSeries obj)
    {
        return seriesName.compareTo(obj.seriesName);
    }


    // Add Methods

    // Delete Add Methods
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        return seriesName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public int getTableId()
    {
        return getClassTableId();
    }
    
    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 23;
    }

}
