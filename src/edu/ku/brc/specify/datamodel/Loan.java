package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.Set;




/**
 *        @hibernate.class
 *         table="loan"
 *     
 */
public class Loan  implements java.io.Serializable {

    // Fields    

     protected Integer loanId;
     protected Integer collectionId;
     protected String loanNumber;
     protected Integer loanDate;
     protected Integer currentDueDate;
     protected Integer originalDueDate;
     protected Integer dateClosed;
     protected Byte category;
     protected String remarks;
     protected String text1;
     protected String text2;
     protected Float number1;
     protected Float number2;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected Short closed;
     protected Short yesNo1;
     protected Short yesNo2;
     private Set loanAgents;
     private Set loanPhysicalObjects;
     private Shipment shipment;
     private Set externalResources;


    // Constructors

    /** default constructor */
    public Loan() {
    }
    
    /** constructor with id */
    public Loan(Integer loanId) {
        this.loanId = loanId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="LoanID"
     *         
     */
    public Integer getLoanId() {
        return this.loanId;
    }
    
    public void setLoanId(Integer loanId) {
        this.loanId = loanId;
    }

    /**
     *      *            @hibernate.property
     *             column="CollectionID"
     *             length="10"
     *             not-null="true"
     *         
     */
    public Integer getCollectionId() {
        return this.collectionId;
    }
    
    public void setCollectionId(Integer collectionId) {
        this.collectionId = collectionId;
    }

    /**
     *      *            @hibernate.property
     *             column="LoanNumber"
     *             length="50"
     *             not-null="true"
     *         
     */
    public String getLoanNumber() {
        return this.loanNumber;
    }
    
    public void setLoanNumber(String loanNumber) {
        this.loanNumber = loanNumber;
    }

    /**
     *      *            @hibernate.property
     *             column="LoanDate"
     *             length="10"
     *         
     */
    public Integer getLoanDate() {
        return this.loanDate;
    }
    
    public void setLoanDate(Integer loanDate) {
        this.loanDate = loanDate;
    }

    /**
     *      *            @hibernate.property
     *             column="CurrentDueDate"
     *             length="10"
     *         
     */
    public Integer getCurrentDueDate() {
        return this.currentDueDate;
    }
    
    public void setCurrentDueDate(Integer currentDueDate) {
        this.currentDueDate = currentDueDate;
    }

    /**
     *      *            @hibernate.property
     *             column="OriginalDueDate"
     *             length="10"
     *         
     */
    public Integer getOriginalDueDate() {
        return this.originalDueDate;
    }
    
    public void setOriginalDueDate(Integer originalDueDate) {
        this.originalDueDate = originalDueDate;
    }

    /**
     *      *            @hibernate.property
     *             column="DateClosed"
     *             length="10"
     *         
     */
    public Integer getDateClosed() {
        return this.dateClosed;
    }
    
    public void setDateClosed(Integer dateClosed) {
        this.dateClosed = dateClosed;
    }

    /**
     *      *            @hibernate.property
     *             column="Category"
     *             length="3"
     *         
     */
    public Byte getCategory() {
        return this.category;
    }
    
    public void setCategory(Byte category) {
        this.category = category;
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
     *             column="Text1"
     *             length="300"
     *         
     */
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      *            @hibernate.property
     *             column="Text2"
     *             length="300"
     *         
     */
    public String getText2() {
        return this.text2;
    }
    
    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      *            @hibernate.property
     *             column="Number1"
     *             length="24"
     *         
     */
    public Float getNumber1() {
        return this.number1;
    }
    
    public void setNumber1(Float number1) {
        this.number1 = number1;
    }

    /**
     *      *            @hibernate.property
     *             column="Number2"
     *             length="24"
     *         
     */
    public Float getNumber2() {
        return this.number2;
    }
    
    public void setNumber2(Float number2) {
        this.number2 = number2;
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
     *      *            @hibernate.property
     *             column="Closed"
     *             length="5"
     *         
     */
    public Short getClosed() {
        return this.closed;
    }
    
    public void setClosed(Short closed) {
        this.closed = closed;
    }

    /**
     *      *            @hibernate.property
     *             column="YesNo1"
     *             length="5"
     *         
     */
    public Short getYesNo1() {
        return this.yesNo1;
    }
    
    public void setYesNo1(Short yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      *            @hibernate.property
     *             column="YesNo2"
     *             length="5"
     *         
     */
    public Short getYesNo2() {
        return this.yesNo2;
    }
    
    public void setYesNo2(Short yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="LoanID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.LoanAgent"
     *         
     */
    public Set getLoanAgents() {
        return this.loanAgents;
    }
    
    public void setLoanAgents(Set loanAgents) {
        this.loanAgents = loanAgents;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="LoanID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.LoanPhysicalObject"
     *         
     */
    public Set getLoanPhysicalObjects() {
        return this.loanPhysicalObjects;
    }
    
    public void setLoanPhysicalObjects(Set loanPhysicalObjects) {
        this.loanPhysicalObjects = loanPhysicalObjects;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="ShipmentID"         
     *         
     */
    public Shipment getShipment() {
        return this.shipment;
    }
    
    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    /**
     * 
     */
    public Set getExternalResources() {
        return this.externalResources;
    }
    
    public void setExternalResources(Set externalResources) {
        this.externalResources = externalResources;
    }




}