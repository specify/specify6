package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="biologicalobjectrelation"
 *     
 */
public class BiologicalObjectRelation  implements java.io.Serializable {

    // Fields    

     protected Integer biologicalObjectRelationId;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     private CollectionObject collectionObjectByRelatedBiologicalObjectId;
     private CollectionObject collectionObjectByBiologicalObjectId;
     private BiologicalObjectRelationType biologicalObjectRelationType;


    // Constructors

    /** default constructor */
    public BiologicalObjectRelation() {
    }
    
    /** constructor with id */
    public BiologicalObjectRelation(Integer biologicalObjectRelationId) {
        this.biologicalObjectRelationId = biologicalObjectRelationId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="BiologicalObjectRelationID"
     *         
     */
    public Integer getBiologicalObjectRelationId() {
        return this.biologicalObjectRelationId;
    }
    
    public void setBiologicalObjectRelationId(Integer biologicalObjectRelationId) {
        this.biologicalObjectRelationId = biologicalObjectRelationId;
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
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="RelatedBiologicalObjectID"         
     *         
     */
    public CollectionObject getCollectionObjectByRelatedBiologicalObjectId() {
        return this.collectionObjectByRelatedBiologicalObjectId;
    }
    
    public void setCollectionObjectByRelatedBiologicalObjectId(CollectionObject collectionObjectByRelatedBiologicalObjectId) {
        this.collectionObjectByRelatedBiologicalObjectId = collectionObjectByRelatedBiologicalObjectId;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="BiologicalObjectID"         
     *         
     */
    public CollectionObject getCollectionObjectByBiologicalObjectId() {
        return this.collectionObjectByBiologicalObjectId;
    }
    
    public void setCollectionObjectByBiologicalObjectId(CollectionObject collectionObjectByBiologicalObjectId) {
        this.collectionObjectByBiologicalObjectId = collectionObjectByBiologicalObjectId;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="BiologicalObjectRelationTypeID"         
     *         
     */
    public BiologicalObjectRelationType getBiologicalObjectRelationType() {
        return this.biologicalObjectRelationType;
    }
    
    public void setBiologicalObjectRelationType(BiologicalObjectRelationType biologicalObjectRelationType) {
        this.biologicalObjectRelationType = biologicalObjectRelationType;
    }




}