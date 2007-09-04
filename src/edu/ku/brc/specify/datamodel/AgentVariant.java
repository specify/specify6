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
/**
 * 
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

/**
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Sep 4, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "agentvariant")
public class AgentVariant extends DataModelObjBase implements Serializable
{
    public static final Byte VARIANT    = 0;
    public static final Byte VERNACULAR = 1;
    
    protected Integer            agentVariantId;
    protected Byte               varType;
    protected String             name;
    protected Agent              agent;

    public AgentVariant()
    {
        super();
    }

    public AgentVariant(Integer agentVariantId)
    {
        super();
        this.agentVariantId = agentVariantId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        agentVariantId = null;
        name           = null;
        agent          = null;
    }

    @Id
    @GeneratedValue
    @Column(name = "AgentVariantID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getAgentVariantId()
    {
        return agentVariantId;
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
        return this.agentVariantId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return AgentVariant.class;
    }

    public void setAgentVariantId(Integer agentVariantId)
    {
        this.agentVariantId = agentVariantId;
    }

    @Column(name = "Name", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the varType
     */
    @Column(name = "VarType", unique = false, nullable = false, insertable = true, updatable = true)
    public Byte getVarType()
    {
        return varType;
    }

    /**
     * @param varType the varType to set
     */
    public void setVarType(Byte varType)
    {
        this.varType = varType;
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
        return 107;
    }

    @Override
    @Transient
    public String getIdentityTitle()
    {
        return this.name;
    }

}
