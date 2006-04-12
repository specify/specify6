package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;




/**

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
   
    
    

    // Initializer
    public void initialize()
    {
        attrId = null;
        strValue = null;
        dblValue = null;
        timestampCreated = Calendar.getInstance().getTime();
        timestampModified = null;
        externalResource = null;
        definition = null;
    }
    // End Initializer

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
     * 
     */
    public String getStrValue() {
        return this.strValue;
    }
    
    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }

    /**
     * 
     */
    public Double getDblValue() {
        return this.dblValue;
    }
    
    public void setDblValue(Double dblValue) {
        this.dblValue = dblValue;
    }

    /**
     * 
     */
    public Date getTimestampCreated() {
        return this.timestampCreated;
    }
    
    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    /**
     * 
     */
    public Date getTimestampModified() {
        return this.timestampModified;
    }
    
    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
    }

    /**
     * 
     */
    public ExternalResource getExternalResource() {
        return this.externalResource;
    }
    
    public void setExternalResource(ExternalResource externalResource) {
        this.externalResource = externalResource;
    }

    /**
     * 
     */
    public AttributeDef getDefinition() {
        return this.definition;
    }
    
    public void setDefinition(AttributeDef definition) {
        this.definition = definition;
    }




    // Add Methods

    // Done Add Methods
}
