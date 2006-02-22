package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="loanreturnphysicalobject"
 *     
 */
public class LoanReturnPhysicalObject  implements java.io.Serializable {

    // Fields    

     protected Integer loanReturnPhysicalObjectId;
     protected Integer dateField;
     protected Short quantity;
     protected String remarks;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     private LoanPhysicalObject loanPhysicalObject;
     private DeaccessionCollectionObject deaccessionCollectionObject;
     private Agent agent;


    // Constructors

    /** default constructor */
    public LoanReturnPhysicalObject() {
    }
    
    /** constructor with id */
    public LoanReturnPhysicalObject(Integer loanReturnPhysicalObjectId) {
        this.loanReturnPhysicalObjectId = loanReturnPhysicalObjectId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="LoanReturnPhysicalObjectID"
     *         
     */
    public Integer getLoanReturnPhysicalObjectId() {
        return this.loanReturnPhysicalObjectId;
    }
    
    public void setLoanReturnPhysicalObjectId(Integer loanReturnPhysicalObjectId) {
        this.loanReturnPhysicalObjectId = loanReturnPhysicalObjectId;
    }

    /**
     *      *            @hibernate.property
     *             column="DateField"
     *             length="10"
     *         
     */
    public Integer getDateField() {
        return this.dateField;
    }
    
    public void setDateField(Integer dateField) {
        this.dateField = dateField;
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
     *             column="Remarks"
     *             length="1073741823"
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
     * 			cascade="delete"
     *            @hibernate.column name="LoanPhysicalObjectID"         
     *         
     */
    public LoanPhysicalObject getLoanPhysicalObject() {
        return this.loanPhysicalObject;
    }
    
    public void setLoanPhysicalObject(LoanPhysicalObject loanPhysicalObject) {
        this.loanPhysicalObject = loanPhysicalObject;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="DeaccessionPhysicalObjectID"         
     *         
     */
    public DeaccessionCollectionObject getDeaccessionCollectionObject() {
        return this.deaccessionCollectionObject;
    }
    
    public void setDeaccessionCollectionObject(DeaccessionCollectionObject deaccessionCollectionObject) {
        this.deaccessionCollectionObject = deaccessionCollectionObject;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="ReceivedByID"         
     *         
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }




}