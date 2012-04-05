/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.util.Orderable;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "fundingagent", uniqueConstraints = { @UniqueConstraint(columnNames = {"AgentID", "CollectingTripID"}) })
@org.hibernate.annotations.Table(appliesTo="fundingagent", indexes =
    {   
        @Index (name="COLTRIPDivIDX", columnNames={"DivisionID"})
    })
public class FundingAgent extends DataModelObjBase implements java.io.Serializable, 
                                                              Orderable, 
                                                              Comparable<FundingAgent>,
                                                              Cloneable
{
    // Fields    

     protected Integer        fundingAgentId;
     protected Integer        orderNumber;
     protected Boolean        isPrimary;
     protected String         type;
     protected String         remarks;
     protected CollectingTrip collectingTrip;
     protected Agent          agent;
     protected Division       division;

    // Constructors

    /** default constructor */
    public FundingAgent() 
    {
        //
    }
    
    /** constructor with id */
    public FundingAgent(Integer fundingAgentId) 
    {
        this.fundingAgentId = fundingAgentId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        fundingAgentId  = null;
        orderNumber     = null;
        isPrimary       = true;
        type            = null;
        remarks         = null;
        collectingTrip  = null;
        agent           = null;
        division        = AppContextMgr.getInstance() != null ? AppContextMgr.getInstance().getClassObject(Division.class) : null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "FundingAgentID")
    public Integer getFundingAgentId() 
    {
        return this.fundingAgentId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.fundingAgentId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return FundingAgent.class;
    }
    
    public void setFundingAgentId(Integer fundingAgentId) {
        this.fundingAgentId = fundingAgentId;
    }

    /**
     * 
     */
    @Column(name = "OrderNumber", nullable = false)
    public Integer getOrderNumber() {
        return this.orderNumber;
    }
    
    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    /**
     * @return the isPrimary
     */
    @Column(name = "IsPrimary", unique = false, nullable = false, insertable = true, updatable = true)
    public Boolean getIsPrimary()
    {
        return isPrimary;
    }

    /**
     * @param isPrimary the isPrimary to set
     */
    public void setIsPrimary(Boolean isPrimary)
    {
        this.isPrimary = isPrimary;
    }

    /**
     * @return the type
     */
    @Column(name = "Type", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
   public String getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        this.type = type;
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
     *      * The CollectingTrip the agent participated in
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CollectingTripID", nullable = false)
    public CollectingTrip getCollectingTrip()
    {
        return this.collectingTrip;
    }
    
    public void setCollectingTrip(CollectingTrip collectingTrip) 
    {
        this.collectingTrip = collectingTrip;
    }

    /**
     *      * Link to FundingAgent's record in Agent table
     */
    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @JoinColumn(name = "AgentID", nullable = false)
    public Agent getAgent()
    {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }
    

    /**
     *  The Division this Agent belongs to.
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "DivisionID", unique = false, nullable = true, insertable = true, updatable = true)
    public Division getDivision() 
    {
        return this.division;
    }
    
    public void setDivision(Division division) 
    {
        this.division = division;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        String name = "";
        if (agent != null)
        {
            name = agent.getIdentityTitle();
        }

        if (StringUtils.isNotEmpty(name))
        {
            return name;
        }
        
        return super.getIdentityTitle();
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
        return 146;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.Orderable#getOrderIndex()
     */
    @Transient
    public int getOrderIndex()
    {
        return getOrderNumber();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.Orderable#setOrderIndex(int)
     */
    public void setOrderIndex(int order)
    {
        setOrderNumber(order);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return CollectingTrip.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return collectingTrip != null ? collectingTrip.getId() : null;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(FundingAgent obj)
    {
        return orderNumber != null && obj != null && obj.orderNumber != null ? orderNumber.compareTo(obj.orderNumber) : 0;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        FundingAgent obj = (FundingAgent)super.clone();
        obj.setFundingAgentId(null);
        return obj;
    }

}
