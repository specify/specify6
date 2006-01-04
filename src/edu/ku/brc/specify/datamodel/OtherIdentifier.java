package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="OtherIdentifier"
 *     
 */
public class OtherIdentifier  implements java.io.Serializable {

    // Fields    

     protected Integer otherIdentifierId;
     protected String identifier;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     private CollectionObj collectionObjectCatalog;


    // Constructors

    /** default constructor */
    public OtherIdentifier() {
    }
    
    /** constructor with id */
    public OtherIdentifier(Integer otherIdentifierId) {
        this.otherIdentifierId = otherIdentifierId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="OtherIdentifierID"
     *         
     */
    public Integer getOtherIdentifierId() {
        return this.otherIdentifierId;
    }
    
    public void setOtherIdentifierId(Integer otherIdentifierId) {
        this.otherIdentifierId = otherIdentifierId;
    }

    /**
     *      *            @hibernate.property
     *             column="Identifier"
     *             length="50"
     *             not-null="true"
     *         
     */
    public String getIdentifier() {
        return this.identifier;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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
     * 			cascade="delete"
     *            @hibernate.column name="CollectionObjectID"         
     *         
     */
    public CollectionObj getCollectionObjectCatalog() {
        return this.collectionObjectCatalog;
    }
    
    public void setCollectionObjectCatalog(CollectionObj collectionObjectCatalog) {
        this.collectionObjectCatalog = collectionObjectCatalog;
    }




}