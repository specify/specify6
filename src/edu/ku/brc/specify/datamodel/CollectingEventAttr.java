package edu.ku.brc.specify.datamodel;

import java.util.Date;

import edu.ku.brc.dbsupport.AttributeIFace;




/**

 */
public class CollectingEventAttr  implements AttributeIFace,java.io.Serializable {

    // Fields    

     protected Integer attrId;
     protected String strValue;
     protected Double dblValue;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected CollectingEvent collectingEvent;
     protected AttributeDef definition;


    // Constructors

    /** default constructor */
    public CollectingEventAttr() {
    }
    
    /** constructor with id */
    public CollectingEventAttr(Integer attrId) {
        this.attrId = attrId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        attrId = null;
        strValue = null;
        dblValue = null;
        timestampCreated = new Date();
        timestampModified = new Date();
        collectingEvent = null;
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
    public CollectingEvent getCollectingEvent() {
        return this.collectingEvent;
    }
    
    public void setCollectingEvent(CollectingEvent collectingEvent) {
        this.collectingEvent = collectingEvent;
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

    // Delete Methods

    // Delete Add Methods
}
