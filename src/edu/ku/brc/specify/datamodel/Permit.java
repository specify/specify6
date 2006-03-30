package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;




/**

 */
public class Permit  implements java.io.Serializable {

    // Fields    

     protected Integer permitId;
     protected String permitNumber;
     protected String type;
     protected Calendar issuedDate;
     protected Calendar startDate;
     protected Calendar endDate;
     protected Calendar renewalDate;
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
     private Set accessionAuthorizations;
     private AgentAddress agentAddressByIssueeId;
     private AgentAddress agentAddressByIssuerId;
     private Set externalResources;


    // Constructors

    /** default constructor */
    public Permit() {
    }
    
    /** constructor with id */
    public Permit(Integer permitId) {
        this.permitId = permitId;
    }
   
    
    

    // Property accessors

    /**
     *      * Primary key
     */
    public Integer getPermitId() {
        return this.permitId;
    }
    
    public void setPermitId(Integer permitId) {
        this.permitId = permitId;
    }

    /**
     *      * Identifier for the permit
     */
    public String getPermitNumber() {
        return this.permitNumber;
    }
    
    public void setPermitNumber(String permitNumber) {
        this.permitNumber = permitNumber;
    }

    /**
     *      * Permit category - 'CITES', 'Migratory Bird Treaty Act', ...
     */
    public String getType() {
        return this.type;
    }
    
    public void setType(String type) {
        this.type = type;
    }

    /**
     *      * Date permit was issued
     */
    public Calendar getIssuedDate() {
        return this.issuedDate;
    }
    
    public void setIssuedDate(Calendar issuedDate) {
        this.issuedDate = issuedDate;
    }

    /**
     *      * Date permit becomes effective
     */
    public Calendar getStartDate() {
        return this.startDate;
    }
    
    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    /**
     *      * Date permit expires
     */
    public Calendar getEndDate() {
        return this.endDate;
    }
    
    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    /**
     *      * Date of renewal
     */
    public Calendar getRenewalDate() {
        return this.renewalDate;
    }
    
    public void setRenewalDate(Calendar renewalDate) {
        this.renewalDate = renewalDate;
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
     * 
     */
    public Set getAccessionAuthorizations() {
        return this.accessionAuthorizations;
    }
    
    public void setAccessionAuthorizations(Set accessionAuthorizations) {
        this.accessionAuthorizations = accessionAuthorizations;
    }

    /**
     *      * AgentAddressID of Issuee
     */
    public AgentAddress getAgentAddressByIssueeId() {
        return this.agentAddressByIssueeId;
    }
    
    public void setAgentAddressByIssueeId(AgentAddress agentAddressByIssueeId) {
        this.agentAddressByIssueeId = agentAddressByIssueeId;
    }

    /**
     *      * AgentAddressID of Issuer
     */
    public AgentAddress getAgentAddressByIssuerId() {
        return this.agentAddressByIssuerId;
    }
    
    public void setAgentAddressByIssuerId(AgentAddress agentAddressByIssuerId) {
        this.agentAddressByIssuerId = agentAddressByIssuerId;
    }

    /**
     * 
     */
    public Set getExternalResources() {
        return this.externalResources;
    }
    
    public void setExternalResources(Set externalResources) {
        this.externalResources = externalResources;
    }




}