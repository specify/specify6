package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;




/**

 */
public class BorrowReturnMaterial  implements java.io.Serializable {

    // Fields    

     protected Integer borrowReturnMaterialId;
     protected Calendar returnedDate;
     protected Short quantity;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     private Agent agent;
     private BorrowMaterial borrowMaterial;


    // Constructors

    /** default constructor */
    public BorrowReturnMaterial() {
    }
    
    /** constructor with id */
    public BorrowReturnMaterial(Integer borrowReturnMaterialId) {
        this.borrowReturnMaterialId = borrowReturnMaterialId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        borrowReturnMaterialId = null;
        returnedDate = null;
        quantity = null;
        remarks = null;
        timestampModified = null;
        timestampCreated = Calendar.getInstance().getTime();
        lastEditedBy = null;
        agent = null;
        borrowMaterial = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Integer getBorrowReturnMaterialId() {
        return this.borrowReturnMaterialId;
    }
    
    public void setBorrowReturnMaterialId(Integer borrowReturnMaterialId) {
        this.borrowReturnMaterialId = borrowReturnMaterialId;
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
     *      * Quantity of preparations returned
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
     *      * person processing the  return
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    /**
     *      * Borrowed preparation returned
     */
    public BorrowMaterial getBorrowMaterial() {
        return this.borrowMaterial;
    }
    
    public void setBorrowMaterial(BorrowMaterial borrowMaterial) {
        this.borrowMaterial = borrowMaterial;
    }




    // Add Methods

    // Done Add Methods
}
