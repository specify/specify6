package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.Set;




/**
 *        @hibernate.class
 *         table="journal"
 *     
 */
public class Journal  implements java.io.Serializable {

    // Fields    

     protected Integer journalId;
     protected String journalName;
     protected String journalAbbreviation;
     protected String remarks;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     private Set referenceWorks;


    // Constructors

    /** default constructor */
    public Journal() {
    }
    
    /** constructor with id */
    public Journal(Integer journalId) {
        this.journalId = journalId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="JournalID"
     *         
     */
    public Integer getJournalId() {
        return this.journalId;
    }
    
    public void setJournalId(Integer journalId) {
        this.journalId = journalId;
    }

    /**
     *      *            @hibernate.property
     *             column="JournalName"
     *             length="255"
     *         
     */
    public String getJournalName() {
        return this.journalName;
    }
    
    public void setJournalName(String journalName) {
        this.journalName = journalName;
    }

    /**
     *      *            @hibernate.property
     *             column="JournalAbbreviation"
     *             length="50"
     *         
     */
    public String getJournalAbbreviation() {
        return this.journalAbbreviation;
    }
    
    public void setJournalAbbreviation(String journalAbbreviation) {
        this.journalAbbreviation = journalAbbreviation;
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
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="JournalID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.ReferenceWork"
     *         
     */
    public Set getReferenceWorks() {
        return this.referenceWorks;
    }
    
    public void setReferenceWorks(Set referenceWorks) {
        this.referenceWorks = referenceWorks;
    }




}