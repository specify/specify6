package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="imagelocalities"
 *     
 */
public class ImageLocality  implements java.io.Serializable {

    // Fields    

     protected Integer imageLocalitiesId;
     private String remarks;
     private Date timestampModified;
     private Date timestampCreated;
     private String lastEditedBy;
     private Image image;
     private Locality locality;


    // Constructors

    /** default constructor */
    public ImageLocality() {
    }
    
    /** constructor with id */
    public ImageLocality(Integer imageLocalitiesId) {
        this.imageLocalitiesId = imageLocalitiesId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="ImageLocalitiesID"
     *         
     */
    public Integer getImageLocalitiesId() {
        return this.imageLocalitiesId;
    }
    
    public void setImageLocalitiesId(Integer imageLocalitiesId) {
        this.imageLocalitiesId = imageLocalitiesId;
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