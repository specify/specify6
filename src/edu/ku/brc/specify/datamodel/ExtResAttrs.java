package edu.ku.brc.specify.datamodel;

import java.util.Date;




/**
 *        @hibernate.class
 *         table="extResattrs"
 *     
 */
public class ExtResAttrs  implements AttrsSettableGettable,java.io.Serializable {

    // Fields    

     protected Integer extResAttrsId;
     protected String name;
     protected String strValue;
     protected Integer intValue;
     protected Short fieldType;
     protected Short unit;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String remarks;
     private ExternalResource externalResource;


    // Constructors

    /** default constructor */
    public ExtResAttrs() {
    }
    
    /** constructor with id */
    public ExtResAttrs(Integer extResAttrsId) {
        this.extResAttrsId = extResAttrsId;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Integer getExtResAttrsId() {
        return this.extResAttrsId;
    }
    
    public void setExtResAttrsId(Integer extResAttrsId) {
        this.extResAttrsId = extResAttrsId;
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
     *            @hibernate.column name="ExternalResourceID"         
     *         
     */
    public ExternalResource getExternalResource() {
        return this.externalResource;
    }
    
    public void setExternalResource(ExternalResource externalResource) {
        this.externalResource = externalResource;
    }




}