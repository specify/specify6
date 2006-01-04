package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="CollectionObjectCatalog"
 *     
 */
public class CollectionObjectCatalog  implements java.io.Serializable {

    // Fields    

     protected Integer collectionObjectCatalogId;
     protected Integer subNumber;
     protected String name;
     protected String modifier;
     protected Integer catalogedDate;
     protected String location;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected Short deaccessioned;
     protected Double catalogNumber;
     protected CollectionObject collectionObject;
     protected Set projectCollectionObjects;
     protected Set deaccessionCollectionObjects;
     protected Set loanPhysicalObjects;
     protected Set otherIdentifiers;
     protected CatalogSeries catalogSeries;
     protected Accession accession;
     protected Agent agent;


    // Constructors

    /** default constructor */
    public CollectionObjectCatalog() {
    }
    
    /** constructor with id */
    public CollectionObjectCatalog(Integer collectionObjectCatalogId) {
        this.collectionObjectCatalogId = collectionObjectCatalogId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="CollectionObjectCatalogID"
     *         
     */
    public Integer getCollectionObjectCatalogId() {
        return this.collectionObjectCatalogId;
    }
    
    public void setCollectionObjectCatalogId(Integer collectionObjectCatalogId) {
        this.collectionObjectCatalogId = collectionObjectCatalogId;
    }

    /**
     *      *            @hibernate.property
     *             column="SubNumber"
     *             length="10"
     *         
     */
    public Integer getSubNumber() {
        return this.subNumber;
    }
    
    public void setSubNumber(Integer subNumber) {
        this.subNumber = subNumber;
    }

    /**
     *      *            @hibernate.property
     *             column="Name"
     *             length="50"
     *         
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     *      *            @hibernate.property
     *             column="Modifier"
     *             length="50"
     *         
     */
    public String getModifier() {
        return this.modifier;
    }
    
    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    /**
     *      *            @hibernate.property
     *             column="CatalogedDate"
     *             length="10"
     *         
     */
    public Integer getCatalogedDate() {
        return this.catalogedDate;
    }
    
    public void setCatalogedDate(Integer catalogedDate) {
        this.catalogedDate = catalogedDate;
    }

    /**
     *      *            @hibernate.property
     *             column="Location"
     *             length="50"
     *         
     */
    public String getLocation() {
        return this.location;
    }
    
    public void setLocation(String location) {
        this.location = location;
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
     *      *            @hibernate.property
     *             column="Deaccessioned"
     *             length="5"
     *         
     */
    public Short getDeaccessioned() {
        return this.deaccessioned;
    }
    
    public void setDeaccessioned(Short deaccessioned) {
        this.deaccessioned = deaccessioned;
    }

    /**
     *      *            @hibernate.property
     *             column="CatalogNumber"
     *             length="53"
     *         
     */
    public Double getCatalogNumber() {
        return this.catalogNumber;
    }
    
    public void setCatalogNumber(Double catalogNumber) {
        this.catalogNumber = catalogNumber;
    }

    /**
     *      *            @hibernate.one-to-one
     *             class="edu.ku.brc.specify.datamodel.CollectionObject"
     *             outer-join="auto"
     *             constrained="true"
     *         
     */
    public CollectionObject getCollectionObject() {
        return this.collectionObject;
    }
    
    public void setCollectionObject(CollectionObject collectionObject) {
        this.collectionObject = collectionObject;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="CollectionObjectID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.ProjectCollectionObject"
     *         
     */
    public Set getProjectCollectionObjects() {
        return this.projectCollectionObjects;
    }
    
    public void setProjectCollectionObjects(Set projectCollectionObjects) {
        this.projectCollectionObjects = projectCollectionObjects;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="CollectionObjectID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.DeaccessionCollectionObject"
     *         
     */
    public Set getDeaccessionCollectionObjects() {
        return this.deaccessionCollectionObjects;
    }
    
    public void setDeaccessionCollectionObjects(Set deaccessionCollectionObjects) {
        this.deaccessionCollectionObjects = deaccessionCollectionObjects;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="PhysicalObjectID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.LoanPhysicalObject"
     *         
     */
    public Set getLoanPhysicalObjects() {
        return this.loanPhysicalObjects;
    }
    
    public void setLoanPhysicalObjects(Set loanPhysicalObjects) {
        this.loanPhysicalObjects = loanPhysicalObjects;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="CollectionObjectID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.OtherIdentifier"
     *         
     */
    public Set getOtherIdentifiers() {
        return this.otherIdentifiers;
    }
    
    public void setOtherIdentifiers(Set otherIdentifiers) {
        this.otherIdentifiers = otherIdentifiers;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="CatalogSeriesID"         
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
     *            @hibernate.column name="AccessionID"         
     *         
     */
    public Accession getAccession() {
        return this.accession;
    }
    
    public void setAccession(Accession accession) {
        this.accession = accession;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="CatalogerID"         
     *         
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }




}