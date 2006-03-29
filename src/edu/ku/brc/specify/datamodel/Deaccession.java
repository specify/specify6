package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;




/**
 *        @hibernate.class
 *         table="deaccession"
 *     
 */
public class Deaccession  implements java.io.Serializable {

    // Fields    

     protected Integer deaccessionId;
     protected String type;
     protected String deaccessionNumber;
     protected Calendar deaccessionDate;
     protected String remarks;
     protected String text1;
     protected String text2;
     protected Float number1;
     protected Float number2;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     private Set deaccessionAgents;
     private Set deaccessionCollectionObjects;


    // Constructors

    /** default constructor */
    public Deaccession() {
    }
    
    /** constructor with id */
    public Deaccession(Integer deaccessionId) {
        this.deaccessionId = deaccessionId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="native"
     *             type="java.lang.Integer"
     *             column="DeaccessionID"
     *         
     */
    public Integer getDeaccessionId() {
        return this.deaccessionId;
    }
    
    public void setDeaccessionId(Integer deaccessionId) {
        this.deaccessionId = deaccessionId;
    }

    /**
     *      *            @hibernate.property
     *             column="Type"
     *             length="32"
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
     *             column="DeaccessionNumber"
     *             length="50"
     *         
     */
    public String getDeaccessionNumber() {
        return this.deaccessionNumber;
    }
    
    public void setDeaccessionNumber(String deaccessionNumber) {
        this.deaccessionNumber = deaccessionNumber;
    }

    /**
     *      *            @hibernate.property
     *             column="DeaccessionDate"
     *             length="10"
     *         
     */
    public Calendar getDeaccessionDate() {
        return this.deaccessionDate;
    }
    
    public void setDeaccessionDate(Calendar deaccessionDate) {
        this.deaccessionDate = deaccessionDate;
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
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="DeaccessionID"
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
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="DeaccessionID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.DeaccessionCollectionObject"
     *         
     */
    public Set getDeaccessionCollectionObjects() {
        return this.deaccessionCollectionObjects;
    }
    
    public void setDeaccessionCollectionObjects(Set deaccessionCollectionObjects) {
        this.deaccessionCollectionObjects = deaccessionCollectionObjects;
    }




}