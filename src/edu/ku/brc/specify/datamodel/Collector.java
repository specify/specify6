package edu.ku.brc.specify.datamodel;

import java.util.Date;




/**

 */
public class Collector  implements java.io.Serializable {

    // Fields    

     protected Integer collectorsId;
     private Integer orderNumber;
     protected String remarks;
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
     * 
     */
    public Integer getCollectorsId() {
        return this.collectorsId;
    }
    
    public void setCollectorsId(Integer collectorsId) {
        this.collectorsId = collectorsId;
    }

    /**
     * 
     */
    public Integer getOrderNumber() {
        return this.orderNumber;
    }
    
    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    /**
     * 
     */
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * 
     */
    public Date getTimestampModified() {
        return this.timestampModified;
    }
    
    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
    }

    /**
     * 
     */
    public Date getTimestampCreated() {
        return this.timestampCreated;
    }
    
    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    /**
     * 
     */
    public String getLastEditedBy() {
        return this.lastEditedBy;
    }
    
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     *      * The CollectingEvent the agent participated in
     */
    public CollectingEvent getCollectingEvent() {
        return this.collectingEvent;
    }
    
    public void setCollectingEvent(CollectingEvent collectingEvent) {
        this.collectingEvent = collectingEvent;
    }

    /**
     *      * Link to Collector's record in Agent table
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }




}