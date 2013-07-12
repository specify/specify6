/* Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.datamodel;

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "shipment")
@org.hibernate.annotations.Table(appliesTo="shipment", indexes =
    {   @Index (name="ShipmentNumberIDX", columnNames={"ShipmentNumber"}),
        @Index (name="ShipmentDateIDX", columnNames={"ShipmentDate"}),
        @Index (name="ShipmentDspMemIDX", columnNames={"DisciplineID"}),
        @Index (name="ShipmentMethodIDX", columnNames={"ShipmentMethod"})
   })
public class Shipment extends DisciplineMember implements java.io.Serializable 
{

    // Fields    

    protected Integer          shipmentId;
    protected Calendar         shipmentDate;
    protected String           shipmentNumber;
    protected String           shipmentMethod;
    protected Short            numberOfPackages;
    protected String           weight;
    protected String           insuredForAmount;
    protected String           remarks;
    protected String           text1;
    protected String           text2;
    protected Float            number1;
    protected Float            number2;
    protected Boolean          yesNo1;
    protected Boolean          yesNo2;
    protected Agent            shipper;
    protected Agent            shippedTo;
    protected Agent            shippedBy;
    protected Borrow           borrow;
    protected Loan             loan;
    protected Gift             gift;
    protected ExchangeOut      exchangeOut;


    // Constructors

    /** default constructor */
    public Shipment() 
    {
        //
    }
    
    /** constructor with id */
    public Shipment(Integer shipmentId) 
    {
        this.shipmentId = shipmentId;
    }
   
    
    

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        shipmentId      = null;
        shipmentDate     = null;
        shipmentNumber   = null;
        shipmentMethod   = null;
        numberOfPackages = null;
        weight           = null;
        insuredForAmount = null;
        remarks   = null;
        text1     = null;
        text2     = null;
        number1   = null;
        number2   = null;
        yesNo1    = null;
        yesNo2    = null;
        shipper   = null;
        shippedTo = null;
        shippedBy = null;
        //borrowShipments = new HashSet<BorrowShipment>();
        borrow      = null;
        loan        = null;
        gift        = null;
        exchangeOut = null;
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "ShipmentID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getShipmentId() {
        return this.shipmentId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.shipmentId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Shipment.class;
    }
    
    public void setShipmentId(Integer shipmentId) {
        this.shipmentId = shipmentId;
    }

    /**
     *      * Date of shipment
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "ShipmentDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getShipmentDate() {
        return this.shipmentDate;
    }
    
    public void setShipmentDate(Calendar shipmentDate) {
        this.shipmentDate = shipmentDate;
    }

    /**
     *      * Shipper's tracking number
     */
    @Column(name = "ShipmentNumber", unique = false, nullable = false, insertable = true, updatable = true, length = 50)
    public String getShipmentNumber() {
        return this.shipmentNumber;
    }
    
    public void setShipmentNumber(String shipmentNumber) {
        this.shipmentNumber = shipmentNumber;
    }

    /**
     *      * Description of shipment. E.g. 'Hand-carried', 'Overnight', 'Air', 'Land', 'Sea', ...
     */
    @Column(name = "ShipmentMethod", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getShipmentMethod() {
        return this.shipmentMethod;
    }
    
    public void setShipmentMethod(String shipmentMethod) {
        this.shipmentMethod = shipmentMethod;
    }

    /**
     *      * Number of packages shipped
     */
    @Column(name = "NumberOfPackages", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getNumberOfPackages() {
        return this.numberOfPackages;
    }
    
    public void setNumberOfPackages(Short numberOfPackages) {
        this.numberOfPackages = numberOfPackages;
    }

    /**
     *      * The weight of the shipment
     */
    @Column(name = "Weight", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getWeight() {
        return this.weight;
    }
    
    public void setWeight(String weight) {
        this.weight = weight;
    }

    /**
     * 
     */
    @Column(name = "InsuredForAmount", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getInsuredForAmount() {
        return this.insuredForAmount;
    }
    
    public void setInsuredForAmount(String insuredForAmount) {
        this.insuredForAmount = insuredForAmount;
    }

    /**
     * 
     */
    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *      * User definable
     */
    @Column(name = "Text1", length=300, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    @Column(name = "Text2", length=300, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText2() {
        return this.text2;
    }
    
    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      * User definable
     */
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
    public Float getNumber1() {
        return this.number1;
    }
    
    public void setNumber1(Float number1) {
        this.number1 = number1;
    }

    /**
     *      * User definable
     */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
    public Float getNumber2() {
        return this.number2;
    }
    
    public void setNumber2(Float number2) {
        this.number2 = number2;
    }

    /**
     *      * User definable
     */
    @Column(name="YesNo1",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo1() {
        return this.yesNo1;
    }
    
    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      * User definable
     */
    @Column(name="YesNo2",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo2() {
        return this.yesNo2;
    }
    
    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     *      * AgentID of agent transporting the material
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ShipperID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getShipper() {
        return this.shipper;
    }
    
    public void setShipper(Agent agentByShipper) {
        this.shipper = agentByShipper;
    }

    /**
     *      * AgentID of agent material is shipped to
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ShippedToID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getShippedTo() {
        return this.shippedTo;
    }
    
    public void setShippedTo(Agent shippedTo) {
        this.shippedTo = shippedTo;
    }

    /**
     *      * AgentID of person approving/initiating the shipment
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ShippedByID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getShippedBy() {
        return this.shippedBy;
    }
    
    public void setShippedBy(Agent agent) {
        this.shippedBy = agent;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "BorrowID", unique = false, nullable = true, insertable = true, updatable = true)
    public Borrow getBorrow() {
        return this.borrow;
    }
    
    public void setBorrow(Borrow borrow) {
        this.borrow = borrow;
    }
    
    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "LoanID", unique = false, nullable = true, insertable = true, updatable = true)
    public Loan getLoan() {
        return this.loan;
    }
    
    public void setLoan(Loan loan) 
    {
        this.loan = loan;
    }
    
    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "GiftID", unique = false, nullable = true, insertable = true, updatable = true)
    public Gift getGift() {
        return this.gift;
    }
    
    public void setGift(Gift gift) 
    {
        this.gift = gift;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ExchangeOutID", unique = false, nullable = true, insertable = true, updatable = true)
    public ExchangeOut getExchangeOut() {
        return this.exchangeOut;
    }
    
    public void setExchangeOut(ExchangeOut exchangeOut) {
        this.exchangeOut = exchangeOut;
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
   public Integer getParentTableId()
    {
        if (borrow != null)
        {
            return Borrow.getClassTableId();
        }
        if (loan != null)
        {
            return Loan.getClassTableId();
        }
        if (exchangeOut != null)
        {
            return ExchangeOut.getClassTableId();
        }
        if (gift != null)
        {
            return Gift.getClassTableId();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        if (borrow != null)
        {
            return borrow.getId();
        }
        if (loan != null)
        {
            return loan.getId();
        }
        if (exchangeOut != null)
        {
            return exchangeOut.getId();
        }
        if (gift != null)
        {
            return gift.getId();
        }
        return null;
    }
    
    /* (non-Javadoc)
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
        return 71;
    }

}
