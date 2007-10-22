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
    {   @Index (name="LoanReturnedDateIDX", columnNames={"ReturnedDate"})
    })
public class LoanReturnPreparation extends CollectionMember implements java.io.Serializable {

    // Fields    

    protected Integer                loanReturnPreparationId;
    protected Calendar               returnedDate;
    protected Integer                quantity;
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
        returnedDate = null;
        quantity = null;
        remarks = null;
        loanPreparation = null;
        deaccessionPreparation = null;
        receivedBy = null;
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
     *      * Quantity of items returned (necessary for lots)
     */
    @Column(name = "Quantity", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getQuantity() {
        return this.quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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
