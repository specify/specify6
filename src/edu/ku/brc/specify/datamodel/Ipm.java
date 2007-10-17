/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.datamodel;

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.ui.DateWrapper;

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
@Table(name = "ipm")
@org.hibernate.annotations.Table(appliesTo="ipm", indexes =
    {   @Index (name="IPMDateCompletedIDX", columnNames={"DateCompleted"}),
        @Index (name="IPMDateInitiatedIDX", columnNames={"DateInitiated"})
    })
public class Ipm extends DataModelObjBase
{
    protected static DateWrapper scrDateFormat = null;

    protected Integer  ipmId;
    protected Calendar dateInitiated;
    protected Calendar dateCompleted;
    protected Calendar dateEnteredIsolation;
    protected Calendar dateLeftIsolation;
    protected String   locationInIsolation;
    
    protected Accession        accession;
    protected CollectionObject collectionObject;
    
    /**
     * 
     */
    public Ipm()
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        ipmId                = null;
        dateInitiated        = null;
        dateCompleted        = null;
        dateEnteredIsolation = null;
        dateLeftIsolation    = null;
        locationInIsolation  = null;
        accession            = null;
        collectionObject     = null;

    }

    /**
     * @param scrDateFormat the scrDateFormat to set
     */
    public static void setScrDateFormat(DateWrapper scrDateFormat)
    {
        Ipm.scrDateFormat = scrDateFormat;
    }

    /**
     * @param ipmId the ipmId to set
     */
    public void setIpmId(Integer ipmId)
    {
        this.ipmId = ipmId;
    }

    /**
     * @param dateInitiated the dateInitiated to set
     */
    public void setDateInitiated(Calendar dateInitiated)
    {
        this.dateInitiated = dateInitiated;
    }

    /**
     * @param dateCompleted the dateCompleted to set
     */
    public void setDateCompleted(Calendar dateCompleted)
    {
        this.dateCompleted = dateCompleted;
    }

    /**
     * @param dateEnteredIsolation the dateEnteredIsolation to set
     */
    public void setDateEnteredIsolation(Calendar dateEnteredIsolation)
    {
        this.dateEnteredIsolation = dateEnteredIsolation;
    }

    /**
     * @param dateLeftIsolation the dateLeftIsolation to set
     */
    public void setDateLeftIsolation(Calendar dateLeftIsolation)
    {
        this.dateLeftIsolation = dateLeftIsolation;
    }

    /**
     * @param locationInIsolation the locationInIsolation to set
     */
    public void setLocationInIsolation(String locationInIsolation)
    {
        this.locationInIsolation = locationInIsolation;
    }

    /**
     * @param accession the accession to set
     */
    public void setAccession(Accession accession)
    {
        this.accession = accession;
    }

    /**
     * @param collectionObject the collectionObject to set
     */
    public void setCollectionObject(CollectionObject collectionObject)
    {
        this.collectionObject = collectionObject;
    }

    /**
     * @return the scrDateFormat
     */
    public static DateWrapper getScrDateFormat()
    {
        return scrDateFormat;
    }

    /**
     * @return the ipmId
     */
    @Id
    @GeneratedValue
    @Column(name = "IpmID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getIpmId()
    {
        return ipmId;
    }

    /**
     * @return the dateInitiated
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DateInitiated", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDateInitiated()
    {
        return dateInitiated;
    }

    /**
     * @return the dateCompleted
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DateCompleted", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDateCompleted()
    {
        return dateCompleted;
    }

    /**
     * @return the dateEnteredIsolation
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DateEnteredIsolation", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDateEnteredIsolation()
    {
        return dateEnteredIsolation;
    }

    /**
     * @return the dateLeftIsolation
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DateLeftIsolation", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDateLeftIsolation()
    {
        return dateLeftIsolation;
    }

    /**
     * @return the locationInIsolation
     */
    @Column(name = "LocationInIsolation", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getLocationInIsolation()
    {
        return locationInIsolation;
    }

    /**
     * @return the accession
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AccessionID", unique = false, nullable = true, insertable = true, updatable = true)
    public Accession getAccession()
    {
        return accession;
    }

    /**
     * @return the collectionObject
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CollectionObjectID", unique = false, nullable = true, insertable = true, updatable = true)
    public CollectionObject getCollectionObject()
    {
        return collectionObject;
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
        return ipmId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return Ipm.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        if (scrDateFormat == null)
        {
            scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");
        }

        // Not sure this is right
        if (dateCompleted != null)
        {
            return scrDateFormat.format(dateCompleted);
        }
        if (dateInitiated != null)
        {
            return scrDateFormat.format(dateInitiated);
        }
        return super.getIdentityTitle();
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
        return 86;
    }

}
