package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="ImageCollectionObjects"
 *     
 */
public class ImageCollectionObject  implements java.io.Serializable {

    // Fields    

     protected Integer imageCollectionObjectsId;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     private Image image;
     private CollectionObj collectionObject;


    // Constructors

    /** default constructor */
    public ImageCollectionObject() {
    }
    
    /** constructor with id */
    public ImageCollectionObject(Integer imageCollectionObjectsId) {
        this.imageCollectionObjectsId = imageCollectionObjectsId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="ImageCollectionObjectsID"
     *         
     */
    public Integer getImageCollectionObjectsId() {
        return this.imageCollectionObjectsId;
    }
    
    public void setImageCollectionObjectsId(Integer imageCollectionObjectsId) {
        this.imageCollectionObjectsId = imageCollectionObjectsId;
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
     *            @hibernate.column name="ImageID"         
     *         
     */
    public Image getImage() {
        return this.image;
    }
    
    public void setImage(Image image) {
        this.image = image;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="CollectionlObjectID"         
     *         
     */
    public CollectionObj getCollectionObject() {
        return this.collectionObject;
    }
    
    public void setCollectionObject(CollectionObj collectionObject) {
        this.collectionObject = collectionObject;
    }




}