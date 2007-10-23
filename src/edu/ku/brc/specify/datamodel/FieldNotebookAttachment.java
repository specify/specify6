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
@Table(name = "fieldnotebookattachment")
public class FieldNotebookAttachment extends DataModelObjBase implements ObjectAttachmentIFace<FieldNotebook>, Orderable, Serializable
{
    protected Integer       fieldNotebookAttachmentId;
    protected Integer       ordinal;
    protected String        remarks;
    protected FieldNotebook fieldNotebook;
    protected Attachment    attachment;
    
    public FieldNotebookAttachment()
    {
        // do nothing
    }
    
    public FieldNotebookAttachment(Integer id)
    {
        this.fieldNotebookAttachmentId = id;
    }

    @Override
    public void initialize()
    {
        fieldNotebookAttachmentId = null;
        ordinal                   = null;
        remarks                   = null;
        fieldNotebook             = null;
        attachment                = null;
    }

    @Id
    @GeneratedValue
    @Column(name = "FieldNotebookAttachmentId")
    public Integer getFieldNotebookAttachmentId()
    {
        return fieldNotebookAttachmentId;
    }

    public void setFieldNotebookAttachmentId(Integer fieldNotebookAttachmentId)
    {
        this.fieldNotebookAttachmentId = fieldNotebookAttachmentId;
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
    @Column(name = "Remarks", length = 65535)
    public String getRemarks()
    {
        return remarks;
    }

    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
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

    @ManyToOne
    @JoinColumn(name = "FieldNotebookID", nullable = false)
    public FieldNotebook getFieldNotebook()
    {
        return fieldNotebook;
    }

    public void setFieldNotebook(FieldNotebook fieldNotebook)
    {
        this.fieldNotebook = fieldNotebook;
    }

    @Transient
    public int getOrderIndex()
    {
        return (this.ordinal != null) ? this.ordinal : 0;
    }

    public void setOrderIndex(int ordinal)
    {
        this.ordinal = ordinal;
    }

    @Transient
    public FieldNotebook getObject()
    {
        return getFieldNotebook();
    }

    public void setObject(FieldNotebook object)
    {
        setFieldNotebook(object);
    }
    
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return FieldNotebookAttachment.class;
    }

    @Transient
    @Override
    public Integer getId()
    {
        return getFieldNotebookAttachmentId();
    }

    @Transient
    @Override
    public int getTableId()
    {
        return 127;
    }
}
