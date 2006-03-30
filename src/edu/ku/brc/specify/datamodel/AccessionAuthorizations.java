package edu.ku.brc.specify.datamodel;

import java.util.Date;




/**

 */
public class AccessionAuthorizations  implements java.io.Serializable {

    // Fields    

     protected Integer accessionAuthorizationsId;
     protected String remarks;
     private Date timestampModified;
     private Date timestampCreated;
     private String lastEditedBy;
     private Permit permit;
     private Accession accession;
     private RepositoryAgreement repositoryAgreement;


    // Constructors

    /** default constructor */
    public AccessionAuthorizations() {
    }
    
    /** constructor with id */
    public AccessionAuthorizations(Integer accessionAuthorizationsId) {
        this.accessionAuthorizationsId = accessionAuthorizationsId;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Integer getAccessionAuthorizationsId() {
        return this.accessionAuthorizationsId;
    }
    
    public void setAccessionAuthorizationsId(Integer accessionAuthorizationsId) {
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




}