package edu.ku.brc.specify.datamodel;

import java.util.Date;




/**
 *        @hibernate.class
 *         table="loanagents"
 *     
 */
public class LoanAgent  implements java.io.Serializable {

    // Fields    

     protected Integer loanAgentsId;
     protected String role;
     protected String remarks;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected Integer roleId;
     private Loan loan;
     private AgentAddress agentAddress;


    // Constructors

    /** default constructor */
    public LoanAgent() {
    }
    
    /** constructor with id */
    public LoanAgent(Integer loanAgentsId) {
        this.loanAgentsId = loanAgentsId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="LoanAgentsID"
     *         
     */
    public Integer getLoanAgentsId() {
        return this.loanAgentsId;
    }
    
    public void setLoanAgentsId(Integer loanAgentsId) {
        this.loanAgentsId = loanAgentsId;
    }

    /**
     *      *            @hibernate.property
     *             column="Role"
     *             length="50"
     *             not-null="true"
     *         
     */
    public String getRole() {
        return this.role;
    }
    
    public void setRole(String role) {
        this.role = role;
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
     *             update="false"
     *             not-null="true"
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
     *             not-null="true"
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
     *      *            @hibernate.property
     *             column="RoleID"
     *             length="10"
     *         
     */
    public Integer getRoleId() {
        return this.roleId;
    }
    
    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     * 			cascade="delete"
     *            @hibernate.column name="LoanID"         
     *         
     */
    public Loan getLoan() {
        return this.loan;
    }
    
    public void setLoan(Loan loan) {
        this.loan = loan;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="AgentAddressID"         
     *         
     */
    public AgentAddress getAgentAddress() {
        return this.agentAddress;
    }
    
    public void setAgentAddress(AgentAddress agentAddress) {
        this.agentAddress = agentAddress;
    }




}