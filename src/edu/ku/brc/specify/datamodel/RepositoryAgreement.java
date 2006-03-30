package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;




/**

 */
public class RepositoryAgreement  implements java.io.Serializable {

    // Fields    

     protected Integer repositoryAgreementId;
     protected String number;
     protected String status;
     protected Calendar startDate;
     protected Calendar endDate;
     protected Calendar dateReceived;
     protected String text1;
     protected String text2;
     protected String text3;
     protected Float number1;
     protected Float number2;
     protected String remarks;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     protected Set collectionObjects;
     protected Set repositoryAgreementAuthorizations;
     protected Set repositoryAgreementAgents;
     protected Agent originator;


    // Constructors

    /** default constructor */
    public RepositoryAgreement() {
    }
    
    /** constructor with id */
    public RepositoryAgreement(Integer repositoryAgreementId) {
        this.repositoryAgreementId = repositoryAgreementId;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Integer getRepositoryAgreementId() {
        return this.repositoryAgreementId;
    }
    
    public void setRepositoryAgreementId(Integer repositoryAgreementId) {
        this.repositoryAgreementId = repositoryAgreementId;
    }

    /**
     * 
     */
    public String getNumber() {
        return this.number;
    }
    
    public void setNumber(String number) {
        this.number = number;
    }

    /**
     * 
     */
    public String getStatus() {
        return this.status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 
     */
    public Calendar getStartDate() {
        return this.startDate;
    }
    
    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    /**
     * 
     */
    public Calendar getEndDate() {
        return this.endDate;
    }
    
    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    /**
     * 
     */
    public Calendar getDateReceived() {
        return this.dateReceived;
    }
    
    public void setDateReceived(Calendar dateReceived) {
        this.dateReceived = dateReceived;
    }

    /**
     * 
     */
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     * 
     */
    public String getText2() {
        return this.text2;
    }
    
    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     * 
     */
    public String getText3() {
        return this.text3;
    }
    
    public void setText3(String text3) {
        this.text3 = text3;
    }

    /**
     * 
     */
    public Float getNumber1() {
        return this.number1;
    }
    
    public void setNumber1(Float number1) {
        this.number1 = number1;
    }

    /**
     * 
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
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
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
     * 
     */
    public Boolean getYesNo1() {
        return this.yesNo1;
    }
    
    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     * 
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
    public Set getCollectionObjects() {
        return this.collectionObjects;
    }
    
    public void setCollectionObjects(Set collectionObjects) {
        this.collectionObjects = collectionObjects;
    }

    /**
     * 
     */
    public Set getRepositoryAgreementAuthorizations() {
        return this.repositoryAgreementAuthorizations;
    }
    
    public void setRepositoryAgreementAuthorizations(Set repositoryAgreementAuthorizations) {
        this.repositoryAgreementAuthorizations = repositoryAgreementAuthorizations;
    }

    /**
     * 
     */
    public Set getRepositoryAgreementAgents() {
        return this.repositoryAgreementAgents;
    }
    
    public void setRepositoryAgreementAgents(Set repositoryAgreementAgents) {
        this.repositoryAgreementAgents = repositoryAgreementAgents;
    }

    /**
     * 
     */
    public Agent getOriginator() {
        return this.originator;
    }
    
    public void setOriginator(Agent originator) {
        this.originator = originator;
    }




}