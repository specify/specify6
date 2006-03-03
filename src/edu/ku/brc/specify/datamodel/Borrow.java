package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.Set;




/**
 *        @hibernate.class
 *         table="borrow"
 *     
 */
public class Borrow  implements java.io.Serializable {

    // Fields    

     protected Integer borrowId;
     private Integer collectionId;
     private String invoiceNumber;
     private Integer receivedDate;
     private Integer originalDueDate;
     private Integer dateClosed;
     protected String remarks;
     private String text1;
     private String text2;
     private Float number1;
     private Float number2;
     private Date timestampModified;
     private Date timestampCreated;
     private String lastEditedBy;
     private Short closed;
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     private Integer currentDueDate;
     private Set borrowShipments;
     private Set borrowAgents;
     private Set borrowMaterials;


    // Constructors

    /** default constructor */
    public Borrow() {
    }
    
    /** constructor with id */
    public Borrow(Integer borrowId) {
        this.borrowId = borrowId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="BorrowID"
     *         
     */
    public Integer getBorrowId() {
        return this.borrowId;
    }
    
    public void setBorrowId(Integer borrowId) {
        this.borrowId = borrowId;
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
     *             column="InvoiceNumber"
     *             length="50"
     *             not-null="true"
     *         
     */
    public String getInvoiceNumber() {
        return this.invoiceNumber;
    }
    
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    /**
     *      *            @hibernate.property
     *             column="ReceivedDate"
     *             length="10"
     *         
     */
    public Integer getReceivedDate() {
        return this.receivedDate;
    }
    
    public void setReceivedDate(Integer receivedDate) {
        this.receivedDate = receivedDate;
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
     *      *            @hibernate.property
     *             column="Closed"
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
     *         
     */
    public Boolean getYesNo1() {
        return this.yesNo1;
    }
    
    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      *            @hibernate.property
     *             column="YesNo2"
     *         
     */
    public Boolean getYesNo2() {
        return this.yesNo2;
    }
    
    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
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
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="BorrowID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.BorrowShipment"
     *         
     */
    public Set getBorrowShipments() {
        return this.borrowShipments;
    }
    
    public void setBorrowShipments(Set borrowShipments) {
        this.borrowShipments = borrowShipments;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="BorrowID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.BorrowAgent"
     *         
     */
    public Set getBorrowAgents() {
        return this.borrowAgents;
    }
    
    public void setBorrowAgents(Set borrowAgents) {
        this.borrowAgents = borrowAgents;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="BorrowID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.BorrowMaterial"
     *         
     */
    public Set getBorrowMaterials() {
        return this.borrowMaterials;
    }
    
    public void setBorrowMaterials(Set borrowMaterials) {
        this.borrowMaterials = borrowMaterials;
    }




}