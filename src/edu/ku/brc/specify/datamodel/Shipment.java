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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@Table(name = "shipment")
public class Shipment extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long shipmentId;
     protected Calendar shipmentDate;
     protected String shipmentNumber;
     protected String shipmentMethod;
     protected Short numberOfPackages;
     protected String weight;
     protected String insuredForAmount;
     protected String remarks;
     protected String text1;
     protected String text2;
     protected Float number1;
     protected Float number2;
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     protected Agent shipper;
     protected Agent shippedTo;
     protected Agent shippedBy;
     //protected Set<BorrowShipments> borrowShipments;
     protected Set<Borrow> borrows;
     protected Set<Loan> loans;
     protected Set<ExchangeOut> exchangeOuts;


    // Constructors

    /** default constructor */
    public Shipment() {
        //
    }
    
    /** constructor with id */
    public Shipment(Long shipmentId) {
        this.shipmentId = shipmentId;
    }
   
    
    

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        shipmentId = null;
        shipmentDate = null;
        shipmentNumber = null;
        shipmentMethod = null;
        numberOfPackages = null;
        weight = null;
        insuredForAmount = null;
        remarks = null;
        text1 = null;
        text2 = null;
        number1 = null;
        number2 = null;
        yesNo1 = null;
        yesNo2 = null;
        shipper = null;
        shippedTo = null;
        shippedBy = null;
        //borrowShipments = new HashSet<BorrowShipments>();
        borrows = new HashSet<Borrow>();
        loans = new HashSet<Loan>();
        exchangeOuts = new HashSet<ExchangeOut>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "ShipmentID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getShipmentId() {
        return this.shipmentId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
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
    
    public void setShipmentId(Long shipmentId) {
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
    @Column(name = "Remarks", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *      * User definable
     */
    @Column(name = "Text1", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    @Column(name = "Text2", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
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
    @Cascade( { CascadeType.SAVE_UPDATE })
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
    @Cascade( { CascadeType.SAVE_UPDATE })
    @JoinColumn(name = "ShippedToID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getShippedTo() {
        return this.shippedTo;
    }
    
    public void setShippedTo(Agent agentByShippedTo) {
        this.shippedTo = agentByShippedTo;
    }

    /**
     *      * AgentID of person approving/initiating the shipment
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE })
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
//    public Set<BorrowShipments> getBorrowShipments() {
//        return this.borrowShipments;
//    }
//    
//    public void setBorrowShipments(Set<BorrowShipments> borrowShipments) {
//        this.borrowShipments = borrowShipments;
//    }
    /**
     * 
     */
    @ManyToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy="shipments")
    @Cascade( { CascadeType.SAVE_UPDATE })
    public Set<Borrow> getBorrows() {
        return this.borrows;
    }
    
    public void setBorrows(Set<Borrow> borrows) {
        this.borrows = borrows;
    }
    /**
     * 
     */
    @ManyToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy="shipments")
    @Cascade( { CascadeType.SAVE_UPDATE })
    public Set<Loan> getLoans() {
        return this.loans;
    }
    
    public void setLoans(Set<Loan> loans) {
        this.loans = loans;
    }

    /**
     * 
     */
    @ManyToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy="shipments")
    @Cascade( { CascadeType.SAVE_UPDATE })
    public Set<ExchangeOut> getExchangeOuts() {
        return this.exchangeOuts;
    }
    
    public void setExchangeOuts(Set<ExchangeOut> exchangeOuts) {
        this.exchangeOuts = exchangeOuts;
    }





    // Add Methods

//    public void addBorrowShipments(final BorrowShipments borrowShipment)
//    {
//        this.borrowShipments.add(borrowShipment);
//        borrowShipment.setShipment(this);
//    }
  public void addBorrows(final Borrow borrow)
  {
      this.borrows.add(borrow);
      borrow.getShipments().add(this);
  }
    public void addLoans(final Loan loan)
    {
        this.loans.add(loan);
        loan.getShipments().add(this);
    }

    public void addExchangeOuts(final ExchangeOut exchangeOut)
    {
        this.exchangeOuts.add(exchangeOut);
        //exchangeOut.setShipments(this);
        exchangeOut.getShipments().add(this);
    }

    // Done Add Methods

    // Delete Methods

//    public void removeBorrowShipments(final BorrowShipments borrowShipment)
//    {
//        this.borrowShipments.remove(borrowShipment);
//        borrowShipment.setShipment(null);
//    }
  public void removeBorrow(final Borrow borrow)
  {
      this.borrows.remove(borrow);
      borrow.removeShipment(this);
  }
    public void removeLoan(final Loan loan)
    {
        this.loans.remove(loan);
        loan.removeShipment(this);
    }

    public void removeExchangeOuts(final ExchangeOut exchangeOut)
    {
        this.exchangeOuts.remove(exchangeOut);
        exchangeOut.setShipments(null);
    }

    // Delete Add Methods
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public Integer getTableId()
    {
        return 71;
    }

}
