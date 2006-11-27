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
     protected Agent agentByShipper;
     protected Agent agentByShippedTo;
     protected Agent agent;
     protected Set<BorrowShipments> borrowShipments;
     protected Set<Loan> loans;
     protected Set<ExchangeOut> exchangeOuts;


    // Constructors

    /** default constructor */
    public Shipment() {
    }
    
    /** constructor with id */
    public Shipment(Long shipmentId) {
        this.shipmentId = shipmentId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
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
        timestampCreated = new Date();
        timestampModified = null;
        lastEditedBy = null;
        yesNo1 = null;
        yesNo2 = null;
        agentByShipper = null;
        agentByShippedTo = null;
        agent = null;
        borrowShipments = new HashSet<BorrowShipments>();
        loans = new HashSet<Loan>();
        exchangeOuts = new HashSet<ExchangeOut>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    public Long getShipmentId() {
        return this.shipmentId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.shipmentId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    public Class getDataClass()
    {
        return Shipment.class;
    }
    
    public void setShipmentId(Long shipmentId) {
        this.shipmentId = shipmentId;
    }

    /**
     *      * Date of shipment
     */
    public Calendar getShipmentDate() {
        return this.shipmentDate;
    }
    
    public void setShipmentDate(Calendar shipmentDate) {
        this.shipmentDate = shipmentDate;
    }

    /**
     *      * Shipper's tracking number
     */
    public String getShipmentNumber() {
        return this.shipmentNumber;
    }
    
    public void setShipmentNumber(String shipmentNumber) {
        this.shipmentNumber = shipmentNumber;
    }

    /**
     *      * Description of shipment. E.g. 'Hand-carried', 'Overnight', 'Air', 'Land', 'Sea', ...
     */
    public String getShipmentMethod() {
        return this.shipmentMethod;
    }
    
    public void setShipmentMethod(String shipmentMethod) {
        this.shipmentMethod = shipmentMethod;
    }

    /**
     *      * Number of packages shipped
     */
    public Short getNumberOfPackages() {
        return this.numberOfPackages;
    }
    
    public void setNumberOfPackages(Short numberOfPackages) {
        this.numberOfPackages = numberOfPackages;
    }

    /**
     *      * The weight of the shipment
     */
    public String getWeight() {
        return this.weight;
    }
    
    public void setWeight(String weight) {
        this.weight = weight;
    }

    /**
     * 
     */
    public String getInsuredForAmount() {
        return this.insuredForAmount;
    }
    
    public void setInsuredForAmount(String insuredForAmount) {
        this.insuredForAmount = insuredForAmount;
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
     *      * AgentID of agent transporting the material
     */
    public Agent getAgentByShipper() {
        return this.agentByShipper;
    }
    
    public void setAgentByShipper(Agent agentByShipper) {
        this.agentByShipper = agentByShipper;
    }

    /**
     *      * AgentID of agent material is shipped to
     */
    public Agent getAgentByShippedTo() {
        return this.agentByShippedTo;
    }
    
    public void setAgentByShippedTo(Agent agentByShippedTo) {
        this.agentByShippedTo = agentByShippedTo;
    }

    /**
     *      * AgentID of person approving/initiating the shipment
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
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
    public Set<Loan> getLoans() {
        return this.loans;
    }
    
    public void setLoans(Set<Loan> loans) {
        this.loans = loans;
    }

    /**
     * 
     */
    public Set<ExchangeOut> getExchangeOuts() {
        return this.exchangeOuts;
    }
    
    public void setExchangeOuts(Set<ExchangeOut> exchangeOuts) {
        this.exchangeOuts = exchangeOuts;
    }





    // Add Methods

    public void addBorrowShipments(final BorrowShipments borrowShipment)
    {
        this.borrowShipments.add(borrowShipment);
        borrowShipment.setShipment(this);
    }

    public void addLoans(final Loan loan)
    {
        this.loans.add(loan);
        loan.setShipment(this);
    }

    public void addExchangeOuts(final ExchangeOut exchangeOut)
    {
        this.exchangeOuts.add(exchangeOut);
        exchangeOut.setShipment(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeBorrowShipments(final BorrowShipments borrowShipment)
    {
        this.borrowShipments.remove(borrowShipment);
        borrowShipment.setShipment(null);
    }

    public void removeLoans(final Loan loan)
    {
        this.loans.remove(loan);
        loan.setShipment(null);
    }

    public void removeExchangeOuts(final ExchangeOut exchangeOut)
    {
        this.exchangeOuts.remove(exchangeOut);
        exchangeOut.setShipment(null);
    }

    // Delete Add Methods
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    public Integer getTableId()
    {
        return 71;
    }

}
