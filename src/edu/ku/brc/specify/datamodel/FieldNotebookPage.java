/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
public class FieldNotebookPage extends DataModelObjBase
{
    protected Integer  fieldNotebookPageId;
    protected Short    pageNumber;
    protected Calendar scanDate;
    protected String   description;
    
    protected FieldNotebookPageSet  pageSet;
    protected Set<CollectionObject> collectionObjects;

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
        fieldNotebookPageId = null;
        pageNumber          = null;
        scanDate            = null;
        description         = null;
        
        pageSet           = null;
        collectionObjects = new HashSet<CollectionObject>();
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
    public void setPageNumber(Short pageNumber)
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
    @Column(name = "PageNumber", unique = false, nullable = false, insertable = true, updatable = true)
    public Short getPageNumber()
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
        return pageNumber != null ? Short.toString(pageNumber) : super.getIdentityTitle();
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
