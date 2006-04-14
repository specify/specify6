package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.HashSet;
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
     protected Set<ReferenceWork> referenceWorks;


    // Constructors

    /** default constructor */
    public Journal() {
    }
    
    /** constructor with id */
    public Journal(Integer journalId) {
        this.journalId = journalId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        journalId = null;
        journalName = null;
        journalAbbreviation = null;
        remarks = null;
        timestampCreated = new Date();
        timestampModified = new Date();
        lastEditedBy = null;
        referenceWorks = new HashSet<ReferenceWork>();
    }
    // End Initializer

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
    public Set<ReferenceWork> getReferenceWorks() {
        return this.referenceWorks;
    }
    
    public void setReferenceWorks(Set<ReferenceWork> referenceWorks) {
        this.referenceWorks = referenceWorks;
    }





    // Add Methods

    public void addReferenceWork(final ReferenceWork referenceWork)
    {
        this.referenceWorks.add(referenceWork);
        referenceWork.setJournal(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeReferenceWork(final ReferenceWork referenceWork)
    {
        this.referenceWorks.remove(referenceWork);
        referenceWork.setJournal(null);
    }

    // Delete Add Methods
}
