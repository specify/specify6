package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;




/**
 *        @hibernate.class
 *         table="exchangeout"
 *     
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
     private AgentAddress agentAddress;
     private Agent agent;
     private Shipment shipment;


    // Constructors

    /** default constructor */
    public ExchangeOut() {
    }
    
    /** constructor with id */
    public ExchangeOut(Integer exchangeOutId) {
        this.exchangeOutId = exchangeOutId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="ExchangeOutID"
     *         
     */
    public Integer getExchangeOutId() {
        return this.exchangeOutId;
    }
    
    public void setExchangeOutId(Integer exchangeOutId) {
        this.exchangeOutId = exchangeOutId;
    }

    /**
     *      *            @hibernate.property
     *             column="ExchangeDate"
     *         
     */
    public Calendar getExchangeDate() {
        return this.exchangeDate;
    }
    
    public void setExchangeDate(Calendar exchangeDate) {
        this.exchangeDate = exchangeDate;
    }

    /**
     *      *            @hibernate.property
     *             column="QuantityExchanged"
     *             not-null="true"
     *         
     */
    public Short getQuantityExchanged() {
        return this.quantityExchanged;
    }
    
    public void setQuantityExchanged(Short quantityExchanged) {
        this.quantityExchanged = quantityExchanged;
    }

    /**
     *      *            @hibernate.property
     *             column="DescriptionOfMaterial"
     *             length="120"
     *         
     */
    public String getDescriptionOfMaterial() {
        return this.descriptionOfMaterial;
    }
    
    public void setDescriptionOfMaterial(String descriptionOfMaterial) {
        this.descriptionOfMaterial = descriptionOfMaterial;
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
     *             update="false"
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
     *             not-null="true"
     *             not-null="true"
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
     *            @hibernate.column name="SentToOrganizationID"         
     *         
     */
    public AgentAddress getAgentAddress() {
        return this.agentAddress;
    }
    
    public void setAgentAddress(AgentAddress agentAddress) {
        this.agentAddress = agentAddress;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="CatalogedByID"         
     *         
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="ShipmentID"         
     *         
     */
    public Shipment getShipment() {
        return this.shipment;
    }
    
    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }




}