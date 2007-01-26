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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.Calendar;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@Table(name = "loanreturnphysicalobject")
public class LoanReturnPhysicalObject extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long loanReturnPhysicalObjectId;
     protected Calendar returnedDate;
     protected Integer quantity;
     protected String remarks;
     protected LoanPhysicalObject loanPhysicalObject;
     protected DeaccessionPreparation deaccessionPreparation;
     protected Agent receivedBy;


    // Constructors

    /** default constructor */
    public LoanReturnPhysicalObject() {
        //
    }
    
    /** constructor with id */
    public LoanReturnPhysicalObject(Long loanReturnPhysicalObjectId) {
        this.loanReturnPhysicalObjectId = loanReturnPhysicalObjectId;
    }
   
    
    

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        loanReturnPhysicalObjectId = null;
        returnedDate = null;
        quantity = null;
        remarks = null;
        loanPhysicalObject = null;
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
    @Column(name = "LoanReturnPhysicalObjectID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getLoanReturnPhysicalObjectId() {
        return this.loanReturnPhysicalObjectId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.loanReturnPhysicalObjectId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return LoanReturnPhysicalObject.class;
    }
    
    public void setLoanReturnPhysicalObjectId(Long loanReturnPhysicalObjectId) {
        this.loanReturnPhysicalObjectId = loanReturnPhysicalObjectId;
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
    @Column(name = "Remarks", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *      * Link to LoanPhysicalObject table
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "LoanPhysicalObjectID", unique = false, nullable = false, insertable = true, updatable = true)
    public LoanPhysicalObject getLoanPhysicalObject() {
        return this.loanPhysicalObject;
    }
    
    public void setLoanPhysicalObject(LoanPhysicalObject loanPhysicalObject) {
        this.loanPhysicalObject = loanPhysicalObject;
    }

    /**
     *      * ID of associated (if present) DeaccessionPhysicalObject record
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "DeaccessionPhysicalObjectID", unique = false, nullable = true, insertable = true, updatable = true)
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
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "ReceivedByID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getReceivedBy() {
        return this.receivedBy;
    }
    
    public void setReceivedBy(Agent agent) {
        this.receivedBy = agent;
    }





    // Add Methods

    // Done Add Methods

    // Delete Methods

    // Delete Add Methods
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public Integer getTableId()
    {
        return 55;
    }

}
