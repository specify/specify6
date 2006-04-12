package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;




/**

 */
public class BorrowMaterial  implements java.io.Serializable {

    // Fields    

     protected Integer borrowMaterialId;
     protected String materialNumber;
     protected String description;
     protected Short quantity;
     protected String outComments;
     protected String inComments;
     protected Short quantityResolved;
     protected Short quantityReturned;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     protected Set<BorrowReturnMaterial> borrowReturnMaterials;
     protected Borrow borrow;


    // Constructors

    /** default constructor */
    public BorrowMaterial() {
    }
    
    /** constructor with id */
    public BorrowMaterial(Integer borrowMaterialId) {
        this.borrowMaterialId = borrowMaterialId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        borrowMaterialId = null;
        materialNumber = null;
        description = null;
        quantity = null;
        outComments = null;
        inComments = null;
        quantityResolved = null;
        quantityReturned = null;
        timestampModified = null;
        timestampCreated = Calendar.getInstance().getTime();
        lastEditedBy = null;
        borrowReturnMaterials = new HashSet<BorrowReturnMaterial>();
        borrow = null;
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    public Integer getBorrowMaterialId() {
        return this.borrowMaterialId;
    }
    
    public void setBorrowMaterialId(Integer borrowMaterialId) {
        this.borrowMaterialId = borrowMaterialId;
    }

    /**
     *      * e.g. 'FMNH 223456'
     */
    public String getMaterialNumber() {
        return this.materialNumber;
    }
    
    public void setMaterialNumber(String materialNumber) {
        this.materialNumber = materialNumber;
    }

    /**
     *      * Description of the material. 'e.g. Bufo bufo skull'
     */
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *      * Number of specimens (for lots)
     */
    public Short getQuantity() {
        return this.quantity;
    }
    
    public void setQuantity(Short quantity) {
        this.quantity = quantity;
    }

    /**
     *      * Notes concerning the return of the material
     */
    public String getOutComments() {
        return this.outComments;
    }
    
    public void setOutComments(String outComments) {
        this.outComments = outComments;
    }

    /**
     *      * Notes concerning the receipt of the material
     */
    public String getInComments() {
        return this.inComments;
    }
    
    public void setInComments(String inComments) {
        this.inComments = inComments;
    }

    /**
     *      * Quantity resolved (Returned, Accessioned, Lost, Discarded, Destroyed ...)
     */
    public Short getQuantityResolved() {
        return this.quantityResolved;
    }
    
    public void setQuantityResolved(Short quantityResolved) {
        this.quantityResolved = quantityResolved;
    }

    /**
     *      * Quantity returned
     */
    public Short getQuantityReturned() {
        return this.quantityReturned;
    }
    
    public void setQuantityReturned(Short quantityReturned) {
        this.quantityReturned = quantityReturned;
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
     * 
     */
    public Set<BorrowReturnMaterial> getBorrowReturnMaterials() {
        return this.borrowReturnMaterials;
    }
    
    public void setBorrowReturnMaterials(Set<BorrowReturnMaterial> borrowReturnMaterials) {
        this.borrowReturnMaterials = borrowReturnMaterials;
    }

    /**
     *      * ID of the Borrow containing the Prep
     */
    public Borrow getBorrow() {
        return this.borrow;
    }
    
    public void setBorrow(Borrow borrow) {
        this.borrow = borrow;
    }




    // Add Methods

    public void addBorrowReturnMaterial(final BorrowReturnMaterial borrowReturnMaterial)
    {
        this.borrowReturnMaterials.add(borrowReturnMaterial);
    }

    // Done Add Methods
}
