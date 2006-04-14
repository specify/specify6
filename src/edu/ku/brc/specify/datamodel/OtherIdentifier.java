package edu.ku.brc.specify.datamodel;

import java.util.Date;




/**

 */
public class OtherIdentifier  implements java.io.Serializable {

    // Fields    

     protected Integer otherIdentifierId;
     protected String identifier;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     protected CollectionObject collectionObject;


    // Constructors

    /** default constructor */
    public OtherIdentifier() {
    }
    
    /** constructor with id */
    public OtherIdentifier(Integer otherIdentifierId) {
        this.otherIdentifierId = otherIdentifierId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        otherIdentifierId = null;
        identifier = null;
        remarks = null;
        timestampModified = new Date();
        timestampCreated = new Date();
        lastEditedBy = null;
        collectionObject = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Integer getOtherIdentifierId() {
        return this.otherIdentifierId;
    }
    
    public void setOtherIdentifierId(Integer otherIdentifierId) {
        this.otherIdentifierId = otherIdentifierId;
    }

    /**
     * 
     */
    public String getIdentifier() {
        return this.identifier;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * 
     */
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
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
    public Date getTimestampCreated() {
        return this.timestampCreated;
    }
    
    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    /**
     * 
     */
    public String getLastEditedBy() {
        return this.lastEditedBy;
    }
    
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     *      * ID of object identified by Identifier
     */
    public CollectionObject getCollectionObject() {
        return this.collectionObject;
    }
    
    public void setCollectionObject(CollectionObject collectionObject) {
        this.collectionObject = collectionObject;
    }





    // Add Methods

    // Done Add Methods

    // Delete Methods

    // Delete Add Methods
}
