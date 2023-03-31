/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
@Table(name = "fieldnotebookpageattachment")
@SuppressWarnings("serial")
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

    @Column(name = "Ordinal", nullable=false)
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
     * @see edu.ku.brc.specify.datamodel.ObjectAttachmentIFace#getTableID()
     */
    @Override
    @Transient
    public int getTableID()
    {
        return FieldNotebookPage.getClassTableId();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return FieldNotebookPage.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return fieldNotebookPage != null ? fieldNotebookPage.getId() : null;
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
        return 128;
    }

    
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(FieldNotebookPageAttachment obj)
    {
        return ordinal != null && obj.ordinal != null ? ordinal.compareTo(obj.ordinal) : 0;
    }

}
