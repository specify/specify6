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
import java.util.Date;




/**

 */
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
    }
    
    /** constructor with id */
    public BorrowReturnMaterial(Long borrowReturnMaterialId) {
        this.borrowReturnMaterialId = borrowReturnMaterialId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        borrowReturnMaterialId = null;
        returnedDate = null;
        quantity = null;
        remarks = null;
        timestampModified = null;
        timestampCreated = new Date();
        lastEditedBy = null;
        agent = null;
        borrowMaterial = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Long getBorrowReturnMaterialId() {
        return this.borrowReturnMaterialId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.borrowReturnMaterialId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    public Class getDataClass()
    {
        return BorrowReturnMaterial.class;
    }
    
    public void setBorrowReturnMaterialId(Long borrowReturnMaterialId) {
        this.borrowReturnMaterialId = borrowReturnMaterialId;
    }

    /**
     * 
     */
    public Calendar getReturnedDate() {
        return this.returnedDate;
    }
    
    public void setReturnedDate(Calendar returnedDate) {
        this.returnedDate = returnedDate;
    }

    /**
     *      * Quantity of preparations returned
     */
    public Short getQuantity() {
        return this.quantity;
    }
    
    public void setQuantity(Short quantity) {
        this.quantity = quantity;
    }

    /**
     * 
     */
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }


    /**
     *      * person processing the  return
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    /**
     *      * Borrowed preparation returned
     */
    public BorrowMaterial getBorrowMaterial() {
        return this.borrowMaterial;
    }
    
    public void setBorrowMaterial(BorrowMaterial borrowMaterial) {
        this.borrowMaterial = borrowMaterial;
    }





    // Add Methods

    // Done Add Methods

    // Delete Methods

    // Delete Add Methods
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    public Integer getTableId()
    {
        return 21;
    }

}
