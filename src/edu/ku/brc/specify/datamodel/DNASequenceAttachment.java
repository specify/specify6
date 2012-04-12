/* Copyright (C) 2009, University of Kansas Center for Research
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
public class DNASequenceAttachment extends DataModelObjBase implements ObjectAttachmentIFace<DNASequence>, 
                                                                       Orderable, 
                                                                       Serializable,
                                                                       Comparable<DNASequenceAttachment>
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
        super.init();

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
    @Column(name = "Remarks", length = 4096)
    public String getRemarks()
    {
        return this.remarks;
    }

    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "DnaSequenceID", nullable = false)
    public DNASequence getDnaSequence()
    {
        return dnaSequence;
    }

    public void setDnaSequence(DNASequence dnaSequence)
    {
        this.dnaSequence = dnaSequence;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AttachmentID", nullable = false)
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
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

    public void setObject(DNASequence dnaSequence)
    {
        this.dnaSequence = dnaSequence;
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
         return Attachment.getIdentityTitle(this);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return DNASequence.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return dnaSequence != null ? dnaSequence.getId() : null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
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
        return dnaSequenceAttachmentId;
    }

	/**
     * @return the Table ID for the class.
     */
	@Transient
    public static int getClassTableId()
    {
    	return 147;
    }
	
    @Override
    @Transient
    public int getTableId()
    {
        return getClassTableId();
    }
    
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(DNASequenceAttachment obj)
    {
        if (ordinal != null && obj != null && obj.ordinal != null)
        {
            return ordinal.compareTo(obj.ordinal);
        }
        return 0;
    }

}
