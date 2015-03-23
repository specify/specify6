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
 * @code_status Beta
 *
 * Oct 16, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "treatmentevent")
@org.hibernate.annotations.Table(appliesTo="treatmentevent", indexes =
    {   @Index (name="TEDateReceivedIDX",        columnNames={"DateReceived"}),
        @Index (name="TEDateTreatmentStartedIDX", columnNames={"DateTreatmentStarted"}),
        @Index (name="TEFieldNumberIDX", columnNames={"FieldNumber"}),
        @Index (name="TETreatmentNumberIDX", columnNames={"TreatmentNumber"})
    })
public class TreatmentEvent extends DataModelObjBase
{
    protected static DateWrapper scrDateFormat = null;

    protected Integer  treatmentEventId;
    
    // Dates for Bubble
    protected Calendar dateReceived;
    protected Calendar dateCompleted;
    protected Calendar dateTreatmentStarted;
    protected Calendar dateTreatmentEnded;
    
    // Dates for Bug Room
    protected Calendar dateCleaned;
    protected Calendar dateBoxed;
    protected Calendar dateToIsolation;
    
    protected String   type;
    protected String   treatmentNumber;
    protected String   location;
    protected String   fieldNumber;
    protected String   remarks;
    
    protected Accession        accession;
    protected CollectionObject collectionObject;
    protected Division         division;
    
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
        super.init();
        
        treatmentEventId     = null;
        dateReceived         = null;
        dateCompleted        = null;
        dateTreatmentStarted = null;
        dateTreatmentEnded   = null;
        dateCleaned          = null;
        dateBoxed            = null;
        dateToIsolation      = null;
        type                 = null;
        treatmentNumber      = null;
        location             = null;
        fieldNumber          = null;
        remarks              = null;
        accession            = null;
        collectionObject     = null;
        division             = null;
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
     * @param dateReceived the dateReceived to set
     */
    public void setDateReceived(Calendar dateReceived)
    {
        this.dateReceived = dateReceived;
    }

    /**
     * @param dateCompleted the dateCompleted to set
     */
    public void setDateCompleted(Calendar dateCompleted)
    {
        this.dateCompleted = dateCompleted;
    }

    /**
     * @param dateTreatmentStarted the dateTreatmentStarted to set
     */
    public void setDateTreatmentStarted(Calendar dateTreatmentStarted)
    {
        this.dateTreatmentStarted = dateTreatmentStarted;
    }

    /**
     * @param dateTreatmentEnded the dateTreatmentEnded to set
     */
    public void setDateTreatmentEnded(Calendar dateTreatmentEnded)
    {
        this.dateTreatmentEnded = dateTreatmentEnded;
    }

    /**
     * @param dateCleaned the dateCleaned to set
     */
    public void setDateCleaned(Calendar dateCleaned)
    {
        this.dateCleaned = dateCleaned;
    }

    /**
     * @param dateBoxed the dateBoxed to set
     */
    public void setDateBoxed(Calendar dateBoxed)
    {
        this.dateBoxed = dateBoxed;
    }

    /**
     * @param dateToIsolation the dateToIsolation to set
     */
    public void setDateToIsolation(Calendar dateToIsolation)
    {
        this.dateToIsolation = dateToIsolation;
    }   
    
    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * @param storage the storage to set
     */
    public void setLocation(String location)
    {
        this.location = location;
    }

    /**
     * @param treatmentNumber the treatmentNumber to set
     */
    public void setTreatmentNumber(String treatmentNumber)
    {
        this.treatmentNumber = treatmentNumber;
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
     * @param division the division to set
     */
    public void setDivision(Division division)
    {
        this.division = division;
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
     * @return the dateReceived
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DateReceived", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDateReceived()
    {
        return dateReceived;
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
     * @return the dateTreatmentStarted
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DateTreatmentStarted", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDateTreatmentStarted()
    {
        return dateTreatmentStarted;
    }

    /**
     * @return the dateTreatmentEnded
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DateTreatmentEnded", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDateTreatmentEnded()
    {
        return dateTreatmentEnded;
    }

    /**
     * @return the dateCleaned
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DateCleaned", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDateCleaned()
    {
        return dateCleaned;
    }

    /**
     * @return the dateBoxed
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DateBoxed", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDateBoxed()
    {
        return dateBoxed;
    }

    /**
     * @return the dateToIsolation
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DateToIsolation", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDateToIsolation()
    {
        return dateToIsolation;
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
     * @return the storage
     */
    @Column(name = "Storage", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getLocation()
    {
        return location;
    }

    /**
     * @return the treatmentNumber
     */
    @Column(name = "TreatmentNumber", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getTreatmentNumber()
    {
        return treatmentNumber;
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
    

    /**
     * @return the division
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "DivisionID", unique = false, nullable = true, insertable = true, updatable = true)
    public Division getDivision()
    {
        return division;
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
        // Throws exception when inlined
        Integer tblId = accession != null ? Accession.getClassTableId() : null;
        tblId = tblId != null ? tblId : collectionObject != null ? CollectionObject.getClassTableId() : null;
        tblId = tblId != null ? tblId : division != null ? Division.getClassTableId() : null;
        return tblId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return accession != null ? accession.getId() : collectionObject != null ? collectionObject.getId() : division != null ? division.getId() : null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return treatmentEventId;
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
        if (dateReceived != null)
        {
            return scrDateFormat.format(dateReceived);
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
