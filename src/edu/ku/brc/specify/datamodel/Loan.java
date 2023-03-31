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

import java.util.*;
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

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.tasks.InteractionsTask;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 26, 2008
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "loan")
@org.hibernate.annotations.Table(appliesTo="loan", indexes =
    {   @Index (name="LoanNumberIDX", columnNames={"LoanNumber"}),
        @Index (name="LoanDateIDX", columnNames={"LoanDate"}),
        @Index (name="CurrentDueDateIDX", columnNames={"CurrentDueDate"})
    })
@SuppressWarnings("serial")
public class Loan extends DisciplineMember implements AttachmentOwnerIFace<LoanAttachment>, OneToManyProviderIFace, java.io.Serializable 
{
    private static final Logger log = Logger.getLogger(Loan.class);

    // options for the 'closed' field
    public static final Boolean CLOSED = true;
    public static final Boolean OPEN   = false;
    
    // Fields    
    protected Integer                 loanId;
    protected String                  loanNumber;
    protected Calendar                loanDate;
    protected Calendar                currentDueDate; // Loan Only
    protected Calendar                originalDueDate; // Loan Only
    protected Calendar                dateClosed; // Loan Only
    protected String status;
    
    protected String                  receivedComments;
    protected String                  specialConditions;
    protected Boolean                 isFinancialResponsibility;
    protected String                  purposeOfLoan;
    protected Calendar                overdueNotiSentDate; // Loan Only
    protected Calendar                dateReceived;
    
    protected String                  srcGeography;
    protected String                  srcTaxonomy;
    
    protected String                  remarks;
    protected String				  contents;
    protected String                  text1;
    protected String                  text2;
    protected String                  text3;
    protected String                  text4;
    protected String                  text5;
    protected BigDecimal                   number1;
    protected BigDecimal                   number2;
    protected Boolean                 isClosed; // Loan Only
    protected Boolean                 yesNo1;
    protected Boolean                 yesNo2;
	protected Integer integer1;
	protected Integer integer2;
	protected Integer integer3;
    
    protected AddressOfRecord         addressOfRecord;
    protected Set<LoanAgent>          loanAgents;
    protected Set<LoanPreparation>    loanPreparations;
    protected Set<Shipment>           shipments;
    protected Set<LoanAttachment>     loanAttachments;
    
    protected Division                division;


    // Constructors

    /** default constructor */
    public Loan()
    {
        // do nothing
    }
    
    /** constructor with id */
    public Loan(Integer loanId) {
        this.loanId = loanId;
    }

    // Initializer
    @Override
    public void initialize()
    {
		super.init();
		loanId = null;
		loanNumber = null;
		loanDate = null;
		currentDueDate = null;
		originalDueDate = null;

		dateClosed = null;

		receivedComments = null;
		specialConditions = null;
		isFinancialResponsibility = null;
		purposeOfLoan = null;
		overdueNotiSentDate = null;
		dateReceived = null;
        status = null;

		srcGeography = null;
		srcTaxonomy = null;

		remarks = null;
		contents = null;
		text1 = null;
		text2 = null;
                text3 = null;
                text4 = null;
                text5 = null;
		number1 = null;
		number2 = null;
		integer1 = null;
		integer2 = null;
		integer3 = null;

		isClosed = OPEN;
		yesNo1 = null;
		yesNo2 = null;
		loanAgents = new HashSet<LoanAgent>();

		loanPreparations = new HashSet<LoanPreparation>();
		shipments = new HashSet<Shipment>();

		loanAttachments = new HashSet<LoanAttachment>();
		division = null;
        addressOfRecord = null;
    }
    // End Initializer
    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#forceLoad()
     */
    @Override
    public void forceLoad()
    {
        super.forceLoad();
        
        for (LoanPreparation lp : loanPreparations)
        {
            lp.forceLoad();
        }
        
        for (Shipment shipment : shipments)
        {
            shipment.forceLoad();
        }
        
        for (LoanAgent loanAgent : loanAgents)
        {
            loanAgent.forceLoad();
        }
        for (LoanAttachment loanAtt : loanAttachments)
        {
            loanAtt.forceLoad();
        }
    }


    // Property accessors

    /**
     *      * PrimaryKey
     */
    @Id
    @GeneratedValue
    @Column(name = "LoanID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getLoanId() {
        return this.loanId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Override
    @Transient
    public Integer getId()
    {
        return this.loanId;
    }
   
    public void setLoanId(Integer loanId) {
        this.loanId = loanId;
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Loan.class;
    }
    /**
     *      * Invoice number
     */
    @Column(name = "LoanNumber", unique = false, nullable = false, insertable = true, updatable = true, length = 50)
    public String getLoanNumber() {
        return this.loanNumber;
    }
    
    public void setLoanNumber(String loanNumber) {
        this.loanNumber = loanNumber;
    }

    /**
     *      * Date the Loan was created.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "LoanDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getLoanDate() {
        return this.loanDate;
    }
    
    public void setLoanDate(Calendar loanDate) {
        this.loanDate = loanDate;
    }

    /**
     *      * Date the loan is due for return (Same as original Due date unless loan period has been extended)
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "CurrentDueDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getCurrentDueDate() {
        return this.currentDueDate;
    }
    
    public void setCurrentDueDate(Calendar currentDueDate) {
        this.currentDueDate = currentDueDate;
    }

    @Column(name = "Status", length = 64)
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     *      * Date the loan was originally due.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "OriginalDueDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getOriginalDueDate() {
        return this.originalDueDate;
    }
    
    public void setOriginalDueDate(Calendar originalDueDate) {
        this.originalDueDate = originalDueDate;
    }

    /**
     *      * Date loan was closed.
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DateClosed", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDateClosed() {
        return this.dateClosed;
    }
    
    public void setDateClosed(Calendar dateClosed) {
        this.dateClosed = dateClosed;
    }

    /**
     * @return the srcGeography
     */
    @Column(name = "SrcGeography", unique = false, nullable = true, insertable = true, updatable = true, length = 500)
    public String getSrcGeography()
    {
        return srcGeography;
    }

    /**
     * @param srcGeography the srcGeography to set
     */
    public void setSrcGeography(String srcGeography)
    {
        this.srcGeography = srcGeography;
    }

    /**
     * @return the srcTaxonomy
     */
    @Column(name = "SrcTaxonomy", unique = false, nullable = true, insertable = true, updatable = true, length = 500)
    public String getSrcTaxonomy()
    {
        return srcTaxonomy;
    }

    /**
     * @param srcTaxonomy the srcTaxonomy to set
     */
    public void setSrcTaxonomy(String srcTaxonomy)
    {
        this.srcTaxonomy = srcTaxonomy;
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
     * 
     */
    @Lob
    @Column(name = "Contents", length = 4096)
    public String getContents() {
        return this.contents;
    }
    
    /**
     * @param contents
     */
    public void setContents(String contents) {
        this.contents = contents;
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
    @Column(name = "Text3", length = 65535)
    public String getText3() {
        return this.text3;
    }
    
    public void setText3(String text3) {
        this.text3 = text3;
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
     *      * 'No' until all preparations in the loan have been returned/resolved.
     */
    @Column(name="IsClosed", unique = false, nullable = true, updatable = true, insertable = true)
    public Boolean getIsClosed() {
        return this.isClosed != null ? this.isClosed : false;
    }
   
    public void setIsClosed(Boolean isClosed) {
        this.isClosed = isClosed;
    }

    /**
     *      * User definable
     */
    @Column(name="YesNo1", unique = false, nullable = true, updatable = true, insertable = true)
    public Boolean getYesNo1() {
        return this.yesNo1;
    }
    
    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      * User definable
     */
    @Column(name="YesNo2", unique = false, nullable = true, updatable = true, insertable = true)
    public Boolean getYesNo2() {
        return this.yesNo2;
    }
    
    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     * @return the receivedComments
     */
    @Column(name = "ReceivedComments", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getReceivedComments()
    {
        return receivedComments;
    }

    /**
     * @param receivedComments the receivedComments to set
     */
    public void setReceivedComments(String receivedComments)
    {
        this.receivedComments = receivedComments;
    }

    /**
     * @return the specialConditions
     */
    @Lob
    @Column(name = "SpecialConditions", unique = false, nullable = true, insertable = true, updatable = true, length = 2048)
    public String getSpecialConditions()
    {
        return specialConditions;
    }

    /**
     * @param specialConditions the specialConditions to set
     */
    public void setSpecialConditions(String specialConditions)
    {
        this.specialConditions = specialConditions;
    }

    /**
     * @return the isFinancialResponsibility
     */
    @Column(name = "IsFinancialResponsibility", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIsFinancialResponsibility()
    {
        return isFinancialResponsibility;
    }

    /**
     * @param isFinancialResponsibility the isFinancialResponsibility to set
     */
    public void setIsFinancialResponsibility(Boolean isFinancialResponsibility)
    {
        this.isFinancialResponsibility = isFinancialResponsibility;
    }


    /**
     * @return the purposeOfLoan
     */
    @Column(name = "PurposeOfLoan", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getPurposeOfLoan()
    {
        return purposeOfLoan;
    }

    /**
     * @param purposeOfLoan the purposeOfLoan to set
     */
    public void setPurposeOfLoan(String purposeOfLoan)
    {
        this.purposeOfLoan = purposeOfLoan;
    }

    /**
     * @return the overdueNotiSentDate
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "OverdueNotiSetDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getOverdueNotiSentDate()
    {
        return overdueNotiSentDate;
    }

    /**
     * @param overdueNotiSentDate the overdueNotiSentDate to set
     */
    public void setOverdueNotiSentDate(Calendar overdueNotiSentDate)
    {
        this.overdueNotiSentDate = overdueNotiSentDate;
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
     * @param dateReceived the dateReceived to set
     */
    public void setDateReceived(Calendar dateReceived)
    {
        this.dateReceived = dateReceived;
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

    /**
     * @param division the division to set
     */
    public void setDivision(Division division)
    {
        this.division = division;
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
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "loan")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<LoanAgent> getLoanAgents() {
        return this.loanAgents;
    }
    
    public void setLoanAgents(Set<LoanAgent> loanAgents) {
        this.loanAgents = loanAgents;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "loan")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<LoanPreparation> getLoanPreparations() {
        return this.loanPreparations;
    }
    
    public void setLoanPreparations(Set<LoanPreparation> loanPreparations) {
        this.loanPreparations = loanPreparations;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "loan")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<Shipment> getShipments() {
        return this.shipments;
    }
    
    public void setShipments(Set<Shipment> shipments) {
        this.shipments = shipments;
    }
    
    //@OneToMany(cascade = {javax.persistence.CascadeType.ALL}, mappedBy = "loan")
    @OneToMany(mappedBy = "loan")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @OrderBy("ordinal ASC")
    public Set<LoanAttachment> getLoanAttachments()
    {
        return loanAttachments;
    }

    public void setLoanAttachments(Set<LoanAttachment> loanAttachments)
    {
        this.loanAttachments = loanAttachments;
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.PreparationsProviderIFace#getPreparations()
     */
//    @Override
//    @Transient
//    public Set<PreparationHolderIFace> getPreparations()
//    {
//        HashSet<PreparationHolderIFace> set = new HashSet<PreparationHolderIFace>();
//        for (LoanPreparation gp : loanPreparations)
//        {
//            set.add(gp);
//        }
//        return set;
//    }
    
    @Transient
    public Integer getTotalPreps() {
        return countContents(false, false);
    }

    @Transient
    public Integer getTotalItems() {
        return countContents(true, false);
    }

    @Transient
    public Integer getUnresolvedPreps() {
        return countContents(false, true);
    }

    @Transient
    public Integer getUnresolvedItems() {
        return countContents(true, true);
    }

    @Transient
    public Integer getResolvedPreps() {
        return getId() == null ? null : getTotalPreps() - getUnresolvedPreps();
    }

    @Transient
    public Integer getResolvedItems() {
        return getId() == null ? null : getTotalItems() - getUnresolvedItems();
    }

    protected Integer countContents(Boolean countQuantity, Boolean countUnresolved) {
        if (getId() == null) {
            return null;
        } else {
            return BasicSQLUtils.getCountAsInt(getCountContentsSql(countQuantity, countUnresolved, getId()));
        }
    }

    protected static String getCountContentsSql(boolean countQuantity, boolean countUnresolved, int id) {
        return InteractionsTask.getCountContentsSql(countQuantity, countUnresolved, id, getClassTableId());
    }

    @Transient
    public static List<String> getQueryableTransientFields() {
        List<String> result = new ArrayList<>();
        result.add("TotalPreps");
        result.add("TotalItems");
        result.add("UnresolvedPreps");
        result.add("UnresolvedItems");
        result.add("ResolvedPreps");
        result.add("ResolvedItems");
        return result;
    }

    public static Object getQueryableTransientFieldValue(String fldName, Object[] vals) {
        if (vals == null || vals[0] == null) {
            return null;
        } else if (fldName.equalsIgnoreCase("TotalPreps")) {
            return BasicSQLUtils.getCountAsInt(getCountContentsSql(false, false, (Integer)vals[0]));
        } else if (fldName.equalsIgnoreCase("TotalItems")) {
            return BasicSQLUtils.getCountAsInt(getCountContentsSql(true, false, (Integer)vals[0]));
        } else  if (fldName.equalsIgnoreCase("UnresolvedPreps")) {
            return BasicSQLUtils.getCountAsInt(getCountContentsSql(false, true, (Integer)vals[0]));
        } else if (fldName.equalsIgnoreCase("UnresolvedItems")) {
            return BasicSQLUtils.getCountAsInt(getCountContentsSql(true, true, (Integer)vals[0]));
        } else if (fldName.equalsIgnoreCase("ResolvedPreps")) {
            return BasicSQLUtils.getCountAsInt(getCountContentsSql(false, false, (Integer)vals[0])) - BasicSQLUtils.getCountAsInt(getCountContentsSql(false, true, (Integer)vals[0]));
        } else if (fldName.equalsIgnoreCase("ResolvedItems")) {
            return BasicSQLUtils.getCountAsInt(getCountContentsSql(true, false, (Integer)vals[0])) - BasicSQLUtils.getCountAsInt(getCountContentsSql(true, true, (Integer)vals[0]));
        } else {
            log.error("Unknown calculated field: " + fldName);
            return null;
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Discipline.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return discipline != null ? discipline.getId() : null;
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
        return getLoanPreparations();
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
        return 52;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        return loanNumber != null ? loanNumber : super.getIdentityTitle();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentReferences()
     */
    @Override
    @Transient
    public Set<LoanAttachment> getAttachmentReferences()
    {
        return loanAttachments;
    }

}
