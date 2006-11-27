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
public class LoanAgents extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long loanAgentsId;
     protected String role;
     protected String remarks;
     protected Loan loan;
     protected Agent agent;


    // Constructors

    /** default constructor */
    public LoanAgents() {
    }
    
    /** constructor with id */
    public LoanAgents(Long loanAgentsId) {
        this.loanAgentsId = loanAgentsId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        loanAgentsId = null;
        role = null;
        remarks = null;
        timestampCreated = new Date();
        timestampModified = null;
        lastEditedBy = null;
        loan = null;
        agent = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Long getLoanAgentsId() {
        return this.loanAgentsId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.loanAgentsId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    public Class getDataClass()
    {
        return LoanAgents.class;
    }
    
    public void setLoanAgentsId(Long loanAgentsId) {
        this.loanAgentsId = loanAgentsId;
    }

    /**
     *      * Role the agent played in the loan
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
     *      * ID of loan agent at AgentID played a role in
     */
    public Loan getLoan() {
        return this.loan;
    }
    
    public void setLoan(Loan loan) {
        this.loan = loan;
    }

    /**
     *      * Address of agent
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
        return 53;
    }

}
