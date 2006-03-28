package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="projectcollectionobjects"
 *     
 */
public class ProjectCollectionObject  implements java.io.Serializable {

    // Fields    

     protected Integer projectCollectionObjectsId;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     private CollectionObject collectionObject;
     private Project project;


    // Constructors

    /** default constructor */
    public ProjectCollectionObject() {
    }
    
    /** constructor with id */
    public ProjectCollectionObject(Integer projectCollectionObjectsId) {
        this.projectCollectionObjectsId = projectCollectionObjectsId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="native"
     *             type="java.lang.Integer"
     *             column="ProjectCollectionObjectsID"
     *         
     */
    public Integer getProjectCollectionObjectsId() {
        return this.projectCollectionObjectsId;
    }
    
    public void setProjectCollectionObjectsId(Integer projectCollectionObjectsId) {
        this.projectCollectionObjectsId = projectCollectionObjectsId;
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
     *            @hibernate.column name="CollectionObjectID"         
     *         
     */
    public CollectionObject getCollectionObject() {
        return this.collectionObject;
    }
    
    public void setCollectionObject(CollectionObject collectionObject) {
        this.collectionObject = collectionObject;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     * 			cascade="none"
     *            @hibernate.column name="ProjectID"         
     *         
     */
    public Project getProject() {
        return this.project;
    }
    
    public void setProject(Project project) {
        this.project = project;
    }




}