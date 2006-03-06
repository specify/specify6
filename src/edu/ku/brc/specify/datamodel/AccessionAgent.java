package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="accessionagents"
 *     
 */
public class AccessionAgent  implements java.io.Serializable {

    // Fields    

     protected Integer accessionAgentsId;
     protected String role;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     protected Integer roleId;
     private AgentAddress agentAddress;
     protected Accession accession;


    // Constructors

    /** default constructor */
    public AccessionAgent() {
    }
    
    /** constructor with id */
    public AccessionAgent(Integer accessionAgentsId) {
        this.accessionAgentsId = accessionAgentsId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="AccessionAgentsID"
     *         
     */
    public Integer getAccessionAgentsId() {
        return this.accessionAgentsId;
    }
    
    public void setAccessionAgentsId(Integer accessionAgentsId) {
        this.accessionAgentsId = accessionAgentsId;
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
     * 			cascade="none"
     *            @hibernate.column name="AgentAddressID"         
     *         
     */
    public AgentAddress getAgentAddress() {
        return this.agentAddress;
    }
    
    public void setAgentAddress(AgentAddress agentAddress) {
        this.agentAddress = agentAddress;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="AccessionID"         
     *         
     */
    public Accession getAccession() {
        return this.accession;
    }
    
    public void setAccession(Accession accession) {
        this.accession = accession;
    }




}