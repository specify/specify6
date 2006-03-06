package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="permit"
 *     
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
     protected Integer typeId;
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
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="PermitID"
     *         
     */
    public Integer getPermitId() {
        return this.permitId;
    }
    
    public void setPermitId(Integer permitId) {
        this.permitId = permitId;
    }

    /**
     *      *            @hibernate.property
     *             column="PermitNumber"
     *             length="50"
     *             not-null="true"
     *         
     */
    public String getPermitNumber() {
        return this.permitNumber;
    }
    
    public void setPermitNumber(String permitNumber) {
        this.permitNumber = permitNumber;
    }

    /**
     *      *            @hibernate.property
     *             column="Type"
     *             length="50"
     *         
     */
    public String getType() {
        return this.type;
    }
    
    public void setType(String type) {
        this.type = type;
    }

    /**
     *      *            @hibernate.property
     *             column="IssuedDate"
     *         
     */
    public Calendar getIssuedDate() {
        return this.issuedDate;
    }
    
    public void setIssuedDate(Calendar issuedDate) {
        this.issuedDate = issuedDate;
    }

    /**
     *      *            @hibernate.property
     *             column="StartDate"
     *         
     */
    public Calendar getStartDate() {
        return this.startDate;
    }
    
    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    /**
     *      *            @hibernate.property
     *             column="EndDate"
     *         
     */
    public Calendar getEndDate() {
        return this.endDate;
    }
    
    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    /**
     *      *            @hibernate.property
     *             column="RenewalDate"
     *         
     */
    public Calendar getRenewalDate() {
        return this.renewalDate;
    }
    
    public void setRenewalDate(Calendar renewalDate) {
        this.renewalDate = renewalDate;
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
     *             column="TypeID"
     *             length="10"
     *         
     */
    public Integer getTypeId() {
        return this.typeId;
    }
    
    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
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
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="PermitID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.AccessionAuthorizations"
     *         
     */
    public Set getAccessionAuthorizations() {
        return this.accessionAuthorizations;
    }
    
    public void setAccessionAuthorizations(Set accessionAuthorizations) {
        this.accessionAuthorizations = accessionAuthorizations;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="IssueeID"         
     *         
     */
    public AgentAddress getAgentAddressByIssueeId() {
        return this.agentAddressByIssueeId;
    }
    
    public void setAgentAddressByIssueeId(AgentAddress agentAddressByIssueeId) {
        this.agentAddressByIssueeId = agentAddressByIssueeId;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="IssuerID"         
     *         
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