package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;




/**

 */
public class Author  implements java.io.Serializable {

    // Fields    

     protected Integer authorsId;
     protected Short orderNumber;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     private ReferenceWork referenceWork;
     private Agent agent;


    // Constructors

    /** default constructor */
    public Author() {
    }
    
    /** constructor with id */
    public Author(Integer authorsId) {
        this.authorsId = authorsId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        authorsId = null;
        orderNumber = null;
        remarks = null;
        timestampModified = null;
        timestampCreated = Calendar.getInstance().getTime();
        lastEditedBy = null;
        referenceWork = null;
        agent = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Integer getAuthorsId() {
        return this.authorsId;
    }
    
    public void setAuthorsId(Integer authorsId) {
        this.authorsId = authorsId;
    }

    /**
     * 
     */
    public Short getOrderNumber() {
        return this.orderNumber;
    }
    
    public void setOrderNumber(Short orderNumber) {
        this.orderNumber = orderNumber;
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
    public Date getTimestampModified() {
        return this.timestampModified;
    }
    
    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
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
    public String getLastEditedBy() {
        return this.lastEditedBy;
    }
    
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     *      * Reference record the Agent authored
     */
    public ReferenceWork getReferenceWork() {
        return this.referenceWork;
    }
    
    public void setReferenceWork(ReferenceWork referenceWork) {
        this.referenceWork = referenceWork;
    }

    /**
     *      * Agent record representing the Author
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }




    // Add Methods

    // Done Add Methods
}
