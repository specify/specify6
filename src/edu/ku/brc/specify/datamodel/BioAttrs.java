package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="bioattrs"
 *     
 */
public class BioAttrs  implements AttrsSettableGettable,java.io.Serializable {

    // Fields    

     protected Integer bioAttrsId;
     protected String name;
     protected String strValue;
     protected Integer intValue;
     protected Integer fieldType;
     protected Integer unit;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String remarks;
     private CollectionObj collectionObj;
     protected CollectionObjectType collectionobjecttypes;


    // Constructors

    /** default constructor */
    public BioAttrs() {
    }
    
    /** constructor with id */
    public BioAttrs(Integer bioAttrsId) {
        this.bioAttrsId = bioAttrsId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="BioAttrsID"
     *         
     */
    public Integer getBioAttrsId() {
        return this.bioAttrsId;
    }
    
    public void setBioAttrsId(Integer bioAttrsId) {
        this.bioAttrsId = bioAttrsId;
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
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="CollectionObjectID"         
     *         
     */
    public CollectionObj getCollectionObj() {
        return this.collectionObj;
    }
    
    public void setCollectionObj(CollectionObj collectionObj) {
        this.collectionObj = collectionObj;
    }

    /**
     *      *            @hibernate.one-to-one
     *             class="edu.ku.brc.specify.datamodel.CollectionObjectType"
     *             outer-join="auto"
     *             constrained="true"
     *         
     */
    public CollectionObjectType getCollectionobjecttypes() {
        return this.collectionobjecttypes;
    }
    
    public void setCollectionobjecttypes(CollectionObjectType collectionobjecttypes) {
        this.collectionobjecttypes = collectionobjecttypes;
    }




}