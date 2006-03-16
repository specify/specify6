package edu.ku.brc.specify.datamodel;

import java.util.Date;




/**
 *        @hibernate.class
 *         table="determinationcitation"
 *     
 */
public class DeterminationCitation  implements java.io.Serializable {

    // Fields    

     protected Integer determinationCitationId;
     protected String remarks;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     private ReferenceWork referenceWork;
     private Determination determination;


    // Constructors

    /** default constructor */
    public DeterminationCitation() {
    }
    
    /** constructor with id */
    public DeterminationCitation(Integer determinationCitationId) {
        this.determinationCitationId = determinationCitationId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="DeterminationCitationID"
     *         
     */
    public Integer getDeterminationCitationId() {
        return this.determinationCitationId;
    }
    
    public void setDeterminationCitationId(Integer determinationCitationId) {
        this.determinationCitationId = determinationCitationId;
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
     *             update="false"
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
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="ReferenceWorkID"         
     *         
     */
    public ReferenceWork getReferenceWork() {
        return this.referenceWork;
    }
    
    public void setReferenceWork(ReferenceWork referenceWork) {
        this.referenceWork = referenceWork;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     * 			cascade="delete"
     *            @hibernate.column name="DeterminationID"         
     *         
     */
    public Determination getDetermination() {
        return this.determination;
    }
    
    public void setDetermination(Determination determination) {
        this.determination = determination;
    }




}