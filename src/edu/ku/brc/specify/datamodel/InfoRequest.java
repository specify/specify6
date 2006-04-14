package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;




/**

 */
public class InfoRequest  implements java.io.Serializable {

    // Fields    

     protected Long infoRequestID;
     protected String firstName;
     protected String lastName;
     protected String institution;
     protected String email;
     protected Calendar requestDate;
     protected Calendar replyDate;
     protected String remarks;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected RecordSet recordSet;
     protected Agent agent;


    // Constructors

    /** default constructor */
    public InfoRequest() {
    }
    
    /** constructor with id */
    public InfoRequest(Long infoRequestID) {
        this.infoRequestID = infoRequestID;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        infoRequestID = null;
        firstName = null;
        lastName = null;
        institution = null;
        email = null;
        requestDate = null;
        replyDate = null;
        remarks = null;
        timestampCreated = new Date();
        timestampModified = new Date();
        recordSet = null;
        agent = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Long getInfoRequestID() {
        return this.infoRequestID;
    }
    
    public void setInfoRequestID(Long infoRequestID) {
        this.infoRequestID = infoRequestID;
    }

    /**
     * 
     */
    public String getFirstName() {
        return this.firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * 
     */
    public String getLastName() {
        return this.lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * 
     */
    public String getInstitution() {
        return this.institution;
    }
    
    public void setInstitution(String institution) {
        this.institution = institution;
    }

    /**
     * 
     */
    public String getEmail() {
        return this.email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 
     */
    public Calendar getRequestDate() {
        return this.requestDate;
    }
    
    public void setRequestDate(Calendar requestDate) {
        this.requestDate = requestDate;
    }

    /**
     * 
     */
    public Calendar getReplyDate() {
        return this.replyDate;
    }
    
    public void setReplyDate(Calendar replyDate) {
        this.replyDate = replyDate;
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
    public RecordSet getRecordSet() {
        return this.recordSet;
    }
    
    public void setRecordSet(RecordSet recordSet) {
        this.recordSet = recordSet;
    }

    /**
     * 
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }





    // Add Methods

    // Done Add Methods

    // Delete Methods

    // Delete Add Methods
}
