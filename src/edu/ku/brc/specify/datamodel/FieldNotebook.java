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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
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
@Table(name = "fieldnotebook")
@org.hibernate.annotations.Table(appliesTo="fieldnotebook", indexes =
    {   @Index (name="FNBNameIDX", columnNames={"Name"}),
        @Index (name="FNBStartDateIDX", columnNames={"StartDate"}),
        @Index (name="FNBEndDateIDX", columnNames={"EndDate"})
    })
public class FieldNotebook extends DataModelObjBase
{
    protected Integer    fieldNotebookId;
    protected String     name;
    protected Calendar   startDate;
    protected Calendar   endDate;
    protected String     location;      // physical location of notebook
    protected String     description;
    
    protected Collection                collection;
    protected Agent                     ownerAgent;
    protected Set<FieldNotebookPageSet> pageSets;
    
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
        fieldNotebookId = null;
        name            = null;
        startDate       = null;
        endDate         = null;
        location        = null;
        description     = null;
        collection      = null;
        ownerAgent      = null;
        pageSets        = new HashSet<FieldNotebookPageSet>();
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
     * @param location the location to set
     */
    public void setLocation(String location)
    {
        this.location = location;
    }

    /**
     * @param collectionType the collectionType to set
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
     * @return the location
     */
    @Column(name = "Location", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
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
     * @return the collectionType
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
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<FieldNotebookPageSet> getPageSets()
    {
        return pageSets;
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
    
    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 83;
    }

}
