package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;




/**

 */
public class GroupPerson  implements java.io.Serializable {

    // Fields    

     protected Integer groupPersonsId;
     protected Short orderNumber;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     private Agent agentByGroup;
     private Agent agentByMember;


    // Constructors

    /** default constructor */
    public GroupPerson() {
    }
    
    /** constructor with id */
    public GroupPerson(Integer groupPersonsId) {
        this.groupPersonsId = groupPersonsId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        groupPersonsId = null;
        orderNumber = null;
        remarks = null;
        timestampModified = null;
        timestampCreated = Calendar.getInstance().getTime();
        lastEditedBy = null;
        agentByGroup = null;
        agentByMember = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Integer getGroupPersonsId() {
        return this.groupPersonsId;
    }
    
    public void setGroupPersonsId(Integer groupPersonsId) {
        this.groupPersonsId = groupPersonsId;
    }

    /**
     * 
     */
    public Short getOrderNumber() {
        return this.orderNumber;
    }
    
    public void setOrderNumber(Short orderNumber) {
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
     *      * AgentID of group
     */
    public Agent getAgentByGroup() {
        return this.agentByGroup;
    }
    
    public void setAgentByGroup(Agent agentByGroup) {
        this.agentByGroup = agentByGroup;
    }

    /**
     *      * AgentID of member (member must be of type Person)
     */
    public Agent getAgentByMember() {
        return this.agentByMember;
    }
    
    public void setAgentByMember(Agent agentByMember) {
        this.agentByMember = agentByMember;
    }




    // Add Methods

    // Done Add Methods
}
