package edu.ku.brc.specify.datamodel;

import java.util.Date;




/**
 *        @hibernate.class
 *         table="externalresourceattr"
 *     
 */
public class ExternalResourceAttr  implements AttributeIFace,java.io.Serializable {

    // Fields    

     protected Integer attrId;
     protected String strValue;
     protected Double dblValue;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected ExternalResource externalResource;
     protected AttributeDef definition;


    // Constructors

    /** default constructor */
    public ExternalResourceAttr() {
    }
    
    /** constructor with id */
    public ExternalResourceAttr(Integer attrId) {
        this.attrId = attrId;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Integer getAttrId() {
        return this.attrId;
    }
    
    public void setAttrId(Integer attrId) {
        this.attrId = attrId;
    }

    /**
     *      *            @hibernate.property
     *             column="StrValue"
     *             length="64"
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
     *             column="DoubleValue"
     *         
     */
    public Double getDblValue() {
        return this.dblValue;
    }
    
    public void setDblValue(Double dblValue) {
        this.dblValue = dblValue;
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

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="AttributeDefID"         
     *         
     */
    public AttributeDef getDefinition() {
        return this.definition;
    }
    
    public void setDefinition(AttributeDef definition) {
        this.definition = definition;
    }




}