package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.Set;




/**
 *        @hibernate.class
 *         table="collectionobject"
 *     
 */
public class CollectionObject  implements java.io.Serializable {

    // Fields    

     protected Integer collectionObjectId;
     protected String fieldNumber;
     protected String description;
     protected String containerType;
     protected Integer containerTypeId;
     protected String text1;
     protected String text2;
     protected Float number1;
     protected Float number2;
     protected Short yesNo1;
     protected Short yesNo2;
     protected Integer count1;
     protected String remarks;
     protected Integer subNumber;
     protected String name;
     protected String modifier;
     protected Integer catalogedDate;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected Short deaccessioned;
     protected String catalogNumber;
     protected CollectingEvent collectingEvent;
     protected Observation observation;
     protected Set soundEventStorages;
     protected Set collectionObjectCitations;
     protected Set bioAttrs;
     protected Set preparations;
     protected Set determinations;
     protected Set projectCollectionObjects;
     protected Set deaccessionCollectionObjects;
     protected Set otherIdentifiers;
     protected CatalogSeries catalogSeries;
     protected Accession accession;
     protected Agent agent;
     private Set loanPhysicalObjects;
     private Set externalFiles;


    // Constructors

    /** default constructor */
    public CollectionObject() {
    }
    
    /** constructor with id */
    public CollectionObject(Integer collectionObjectId) {
        this.collectionObjectId = collectionObjectId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="CollectionObjectID"
     *         
     */
    public Integer getCollectionObjectId() {
        return this.collectionObjectId;
    }
    
    public void setCollectionObjectId(Integer collectionObjectId) {
        this.collectionObjectId = collectionObjectId;
    }

    /**
     *      *            @hibernate.property
     *             column="FieldNumber"
     *             length="50"
     *         
     */
    public String getFieldNumber() {
        return this.fieldNumber;
    }
    
    public void setFieldNumber(String fieldNumber) {
        this.fieldNumber = fieldNumber;
    }

    /**
     *      *            @hibernate.property
     *             column="Description"
     *             length="50"
     *         
     */
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *      *            @hibernate.property
     *             column="ContainerType"
     *             length="50"
     *         
     */
    public String getContainerType() {
        return this.containerType;
    }
    
    public void setContainerType(String containerType) {
        this.containerType = containerType;
    }

    /**
     *      *            @hibernate.property
     *             column="ContainerTypeID"
     *             length="10"
     *         
     */
    public Integer getContainerTypeId() {
        return this.containerTypeId;
    }
    
    public void setContainerTypeId(Integer containerTypeId) {
        this.containerTypeId = containerTypeId;
    }

    /**
     *      *            @hibernate.property
     *             column="Text1"
     *             length="300"
     *         
     */
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      *            @hibernate.property
     *             column="Text2"
     *             length="300"
     *         
     */
    public String getText2() {
        return this.text2;
    }
    
    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      *            @hibernate.property
     *             column="Number1"
     *             length="24"
     *         
     */
    public Float getNumber1() {
        return this.number1;
    }
    
    public void setNumber1(Float number1) {
        this.number1 = number1;
    }

    /**
     *      *            @hibernate.property
     *             column="Number2"
     *             length="24"
     *         
     */
    public Float getNumber2() {
        return this.number2;
    }
    
    public void setNumber2(Float number2) {
        this.number2 = number2;
    }

    /**
     *      *            @hibernate.property
     *             column="YesNo1"
     *             length="5"
     *         
     */
    public Short getYesNo1() {
        return this.yesNo1;
    }
    
    public void setYesNo1(Short yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      *            @hibernate.property
     *             column="YesNo2"
     *             length="5"
     *         
     */
    public Short getYesNo2() {
        return this.yesNo2;
    }
    
    public void setYesNo2(Short yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     *      *            @hibernate.property
     *             column="Count1"
     *             length="10"
     *         
     */
    public Integer getCount1() {
        return this.count1;
    }
    
    public void setCount1(Integer count1) {
        this.count1 = count1;
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
     *             length="32"
     *         
     */
    public String getCatalogNumber() {
        return this.catalogNumber;
    }
    
    public void setCatalogNumber(String catalogNumber) {
        this.catalogNumber = catalogNumber;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     * 			
     *            @hibernate.column name="CollectingEventID"
     *         
     */
    public CollectingEvent getCollectingEvent() {
        return this.collectingEvent;
    }
    
    public void setCollectingEvent(CollectingEvent collectingEvent) {
        this.collectingEvent = collectingEvent;
    }

    /**
     *      *            @hibernate.one-to-one
     *            @hibernate.column name="ObservationID"         
     *         
     */
    public Observation getObservation() {
        return this.observation;
    }
    
    public void setObservation(Observation observation) {
        this.observation = observation;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="SoundRecordingID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.SoundEventStorage"
     *         
     */
    public Set getSoundEventStorages() {
        return this.soundEventStorages;
    }
    
    public void setSoundEventStorages(Set soundEventStorages) {
        this.soundEventStorages = soundEventStorages;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="BiologicalObjectID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.CollectionObjectCitation"
     *         
     */
    public Set getCollectionObjectCitations() {
        return this.collectionObjectCitations;
    }
    
    public void setCollectionObjectCitations(Set collectionObjectCitations) {
        this.collectionObjectCitations = collectionObjectCitations;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="BioAttrsID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.BioAttrs"
     *         
     */
    public Set getBioAttrs() {
        return this.bioAttrs;
    }
    
    public void setBioAttrs(Set bioAttrs) {
        this.bioAttrs = bioAttrs;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="CollectionObjID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Preparation"
     *         
     */
    public Set getPreparations() {
        return this.preparations;
    }
    
    public void setPreparations(Set preparations) {
        this.preparations = preparations;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="BiologicalObjectID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Determination"
     *         
     */
    public Set getDeterminations() {
        return this.determinations;
    }
    
    public void setDeterminations(Set determinations) {
        this.determinations = determinations;
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
     *             cascade="delete"
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
     * 
     */
    public Set getExternalFiles() {
        return this.externalFiles;
    }
    
    public void setExternalFiles(Set externalFiles) {
        this.externalFiles = externalFiles;
    }




}