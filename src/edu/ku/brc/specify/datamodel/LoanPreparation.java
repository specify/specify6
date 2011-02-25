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

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "loanpreparation")
@org.hibernate.annotations.Table(appliesTo="loanpreparation", indexes =
    {   @Index (name="LoanPrepDspMemIDX", columnNames={"DisciplineID"})
    })
public class LoanPreparation extends DisciplineMember implements java.io.Serializable, PreparationHolderIFace, Comparable<LoanPreparation>
{

    // Fields    

    protected Integer                       loanPreparationId;
    protected Integer                       quantity;
    protected String                        descriptionOfMaterial;
    protected String                        outComments;          // Shipped Comments
    protected String                        inComments;           // Returned Comments
    protected String                        receivedComments;     // Received Comments
    protected Integer                       quantityResolved;
    protected Integer                       quantityReturned;
    protected Boolean                       isResolved;
    protected Preparation                   preparation;
    protected Loan                          loan;
    protected Set<LoanReturnPreparation>    loanReturnPreparations;


    // Constructors

    /** default constructor */
    public LoanPreparation() 
    {
        //
    }
    
    /** constructor with id */
    public LoanPreparation(Integer loanPreparationId) 
    {
        this.loanPreparationId = loanPreparationId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        loanPreparationId = null;
        quantity = null;
        descriptionOfMaterial = null;
        outComments = null;
        inComments = null;
        receivedComments = null;
        quantityResolved = null;
        quantityReturned = null;
        isResolved = false;
        preparation = null;
        loan = null;
        loanReturnPreparations = new HashSet<LoanReturnPreparation>();
    }
    // End Initializer

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#forceLoad()
     */
    @Override
    public void forceLoad()
    {
        super.forceLoad();
        
        for (LoanReturnPreparation lrp : getLoanReturnPreparations())
        {
            lrp.forceLoad();
        }
    }

    // Property accessors

    /**
     *      * PrimaryKey
     */
    @Id
    @GeneratedValue
    @Column(name = "LoanPreparationID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getLoanPreparationId() {
        return this.loanPreparationId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.loanPreparationId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return LoanPreparation.class;
    }
    
    public void setLoanPreparationId(Integer loanPreparationId) {
        this.loanPreparationId = loanPreparationId;
    }

    /**
     *      * The total number of specimens  loaned (necessary for lots)
     */
    @Column(name = "Quantity", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getQuantity() 
    {
        return this.quantity == null ? 0 : this.quantity;
    }
    
    public void setQuantity(Integer quantity) 
    {
        this.quantity = quantity;
    }

    /**
     *      * Description of loaned material (intended to be used for non-cataloged items, i.e. when PreparationID is null)
     */
    @Column(name = "DescriptionOfMaterial", unique = false, nullable = true, insertable = true, updatable = true)
    public String getDescriptionOfMaterial() 
    {
        return this.descriptionOfMaterial;
    }
    
    public void setDescriptionOfMaterial(String descriptionOfMaterial) {
        this.descriptionOfMaterial = descriptionOfMaterial;
    }

    /**
     *      * Comments on item when loaned
     */
    @Lob
    @Column(name = "OutComments", unique = false, nullable = true, insertable = true, updatable = true, length = 1024)
    public String getOutComments() 
    {
        return this.outComments;
    }
    
    public void setOutComments(String outComments) 
    {
        this.outComments = outComments;
    }

    /**
     *      * Comments on item when returned
     */
    @Lob
    @Column(name = "InComments", unique = false, nullable = true, insertable = true, updatable = true, length = 1024)
    public String getInComments() {
        return this.inComments;
    }
    
    public void setInComments(String inComments) {
        this.inComments = inComments;
    }

    /**
     * @return the receivedComments
     */
    @Lob
    @Column(name = "ReceivedComments", unique = false, nullable = true, insertable = true, updatable = true, length = 1024)
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
     *      * Number of specimens returned, deaccessioned or otherwise accounted for. (necessary for Lots)
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
     *      * User definable
     */
    @Column(name="IsResolved",unique=false,nullable=false,insertable=true,updatable=true)
    public Boolean getIsResolved() 
    {
        return this.isResolved == null ? false : this.isResolved;
    }
    
    public void setIsResolved(Boolean isResolved) 
    {
        this.isResolved = isResolved;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "PreparationID", unique = false, nullable = true, insertable = true, updatable = true)
    public Preparation getPreparation() {
        return this.preparation;
    }
    
    public void setPreparation(Preparation preparation) {
        this.preparation = preparation;
    }

    /**
     *      * Loan containing the Preparation
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "LoanID", unique = false, nullable = false, insertable = true, updatable = true)
    public Loan getLoan() {
        return this.loan;
    }
    
    public void setLoan(Loan loan) {
        this.loan = loan;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "loanPreparation")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN})
    public Set<LoanReturnPreparation> getLoanReturnPreparations() {
        return this.loanReturnPreparations;
    }
    
    public void setLoanReturnPreparations(Set<LoanReturnPreparation> loanReturnPreparations) {
        this.loanReturnPreparations = loanReturnPreparations;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Loan.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
   public Integer getParentId()
    {
        return loan != null ? loan.getId() : null;
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
        return 54;
    }
    
    //----------------------------------------------------------------------
    //-- Comparable Interface
    //----------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(final LoanPreparation obj)
    {
    	if (preparation != null && obj.getPreparation() != null)
    	{
    		CollectionObject co1 = preparation.getCollectionObject();
    		CollectionObject co2 = obj.getPreparation().getCollectionObject();
    		if (co1 != null && co2 != null && co1.getCatalogNumber() != null && co2.getCatalogNumber() != null)
    		{
    			return co1.getCatalogNumber().compareTo(co2.getCatalogNumber());
    		}
    	}
        return timestampCreated != null && obj != null && obj.timestampCreated != null ? timestampCreated.compareTo(obj.timestampCreated) : 0;
    }
}
