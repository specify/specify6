/* Copyright (C) 2015, University of Kansas Center for Research
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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "attachmenttag")
public class AttachmentTag extends DataModelObjBase implements Serializable
{
    protected Integer    attachmentTagID;
    protected String     tag;
    protected Attachment attachment;
    
    public AttachmentTag()
    {
        
    }
    
    @Override
    public void initialize()
    {
        super.init();
        
        attachmentTagID = null;
        tag             = null;
        attachment      = null;
    }

    @Id
    @GeneratedValue
    @Column(name = "AttachmentTagID")
    public Integer getAttachmentTagID()
    {
        return attachmentTagID;
    }

    public void setAttachmentTagID(Integer attachmentTagID)
    {
        this.attachmentTagID = attachmentTagID;
    }

    @Column(name = "Tag", nullable=false, length=64)
    public String getTag()
    {
        return tag;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AttachmentID", nullable = false)
    public Attachment getAttachment()
    {
        return attachment;
    }

    public void setAttachment(Attachment attachment)
    {
        this.attachment = attachment;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Attachment.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return attachment != null ? attachment.getId() : null;
    }
    
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return AttachmentTag.class;
    }

    @Transient
    @Override
    public Integer getId()
    {
        return getAttachmentTagID();
    }

    @Transient
    @Override
    public int getTableId()
    {
        return 130;
    }
}
