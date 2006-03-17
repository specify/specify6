package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="agentaddress"
 *     
 */
public class AgentAddress  implements java.io.Serializable {

    // Fields    

     protected Integer agentAddressId;
     protected Short typeOfAgentAddressed;
     protected String jobTitle;
     protected String phone1;
     protected String phone2;
     protected String fax;
     protected String roomOrBuilding;
     protected String email;
     protected String url;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     protected Short isCurrent;
     private Set loanAgents;
     private Set shipmentsByShipperId;
     private Set shipmentsByShippedToId;
     private Set deaccessionAgents;
     private Set exchangeIns;
     private Set permitsByIssueeId;
     private Set permitsByIssuerId;
     private Set borrowAgents;
     private Set accessionAgents;
     private Set exchangeOuts;
     private Agent agentByOrganizationId;
     private Agent agentByAgentId;
     private Address address;


    // Constructors

    /** default constructor */
    public AgentAddress() {
    }
    
    /** constructor with id */
    public AgentAddress(Integer agentAddressId) {
        this.agentAddressId = agentAddressId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="AgentAddressID"
     *         
     */
    public Integer getAgentAddressId() {
        return this.agentAddressId;
    }
    
    public void setAgentAddressId(Integer agentAddressId) {
        this.agentAddressId = agentAddressId;
    }

    /**
     *      *            @hibernate.property
     *             column="TypeOfAgentAddressed"
     *         
     */
    public Short getTypeOfAgentAddressed() {
        return this.typeOfAgentAddressed;
    }
    
    public void setTypeOfAgentAddressed(Short typeOfAgentAddressed) {
        this.typeOfAgentAddressed = typeOfAgentAddressed;
    }

    /**
     *      *            @hibernate.property
     *             column="JobTitle"
     *             length="50"
     *         
     */
    public String getJobTitle() {
        return this.jobTitle;
    }
    
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    /**
     *      *            @hibernate.property
     *             column="Phone1"
     *             length="50"
     *         
     */
    public String getPhone1() {
        return this.phone1;
    }
    
    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    /**
     *      *            @hibernate.property
     *             column="Phone2"
     *             length="50"
     *         
     */
    public String getPhone2() {
        return this.phone2;
    }
    
    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    /**
     *      *            @hibernate.property
     *             column="Fax"
     *             length="50"
     *         
     */
    public String getFax() {
        return this.fax;
    }
    
    public void setFax(String fax) {
        this.fax = fax;
    }

    /**
     *      *            @hibernate.property
     *             column="RoomOrBuilding"
     *             length="50"
     *         
     */
    public String getRoomOrBuilding() {
        return this.roomOrBuilding;
    }
    
    public void setRoomOrBuilding(String roomOrBuilding) {
        this.roomOrBuilding = roomOrBuilding;
    }

    /**
     *      *            @hibernate.property
     *             column="Email"
     *             length="50"
     *         
     */
    public String getEmail() {
        return this.email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     *      *            @hibernate.property
     *             column="URL"
     *             length="300"
     *         
     */
    public String getUrl() {
        return this.url;
    }
    
    public void setUrl(String url) {
        this.url = url;
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
     *             column="TimestampModified"
     *             length="23"
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
     *             column="TimestampCreated"
     *             length="23"
     *             update="false"
     *             not-null="true"
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
     *             column="IsCurrent"
     *         
     */
    public Short getIsCurrent() {
        return this.isCurrent;
    }
    
    public void setIsCurrent(Short isCurrent) {
        this.isCurrent = isCurrent;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="AgentAddressID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.LoanAgent"
     *         
     */
    public Set getLoanAgents() {
        return this.loanAgents;
    }
    
    public void setLoanAgents(Set loanAgents) {
        this.loanAgents = loanAgents;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="ShipperID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Shipment"
     *         
     */
    public Set getShipmentsByShipperId() {
        return this.shipmentsByShipperId;
    }
    
    public void setShipmentsByShipperId(Set shipmentsByShipperId) {
        this.shipmentsByShipperId = shipmentsByShipperId;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="ShippedToID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Shipment"
     *         
     */
    public Set getShipmentsByShippedToId() {
        return this.shipmentsByShippedToId;
    }
    
    public void setShipmentsByShippedToId(Set shipmentsByShippedToId) {
        this.shipmentsByShippedToId = shipmentsByShippedToId;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="AgentAddressID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.DeaccessionAgent"
     *         
     */
    public Set getDeaccessionAgents() {
        return this.deaccessionAgents;
    }
    
    public void setDeaccessionAgents(Set deaccessionAgents) {
        this.deaccessionAgents = deaccessionAgents;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="ReceivedFromOrganizationID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.ExchangeIn"
     *         
     */
    public Set getExchangeIns() {
        return this.exchangeIns;
    }
    
    public void setExchangeIns(Set exchangeIns) {
        this.exchangeIns = exchangeIns;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="IssueeID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Permit"
     *         
     */
    public Set getPermitsByIssueeId() {
        return this.permitsByIssueeId;
    }
    
    public void setPermitsByIssueeId(Set permitsByIssueeId) {
        this.permitsByIssueeId = permitsByIssueeId;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="IssuerID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Permit"
     *         
     */
    public Set getPermitsByIssuerId() {
        return this.permitsByIssuerId;
    }
    
    public void setPermitsByIssuerId(Set permitsByIssuerId) {
        this.permitsByIssuerId = permitsByIssuerId;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="AgentAddressID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.BorrowAgent"
     *         
     */
    public Set getBorrowAgents() {
        return this.borrowAgents;
    }
    
    public void setBorrowAgents(Set borrowAgents) {
        this.borrowAgents = borrowAgents;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="AgentAddressID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.AccessionAgent"
     *         
     */
    public Set getAccessionAgents() {
        return this.accessionAgents;
    }
    
    public void setAccessionAgents(Set accessionAgents) {
        this.accessionAgents = accessionAgents;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="SentToOrganizationID"
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

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="OrganizationID"         
     *         
     */
    public Agent getAgentByOrganizationId() {
        return this.agentByOrganizationId;
    }
    
    public void setAgentByOrganizationId(Agent agentByOrganizationId) {
        this.agentByOrganizationId = agentByOrganizationId;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="AgentID"         
     *         
     */
    public Agent getAgentByAgentId() {
        return this.agentByAgentId;
    }
    
    public void setAgentByAgentId(Agent agentByAgentId) {
        this.agentByAgentId = agentByAgentId;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="AddressID"         
     *         
     */
    public Address getAddress() {
        return this.address;
    }
    
    public void setAddress(Address address) {
        this.address = address;
    }




}