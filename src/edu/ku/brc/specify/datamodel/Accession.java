package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.Set;




/**
 *        @hibernate.class
 *         table="accession"
 *     
 */
public class Accession  implements java.io.Serializable {

    // Fields    

     protected Integer accessionId;
     protected String number;
     protected String status;
     protected String type;
     protected String verbatimDate;
     protected Integer dateAccessioned;
     protected Integer dateReceived;
     protected String text1;
     protected String text2;
     protected String text3;
     protected Float number1;
     protected Float number2;
     protected String remarks;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected Integer statusId;
     protected Integer typeId;
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     protected Set collectionObjects;
     protected Set accessionAuthorizations;
     protected Set accessionAgents;


    // Constructors

    /** default constructor */
    public Accession() {
    }
    
    /** constructor with id */
    public Accession(Integer accessionId) {
        this.accessionId = accessionId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="AccessionID"
     *         
     */
    public Integer getAccessionId() {
        return this.accessionId;
    }
    
    public void setAccessionId(Integer accessionId) {
        this.accessionId = accessionId;
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
     *             length="30"
     *         
     */
    public String getStatus() {
        return this.status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     *      *            @hibernate.property
     *             column="Type"
     *             length="30"
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
     *             column="VerbatimDate"
     *             length="50"
     *         
     */
    public String getVerbatimDate() {
        return this.verbatimDate;
    }
    
    public void setVerbatimDate(String verbatimDate) {
        this.verbatimDate = verbatimDate;
    }

    /**
     *      *            @hibernate.property
     *             column="DateAccessioned"
     *             length="10"
     *         
     */
    public Integer getDateAccessioned() {
        return this.dateAccessioned;
    }
    
    public void setDateAccessioned(Integer dateAccessioned) {
        this.dateAccessioned = dateAccessioned;
    }

    /**
     *      *            @hibernate.property
     *             column="DateReceived"
     *             length="10"
     *         
     */
    public Integer getDateReceived() {
        return this.dateReceived;
    }
    
    public void setDateReceived(Integer dateReceived) {
        this.dateReceived = dateReceived;
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
     *             column="Text3"
     *             length="300"
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
     *             column="StatusID"
     *             length="10"
     *         
     */
    public Integer getStatusId() {
        return this.statusId;
    }
    
    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
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
     *             column="AccessionID"
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
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="AccessionID"
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
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="AccessionID"
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




}