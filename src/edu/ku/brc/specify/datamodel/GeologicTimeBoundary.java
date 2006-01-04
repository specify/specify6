package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="GeologicTimeBoundary"
 *     
 */
public class GeologicTimeBoundary  implements java.io.Serializable {

    // Fields    

     protected Integer geologicTimeBoundaryId;
     protected Float age;
     protected Float ageUncertainty;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected Date timestampVersion;
     protected String lastEditedBy;
     private Set geologicTimePeriodsByLowerBoundaryId;
     private Set geologicTimePeriodsByUpperBoundaryId;


    // Constructors

    /** default constructor */
    public GeologicTimeBoundary() {
    }
    
    /** constructor with id */
    public GeologicTimeBoundary(Integer geologicTimeBoundaryId) {
        this.geologicTimeBoundaryId = geologicTimeBoundaryId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="GeologicTimeBoundaryID"
     *         
     */
    public Integer getGeologicTimeBoundaryId() {
        return this.geologicTimeBoundaryId;
    }
    
    public void setGeologicTimeBoundaryId(Integer geologicTimeBoundaryId) {
        this.geologicTimeBoundaryId = geologicTimeBoundaryId;
    }

    /**
     *      *            @hibernate.property
     *             column="Age"
     *             length="24"
     *         
     */
    public Float getAge() {
        return this.age;
    }
    
    public void setAge(Float age) {
        this.age = age;
    }

    /**
     *      *            @hibernate.property
     *             column="AgeUncertainty"
     *             length="24"
     *         
     */
    public Float getAgeUncertainty() {
        return this.ageUncertainty;
    }
    
    public void setAgeUncertainty(Float ageUncertainty) {
        this.ageUncertainty = ageUncertainty;
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
     *             column="TimestampVersion"
     *             length="16"
     *         
     */
    public Date getTimestampVersion() {
        return this.timestampVersion;
    }
    
    public void setTimestampVersion(Date timestampVersion) {
        this.timestampVersion = timestampVersion;
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
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="LowerBoundaryID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.GeologicTimePeriod"
     *         
     */
    public Set getGeologicTimePeriodsByLowerBoundaryId() {
        return this.geologicTimePeriodsByLowerBoundaryId;
    }
    
    public void setGeologicTimePeriodsByLowerBoundaryId(Set geologicTimePeriodsByLowerBoundaryId) {
        this.geologicTimePeriodsByLowerBoundaryId = geologicTimePeriodsByLowerBoundaryId;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="UpperBoundaryID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.GeologicTimePeriod"
     *         
     */
    public Set getGeologicTimePeriodsByUpperBoundaryId() {
        return this.geologicTimePeriodsByUpperBoundaryId;
    }
    
    public void setGeologicTimePeriodsByUpperBoundaryId(Set geologicTimePeriodsByUpperBoundaryId) {
        this.geologicTimePeriodsByUpperBoundaryId = geologicTimePeriodsByUpperBoundaryId;
    }




}