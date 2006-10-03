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
public class BorrowAgents extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long borrowAgentsId;
     protected String role;
     protected String remarks;
     protected Agent agent;
     protected Borrow borrow;


    // Constructors

    /** default constructor */
    public BorrowAgents() {
    }
    
    /** constructor with id */
    public BorrowAgents(Long borrowAgentsId) {
        this.borrowAgentsId = borrowAgentsId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        borrowAgentsId = null;
        role = null;
        remarks = null;
        timestampModified = null;
        timestampCreated = new Date();
        lastEditedBy = null;
        agent = null;
        borrow = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Long getBorrowAgentsId() {
        return this.borrowAgentsId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.borrowAgentsId;
    }
    
    public void setBorrowAgentsId(Long borrowAgentsId) {
        this.borrowAgentsId = borrowAgentsId;
    }

    /**
     *      * Role played by agent in borrow
     */
    public String getRole() {
        return this.role;
    }
    
    public void setRole(String role) {
        this.role = role;
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
     *      * Address/Organization from which agent participated in the borrow
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    /**
     *      * ID of borrow in which Agent played role
     */
    public Borrow getBorrow() {
        return this.borrow;
    }
    
    public void setBorrow(Borrow borrow) {
        this.borrow = borrow;
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
        return 19;
    }

}
