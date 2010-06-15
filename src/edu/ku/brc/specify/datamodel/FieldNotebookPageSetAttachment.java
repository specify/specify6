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

/**
 * @author jstewart
 * @code_status Alpha
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "fieldnotebookpagesetattachment")
public class FieldNotebookPageSetAttachment extends DataModelObjBase implements ObjectAttachmentIFace<FieldNotebookPageSet>, 
                                                                                Orderable, 
                                                                                Serializable,
                                                                                Comparable<FieldNotebookPageSetAttachment>
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
        super.init();

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
    @Column(name = "Remarks", length = 8192)
    public String getRemarks()
    {
        return remarks;
    }

    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @JoinColumn(name = "AttachmentID", nullable = false)
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Attachment getAttachment()
    {
        return attachment;
    }

    public void setAttachment(Attachment attachment)
    {
        this.attachment = attachment;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return FieldNotebookPageSet.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return fieldNotebookPageSet != null ? fieldNotebookPageSet.getId() : null;
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
        return FieldNotebookPageSetAttachment.class;
    }

    @Transient
    @Override
    public Integer getId()
    {
        return fieldNotebookPageSetAttachmentId;
    }

    @Transient
    @Override
    public int getTableId()
    {
        return 129;
    }
    
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(FieldNotebookPageSetAttachment obj)
    {
        if (ordinal != null && obj != null && obj.ordinal != null)
        {
            return ordinal.compareTo(obj.ordinal);
        }
        return 0;
    }

}
