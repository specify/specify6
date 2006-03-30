package edu.ku.brc.specify.datamodel;

import java.util.Date;




/**

 */
public class BorrowAgent  implements java.io.Serializable {

    // Fields    

     protected Integer borrowAgentsId;
     protected String role;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     private AgentAddress agentAddress;
     private Borrow borrow;


    // Constructors

    /** default constructor */
    public BorrowAgent() {
    }
    
    /** constructor with id */
    public BorrowAgent(Integer borrowAgentsId) {
        this.borrowAgentsId = borrowAgentsId;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Integer getBorrowAgentsId() {
        return this.borrowAgentsId;
    }
    
    public void setBorrowAgentsId(Integer borrowAgentsId) {
        this.borrowAgentsId = borrowAgentsId;
    }

    /**
     *      * Role played by agent in borrow
     */
    public String getRole() {
        return this.role;
    }
    
    public void setRole(String role) {
        this.role = role;
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
     *      * Address/Organization from which agent participated in the borrow
     */
    public AgentAddress getAgentAddress() {
        return this.agentAddress;
    }
    
    public void setAgentAddress(AgentAddress agentAddress) {
        this.agentAddress = agentAddress;
    }

    /**
     *      * ID of borrow in which Agent played role
     */
    public Borrow getBorrow() {
        return this.borrow;
    }
    
    public void setBorrow(Borrow borrow) {
        this.borrow = borrow;
    }




}