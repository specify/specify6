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
@Table(name = "fieldnotebookpagesetattachment")
public class FieldNotebookPageSetAttachment extends DataModelObjBase implements ObjectAttachmentIFace<FieldNotebookPageSet>, Orderable, Serializable
{
    protected Integer           fieldNotebookPageSetAttachmentId;
    protected Integer           ordinal;
    protected String            remarks;
    protected FieldNotebookPageSet fieldNotebookPageSet;
    protected Attachment        attachment;
    
    public FieldNotebookPageSetAttachment()
    {
        // do nothing
    }
    
    public FieldNotebookPageSetAttachment(Integer id)
    {
        this.fieldNotebookPageSetAttachmentId = id;
    }

    @Override
    public void initialize()
    {
        fieldNotebookPageSetAttachmentId = null;
        ordinal                       = null;
        remarks                       = null;
        fieldNotebookPageSet             = null;
        attachment                    = null;
    }

    @Id
    @GeneratedValue
    @Column(name = "FieldNotebookPageSetAttachmentId")
    public Integer getFieldNotebookPageSetAttachmentId()
    {
        return fieldNotebookPageSetAttachmentId;
    }

    public void setFieldNotebookPageSetAttachmentId(Integer fieldNotebookPageSetAttachmentId)
    {
        this.fieldNotebookPageSetAttachmentId = fieldNotebookPageSetAttachmentId;
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
    @JoinColumn(name = "FieldNotebookPageSetID", nullable = false)
    public FieldNotebookPageSet getFieldNotebookPageSet()
    {
        return fieldNotebookPageSet;
    }

    public void setFieldNotebookPageSet(FieldNotebookPageSet fieldNotebookPageSet)
    {
        this.fieldNotebookPageSet = fieldNotebookPageSet;
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
    public FieldNotebookPageSet getObject()
    {
        return getFieldNotebookPageSet();
    }

    public void setObject(FieldNotebookPageSet object)
    {
        setFieldNotebookPageSet(object);
    }
    
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return FieldNotebookPageSetAttachment.class;
    }

    @Transient
    @Override
    public Integer getId()
    {
        return getFieldNotebookPageSetAttachmentId();
    }

    @Transient
    @Override
    public int getTableId()
    {
        return 129;
    }
}
