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
@Table(name = "fieldnotebookattachment")
public class FieldNotebookAttachment extends DataModelObjBase implements ObjectAttachmentIFace<FieldNotebook>, 
                                                                         Orderable, 
                                                                         Serializable,
                                                                         Comparable<FieldNotebookAttachment>
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
        super.init();

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
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
    public Attachment getAttachment()
    {
        return attachment;
    }

    public void setAttachment(Attachment attachment)
    {
        this.attachment = attachment;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return FieldNotebook.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return fieldNotebook != null ? fieldNotebook.getId() : null;
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
        return FieldNotebookAttachment.class;
    }

    @Transient
    @Override
    public Integer getId()
    {
        return fieldNotebookAttachmentId;
    }

    @Transient
    @Override
    public int getTableId()
    {
        return 127;
    }
    
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(FieldNotebookAttachment obj)
    {
        return ordinal.compareTo(obj.ordinal);
    }

}
