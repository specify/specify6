/* Copyright (C) 2013, University of Kansas Center for Research
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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.core.AppContextMgr;

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
@Table(name = "fieldnotebook")
@org.hibernate.annotations.Table(appliesTo="fieldnotebook", indexes =
    {   @Index (name="FNBNameIDX", columnNames={"Name"}),
        @Index (name="FNBStartDateIDX", columnNames={"StartDate"}),
        @Index (name="FNBEndDateIDX", columnNames={"EndDate"})
    })
public class FieldNotebook extends DisciplineMember implements AttachmentOwnerIFace<FieldNotebookAttachment>
{
    protected Integer    fieldNotebookId;
    protected String     name;
    protected Calendar   startDate;
    protected Calendar   endDate;
    protected String     location;      // physical storage of notebook
    protected String     description;
    
    protected Collection                   collection;
    protected Agent                        ownerAgent;
    protected Set<FieldNotebookPageSet>    pageSets;
    protected Set<FieldNotebookAttachment> attachments;
    
    /**
     * 
     */
    public FieldNotebook()
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
        
        fieldNotebookId = null;
        name            = null;
        startDate       = null;
        endDate         = null;
        location        = null;
        description     = null;
        collection      = null;
        ownerAgent      = null;
        pageSets        = new HashSet<FieldNotebookPageSet>();
        attachments     = new TreeSet<FieldNotebookAttachment>();
        
        collection      = AppContextMgr.getInstance().getClassObject(Collection.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#forceLoad()
     */
    @Override
    public void forceLoad()
    {
        attachments.size();
    }

    /**
     * @param fieldNotebookId the fieldNotebookId to set
     */
    public void setFieldNotebookId(Integer fieldNotebookId)
    {
        this.fieldNotebookId = fieldNotebookId;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
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
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @param storage the storage to set
     */
    public void setLocation(String location)
    {
        this.location = location;
    }

    /**
     * @param discipline the discipline to set
     */
    public void setCollection(Collection collection)
    {
        this.collection = collection;
    }

    /**
     * @param ownerAgent the ownerAgent to set
     */
    public void setOwnerAgent(Agent ownerAgent)
    {
        this.ownerAgent = ownerAgent;
    }

    /**
     * @param pageSets the pageSets to set
     */
    public void setPageSets(Set<FieldNotebookPageSet> pageSets)
    {
        this.pageSets = pageSets;
    }

    /**
     * @return the fieldNotebookId
     */
    @Id
    @GeneratedValue
    @Column(name = "FieldNotebookID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getFieldNotebookId()
    {
        return fieldNotebookId;
    }

    /**
     * @return the name
     */
    @Column(name = "Name", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getName()
    {
        return name;
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
     * @return the description
     */
    @Lob
    @Column(name = "Description", unique = false, nullable = true, insertable = true, updatable = true, length = 2048)
    public String getDescription()
    {
        return description;
    }

    /**
     * @return the storage
     */
    @Column(name = "Storage", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getLocation()
    {
        return location;
    }

    /**
     * @return the ownerAgent
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AgentID", unique = false, nullable = false, insertable = true, updatable = true)
    public Agent getOwnerAgent()
    {
        return ownerAgent;
    }

    /**
     * @return the discipline
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CollectionID", unique = false, nullable = false, insertable = true, updatable = true)
    public Collection getCollection()
    {
        return collection;
    }

    /**
     * @return the pageSets
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "fieldNotebook")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @OrderBy("startDate ASC")
    public Set<FieldNotebookPageSet> getPageSets()
    {
        return pageSets;
    }

    @OneToMany(mappedBy = "fieldNotebook")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @OrderBy("ordinal ASC")
    public Set<FieldNotebookAttachment> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(Set<FieldNotebookAttachment> attachments)
    {
        this.attachments = attachments;
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentReferences()
     */
    @Transient
    @Override
    public Set<FieldNotebookAttachment> getAttachmentReferences()
    {
        return attachments;
    }
    
    //---------------------------------------------------------------------------
    // Overrides DataModelObjBase
    //---------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
   public Integer getParentTableId()
    {
        return Collection.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
   public Integer getParentId()
    {
        return collection != null ? collection.getId() : null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return fieldNotebookId;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return FieldNotebook.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        return StringUtils.isNotEmpty(name) ? name : super.getIdentityTitle();
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentTableId()
     */
    @Override
    @Transient
    public int getAttachmentTableId()
    {
        return getClassTableId();
    }
   
    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 83;
    }

}
