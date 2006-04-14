package edu.ku.brc.specify.datamodel;

import java.util.Date;




/**

 */
public class CollectionObjectCitation  implements java.io.Serializable {

    // Fields    

     protected Integer collectionObjectCitationId;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     protected ReferenceWork referenceWork;
     protected CollectionObject collectionObject;


    // Constructors

    /** default constructor */
    public CollectionObjectCitation() {
    }
    
    /** constructor with id */
    public CollectionObjectCitation(Integer collectionObjectCitationId) {
        this.collectionObjectCitationId = collectionObjectCitationId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        collectionObjectCitationId = null;
        remarks = null;
        timestampModified = new Date();
        timestampCreated = new Date();
        lastEditedBy = null;
        referenceWork = null;
        collectionObject = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Integer getCollectionObjectCitationId() {
        return this.collectionObjectCitationId;
    }
    
    public void setCollectionObjectCitationId(Integer collectionObjectCitationId) {
        this.collectionObjectCitationId = collectionObjectCitationId;
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
     *      * The associated reference
     */
    public ReferenceWork getReferenceWork() {
        return this.referenceWork;
    }
    
    public void setReferenceWork(ReferenceWork referenceWork) {
        this.referenceWork = referenceWork;
    }

    /**
     *      * Biological Object cited
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
