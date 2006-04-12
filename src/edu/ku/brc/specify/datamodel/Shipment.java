package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;




/**

 */
public class Shipment  implements java.io.Serializable {

    // Fields    

     protected Integer shipmentId;
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
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     private AgentAddress agentAddressByShipper;
     private AgentAddress agentAddressByShippedTo;
     private Agent agent;
     protected Set<BorrowShipment> borrowShipments;
     protected Set<Loan> loans;
     protected Set<ExchangeOut> exchangeOuts;


    // Constructors

    /** default constructor */
    public Shipment() {
    }
    
    /** constructor with id */
    public Shipment(Integer shipmentId) {
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
        timestampCreated = Calendar.getInstance().getTime();
        timestampModified = null;
        lastEditedBy = null;
        yesNo1 = null;
        yesNo2 = null;
        agentAddressByShipper = null;
        agentAddressByShippedTo = null;
        agent = null;
        borrowShipments = new HashSet<BorrowShipment>();
        loans = new HashSet<Loan>();
        exchangeOuts = new HashSet<ExchangeOut>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    public Integer getShipmentId() {
        return this.shipmentId;
    }
    
    public void setShipmentId(Integer shipmentId) {
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
    public Date getTimestampModified() {
        return this.timestampModified;
    }
    
    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
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
     *      * AgentAddressID of agent transporting the material
     */
    public AgentAddress getAgentAddressByShipper() {
        return this.agentAddressByShipper;
    }
    
    public void setAgentAddressByShipper(AgentAddress agentAddressByShipper) {
        this.agentAddressByShipper = agentAddressByShipper;
    }

    /**
     *      * AgentAddressID of agent material is shipped to
     */
    public AgentAddress getAgentAddressByShippedTo() {
        return this.agentAddressByShippedTo;
    }
    
    public void setAgentAddressByShippedTo(AgentAddress agentAddressByShippedTo) {
        this.agentAddressByShippedTo = agentAddressByShippedTo;
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
    public Set<BorrowShipment> getBorrowShipments() {
        return this.borrowShipments;
    }
    
    public void setBorrowShipments(Set<BorrowShipment> borrowShipments) {
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

    public void addBorrowShipment(final BorrowShipment borrowShipment)
    {
        this.borrowShipments.add(borrowShipment);
    }

    public void addLoan(final Loan loan)
    {
        this.loans.add(loan);
    }

    public void addExchangeOut(final ExchangeOut exchangeOut)
    {
        this.exchangeOuts.add(exchangeOut);
    }

    // Done Add Methods
}
