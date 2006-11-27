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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;




/**

 */
public class DeaccessionCollectionObject extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long deaccessionCollectionObjectId;
     protected Short quantity;
     protected String remarks;
     protected CollectionObject collectionObject;
     protected Deaccession deaccession;
     protected Set<LoanReturnPhysicalObject> loanReturnPhysicalObjects;


    // Constructors

    /** default constructor */
    public DeaccessionCollectionObject() {
    }
    
    /** constructor with id */
    public DeaccessionCollectionObject(Long deaccessionCollectionObjectId) {
        this.deaccessionCollectionObjectId = deaccessionCollectionObjectId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        deaccessionCollectionObjectId = null;
        quantity = null;
        remarks = null;
        timestampModified = null;
        timestampCreated = new Date();
        lastEditedBy = null;
        collectionObject = null;
        deaccession = null;
        loanReturnPhysicalObjects = new HashSet<LoanReturnPhysicalObject>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    public Long getDeaccessionCollectionObjectId() {
        return this.deaccessionCollectionObjectId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.deaccessionCollectionObjectId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    public Class getDataClass()
    {
        return DeaccessionCollectionObject.class;
    }
    
    public void setDeaccessionCollectionObjectId(Long deaccessionCollectionObjectId) {
        this.deaccessionCollectionObjectId = deaccessionCollectionObjectId;
    }

    /**
     *      * Number of specimens deaccessioned (necessary for lots)
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
     *      * The object being deaccessioned
     */
    public CollectionObject getCollectionObject() {
        return this.collectionObject;
    }
    
    public void setCollectionObject(CollectionObject collectionObject) {
        this.collectionObject = collectionObject;
    }

    /**
     *      * The deaccession
     */
    public Deaccession getDeaccession() {
        return this.deaccession;
    }
    
    public void setDeaccession(Deaccession deaccession) {
        this.deaccession = deaccession;
    }

    /**
     * 
     */
    public Set<LoanReturnPhysicalObject> getLoanReturnPhysicalObjects() {
        return this.loanReturnPhysicalObjects;
    }
    
    public void setLoanReturnPhysicalObjects(Set<LoanReturnPhysicalObject> loanReturnPhysicalObjects) {
        this.loanReturnPhysicalObjects = loanReturnPhysicalObjects;
    }





    // Add Methods

    public void addLoanReturnPhysicalObjects(final LoanReturnPhysicalObject loanReturnPhysicalObject)
    {
        this.loanReturnPhysicalObjects.add(loanReturnPhysicalObject);
        loanReturnPhysicalObject.setDeaccessionCollectionObject(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeLoanReturnPhysicalObjects(final LoanReturnPhysicalObject loanReturnPhysicalObject)
    {
        this.loanReturnPhysicalObjects.remove(loanReturnPhysicalObject);
        loanReturnPhysicalObject.setDeaccessionCollectionObject(null);
    }

    // Delete Add Methods
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    public Integer getTableId()
    {
        return 36;
    }

}
