package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="habitatattrs"
 *     
 */
public class HabitatAttrs  implements AttrsSettableGettable,java.io.Serializable {

    // Fields    

     protected Integer habitatAttrsId;
     protected String name;
     protected String strValue;
     protected Integer intValue;
     protected Integer fieldType;
     protected Integer unit;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String remarks;
     protected CollectingEvent collectingEvent;
     protected CollectionObjectType collectionObjectType;
     protected TaxonName taxonName;


    // Constructors

    /** default constructor */
    public HabitatAttrs() {
    }
    
    /** constructor with id */
    public HabitatAttrs(Integer habitatAttrsId) {
        this.habitatAttrsId = habitatAttrsId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="HabitatAttrsID"
     *         
     */
    public Integer getHabitatAttrsId() {
        return this.habitatAttrsId;
    }
    
    public void setHabitatAttrsId(Integer habitatAttrsId) {
        this.habitatAttrsId = habitatAttrsId;
    }

    /**
     *      *            @hibernate.property
     *             column="name"
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
     *             column="strValue"
     *             length="128"
     *         
     */
    public String getStrValue() {
        return this.strValue;
    }
    
    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }

    /**
     *      *            @hibernate.property
     *             column="intValue"
     *             length="10"
     *         
     */
    public Integer getIntValue() {
        return this.intValue;
    }
    
    public void setIntValue(Integer intValue) {
        this.intValue = intValue;
    }

    /**
     *      *            @hibernate.property
     *             column="fieldType"
     *             length="10"
     *         
     */
    public Integer getFieldType() {
        return this.fieldType;
    }
    
    public void setFieldType(Integer fieldType) {
        this.fieldType = fieldType;
    }

    /**
     *      *            @hibernate.property
     *             column="unit"
     *             length="2"
     *         
     */
    public Integer getUnit() {
        return this.unit;
    }
    
    public void setUnit(Integer unit) {
        this.unit = unit;
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
     *      *            @hibernate.one-to-one
     *             class="edu.ku.brc.specify.datamodel.CollectingEvent"
     *             outer-join="auto"
     *             constrained="true"
     * 			cascade="delete"
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
     *             not-null="true"
     *            @hibernate.column name="BiologicalObjectTypeCollectedID"         
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
     *            @hibernate.column name="HostTaxonID"         
     *         
     */
    public TaxonName getTaxonName() {
        return this.taxonName;
    }
    
    public void setTaxonName(TaxonName taxonName) {
        this.taxonName = taxonName;
    }




}