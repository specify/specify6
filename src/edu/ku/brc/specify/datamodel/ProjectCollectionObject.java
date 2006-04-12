package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;




/**

 */
public class ProjectCollectionObject  implements java.io.Serializable {

    // Fields    

     protected Integer projectCollectionObjectsId;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     protected CollectionObject collectionObject;
     protected Project project;


    // Constructors

    /** default constructor */
    public ProjectCollectionObject() {
    }
    
    /** constructor with id */
    public ProjectCollectionObject(Integer projectCollectionObjectsId) {
        this.projectCollectionObjectsId = projectCollectionObjectsId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        projectCollectionObjectsId = null;
        remarks = null;
        timestampModified = null;
        timestampCreated = Calendar.getInstance().getTime();
        lastEditedBy = null;
        collectionObject = null;
        project = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Integer getProjectCollectionObjectsId() {
        return this.projectCollectionObjectsId;
    }
    
    public void setProjectCollectionObjectsId(Integer projectCollectionObjectsId) {
        this.projectCollectionObjectsId = projectCollectionObjectsId;
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
     * 
     */
    public CollectionObject getCollectionObject() {
        return this.collectionObject;
    }
    
    public void setCollectionObject(CollectionObject collectionObject) {
        this.collectionObject = collectionObject;
    }

    /**
     * 
     */
    public Project getProject() {
        return this.project;
    }
    
    public void setProject(Project project) {
        this.project = project;
    }




    // Add Methods

    // Done Add Methods
}
