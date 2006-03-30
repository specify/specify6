package edu.ku.brc.specify.datamodel;

import java.util.Date;




/**

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
     * 
     */
    public Integer getBorrowShipmentsId() {
        return this.borrowShipmentsId;
    }
    
    public void setBorrowShipmentsId(Integer borrowShipmentsId) {
        this.borrowShipmentsId = borrowShipmentsId;
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
     *      * The shipment
     */
    public Shipment getShipment() {
        return this.shipment;
    }
    
    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    /**
     *      * The borrow being shipped (returned)
     */
    public Borrow getBorrow() {
        return this.borrow;
    }
    
    public void setBorrow(Borrow borrow) {
        this.borrow = borrow;
    }




}