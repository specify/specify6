package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.criterion.Expression;

import edu.ku.brc.specify.dbsupport.HibernateUtil;


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
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected Boolean deaccessioned;
     protected Float catalogNumber;
     protected CollectingEvent collectingEvent;
     protected ContainerItem containerItem;
     protected Set collectionObjectCitations;
     protected Set attrs;
     protected Set preparations;
     protected Set determinations;
     protected Set projectCollectionObjects;
     protected Set deaccessionCollectionObjects;
     protected Set otherIdentifiers;
     protected CatalogSeries catalogSeries;
     protected Accession accession;
     protected Agent cataloger;
     private Set externalResources;


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
     *             column="Text1"
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
     *         
     */
    public Boolean getYesNo1() {
        return this.yesNo1;
    }
    
    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      *            @hibernate.property
     *             column="YesNo2"
     *         
     */
    public Boolean getYesNo2() {
        return this.yesNo2;
    }
    
    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     *      *            @hibernate.property
     *             column="CountAmt"
     *             length="10"
     *         
     */
    public Integer getCountAmt() {
        return this.countAmt;
    }
    
    public void setCountAmt(Integer countAmt) {
        this.countAmt = countAmt;
    }

    /**
     *      *            @hibernate.property
     *             column="Remarks"
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
     *             column="Name"
     *             length="64"
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
     *         
     */
    public Calendar getCatalogedDate() {
        return this.catalogedDate;
    }
    
    public void setCatalogedDate(Calendar catalogedDate) {
        this.catalogedDate = catalogedDate;
    }

    /**
     *      *            @hibernate.property
     *             column="CatalogedDateVerbatim"
     *         length="32"
     *         
     */
    public String getCatalogedDateVerbatim() {
        return this.catalogedDateVerbatim;
    }
    
    public void setCatalogedDateVerbatim(String catalogedDateVerbatim) {
        this.catalogedDateVerbatim = catalogedDateVerbatim;
    }

    /**
     *      *            @hibernate.property
     *             column="GUID"
     *             length="255"
     *         
     */
    public String getGuid() {
        return this.guid;
    }
    
    public void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     *      *            @hibernate.property
     *             column="TimestampCreated"
     *             length="23"
     *             update="false"
     *             not-null="true"
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
     *             not-null="true"
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
     *         
     */
    public Boolean getDeaccessioned() {
        return this.deaccessioned;
    }
    
    public void setDeaccessioned(Boolean deaccessioned) {
        this.deaccessioned = deaccessioned;
    }

    /**
     *      *            @hibernate.property
     *             column="CatalogNumber"
     *         
     */
    public Float getCatalogNumber() {
        return this.catalogNumber;
    }
    
    public void setCatalogNumber(Float catalogNumber) {
        this.catalogNumber = catalogNumber;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     * 			cascade="none"
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
     *      *            @hibernate.many-to-one
     *             not-null="auto"
     *            @hibernate.column name="ContainerItemID"         
     *         
     */
    public ContainerItem getContainerItem() {
        return this.containerItem;
    }
    
    public void setContainerItem(ContainerItem containerItem) {
        this.containerItem = containerItem;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="CollectionObjectID"
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
     *             column="CollectionObjectAttrID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.CollectionObjectAttr"
     *         
     */
    public Set getAttrs() {
        return this.attrs;
    }
    
    public void setAttrs(Set attrs) {
        this.attrs = attrs;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="all"
     *            @hibernate.collection-key
     *             column="CollectionObjectID"
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
     *             cascade="all"
     *            @hibernate.collection-key
     *             column="CollectionObjectID"
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
    public Agent getCataloger() {
        return this.cataloger;
    }
    
    public void setCataloger(Agent cataloger) {
        this.cataloger = cataloger;
    }

    /**
     * 
     */
    public Set getExternalResources() {
        return this.externalResources;
    }
    
    public void setExternalResources(Set externalResources) {
        this.externalResources = externalResources;
    }




  // The following is extra code specified in the hbm.xml files

        
    protected Container container = null; // When not null it means this is the Container
    
    /**
     * 
     */
    public Container getContainer() 
    {
        Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(Container.class);
        criteria.add(Expression.eq("containerId", collectionObjectId));
        java.util.List list = criteria.list();
        this.container = list != null && list.size() > 0 ? (Container)list.get(0) : null;
        return this.container;
    }
    
    public void setContainer(Container container) 
    {
        this.container = container;
    }
    
    
  // end of extra code specified in the hbm.xml files
}