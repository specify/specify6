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

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "collection")
public class Collection extends DataModelObjBase implements java.io.Serializable, Comparable<Collection>
{
    protected static Collection currentCollection = null;
    
    // Fields
    protected Long                       collectionId;
    protected String                     collectionName;
    protected String                     collectionPrefix;
    protected String                     remarks;
    protected CollectionType             collectionType;
    protected Set<AppResourceDefault>    appResourceDefaults;
    protected CatalogNumberingScheme     catalogNumberingScheme;
    
    // Constructors

    /** default constructor */
    public Collection() {
        //
    }

    /** constructor with id */
    public Collection(Long collectionId) {
        this.collectionId = collectionId;
    }

    public static Collection getCurrentCollection()
    {
        return currentCollection;
    }

    public static void setCurrentCollection(final Collection currentCollection)
    {
        Collection.currentCollection = currentCollection;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        collectionId           = null;
        collectionName         = null;
        collectionPrefix       = null;
        remarks                = null;
        collectionType       = null;
        appResourceDefaults    = new HashSet<AppResourceDefault>();
        catalogNumberingScheme = null;
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "CollectionID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getCollectionId() {
        return this.collectionId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.collectionId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Collection.class;
    }

    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
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
    @Column(name = "CollectionName", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getCollectionName() {
        return this.collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    /**
     *      * Text Displayed with Catalog numbers. E.g. 'KU'
     */
    @Column(name = "CollectionPrefix", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getCollectionPrefix() {
        return this.collectionPrefix;
    }

    public void setCollectionPrefix(String collectionPrefix) {
        this.collectionPrefix = collectionPrefix;
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


    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "CatalogNumberingSchemeID", unique = false, nullable = false, insertable = true, updatable = true)
    public CatalogNumberingScheme getCatalogNumberingScheme()
    {
        return catalogNumberingScheme;
    }

    public void setCatalogNumberingScheme(CatalogNumberingScheme catalogNumberingScheme)
    {
        this.catalogNumberingScheme = catalogNumberingScheme;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CollectionTypeID", unique = false, nullable = false, insertable = true, updatable = true)
    public CollectionType getCollectionType() {
        return this.collectionType;
    }

    public void setCollectionType(CollectionType collectionType) {
        this.collectionType = collectionType;
    }
    
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "collection")
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
        return collectionName;
    }
    
    public int compareTo(Collection obj)
    {
        return collectionName.compareTo(obj.collectionName);
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
        return collectionName;
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
