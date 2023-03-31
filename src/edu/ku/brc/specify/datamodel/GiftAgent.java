/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.datamodel;

import javax.persistence.*;

import org.hibernate.annotations.Index;

import java.util.Calendar;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "giftagent", uniqueConstraints = { 
        @UniqueConstraint(columnNames = { "Role", "GiftID", "AgentID" }) 
        })
@org.hibernate.annotations.Table(appliesTo="giftagent", indexes =
    {   @Index (name="GiftAgDspMemIDX", columnNames={"DisciplineID"})
    })
public class GiftAgent extends DisciplineMember implements java.io.Serializable {

    // Fields    

     protected Integer giftAgentId;
     protected String  role;
     protected String  remarks;
     protected Gift    gift;
     protected Agent   agent;
    protected Calendar date1;
    protected Byte                        date1Precision;


    // Constructors

    /** default constructor */
    public GiftAgent() {
        //
    }
    
    /** constructor with id */
    public GiftAgent(Integer giftAgentId) {
        this.giftAgentId = giftAgentId;
    }
   
    
    

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        giftAgentId = null;
        role = null;
        remarks = null;
        gift = null;
        agent = null;
        date1 = null;
        date1Precision = 1;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "GiftAgentID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getGiftAgentId() {
        return this.giftAgentId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.giftAgentId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return GiftAgent.class;
    }
    
    public void setGiftAgentId(Integer giftAgentId) {
        this.giftAgentId = giftAgentId;
    }

    /**
     *      * Role the agent played in the gift
     */
    @Column(name = "Role", unique = false, nullable = false, insertable = true, updatable = true, length = 50)
    public String getRole() {
        return this.role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * 
     */
    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *      * ID of gift agent at AgentID played a role in
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "GiftID", unique = false, nullable = false, insertable = true, updatable = true)
    public Gift getGift() {
        return this.gift;
    }
    
    public void setGift(Gift gift) {
        this.gift = gift;
    }

    /**
     *      * Address of agent
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AgentID", unique = false, nullable = false, insertable = true, updatable = true)
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    /**
     *
     * @return
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "Date1", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate1() {
        return date1;
    }

    /**
     *
     * @param date1
     */
    public void setDate1(Calendar date1) {
        date1 = date1;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Gift.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return gift != null ? gift.getId() : null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public int getTableId()
    {
        return getClassTableId();
    }
    
    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 133;
    }

}
