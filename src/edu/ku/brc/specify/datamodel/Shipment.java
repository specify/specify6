package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.Set;




/**
 *        @hibernate.class
 *         table="shipment"
 *     
 */
public class Shipment  implements java.io.Serializable {

    // Fields    

     protected Integer shipmentId;
     protected Integer shipmentDate;
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
     protected Integer shipmentMethodId;
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     private AgentAddress agentAddressByShipperId;
     private AgentAddress agentAddressByShippedToId;
     private Agent agent;
     private Set borrowShipments;
     private Set loans;
     private Set exchangeOuts;


    // Constructors

    /** default constructor */
    public Shipment() {
    }
    
    /** constructor with id */
    public Shipment(Integer shipmentId) {
        this.shipmentId = shipmentId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="ShipmentID"
     *         
     */
    public Integer getShipmentId() {
        return this.shipmentId;
    }
    
    public void setShipmentId(Integer shipmentId) {
        this.shipmentId = shipmentId;
    }

    /**
     *      *            @hibernate.property
     *             column="ShipmentDate"
     *             length="10"
     *         
     */
    public Integer getShipmentDate() {
        return this.shipmentDate;
    }
    
    public void setShipmentDate(Integer shipmentDate) {
        this.shipmentDate = shipmentDate;
    }

    /**
     *      *            @hibernate.property
     *             column="ShipmentNumber"
     *             length="50"
     *             not-null="true"
     *         
     */
    public String getShipmentNumber() {
        return this.shipmentNumber;
    }
    
    public void setShipmentNumber(String shipmentNumber) {
        this.shipmentNumber = shipmentNumber;
    }

    /**
     *      *            @hibernate.property
     *             column="ShipmentMethod"
     *             length="50"
     *         
     */
    public String getShipmentMethod() {
        return this.shipmentMethod;
    }
    
    public void setShipmentMethod(String shipmentMethod) {
        this.shipmentMethod = shipmentMethod;
    }

    /**
     *      *            @hibernate.property
     *             column="NumberOfPackages"
     *         
     */
    public Short getNumberOfPackages() {
        return this.numberOfPackages;
    }
    
    public void setNumberOfPackages(Short numberOfPackages) {
        this.numberOfPackages = numberOfPackages;
    }

    /**
     *      *            @hibernate.property
     *             column="Weight"
     *             length="50"
     *         
     */
    public String getWeight() {
        return this.weight;
    }
    
    public void setWeight(String weight) {
        this.weight = weight;
    }

    /**
     *      *            @hibernate.property
     *             column="InsuredForAmount"
     *             length="50"
     *         
     */
    public String getInsuredForAmount() {
        return this.insuredForAmount;
    }
    
    public void setInsuredForAmount(String insuredForAmount) {
        this.insuredForAmount = insuredForAmount;
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
     *             column="Text1"
     *             length="300"
     *         
     */
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      *            @hibernate.property
     *             column="Text2"
     *             length="300"
     *         
     */
    public String getText2() {
        return this.text2;
    }
    
    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      *            @hibernate.property
     *             column="Number1"
     *             length="24"
     *         
     */
    public Float getNumber1() {
        return this.number1;
    }
    
    public void setNumber1(Float number1) {
        this.number1 = number1;
    }

    /**
     *      *            @hibernate.property
     *             column="Number2"
     *             length="24"
     *         
     */
    public Float getNumber2() {
        return this.number2;
    }
    
    public void setNumber2(Float number2) {
        this.number2 = number2;
    }

    /**
     *      *            @hibernate.property
     *             column="TimestampCreated"
     *             length="23"
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
     *             column="TimestampModified"
     *             length="23"
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
     *      *            @hibernate.property
     *             column="ShipmentMethodID"
     *             length="10"
     *         
     */
    public Integer getShipmentMethodId() {
        return this.shipmentMethodId;
    }
    
    public void setShipmentMethodId(Integer shipmentMethodId) {
        this.shipmentMethodId = shipmentMethodId;
    }

    /**
     *      *            @hibernate.property
     *             column="YesNo1"
     *         
     */
    public Boolean getYesNo1() {
        return this.yesNo1;
    }
    
    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      *            @hibernate.property
     *             column="YesNo2"
     *         
     */
    public Boolean getYesNo2() {
        return this.yesNo2;
    }
    
    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="ShipperID"         
     *         
     */
    public AgentAddress getAgentAddressByShipperId() {
        return this.agentAddressByShipperId;
    }
    
    public void setAgentAddressByShipperId(AgentAddress agentAddressByShipperId) {
        this.agentAddressByShipperId = agentAddressByShipperId;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="ShippedToID"         
     *         
     */
    public AgentAddress getAgentAddressByShippedToId() {
        return this.agentAddressByShippedToId;
    }
    
    public void setAgentAddressByShippedToId(AgentAddress agentAddressByShippedToId) {
        this.agentAddressByShippedToId = agentAddressByShippedToId;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="ShippedByID"         
     *         
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="ShipmentID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.BorrowShipment"
     *         
     */
    public Set getBorrowShipments() {
        return this.borrowShipments;
    }
    
    public void setBorrowShipments(Set borrowShipments) {
        this.borrowShipments = borrowShipments;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="ShipmentID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Loan"
     *         
     */
    public Set getLoans() {
        return this.loans;
    }
    
    public void setLoans(Set loans) {
        this.loans = loans;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="ShipmentID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.ExchangeOut"
     *         
     */
    public Set getExchangeOuts() {
        return this.exchangeOuts;
    }
    
    public void setExchangeOuts(Set exchangeOuts) {
        this.exchangeOuts = exchangeOuts;
    }




}