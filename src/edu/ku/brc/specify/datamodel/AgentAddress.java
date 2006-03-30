package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.Set;




/**

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
     protected Boolean isCurrent;
     private Set loanAgents;
     private Set shipmentsByShipper;
     private Set shipmentsByShippedTo;
     private Set deaccessionAgents;
     private Set exchangeIns;
     private Set permitsByIssuee;
     private Set permitsByIssuer;
     private Set borrowAgents;
     private Set accessionAgents;
     private Set exchangeOuts;
     private Agent organization;
     private Agent agent;
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
     *      * PrimaryKey
     */
    public Integer getAgentAddressId() {
        return this.agentAddressId;
    }
    
    public void setAgentAddressId(Integer agentAddressId) {
        this.agentAddressId = agentAddressId;
    }

    /**
     *      * 0 for organization, 1 for person
     */
    public Short getTypeOfAgentAddressed() {
        return this.typeOfAgentAddressed;
    }
    
    public void setTypeOfAgentAddressed(Short typeOfAgentAddressed) {
        this.typeOfAgentAddressed = typeOfAgentAddressed;
    }

    /**
     *      * Agent's (person) job title at specified address and organization
     */
    public String getJobTitle() {
        return this.jobTitle;
    }
    
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    /**
     * 
     */
    public String getPhone1() {
        return this.phone1;
    }
    
    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    /**
     * 
     */
    public String getPhone2() {
        return this.phone2;
    }
    
    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    /**
     * 
     */
    public String getFax() {
        return this.fax;
    }
    
    public void setFax(String fax) {
        this.fax = fax;
    }

    /**
     * 
     */
    public String getRoomOrBuilding() {
        return this.roomOrBuilding;
    }
    
    public void setRoomOrBuilding(String roomOrBuilding) {
        this.roomOrBuilding = roomOrBuilding;
    }

    /**
     * 
     */
    public String getEmail() {
        return this.email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 
     */
    public String getUrl() {
        return this.url;
    }
    
    public void setUrl(String url) {
        this.url = url;
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
    public Date getTimestampCreated() {
        return this.timestampCreated;
    }
    
    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
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
     *      * Is the agent currently located at this address?
     */
    public Boolean getIsCurrent() {
        return this.isCurrent;
    }
    
    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    /**
     * 
     */
    public Set getLoanAgents() {
        return this.loanAgents;
    }
    
    public void setLoanAgents(Set loanAgents) {
        this.loanAgents = loanAgents;
    }

    /**
     * 
     */
    public Set getShipmentsByShipper() {
        return this.shipmentsByShipper;
    }
    
    public void setShipmentsByShipper(Set shipmentsByShipper) {
        this.shipmentsByShipper = shipmentsByShipper;
    }

    /**
     * 
     */
    public Set getShipmentsByShippedTo() {
        return this.shipmentsByShippedTo;
    }
    
    public void setShipmentsByShippedTo(Set shipmentsByShippedTo) {
        this.shipmentsByShippedTo = shipmentsByShippedTo;
    }

    /**
     * 
     */
    public Set getDeaccessionAgents() {
        return this.deaccessionAgents;
    }
    
    public void setDeaccessionAgents(Set deaccessionAgents) {
        this.deaccessionAgents = deaccessionAgents;
    }

    /**
     * 
     */
    public Set getExchangeIns() {
        return this.exchangeIns;
    }
    
    public void setExchangeIns(Set exchangeIns) {
        this.exchangeIns = exchangeIns;
    }

    /**
     * 
     */
    public Set getPermitsByIssuee() {
        return this.permitsByIssuee;
    }
    
    public void setPermitsByIssuee(Set permitsByIssuee) {
        this.permitsByIssuee = permitsByIssuee;
    }

    /**
     * 
     */
    public Set getPermitsByIssuer() {
        return this.permitsByIssuer;
    }
    
    public void setPermitsByIssuer(Set permitsByIssuer) {
        this.permitsByIssuer = permitsByIssuer;
    }

    /**
     * 
     */
    public Set getBorrowAgents() {
        return this.borrowAgents;
    }
    
    public void setBorrowAgents(Set borrowAgents) {
        this.borrowAgents = borrowAgents;
    }

    /**
     * 
     */
    public Set getAccessionAgents() {
        return this.accessionAgents;
    }
    
    public void setAccessionAgents(Set accessionAgents) {
        this.accessionAgents = accessionAgents;
    }

    /**
     * 
     */
    public Set getExchangeOuts() {
        return this.exchangeOuts;
    }
    
    public void setExchangeOuts(Set exchangeOuts) {
        this.exchangeOuts = exchangeOuts;
    }

    /**
     *      * Associates Agent identified by AgentID (type Person) as a member of Agent identified by OrganizationID (type Organization)
     */
    public Agent getOrganization() {
        return this.organization;
    }
    
    public void setOrganization(Agent organization) {
        this.organization = organization;
    }

    /**
     *      * Associated record in Agent table
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    /**
     *      * Associated record in Address table
     */
    public Address getAddress() {
        return this.address;
    }
    
    public void setAddress(Address address) {
        this.address = address;
    }




}