package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="project"
 *     
 */
public class Project  implements java.io.Serializable {

    // Fields    

     protected Integer projectId;
     private String projectName;
     private String projectDescription;
     private String url;
     private Integer startDate;
     private Integer endDate;
     private String remarks;
     private String text1;
     private String text2;
     private Float number1;
     private Float number2;
     private Date timestampCreated;
     private Date timestampModified;
     private String lastEditedBy;
     private Short yesNo1;
     private Short yesNo2;
     private Agent agent;
     private Set projectCollectionObjects;


    // Constructors

    /** default constructor */
    public Project() {
    }
    
    /** constructor with id */
    public Project(Integer projectId) {
        this.projectId = projectId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="ProjectID"
     *         
     */
    public Integer getProjectId() {
        return this.projectId;
    }
    
    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    /**
     *      *            @hibernate.property
     *             column="ProjectName"
     *             length="50"
     *             not-null="true"
     *         
     */
    public String getProjectName() {
        return this.projectName;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     *      *            @hibernate.property
     *             column="ProjectDescription"
     *             length="1073741823"
     *         
     */
    public String getProjectDescription() {
        return this.projectDescription;
    }
    
    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    /**
     *      *            @hibernate.property
     *             column="URL"
     *             length="300"
     *         
     */
    public String getUrl() {
        return this.url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     *      *            @hibernate.property
     *             column="StartDate"
     *             length="10"
     *         
     */
    public Integer getStartDate() {
        return this.startDate;
    }
    
    public void setStartDate(Integer startDate) {
        this.startDate = startDate;
    }

    /**
     *      *            @hibernate.property
     *             column="EndDate"
     *             length="10"
     *         
     */
    public Integer getEndDate() {
        return this.endDate;
    }
    
    public void setEndDate(Integer endDate) {
        this.endDate = endDate;
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
     *            @hibernate.column name="ProjectAgentID"         
     *         
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="ProjectID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.ProjectCollectionObject"
     *         
     */
    public Set getProjectCollectionObjects() {
        return this.projectCollectionObjects;
    }
    
    public void setProjectCollectionObjects(Set projectCollectionObjects) {
        this.projectCollectionObjects = projectCollectionObjects;
    }




}