package edu.ku.brc.specify.datamodel;

import java.util.Date;




/**

 */
public class CollectionObjectAttr  implements AttributeIFace,java.io.Serializable {

    // Fields    

     protected Integer attrId;
     protected String strValue;
     protected Double dblValue;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected CollectionObject collectionObject;
     protected AttributeDef definition;


    // Constructors

    /** default constructor */
    public CollectionObjectAttr() {
    }
    
    /** constructor with id */
    public CollectionObjectAttr(Integer attrId) {
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
    public CollectionObject getCollectionObject() {
        return this.collectionObject;
    }
    
    public void setCollectionObject(CollectionObject collectionObject) {
        this.collectionObject = collectionObject;
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




}