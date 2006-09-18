package edu.ku.brc.specify.datamodel;

import java.util.Date;




/**

 */
public class AccessionAuthorizations  implements java.io.Serializable {

    // Fields    

     protected Long accessionAuthorizationsId;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     protected Permit permit;
     protected Accession accession;
     protected RepositoryAgreement repositoryAgreement;


    // Constructors

    /** default constructor */
    public AccessionAuthorizations() {
    }
    
    /** constructor with id */
    public AccessionAuthorizations(Long accessionAuthorizationsId) {
        this.accessionAuthorizationsId = accessionAuthorizationsId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        accessionAuthorizationsId = null;
        remarks = null;
        timestampModified = null;
        timestampCreated = new Date();
        lastEditedBy = null;
        permit = null;
        accession = null;
        repositoryAgreement = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Long getAccessionAuthorizationsId() {
        return this.accessionAuthorizationsId;
    }
    
    public void setAccessionAuthorizationsId(Long accessionAuthorizationsId) {
        this.accessionAuthorizationsId = accessionAuthorizationsId;
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
     *      * Permit authorizing accession
     */
    public Permit getPermit() {
        return this.permit;
    }
    
    public void setPermit(Permit permit) {
        this.permit = permit;
    }

    /**
     *      * Accession authorized by permit
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
