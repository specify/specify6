package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="collectionobjectcitation"
 *     
 */
public class CollectionObjectCitation  implements java.io.Serializable {

    // Fields    

     protected Integer collectionObjectCitationId;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     private ReferenceWork referenceWork;
     private CollectionObject collectionObject;


    // Constructors

    /** default constructor */
    public CollectionObjectCitation() {
    }
    
    /** constructor with id */
    public CollectionObjectCitation(Integer collectionObjectCitationId) {
        this.collectionObjectCitationId = collectionObjectCitationId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="native"
     *             type="java.lang.Integer"
     *             column="CollectionObjectCitationID"
     *         
     */
    public Integer getCollectionObjectCitationId() {
        return this.collectionObjectCitationId;
    }
    
    public void setCollectionObjectCitationId(Integer collectionObjectCitationId) {
        this.collectionObjectCitationId = collectionObjectCitationId;
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
     *            @hibernate.column name="BiologicalObjectID"         
     *         
     */
    public CollectionObject getCollectionObject() {
        return this.collectionObject;
    }
    
    public void setCollectionObject(CollectionObject collectionObject) {
        this.collectionObject = collectionObject;
    }




}