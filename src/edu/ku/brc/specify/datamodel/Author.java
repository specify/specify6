package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="authors"
 *     
 */
public class Author  implements java.io.Serializable {

    // Fields    

     protected Integer authorsId;
     protected Short order1;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     private ReferenceWork referenceWork;
     private Agent agent;


    // Constructors

    /** default constructor */
    public Author() {
    }
    
    /** constructor with id */
    public Author(Integer authorsId) {
        this.authorsId = authorsId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="AuthorsID"
     *         
     */
    public Integer getAuthorsId() {
        return this.authorsId;
    }
    
    public void setAuthorsId(Integer authorsId) {
        this.authorsId = authorsId;
    }

    /**
     *      *            @hibernate.property
     *             column="Order1"
     *             length="5"
     *             not-null="true"
     *         
     */
    public Short getOrder1() {
        return this.order1;
    }
    
    public void setOrder1(Short order1) {
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
     *            @hibernate.column name="ReferenceWorkID"         
     *         
     */
    public ReferenceWork getReferenceWork() {
        return this.referenceWork;
    }
    
    public void setReferenceWork(ReferenceWork referenceWork) {
        this.referenceWork = referenceWork;
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