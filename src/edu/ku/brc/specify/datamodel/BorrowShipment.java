package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="borrowshipments"
 *     
 */
public class BorrowShipment  implements java.io.Serializable {

    // Fields    

     protected Integer borrowShipmentsId;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     private Shipment shipment;
     private Borrow borrow;


    // Constructors

    /** default constructor */
    public BorrowShipment() {
    }
    
    /** constructor with id */
    public BorrowShipment(Integer borrowShipmentsId) {
        this.borrowShipmentsId = borrowShipmentsId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="native"
     *             type="java.lang.Integer"
     *             column="BorrowShipmentsID"
     *         
     */
    public Integer getBorrowShipmentsId() {
        return this.borrowShipmentsId;
    }
    
    public void setBorrowShipmentsId(Integer borrowShipmentsId) {
        this.borrowShipmentsId = borrowShipmentsId;
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
     *      *            @hibernate.many-to-one
     *             not-null="true"
     * 			cascade="none"
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