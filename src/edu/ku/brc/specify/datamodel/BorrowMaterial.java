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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;


import java.util.Date;
import java.util.HashSet;
import java.util.Set;




/**

 */
@Entity
@Table(name = "borrowmaterial")
public class BorrowMaterial extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long borrowMaterialId;
     protected String materialNumber;
     protected String description;
     protected Short quantity;
     protected String outComments;
     protected String inComments;
     protected Short quantityResolved;
     protected Short quantityReturned;
     protected Set<BorrowReturnMaterial> borrowReturnMaterials;
     protected Borrow borrow;


    // Constructors

    /** default constructor */
    public BorrowMaterial() {
        //
    }
    
    /** constructor with id */
    public BorrowMaterial(Long borrowMaterialId) {
        this.borrowMaterialId = borrowMaterialId;
    }
   
    
    

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        borrowMaterialId = null;
        materialNumber = null;
        description = null;
        quantity = null;
        outComments = null;
        inComments = null;
        quantityResolved = null;
        quantityReturned = null;
        borrowReturnMaterials = new HashSet<BorrowReturnMaterial>();
        borrow = null;
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "BorrowMaterialID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getBorrowMaterialId() {
        return this.borrowMaterialId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.borrowMaterialId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return BorrowMaterial.class;
    }
    
    public void setBorrowMaterialId(Long borrowMaterialId) {
        this.borrowMaterialId = borrowMaterialId;
    }

    /**
     *      * e.g. 'FMNH 223456'
     */
    @Column(name = "MaterialNumber", unique = false, nullable = false, insertable = true, updatable = true, length = 50)
    public String getMaterialNumber() {
        return this.materialNumber;
    }
    
    public void setMaterialNumber(String materialNumber) {
        this.materialNumber = materialNumber;
    }

    /**
     *      * Description of the material. 'e.g. Bufo bufo skull'
     */
    @Column(name = "Description", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *      * Number of specimens (for lots)
     */
    @Column(name = "Quantity", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getQuantity() {
        return this.quantity;
    }
    
    public void setQuantity(Short quantity) {
        this.quantity = quantity;
    }

    /**
     *      * Notes concerning the return of the material
     */
    @Column(name = "OutComments", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
    public String getOutComments() {
        return this.outComments;
    }
    
    public void setOutComments(String outComments) {
        this.outComments = outComments;
    }

    /**
     *      * Notes concerning the receipt of the material
     */
    @Column(name = "InComments", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
    public String getInComments() {
        return this.inComments;
    }
    
    public void setInComments(String inComments) {
        this.inComments = inComments;
    }

    /**
     *      * Quantity resolved (Returned, Accessioned, Lost, Discarded, Destroyed ...)
     */
    @Column(name = "QuantityResolved", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getQuantityResolved() {
        return this.quantityResolved;
    }
    
    public void setQuantityResolved(Short quantityResolved) {
        this.quantityResolved = quantityResolved;
    }

    /**
     *      * Quantity returned
     */
    @Column(name = "QuantityReturned", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getQuantityReturned() {
        return this.quantityReturned;
    }
    
    public void setQuantityReturned(Short quantityReturned) {
        this.quantityReturned = quantityReturned;
    }


    /**
     * 
     */
    @OneToMany(cascade = { CascadeType.REMOVE }, fetch = FetchType.LAZY, mappedBy = "borrowMaterial")
    public Set<BorrowReturnMaterial> getBorrowReturnMaterials() {
        return this.borrowReturnMaterials;
    }
    
    public void setBorrowReturnMaterials(Set<BorrowReturnMaterial> borrowReturnMaterials) {
        this.borrowReturnMaterials = borrowReturnMaterials;
    }

    /**
     *      * ID of the Borrow containing the Prep
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "BorrowID", unique = false, nullable = false, insertable = true, updatable = true)
    public Borrow getBorrow() {
        return this.borrow;
    }
    
    public void setBorrow(Borrow borrow) {
        this.borrow = borrow;
    }





    // Add Methods

    public void addBorrowReturnMaterials(final BorrowReturnMaterial borrowReturnMaterial)
    {
        this.borrowReturnMaterials.add(borrowReturnMaterial);
        borrowReturnMaterial.setBorrowMaterial(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeBorrowReturnMaterials(final BorrowReturnMaterial borrowReturnMaterial)
    {
        this.borrowReturnMaterials.remove(borrowReturnMaterial);
        borrowReturnMaterial.setBorrowMaterial(null);
    }

    // Delete Add Methods
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public Integer getTableId()
    {
        return 20;
    }

}
