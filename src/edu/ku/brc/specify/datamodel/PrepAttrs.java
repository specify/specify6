package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="prepattrs"
 *     
 */
public class PrepAttrs  implements AttrsSettableGettable,java.io.Serializable {

    // Fields    

     protected Integer prepAttrsId;
     protected String name;
     protected String strValue;
     protected Integer intValue;
     protected Short fieldType;
     protected Short unit;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String remarks;
     private Preparation preparation;
     private Taxon Taxon;


    // Constructors

    /** default constructor */
    public PrepAttrs() {
    }
    
    /** constructor with id */
    public PrepAttrs(Integer prepAttrsId) {
        this.prepAttrsId = prepAttrsId;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Integer getPrepAttrsId() {
        return this.prepAttrsId;
    }
    
    public void setPrepAttrsId(Integer prepAttrsId) {
        this.prepAttrsId = prepAttrsId;
    }

    /**
     *      *            @hibernate.property
     *             column="Name"
     *             length="32"
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
     *             length="2"
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
     *             column="unit"
     *             length="2"
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
     *            @hibernate.column name="PreparationId"
     *         
     */
    public Preparation getPreparation() {
        return this.preparation;
    }
    
    public void setPreparation(Preparation preparation) {
        this.preparation = preparation;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="ParasiteTaxonID"         
     *         
     */
    public Taxon getTaxon() {
        return this.Taxon;
    }
    
    public void setTaxon(Taxon Taxon) {
        this.Taxon = Taxon;
    }




}