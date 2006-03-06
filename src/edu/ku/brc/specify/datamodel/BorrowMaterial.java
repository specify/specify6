package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="borrowmaterial"
 *     
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
     private Set borrowReturnMaterials;
     private Borrow borrow;


    // Constructors

    /** default constructor */
    public BorrowMaterial() {
    }
    
    /** constructor with id */
    public BorrowMaterial(Integer borrowMaterialId) {
        this.borrowMaterialId = borrowMaterialId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="BorrowMaterialID"
     *         
     */
    public Integer getBorrowMaterialId() {
        return this.borrowMaterialId;
    }
    
    public void setBorrowMaterialId(Integer borrowMaterialId) {
        this.borrowMaterialId = borrowMaterialId;
    }

    /**
     *      *            @hibernate.property
     *             column="MaterialNumber"
     *             length="50"
     *             not-null="true"
     *         
     */
    public String getMaterialNumber() {
        return this.materialNumber;
    }
    
    public void setMaterialNumber(String materialNumber) {
        this.materialNumber = materialNumber;
    }

    /**
     *      *            @hibernate.property
     *             column="Description"
     *             length="50"
     *         
     */
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *      *            @hibernate.property
     *             column="Quantity"
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
     *             column="OutComments"
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
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="BorrowMaterialID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.BorrowReturnMaterial"
     *         
     */
    public Set getBorrowReturnMaterials() {
        return this.borrowReturnMaterials;
    }
    
    public void setBorrowReturnMaterials(Set borrowReturnMaterials) {
        this.borrowReturnMaterials = borrowReturnMaterials;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     * 			cascade="delete"
     *            @hibernate.column name="BorrowID"         
     *         
     */
    public Borrow getBorrow() {
        return this.borrow;
    }
    
    public void setBorrow(Borrow borrow) {
        this.borrow = borrow;
    }




}