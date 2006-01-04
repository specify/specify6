package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="Preparation"
 *     
 */
public class Preparation  implements java.io.Serializable {

    // Fields    

     protected Integer preparationId;
     protected Integer preparedDate;
     protected String medium;
     protected Integer mediumId;
     protected String partInformation;
     protected String startBoxNumber;
     protected String endBoxNumber;
     protected String startSlideNumber;
     protected String endSlideNumber;
     protected String sectionOrientation;
     protected String sectionWidth;
     protected String size;
     protected String url;
     protected String identifier;
     protected String nestLining;
     protected String nestMaterial;
     protected String nestLocation;
     protected String setMark;
     protected Integer collectedEggCount;
     protected Integer collectedParasiteEggCount;
     protected Integer fieldEggCount;
     protected Integer fieldParasiteEggCount;
     protected String eggIncubationStage;
     protected String eggDescription;
     protected String format;
     protected String storageInfo;
     protected String preparationType;
     protected Integer preparationTypeId;
     protected String containerType;
     protected String containerTypeId;
     protected String dnaconcentration;
     protected String volume;
     protected String text1;
     protected String text2;
     protected Float number1;
     protected Float number2;
     protected String remarks;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected Short nestCollected;
     protected Short yesNo1;
     protected Short yesNo2;
     private CollectionObject collectionObject;
     private CollectionObjectType collectionObjectType;
     private TaxonName taxonName;
     private Agent agent;


    // Constructors

    /** default constructor */
    public Preparation() {
    }
    
    /** constructor with id */
    public Preparation(Integer preparationId) {
        this.preparationId = preparationId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="PreparationID"
     *         
     */
    public Integer getPreparationId() {
        return this.preparationId;
    }
    
    public void setPreparationId(Integer preparationId) {
        this.preparationId = preparationId;
    }

    /**
     *      *            @hibernate.property
     *             column="PreparedDate"
     *             length="10"
     *         
     */
    public Integer getPreparedDate() {
        return this.preparedDate;
    }
    
    public void setPreparedDate(Integer preparedDate) {
        this.preparedDate = preparedDate;
    }

    /**
     *      *            @hibernate.property
     *             column="Medium"
     *             length="50"
     *         
     */
    public String getMedium() {
        return this.medium;
    }
    
    public void setMedium(String medium) {
        this.medium = medium;
    }

    /**
     *      *            @hibernate.property
     *             column="MediumID"
     *             length="10"
     *         
     */
    public Integer getMediumId() {
        return this.mediumId;
    }
    
    public void setMediumId(Integer mediumId) {
        this.mediumId = mediumId;
    }

    /**
     *      *            @hibernate.property
     *             column="PartInformation"
     *             length="50"
     *         
     */
    public String getPartInformation() {
        return this.partInformation;
    }
    
    public void setPartInformation(String partInformation) {
        this.partInformation = partInformation;
    }

    /**
     *      *            @hibernate.property
     *             column="StartBoxNumber"
     *             length="50"
     *         
     */
    public String getStartBoxNumber() {
        return this.startBoxNumber;
    }
    
    public void setStartBoxNumber(String startBoxNumber) {
        this.startBoxNumber = startBoxNumber;
    }

    /**
     *      *            @hibernate.property
     *             column="EndBoxNumber"
     *             length="50"
     *         
     */
    public String getEndBoxNumber() {
        return this.endBoxNumber;
    }
    
    public void setEndBoxNumber(String endBoxNumber) {
        this.endBoxNumber = endBoxNumber;
    }

    /**
     *      *            @hibernate.property
     *             column="StartSlideNumber"
     *             length="50"
     *         
     */
    public String getStartSlideNumber() {
        return this.startSlideNumber;
    }
    
    public void setStartSlideNumber(String startSlideNumber) {
        this.startSlideNumber = startSlideNumber;
    }

    /**
     *      *            @hibernate.property
     *             column="EndSlideNumber"
     *             length="50"
     *         
     */
    public String getEndSlideNumber() {
        return this.endSlideNumber;
    }
    
    public void setEndSlideNumber(String endSlideNumber) {
        this.endSlideNumber = endSlideNumber;
    }

    /**
     *      *            @hibernate.property
     *             column="SectionOrientation"
     *             length="50"
     *         
     */
    public String getSectionOrientation() {
        return this.sectionOrientation;
    }
    
    public void setSectionOrientation(String sectionOrientation) {
        this.sectionOrientation = sectionOrientation;
    }

    /**
     *      *            @hibernate.property
     *             column="SectionWidth"
     *             length="50"
     *         
     */
    public String getSectionWidth() {
        return this.sectionWidth;
    }
    
    public void setSectionWidth(String sectionWidth) {
        this.sectionWidth = sectionWidth;
    }

    /**
     *      *            @hibernate.property
     *             column="Size"
     *             length="50"
     *         
     */
    public String getSize() {
        return this.size;
    }
    
    public void setSize(String size) {
        this.size = size;
    }

    /**
     *      *            @hibernate.property
     *             column="URL"
     *             length="300"
     *         
     */
    public String getUrl() {
        return this.url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     *      *            @hibernate.property
     *             column="Identifier"
     *             length="50"
     *         
     */
    public String getIdentifier() {
        return this.identifier;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     *      *            @hibernate.property
     *             column="NestLining"
     *             length="50"
     *         
     */
    public String getNestLining() {
        return this.nestLining;
    }
    
    public void setNestLining(String nestLining) {
        this.nestLining = nestLining;
    }

    /**
     *      *            @hibernate.property
     *             column="NestMaterial"
     *             length="50"
     *         
     */
    public String getNestMaterial() {
        return this.nestMaterial;
    }
    
    public void setNestMaterial(String nestMaterial) {
        this.nestMaterial = nestMaterial;
    }

    /**
     *      *            @hibernate.property
     *             column="NestLocation"
     *             length="50"
     *         
     */
    public String getNestLocation() {
        return this.nestLocation;
    }
    
    public void setNestLocation(String nestLocation) {
        this.nestLocation = nestLocation;
    }

    /**
     *      *            @hibernate.property
     *             column="SetMark"
     *             length="50"
     *         
     */
    public String getSetMark() {
        return this.setMark;
    }
    
    public void setSetMark(String setMark) {
        this.setMark = setMark;
    }

    /**
     *      *            @hibernate.property
     *             column="CollectedEggCount"
     *             length="10"
     *         
     */
    public Integer getCollectedEggCount() {
        return this.collectedEggCount;
    }
    
    public void setCollectedEggCount(Integer collectedEggCount) {
        this.collectedEggCount = collectedEggCount;
    }

    /**
     *      *            @hibernate.property
     *             column="CollectedParasiteEggCount"
     *             length="10"
     *         
     */
    public Integer getCollectedParasiteEggCount() {
        return this.collectedParasiteEggCount;
    }
    
    public void setCollectedParasiteEggCount(Integer collectedParasiteEggCount) {
        this.collectedParasiteEggCount = collectedParasiteEggCount;
    }

    /**
     *      *            @hibernate.property
     *             column="FieldEggCount"
     *             length="10"
     *         
     */
    public Integer getFieldEggCount() {
        return this.fieldEggCount;
    }
    
    public void setFieldEggCount(Integer fieldEggCount) {
        this.fieldEggCount = fieldEggCount;
    }

    /**
     *      *            @hibernate.property
     *             column="FieldParasiteEggCount"
     *             length="10"
     *         
     */
    public Integer getFieldParasiteEggCount() {
        return this.fieldParasiteEggCount;
    }
    
    public void setFieldParasiteEggCount(Integer fieldParasiteEggCount) {
        this.fieldParasiteEggCount = fieldParasiteEggCount;
    }

    /**
     *      *            @hibernate.property
     *             column="EggIncubationStage"
     *             length="50"
     *         
     */
    public String getEggIncubationStage() {
        return this.eggIncubationStage;
    }
    
    public void setEggIncubationStage(String eggIncubationStage) {
        this.eggIncubationStage = eggIncubationStage;
    }

    /**
     *      *            @hibernate.property
     *             column="EggDescription"
     *             length="50"
     *         
     */
    public String getEggDescription() {
        return this.eggDescription;
    }
    
    public void setEggDescription(String eggDescription) {
        this.eggDescription = eggDescription;
    }

    /**
     *      *            @hibernate.property
     *             column="Format"
     *             length="50"
     *         
     */
    public String getFormat() {
        return this.format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     *      *            @hibernate.property
     *             column="StorageInfo"
     *             length="50"
     *         
     */
    public String getStorageInfo() {
        return this.storageInfo;
    }
    
    public void setStorageInfo(String storageInfo) {
        this.storageInfo = storageInfo;
    }

    /**
     *      *            @hibernate.property
     *             column="PreparationType"
     *             length="50"
     *         
     */
    public String getPreparationType() {
        return this.preparationType;
    }
    
    public void setPreparationType(String preparationType) {
        this.preparationType = preparationType;
    }

    /**
     *      *            @hibernate.property
     *             column="PreparationTypeID"
     *             length="10"
     *         
     */
    public Integer getPreparationTypeId() {
        return this.preparationTypeId;
    }
    
    public void setPreparationTypeId(Integer preparationTypeId) {
        this.preparationTypeId = preparationTypeId;
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
     *             length="50"
     *         
     */
    public String getContainerTypeId() {
        return this.containerTypeId;
    }
    
    public void setContainerTypeId(String containerTypeId) {
        this.containerTypeId = containerTypeId;
    }

    /**
     *      *            @hibernate.property
     *             column="DNAConcentration"
     *             length="50"
     *         
     */
    public String getDnaconcentration() {
        return this.dnaconcentration;
    }
    
    public void setDnaconcentration(String dnaconcentration) {
        this.dnaconcentration = dnaconcentration;
    }

    /**
     *      *            @hibernate.property
     *             column="Volume"
     *             length="50"
     *         
     */
    public String getVolume() {
        return this.volume;
    }
    
    public void setVolume(String volume) {
        this.volume = volume;
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
     *             column="NestCollected"
     *             length="5"
     *         
     */
    public Short getNestCollected() {
        return this.nestCollected;
    }
    
    public void setNestCollected(Short nestCollected) {
        this.nestCollected = nestCollected;
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
     *      *            @hibernate.one-to-one
     *             class="edu.ku.brc.specify.datamodel.CollectionObject"
     *             outer-join="auto"
     *             constrained="true"
     * 			cascade="delete"
     *         
     */
    public CollectionObject getCollectionObject() {
        return this.collectionObject;
    }
    
    public void setCollectionObject(CollectionObject collectionObject) {
        this.collectionObject = collectionObject;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="PhysicalObjectTypeID"         
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
     *            @hibernate.column name="ParasiteTaxonNameID"         
     *         
     */
    public TaxonName getTaxonName() {
        return this.taxonName;
    }
    
    public void setTaxonName(TaxonName taxonName) {
        this.taxonName = taxonName;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="PreparedByID"         
     *         
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }




}