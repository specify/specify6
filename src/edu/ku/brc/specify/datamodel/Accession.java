/* Copyright (C) 2020, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;




/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "accession")
@org.hibernate.annotations.Table(appliesTo="accession", indexes =
    {   @Index (name="AccessionNumberIDX", columnNames={"AccessionNumber"}),
        @Index (name="AccessionDateIDX", columnNames={"DateAccessioned"})
    })
@SuppressWarnings("serial")
public class Accession extends DataModelObjBase implements java.io.Serializable, AttachmentOwnerIFace<AccessionAttachment>, OneToManyProviderIFace {
    protected static final Logger log = Logger.getLogger(Accession.class);

    // Fields
    protected Integer                     accessionId;
    protected String                      type;
    protected String                      status;
    protected String                      accessionNumber;
    protected String                      verbatimDate;
    protected Calendar                    dateAccessioned;
    protected Calendar                    dateReceived;
    protected Calendar                    dateAcknowledged;
    protected String                      accessionCondition;
    protected BigDecimal                  totalValue;
    
	protected String text1;
	protected String text2;
	protected String text3;
    protected String text4;
    protected String text5;
	protected Float number1;
	protected Float number2;
	protected String remarks;
	protected Boolean yesNo1;
	protected Boolean yesNo2;
	protected Integer integer1;
	protected Integer integer2;
	protected Integer integer3;
    
    protected Division                    division;
    protected AddressOfRecord             addressOfRecord;
    protected RepositoryAgreement         repositoryAgreement;
    protected Set<CollectionObject>       collectionObjects;
    protected Set<AccessionAuthorization> accessionAuthorizations;
    protected Set<AccessionAgent>         accessionAgents;
    protected Set<AccessionAttachment>    accessionAttachments;
    protected Set<Appraisal>              appraisals;
    protected Set<TreatmentEvent>         treatmentEvents;
    protected Set<AccessionCitation> accessionCitations;

    // Constructors

    /** default constructor */
    public Accession()
    {
        // do nothing
    }

    /** constructor with id */
    public Accession(Integer accessionId) 
    {
        this.accessionId = accessionId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        accessionId = null;
        type = null;
        status = null;
        accessionNumber = null;
        verbatimDate = null;
        dateAccessioned = null;
        dateReceived = null;
        accessionCondition = null;
        totalValue         = null;
        text1 = null;
        text2 = null;
        text3 = null;
        text4 = null;
        text5 = null;
        number1 = null;
        number2 = null;
        remarks = null;
        yesNo1 = null;
        yesNo2 = null;
        integer1 = null;
        integer2 = null;
        integer3 = null;
        division                = null;
        addressOfRecord         = null;
        collectionObjects       = new HashSet<CollectionObject>();
        accessionAuthorizations = new HashSet<AccessionAuthorization>();
        accessionAgents         = new HashSet<AccessionAgent>();
        repositoryAgreement     = null;
        accessionAttachments    = new HashSet<AccessionAttachment>();
        appraisals              = new HashSet<Appraisal>();
        treatmentEvents         = new HashSet<TreatmentEvent>();
        accessionCitations = new HashSet<>();
    }
    // End Initializer

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Accession obj = (Accession)super.clone();

        obj.accessionId = null;

        obj.collectionObjects       = new HashSet<CollectionObject>();
        obj.accessionAuthorizations = new HashSet<AccessionAuthorization>();
        obj.accessionAgents         = new HashSet<AccessionAgent>();
        obj.accessionAttachments    = new HashSet<AccessionAttachment>();
        obj.appraisals              = new HashSet<Appraisal>();
        obj.treatmentEvents         = new HashSet<TreatmentEvent>();
        obj.accessionCitations = new HashSet<>();

        for (AccessionAuthorization a : this.accessionAuthorizations) {
            AccessionAuthorization c = (AccessionAuthorization)a.clone();
            obj.accessionAuthorizations.add(c);
            c.setAccession(obj);
        }
        for (AccessionAgent a : this.accessionAgents) {
            AccessionAgent c = (AccessionAgent)a.clone();
            obj.accessionAgents.add(c);
            c.setAccession(obj);
        }
        for (AccessionAttachment a : this.accessionAttachments) {
            AccessionAttachment c = (AccessionAttachment)a.clone();
            obj.accessionAttachments.add(c);
            c.setAccession(obj);
        }
        for (Appraisal a : this.appraisals) {
            Appraisal c = (Appraisal)a.clone();
            obj.appraisals.add(c);
            c.setAccession(obj);
        }
        for (TreatmentEvent a : this.treatmentEvents) {
            TreatmentEvent c = (TreatmentEvent)a.clone();
            obj.treatmentEvents.add(c);
            c.setAccession(obj);
        }
        for (AccessionCitation a : this.accessionCitations) {
            AccessionCitation c = (AccessionCitation)a.clone();
            obj.accessionCitations.add(c);
            c.setAccession(obj);
        }

        return obj;
    }

    /**
     *
     * @param originalObj
     * @param deep  if true then copy and clone children
     * @param session
     * @return
     */
    @Override
    public boolean initializeClone(DataModelObjBase originalObj, boolean deep, DataProviderSessionIFace session) {
        if (deep) {
            log.error(getClass().getName() + ": initializeClone is not implemented for deep = true.");
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#forceLoad()
     */
    @Override
    public void forceLoad()
    {
        accessionAttachments.size();
        accessionAuthorizations.size();
        accessionAgents.size();
        appraisals.size();
        treatmentEvents.size();
    }

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "AccessionID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getAccessionId() {
        return this.accessionId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.accessionId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Accession.class;
    }

    public void setAccessionId(Integer accessionId) {
        this.accessionId = accessionId;
    }

    /**
     *      * Source of Accession, e.g. 'Collecting', 'Gift',  'Bequest' ...
     */
    @Column(name = "Type", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     *      * Status of Accession, e.g. 'In process', 'Complete' ...
     */
    @Column(name = "Status", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    /**
     *
     */
    @OneToMany(cascade = { javax.persistence.CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "accession")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<AccessionCitation> getAccessionCitations() {
        return this.accessionCitations;
    }

    public void setAccessionCitations(Set<AccessionCitation> accessionCitations) {
        this.accessionCitations = accessionCitations;
    }

    /**
     * A user-visible identifier of the Accession. Typically an integer, but may include alphanumeric characters as prefix, suffix, and separators
     */
    @Column(name = "AccessionNumber", unique = false, nullable = false, insertable = true, updatable = true, length = 60)
    public String getAccessionNumber() {
        return this.accessionNumber;
    }

    public void setAccessionNumber(final String accessionNumber) 
    {
        firePropertyChange("accessionNumber", this.accessionNumber, accessionNumber);
        this.accessionNumber = accessionNumber;
    }

    /**
     * @return the dateAcknowledged
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DateAcknowledged", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDateAcknowledged()
    {
        return dateAcknowledged;
    }

    /**
     * @param dateAcknowledged the dateAcknowledged to set
     */
    public void setDateAcknowledged(Calendar dateAcknowledged)
    {
        this.dateAcknowledged = dateAcknowledged;
    }

    /**
     * @return the condition
     */
    @Column(name = "AccessionCondition", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getAccessionCondition()
    {
        return accessionCondition;
    }

    /**
     * @param condition the condition to set
     */
    public void setAccessionCondition(String accessionCondition)
    {
        this.accessionCondition = accessionCondition;
    }

    /**
     * @return the totalValue
     */
    @Column(name = "TotalValue", unique = false, nullable = true, insertable = true, updatable = true, precision = 12, scale = 2)
    public BigDecimal getTotalValue()
    {
        return totalValue;
    }

    /**
     * @param totalValue the totalValue to set
     */
    public void setTotalValue(BigDecimal totalValue)
    {
        this.totalValue = totalValue;
    }

    /**
     * accomodates historical accessions.
     */
    @Column(name = "VerbatimDate", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getVerbatimDate() {
        return this.verbatimDate;
    }

    public void setVerbatimDate(String verbatimDate) {
        this.verbatimDate = verbatimDate;
    }

    /**
     *      * Date of Accession
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DateAccessioned", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDateAccessioned() {
        return this.dateAccessioned;
    }

    public void setDateAccessioned(Calendar dateAccessioned) {
        this.dateAccessioned = dateAccessioned;
    }

    /**
     *      * Date material was received
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DateReceived", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDateReceived() {
        return this.dateReceived;
    }

    public void setDateReceived(Calendar dateReceived) {
        this.dateReceived = dateReceived;
    }

    /**
     *      * User definable
     */
    @Lob
    @Column(name = "Text1", length = 65535)
    public String getText1() {
        return this.text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    @Lob
    @Column(name = "Text2", length = 65535)
    public String getText2() {
        return this.text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      * User definable
     */
    @Lob
    @Column(name = "Text4", length = 65535)
    public String getText4() {
        return this.text4;
    }

    public void setText4(String text4) {
        this.text4 = text4;
    }

    /**
     *      * User definable
     */
    @Lob
    @Column(name = "Text5", length = 65535)
    public String getText5() {
        return this.text5;
    }

    public void setText5(String text5) {
        this.text5 = text5;
    }

    /**
     *      * User definable
     */
    @Lob
    @Column(name = "Text3", length = 65535)
    public String getText3() {
        return this.text3;
    }

    public void setText3(String text3) {
        this.text3 = text3;
    }


    /**
	 * @return the integer1
	 */
    @Column(name = "Integer1", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getInteger1() {
		return integer1;
	}

	/**
	 * @param integer1 the integer1 to set
	 */
	public void setInteger1(Integer integer1) {
		this.integer1 = integer1;
	}

	/**
	 * @return the integer2
	 */
    @Column(name = "Integer2", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getInteger2() {
		return integer2;
	}

	/**
	 * @param integer2 the integer2 to set
	 */
	public void setInteger2(Integer integer2) {
		this.integer2 = integer2;
	}

	/**
	 * @return the integer3
	 */
    @Column(name = "Integer3", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getInteger3() {
		return integer3;
	}

	/**
	 * @param integer3 the integer3 to set
	 */
	public void setInteger3(Integer integer3) {
		this.integer3 = integer3;
	}

	/**
     *      * User definable
     */
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber1() {
        return this.number1;
    }

    public void setNumber1(Float number1) {
        this.number1 = number1;
    }

    /**
     *      * User definable
     */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true)
    public Float getNumber2() {
        return this.number2;
    }

    public void setNumber2(Float number2) {
        this.number2 = number2;
    }

    /**
     *      * Comments
     */
    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks() {
        return this.remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *      * User definable
     */
    @Column(name="YesNo1",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo1() {
        return this.yesNo1;
    }

    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      * User definable
     */
    @Column(name="YesNo2",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo2() {
        return this.yesNo2;
    }

    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }
    
    @OneToMany(mappedBy = "accession")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @OrderBy("ordinal ASC")
    public Set<AccessionAttachment> getAccessionAttachments()
    {
        return accessionAttachments;
    }

    public void setAccessionAttachments(Set<AccessionAttachment> accessionAttachments)
    {
        this.accessionAttachments = accessionAttachments;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "accession")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<CollectionObject> getCollectionObjects() {
        return this.collectionObjects;
    }

    public void setCollectionObjects(Set<CollectionObject> collectionObjects) {
        this.collectionObjects = collectionObjects;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "accession")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<AccessionAuthorization> getAccessionAuthorizations() {
        return this.accessionAuthorizations;
    }

    public void setAccessionAuthorizations(Set<AccessionAuthorization> accessionAuthorizations) {
        this.accessionAuthorizations = accessionAuthorizations;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "accession")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<AccessionAgent> getAccessionAgents() {
        return this.accessionAgents;
    }

    public void setAccessionAgents(Set<AccessionAgent> accessionAgents) {
        this.accessionAgents = accessionAgents;
    }


    /**
     * RepositoryAgreement for this Accession
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "RepositoryAgreementID", unique = false, nullable = true, insertable = true, updatable = true)
    public RepositoryAgreement getRepositoryAgreement() {
        return this.repositoryAgreement;
    }
    
    public void setRepositoryAgreement(RepositoryAgreement repositoryAgreement) {
        this.repositoryAgreement = repositoryAgreement;
    }

    /**
     * @return the appraisal
     */
    @OneToMany(cascade = {javax.persistence.CascadeType.ALL}, mappedBy = "accession")
    public Set<Appraisal> getAppraisals()
    {
        return appraisals;
    }

    /**
     * @param appraisal the appraisal to set
     */
    public void setAppraisals(Set<Appraisal> appraisals)
    {
        this.appraisals = appraisals;
    }


    /**
     * @return the treatmentEvents
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "accession")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<TreatmentEvent> getTreatmentEvents()
    {
        return treatmentEvents;
    }

    /**
     * @param treatmentEvents the treatmentEvents to set
     */
    public void setTreatmentEvents(Set<TreatmentEvent> treatmentEvents)
    {
        this.treatmentEvents = treatmentEvents;
    }

    /**
     * @return the addressOfRecord
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AddressOfRecordID", unique = false, nullable = true, insertable = true, updatable = true)
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public AddressOfRecord getAddressOfRecord()
    {
        return addressOfRecord;
    }

    /**
     * @param addressOfRecord the addressOfRecord to set
     */
    public void setAddressOfRecord(AddressOfRecord addressOfRecord)
    {
        this.addressOfRecord = addressOfRecord;
    }

    /**
     * @return the division
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "DivisionID", unique = false, nullable = false, insertable = true, updatable = true)
    public Division getDivision()
    {
        return division;
    }

    /**
     * @param division the division to set
     */
    public void setDivision(Division division)
    {
        this.division = division;
    }
    
    //---------------------------------------------------------------------------
    // Overrides DataModelObjBase
    //---------------------------------------------------------------------------


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        return accessionNumber != null ? accessionNumber : super.getIdentityTitle();
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

    @Override
    @Transient
    public Set<? extends PreparationHolderIFace> getPreparationHolders() {
        return null;
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
        return 7;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentReferences()
     */
    @Override
    @Transient
    public Set<AccessionAttachment> getAttachmentReferences()
    {
        return accessionAttachments;
    }
}
