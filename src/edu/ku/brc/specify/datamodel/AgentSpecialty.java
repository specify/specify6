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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.util.Orderable;

/**
 * @author mkelly
 * 
 * @code_status Alpha
 * 
 * Created Date: Aug 19, 2008
 */

@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "agentspecialty", uniqueConstraints = { @UniqueConstraint(columnNames = {"AgentID", "OrderNumber"}) })
public class AgentSpecialty extends DataModelObjBase implements Serializable, Orderable, Comparable<AgentSpecialty>
{

    protected Integer agentSpecialtyId;
    protected Integer orderNumber;
    protected String  specialtyName;
    
    protected Agent agent;
    
    public AgentSpecialty()
    {
        super();
    }
    
    public AgentSpecialty(Integer agentSpecialtyId)
    {
        super();
        this.agentSpecialtyId = agentSpecialtyId;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        agentSpecialtyId = null;
        orderNumber      = null;
        specialtyName    = null;
        agent            = null;
    }
    
    @Id
    @GeneratedValue
    @Column(name = "AgentSpecialtyID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getAgentSpecialtyId()
    {
        return agentSpecialtyId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return this.agentSpecialtyId;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return AgentSpecialty.class;
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
    
    public void setAgentSpecialtyId(Integer agentSpecialtyId)
    {
        this.agentSpecialtyId = agentSpecialtyId;
    }
    
    @Column(name = "SpecialtyName", unique = false, nullable = false, insertable = true, updatable = true, length = 64)
    public String getSpecialtyName()
    {
        return specialtyName;
    }

    public void setSpecialtyName(String specialtyName)
    {
        this.specialtyName = specialtyName;
    }
    
    /**
     * @return the agent
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AgentID", unique = false, nullable = false, insertable = true, updatable = true)
    public Agent getAgent()
    {
        return agent;
    }

    /**
     * @param agent the agent to set
     */
    public void setAgent(Agent agent)
    {
        this.agent = agent;
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
        return 86;
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
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(AgentSpecialty obj)
    {
        return orderNumber.compareTo(obj.orderNumber);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Short getParentTableId()
    {
        return (short)Agent.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return agent != null ? agent.getId() : null;
    }
    
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
}
