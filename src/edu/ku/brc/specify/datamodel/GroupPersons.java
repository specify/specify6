/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.datamodel;

import java.util.Date;




/**

 */
public class GroupPersons extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long groupPersonsId;
     protected Short orderNumber;
     protected String remarks;
     protected Agent agentByGroup;
     protected Agent agentByMember;


    // Constructors

    /** default constructor */
    public GroupPersons() {
    }
    
    /** constructor with id */
    public GroupPersons(Long groupPersonsId) {
        this.groupPersonsId = groupPersonsId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        groupPersonsId = null;
        orderNumber = null;
        remarks = null;
        timestampModified = null;
        timestampCreated = new Date();
        lastEditedBy = null;
        agentByGroup = null;
        agentByMember = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Long getGroupPersonsId() {
        return this.groupPersonsId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.groupPersonsId;
    }
    
    public void setGroupPersonsId(Long groupPersonsId) {
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

    // Delete Methods

    // Delete Add Methods
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    public Integer getTableId()
    {
        return 49;
    }

}
