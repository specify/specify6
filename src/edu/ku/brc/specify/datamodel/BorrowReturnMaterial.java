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

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "borrowreturnmaterial")
public class BorrowReturnMaterial extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long borrowReturnMaterialId;
     protected Calendar returnedDate;
     protected Short quantity;
     protected String remarks;
     protected Agent agent;
     protected BorrowMaterial borrowMaterial;


    // Constructors

    /** default constructor */
    public BorrowReturnMaterial() {
        //
    }
    
    /** constructor with id */
    public BorrowReturnMaterial(Long borrowReturnMaterialId) {
        this.borrowReturnMaterialId = borrowReturnMaterialId;
    }
   
    
    

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        borrowReturnMaterialId = null;
        returnedDate = null;
        quantity = null;
        remarks = null;
        agent = null;
        borrowMaterial = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "BorrowReturnMaterialID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getBorrowReturnMaterialId() {
        return this.borrowReturnMaterialId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.borrowReturnMaterialId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return BorrowReturnMaterial.class;
    }
    
    public void setBorrowReturnMaterialId(Long borrowReturnMaterialId) {
        this.borrowReturnMaterialId = borrowReturnMaterialId;
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
     *      * Quantity of preparations returned
     */
    @Column(name = "Quantity", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getQuantity() {
        return this.quantity;
    }
    
    public void setQuantity(Short quantity) {
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
     *      * person processing the  return
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ReturnedByID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    /**
     *      * Borrowed preparation returned
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "BorrowMaterialID", unique = false, nullable = false, insertable = true, updatable = true)
    public BorrowMaterial getBorrowMaterial() {
        return this.borrowMaterial;
    }
    
    public void setBorrowMaterial(BorrowMaterial borrowMaterial) {
        this.borrowMaterial = borrowMaterial;
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
        return 21;
    }

}
