/**
 * Copyright (C) 2006  The University of Kansas
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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "attachmenttag")
public class AttachmentTag extends DataModelObjBase implements Serializable
{
    protected Integer    attachmentTagID;
    protected String     tag;
    protected Attachment attachment;
    
    public AttachmentTag()
    {
        
    }
    
    @Override
    public void initialize()
    {
        attachmentTagID = null;
        tag             = null;
        attachment      = null;
    }

    @Id
    @GeneratedValue
    @Column(name = "AttachmentTagID")
    public Integer getAttachmentTagID()
    {
        return attachmentTagID;
    }

    public void setAttachmentTagID(Integer attachmentTagID)
    {
        this.attachmentTagID = attachmentTagID;
    }

    @Column(name = "Tag", nullable=false, length=64)
    public String getTag()
    {
        return tag;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }

    @ManyToOne
    @JoinColumn(name = "AttachmentID", nullable = false)
    public Attachment getAttachment()
    {
        return attachment;
    }

    public void setAttachment(Attachment attachment)
    {
        this.attachment = attachment;
    }

    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return AttachmentTag.class;
    }

    @Transient
    @Override
    public Integer getId()
    {
        return getAttachmentTagID();
    }

    @Transient
    @Override
    public int getTableId()
    {
        return 130;
    }
}
