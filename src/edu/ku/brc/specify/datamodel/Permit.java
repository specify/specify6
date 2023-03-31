/* Copyright (C) 2023, Specify Collections Consortium
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

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.math.BigDecimal;

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

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.specify.conversion.BasicSQLUtils;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "permit")
@org.hibernate.annotations.Table(appliesTo="permit", indexes =
    {   @Index (name="PermitNumberIDX", columnNames={"PermitNumber"}),
        @Index (name="IssuedDateIDX", columnNames={"IssuedDate"})
    })
@SuppressWarnings("serial")
public class Permit extends DataModelObjBase implements AttachmentOwnerIFace<PermitAttachment>, java.io.Serializable {

    // Fields

    protected Integer                     permitId;
    protected String                      permitNumber;
    protected String                      type;
    protected Calendar                    issuedDate;
    protected Calendar                    startDate;
    protected Calendar                    endDate;
    protected Calendar                    renewalDate;
    protected String                      remarks;
    protected String                      text1;
    protected String                      text2;
    protected BigDecimal                       number1;
    protected BigDecimal                       number2;
    protected Boolean                     yesNo1;
    protected Boolean                     yesNo2;
    protected Set<AccessionAuthorization> accessionAuthorizations;
    protected Set<CollectingEventAuthorization> collectingEventAuthorizations;
    protected Set<CollectingTripAuthorization> collectingTripAuthorizations;
    protected Agent                       issuedTo;
    protected Agent                       issuedBy;
    protected Set<PermitAttachment>       permitAttachments;
    protected Institution                 institution;
    protected String                      status;
    protected String                      statusQualifier;
    protected Boolean                     isRequired;
    protected Boolean                     isAvailable;
    protected String                      copyright;
    protected String                      reservedText3;
    protected String                      reservedText4;
    protected Integer                     reservedInteger1;
    protected Integer                     reservedInteger2;
    protected String                      permitText;	

    // Constructors

    /** default constructor */
    public Permit() {
        //
        // do nothing
    }

    /** constructor with id */
    public Permit(Integer permitId) {
        this.permitId = permitId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        permitId = null;
        permitNumber = null;
        type = null;
        issuedDate = null;
        startDate = null;
        endDate = null;
        renewalDate = null;
        remarks = null;
        text1 = null;
        text2 = null;
        number1 = null;
        number2 = null;
        yesNo1 = null;
        yesNo2 = null;
        accessionAuthorizations = new HashSet<AccessionAuthorization>();
        collectingEventAuthorizations = new HashSet<CollectingEventAuthorization>();
        collectingTripAuthorizations = new HashSet<CollectingTripAuthorization>();
        issuedTo = null;
        issuedBy = null;
        permitAttachments = new HashSet<PermitAttachment>();
        institution       = AppContextMgr.getInstance().getClassObject(Institution.class);
        status = null;
        statusQualifier = null;
        isRequired = null;
        isAvailable = null;
        copyright = null;
        reservedText3 = null;
        reservedText4 = null;
        reservedInteger1 = null;
        reservedInteger2 = null;
        permitText = null;
    }
    // End Initializer

    // Property accessors
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#forceLoad()
     */
    @Override
    public void forceLoad()
    {
        permitAttachments.size();
    }

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "PermitID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getPermitId() {
        return this.permitId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.permitId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Permit.class;
    }

    public void setPermitId(Integer permitId) {
        this.permitId = permitId;
    }

    /**
     *      * Identifier for the permit
     */
    @Column(name = "PermitNumber", unique = false, nullable = false, insertable = true, updatable = true, length = 50)
    public String getPermitNumber() {
        return this.permitNumber;
    }

    public void setPermitNumber(String permitNumber) {
        this.permitNumber = permitNumber;
    }

    /**
     *      * Permit category - 'CITES', 'Migratory Bird Treaty Act', ...
     */
    @Column(name = "Type", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     *      * Date permit was issued
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "IssuedDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getIssuedDate() {
        return this.issuedDate;
    }

    public void setIssuedDate(Calendar issuedDate) {
        this.issuedDate = issuedDate;
    }

    /**
     *      * Date permit becomes effective
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "StartDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getStartDate() {
        return this.startDate;
    }

    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    /**
     *      * Date permit expires
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "EndDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getEndDate() {
        return this.endDate;
    }

    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    /**
     *      * Date of renewal
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "RenewalDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getRenewalDate() {
        return this.renewalDate;
    }

    public void setRenewalDate(Calendar renewalDate) {
        this.renewalDate = renewalDate;
    }

    /**
     *
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
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true, length = 24, precision = 20, scale = 10)
    public BigDecimal getNumber1() {
        return this.number1;
    }

    public void setNumber1(BigDecimal number1) {
        this.number1 = number1;
    }

    /**
     *      * User definable
     */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true, length = 24, precision = 20, scale = 10)
    public BigDecimal getNumber2() {
        return this.number2;
    }

    public void setNumber2(BigDecimal number2) {
        this.number2 = number2;
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

    
    /**
	 * @return the status
	 */
    @Column(name = "Status", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the statusQualifier
	 */
    @Column(name = "StatusQualifier", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
	public String getStatusQualifier() {
		return statusQualifier;
	}

	/**
	 * @param statusQualifier the statusQualifier to set
	 */
	public void setStatusQualifier(String statusQualifier) {
		this.statusQualifier = statusQualifier;
	}

	/**
	 * @return the isRequired
	 */
    @Column(name="IsRequired",unique=false,nullable=true,updatable=true,insertable=true)
	public Boolean getIsRequired() {
		return isRequired;
	}

	/**
	 * @param isRequired the isRequired to set
	 */
	public void setIsRequired(Boolean isRequired) {
		this.isRequired = isRequired;
	}

	/**
	 * @return the isAvailable
	 */
    @Column(name="IsAvailable",unique=false,nullable=true,updatable=true,insertable=true)
	public Boolean getIsAvailable() {
		return isAvailable;
	}

	/**
	 * @param isAvailable the isAvailable to set
	 */
	public void setIsAvailable(Boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

	/**
	 * @return the copyright
	 */
    @Column(name = "Copyright", unique = false, nullable = true, insertable = true, updatable = true, length = 256)
	public String getCopyright() {
		return copyright;
	}

	/**
	 * @param copyright the copyright to set
	 */
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	/**
	 * @return the reservedText3
	 */
    @Column(name = "ReservedText3", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
	public String getReservedText3() {
		return reservedText3;
	}

	/**
	 * @param reservedText3 the reservedText3 to set
	 */
	public void setReservedText3(String reservedText3) {
		this.reservedText3 = reservedText3;
	}

	/**
	 * @return the reservedText4
	 */
    @Column(name = "ReservedText4", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
	public String getReservedText4() {
		return reservedText4;
	}

	/**
	 * @param reservedText4 the reservedText4 to set
	 */
	public void setReservedText4(String reservedText4) {
		this.reservedText4 = reservedText4;
	}

	/**
	 * @return the reservedInteger1
	 */
    @Column(name = "ReservedInteger1", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getReservedInteger1() {
		return reservedInteger1;
	}

	/**
	 * @param reservedInteger1 the reservedInteger1 to set
	 */
	public void setReservedInteger1(Integer reservedInteger1) {
		this.reservedInteger1 = reservedInteger1;
	}

	/**
	 * @return the reservedInteger2
	 */
    @Column(name = "ReservedInteger2", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getReservedInteger2() {
		return reservedInteger2;
	}

	/**
	 * @param reservedInteger2 the reservedInteger2 to set
	 */
	public void setReservedInteger2(Integer reservedInteger2) {
		this.reservedInteger2 = reservedInteger2;
	}

	/**
    *
    */
   @Lob
   @Column(name = "PermitText", length = 4096)
   public String getPermitText() {
       return this.permitText;
   }

   public void setPermitText(String permitText) {
       this.permitText = permitText;
   }

	/**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "permit")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<AccessionAuthorization> getAccessionAuthorizations() {

        return this.accessionAuthorizations;
    }

    public void setAccessionAuthorizations(Set<AccessionAuthorization> accessionAuthorizations) {
        this.accessionAuthorizations = accessionAuthorizations;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "permit")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<CollectingEventAuthorization> getCollectingEventAuthorizations() {

        return this.collectingEventAuthorizations;
    }

    public void setCollectingEventAuthorizations(Set<CollectingEventAuthorization> collectingEventAuthorizations) {
        this.collectingEventAuthorizations = collectingEventAuthorizations;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "permit")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<CollectingTripAuthorization> getCollectingTripAuthorizations() {

        return this.collectingTripAuthorizations;
    }

    public void setCollectingTripAuthorizations(Set<CollectingTripAuthorization> collectingTripAuthorizations) {
        this.collectingTripAuthorizations = collectingTripAuthorizations;
    }

    /**
     *      * AgentID of Issuee
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "IssuedToID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getIssuedTo() {
        return this.issuedTo;
    }

    public void setIssuedTo(Agent issuedTo) {
        this.issuedTo = issuedTo;
    }

    /**
     *      * AgentID of Issuer
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "IssuedByID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getIssuedBy() {
        return this.issuedBy;
    }

    public void setIssuedBy(Agent issuedBy) {
        this.issuedBy = issuedBy;
    }

    //@OneToMany(cascade = {javax.persistence.CascadeType.ALL}, mappedBy = "permit")
    @OneToMany(mappedBy = "permit")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @OrderBy("ordinal ASC")
    public Set<PermitAttachment> getPermitAttachments()
    {
        return permitAttachments;
    }

    public void setPermitAttachments(Set<PermitAttachment> permitAttachments)
    {
        this.permitAttachments = permitAttachments;
    }

    /**
     * Link to Institution 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "InstitutionID", unique = false, nullable = false, insertable = true, updatable = true)
    public Institution getInstitution() {
        return institution;
    }
    
    public void setInstitution(Institution institution) {
        this.institution = institution;
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
        return AccessionAuthorization.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        Vector<Object> ids = BasicSQLUtils.querySingleCol("SELECT AccessionAuthorizationID FROM accessionauthorization WHERE PermitID = "+ permitId);
        if (ids.size() == 1)
        {
            return (Integer)ids.get(0);
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        return permitNumber != null ? permitNumber : super.getIdentityTitle();
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
        return 6;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentReferences()
     */
    @Override
    @Transient
    public Set<PermitAttachment> getAttachmentReferences()
    {
        return permitAttachments;
    }

}
