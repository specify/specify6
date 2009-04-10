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

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
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
@Table(name = "fieldnotebookpage")
@org.hibernate.annotations.Table(appliesTo="fieldnotebookpage", indexes =
    {   @Index (name="FNBPPageNumberIDX", columnNames={"PageNumber"}),
        @Index (name="FNBPScanDateIDX", columnNames={"ScanDate"})
    })
public class FieldNotebookPage extends DisciplineMember
{
    protected Integer  fieldNotebookPageId;
    protected String   pageNumber;
    protected Calendar scanDate;
    protected String   description;
    
    protected FieldNotebookPageSet             pageSet;
    protected Set<CollectionObject>            collectionObjects;
    protected Set<FieldNotebookPageAttachment> attachments;

    /**
     * 
     */
    public FieldNotebookPage()
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

        fieldNotebookPageId = null;
        pageNumber          = null;
        scanDate            = null;
        description         = null;
        
        pageSet           = null;
        collectionObjects = new HashSet<CollectionObject>();
        attachments       = new TreeSet<FieldNotebookPageAttachment>();
    }

    /**
     * @param fieldNotebookPageId the fieldNotebookPageId to set
     */
    public void setFieldNotebookPageId(Integer fieldNotebookPageId)
    {
        this.fieldNotebookPageId = fieldNotebookPageId;
    }

    /**
     * @param pageNumber the pageNumber to set
     */
    public void setPageNumber(String pageNumber)
    {
        this.pageNumber = pageNumber;
    }

    /**
     * @param scanDate the scanDate to set
     */
    public void setScanDate(Calendar scanDate)
    {
        this.scanDate = scanDate;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @param pageSet the pageSet to set
     */
    public void setPageSet(FieldNotebookPageSet pageSet)
    {
        this.pageSet = pageSet;
    }

    /**
     * @param collectionObjects the collectionObjects to set
     */
    public void setCollectionObjects(Set<CollectionObject> collectionObjects)
    {
        this.collectionObjects = collectionObjects;
    }

    /**
     * @return the fieldNotebookPageId
     */
    @Id
    @GeneratedValue
    @Column(name = "FieldNotebookPageID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getFieldNotebookPageId()
    {
        return fieldNotebookPageId;
    }

    /**
     * @return the pageNumber
     */
    @Column(name = "PageNumber", unique = false, nullable = false, insertable = true, updatable = true, length = 16)
    public String getPageNumber()
    {
        return pageNumber;
    }

    /**
     * @return the scanDate
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "ScanDate", unique = false, nullable = false, insertable = true, updatable = true)
    public Calendar getScanDate()
    {
        return scanDate;
    }

    /**
     * @return the description
     */
    @Column(name = "Description", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getDescription()
    {
        return description;
    }

    @OneToMany(mappedBy = "fieldNotebookPage")
    @Cascade( {CascadeType.ALL} )
    @OrderBy("ordinal ASC")
    public Set<FieldNotebookPageAttachment> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(Set<FieldNotebookPageAttachment> attachments)
    {
        this.attachments = attachments;
    }

    /**
     * @return the pageSet
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "FieldNotebookPageSetID", unique = false, nullable = true, insertable = true, updatable = true)
    public FieldNotebookPageSet getPageSet()
    {
        return pageSet;
    }

    /**
     * @return the collectionObjects
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "fieldNotebookPage")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<CollectionObject> getCollectionObjects()
    {
        return collectionObjects;
    }

    //---------------------------------------------------------------------------
    // Overrides DataModelObjBase
    //---------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return fieldNotebookPageId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return FieldNotebookPage.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        return pageNumber != null ? pageNumber : super.getIdentityTitle();
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
        return 85;
    }

}
