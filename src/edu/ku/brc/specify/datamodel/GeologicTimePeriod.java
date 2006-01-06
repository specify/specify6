package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="geologictimeperiod"
 *     
 */
public class GeologicTimePeriod  implements java.io.Serializable {

    // Fields    

     protected Integer geologicTimePeriodId;
     protected Integer rankCode;
     protected String rankName;
     protected String name;
     protected String standard;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected Date timestampVersion;
     protected String lastEditedBy;
     private GeologicTimeBoundary geologicTimeBoundaryByLowerBoundaryId;
     private GeologicTimeBoundary geologicTimeBoundaryByUpperBoundaryId;


    // Constructors

    /** default constructor */
    public GeologicTimePeriod() {
    }
    
    /** constructor with id */
    public GeologicTimePeriod(Integer geologicTimePeriodId) {
        this.geologicTimePeriodId = geologicTimePeriodId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="GeologicTimePeriodID"
     *         
     */
    public Integer getGeologicTimePeriodId() {
        return this.geologicTimePeriodId;
    }
    
    public void setGeologicTimePeriodId(Integer geologicTimePeriodId) {
        this.geologicTimePeriodId = geologicTimePeriodId;
    }

    /**
     *      *            @hibernate.property
     *             column="RankCode"
     *             length="10"
     *         
     */
    public Integer getRankCode() {
        return this.rankCode;
    }
    
    public void setRankCode(Integer rankCode) {
        this.rankCode = rankCode;
    }

    /**
     *      *            @hibernate.property
     *             column="RankName"
     *             length="50"
     *         
     */
    public String getRankName() {
        return this.rankName;
    }
    
    public void setRankName(String rankName) {
        this.rankName = rankName;
    }

    /**
     *      *            @hibernate.property
     *             column="Name"
     *             length="50"
     *         
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     *      *            @hibernate.property
     *             column="Standard"
     *             length="50"
     *         
     */
    public String getStandard() {
        return this.standard;
    }
    
    public void setStandard(String standard) {
        this.standard = standard;
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
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="LowerBoundaryID"         
     *         
     */
    public GeologicTimeBoundary getGeologicTimeBoundaryByLowerBoundaryId() {
        return this.geologicTimeBoundaryByLowerBoundaryId;
    }
    
    public void setGeologicTimeBoundaryByLowerBoundaryId(GeologicTimeBoundary geologicTimeBoundaryByLowerBoundaryId) {
        this.geologicTimeBoundaryByLowerBoundaryId = geologicTimeBoundaryByLowerBoundaryId;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="UpperBoundaryID"         
     *         
     */
    public GeologicTimeBoundary getGeologicTimeBoundaryByUpperBoundaryId() {
        return this.geologicTimeBoundaryByUpperBoundaryId;
    }
    
    public void setGeologicTimeBoundaryByUpperBoundaryId(GeologicTimeBoundary geologicTimeBoundaryByUpperBoundaryId) {
        this.geologicTimeBoundaryByUpperBoundaryId = geologicTimeBoundaryByUpperBoundaryId;
    }




}