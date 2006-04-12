package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;




/**

 */
public class DeterminationCitation  implements java.io.Serializable {

    // Fields    

     protected Integer determinationCitationId;
     protected String remarks;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected ReferenceWork referenceWork;
     protected Determination determination;


    // Constructors

    /** default constructor */
    public DeterminationCitation() {
    }
    
    /** constructor with id */
    public DeterminationCitation(Integer determinationCitationId) {
        this.determinationCitationId = determinationCitationId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        determinationCitationId = null;
        remarks = null;
        timestampCreated = Calendar.getInstance().getTime();
        timestampModified = null;
        lastEditedBy = null;
        referenceWork = null;
        determination = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Integer getDeterminationCitationId() {
        return this.determinationCitationId;
    }
    
    public void setDeterminationCitationId(Integer determinationCitationId) {
        this.determinationCitationId = determinationCitationId;
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
     *      * ID of the publication citing the determination
     */
    public ReferenceWork getReferenceWork() {
        return this.referenceWork;
    }
    
    public void setReferenceWork(ReferenceWork referenceWork) {
        this.referenceWork = referenceWork;
    }

    /**
     *      * Determination being cited
     */
    public Determination getDetermination() {
        return this.determination;
    }
    
    public void setDetermination(Determination determination) {
        this.determination = determination;
    }




    // Add Methods

    // Done Add Methods
}
