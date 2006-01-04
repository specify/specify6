package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="SoundEventStorage"
 *     
 */
public class SoundEventStorage  implements java.io.Serializable {

    // Fields    

     protected Integer soundEventStorageId;
     protected String soundRecordingPosition;
     protected String remarks;
     protected String text1;
     protected String text2;
     protected Float number1;
     protected Float number2;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected Short yesNo1;
     protected Short yesNo2;
     private CollectionObj collectionObject;
     private Sound sound;


    // Constructors

    /** default constructor */
    public SoundEventStorage() {
    }
    
    /** constructor with id */
    public SoundEventStorage(Integer soundEventStorageId) {
        this.soundEventStorageId = soundEventStorageId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="SoundEventStorageID"
     *         
     */
    public Integer getSoundEventStorageId() {
        return this.soundEventStorageId;
    }
    
    public void setSoundEventStorageId(Integer soundEventStorageId) {
        this.soundEventStorageId = soundEventStorageId;
    }

    /**
     *      *            @hibernate.property
     *             column="SoundRecordingPosition"
     *             length="100"
     *         
     */
    public String getSoundRecordingPosition() {
        return this.soundRecordingPosition;
    }
    
    public void setSoundRecordingPosition(String soundRecordingPosition) {
        this.soundRecordingPosition = soundRecordingPosition;
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
     *             column="Text1"
     *             length="300"
     *         
     */
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      *            @hibernate.property
     *             column="Text2"
     *             length="300"
     *         
     */
    public String getText2() {
        return this.text2;
    }
    
    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      *            @hibernate.property
     *             column="Number1"
     *             length="24"
     *         
     */
    public Float getNumber1() {
        return this.number1;
    }
    
    public void setNumber1(Float number1) {
        this.number1 = number1;
    }

    /**
     *      *            @hibernate.property
     *             column="Number2"
     *             length="24"
     *         
     */
    public Float getNumber2() {
        return this.number2;
    }
    
    public void setNumber2(Float number2) {
        this.number2 = number2;
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
     *      *            @hibernate.property
     *             column="YesNo1"
     *             length="5"
     *         
     */
    public Short getYesNo1() {
        return this.yesNo1;
    }
    
    public void setYesNo1(Short yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      *            @hibernate.property
     *             column="YesNo2"
     *             length="5"
     *         
     */
    public Short getYesNo2() {
        return this.yesNo2;
    }
    
    public void setYesNo2(Short yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     * 			cascade="delete"
     *            @hibernate.column name="SoundRecordingID"         
     *         
     */
    public CollectionObj getCollectionObject() {
        return this.collectionObject;
    }
    
    public void setCollectionObject(CollectionObj collectionObject) {
        this.collectionObject = collectionObject;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="SoundEventID"         
     *         
     */
    public Sound getSound() {
        return this.sound;
    }
    
    public void setSound(Sound sound) {
        this.sound = sound;
    }




}