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
import javax.persistence.OrderBy;
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
@SuppressWarnings("serial")
public class TreatmentEvent extends DataModelObjBase implements java.io.Serializable, AttachmentOwnerIFace<TreatmentEventAttachment> 
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
    
    protected String text1;
    protected String text2;
    protected String text3;
    protected String text4;
    protected String text5;
    
    protected Integer              number1;
    protected Integer              number2;
    protected Float				   number3;
    protected Float                number4;
    protected Float                number5;
    
    protected Boolean              yesNo1;
    protected Boolean              yesNo2;
    protected Boolean              yesNo3;

    protected Agent	performedBy;
    protected Agent authorizedBy;
    
    protected Accession        accession;
    protected CollectionObject collectionObject;
    protected Division         division;
 
    protected Set<TreatmentEventAttachment>    treatmentEventAttachments;

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
        number1						  = null;
        number2                       = null;
        number3                       = null;
        number4                       = null;
        number5                       = null;
        text1                         = null;
        text2                         = null;
        text3                         = null;
        text4                         = null;
        text5                         = null;
        yesNo1                        = null;
        yesNo2                        = null;
        yesNo3                        = null;

        performedBy = null;
        authorizedBy = null;
        
        accession            = null;
        collectionObject     = null;
        division             = null;

        treatmentEventAttachments    = new HashSet<TreatmentEventAttachment>();
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

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "PerformedByID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getPerformedBy() 
    {
        return this.performedBy;
    }
    
    public void setPerformedBy(Agent performedBy) 
    {
        this.performedBy = performedBy;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AuthorizedByID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAuthorizedBy() 
    {
        return this.authorizedBy;
    }
    
    public void setAuthorizedBy(Agent authorizedBy) 
    {
        this.authorizedBy = authorizedBy;
    }

    @Lob
    @Column(name="Text1", length = 65535)
    public String getText1()
    {
        return text1;
    }

    public void setText1(String text1)
    {
        this.text1 = text1;
    }

    @Lob
    @Column(name="Text2", length = 65535)
    public String getText2()
    {
        return text2;
    }

    public void setText2(String text2)
    {
        this.text2 = text2;
    }

    
    /**
	 * @return the text3
	 */
    @Lob
    @Column(name = "Text3", length = 65535)
	public String getText3() {
		return text3;
	}

	/**
	 * @param text3 the text3 to set
	 */
	public void setText3(String text3) {
		this.text3 = text3;
	}

	/**
	 * @return the text4
	 */
    @Lob
    @Column(name = "Text4", length = 65535)
	public String getText4() {
		return text4;
	}

	/**
	 * @param text4 the text4 to set
	 */
	public void setText4(String text4) {
		this.text4 = text4;
	}

	/**
	 * @return the text5
	 */
    @Lob
    @Column(name = "Text5", length = 65535)
	public String getText5() {
		return text5;
	}

	/**
	 * @param text5 the text5 to set
	 */
	public void setText5(String text5) {
		this.text5 = text5;
	}

	@Column(name="Number1")
    public Integer getNumber1()
    {
        return number1;
    }

    public void setNumber1(Integer number1)
    {
        this.number1 = number1;
    }

    @Column(name="Number2")
    public Integer getNumber2()
    {
        return number2;
    }

    public void setNumber2(Integer number2)
    {
        this.number2 = number2;
    }

	/**
	 * @return the number3
	 */
    @Column(name = "Number3", unique = false, nullable = true, insertable = true, updatable = true)
	public Float getNumber3() {
		return number3;
	}

	/**
	 * @param number3 the number3 to set
	 */
	public void setNumber3(Float number3) {
		this.number3 = number3;
	}

	/**
	 * @return the number4
	 */
    @Column(name = "Number4", unique = false, nullable = true, insertable = true, updatable = true)
	public Float getNumber4() {
		return number4;
	}

	/**
	 * @param number4 the number4 to set
	 */
	public void setNumber4(Float number4) {
		this.number4 = number4;
	}

	/**
	 * @return the number5
	 */
    @Column(name = "Number5", unique = false, nullable = true, insertable = true, updatable = true)
	public Float getNumber5() {
		return number5;
	}

	/**
	 * @param number5 the number5 to set
	 */
	public void setNumber5(Float number5) {
		this.number5 = number5;
	}

	/**
	 * @return the yesNo1
	 */
    @Column(name = "YesNo1", unique = false, nullable = true, insertable = true, updatable = true)
	public Boolean getYesNo1() {
		return yesNo1;
	}

	/**
	 * @param yesNo1 the yesNo1 to set
	 */
	public void setYesNo1(Boolean yesNo1) {
		this.yesNo1 = yesNo1;
	}

	/**
	 * @return the yesNo2
	 */
    @Column(name = "YesNo2", unique = false, nullable = true, insertable = true, updatable = true)
	public Boolean getYesNo2() {
		return yesNo2;
	}

	/**
	 * @param yesNo2 the yesNo2 to set
	 */
	public void setYesNo2(Boolean yesNo2) {
		this.yesNo2 = yesNo2;
	}

	/**
	 * @return the yesNo3
	 */
    @Column(name = "YesNo3", unique = false, nullable = true, insertable = true, updatable = true)
	public Boolean getYesNo3() {
		return yesNo3;
	}

	/**
	 * @param yesNo3 the yesNo3 to set
	 */
	public void setYesNo3(Boolean yesNo3) {
		this.yesNo3 = yesNo3;
	}

    @OneToMany(mappedBy = "treatmentEvent")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @OrderBy("ordinal ASC")
    public Set<TreatmentEventAttachment> getTreatmentEventAttachments()
    {
        return treatmentEventAttachments;
    }

    public void setTreatmentEventAttachments(Set<TreatmentEventAttachment> treatmentEventAttachments)
    {
        this.treatmentEventAttachments = treatmentEventAttachments;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentReferences()
     */
    @Override
    @Transient
    public Set<TreatmentEventAttachment> getAttachmentReferences()
    {
        return treatmentEventAttachments;
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
