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
@Table(name = "fieldnotebookpageattachment")
public class FieldNotebookPageAttachment extends DataModelObjBase implements ObjectAttachmentIFace<FieldNotebookPage>, 
                                                                             Orderable, 
                                                                             Serializable,
                                                                             Comparable<FieldNotebookPageAttachment>
{
    protected Integer           fieldNotebookPageAttachmentId;
    protected Integer           ordinal;
    protected String            remarks;
    protected FieldNotebookPage fieldNotebookPage;
    protected Attachment        attachment;
    
    public FieldNotebookPageAttachment()
    {
        // do nothing
    }
    
    public FieldNotebookPageAttachment(Integer id)
    {
        this.fieldNotebookPageAttachmentId = id;
    }

    @Override
    public void initialize()
    {
        super.init();

        fieldNotebookPageAttachmentId = null;
        ordinal                       = null;
        remarks                       = null;
        fieldNotebookPage             = null;
        attachment                    = null;
    }

    @Id
    @GeneratedValue
    @Column(name = "FieldNotebookPageAttachmentId")
    public Integer getFieldNotebookPageAttachmentId()
    {
        return fieldNotebookPageAttachmentId;
    }

    public void setFieldNotebookPageAttachmentId(Integer fieldNotebookPageAttachmentId)
    {
        this.fieldNotebookPageAttachmentId = fieldNotebookPageAttachmentId;
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
    @Column(name = "Remarks", length = 8192)
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
    @JoinColumn(name = "FieldNotebookPageID", nullable = false)
    public FieldNotebookPage getFieldNotebookPage()
    {
        return fieldNotebookPage;
    }

    public void setFieldNotebookPage(FieldNotebookPage fieldNotebookPage)
    {
        this.fieldNotebookPage = fieldNotebookPage;
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
    public FieldNotebookPage getObject()
    {
        return getFieldNotebookPage();
    }

    public void setObject(FieldNotebookPage object)
    {
        setFieldNotebookPage(object);
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
    
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return FieldNotebookPageAttachment.class;
    }

    @Transient
    @Override
    public Integer getId()
    {
        return fieldNotebookPageAttachmentId;
    }

    @Transient
    @Override
    public int getTableId()
    {
        return 128;
    }
    
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(FieldNotebookPageAttachment obj)
    {
        return ordinal.compareTo(obj.ordinal);
    }

}
