package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="bioattrs"
 *     
 */
public class BioAttrs  implements java.io.Serializable {

    // Fields    

     protected Integer bioAttrsId;
     protected String name;
     protected String value;
     protected Integer fieldType;
     protected Integer unit;
     protected Integer bioDate;
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
     *             column="value"
     *             length="128"
     *         
     */
    public String getValue() {
        return this.value;
    }
    
    public void setValue(String value) {
        this.value = value;
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
     *             column="bioDate"
     *             length="10"
     *         
     */
    public Integer getBioDate() {
        return this.bioDate;
    }
    
    public void setBioDate(Integer bioDate) {
        this.bioDate = bioDate;
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