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
@Table(name = "repositoryagreementattachment")
public class RepositoryAgreementAttachment extends DataModelObjBase implements ObjectAttachmentIFace<RepositoryAgreement>, Orderable, Serializable
{
    protected Integer    repositoryAgreementAttachmentId;
    protected RepositoryAgreement     repositoryAgreement;
    protected Attachment attachment;
    protected Integer    ordinal;
    protected String     remarks;
    
    public RepositoryAgreementAttachment()
    {
        // do nothing
    }
    
    public RepositoryAgreementAttachment(Integer id)
    {
        this.repositoryAgreementAttachmentId = id;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        repositoryAgreementAttachmentId = null;
        repositoryAgreement             = null;
        attachment         = new Attachment();
        attachment.initialize();
        ordinal            = null;
    }

    @Id
    @GeneratedValue
    @Column(name = "RepositoryAgreementAttachmentID")
    public Integer getRepositoryAgreementAttachmentId()
    {
        return repositoryAgreementAttachmentId;
    }

    public void setRepositoryAgreementAttachmentId(Integer repositoryAgreementAttachmentId)
    {
        this.repositoryAgreementAttachmentId = repositoryAgreementAttachmentId;
    }

    @ManyToOne
    @JoinColumn(name = "RepositoryAgreementID", nullable = false)
    public RepositoryAgreement getRepositoryAgreement()
    {
        return repositoryAgreement;
    }

    public void setRepositoryAgreement(RepositoryAgreement repositoryAgreement)
    {
        this.repositoryAgreement = repositoryAgreement;
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
        return RepositoryAgreementAttachment.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return getRepositoryAgreementAttachmentId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getTableId()
     */
    @Override
    @Transient
    public int getTableId()
    {
        return 118;
    }

    @Transient
    public RepositoryAgreement getObject()
    {
        return getRepositoryAgreement();
    }

    public void setObject(RepositoryAgreement object)
    {
        setRepositoryAgreement(object);
    }
    
    @Override
    public String toString()
    {
        String aString = (attachment != null) ? attachment.getIdentityTitle() : "NULL Attachment";
        String oString = (getObject() != null) ? getObject().getIdentityTitle() : "NULL Object Reference";
        return aString + " : " + oString;
    }
}
