package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="loanphysicalobject"
 *     
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
     private CollectionObj collectionObjectCatalog;
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
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="LoanPhysicalObjectID"
     *         
     */
    public Integer getLoanPhysicalObjectId() {
        return this.loanPhysicalObjectId;
    }
    
    public void setLoanPhysicalObjectId(Integer loanPhysicalObjectId) {
        this.loanPhysicalObjectId = loanPhysicalObjectId;
    }

    /**
     *      *            @hibernate.property
     *             column="Quantity"
     *             length="5"
     *         
     */
    public Short getQuantity() {
        return this.quantity;
    }
    
    public void setQuantity(Short quantity) {
        this.quantity = quantity;
    }

    /**
     *      *            @hibernate.property
     *             column="DescriptionOfMaterial"
     *             length="255"
     *         
     */
    public String getDescriptionOfMaterial() {
        return this.descriptionOfMaterial;
    }
    
    public void setDescriptionOfMaterial(String descriptionOfMaterial) {
        this.descriptionOfMaterial = descriptionOfMaterial;
    }

    /**
     *      *            @hibernate.property
     *             column="OutComments"
     *             length="1073741823"
     *         
     */
    public String getOutComments() {
        return this.outComments;
    }
    
    public void setOutComments(String outComments) {
        this.outComments = outComments;
    }

    /**
     *      *            @hibernate.property
     *             column="InComments"
     *             length="1073741823"
     *         
     */
    public String getInComments() {
        return this.inComments;
    }
    
    public void setInComments(String inComments) {
        this.inComments = inComments;
    }

    /**
     *      *            @hibernate.property
     *             column="QuantityResolved"
     *             length="5"
     *         
     */
    public Short getQuantityResolved() {
        return this.quantityResolved;
    }
    
    public void setQuantityResolved(Short quantityResolved) {
        this.quantityResolved = quantityResolved;
    }

    /**
     *      *            @hibernate.property
     *             column="QuantityReturned"
     *             length="5"
     *         
     */
    public Short getQuantityReturned() {
        return this.quantityReturned;
    }
    
    public void setQuantityReturned(Short quantityReturned) {
        this.quantityReturned = quantityReturned;
    }

    /**
     *      *            @hibernate.property
     *             column="TimestampCreated"
     *             length="23"
     *         
     */
    public Date getTimestampCreated() {
        return this.timestampCreated;
    }
    
    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    /**
     *      *            @hibernate.property
     *             column="TimestampModified"
     *             length="23"
     *         
     */
    public Date getTimestampModified() {
        return this.timestampModified;
    }
    
    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
    }

    /**
     *      *            @hibernate.property
     *             column="LastEditedBy"
     *             length="50"
     *         
     */
    public String getLastEditedBy() {
        return this.lastEditedBy;
    }
    
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="PhysicalObjectID"         
     *         
     */
    public CollectionObj getCollectionObjectCatalog() {
        return this.collectionObjectCatalog;
    }
    
    public void setCollectionObjectCatalog(CollectionObj collectionObjectCatalog) {
        this.collectionObjectCatalog = collectionObjectCatalog;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     * 			cascade="delete"
     *            @hibernate.column name="LoanID"         
     *         
     */
    public Loan getLoan() {
        return this.loan;
    }
    
    public void setLoan(Loan loan) {
        this.loan = loan;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="LoanPhysicalObjectID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.LoanReturnPhysicalObject"
     *         
     */
    public Set getLoanReturnPhysicalObjects() {
        return this.loanReturnPhysicalObjects;
    }
    
    public void setLoanReturnPhysicalObjects(Set loanReturnPhysicalObjects) {
        this.loanReturnPhysicalObjects = loanReturnPhysicalObjects;
    }




}