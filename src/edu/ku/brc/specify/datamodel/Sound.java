package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="sound"
 *     
 */
public class Sound  implements java.io.Serializable {

    // Fields    

     protected Integer soundId;
     protected Integer recordedDate;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     private CollectionObject collectionObject;
     private Set soundEventStorages;
     private Agent agent;


    // Constructors

    /** default constructor */
    public Sound() {
    }
    
    /** constructor with id */
    public Sound(Integer soundId) {
        this.soundId = soundId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="SoundID"
     *         
     */
    public Integer getSoundId() {
        return this.soundId;
    }
    
    public void setSoundId(Integer soundId) {
        this.soundId = soundId;
    }

    /**
     *      *            @hibernate.property
     *             column="RecordedDate"
     *             length="10"
     *         
     */
    public Integer getRecordedDate() {
        return this.recordedDate;
    }
    
    public void setRecordedDate(Integer recordedDate) {
        this.recordedDate = recordedDate;
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
     *             class="edu.ku.brc.specify.datamodel.CollectionObject"
     *             outer-join="auto"
     *             constrained="true"
     * 			cascade="delete"
     *         
     */
    public CollectionObject getCollectionObject() {
        return this.collectionObject;
    }
    
    public void setCollectionObject(CollectionObject collectionObject) {
        this.collectionObject = collectionObject;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="SoundEventID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.SoundEventStorage"
     *         
     */
    public Set getSoundEventStorages() {
        return this.soundEventStorages;
    }
    
    public void setSoundEventStorages(Set soundEventStorages) {
        this.soundEventStorages = soundEventStorages;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="RecordedByID"         
     *         
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }




}