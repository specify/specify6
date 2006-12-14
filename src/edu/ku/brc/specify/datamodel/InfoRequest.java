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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.RecordSetIFace;




/**

 */
public class InfoRequest extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long     infoRequestID;
     protected String   number;
     protected String   firstName;
     protected String   lastName;
     protected String   institution;
     protected String   email;
     protected Calendar requestDate;
     protected Calendar replyDate;
     protected String   remarks;
     protected RecordSetIFace recordSet;
     protected Agent    agent;


    // Constructors

    /** default constructor */
    public InfoRequest() {
    }
    
    /** constructor with id */
    public InfoRequest(Long infoRequestID) {
        this.infoRequestID = infoRequestID;
    }

    // Initializer
    public void initialize()
    {
        infoRequestID = null;
        number = null;
        firstName = null;
        lastName = null;
        institution = null;
        email = null;
        requestDate = null;
        replyDate = null;
        remarks = null;
        timestampCreated = new Date();
        timestampModified = null;
        recordSet = null;
        agent = null;
        
        // For Demo
        try
        {
            Connection conn = DBConnection.getInstance().createConnection();
            Statement  stmt = conn.createStatement();
            ResultSet  rs   = stmt.executeQuery("select number from inforequest order by Number desc limit 0,1");
            if (rs.first())
            {
                String numStr = rs.getString(1);
                int num = Integer.parseInt(numStr.substring(6,8));
                num++;
                number = String.format("2006-%03d", new Object[] {num});
            } else
            {
                number = "2006-001";
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Long getInfoRequestID() {
        return this.infoRequestID;
    }
    
    public void setInfoRequestID(Long infoRequestID) {
        this.infoRequestID = infoRequestID;
    }
    
    @Override
    public Long getId()
    {
        return infoRequestID;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    public Class<?> getDataClass()
    {
        return InfoRequest.class;
    }
    
    /**
     * 
     */
    public String getFirstName() {
        return this.firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * 
     */
    public String getLastName() {
        return this.lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * 
     */
    public String getInstitution() {
        return this.institution;
    }
    
    public void setInstitution(String institution) {
        this.institution = institution;
    }

    /**
     * 
     */
    public String getEmail() {
        return this.email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 
     */
    public Calendar getRequestDate() {
        return this.requestDate;
    }
    
    public void setRequestDate(Calendar requestDate) {
        this.requestDate = requestDate;
    }

    /**
     * 
     */
    public Calendar getReplyDate() {
        return this.replyDate;
    }
    
    public void setReplyDate(Calendar replyDate) {
        this.replyDate = replyDate;
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
    public RecordSetIFace getRecordSet() {
        return this.recordSet;
    }
    
    public void setRecordSet(RecordSetIFace recordSet) {
        this.recordSet = recordSet;
    }

    public String getNumber()
    {
        return number;
    }

    public void setNumber(String requestNumber)
    {
        this.number = requestNumber;
    }

    /**
     * 
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
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    public String getIdentityTitle()
    {
        return number != null ? number : super.getIdentityTitle();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    public Integer getTableId()
    {
        return 50;
    }

}
