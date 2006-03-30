package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.Set;




/**

 */
public class LoanPhysicalObject  implements java.io.Serializable {

    // Fields    

     protected Integer loanPhysicalObjectId;
     protected Short quantity;
     protected String descriptionOfMaterial;
     protected String outComments;
     protected String inComments;
     protected Short quantityResolved;
     protected Short quantityReturned;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     private Preparation preparation;
     private Loan loan;
     private Set loanReturnPhysicalObjects;


    // Constructors

    /** default constructor */
    public LoanPhysicalObject() {
    }
    
    /** constructor with id */
    public LoanPhysicalObject(Integer loanPhysicalObjectId) {
        this.loanPhysicalObjectId = loanPhysicalObjectId;
    }
   
    
    

    // Property accessors

    /**
     *      * PrimaryKey
     */
    public Integer getLoanPhysicalObjectId() {
        return this.loanPhysicalObjectId;
    }
    
    public void setLoanPhysicalObjectId(Integer loanPhysicalObjectId) {
        this.loanPhysicalObjectId = loanPhysicalObjectId;
    }

    /**
     *      * The total number of specimens  loaned (necessary for lots)
     */
    public Short getQuantity() {
        return this.quantity;
    }
    
    public void setQuantity(Short quantity) {
        this.quantity = quantity;
    }

    /**
     *      * Description of loaned material (intended to be used for non-cataloged items, i.e. when PreparationID is null)
     */
    public String getDescriptionOfMaterial() {
        return this.descriptionOfMaterial;
    }
    
    public void setDescriptionOfMaterial(String descriptionOfMaterial) {
        this.descriptionOfMaterial = descriptionOfMaterial;
    }

    /**
     *      * Comments on item when loaned
     */
    public String getOutComments() {
        return this.outComments;
    }
    
    public void setOutComments(String outComments) {
        this.outComments = outComments;
    }

    /**
     *      * Comments on item when returned
     */
    public String getInComments() {
        return this.inComments;
    }
    
    public void setInComments(String inComments) {
        this.inComments = inComments;
    }

    /**
     *      * Number of specimens returned, deaccessioned or otherwise accounted for. (necessary for Lots)
     */
    public Short getQuantityResolved() {
        return this.quantityResolved;
    }
    
    public void setQuantityResolved(Short quantityResolved) {
        this.quantityResolved = quantityResolved;
    }

    /**
     *      * Number of specimens returned. (necessary for Lots)
     */
    public Short getQuantityReturned() {
        return this.quantityReturned;
    }
    
    public void setQuantityReturned(Short quantityReturned) {
        this.quantityReturned = quantityReturned;
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
     *      * Login name of the user last editing the record
     */
    public String getLastEditedBy() {
        return this.lastEditedBy;
    }
    
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     * 
     */
    public Preparation getPreparation() {
        return this.preparation;
    }
    
    public void setPreparation(Preparation preparation) {
        this.preparation = preparation;
    }

    /**
     *      * Loan containing the PhysicalObject
     */
    public Loan getLoan() {
        return this.loan;
    }
    
    public void setLoan(Loan loan) {
        this.loan = loan;
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