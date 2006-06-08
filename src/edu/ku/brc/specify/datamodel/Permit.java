package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
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
     protected Set<AccessionAuthorizations> accessionAuthorizations;
     protected Agent agentByIssuee;
     protected Agent agentByIssuer;
     protected Set<ExternalResource> externalResources;


    // Constructors

    /** default constructor */
    public Permit() {
    }

    /** constructor with id */
    public Permit(Integer permitId) {
        this.permitId = permitId;
    }




    // Initializer
    public void initialize()
    {
        permitId = null;
        permitNumber = null;
        type = null;
        issuedDate = null;
        startDate = null;
        endDate = null;
        renewalDate = null;
        remarks = null;
        text1 = null;
        text2 = null;
        number1 = null;
        number2 = null;
        timestampCreated = new Date();
        timestampModified = new Date();
        lastEditedBy = null;
        yesNo1 = null;
        yesNo2 = null;
        accessionAuthorizations = new HashSet<AccessionAuthorizations>();
        agentByIssuee = null;
        agentByIssuer = null;
        externalResources = new HashSet<ExternalResource>();
    }
    // End Initializer

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
    public Set<AccessionAuthorizations> getAccessionAuthorizations() {
        return this.accessionAuthorizations;
    }

    public void setAccessionAuthorizations(Set<AccessionAuthorizations> accessionAuthorizations) {
        this.accessionAuthorizations = accessionAuthorizations;
    }

    /**
     *      * AgentID of Issuee
     */
    public Agent getAgentByIssuee() {
        return this.agentByIssuee;
    }

    public void setAgentByIssuee(Agent agentByIssuee) {
        this.agentByIssuee = agentByIssuee;
    }

    /**
     *      * AgentID of Issuer
     */
    public Agent getAgentByIssuer() {
        return this.agentByIssuer;
    }

    public void setAgentByIssuer(Agent agentByIssuer) {
        this.agentByIssuer = agentByIssuer;
    }

    /**
     *
     */
    public Set<ExternalResource> getExternalResources() {
        return this.externalResources;
    }

    public void setExternalResources(Set<ExternalResource> externalResources) {
        this.externalResources = externalResources;
    }





    // Add Methods

    public void addExternalResources(final ExternalResource externalResource)
    {
        this.externalResources.add(externalResource);
        externalResource.getPermits().add(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeExternalResources(final ExternalResource externalResource)
    {
        this.externalResources.remove(externalResource);
        externalResource.getPermits().remove(this);
    }

    // Delete Add Methods
}
