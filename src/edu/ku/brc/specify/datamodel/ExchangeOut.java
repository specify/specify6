package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;




/**

 */
public class ExchangeOut  implements java.io.Serializable {

    // Fields    

     protected Integer exchangeOutId;
     protected Calendar exchangeDate;
     protected Short quantityExchanged;
     protected String descriptionOfMaterial;
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
     protected AgentAddress agentAddress;
     protected Agent agent;
     protected Shipment shipment;


    // Constructors

    /** default constructor */
    public ExchangeOut() {
    }
    
    /** constructor with id */
    public ExchangeOut(Integer exchangeOutId) {
        this.exchangeOutId = exchangeOutId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        exchangeOutId = null;
        exchangeDate = null;
        quantityExchanged = null;
        descriptionOfMaterial = null;
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
        agentAddress = null;
        agent = null;
        shipment = null;
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    public Integer getExchangeOutId() {
        return this.exchangeOutId;
    }
    
    public void setExchangeOutId(Integer exchangeOutId) {
        this.exchangeOutId = exchangeOutId;
    }

    /**
     *      * Date exchange was sent
     */
    public Calendar getExchangeDate() {
        return this.exchangeDate;
    }
    
    public void setExchangeDate(Calendar exchangeDate) {
        this.exchangeDate = exchangeDate;
    }

    /**
     *      * Number of items sent
     */
    public Short getQuantityExchanged() {
        return this.quantityExchanged;
    }
    
    public void setQuantityExchanged(Short quantityExchanged) {
        this.quantityExchanged = quantityExchanged;
    }

    /**
     * 
     */
    public String getDescriptionOfMaterial() {
        return this.descriptionOfMaterial;
    }
    
    public void setDescriptionOfMaterial(String descriptionOfMaterial) {
        this.descriptionOfMaterial = descriptionOfMaterial;
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
     *      * AgentAddress ID of organization material was sent to
     */
    public AgentAddress getAgentAddress() {
        return this.agentAddress;
    }
    
    public void setAgentAddress(AgentAddress agentAddress) {
        this.agentAddress = agentAddress;
    }

    /**
     *      * Agent ID of person who recorded  the exchange
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    /**
     *      * Shipment information for the exchange
     */
    public Shipment getShipment() {
        return this.shipment;
    }
    
    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }




    // Add Methods

    // Done Add Methods
}
