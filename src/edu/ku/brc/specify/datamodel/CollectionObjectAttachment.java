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
import javax.persistence.OrderBy;
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
@Table(name = "collectionobjectattachment")
public class CollectionObjectAttachment extends CollectionMember implements ObjectAttachmentIFace<CollectionObject>, Orderable, Serializable
{
    protected Integer          collectionObjectAttachmentId;
    protected CollectionObject collectionObject;
    protected Attachment       attachment;
    protected Integer          ordinal;
    protected String           remarks;
    
    public CollectionObjectAttachment()
    {
        // do nothing
    }
    
    public CollectionObjectAttachment(Integer id)
    {
        this.collectionObjectAttachmentId = id;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        collectionObjectAttachmentId = null;
        collectionObject             = null;
        ordinal                      = null;
        
        // Since the UI always creates new objects that implement ObjectAttachmentIFace and NOT new Attachments, we
        // need to create the new Attachment object here.
        attachment                   = new Attachment();
        attachment.initialize();
    }

    @Id
    @GeneratedValue
    @Column(name = "CollectionObjectAttachmentID")
    public Integer getCollectionObjectAttachmentId()
    {
        return collectionObjectAttachmentId;
    }

    public void setCollectionObjectAttachmentId(Integer collectionObjectAttachmentId)
    {
        this.collectionObjectAttachmentId = collectionObjectAttachmentId;
    }

    @ManyToOne
    @JoinColumn(name = "CollectionObjectID", nullable = false)
    public CollectionObject getCollectionObject()
    {
        return collectionObject;
    }

    public void setCollectionObject(CollectionObject collectionObject)
    {
        this.collectionObject = collectionObject;
    }

    @ManyToOne()
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
    @JoinColumn(name = "AttachmentID", nullable = false)
    @OrderBy("ordinal ASC")
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
     * @see edu.ku.brc.ui.OrderableFormDataObj#getOrderIndex()
     */
    @Transient
    public int getOrderIndex()
    {
        return (this.ordinal != null) ? this.ordinal : 0;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.OrderableFormDataObj#setOrderIndex(int)
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
        return CollectionObjectAttachment.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return getCollectionObjectAttachmentId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getTableId()
     */
    @Override
    @Transient
    public int getTableId()
    {
        return 111;
    }

    @Transient
    public CollectionObject getObject()
    {
        return getCollectionObject();
    }

    public void setObject(CollectionObject object)
    {
        setCollectionObject(object);
    }
    
    @Override
    @Transient
    public String getIdentityTitle()
    {
        if (attachment == null)
        {
            return super.getIdentityTitle() + ": NULL Attachment";
        }
        
        Attachment a = attachment;
        return a.getIdentityTitle();
    }

    @Override
    public String toString()
    {
        String aString = (attachment != null) ? attachment.getIdentityTitle() : "NULL Attachment";
        String oString = (getObject() != null) ? getObject().getIdentityTitle() : "NULL Object Reference";
        return aString + " : " + oString;
    }
}
