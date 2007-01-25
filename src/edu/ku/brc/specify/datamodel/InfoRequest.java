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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.RecordSetIFace;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@Table(name = "inforequest")
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
     protected RecordSet recordSet;
     protected Agent    agent;


    // Constructors

    /** default constructor */
    public InfoRequest() {
        //
    }
    
    /** constructor with id */
    public InfoRequest(Long infoRequestID) {
        this.infoRequestID = infoRequestID;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        infoRequestID = null;
        number = null;
        firstName = null;
        lastName = null;
        institution = null;
        email = null;
        requestDate = null;
        replyDate = null;
        remarks = null;
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
    @Id
    @GeneratedValue
    @Column(name = "InfoRequestID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getInfoRequestID() {
        return this.infoRequestID;
    }
    
    public void setInfoRequestID(Long infoRequestID) {
        this.infoRequestID = infoRequestID;
    }
    
    @Transient
    @Override
    public Long getId()
    {
        return infoRequestID;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return InfoRequest.class;
    }
    
    /**
     * 
     */
    @Column(name = "Firstname", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getFirstName() {
        return this.firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * 
     */
    @Column(name = "Lastname", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getLastName() {
        return this.lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * 
     */
    @Column(name = "Institution", unique = false, nullable = true, insertable = true, updatable = true, length = 127)
    public String getInstitution() {
        return this.institution;
    }
    
    public void setInstitution(String institution) {
        this.institution = institution;
    }

    /**
     * 
     */
    @Column(name = "Email", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getEmail() {
        return this.email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "RequestDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getRequestDate() {
        return this.requestDate;
    }
    
    public void setRequestDate(Calendar requestDate) {
        this.requestDate = requestDate;
    }

    /**
     * 
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "ReplyDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getReplyDate() {
        return this.replyDate;
    }
    
    public void setReplyDate(Calendar replyDate) {
        this.replyDate = replyDate;
    }

    /**
     * 
     */
    @Column(name = "Remarks", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @Cascade( { CascadeType.SAVE_UPDATE })
    @JoinColumn(name = "RecordSetID", unique = false, nullable = false, insertable = true, updatable = true)
    public RecordSet getRecordSet() {
        return this.recordSet;
    }
    
    public void setRecordSet(RecordSet recordSet) {
        this.recordSet = recordSet;
    }
    
    public void setRecordSet(RecordSetIFace recordSet)
    {
        this.recordSet = (RecordSet)recordSet;
    }

    @Column(name = "Number", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
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
    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @Cascade( { CascadeType.SAVE_UPDATE })
    @JoinColumn(name = "AgentID", unique = false, nullable = true, insertable = true, updatable = true)
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
    @Transient
    public String getIdentityTitle()
    {
        return number != null ? number : super.getIdentityTitle();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public Integer getTableId()
    {
        return 50;
    }

}
