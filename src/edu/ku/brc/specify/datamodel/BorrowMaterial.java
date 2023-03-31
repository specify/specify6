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

import javax.persistence.CascadeType;
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

import org.hibernate.annotations.Index;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "borrowmaterial")
@org.hibernate.annotations.Table(appliesTo="borrowmaterial", indexes =
    {   
        @Index (name="BorMaterialNumberIDX", columnNames={"MaterialNumber"}),
        @Index (name="BorMaterialColMemIDX", columnNames={"CollectionMemberID"}),
        @Index (name="DescriptionIDX", columnNames={"Description"})
    })
public class BorrowMaterial extends CollectionMember implements java.io.Serializable {

    // Fields    

     protected Integer borrowMaterialId;
     protected String  materialNumber;
     protected String  description;
     protected Short   quantity;
     protected String  outComments;
     protected String  inComments;
     protected Short   quantityResolved;
     protected Short   quantityReturned;
     protected Set<BorrowReturnMaterial> borrowReturnMaterials;
     protected Borrow borrow;
     protected String text1;
     protected String text2;


    // Constructors

    /** default constructor */
    public BorrowMaterial() {
        //
    }
    
    /** constructor with id */
    public BorrowMaterial(Integer borrowMaterialId) {
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
        text1 = null;
        text2 = null;
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "BorrowMaterialID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getBorrowMaterialId() {
        return this.borrowMaterialId;
    }

    
    /* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#forceLoad()
	 */
	@Override
	public void forceLoad() {
		super.forceLoad();
		borrowReturnMaterials.size();	
	}

	/**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
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
    
    public void setBorrowMaterialId(Integer borrowMaterialId) {
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
    @Column(name = "Description", unique = false, nullable = true, insertable = true, updatable = true, length = 250)
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
    @Lob
    @Column(name = "OutComments", length=1024, unique = false, nullable = true, insertable = true, updatable = true)
    public String getOutComments() {
        return this.outComments;
    }
    
    public void setOutComments(String outComments) {
        this.outComments = outComments;
    }

    
    /**
	 * @return the text1
	 */
    @Lob
    @Column(name = "Text1", length=6667, unique = false, nullable = true, insertable = true, updatable = true)
	public String getText1() {
		return text1;
	}

	/**
	 * @param text1 the text1 to set
	 */
	public void setText1(String text1) {
		this.text1 = text1;
	}

	/**
	 * @return the text2
	 */
    @Lob
    @Column(name = "Text2", length=6667, unique = false, nullable = true, insertable = true, updatable = true)
	public String getText2() {
		return text2;
	}

	/**
	 * @param text2 the text2 to set
	 */
	public void setText2(String text2) {
		this.text2 = text2;
	}

	/**
     *      * Notes concerning the receipt of the material
     */
    @Lob
    @Column(name = "InComments", length=1024, unique = false, nullable = true, insertable = true, updatable = true)
    public String getInComments() {
        return this.inComments;
    }
    
    public void setInComments(String inComments) {
        this.inComments = inComments;
    }

    /**
     *      * Quantity resolved (Returned, Accessioned, Lost, Discarded, Destroyed, ...)
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
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
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


    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Borrow.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return borrow != null ? borrow.getId() : null;
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
        return 20;
    }

}
