package edu.ku.brc.specify.datamodel;

import java.util.Date;




/**

 */
public class AccessionAgent  implements java.io.Serializable {

    // Fields    

     protected Integer accessionAgentsId;
     protected String role;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     protected Agent agent;
     protected Accession accession;
     protected RepositoryAgreement repositoryAgreement;


    // Constructors

    /** default constructor */
    public AccessionAgent() {
    }
    
    /** constructor with id */
    public AccessionAgent(Integer accessionAgentsId) {
        this.accessionAgentsId = accessionAgentsId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        accessionAgentsId = null;
        role = null;
        remarks = null;
        timestampModified = new Date();
        timestampCreated = new Date();
        lastEditedBy = null;
        agent = null;
        accession = null;
        repositoryAgreement = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Integer getAccessionAgentsId() {
        return this.accessionAgentsId;
    }
    
    public void setAccessionAgentsId(Integer accessionAgentsId) {
        this.accessionAgentsId = accessionAgentsId;
    }

    /**
     *      * Role the agent played in the accession process
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
     *      * AgentAdress of agent playing role in Accession
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    /**
     *      * Accession in which the Agent played a role
     */
    public Accession getAccession() {
        return this.accession;
    }
    
    public void setAccession(Accession accession) {
        this.accession = accession;
    }

    /**
     * 
     */
    public RepositoryAgreement getRepositoryAgreement() {
        return this.repositoryAgreement;
    }
    
    public void setRepositoryAgreement(RepositoryAgreement repositoryAgreement) {
        this.repositoryAgreement = repositoryAgreement;
    }





    // Add Methods

    // Done Add Methods

    // Delete Methods

    // Delete Add Methods
}
