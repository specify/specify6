package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="borrowreturnmaterial"
 *     
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
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="BorrowReturnMaterialID"
     *         
     */
    public Integer getBorrowReturnMaterialId() {
        return this.borrowReturnMaterialId;
    }
    
    public void setBorrowReturnMaterialId(Integer borrowReturnMaterialId) {
        this.borrowReturnMaterialId = borrowReturnMaterialId;
    }

    /**
     *      *            @hibernate.property
     *             column="ReturnedDate"
     *         
     */
    public Calendar getReturnedDate() {
        return this.returnedDate;
    }
    
    public void setReturnedDate(Calendar returnedDate) {
        this.returnedDate = returnedDate;
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
     *             column="Remarks"
     *         
     */
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *      *            @hibernate.property
     *             column="TimestampModified"
     *             length="23"
     *             not-null="true"
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
     *             update="false"
     *             not-null="true"
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
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="ReturnedByID"         
     *         
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     * 			cascade="delete"
     *            @hibernate.column name="BorrowMaterialID"         
     *         
     */
    public BorrowMaterial getBorrowMaterial() {
        return this.borrowMaterial;
    }
    
    public void setBorrowMaterial(BorrowMaterial borrowMaterial) {
        this.borrowMaterial = borrowMaterial;
    }




}