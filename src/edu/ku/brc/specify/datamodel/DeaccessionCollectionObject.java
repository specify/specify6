package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.HashSet;
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
     protected CollectionObject collectionObjectCatalog;
     protected Deaccession deaccession;
     protected Set<LoanReturnPhysicalObject> loanReturnPhysicalObjects;


    // Constructors

    /** default constructor */
    public DeaccessionCollectionObject() {
    }
    
    /** constructor with id */
    public DeaccessionCollectionObject(Integer deaccessionCollectionObjectId) {
        this.deaccessionCollectionObjectId = deaccessionCollectionObjectId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        deaccessionCollectionObjectId = null;
        quantity = null;
        remarks = null;
        timestampModified = new Date();
        timestampCreated = new Date();
        lastEditedBy = null;
        collectionObjectCatalog = null;
        deaccession = null;
        loanReturnPhysicalObjects = new HashSet<LoanReturnPhysicalObject>();
    }
    // End Initializer

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
    public Set<LoanReturnPhysicalObject> getLoanReturnPhysicalObjects() {
        return this.loanReturnPhysicalObjects;
    }
    
    public void setLoanReturnPhysicalObjects(Set<LoanReturnPhysicalObject> loanReturnPhysicalObjects) {
        this.loanReturnPhysicalObjects = loanReturnPhysicalObjects;
    }





    // Add Methods

    public void addLoanReturnPhysicalObject(final LoanReturnPhysicalObject loanReturnPhysicalObject)
    {
        this.loanReturnPhysicalObjects.add(loanReturnPhysicalObject);
        loanReturnPhysicalObject.setDeaccessionCollectionObject(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeLoanReturnPhysicalObject(final LoanReturnPhysicalObject loanReturnPhysicalObject)
    {
        this.loanReturnPhysicalObjects.remove(loanReturnPhysicalObject);
        loanReturnPhysicalObject.setDeaccessionCollectionObject(null);
    }

    // Delete Add Methods
}
