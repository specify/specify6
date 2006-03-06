package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="stratigraphy"
 *     
 */
public class Stratigraphy  implements java.io.Serializable {

    // Fields    

     protected Integer stratigraphyId;
     protected String superGroup;
     protected String lithoGroup;
     protected String formation;
     protected String member;
     protected String bed;
     protected String remarks;
     protected String text1;
     protected String text2;
     protected Float number1;
     protected Float number2;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     private CollectingEvent collectingEvent;
     private Set children;


    // Constructors

    /** default constructor */
    public Stratigraphy() {
    }
    
    /** constructor with id */
    public Stratigraphy(Integer stratigraphyId) {
        this.stratigraphyId = stratigraphyId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="StratigraphyID"
     *         
     */
    public Integer getStratigraphyId() {
        return this.stratigraphyId;
    }
    
    public void setStratigraphyId(Integer stratigraphyId) {
        this.stratigraphyId = stratigraphyId;
    }

    /**
     *      *            @hibernate.property
     *             column="SuperGroup"
     *             length="50"
     *         
     */
    public String getSuperGroup() {
        return this.superGroup;
    }
    
    public void setSuperGroup(String superGroup) {
        this.superGroup = superGroup;
    }

    /**
     *      *            @hibernate.property
     *             column="LithoGroup"
     *             length="50"
     *         
     */
    public String getLithoGroup() {
        return this.lithoGroup;
    }
    
    public void setLithoGroup(String lithoGroup) {
        this.lithoGroup = lithoGroup;
    }

    /**
     *      *            @hibernate.property
     *             column="Formation"
     *             length="50"
     *         
     */
    public String getFormation() {
        return this.formation;
    }
    
    public void setFormation(String formation) {
        this.formation = formation;
    }

    /**
     *      *            @hibernate.property
     *             column="Member"
     *             length="50"
     *         
     */
    public String getMember() {
        return this.member;
    }
    
    public void setMember(String member) {
        this.member = member;
    }

    /**
     *      *            @hibernate.property
     *             column="Bed"
     *             length="50"
     *         
     */
    public String getBed() {
        return this.bed;
    }
    
    public void setBed(String bed) {
        this.bed = bed;
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
     *         
     */
    public Boolean getYesNo1() {
        return this.yesNo1;
    }
    
    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      *            @hibernate.property
     *             column="YesNo2"
     *         
     */
    public Boolean getYesNo2() {
        return this.yesNo2;
    }
    
    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     *      *            @hibernate.one-to-one
     *             class="edu.ku.brc.specify.datamodel.CollectingEvent"
     *             outer-join="auto"
     *             constrained="true"
     * 			cascade="delete"
     *         
     */
    public CollectingEvent getCollectingEvent() {
        return this.collectingEvent;
    }
    
    public void setCollectingEvent(CollectingEvent collectingEvent) {
        this.collectingEvent = collectingEvent;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="GeologicTimePeriodTypeID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.GeologicTimePeriod"
     *         
     */
    public Set getChildren() {
        return this.children;
    }
    
    public void setChildren(Set children) {
        this.children = children;
    }




}