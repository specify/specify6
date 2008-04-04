/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.datamodel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
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

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.dbsupport.DBConnection;

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
public class Loan extends DisciplineMember implements AttachmentOwnerIFace<LoanAttachment>, java.io.Serializable 
{

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
    
    protected String                  receivedComments;
    protected String                  specialConditions;
    protected Boolean                 isFinancialResponsibility;
    protected String                  purposeOfLoan;
    protected Calendar                overdueNotiSentDate; // Loan Only
    protected Calendar                dateReceived;
    
    protected String                  srcGeography;
    protected String                  srcTaxonomy;
    
    protected String                  remarks;
    protected String                  text1;
    protected String                  text2;
    protected Float                   number1;
    protected Float                   number2;
    protected Boolean                 isClosed; // Loan Only
    protected Boolean                 yesNo1;
    protected Boolean                 yesNo2;
    
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
        loanId          = null;
        loanNumber      = null;
        loanDate        = null;
        currentDueDate  = null;
        originalDueDate = null;

        dateClosed      = null;
        
        receivedComments    = null;
        specialConditions   = null;
        isFinancialResponsibility = null;
        purposeOfLoan       = null;
        overdueNotiSentDate = null;
        dateReceived        = null;
        
        srcGeography        = null;
        srcTaxonomy         = null;
        
        remarks         = null;
        text1           = null;
        text2           = null;
        number1         = null;
        number2         = null;

        isClosed        = OPEN;
        yesNo1          = null;
        yesNo2          = null;
        loanAgents      = new HashSet<LoanAgent>();

        loanPreparations = new HashSet<LoanPreparation>();
        shipments           = new HashSet<Shipment>();
        
        loanAttachments = new HashSet<LoanAttachment>();
        division        = null;
        addressOfRecord = null;

        if (false)
        {
            // XXX For Demo
            try
            {
                Connection conn = DBConnection.getInstance().createConnection();
                if (conn != null)
                {
                    Statement  stmt = conn.createStatement();
                    ResultSet  rs   = stmt.executeQuery("select LoanNumber from loan order by LoanNumber desc");
                    if (rs.next())
                    {
                        String numStr = rs.getString(1);
                        int num = Integer.parseInt(numStr.substring(6,8));
                        num++;
                        loanNumber = String.format("2007-%03d", new Object[] {num});
                    } else
                    {
                        loanNumber = "2007-001";
                    }
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

    }
    // End Initializer

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
    @Column(name = "SrcGeography", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
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
    @Column(name = "SrcTaxonomy", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
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
     *      * User definable
     */
    @Column(name = "Text1", length=300, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    @Column(name = "Text2", length=300, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText2() {
        return this.text2;
    }
    
    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      * User definable
     */
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
    public Float getNumber1() {
        return this.number1;
    }
    
    public void setNumber1(Float number1) {
        this.number1 = number1;
    }

    /**
     *      * User definable
     */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
    public Float getNumber2() {
        return this.number2;
    }
    
    public void setNumber2(Float number2) {
        this.number2 = number2;
    }

    /**
     *      * 'No' until all preparations in the loan have been returned/resolved.
     */
    @Column(name="IsClosed", unique=false, nullable=true, insertable=true, updatable=false)
    public Boolean getIsClosed() {
        return this.isClosed;
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
    
    @OneToMany(cascade = {javax.persistence.CascadeType.ALL}, mappedBy = "loan")
    public Set<LoanAttachment> getLoanAttachments()
    {
        return loanAttachments;
    }

    public void setLoanAttachments(Set<LoanAttachment> loanAttachments)
    {
        this.loanAttachments = loanAttachments;
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

    @Transient
    public Set<LoanAttachment> getAttachmentReferences()
    {
        return loanAttachments;
    }

}
