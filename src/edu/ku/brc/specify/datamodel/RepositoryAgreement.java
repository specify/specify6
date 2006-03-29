package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;




/**
 *        @hibernate.class
 *         table="repositoryAgreement"
 *     
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
     *      *            @hibernate.id
     *             generator-class="native"
     *             type="java.lang.Integer"
     *             column="RepositoryAgreementID"
     *         
     */
    public Integer getRepositoryAgreementId() {
        return this.repositoryAgreementId;
    }
    
    public void setRepositoryAgreementId(Integer repositoryAgreementId) {
        this.repositoryAgreementId = repositoryAgreementId;
    }

    /**
     *      *            @hibernate.property
     *             column="Number"
     *             length="60"
     *             not-null="true"
     *         
     */
    public String getNumber() {
        return this.number;
    }
    
    public void setNumber(String number) {
        this.number = number;
    }

    /**
     *      *            @hibernate.property
     *             column="Status"
     *             length="32"
     *         
     */
    public String getStatus() {
        return this.status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     *      *             @hibernate.property
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
     *      *             @hibernate.property
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
     *             column="DateReceived"
     *         
     */
    public Calendar getDateReceived() {
        return this.dateReceived;
    }
    
    public void setDateReceived(Calendar dateReceived) {
        this.dateReceived = dateReceived;
    }

    /**
     *      *            @hibernate.property
     *             column="Text1"
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
     *             column="Text3"
     *         
     */
    public String getText3() {
        return this.text3;
    }
    
    public void setText3(String text3) {
        this.text3 = text3;
    }

    /**
     *      *            @hibernate.property
     *             column="Number1"
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
     *             column="TimestampCreated"
     *             length="23"
     *             not-null="true"
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
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="RepositoryAgreementID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.CollectionObject"
     *         
     */
    public Set getCollectionObjects() {
        return this.collectionObjects;
    }
    
    public void setCollectionObjects(Set collectionObjects) {
        this.collectionObjects = collectionObjects;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="all"
     *            @hibernate.collection-key
     *             column="RepositoryAgreementID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.AccessionAuthorizations"
     *         
     */
    public Set getRepositoryAgreementAuthorizations() {
        return this.repositoryAgreementAuthorizations;
    }
    
    public void setRepositoryAgreementAuthorizations(Set repositoryAgreementAuthorizations) {
        this.repositoryAgreementAuthorizations = repositoryAgreementAuthorizations;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="all"
     *            @hibernate.collection-key
     *             column="RepositoryAgreementID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.AccessionAgent"
     *         
     */
    public Set getRepositoryAgreementAgents() {
        return this.repositoryAgreementAgents;
    }
    
    public void setRepositoryAgreementAgents(Set repositoryAgreementAgents) {
        this.repositoryAgreementAgents = repositoryAgreementAgents;
    }

    /**
     *      *             @hibernate.many-to-one
     *             not-null="true"
     *             cascade="none"
     *             @hibernate.column name="AgentID"
     *         
     */
    public Agent getOriginator() {
        return this.originator;
    }
    
    public void setOriginator(Agent originator) {
        this.originator = originator;
    }




}