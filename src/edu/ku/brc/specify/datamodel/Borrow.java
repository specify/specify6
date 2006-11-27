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

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;




/**

 */
public class Borrow extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long borrowId;
     protected String invoiceNumber;
     protected Calendar receivedDate;
     protected Calendar originalDueDate;
     protected Calendar dateClosed;
     protected String remarks;
     protected String text1;
     protected String text2;
     protected Float number1;
     protected Float number2;
     protected Short closed;
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     protected Calendar currentDueDate;
     protected Set<BorrowShipments> borrowShipments;
     protected Set<BorrowAgents> borrowAgents;
     protected Set<BorrowMaterial> borrowMaterials;


    // Constructors

    /** default constructor */
    public Borrow() {
    }
    
    /** constructor with id */
    public Borrow(Long borrowId) {
        this.borrowId = borrowId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        borrowId = null;
        invoiceNumber = null;
        receivedDate = null;
        originalDueDate = null;
        dateClosed = null;
        remarks = null;
        text1 = null;
        text2 = null;
        number1 = null;
        number2 = null;
        timestampModified = null;
        timestampCreated = new Date();
        lastEditedBy = null;
        closed = null;
        yesNo1 = null;
        yesNo2 = null;
        currentDueDate = null;
        borrowShipments = new HashSet<BorrowShipments>();
        borrowAgents = new HashSet<BorrowAgents>();
        borrowMaterials = new HashSet<BorrowMaterial>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    public Long getBorrowId() {
        return this.borrowId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.borrowId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    public Class getDataClass()
    {
        return Borrow.class;
    }
    
    public void setBorrowId(Long borrowId) {
        this.borrowId = borrowId;
    }

    /**
     *      * Lender's loan number
     */
    public String getInvoiceNumber() {
        return this.invoiceNumber;
    }
    
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    /**
     *      * Date material was received
     */
    public Calendar getReceivedDate() {
        return this.receivedDate;
    }
    
    public void setReceivedDate(Calendar receivedDate) {
        this.receivedDate = receivedDate;
    }

    /**
     *      * Original Due date for loan
     */
    public Calendar getOriginalDueDate() {
        return this.originalDueDate;
    }
    
    public void setOriginalDueDate(Calendar originalDueDate) {
        this.originalDueDate = originalDueDate;
    }

    /**
     *      * Date loan was closed
     */
    public Calendar getDateClosed() {
        return this.dateClosed;
    }
    
    public void setDateClosed(Calendar dateClosed) {
        this.dateClosed = dateClosed;
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
     *      * User definable
     */
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    public String getText2() {
        return this.text2;
    }
    
    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      * User definable
     */
    public Float getNumber1() {
        return this.number1;
    }
    
    public void setNumber1(Float number1) {
        this.number1 = number1;
    }

    /**
     *      * User definable
     */
    public Float getNumber2() {
        return this.number2;
    }
    
    public void setNumber2(Float number2) {
        this.number2 = number2;
    }


    /**
     *      * False until all material has been returned
     */
    public Short getClosed() {
        return this.closed;
    }
    
    public void setClosed(Short closed) {
        this.closed = closed;
    }

    /**
     *      * User definable
     */
    public Boolean getYesNo1() {
        return this.yesNo1;
    }
    
    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      * User definable
     */
    public Boolean getYesNo2() {
        return this.yesNo2;
    }
    
    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     * 
     */
    public Calendar getCurrentDueDate() {
        return this.currentDueDate;
    }
    
    public void setCurrentDueDate(Calendar currentDueDate) {
        this.currentDueDate = currentDueDate;
    }

    /**
     * 
     */
    public Set<BorrowShipments> getBorrowShipments() {
        return this.borrowShipments;
    }
    
    public void setBorrowShipments(Set<BorrowShipments> borrowShipments) {
        this.borrowShipments = borrowShipments;
    }

    /**
     * 
     */
    public Set<BorrowAgents> getBorrowAgents() {
        return this.borrowAgents;
    }
    
    public void setBorrowAgents(Set<BorrowAgents> borrowAgents) {
        this.borrowAgents = borrowAgents;
    }

    /**
     * 
     */
    public Set<BorrowMaterial> getBorrowMaterials() {
        return this.borrowMaterials;
    }
    
    public void setBorrowMaterials(Set<BorrowMaterial> borrowMaterials) {
        this.borrowMaterials = borrowMaterials;
    }





    // Add Methods

    public void addBorrowShipments(final BorrowShipments borrowShipment)
    {
        this.borrowShipments.add(borrowShipment);
        borrowShipment.setBorrow(this);
    }

    public void addBorrowAgents(final BorrowAgents borrowAgent)
    {
        this.borrowAgents.add(borrowAgent);
        borrowAgent.setBorrow(this);
    }

    public void addBorrowMaterials(final BorrowMaterial borrowMaterial)
    {
        this.borrowMaterials.add(borrowMaterial);
        borrowMaterial.setBorrow(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeBorrowShipments(final BorrowShipments borrowShipment)
    {
        this.borrowShipments.remove(borrowShipment);
        borrowShipment.setBorrow(null);
    }

    public void removeBorrowAgents(final BorrowAgents borrowAgent)
    {
        this.borrowAgents.remove(borrowAgent);
        borrowAgent.setBorrow(null);
    }

    public void removeBorrowMaterials(final BorrowMaterial borrowMaterial)
    {
        this.borrowMaterials.remove(borrowMaterial);
        borrowMaterial.setBorrow(null);
    }

    // Delete Add Methods
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    public Integer getTableId()
    {
        return 18;
    }

}
