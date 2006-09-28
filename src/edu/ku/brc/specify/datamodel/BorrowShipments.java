/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.datamodel;

import java.util.Date;




/**

 */
public class BorrowShipments extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long borrowShipmentsId;
     protected String remarks;
     protected Shipment shipment;
     protected Borrow borrow;


    // Constructors

    /** default constructor */
    public BorrowShipments() {
    }
    
    /** constructor with id */
    public BorrowShipments(Long borrowShipmentsId) {
        this.borrowShipmentsId = borrowShipmentsId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        borrowShipmentsId = null;
        remarks = null;
        timestampModified = null;
        timestampCreated = new Date();
        lastEditedBy = null;
        shipment = null;
        borrow = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Long getBorrowShipmentsId() {
        return this.borrowShipmentsId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.borrowShipmentsId;
    }
    
    public void setBorrowShipmentsId(Long borrowShipmentsId) {
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





    // Add Methods

    // Done Add Methods

    // Delete Methods

    // Delete Add Methods
}
