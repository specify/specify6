package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;




/**

 */
public class LocalityCitation  implements java.io.Serializable {

    // Fields    

     protected Integer localityCitationId;
     protected String remarks;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected ReferenceWork referenceWork;
     protected Locality locality;


    // Constructors

    /** default constructor */
    public LocalityCitation() {
    }
    
    /** constructor with id */
    public LocalityCitation(Integer localityCitationId) {
        this.localityCitationId = localityCitationId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        localityCitationId = null;
        remarks = null;
        timestampCreated = Calendar.getInstance().getTime();
        timestampModified = null;
        lastEditedBy = null;
        referenceWork = null;
        locality = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Integer getLocalityCitationId() {
        return this.localityCitationId;
    }
    
    public void setLocalityCitationId(Integer localityCitationId) {
        this.localityCitationId = localityCitationId;
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
     *      * ID of work citing locality
     */
    public ReferenceWork getReferenceWork() {
        return this.referenceWork;
    }
    
    public void setReferenceWork(ReferenceWork referenceWork) {
        this.referenceWork = referenceWork;
    }

    /**
     *      * ID of locality cited
     */
    public Locality getLocality() {
        return this.locality;
    }
    
    public void setLocality(Locality locality) {
        this.locality = locality;
    }




    // Add Methods

    // Done Add Methods
}
