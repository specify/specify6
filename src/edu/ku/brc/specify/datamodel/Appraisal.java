/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.datamodel;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
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
@Table(name = "appraisal")
@org.hibernate.annotations.Table(appliesTo="appraisal", indexes =
    {   @Index (name="AppraisalNumberIDX", columnNames={"AppraisalNumber"}),
        @Index (name="AppraisalDateIDX", columnNames={"AppraisalDate"})
    })
public class Appraisal extends DataModelObjBase
{
    protected Integer    appraisalId;
    protected Calendar   appraisalDate;
    protected String     appraisalNumber;
    protected BigDecimal appraisalValue;
    protected String     monetaryUnitType;
    protected String     notes;
    
    protected Set<CollectionObject> collectionObjects;
    protected Set<Accession>        accessions;
    
    
    /**
     * 
     */
    public Appraisal()
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        appraisalId       = null;
        appraisalDate     = null;
        appraisalNumber   = null;
        appraisalValue    = null;
        monetaryUnitType  = null;
        notes             = null;
        
        collectionObjects = new HashSet<CollectionObject>();
        accessions        = new HashSet<Accession>();

    }
    
    /**
     * @param appraisalId the appraisalId to set
     */
    public void setAppraisalId(Integer appraisalId)
    {
        this.appraisalId = appraisalId;
    }

    /**
     * @param appraisalDate the appraisalDate to set
     */
    public void setAppraisalDate(Calendar appraisalDate)
    {
        this.appraisalDate = appraisalDate;
    }

    /**
     * @param appraisalNumber the appraisalNumber to set
     */
    public void setAppraisalNumber(String appraisalNumber)
    {
        this.appraisalNumber = appraisalNumber;
    }

    /**
     * @param appraisalValue the appraisalValue to set
     */
    public void setAppraisalValue(BigDecimal appraisalValue)
    {
        this.appraisalValue = appraisalValue;
    }

    /**
     * @param monetaryUnitType the monetaryUnitType to set
     */
    public void setMonetaryUnitType(String monetaryUnitType)
    {
        this.monetaryUnitType = monetaryUnitType;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes)
    {
        this.notes = notes;
    }

    /**
     * @param collectionObjects the collectionObjects to set
     */
    public void setCollectionObjects(Set<CollectionObject> collectionObjects)
    {
        this.collectionObjects = collectionObjects;
    }

    /**
     * @param accessions the accessions to set
     */
    public void setAccessions(Set<Accession> accessions)
    {
        this.accessions = accessions;
    }

    /**
     * @return the appraisalId
     */
    @Id
    @GeneratedValue
    @Column(name = "AppraisalID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getAppraisalId()
    {
        return appraisalId;
    }

    /**
     * @return the appraisalDate
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "AppraisalDate", unique = false, nullable = false, insertable = true, updatable = true)
    public Calendar getAppraisalDate()
    {
        return appraisalDate;
    }

    /**
     * @return the appraisalNumber
     */
    @Column(name = "AppraisalNumber", unique = true, nullable = false, insertable = true, updatable = true, length = 64)
    public String getAppraisalNumber()
    {
        return appraisalNumber;
    }

    /**
     * @return the appraisalValue
     */
    @Column(name = "AppraisalValue", unique = false, nullable = true, insertable = true, updatable = true, precision = 12, scale = 10)
    public BigDecimal getAppraisalValue()
    {
        return appraisalValue;
    }

    /**
     * @return the monetaryUnitType
     */
    @Column(name = "MonetaryUnitType", unique = false, nullable = true, insertable = true, updatable = true, length = 8)
    public String getMonetaryUnitType()
    {
        return monetaryUnitType;
    }

    /**
     * @return the notes
     */
    @Lob
    @Column(name = "Notes", unique = false, nullable = true, insertable = true, updatable = true, length = 2048)
    public String getNotes()
    {
        return notes;
    }

    /**
     * @return the collectionObjects
     */
    @OneToMany(cascade = {javax.persistence.CascadeType.ALL}, mappedBy = "appraisal")
    public Set<CollectionObject> getCollectionObjects()
    {
        return collectionObjects;
    }

    /**
     * @return the accessions
     */
    @OneToMany(cascade = {javax.persistence.CascadeType.ALL}, mappedBy = "appraisal")
    public Set<Accession> getAccessions()
    {
        return accessions;
    }

    //---------------------------------------------------------------------------
    // Overrides DataModelObjBase
    //---------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return Appraisal.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return appraisalId;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        return StringUtils.isNotEmpty(appraisalNumber) ? appraisalNumber : super.getIdentityTitle();
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
        return 67;
    }
}
