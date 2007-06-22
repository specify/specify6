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
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.ui.forms.FormDataObjIFace;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@Table(name = "loan")
public class Loan extends DataModelObjBase implements java.io.Serializable {

    // options for the 'category' field
    public static final Boolean LOAN = false;
    public static final Boolean GIFT = true;
    
    // options for the 'closed' field
    public static final Boolean CLOSED = true;
    public static final Boolean OPEN   = false;
    
    // Fields    
     protected Long loanId;
     protected String loanNumber;
     protected Calendar loanDate;
     protected Calendar currentDueDate;
     protected Calendar originalDueDate;
     protected Calendar dateClosed;
     protected Boolean isGift;
     protected String remarks;
     protected String text1;
     protected String text2;
     protected Float number1;
     protected Float number2;
     protected Boolean isClosed;
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     protected Set<LoanAgent> loanAgents;
     protected Set<LoanPhysicalObject> loanPhysicalObjects;
     //protected Shipment shipment;
     protected Set<Shipment> shipments;
     protected Set<Attachment> attachments;


    // Constructors

    /** default constructor */
    public Loan()
    {
        // do nothing
    }
    
    /** constructor with id */
    public Loan(Long loanId) {
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
        isGift          = null;
        remarks         = null;
        text1           = null;
        text2           = null;
        number1         = null;
        number2         = null;

        isClosed        = OPEN;
        yesNo1          = null;
        yesNo2          = null;
        loanAgents      = new HashSet<LoanAgent>();

        loanPhysicalObjects = new HashSet<LoanPhysicalObject>();
        shipments           = new HashSet<Shipment>();
        attachments         = new HashSet<Attachment>();

        
        if (true)
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
    public Long getLoanId() {
        return this.loanId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Override
    @Transient
    public Long getId()
    {
        return this.loanId;
    }
   
    public void setLoanId(Long loanId) {
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
    @Column(name = "CurrentDueDate", unique = false, nullable = true, insertable = true, updatable = true, length = 10)
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
     *      * Type of record: loan(0), Gift(1)
     */
    @Column(name="IsGift", unique=false, nullable=true, insertable=true, updatable=false)
    public Boolean getIsGift() {
        return this.isGift;
    }
    

    public void setIsGift(Boolean isGift) {
        this.isGift = isGift;
    }

    /**
     * 
     */
    @Lob
    @Column(name="Remarks", unique=false, nullable=true, updatable=true, insertable=true)
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
    public Set<LoanPhysicalObject> getLoanPhysicalObjects() {
        return this.loanPhysicalObjects;
    }
    
    public void setLoanPhysicalObjects(Set<LoanPhysicalObject> loanPhysicalObjects) {
        this.loanPhysicalObjects = loanPhysicalObjects;
    }

    /**
     * 
     */
    @ManyToMany(cascade = {}, fetch = FetchType.LAZY)
    @JoinTable(
            name="loan_shipment",
            joinColumns = {@JoinColumn(name="LoanID")},
            inverseJoinColumns= {@JoinColumn(name="ShipmentID")})
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<Shipment> getShipments() {
        return this.shipments;
    }
    
    public void setShipments(Set<Shipment> shipments) {
        this.shipments = shipments;
    }
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "loan")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<Attachment> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(Set<Attachment> attachments)
    {
        this.attachments = attachments;
    }

    public void addLoanAgent(final LoanAgent loanAgent)
    {
        this.loanAgents.add(loanAgent);
        loanAgent.setLoan(this);
    }

    public void addLoanPhysicalObjects(final LoanPhysicalObject loanPhysicalObject)
    {
        this.loanPhysicalObjects.add(loanPhysicalObject);
        loanPhysicalObject.setLoan(this);
    }


    // Done Add Methods

    // Delete Methods
    public void removeShipment(final Shipment shipment)
    {
        if(shipment==null)return;
        this.shipments.remove(shipment);
        shipment.removeLoan(this);
    }
    public void removeLoanAgent(final LoanAgent loanAgent)
    {
        this.loanAgents.remove(loanAgent);
        loanAgent.setLoan(null);
    }

    public void removeLoanPhysicalObjects(final LoanPhysicalObject loanPhysicalObject)
    {
        this.loanPhysicalObjects.remove(loanPhysicalObject);
        loanPhysicalObject.setLoan(null);
    }

    // Delete Add Methods
    public void addShipment(final Shipment shipment)
    {
        if (shipment != null)
        {
            this.shipments.add(shipment);
            shipment.getLoans().add(this);
        }
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#addReference(edu.ku.brc.ui.forms.FormDataObjIFace, java.lang.String)
     */
    @Override
    public void addReference(FormDataObjIFace ref, String refType)
    {
        if( ref instanceof Attachment )
        {
            Attachment a = (Attachment)ref;
            attachments.add(a);
            a.setLoan(this);
            return;
        }
        super.addReference(ref, refType);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#removeReference(edu.ku.brc.ui.forms.FormDataObjIFace, java.lang.String)
     */
    @Override
    public void removeReference(FormDataObjIFace refObj, String refType)
    {
        if( refObj instanceof Attachment )
        {
            attachments.remove(refObj);
            return;
        }
        super.removeReference(refObj, refType);
    }

}
