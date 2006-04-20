package edu.ku.brc.specify.datamodel;

import java.util.Date;




/**

 */
public class LoanAgent  implements java.io.Serializable {

    // Fields    

     protected Integer loanAgentsId;
     protected String role;
     protected String remarks;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected Loan loan;
     protected Agent agent;


    // Constructors

    /** default constructor */
    public LoanAgent() {
    }
    
    /** constructor with id */
    public LoanAgent(Integer loanAgentsId) {
        this.loanAgentsId = loanAgentsId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        loanAgentsId = null;
        role = null;
        remarks = null;
        timestampCreated = new Date();
        timestampModified = new Date();
        lastEditedBy = null;
        loan = null;
        agent = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Integer getLoanAgentsId() {
        return this.loanAgentsId;
    }
    
    public void setLoanAgentsId(Integer loanAgentsId) {
        this.loanAgentsId = loanAgentsId;
    }

    /**
     *      * Role the agent played in the loan
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
     *      * ID of loan agent at AgentID played a role in
     */
    public Loan getLoan() {
        return this.loan;
    }
    
    public void setLoan(Loan loan) {
        this.loan = loan;
    }

    /**
     *      * Address of agent
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
