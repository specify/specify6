package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.Set;




/**

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
     *      * Primary key
     */
    public Integer getJournalId() {
        return this.journalId;
    }
    
    public void setJournalId(Integer journalId) {
        this.journalId = journalId;
    }

    /**
     *      * Full name of the journal
     */
    public String getJournalName() {
        return this.journalName;
    }
    
    public void setJournalName(String journalName) {
        this.journalName = journalName;
    }

    /**
     * 
     */
    public String getJournalAbbreviation() {
        return this.journalAbbreviation;
    }
    
    public void setJournalAbbreviation(String journalAbbreviation) {
        this.journalAbbreviation = journalAbbreviation;
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
    public Set getReferenceWorks() {
        return this.referenceWorks;
    }
    
    public void setReferenceWorks(Set referenceWorks) {
        this.referenceWorks = referenceWorks;
    }




}