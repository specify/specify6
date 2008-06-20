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
import java.util.List;
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
import org.hibernate.annotations.Index;

import edu.ku.brc.af.core.AppContextMgr;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "collection")
@org.hibernate.annotations.Table(appliesTo="collection", indexes =
    {   @Index (name="CollectionNameIDX", columnNames={"CollectionName"})
    })
public class Collection extends DataModelObjBase implements java.io.Serializable, Comparable<Collection>
{
    protected static Collection    currentCollection    = null;
    protected static List<Integer> currentCollectionIds = null;
    
    // Fields
    protected Integer                    collectionId;
    protected String                     collectionName;
    protected String                     collectionPrefix; // Collection Acronym
    protected Boolean                    isEmbeddedCollectingEvent;
    protected String                     description;
    protected String                     remarks;
    
    // TDWG Fields
    protected String                     kingdomCoverage;
    protected String                     primaryFocus;
    protected String                     collectionType;
    protected String                     primaryPurpose;
    protected String                     preservationMethodType;
    protected String                     developmentStatus;
    protected String                     institutionType;
    
    // Relationships
    protected Discipline                 discipline;
    protected Set<SpAppResourceDir>      spAppResourceDirs;
    protected Set<FieldNotebook>         fieldNoteBooks;
    protected Set<CollectionObject>      collectionObjects;
    protected CatalogNumberingScheme     catalogNumberingScheme;
    protected Agent                      contactAgent;
    protected Agent                      curatorAgent;
    protected Set<PrepType>              prepTypes;
    protected Set<PickList>              pickLists;
    
    protected Set<CollectionRelType>     leftSideRelTypes;
    protected Set<CollectionRelType>     rightSideRelTypes;



    // Constructors

    /** default constructor */
    public Collection() 
    {
        //
    }

    /** constructor with id */
    public Collection(Integer collectionId) 
    {
        this.collectionId = collectionId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        collectionId           = null;
        collectionName         = null;
        collectionPrefix       = null;
        isEmbeddedCollectingEvent = true;
        description            = null;
        remarks                = null;
        discipline             = AppContextMgr.getInstance().getClassObject(Discipline.class);
        kingdomCoverage        = null;
        primaryFocus           = null;
        collectionType         = null;
        primaryPurpose         = null;
        preservationMethodType = null;
        developmentStatus      = null;
        institutionType        = null;
        spAppResourceDirs      = new HashSet<SpAppResourceDir>();
        collectionObjects      = new HashSet<CollectionObject>();
        fieldNoteBooks         = new HashSet<FieldNotebook>();
        catalogNumberingScheme = null;
        contactAgent           = null;
        curatorAgent           = null;
        prepTypes              = new HashSet<PrepType>();
        pickLists              = new HashSet<PickList>();

        leftSideRelTypes       = new HashSet<CollectionRelType>();
        rightSideRelTypes      = new HashSet<CollectionRelType>();
    }
    // End Initializer

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "CollectionID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getCollectionId() 
    {
        return this.collectionId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
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

    public void setCollectionId(Integer collectionId) {
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
     * @return the description
     */
    @Lob
    @Column(name = "Description", unique = false, nullable = true, insertable = true, updatable = true, length = 2048)
    public String getDescription()
    {
        return description;
    }

    /**
     * @return the isEmbeddedCollectingEvent
     */
    @Column(name = "IsEmbeddedCollectingEvent", unique = false, nullable = false, insertable = true, updatable = true)
    public Boolean getIsEmbeddedCollectingEvent()
    {
        return isEmbeddedCollectingEvent;
    }

    /**
     * @param isEmbeddedCollectingEvent the isEmbeddedCollectingEvent to set
     */
    public void setIsEmbeddedCollectingEvent(Boolean isEmbeddedCollectingEvent)
    {
        this.isEmbeddedCollectingEvent = isEmbeddedCollectingEvent;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     *
     */
    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks() {
        return this.remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }


    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
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
     * @return the kingdomCoverage
     */
    @Column(name = "KingdomCoverage", length = 32)
    public String getKingdomCoverage()
    {
        return kingdomCoverage;
    }

    /**
     * @param kingdomCoverage the kingdomCoverage to set
     */
    public void setKingdomCoverage(String kingdomCoverage)
    {
        this.kingdomCoverage = kingdomCoverage;
    }

    /**
     * @return the primaryFocus
     */
    @Column(name = "PrimaryFocus", length = 32)
    public String getPrimaryFocus()
    {
        return primaryFocus;
    }

    /**
     * @param primaryFocus the primaryFocus to set
     */
    public void setPrimaryFocus(String primaryFocus)
    {
        this.primaryFocus = primaryFocus;
    }

    /**
     * @return the collectionType
     */
    @Column(name = "CollectionType", length = 32)
    public String getCollectionType()
    {
        return collectionType;
    }

    /**
     * @param collectionType the collectionType to set
     */
    public void setCollectionType(String collectionType)
    {
        this.collectionType = collectionType;
    }

    /**
     * @return the primaryPurpose
     */
    @Column(name = "PrimaryPurpose", length = 32)
    public String getPrimaryPurpose()
    {
        return primaryPurpose;
    }

    /**
     * @param primaryPurpose the primaryPurpose to set
     */
    public void setPrimaryPurpose(String primaryPurpose)
    {
        this.primaryPurpose = primaryPurpose;
    }

    /**
     * @return the preservationMethodType
     */
    @Column(name = "PreservationMethodType", length = 32)
    public String getPreservationMethodType()
    {
        return preservationMethodType;
    }

    /**
     * @param preservationMethodType the preservationMethodType to set
     */
    public void setPreservationMethodType(String preservationMethodType)
    {
        this.preservationMethodType = preservationMethodType;
    }

    /**
     * @return the developmentStatus
     */
    @Column(name = "DevelopmentStatus", length = 32)
    public String getDevelopmentStatus()
    {
        return developmentStatus;
    }

    /**
     * @param developmentStatus the developmentStatus to set
     */
    public void setDevelopmentStatus(String developmentStatus)
    {
        this.developmentStatus = developmentStatus;
    }

    /**
     * @return the institutionType
     */
    @Column(name = "InstitutionType", length = 32)
    public String getInstitutionType()
    {
        return institutionType;
    }

    /**
     * @param institutionType the institutionType to set
     */
    public void setInstitutionType(String institutionType)
    {
        this.institutionType = institutionType;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "DisciplineID", unique = false, nullable = false, insertable = true, updatable = true)
    public Discipline getDiscipline() {
        return this.discipline;
    }

    public void setDiscipline(Discipline discipline) {
        this.discipline = discipline;
    }
    
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "collection")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<SpAppResourceDir> getSpAppResourceDirs()
    {
        return spAppResourceDirs;
    }

    public void setSpAppResourceDirs(Set<SpAppResourceDir> spAppResourceDirs)
    {
        this.spAppResourceDirs = spAppResourceDirs;
    }

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "collection")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<CollectionObject> getCollectionObjects()
    {
        return collectionObjects;
    }

    public void setCollectionObjects(Set<CollectionObject> collectionObjects)
    {
        this.collectionObjects = collectionObjects;
    }
    
    /**
     * @return the fieldNoteBooks
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "collection")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<FieldNotebook> getFieldNoteBooks()
    {
        return fieldNoteBooks;
    }

    /**
     * @param fieldNoteBooks the fieldNoteBooks to set
     */
    public void setFieldNoteBooks(Set<FieldNotebook> fieldNoteBooks)
    {
        this.fieldNoteBooks = fieldNoteBooks;
    }

    /**
     * @return the contactAgent
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ContactID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getContactAgent()
    {
        return contactAgent;
    }

    /**
     * @param contactAgent the contactAgent to set
     */
    public void setContactAgent(Agent contactAgent)
    {
        this.contactAgent = contactAgent;
    }

    /**
     * @return the curatorAgent
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CuratorID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getCuratorAgent()
    {
        return curatorAgent;
    }

    /**
     * @param curatorAgent the curatorAgent to set
     */
    public void setCuratorAgent(Agent curatorAgent)
    {
        this.curatorAgent = curatorAgent;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return collectionName;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Collection obj)
    {
        return collectionName.compareTo(obj.collectionName);
    }

    /**
     * @return the prepTypes
     */
    @OneToMany(mappedBy="collection")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<PrepType> getPrepTypes()
    {
        return prepTypes;
    }

    /**
     * @param prepTypes the prepTypes to set
     */
    public void setPrepTypes(Set<PrepType> prepTypes)
    {
        this.prepTypes = prepTypes;
    }

    /**
     * @return the leftSideRelTypes
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "leftSideCollection")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL })
    public Set<CollectionRelType> getLeftSideRelTypes()
    {
        return leftSideRelTypes;
    }

    /**
     * @param leftSideRelTypes the leftSideRelTypes to set
     */
    public void setLeftSideRelTypes(Set<CollectionRelType> leftSideRelTypes)
    {
        this.leftSideRelTypes = leftSideRelTypes;
    }

    /**
     * @return the rightSideRelTypes
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "rightSideCollection")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL })
    public Set<CollectionRelType> getRightSideRelTypes()
    {
        return rightSideRelTypes;
    }

    /**
     * @param rightSideRelTypes the rightSideRelTypes to set
     */
    public void setRightSideRelTypes(Set<CollectionRelType> rightSideRelTypes)
    {
        this.rightSideRelTypes = rightSideRelTypes;
    }

    /**
     * @return the pickLists
     */
    @OneToMany(mappedBy="collection")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<PickList> getPickLists()
    {
        return pickLists;
    }

    /**
     * @param pickLists the pickLists to set
     */
    public void setPickLists(Set<PickList> pickLists)
    {
        this.pickLists = pickLists;
    }

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
