package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="CollectionObject"
 *     
 */
public class CollectionObject  implements java.io.Serializable {

    // Fields    

     protected Integer collectionObjectId;
     protected Integer derivedFromId;
     protected String fieldNumber;
     protected String description;
     protected String preparationMethod;
     protected String containerType;
     protected Integer containerTypeId;
     protected String text1;
     protected String text2;
     protected Float number1;
     protected Float number2;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected Integer preparationMethodId;
     protected Short yesNo1;
     protected Short yesNo2;
     protected Integer count1;
     protected String remarks;
     protected CollectionObjectType collectionObjectType;
     protected CollectingEvent collectingEvent;
     protected Set observations;
     protected Set soundEventStorages;
     protected Image image;
     protected Set imageCollectionObjects;
     protected Set collectionObjectCitations;
     protected CollectionObjectCatalog collectionObjectCatalog;
     protected BiologicalObjectAttribute biologicalObjectAttribute;
     protected Set biologicalObjectRelationsByRelatedBiologicalObjectId;
     protected Set biologicalObjectRelationsByBiologicalObjectId;
     protected Preparation preparation;
     protected Sound sound;
     protected Set collectionObjects;
     protected CollectionObject collectionObject;
     protected Set determinationsByBiologicalObjectId;
     protected Set determinationsByPreparationId;


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
     *             column="DerivedFromID"
     *             length="10"
     *         
     */
    public Integer getDerivedFromId() {
        return this.derivedFromId;
    }
    
    public void setDerivedFromId(Integer derivedFromId) {
        this.derivedFromId = derivedFromId;
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
     *             column="PreparationMethod"
     *             length="50"
     *         
     */
    public String getPreparationMethod() {
        return this.preparationMethod;
    }
    
    public void setPreparationMethod(String preparationMethod) {
        this.preparationMethod = preparationMethod;
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
     *             column="PreparationMethodID"
     *             length="10"
     *         
     */
    public Integer getPreparationMethodId() {
        return this.preparationMethodId;
    }
    
    public void setPreparationMethodId(Integer preparationMethodId) {
        this.preparationMethodId = preparationMethodId;
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
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="CollectionObjectTypeID"         
     *         
     */
    public CollectionObjectType getCollectionObjectType() {
        return this.collectionObjectType;
    }
    
    public void setCollectionObjectType(CollectionObjectType collectionObjectType) {
        this.collectionObjectType = collectionObjectType;
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
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="BiologicalObjectID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Observation"
     *         
     */
    public Set getObservations() {
        return this.observations;
    }
    
    public void setObservations(Set observations) {
        this.observations = observations;
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
     *      *            @hibernate.one-to-one
     *             outer-join="auto"
     *         
     */
    public Image getImage() {
        return this.image;
    }
    
    public void setImage(Image image) {
        this.image = image;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="CollectionlObjectID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.ImageCollectionObject"
     *         
     */
    public Set getImageCollectionObjects() {
        return this.imageCollectionObjects;
    }
    
    public void setImageCollectionObjects(Set imageCollectionObjects) {
        this.imageCollectionObjects = imageCollectionObjects;
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
     *      *            @hibernate.one-to-one
     *             outer-join="auto"
     *         
     */
    public CollectionObjectCatalog getCollectionObjectCatalog() {
        return this.collectionObjectCatalog;
    }
    
    public void setCollectionObjectCatalog(CollectionObjectCatalog collectionObjectCatalog) {
        this.collectionObjectCatalog = collectionObjectCatalog;
    }

    /**
     *      *            @hibernate.one-to-one
     *             outer-join="auto"
     *         
     */
    public BiologicalObjectAttribute getBiologicalObjectAttribute() {
        return this.biologicalObjectAttribute;
    }
    
    public void setBiologicalObjectAttribute(BiologicalObjectAttribute biologicalObjectAttribute) {
        this.biologicalObjectAttribute = biologicalObjectAttribute;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="RelatedBiologicalObjectID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.BiologicalObjectRelation"
     *         
     */
    public Set getBiologicalObjectRelationsByRelatedBiologicalObjectId() {
        return this.biologicalObjectRelationsByRelatedBiologicalObjectId;
    }
    
    public void setBiologicalObjectRelationsByRelatedBiologicalObjectId(Set biologicalObjectRelationsByRelatedBiologicalObjectId) {
        this.biologicalObjectRelationsByRelatedBiologicalObjectId = biologicalObjectRelationsByRelatedBiologicalObjectId;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="BiologicalObjectID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.BiologicalObjectRelation"
     *         
     */
    public Set getBiologicalObjectRelationsByBiologicalObjectId() {
        return this.biologicalObjectRelationsByBiologicalObjectId;
    }
    
    public void setBiologicalObjectRelationsByBiologicalObjectId(Set biologicalObjectRelationsByBiologicalObjectId) {
        this.biologicalObjectRelationsByBiologicalObjectId = biologicalObjectRelationsByBiologicalObjectId;
    }

    /**
     *      *            @hibernate.one-to-one
     *             outer-join="auto"
     *         
     */
    public Preparation getPreparation() {
        return this.preparation;
    }
    
    public void setPreparation(Preparation preparation) {
        this.preparation = preparation;
    }

    /**
     *      *            @hibernate.one-to-one
     *             outer-join="auto"
     *         
     */
    public Sound getSound() {
        return this.sound;
    }
    
    public void setSound(Sound sound) {
        this.sound = sound;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="ContainerID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.CollectionObject"
     *         
     */
    public Set getCollectionObjects() {
        return this.collectionObjects;
    }
    
    public void setCollectionObjects(Set collectionObjects) {
        this.collectionObjects = collectionObjects;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="ContainerID"         
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
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="BiologicalObjectID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Determination"
     *         
     */
    public Set getDeterminationsByBiologicalObjectId() {
        return this.determinationsByBiologicalObjectId;
    }
    
    public void setDeterminationsByBiologicalObjectId(Set determinationsByBiologicalObjectId) {
        this.determinationsByBiologicalObjectId = determinationsByBiologicalObjectId;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="PreparationID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Determination"
     *         
     */
    public Set getDeterminationsByPreparationId() {
        return this.determinationsByPreparationId;
    }
    
    public void setDeterminationsByPreparationId(Set determinationsByPreparationId) {
        this.determinationsByPreparationId = determinationsByPreparationId;
    }




}