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

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 16, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "fieldnotebookpageset")
@org.hibernate.annotations.Table(appliesTo="fieldnotebookpageset", indexes =
    {   @Index (name="FNBPSStartDateIDX", columnNames={"StartDate"}),
        @Index (name="FNBPSEndDateIDX", columnNames={"EndDate"})
    })
public class FieldNotebookPageSet extends DisciplineMember
{
    protected Integer    fieldNotebookPageSetId;
    protected Calendar   startDate;
    protected Calendar   endDate;
    protected String     method;
    protected Short      orderNumber;
    protected String     description;
    
    protected Agent                               sourceAgent;
    protected FieldNotebook                       fieldNotebook;
    protected Set<FieldNotebookPage>              pages;
    protected Set<FieldNotebookPageSetAttachment> attachments;
    
    /**
     * 
     */
    public FieldNotebookPageSet()
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();

        fieldNotebookPageSetId = null;
        startDate              = null;
        endDate                = null;
        method                 = null;
        orderNumber            = null;
        description            = null;
        sourceAgent            = null;
        fieldNotebook          = null;
        pages                  = new HashSet<FieldNotebookPage>();
        attachments            = new TreeSet<FieldNotebookPageSetAttachment>();
    }

    /**
     * @param fieldNotebookPageSetId the fieldNotebookPageSetId to set
     */
    public void setFieldNotebookPageSetId(Integer fieldNotebookPageSetId)
    {
        this.fieldNotebookPageSetId = fieldNotebookPageSetId;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(Calendar startDate)
    {
        this.startDate = startDate;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(Calendar endDate)
    {
        this.endDate = endDate;
    }

    /**
     * @param method the method to set
     */
    public void setMethod(String method)
    {
        this.method = method;
    }

    /**
     * @param orderNumber the orderNumber to set
     */
    public void setOrderNumber(Short orderNumber)
    {
        this.orderNumber = orderNumber;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @param sourceAgent the sourceAgent to set
     */
    public void setSourceAgent(Agent sourceAgent)
    {
        this.sourceAgent = sourceAgent;
    }

    /**
     * @param fieldNotebook the fieldNotebook to set
     */
    public void setFieldNotebook(FieldNotebook fieldNotebook)
    {
        this.fieldNotebook = fieldNotebook;
    }

    /**
     * @param pages the pages to set
     */
    public void setPages(Set<FieldNotebookPage> pages)
    {
        this.pages = pages;
    }

    /**
     * @return the fieldNotebookPageSetId
     */
    @Id
    @GeneratedValue
    @Column(name = "FieldNotebookPageSetID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getFieldNotebookPageSetId()
    {
        return fieldNotebookPageSetId;
    }

    /**
     * @return the startDate
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "StartDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getStartDate()
    {
        return startDate;
    }

    /**
     * @return the endDate
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "EndDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getEndDate()
    {
        return endDate;
    }

    /**
     * @return the method
     */
    @Column(name = "Method", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getMethod()
    {
        return method;
    }

    /**
     * @return the orderNumber
     */
    @Column(name = "OrderNumber", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getOrderNumber()
    {
        return orderNumber;
    }

    /**
     * @return the description
     */
    @Column(name = "Description", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getDescription()
    {
        return description;
    }

    /**
     * @return the sourceAgent
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AgentID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getSourceAgent()
    {
        return sourceAgent;
    }

    /**
     * @return the fieldNotebook
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "FieldNotebookID", unique = false, nullable = true, insertable = true, updatable = true)
    public FieldNotebook getFieldNotebook()
    {
        return fieldNotebook;
    }

    /**
     * @return the pages
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "pageSet")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @OrderBy("pageNumber ASC")
    public Set<FieldNotebookPage> getPages()
    {
        return pages;
    }
    
    @OneToMany(mappedBy = "fieldNotebookPageSet")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @OrderBy("ordinal ASC")
    public Set<FieldNotebookPageSetAttachment> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(Set<FieldNotebookPageSetAttachment> attachments)
    {
        this.attachments = attachments;
    }

    //---------------------------------------------------------------------------
    // Overrides DataModelObjBase
    //---------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Short getParentTableId()
    {
        return (short)FieldNotebook.getClassTableId();
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
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return fieldNotebookPageSetId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return FieldNotebookPageSet.class;
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
        return 84;
    }


}
