/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.datamodel;

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
@Table(name = "attachmentmetadata")
public class AttachmentMetadata extends DataModelObjBase implements java.io.Serializable
{
    protected Integer    attachmentMetadataID;
    protected String     name;
    protected String     value;
    protected Attachment attachment;

    public AttachmentMetadata()
    {
        // do nothing
    }

    public AttachmentMetadata(Integer attachmentMetadataID)
    {
        this.attachmentMetadataID = attachmentMetadataID;
    }

    @Override
    public void initialize()
    {
        super.init();
        attachmentMetadataID = null;
        name                 = null;
        value                = null;
        attachment           = null;
    }

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "AttachmentMetadataID")
    public Integer getAttachmentMetadataID()
    {
        return this.attachmentMetadataID;
    }

    public void setAttachmentMetadataID(Integer attachmentMetadataID)
    {
        this.attachmentMetadataID = attachmentMetadataID;
    }

    @Transient
    @Override
    public Integer getId()
    {
        return attachmentMetadataID;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return AttachmentMetadata.class;
    }

    /**
     * 
     */
    @Column(name = "Name", nullable=false, length = 64)
    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * 
     */
    @Column(name = "Value", nullable=false, length = 128)
    public String getValue()
    {
        return this.value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    /**
     * 
     */
    @ManyToOne
    @JoinColumn(name = "AttachmentID")
    public Attachment getAttachment()
    {
        return this.attachment;
    }

    public void setAttachment(Attachment attachment)
    {
        this.attachment = attachment;
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
        return 42;
    }
}
