package edu.ku.brc.specify.datamodel;

import java.util.Date;




/**
 *        @hibernate.class
 *         table="accessionauthorizations"
 *     
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
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="AccessionAuthorizationsID"
     *         
     */
    public Integer getAccessionAuthorizationsId() {
        return this.accessionAuthorizationsId;
    }
    
    public void setAccessionAuthorizationsId(Integer accessionAuthorizationsId) {
        this.accessionAuthorizationsId = accessionAuthorizationsId;
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
     *             column="TimestampCreated"
     *             length="23"
     *             not-null="true"
     *             update="false"
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
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="PermitID"         
     *         
     */
    public Permit getPermit() {
        return this.permit;
    }
    
    public void setPermit(Permit permit) {
        this.permit = permit;
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