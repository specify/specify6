package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.Set;




/**

 */
public class DeaccessionCollectionObject  implements java.io.Serializable {

    // Fields    

     protected Integer deaccessionCollectionObjectId;
     protected Short quantity;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     private CollectionObject collectionObjectCatalog;
     private Deaccession deaccession;
     private Set loanReturnPhysicalObjects;


    // Constructors

    /** default constructor */
    public DeaccessionCollectionObject() {
    }
    
    /** constructor with id */
    public DeaccessionCollectionObject(Integer deaccessionCollectionObjectId) {
        this.deaccessionCollectionObjectId = deaccessionCollectionObjectId;
    }
   
    
    

    // Property accessors

    /**
     *      * Primary key
     */
    public Integer getDeaccessionCollectionObjectId() {
        return this.deaccessionCollectionObjectId;
    }
    
    public void setDeaccessionCollectionObjectId(Integer deaccessionCollectionObjectId) {
        this.deaccessionCollectionObjectId = deaccessionCollectionObjectId;
    }

    /**
     *      * Number of specimens deaccessioned (necessary for lots)
     */
    public Short getQuantity() {
        return this.quantity;
    }
    
    public void setQuantity(Short quantity) {
        this.quantity = quantity;
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
     *      * The object being deaccessioned
     */
    public CollectionObject getCollectionObjectCatalog() {
        return this.collectionObjectCatalog;
    }
    
    public void setCollectionObjectCatalog(CollectionObject collectionObjectCatalog) {
        this.collectionObjectCatalog = collectionObjectCatalog;
    }

    /**
     *      * The deaccession
     */
    public Deaccession getDeaccession() {
        return this.deaccession;
    }
    
    public void setDeaccession(Deaccession deaccession) {
        this.deaccession = deaccession;
    }

    /**
     * 
     */
    public Set getLoanReturnPhysicalObjects() {
        return this.loanReturnPhysicalObjects;
    }
    
    public void setLoanReturnPhysicalObjects(Set loanReturnPhysicalObjects) {
        this.loanReturnPhysicalObjects = loanReturnPhysicalObjects;
    }




}