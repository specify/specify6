package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="grouppersons"
 *     
 */
public class GroupPerson  implements java.io.Serializable {

    // Fields    

     protected Integer groupPersonsId;
     protected Short orderNumber;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     private Agent agentByGroupId;
     private Agent agentByMemberId;


    // Constructors

    /** default constructor */
    public GroupPerson() {
    }
    
    /** constructor with id */
    public GroupPerson(Integer groupPersonsId) {
        this.groupPersonsId = groupPersonsId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="GroupPersonsID"
     *         
     */
    public Integer getGroupPersonsId() {
        return this.groupPersonsId;
    }
    
    public void setGroupPersonsId(Integer groupPersonsId) {
        this.groupPersonsId = groupPersonsId;
    }

    /**
     *      *            @hibernate.property
     *             column="OrderNumber"
     *             length="5"
     *             not-null="true"
     *         
     */
    public Short getOrderNumber() {
        return this.orderNumber;
    }
    
    public void setOrderNumber(Short orderNumber) {
        this.orderNumber = orderNumber;
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
     *            @hibernate.column name="GroupID"         
     *         
     */
    public Agent getAgentByGroupId() {
        return this.agentByGroupId;
    }
    
    public void setAgentByGroupId(Agent agentByGroupId) {
        this.agentByGroupId = agentByGroupId;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="MemberID"         
     *         
     */
    public Agent getAgentByMemberId() {
        return this.agentByMemberId;
    }
    
    public void setAgentByMemberId(Agent agentByMemberId) {
        this.agentByMemberId = agentByMemberId;
    }




}