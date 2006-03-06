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
     protected Short fieldType;
     protected Short unit;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String remarks;
     protected CollectingEvent collectingEvent;
     protected Taxon Taxon;


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
     *             column="StrValue"
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
     *             column="IntValue"
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
     *             column="FieldType"
     *         
     */
    public Short getFieldType() {
        return this.fieldType;
    }
    
    public void setFieldType(Short fieldType) {
        this.fieldType = fieldType;
    }

    /**
     *      *            @hibernate.property
     *             column="Unit"
     *         
     */
    public Short getUnit() {
        return this.unit;
    }
    
    public void setUnit(Short unit) {
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
     *             not-null="true"
     *            @hibernate.column name="HostTaxonID"         
     *         
     */
    public Taxon getTaxon() {
        return this.Taxon;
    }
    
    public void setTaxon(Taxon Taxon) {
        this.Taxon = Taxon;
    }




}