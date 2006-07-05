package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;




/**

 */
public class CollectionObject  implements java.io.Serializable {

    // Fields

     protected Integer collectionObjectId;
     protected String fieldNumber;
     protected String description;
     protected String text1;
     protected String text2;
     protected Float number1;
     protected Float number2;
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     protected Integer countAmt;
     protected String remarks;
     protected String name;
     protected String modifier;
     protected Calendar catalogedDate;
     protected String catalogedDateVerbatim;
     protected String guid;
     protected String altCatalogNumber;
     protected Integer groupPermittedToView;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected Boolean deaccessioned;
     protected Float catalogNumber;
     protected CollectingEvent collectingEvent;
     protected ContainerItem containerItem;
     protected Set<CollectionObjectCitation> collectionObjectCitations;
     protected Set<AttributeIFace> attrs;
     protected Set<Preparation> preparations;
     protected Set<Determination> determinations;
     protected CollectionObjDef collectionObjDef;
     protected Set<ProjectCollectionObject> projectCollectionObjects;
     protected Set<DeaccessionCollectionObject> deaccessionCollectionObjects;
     protected Set<OtherIdentifier> otherIdentifiers;
     protected CatalogSeries catalogSeries;
     protected Accession accession;
     protected Agent cataloger;
     protected Set<ExternalResource> externalResources;
     protected Container container;


    // Constructors

    /** default constructor */
    public CollectionObject() {
    }

    /** constructor with id */
    public CollectionObject(Integer collectionObjectId) {
        this.collectionObjectId = collectionObjectId;
    }




    // Initializer
    public void initialize()
    {
        collectionObjectId = null;
        fieldNumber = null;
        description = null;
        text1 = null;
        text2 = null;
        number1 = null;
        number2 = null;
        yesNo1 = null;
        yesNo2 = null;
        countAmt = null;
        remarks = null;
        name = null;
        modifier = null;
        catalogedDate = null;
        catalogedDateVerbatim = null;
        guid = null;
        altCatalogNumber = null;
        groupPermittedToView = null;
        timestampCreated = new Date();
        timestampModified = new Date();
        lastEditedBy = null;
        deaccessioned = null;
        catalogNumber = null;
        collectingEvent = null;
        containerItem = null;
        collectionObjectCitations = new HashSet<CollectionObjectCitation>();
        attrs = new HashSet<AttributeIFace>();
        preparations = new HashSet<Preparation>();
        determinations = new HashSet<Determination>();
        collectionObjDef = null;
        projectCollectionObjects = new HashSet<ProjectCollectionObject>();
        deaccessionCollectionObjects = new HashSet<DeaccessionCollectionObject>();
        otherIdentifiers = new HashSet<OtherIdentifier>();
        catalogSeries = null;
        accession = null;
        cataloger = null;
        externalResources = new HashSet<ExternalResource>();
        container = null;
    }
    // End Initializer
    
    public void initForSearch()
    {
        catalogSeries = new CatalogSeries();
        catalogSeries.initialize();
        
        accession = new Accession();
        accession.initialize();
        
        cataloger  = new Agent();
        cataloger.initialize();
    }

    // Property accessors

    /**
     *
     */
    public Integer getCollectionObjectId() {
        return this.collectionObjectId;
    }

    public void setCollectionObjectId(Integer collectionObjectId) {
        this.collectionObjectId = collectionObjectId;
    }

    /**
     *      * BiologicalObject (Bird, Fish, etc)
     */
    public String getFieldNumber() {
        return this.fieldNumber;
    }

    public void setFieldNumber(String fieldNumber) {
        this.fieldNumber = fieldNumber;
    }

    /**
     *      * Image, Sound, Preparation, Container(Container Label?)
     */
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *      * User definable
     */
    public String getText1() {
        return this.text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    public String getText2() {
        return this.text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      * User definable
     */
    public Float getNumber1() {
        return this.number1;
    }

    public void setNumber1(Float number1) {
        this.number1 = number1;
    }

    /**
     *      * User definable
     */
    public Float getNumber2() {
        return this.number2;
    }

    public void setNumber2(Float number2) {
        this.number2 = number2;
    }

    /**
     *      * User definable
     */
    public Boolean getYesNo1() {
        return this.yesNo1;
    }

    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      * User definable
     */
    public Boolean getYesNo2() {
        return this.yesNo2;
    }

    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     *
     */
    public Integer getCountAmt() {
        return this.countAmt;
    }

    public void setCountAmt(Integer countAmt) {
        this.countAmt = countAmt;
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
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     */
    public String getModifier() {
        return this.modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    /**
     *
     */
    public Calendar getCatalogedDate() {
        return this.catalogedDate;
    }

    public void setCatalogedDate(Calendar catalogedDate) {
        this.catalogedDate = catalogedDate;
    }

    /**
     *
     */
    public String getCatalogedDateVerbatim() {
        return this.catalogedDateVerbatim;
    }

    public void setCatalogedDateVerbatim(String catalogedDateVerbatim) {
        this.catalogedDateVerbatim = catalogedDateVerbatim;
    }

    /**
     *
     */
    public String getGuid() {
        return this.guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     *
     */
    public String getAltCatalogNumber() {
        return this.altCatalogNumber;
    }

    public void setAltCatalogNumber(String altCatalogNumber) {
        this.altCatalogNumber = altCatalogNumber;
    }

    /**
     *
     */
    public Integer getGroupPermittedToView() {
        return this.groupPermittedToView;
    }

    public void setGroupPermittedToView(Integer groupPermittedToView) {
        this.groupPermittedToView = groupPermittedToView;
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
    public Date getTimestampModified() {
        return this.timestampModified;
    }

    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
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
    public Boolean getDeaccessioned() {
        return this.deaccessioned;
    }

    public void setDeaccessioned(Boolean deaccessioned) {
        this.deaccessioned = deaccessioned;
    }

    /**
     *
     */
    public Float getCatalogNumber() {
        return this.catalogNumber;
    }

    public void setCatalogNumber(Float catalogNumber) {
        this.catalogNumber = catalogNumber;
    }

    /**
     *      * BiologicalObject (Bird, Fish, etc)
     */
    public CollectingEvent getCollectingEvent() {
        return this.collectingEvent;
    }

    public void setCollectingEvent(CollectingEvent collectingEvent) {
        this.collectingEvent = collectingEvent;
    }

    /**
     *
     */
    public ContainerItem getContainerItem() {
        return this.containerItem;
    }

    public void setContainerItem(ContainerItem containerItem) {
        this.containerItem = containerItem;
    }

    /**
     *
     */
    public Set<CollectionObjectCitation> getCollectionObjectCitations() {
        return this.collectionObjectCitations;
    }

    public void setCollectionObjectCitations(Set<CollectionObjectCitation> collectionObjectCitations) {
        this.collectionObjectCitations = collectionObjectCitations;
    }

    /**
     *
     */
    public Set<AttributeIFace> getAttrs() {
        return this.attrs;
    }

    public void setAttrs(Set<AttributeIFace> attrs) {
        this.attrs = attrs;
    }

    /**
     *
     */
    public Set<Preparation> getPreparations() {
        return this.preparations;
    }

    public void setPreparations(Set<Preparation> preparations) {
        this.preparations = preparations;
    }

    /**
     *
     */
    public Set<Determination> getDeterminations() {
        return this.determinations;
    }

    public void setDeterminations(Set<Determination> determinations) {
        this.determinations = determinations;
    }

    /**
     *
     */
    public CollectionObjDef getCollectionObjDef() {
        return this.collectionObjDef;
    }

    public void setCollectionObjDef(CollectionObjDef collectionObjDef) {
        this.collectionObjDef = collectionObjDef;
    }

    /**
     *
     */
    public Set<ProjectCollectionObject> getProjectCollectionObjects() {
        return this.projectCollectionObjects;
    }

    public void setProjectCollectionObjects(Set<ProjectCollectionObject> projectCollectionObjects) {
        this.projectCollectionObjects = projectCollectionObjects;
    }

    /**
     *
     */
    public Set<DeaccessionCollectionObject> getDeaccessionCollectionObjects() {
        return this.deaccessionCollectionObjects;
    }

    public void setDeaccessionCollectionObjects(Set<DeaccessionCollectionObject> deaccessionCollectionObjects) {
        this.deaccessionCollectionObjects = deaccessionCollectionObjects;
    }

    /**
     *
     */
    public Set<OtherIdentifier> getOtherIdentifiers() {
        return this.otherIdentifiers;
    }

    public void setOtherIdentifiers(Set<OtherIdentifier> otherIdentifiers) {
        this.otherIdentifiers = otherIdentifiers;
    }

    /**
     *
     */
    public CatalogSeries getCatalogSeries() {
        return this.catalogSeries;
    }

    public void setCatalogSeries(CatalogSeries catalogSeries) {
        this.catalogSeries = catalogSeries;
    }

    /**
     *
     */
    public Accession getAccession() {
        return this.accession;
    }

    public void setAccession(Accession accession) {
        this.accession = accession;
    }

    /**
     *
     */
    public Agent getCataloger() {
        return this.cataloger;
    }

    public void setCataloger(Agent cataloger) {
        this.cataloger = cataloger;
    }

    /**
     *
     */
    public Set<ExternalResource> getExternalResources() {
        return this.externalResources;
    }

    public void setExternalResources(Set<ExternalResource> externalResources) {
        this.externalResources = externalResources;
    }

    /**
     *      * Preparation, Container
     */
    public Container getContainer() {
        return this.container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }


    // Add Methods

    public void addCollectionObjectCitations(final CollectionObjectCitation collectionObjectCitation)
    {
        this.collectionObjectCitations.add(collectionObjectCitation);
        collectionObjectCitation.setCollectionObject(this);
    }

    public void addAttrs(final CollectionObjectAttr attr)
    {
        this.attrs.add(attr);
        attr.setCollectionObject(this);
    }

    public void addPreparations(final Preparation preparation)
    {
        this.preparations.add(preparation);
        preparation.setCollectionObject(this);
    }

    public void addDeterminations(final Determination determination)
    {
        this.determinations.add(determination);
        determination.setCollectionObject(this);
    }

    public void addProjectCollectionObjects(final ProjectCollectionObject projectCollectionObject)
    {
        this.projectCollectionObjects.add(projectCollectionObject);
        projectCollectionObject.setCollectionObject(this);
    }

    public void addDeaccessionCollectionObjects(final DeaccessionCollectionObject deaccessionCollectionObject)
    {
        this.deaccessionCollectionObjects.add(deaccessionCollectionObject);
        deaccessionCollectionObject.setCollectionObjectCatalog(this);
    }

    public void addOtherIdentifiers(final OtherIdentifier otherIdentifier)
    {
        this.otherIdentifiers.add(otherIdentifier);
        otherIdentifier.setCollectionObject(this);
    }

    public void addExternalResources(final ExternalResource externalResource)
    {
        this.externalResources.add(externalResource);
        externalResource.getCollectionObjects().add(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeCollectionObjectCitations(final CollectionObjectCitation collectionObjectCitation)
    {
        this.collectionObjectCitations.remove(collectionObjectCitation);
        collectionObjectCitation.setCollectionObject(null);
    }

    public void removeAttrs(final CollectionObjectAttr attr)
    {
        this.attrs.remove(attr);
        attr.setCollectionObject(null);
    }

    public void removePreparations(final Preparation preparation)
    {
        this.preparations.remove(preparation);
        preparation.setCollectionObject(null);
    }

    public void removeDeterminations(final Determination determination)
    {
        this.determinations.remove(determination);
        determination.setCollectionObject(null);
    }

    public void removeProjectCollectionObjects(final ProjectCollectionObject projectCollectionObject)
    {
        this.projectCollectionObjects.remove(projectCollectionObject);
        projectCollectionObject.setCollectionObject(null);
    }

    public void removeDeaccessionCollectionObjects(final DeaccessionCollectionObject deaccessionCollectionObject)
    {
        this.deaccessionCollectionObjects.remove(deaccessionCollectionObject);
        deaccessionCollectionObject.setCollectionObjectCatalog(null);
    }

    public void removeOtherIdentifiers(final OtherIdentifier otherIdentifier)
    {
        this.otherIdentifiers.remove(otherIdentifier);
        otherIdentifier.setCollectionObject(null);
    }

    public void removeExternalResources(final ExternalResource externalResource)
    {
        this.externalResources.remove(externalResource);
        externalResource.getCollectionObjects().remove(this);
    }

    // Delete Add Methods
}
