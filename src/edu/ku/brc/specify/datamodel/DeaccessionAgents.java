package edu.ku.brc.specify.datamodel;

import java.util.Date;




/**

 */
public class DeaccessionAgents  implements java.io.Serializable {

    // Fields    

     protected Integer deaccessionAgentsId;
     protected String role;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     protected Agent agent;
     protected Deaccession deaccession;


    // Constructors

    /** default constructor */
    public DeaccessionAgents() {
    }
    
    /** constructor with id */
    public DeaccessionAgents(Integer deaccessionAgentsId) {
        this.deaccessionAgentsId = deaccessionAgentsId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        deaccessionAgentsId = null;
        role = null;
        remarks = null;
        timestampModified = new Date();
        timestampCreated = new Date();
        lastEditedBy = null;
        agent = null;
        deaccession = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Integer getDeaccessionAgentsId() {
        return this.deaccessionAgentsId;
    }
    
    public void setDeaccessionAgentsId(Integer deaccessionAgentsId) {
        this.deaccessionAgentsId = deaccessionAgentsId;
    }

    /**
     *      * Role agent played in deaccession
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
     *      * AgentID for agent
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    /**
     *      * Deaccession agent played role in
     */
    public Deaccession getDeaccession() {
        return this.deaccession;
    }
    
    public void setDeaccession(Deaccession deaccession) {
        this.deaccession = deaccession;
    }





    // Add Methods

    // Done Add Methods

    // Delete Methods

    // Delete Add Methods
}
