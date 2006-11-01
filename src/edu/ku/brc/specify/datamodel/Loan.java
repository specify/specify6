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

import edu.ku.brc.ui.forms.FormDataObjIFace;




/**

 */
public class Loan extends DataModelObjBase implements java.io.Serializable {

    // options for the 'category' field
    public static final Byte LOAN = 0;
    public static final Byte GIFT = 1;
    
    // Fields    

     protected Long loanId;
     protected String loanNumber;
     protected Calendar loanDate;
     protected Calendar currentDueDate;
     protected Calendar originalDueDate;
     protected Calendar dateClosed;
     protected Byte category;
     protected String remarks;
     protected String text1;
     protected String text2;
     protected Float number1;
     protected Float number2;
     protected Short closed;
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     protected Set<LoanAgents> loanAgents;
     protected Set<LoanPhysicalObject> loanPhysicalObjects;
     protected Shipment shipment;
     protected Set<Attachment> attachments;


    // Constructors

    /** default constructor */
    public Loan()
    {
        // do nothing
    }
    
    /** constructor with id */
    public Loan(Long loanId) {
        this.loanId = loanId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        loanId = null;
        loanNumber = null;
        loanDate = null;
        currentDueDate = null;
        originalDueDate = null;
        dateClosed = null;
        category = null;
        remarks = null;
        text1 = null;
        text2 = null;
        number1 = null;
        number2 = null;
        timestampCreated = new Date();
        timestampModified = null;
        lastEditedBy = null;
        closed = null;
        yesNo1 = null;
        yesNo2 = null;
        loanAgents = new HashSet<LoanAgents>();
        loanPhysicalObjects = new HashSet<LoanPhysicalObject>();
        shipment = null;
        attachments = new HashSet<Attachment>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * PrimaryKey
     */
    public Long getLoanId() {
        return this.loanId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Override
    public Long getId()
    {
        return this.loanId;
    }
    
    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    /**
     *      * Invoice number
     */
    public String getLoanNumber() {
        return this.loanNumber;
    }
    
    public void setLoanNumber(String loanNumber) {
        this.loanNumber = loanNumber;
    }

    /**
     *      * Date the Loan was created.
     */
    public Calendar getLoanDate() {
        return this.loanDate;
    }
    
    public void setLoanDate(Calendar loanDate) {
        this.loanDate = loanDate;
    }

    /**
     *      * Date the loan is due for return (Same as original Due date unless loan period has been extended)
     */
    public Calendar getCurrentDueDate() {
        return this.currentDueDate;
    }
    
    public void setCurrentDueDate(Calendar currentDueDate) {
        this.currentDueDate = currentDueDate;
    }

    /**
     *      * Date the loan was originally due.
     */
    public Calendar getOriginalDueDate() {
        return this.originalDueDate;
    }
    
    public void setOriginalDueDate(Calendar originalDueDate) {
        this.originalDueDate = originalDueDate;
    }

    /**
     *      * Date loan was closed.
     */
    public Calendar getDateClosed() {
        return this.dateClosed;
    }
    
    public void setDateClosed(Calendar dateClosed) {
        this.dateClosed = dateClosed;
    }

    /**
     *      * Type of record: loan(0), Gift(1)
     */
    public Byte getCategory() {
        return this.category;
    }
    
    public void setCategory(Byte category) {
        this.category = category;
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
     *      * 'No' until all preparations in the loan have been returned/resolved.
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
    public Set<LoanAgents> getLoanAgents() {
        return this.loanAgents;
    }
    
    public void setLoanAgents(Set<LoanAgents> loanAgents) {
        this.loanAgents = loanAgents;
    }

    /**
     * 
     */
    public Set<LoanPhysicalObject> getLoanPhysicalObjects() {
        return this.loanPhysicalObjects;
    }
    
    public void setLoanPhysicalObjects(Set<LoanPhysicalObject> loanPhysicalObjects) {
        this.loanPhysicalObjects = loanPhysicalObjects;
    }

    /**
     *      * Link to Shipment table
     */
    public Shipment getShipment() {
        return this.shipment;
    }
    
    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    public Set<Attachment> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(Set<Attachment> attachments)
    {
        this.attachments = attachments;
    }

    public void addLoanAgents(final LoanAgents loanAgent)
    {
        this.loanAgents.add(loanAgent);
        loanAgent.setLoan(this);
    }

    public void addLoanPhysicalObjects(final LoanPhysicalObject loanPhysicalObject)
    {
        this.loanPhysicalObjects.add(loanPhysicalObject);
        loanPhysicalObject.setLoan(this);
    }


    // Done Add Methods

    // Delete Methods

    public void removeLoanAgents(final LoanAgents loanAgent)
    {
        this.loanAgents.remove(loanAgent);
        loanAgent.setLoan(null);
    }

    public void removeLoanPhysicalObjects(final LoanPhysicalObject loanPhysicalObject)
    {
        this.loanPhysicalObjects.remove(loanPhysicalObject);
        loanPhysicalObject.setLoan(null);
    }


    // Delete Add Methods
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    public Integer getTableId()
    {
        return 52;
    }

    @Override
    public void addReference(FormDataObjIFace ref, String refType)
    {
        if( ref instanceof Attachment )
        {
            Attachment a = (Attachment)ref;
            attachments.add(a);
            a.setLoan(this);
            return;
        }
        super.addReference(ref, refType);
    }

    @Override
    public void removeReference(FormDataObjIFace refObj, String refType)
    {
        if( refObj instanceof Attachment )
        {
            attachments.remove(refObj);
            return;
        }
        super.removeReference(refObj, refType);
    }

}
