package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.Set;




/**
 *        @hibernate.class
 *         table="geography"
 *     
 */
public class Geography  implements java.io.Serializable {

    // Fields    

     protected Integer geographyId;
     protected String name;
     protected Integer rankId;
     protected Integer nodeNumber;
     protected Integer highestChildNodeNumber;
     protected String abbrev;
     protected String text1;
     protected String text2;
     protected Integer number1;
     protected Integer number2;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected Date timestampVersion;
     protected String lastEditedBy;
     protected Short isCurrent;
     private Set localities;
     private GeographyTreeDef definition;
     private Geography parent;


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
     *             column="name"
     *             length="128"
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
     *             column="RankID"
     *             length="10"
     *             index="IX_GeoRankId"
     *         
     */
    public Integer getRankId() {
        return this.rankId;
    }
    
    public void setRankId(Integer rankId) {
        this.rankId = rankId;
    }

    /**
     *      *            @hibernate.property
     *             column="NodeNumber"
     *             length="10"
     *             index="IX_GeoNodeNumber"
     *         
     */
    public Integer getNodeNumber() {
        return this.nodeNumber;
    }
    
    public void setNodeNumber(Integer nodeNumber) {
        this.nodeNumber = nodeNumber;
    }

    /**
     *      *            @hibernate.property
     *             column="HighestChildNodeNumber"
     *             length="10"
     *             index="IX_GeoHighestChildNodeNumber"
     *         
     */
    public Integer getHighestChildNodeNumber() {
        return this.highestChildNodeNumber;
    }
    
    public void setHighestChildNodeNumber(Integer highestChildNodeNumber) {
        this.highestChildNodeNumber = highestChildNodeNumber;
    }

    /**
     *      *            @hibernate.property
     *             column="abbrev"
     *             length="16"
     *         
     */
    public String getAbbrev() {
        return this.abbrev;
    }
    
    public void setAbbrev(String abbrev) {
        this.abbrev = abbrev;
    }

    /**
     *      *            @hibernate.property
     *             column="text1"
     *             length="32"
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
     *             column="text2"
     *             length="32"
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
     *             length="10"
     *         
     */
    public Integer getNumber1() {
        return this.number1;
    }
    
    public void setNumber1(Integer number1) {
        this.number1 = number1;
    }

    /**
     *      *            @hibernate.property
     *             column="Number2"
     *             length="10"
     *         
     */
    public Integer getNumber2() {
        return this.number2;
    }
    
    public void setNumber2(Integer number2) {
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
    public Set getLocalities() {
        return this.localities;
    }
    
    public void setLocalities(Set localities) {
        this.localities = localities;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="GeographyTreeDefID"         
     *         
     */
    public GeographyTreeDef getDefinition() {
        return this.definition;
    }
    
    public void setDefinition(GeographyTreeDef definition) {
        this.definition = definition;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="ParentID"         
     *         
     */
    public Geography getParent() {
        return this.parent;
    }
    
    public void setParent(Geography parent) {
        this.parent = parent;
    }




}