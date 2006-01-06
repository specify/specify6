package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="image"
 *     
 */
public class Image  implements java.io.Serializable {

    // Fields    

     protected Integer imageId;
     protected Integer madeDate;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     private CollectionObj collectionObject;
     private Set imageCollectionObjects;
     private Set imageAgents;
     private Set imageLocalities;
     private Agent agentByCopyrightOwnerId;
     private Agent agentByMadeById;


    // Constructors

    /** default constructor */
    public Image() {
    }
    
    /** constructor with id */
    public Image(Integer imageId) {
        this.imageId = imageId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="ImageID"
     *         
     */
    public Integer getImageId() {
        return this.imageId;
    }
    
    public void setImageId(Integer imageId) {
        this.imageId = imageId;
    }

    /**
     *      *            @hibernate.property
     *             column="MadeDate"
     *             length="10"
     *         
     */
    public Integer getMadeDate() {
        return this.madeDate;
    }
    
    public void setMadeDate(Integer madeDate) {
        this.madeDate = madeDate;
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
     *      *            @hibernate.one-to-one
     *             class="edu.ku.brc.specify.datamodel.CollectionObj"
     *             outer-join="auto"
     *             constrained="true"
     * 			cascade="delete"
     *         
     */
    public CollectionObj getCollectionObject() {
        return this.collectionObject;
    }
    
    public void setCollectionObject(CollectionObj collectionObject) {
        this.collectionObject = collectionObject;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="ImageID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.ImageCollectionObject"
     *         
     */
    public Set getImageCollectionObjects() {
        return this.imageCollectionObjects;
    }
    
    public void setImageCollectionObjects(Set imageCollectionObjects) {
        this.imageCollectionObjects = imageCollectionObjects;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="ImageID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.ImageAgent"
     *         
     */
    public Set getImageAgents() {
        return this.imageAgents;
    }
    
    public void setImageAgents(Set imageAgents) {
        this.imageAgents = imageAgents;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="ImageID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.ImageLocality"
     *         
     */
    public Set getImageLocalities() {
        return this.imageLocalities;
    }
    
    public void setImageLocalities(Set imageLocalities) {
        this.imageLocalities = imageLocalities;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="CopyrightOwnerID"         
     *         
     */
    public Agent getAgentByCopyrightOwnerId() {
        return this.agentByCopyrightOwnerId;
    }
    
    public void setAgentByCopyrightOwnerId(Agent agentByCopyrightOwnerId) {
        this.agentByCopyrightOwnerId = agentByCopyrightOwnerId;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="MadeByID"         
     *         
     */
    public Agent getAgentByMadeById() {
        return this.agentByMadeById;
    }
    
    public void setAgentByMadeById(Agent agentByMadeById) {
        this.agentByMadeById = agentByMadeById;
    }




}