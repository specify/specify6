package edu.ku.brc.specify.datamodel;

import java.util.Date;




/**
 *        @hibernate.class
 *         table="inforequest"
 *     
 */
public class InfoRequest  implements java.io.Serializable {

    // Fields    

     private Long infoRequestID;
     protected String firstName;
     protected String lastName;
     protected String institution;
     protected String email;
     protected Date requestDate;
     protected Date replyDate;
     protected String remarks;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected RecordSet recordSet;
     private Agent agent;


    // Constructors

    /** default constructor */
    public InfoRequest() {
    }
    
    /** constructor with id */
    public InfoRequest(Long infoRequestID) {
        this.infoRequestID = infoRequestID;
    }
   
    
    

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
     *      *            @hibernate.property
     *             column="Firstname"
     *             length="50"
     *         
     */
    public String getFirstName() {
        return this.firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     *      *            @hibernate.property
     *             column="Lastname"
     *             length="50"
     *         
     */
    public String getLastName() {
        return this.lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     *      *            @hibernate.property
     *             column="Institution"
     *             length="127"
     *         
     */
    public String getInstitution() {
        return this.institution;
    }
    
    public void setInstitution(String institution) {
        this.institution = institution;
    }

    /**
     *      *            @hibernate.property
     *             column="Email"
     *             length="50"
     *         
     */
    public String getEmail() {
        return this.email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     *      *            @hibernate.property
     *             column="RequestDate"
     *             update="false"
     *          
     */
    public Date getRequestDate() {
        return this.requestDate;
    }
    
    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    /**
     *      *            @hibernate.property
     *             column="ReplyDate"
     *          
     */
    public Date getReplyDate() {
        return this.replyDate;
    }
    
    public void setReplyDate(Date replyDate) {
        this.replyDate = replyDate;
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
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="RecordSetID"         
     *         
     */
    public RecordSet getRecordSet() {
        return this.recordSet;
    }
    
    public void setRecordSet(RecordSet recordSet) {
        this.recordSet = recordSet;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="false"
     *            @hibernate.column name="AgentID"         
     *         
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }




}