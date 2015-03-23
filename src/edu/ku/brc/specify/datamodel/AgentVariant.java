/* Copyright (C) 2015, University of Kansas Center for Research
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
public class AgentVariant extends DataModelObjBase implements Serializable, Cloneable
{
    public static final Byte VARIANT       = 0;
    public static final Byte VERNACULAR    = 1;
    public static final Byte AUTHOR        = 2;
    public static final Byte AUTHOR_ABBREV = 3;
    public static final Byte LABLELNAME    = 4;
    
    protected Integer            agentVariantId;
    protected String             country;    // Java Two Character Code
    protected String             language;   // Java Two Character Code
    protected String             variant;    // Java Two Character Code
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
        country        = null;
        language       = null;
        variant        = null;
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
     * @return the language
     */
    @Column(name = "Language", unique = false, nullable = true, insertable = true, updatable = true, length = 2)
    public String getLanguage()
    {
    	return language;
    }
    
    /**
     * @param language the language to set
     */
    public void setLanguage(String language)
    {
    	this.language = language;
    }
    
    /**
     * @return the country 
     */
    @Column(name = "Country", unique = false, nullable = true, insertable = true, updatable = true, length = 2)
    public String getCountry()
    {
    	return country;
    }
    
    /**
     * @param country the country to set 
     */
    public void setCountry(String country)
    {
    	this.country = country;
    }
    
    /**
     * @return the variant
     */
    @Column(name = "Variant", unique = false, nullable = true, insertable = true, updatable = true, length = 2)
    public String getVariant()
    {
    	return variant;
    }
    
    /**
     * @param variant the variant to set
     */
    public void setVariant(String variant)
    {
    	this.variant = variant;
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
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Agent.getClassTableId();
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        return this.name != null ? this.name : super.getIdentityTitle();
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        AgentVariant obj = (AgentVariant) super.clone();
        obj.setAgentVariantId(null);
        return obj;
    }
}
