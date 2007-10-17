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
import javax.persistence.Lob;
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
@Table(name = "treatmentevent")
@org.hibernate.annotations.Table(appliesTo="treatmentevent", indexes =
    {   @Index (name="TEDateCompletedIDX", columnNames={"DateCompleted"}),
        @Index (name="TEDateInitiatedIDX", columnNames={"DateInitiated"}),
        @Index (name="TEFieldNumberIDX", columnNames={"FieldNumber"})
    })
public class TreatmentEvent extends DataModelObjBase
{
    protected static DateWrapper scrDateFormat = null;

    protected Integer  treatmentEventId;
    protected Calendar dateInitiated;
    protected Calendar dateCompleted;
    protected String   type;
    protected String   location;
    protected String   fieldNumber;
    protected String   remarks;
    
    protected Accession        accession;
    protected CollectionObject collectionObject;
    
    /**
     * 
     */
    public TreatmentEvent()
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        treatmentEventId  = null;
        dateInitiated     = null;
        dateCompleted     = null;
        type              = null;
        location          = null;
        fieldNumber       = null;
        remarks           = null;
        accession         = null;
        collectionObject  = null;
    }

    /**
     * @param scrDateFormat the scrDateFormat to set
     */
    public static void setScrDateFormat(DateWrapper scrDateFormat)
    {
        TreatmentEvent.scrDateFormat = scrDateFormat;
    }

    /**
     * @param treatmentEventId the treatmentEventId to set
     */
    public void setTreatmentEventId(Integer treatmentEventId)
    {
        this.treatmentEventId = treatmentEventId;
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
     * @param type the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(String location)
    {
        this.location = location;
    }

    /**
     * @param fieldNumber the fieldNumber to set
     */
    public void setFieldNumber(String fieldNumber)
    {
        this.fieldNumber = fieldNumber;
    }

    /**
     * @param remarks the remarks to set
     */
    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
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
     * @return the treatmentEventId
     */
    @Id
    @GeneratedValue
    @Column(name = "TreatmentEventID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getTreatmentEventId()
    {
        return treatmentEventId;
    }

    /**
     * @return the dateInitiated
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DateInitiated", unique = false, nullable = false, insertable = true, updatable = true)
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
     * @return the type
     */
    @Column(name = "Type", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getType()
    {
        return type;
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
     * @return the fieldNumber
     */
    @Column(name = "FieldNumber", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getFieldNumber()
    {
        return fieldNumber;
    }

    /**
     * @return the remarks
     */
    @Lob
    @Column(name = "Remarks", unique = false, nullable = true, insertable = true, updatable = true, length = 2048)
    public String getRemarks()
    {
        return remarks;
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
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return TreatmentEvent.class;
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
        return 122;
    }

}
