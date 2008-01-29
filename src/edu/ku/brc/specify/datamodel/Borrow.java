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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "borrow")
@org.hibernate.annotations.Table(appliesTo="borrow", indexes =
    {   @Index (name="InvoiceNumberIDX", columnNames={"InvoiceNumber"}),
        @Index (name="ReceivedDateIDX", columnNames={"ReceivedDate"})
    })
public class Borrow extends CollectionMember implements java.io.Serializable {

    // Fields    

    protected Integer             borrowId;
    protected String              invoiceNumber;
    protected Calendar            receivedDate;
    protected Calendar            originalDueDate;
    protected Calendar            dateClosed;
    protected String              remarks;
    protected String              text1;
    protected String              text2;
    protected Float               number1;
    protected Float               number2;
    protected Boolean             isClosed;
    protected Boolean             yesNo1;
    protected Boolean             yesNo2;
    protected Calendar            currentDueDate;
    protected Boolean             isFinancialResponsibility;
    
    protected AddressOfRecord     addressOfRecord;
    protected Set<Shipment>       shipments;
    protected Set<BorrowAgent>    borrowAgents;
    protected Set<BorrowMaterial> borrowMaterials;


    // Constructors

    /** default constructor */
    public Borrow()
    {
        //
    }

    /** constructor with id */
    public Borrow(Integer borrowId)
    {
        this.borrowId = borrowId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
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
        isClosed = null;
        yesNo1 = null;
        yesNo2 = null;
        currentDueDate = null;
        addressOfRecord = null;
        shipments = new HashSet<Shipment>();
        borrowAgents = new HashSet<BorrowAgent>();
        borrowMaterials = new HashSet<BorrowMaterial>();
    }

    // End Initializer

    // Property accessors

    /**
     * * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "BorrowID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getBorrowId()
    {
        return this.borrowId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Override
    @Transient
    public Integer getId()
    {
        return this.borrowId;
    }

    /*
     * (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Borrow.class;
    }

    public void setBorrowId(Integer borrowId)
    {
        this.borrowId = borrowId;
    }

    /**
     * * Lender's loan number
     */
    @Column(name = "InvoiceNumber", unique = false, nullable = false, insertable = true, updatable = true, length = 50)
    public String getInvoiceNumber()
    {
        return this.invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber)
    {
        this.invoiceNumber = invoiceNumber;
    }

    /**
     * * Date material was received
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "ReceivedDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getReceivedDate()
    {
        return this.receivedDate;
    }

    public void setReceivedDate(Calendar receivedDate)
    {
        this.receivedDate = receivedDate;
    }

    /**
     * * Original Due date for loan
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "OriginalDueDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getOriginalDueDate()
    {
        return this.originalDueDate;
    }

    public void setOriginalDueDate(Calendar originalDueDate)
    {
        this.originalDueDate = originalDueDate;
    }

    /**
     * * Date loan was closed
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DateClosed", unique = false, nullable = true, insertable = true, updatable = true, length = 10)
    public Calendar getDateClosed()
    {
        return this.dateClosed;
    }

    public void setDateClosed(Calendar dateClosed)
    {
        this.dateClosed = dateClosed;
    }

    /**
     * @return the isFinancialResponsibility
     */
    @Column(name = "IsFinancialResponsibility", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIsFinancialResponsibility()
    {
        return isFinancialResponsibility;
    }

    /**
     * @param isFinancialResonsibility the isFinancialResonsibility to set
     */
    public void setIsFinancialResponsibility(Boolean isFinancialResonsibility)
    {
        this.isFinancialResponsibility = isFinancialResonsibility;
    }

    /**
     * 
     */
    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks()
    {
        return this.remarks;
    }

    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    /**
     * * User definable
     */
    @Column(name = "Text1", length = 300, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText1()
    {
        return this.text1;
    }

    public void setText1(String text1)
    {
        this.text1 = text1;
    }

    /**
     * * User definable
     */
    @Column(name = "Text2", length = 300, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText2()
    {
        return this.text2;
    }

    public void setText2(String text2)
    {
        this.text2 = text2;
    }

    /**
     * * User definable
     */
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
    public Float getNumber1()
    {
        return this.number1;
    }

    public void setNumber1(Float number1)
    {
        this.number1 = number1;
    }

    /**
     * * User definable
     */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
    public Float getNumber2()
    {
        return this.number2;
    }

    public void setNumber2(Float number2)
    {
        this.number2 = number2;
    }

    /**
     * * False until all material has been returned
     */
    @Column(name = "IsClosed", unique = false, nullable = true, insertable = true, updatable = false)
    public Boolean getIsClosed()
    {
        return this.isClosed;
    }

    public void setIsClosed(Boolean closed)
    {
        this.isClosed = closed;
    }

    /**
     * * User definable
     */
    @Column(name = "YesNo1", unique = false, nullable = true, updatable = true, insertable = true)
    public Boolean getYesNo1()
    {
        return this.yesNo1;
    }

    public void setYesNo1(Boolean yesNo1)
    {
        this.yesNo1 = yesNo1;
    }

    /**
     * * User definable
     */
    @Column(name = "YesNo2", unique = false, nullable = true, updatable = true, insertable = true)
    public Boolean getYesNo2()
    {
        return this.yesNo2;
    }

    public void setYesNo2(Boolean yesNo2)
    {
        this.yesNo2 = yesNo2;
    }

    /**
     * 
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "CurrentDueDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getCurrentDueDate()
    {
        return this.currentDueDate;
    }

    public void setCurrentDueDate(Calendar currentDueDate)
    {
        this.currentDueDate = currentDueDate;
    }

    /**
     * 
     */
    // public Set<BorrowShipment> getBorrowShipments() {
    // return this.borrowShipments;
    // }
    //    
    // public void setBorrowShipments(Set<BorrowShipment> borrowShipments) {
    // this.borrowShipments = borrowShipments;
    // }
    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "borrow")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<Shipment> getShipments()
    {
        return this.shipments;
    }

    public void setShipments(Set<Shipment> shipments)
    {
        this.shipments = shipments;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "borrow")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<BorrowAgent> getBorrowAgents()
    {
        return this.borrowAgents;
    }

    public void setBorrowAgents(Set<BorrowAgent> borrowAgents)
    {
        this.borrowAgents = borrowAgents;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "borrow")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<BorrowMaterial> getBorrowMaterials()
    {
        return this.borrowMaterials;
    }

    public void setBorrowMaterials(Set<BorrowMaterial> borrowMaterials)
    {
        this.borrowMaterials = borrowMaterials;
    }

    /**
     * @return the addressOfRecord
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AddressOfRecordID", unique = false, nullable = true, insertable = true, updatable = true)
    public AddressOfRecord getAddressOfRecord()
    {
        return addressOfRecord;
    }

    /**
     * @param addressOfRecord the addressOfRecord to set
     */
    public void setAddressOfRecord(AddressOfRecord addressOfRecord)
    {
        this.addressOfRecord = addressOfRecord;
    }
    /*
     * (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public int getTableId()
    {
        return getClassTableId();
    }

    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 18;
    }

}
