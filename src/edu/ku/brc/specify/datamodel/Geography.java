package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="Geography"
 *     
 */
public class Geography  implements java.io.Serializable {

    // Fields    

     protected Integer geographyId;
     protected String continentOrOcean;
     protected String country;
     protected String state;
     protected String county;
     protected String islandGroup;
     protected String island;
     protected String waterBody;
     protected String drainage;
     protected String fullGeographicName;
     protected String remarks;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected Date timestampVersion;
     protected String lastEditedBy;
     protected Short isCurrent;
     private Set localitiesByGeographyId1;
     private Set geographies;
     private Geography geography;


    // Constructors

    /** default constructor */
    public Geography() {
    }
    
    /** constructor with id */
    public Geography(Integer geographyId) {
        this.geographyId = geographyId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="GeographyID"
     *         
     */
    public Integer getGeographyId() {
        return this.geographyId;
    }
    
    public void setGeographyId(Integer geographyId) {
        this.geographyId = geographyId;
    }

    /**
     *      *            @hibernate.property
     *             column="ContinentOrOcean"
     *             length="100"
     *         
     */
    public String getContinentOrOcean() {
        return this.continentOrOcean;
    }
    
    public void setContinentOrOcean(String continentOrOcean) {
        this.continentOrOcean = continentOrOcean;
    }

    /**
     *      *            @hibernate.property
     *             column="Country"
     *             length="50"
     *         
     */
    public String getCountry() {
        return this.country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     *      *            @hibernate.property
     *             column="State"
     *             length="50"
     *         
     */
    public String getState() {
        return this.state;
    }
    
    public void setState(String state) {
        this.state = state;
    }

    /**
     *      *            @hibernate.property
     *             column="County"
     *             length="50"
     *         
     */
    public String getCounty() {
        return this.county;
    }
    
    public void setCounty(String county) {
        this.county = county;
    }

    /**
     *      *            @hibernate.property
     *             column="IslandGroup"
     *             length="50"
     *         
     */
    public String getIslandGroup() {
        return this.islandGroup;
    }
    
    public void setIslandGroup(String islandGroup) {
        this.islandGroup = islandGroup;
    }

    /**
     *      *            @hibernate.property
     *             column="Island"
     *             length="50"
     *         
     */
    public String getIsland() {
        return this.island;
    }
    
    public void setIsland(String island) {
        this.island = island;
    }

    /**
     *      *            @hibernate.property
     *             column="WaterBody"
     *             length="50"
     *         
     */
    public String getWaterBody() {
        return this.waterBody;
    }
    
    public void setWaterBody(String waterBody) {
        this.waterBody = waterBody;
    }

    /**
     *      *            @hibernate.property
     *             column="Drainage"
     *             length="50"
     *         
     */
    public String getDrainage() {
        return this.drainage;
    }
    
    public void setDrainage(String drainage) {
        this.drainage = drainage;
    }

    /**
     *      *            @hibernate.property
     *             column="FullGeographicName"
     *             length="255"
     *         
     */
    public String getFullGeographicName() {
        return this.fullGeographicName;
    }
    
    public void setFullGeographicName(String fullGeographicName) {
        this.fullGeographicName = fullGeographicName;
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
     *      *            @hibernate.property
     *             column="IsCurrent"
     *             length="5"
     *         
     */
    public Short getIsCurrent() {
        return this.isCurrent;
    }
    
    public void setIsCurrent(Short isCurrent) {
        this.isCurrent = isCurrent;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="GeographyID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Locality"
     *         
     */
    public Set getLocalitiesByGeographyId1() {
        return this.localitiesByGeographyId1;
    }
    
    public void setLocalitiesByGeographyId1(Set localitiesByGeographyId1) {
        this.localitiesByGeographyId1 = localitiesByGeographyId1;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="CurrentID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Geography"
     *         
     */
    public Set getGeographies() {
        return this.geographies;
    }
    
    public void setGeographies(Set geographies) {
        this.geographies = geographies;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="CurrentID"         
     *         
     */
    public Geography getGeography() {
        return this.geography;
    }
    
    public void setGeography(Geography geography) {
        this.geography = geography;
    }




}