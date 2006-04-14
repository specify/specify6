package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;




/**

 */
public class LoanReturnPhysicalObject  implements java.io.Serializable {

    // Fields    

     protected Integer loanReturnPhysicalObjectId;
     protected Calendar returnedDate;
     protected Short quantity;
     protected String remarks;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected LoanPhysicalObject loanPhysicalObject;
     protected DeaccessionCollectionObject deaccessionCollectionObject;
     protected Agent agent;


    // Constructors

    /** default constructor */
    public LoanReturnPhysicalObject() {
    }
    
    /** constructor with id */
    public LoanReturnPhysicalObject(Integer loanReturnPhysicalObjectId) {
        this.loanReturnPhysicalObjectId = loanReturnPhysicalObjectId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        loanReturnPhysicalObjectId = null;
        returnedDate = null;
        quantity = null;
        remarks = null;
        timestampCreated = new Date();
        timestampModified = new Date();
        lastEditedBy = null;
        loanPhysicalObject = null;
        deaccessionCollectionObject = null;
        agent = null;
    }
    // End Initializer

    // Property accessors

    /**
     *      * PrimaryKey
     */
    public Integer getLoanReturnPhysicalObjectId() {
        return this.loanReturnPhysicalObjectId;
    }
    
    public void setLoanReturnPhysicalObjectId(Integer loanReturnPhysicalObjectId) {
        this.loanReturnPhysicalObjectId = loanReturnPhysicalObjectId;
    }

    /**
     * 
     */
    public Calendar getReturnedDate() {
        return this.returnedDate;
    }
    
    public void setReturnedDate(Calendar returnedDate) {
        this.returnedDate = returnedDate;
    }

    /**
     *      * Quantity of items returned (necessary for lots)
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
     *      * Date the record was created
     */
    public Date getTimestampCreated() {
        return this.timestampCreated;
    }
    
    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    /**
     *      * Date the record was modified
     */
    public Date getTimestampModified() {
        return this.timestampModified;
    }
    
    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
    }

    /**
     *      * user ID  of the person last editing the record
     */
    public String getLastEditedBy() {
        return this.lastEditedBy;
    }
    
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     *      * Link to LoanPhysicalObject table
     */
    public LoanPhysicalObject getLoanPhysicalObject() {
        return this.loanPhysicalObject;
    }
    
    public void setLoanPhysicalObject(LoanPhysicalObject loanPhysicalObject) {
        this.loanPhysicalObject = loanPhysicalObject;
    }

    /**
     *      * ID of associated (if present) DeaccessionPhysicalObject record
     */
    public DeaccessionCollectionObject getDeaccessionCollectionObject() {
        return this.deaccessionCollectionObject;
    }
    
    public void setDeaccessionCollectionObject(DeaccessionCollectionObject deaccessionCollectionObject) {
        this.deaccessionCollectionObject = deaccessionCollectionObject;
    }

    /**
     *      * Person processing the loan return
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }





    // Add Methods

    // Done Add Methods

    // Delete Methods

    // Delete Add Methods
}
