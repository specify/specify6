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
public class Collectors extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long collectorsId;
     protected Integer orderNumber;
     protected String remarks;
     protected CollectingEvent collectingEvent;
     protected Agent agent;


    // Constructors

    /** default constructor */
    public Collectors() {
    }
    
    /** constructor with id */
    public Collectors(Long collectorsId) {
        this.collectorsId = collectorsId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        collectorsId = null;
        orderNumber = null;
        remarks = null;
        timestampModified = null;
        timestampCreated = new Date();
        lastEditedBy = null;
        collectingEvent = null;
        agent = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Long getCollectorsId() {
        return this.collectorsId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.collectorsId;
    }
    
    public void setCollectorsId(Long collectorsId) {
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
     *      * The CollectingEvent the agent participated in
     */
    public CollectingEvent getCollectingEvent() {
        return this.collectingEvent;
    }
    
    public void setCollectingEvent(CollectingEvent collectingEvent) {
        this.collectingEvent = collectingEvent;
    }

    /**
     *      * Link to Collectors's record in Agent table
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
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
        return 30;
    }

}
