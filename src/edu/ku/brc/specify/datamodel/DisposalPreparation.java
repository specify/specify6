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
import javax.persistence.Transient;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "disposalpreparation")
public class DisposalPreparation extends DataModelObjBase implements java.io.Serializable, PreparationHolderIFace {

    // Fields

    protected Integer disposalPreparationId;
    protected Integer quantity;
    protected String remarks;
    protected Disposal disposal;
    protected LoanReturnPreparation loanReturnPreparation;
    protected Preparation preparation;


    // Constructors

    /** default constructor */
    public DisposalPreparation() {
        //
    }

    /** constructor with id */
    public DisposalPreparation(Integer disposalPreparationId) {
        this.disposalPreparationId = disposalPreparationId;
    }




    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        disposalPreparationId = null;
        quantity = null;
        remarks = null;
        disposal = null;
        loanReturnPreparation = null;
        preparation = null;
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "DisposalPreparationID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getDisposalPreparationId() {
        return this.disposalPreparationId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.disposalPreparationId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return DisposalPreparation.class;
    }

    public void setDisposalPreparationId(Integer disposalPreparationId) {
        this.disposalPreparationId = disposalPreparationId;
    }

    /**
     *      * Number of specimens disposaled (necessary for lots)
     */
    @Column(name = "Quantity", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getQuantity() {
        return this.quantity == null ? 0 : this.quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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
     *      * The disposal
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "DisposalID", unique = false, nullable = false, insertable = true, updatable = true)
    public Disposal getDisposal() {
        return this.disposal;
    }

    public void setDisposal(Disposal disposal) {
        this.disposal = disposal;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "LoanReturnPreparationID", unique = false, nullable = true, insertable = true, updatable = true)
    public LoanReturnPreparation getLoanReturnPreparation() {
        return this.loanReturnPreparation;
    }

    public void setLoanReturnPreparation(LoanReturnPreparation loanReturnPreparation) {
        this.loanReturnPreparation = loanReturnPreparation;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "PreparationID", unique = false, nullable = true, insertable = true, updatable = true)
    public Preparation getPreparation()
    {
        return this.preparation;
    }

    public void setPreparation(Preparation preparation)
    {
        this.preparation = preparation;
    }

    @Override
    @Transient
    public Integer getQuantityReturned() {
        return null;
    }

    // Add Methods

    /*public void addLoanReturnPreparations(final LoanReturnPreparation loanReturnPreparation)
    {
        this.loanReturnPreparations.add(loanReturnPreparation);
        loanReturnPreparation.setDisposalPreparation(this);
    }*/

    // Done Add Methods

    // Delete Methods

    /*public void removeLoanReturnPreparations(final LoanReturnPreparation loanReturnPreparation)
    {
        this.loanReturnPreparations.remove(loanReturnPreparation);
        loanReturnPreparation.setDisposalPreparation(null);
    }*/

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Disposal.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return disposal != null ? disposal.getId() : null;
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
        return 36;
    }

}
