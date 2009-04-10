/* Copyright (C) 2009, University of Kansas Center for Research
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

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "loanreturnpreparation")
@org.hibernate.annotations.Table(appliesTo="loanreturnpreparation", indexes =
    {   @Index (name="LoanReturnedDateIDX", columnNames={"ReturnedDate"}),
        @Index (name="LoanRetPrepColMemIDX", columnNames={"CollectionMemberID"})
    })
public class LoanReturnPreparation extends CollectionMember implements java.io.Serializable {

    // Fields    

    protected Integer                loanReturnPreparationId;
    protected Calendar               returnedDate;
    protected Integer                quantityResolved;
    protected Integer                quantityReturned;
    protected String                 remarks;
    protected LoanPreparation        loanPreparation;
    protected DeaccessionPreparation deaccessionPreparation;
    protected Agent                  receivedBy;


    // Constructors

    /** default constructor */
    public LoanReturnPreparation() {
        //
    }
    
    /** constructor with id */
    public LoanReturnPreparation(Integer loanReturnPreparationId)
    {
        this.loanReturnPreparationId = loanReturnPreparationId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        loanReturnPreparationId = null;
        returnedDate            = null;
        quantityResolved        = null;
        quantityReturned        = null;
        remarks                 = null;
        loanPreparation         = null;
        deaccessionPreparation  = null;
        receivedBy              = null;
    }
    // End Initializer

    // Property accessors

    /**
     *      * PrimaryKey
     */
    @Id
    @GeneratedValue
    @Column(name = "LoanReturnPreparationID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getLoanReturnPreparationId() {
        return this.loanReturnPreparationId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.loanReturnPreparationId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return LoanReturnPreparation.class;
    }
    
    public void setLoanReturnPreparationId(Integer loanReturnPreparationId) {
        this.loanReturnPreparationId = loanReturnPreparationId;
    }

    /**
     * 
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "ReturnedDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getReturnedDate() {
        return this.returnedDate;
    }
    
    public void setReturnedDate(Calendar returnedDate) {
        this.returnedDate = returnedDate;
    }

    /**
     * Number of specimens returned, deaccessioned or otherwise accounted for. (necessary for Lots)
     */
    @Column(name = "QuantityResolved", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getQuantityResolved() 
    {
        return this.quantityResolved == null ? 0 : this.quantityResolved;
    }
    
    public void setQuantityResolved(Integer quantityResolved) 
    {
        this.quantityResolved = quantityResolved;
    }

    /**
     *      * Number of specimens returned. (necessary for Lots)
     */
    @Column(name = "QuantityReturned", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getQuantityReturned() 
    {
        return this.quantityReturned == null ? 0 : this.quantityReturned;
    }
    
    public void setQuantityReturned(Integer quantityReturned) 
    {
        this.quantityReturned = quantityReturned;
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
     *      * Link to LoanPreparation table
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "LoanPreparationID", unique = false, nullable = false, insertable = true, updatable = true)
    public LoanPreparation getLoanPreparation() {
        return this.loanPreparation;
    }
    
    public void setLoanPreparation(LoanPreparation loanPreparation) {
        this.loanPreparation = loanPreparation;
    }

    /**
     *      * ID of associated (if present) DeaccessionPreparation record
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "DeaccessionPreparationID", unique = false, nullable = true, insertable = true, updatable = true)
    public DeaccessionPreparation getDeaccessionPreparation() {
        return this.deaccessionPreparation;
    }
    
    public void setDeaccessionPreparation(DeaccessionPreparation deaccessionPreparation) {
        this.deaccessionPreparation = deaccessionPreparation;
    }

    /**
     *      * Person processing the loan return
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ReceivedByID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getReceivedBy() {
        return this.receivedBy;
    }
    
    public void setReceivedBy(Agent agent) {
        this.receivedBy = agent;
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
        return 55;
    }

}
