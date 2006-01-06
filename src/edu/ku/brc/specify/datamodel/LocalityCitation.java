package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="localitycitation"
 *     
 */
public class LocalityCitation  implements java.io.Serializable {

    // Fields    

     protected Integer localityCitationId;
     protected String remarks;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     private ReferenceWork referenceWork;
     private Locality locality;


    // Constructors

    /** default constructor */
    public LocalityCitation() {
    }
    
    /** constructor with id */
    public LocalityCitation(Integer localityCitationId) {
        this.localityCitationId = localityCitationId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="LocalityCitationID"
     *         
     */
    public Integer getLocalityCitationId() {
        return this.localityCitationId;
    }
    
    public void setLocalityCitationId(Integer localityCitationId) {
        this.localityCitationId = localityCitationId;
    }

    /**
     *      *            @hibernate.property
     *             column="Remarks"
     *             length="1073741823"
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
     *            @hibernate.column name="LocalityID"         
     *         
     */
    public Locality getLocality() {
        return this.locality;
    }
    
    public void setLocality(Locality locality) {
        this.locality = locality;
    }




}