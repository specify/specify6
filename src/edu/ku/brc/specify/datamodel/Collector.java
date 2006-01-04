package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="Collectors"
 *     
 */
public class Collector  implements java.io.Serializable {

    // Fields    

     protected Integer collectorsId;
     private Integer order1;
     private String remarks;
     private Date timestampModified;
     private Date timestampCreated;
     private String lastEditedBy;
     private CollectingEvent collectingEvent;
     private Agent agent;


    // Constructors

    /** default constructor */
    public Collector() {
    }
    
    /** constructor with id */
    public Collector(Integer collectorsId) {
        this.collectorsId = collectorsId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="CollectorsID"
     *         
     */
    public Integer getCollectorsId() {
        return this.collectorsId;
    }
    
    public void setCollectorsId(Integer collectorsId) {
        this.collectorsId = collectorsId;
    }

    /**
     *      *            @hibernate.property
     *             column="Order1"
     *             length="10"
     *             not-null="true"
     *         
     */
    public Integer getOrder1() {
        return this.order1;
    }
    
    public void setOrder1(Integer order1) {
        this.order1 = order1;
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
     *            @hibernate.column name="CollectingEventID"         
     *         
     */
    public CollectingEvent getCollectingEvent() {
        return this.collectingEvent;
    }
    
    public void setCollectingEvent(CollectingEvent collectingEvent) {
        this.collectingEvent = collectingEvent;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="AgentID"         
     *         
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }




}