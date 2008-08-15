/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.specify.datamodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import edu.ku.brc.util.Orderable;

/**
 * @author jstewart
 * @code_status Alpha
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "agentattachment")
public class AgentAttachment extends DataModelObjBase implements ObjectAttachmentIFace<Agent>, 
                                                                 Orderable, 
                                                                 Serializable,
                                                                 Comparable<AgentAttachment>
{
    protected Integer    agentAttachmentId;
    protected Agent      agent;
    protected Attachment attachment;
    protected Integer    ordinal;
    protected String     remarks;
    
    public AgentAttachment()
    {
        // do nothing
    }
    
    public AgentAttachment(Integer id)
    {
        this.agentAttachmentId = id;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        agentAttachmentId = null;
        agent             = null;
        attachment         = new Attachment();
        attachment.initialize();
        ordinal            = null;
    }

    @Id
    @GeneratedValue
    @Column(name = "AgentAttachmentID")
    public Integer getAgentAttachmentId()
    {
        return agentAttachmentId;
    }

    public void setAgentAttachmentId(Integer agentAttachmentId)
    {
        this.agentAttachmentId = agentAttachmentId;
    }

    @ManyToOne
    @JoinColumn(name = "AgentID", nullable = false)
    public Agent getAgent()
    {
        return agent;
    }

    public void setAgent(Agent agent)
    {
        this.agent = agent;
    }

    @ManyToOne
    @JoinColumn(name = "AttachmentID", nullable = false)
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
    public Attachment getAttachment()
    {
        return attachment;
    }

    public void setAttachment(Attachment attachment)
    {
        this.attachment = attachment;
    }

    @Column(name = "Ordinal")
    public Integer getOrdinal()
    {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal)
    {
        this.ordinal = ordinal;
    }

    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks()
    {
        return remarks;
    }

    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.Orderable#getOrderIndex()
     */
    @Transient
    public int getOrderIndex()
    {
        return (this.ordinal != null) ? this.ordinal : 0;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.Orderable#setOrderIndex(int)
     */
    public void setOrderIndex(int ordinal)
    {
        this.ordinal = ordinal;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return AgentAttachment.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return agentAttachmentId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getTableId()
     */
    @Override
    @Transient
    public int getTableId()
    {
        return 109;
    }

    @Transient
    public Agent getObject()
    {
        return getAgent();
    }

    public void setObject(Agent object)
    {
        setAgent(object);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
         return Attachment.getIdentityTitle(this);
    }

    @Override
    public String toString()
    {
        return Attachment.getIdentityTitle(this);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(AgentAttachment obj)
    {
        return ordinal.compareTo(obj.ordinal);
    }
}
