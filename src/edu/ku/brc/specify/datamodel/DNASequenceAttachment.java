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

@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "dnasequenceattachment")
public class DNASequenceAttachment extends DataModelObjBase implements ObjectAttachmentIFace<DNASequence>, Orderable, Serializable
{
    protected Integer     dnaSequenceAttachmentId;
    protected DNASequence dnaSequence;
    protected Attachment  attachment;
    protected Integer     ordinal;
    protected String      remarks;
    
    public DNASequenceAttachment()
    {
        
    }

    @Id
    @GeneratedValue
    @Column(name = "DnaSequenceAttachmentId")
    public Integer getDnaSequenceAttachmentId()
    {
        return dnaSequenceAttachmentId;
    }

    public void setDnaSequenceAttachmentId(Integer dnaSequenceAttachmentId)
    {
        this.dnaSequenceAttachmentId = dnaSequenceAttachmentId;
    }

    @Override
    public void initialize()
    {
        dnaSequenceAttachmentId = null;
        dnaSequence             = null;
        attachment              = null;
        ordinal                 = null;
        remarks                 = null;
    }

    @Column(name = "Ordinal")
    public Integer getOrdinal()
    {
        return this.ordinal;
    }

    public void setOrdinal(Integer ordinal)
    {
        this.ordinal = ordinal;
    }

    @Lob
    @Column(name = "Remarks")
    public String getRemarks()
    {
        return this.remarks;
    }

    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    @ManyToOne
    @JoinColumn(name = "DnaSequenceID", nullable = false)
    public DNASequence getDnaSequence()
    {
        return dnaSequence;
    }

    public void setDnaSequence(DNASequence dnaSequence)
    {
        this.dnaSequence = dnaSequence;
    }

    @ManyToOne
    @JoinColumn(name = "AttachmentID", nullable = false)
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
    public Attachment getAttachment()
    {
        return this.attachment;
    }

    public void setAttachment(Attachment attachment)
    {
        this.attachment = attachment;
    }

    @Transient
    public DNASequence getObject()
    {
        return getDnaSequence();
    }

    public void setObject(DNASequence dna)
    {
        this.dnaSequence = dna;
    }

    @Transient
    public int getOrderIndex()
    {
        return getOrdinal();
    }

    public void setOrderIndex(int order)
    {
        setOrdinal(order);
    }

    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return DNASequenceAttachment.class;
    }

    @Override
    @Transient
    public Integer getId()
    {
        return getDnaSequenceAttachmentId();
    }

    @Override
    @Transient
    public int getTableId()
    {
        return 126;
    }
}
