/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
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
//@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "collection")
@org.hibernate.annotations.Table(appliesTo="collection", indexes =
    {   @Index (name="CollectionNameIDX", columnNames={"CollectionName"}),
        @Index (name="CollectionGuidIDX", columnNames={"GUID"})
    })
public class Collection extends UserGroupScope implements java.io.Serializable, Comparable<Collection>
{
    protected static Collection    currentCollection    = null;
    protected static List<Integer> currentCollectionIds = null;
    
    // Fields
    protected String                     collectionName;
    protected String                     catalogNumFormatName;
    protected String                     code; // Collection Acronym
    protected Boolean                    isEmbeddedCollectingEvent;
    protected String                     regNumber;
    protected String                     description;
    protected String                     remarks;
    
    protected String                     webPortalURI;
    protected String                     webSiteURI;
    protected String                     isaNumber;
    
    // ABCD Schema
    protected String                     kingdomCoverage;
    protected String                     primaryFocus;
    protected String                     collectionType;
    protected String                     primaryPurpose;
    protected String                     preservationMethodType;
    protected String                     developmentStatus;
    protected String                     institutionType;
    protected String                     scope;
    protected String                     dbContentVersion;
    protected Integer                    estimatedSize;
    protected String                     guid;
    
    // Relationships
    protected Discipline                 discipline;
    //protected Set<SpAppResourceDir>      spAppResourceDirs;
    //protected Set<FieldNotebook>         fieldNoteBooks;
    //protected Set<CollectionObject>      collectionObjects;
    protected Set<Agent>                 technicalContacts;
    protected Set<Agent>                 contentContacts;
    protected Set<PrepType>              prepTypes;
    protected Set<PickList>              pickLists;
    
    protected Set<CollectionRelType>     leftSideRelTypes;
    protected Set<CollectionRelType>     rightSideRelTypes;

    protected Set<AutoNumberingScheme>   numberingSchemes;
    
    protected Institution                institutionNetwork;
    
    // Constructors

    /** default constructor */
    public Collection() 
    {
        //
    }

    /** constructor with id */
    public Collection(Integer collectionId) 
    {
        super(collectionId);
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        collectionName         = null;
        catalogNumFormatName   = null;
        code                   = null;
        isEmbeddedCollectingEvent = true;
        regNumber              = null;
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
        scope                  = null;
        dbContentVersion       = null;
        webPortalURI           = null;
        webSiteURI             = null;
        isaNumber              = null;
        estimatedSize          = 0;
        guid                   = null;
        
        technicalContacts = new HashSet<Agent>();
        contentContacts   = new HashSet<Agent>();

        //spAppResourceDirs      = new HashSet<SpAppResourceDir>();
        //collectionObjects      = new HashSet<CollectionObject>();
        //fieldNoteBooks         = new HashSet<FieldNotebook>();
        numberingSchemes       = new HashSet<AutoNumberingScheme>();
        prepTypes              = new HashSet<PrepType>();
        pickLists              = new HashSet<PickList>();

        leftSideRelTypes       = new HashSet<CollectionRelType>();
        rightSideRelTypes      = new HashSet<CollectionRelType>();
        
        institutionNetwork     = null;
        
        setGUID();
    }
    // End Initializer

    /**
     *      * Primary key
     */
    public Integer getCollectionId() 
    {
        return getUserGroupScopeId();
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return getUserGroupScopeId();
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
    	setUserGroupScopeId(collectionId);
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
     * @return the catalogNumFormatName
     */
    @Column(name = "CatalogFormatNumName", unique = false, nullable = false, insertable = true, updatable = true, length = 64)
    public String getCatalogNumFormatName()
    {
        return catalogNumFormatName;
    }

    /**
     * @param catalogNumFormatName the catalogFormatName to set
     */
    public void setCatalogNumFormatName(String catalogNumFormatName)
    {
        this.catalogNumFormatName = catalogNumFormatName;
    }

    /**
     *      * Text Displayed with Catalog numbers. E.g. 'KU'
     */
    @Column(name = "Code", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
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
     * @return the isRegistered
     */
    @Column(name = "RegNumber", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
    public String getRegNumber()
    {
        return regNumber;
    }

    /**
     * @param isRegistered the isRegistered to set
     */
    public void setRegNumber(String regNumber)
    {
        this.regNumber = regNumber;
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

    /**
     * @return the numberingSchemes
     */
    @ManyToMany(cascade = {}, fetch = FetchType.LAZY)
    @JoinTable(name = "autonumsch_coll", 
            joinColumns = { @JoinColumn(name = "CollectionID", unique = false, nullable = false, insertable = true, updatable = false) }, 
            inverseJoinColumns = { @JoinColumn(name = "AutoNumberingSchemeID", unique = false, nullable = false, insertable = true, updatable = false) })
    public Set<AutoNumberingScheme> getNumberingSchemes()
    {
        return numberingSchemes;
    }

    /**
     * @param numberingSchemes the numberingSchemes to set
     */
    public void setNumberingSchemes(Set<AutoNumberingScheme> numberingSchemes)
    {
        this.numberingSchemes = numberingSchemes;
    }
    
    	/**
     * @param schemeType
     * @return
     */
    @Transient
    public AutoNumberingScheme getNumberingSchemesByType(final Integer schemeType)
    {
        for (AutoNumberingScheme scheme : numberingSchemes)
        {
            if (scheme.getTableNumber().equals(schemeType))
            {
                return scheme;
            }
        }
        return null;
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
     * @return the scope
     */
    @Lob
    @Column(name = "Scope", unique = false, nullable = true, insertable = true, updatable = true, length = 2048)
    public String getScope()
    {
        return scope;
    }

    /**
     * @param scope the scope to set
     */
    public void setScope(String scope)
    {
        this.scope = scope;
    }

    /**
     * @return the dbContentVersion
     */
    @Column(name = "DbContentVersion", length = 32)
    public String getDbContentVersion()
    {
        return dbContentVersion;
    }

    /**
     * @param dbContentVersion the dbContentVersion to set
     */
    public void setDbContentVersion(String dbContentVersion)
    {
        this.dbContentVersion = dbContentVersion;
    }

    /**
     * @return the webPortalURI
     */
    @Column(name = "WebPortalURI", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getWebPortalURI()
    {
        return webPortalURI;
    }

    /**
     * @param webPortalURI the webPortalURI to set
     */
    public void setWebPortalURI(String webPortalURI)
    {
        this.webPortalURI = webPortalURI;
    }

    /**
     * @return the webSiteURI
     */
    @Column(name = "WebSiteURI", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getWebSiteURI()
    {
        return webSiteURI;
    }

    /**
     * @param webSiteURI the webSiteURI to set
     */
    public void setWebSiteURI(String webSiteURI)
    {
        this.webSiteURI = webSiteURI;
    }
    
    /**
     * @return the isaNumber
     */
    @Column(name = "IsaNumber", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
    public String getIsaNumber()
    {
        return isaNumber;
    }

    /**
     * @param isaNumber the isaNumber to set
     */
    public void setIsaNumber(String isaNumber)
    {
        this.isaNumber = isaNumber;
    }

    /**
     * @return the estimatedSize
     */
    @Column(name = "EstimatedSize", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getEstimatedSize()
    {
        return estimatedSize;
    }

    /**
     * @param estimatedSize the estimatedSize to set
     */
    public void setEstimatedSize(Integer estimatedSize)
    {
        this.estimatedSize = estimatedSize;
    }

    /**
     *
     */
    @Column(name = "GUID", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getGuid() {
        return this.guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
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
    
    /*
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
    
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "collection")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<FieldNotebook> getFieldNoteBooks()
    {
        return fieldNoteBooks;
    }

    public void setFieldNoteBooks(Set<FieldNotebook> fieldNoteBooks)
    {
        this.fieldNoteBooks = fieldNoteBooks;
    }
   */
    /**
     * @return the technicalContacts
     */
    @OneToMany(cascade = {javax.persistence.CascadeType.ALL}, fetch = FetchType.LAZY, mappedBy = "collTechContact")
    public Set<Agent> getTechnicalContacts()
    {
        return technicalContacts;
    }
    
    /**
     * @return the contentContacts
     */
    @OneToMany(cascade = {javax.persistence.CascadeType.ALL}, fetch = FetchType.LAZY, mappedBy = "collContentContact")
    public Set<Agent> getContentContacts()
    {
        return contentContacts;
    }
 
    /**
     * @param technicalContacts the technicalContacts to set
     */
    public void setTechnicalContacts(Set<Agent> technicalContacts)
    {
        this.technicalContacts = technicalContacts;
    }

    /**
     * @param contentContacts the contentContacts to set
     */
    public void setContentContacts(Set<Agent> contentContacts)
    {
        this.contentContacts = contentContacts;
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
        return collectionName != null && obj != null && obj.collectionName != null ? collectionName.compareTo(obj.collectionName) : 0;
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
    @OneToMany(cascade = {javax.persistence.CascadeType.ALL}, fetch = FetchType.LAZY, mappedBy = "leftSideCollection")
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
    //    @OneToMany(cascade = {javax.persistence.CascadeType.ALL}, fetch = FetchType.LAZY, mappedBy = "leftSideCollection")
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "rightSideCollection")
    @Cascade( {CascadeType.ALL} )
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
    
    /**
     * @return the institutionNetwork
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "InstitutionNetworkID", unique = false, nullable = true, insertable = true, updatable = true)
    public Institution getInstitutionNetwork()
    {
        return institutionNetwork;
    }

    /**
     * @param institutionNetwork the institutionNetwork to set
     */
    public void setInstitutionNetwork(Institution institutionNetwork)
    {
        this.institutionNetwork = institutionNetwork;
    }

    /**
     * Asks the Object to force load and child object. This must be done within a Session. 
     */
    public void forceLoad()
    {
        for (AutoNumberingScheme ans : numberingSchemes) // Force Load of Numbering Schemes
        {
            ans.getTableNumber();
            ans.getCollections();
            ans.getDivisions();
            ans.getDisciplines();
        }
        
        rightSideRelTypes.size();
        leftSideRelTypes.size();
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
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Discipline.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return discipline != null ? discipline.getId() : null;
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
