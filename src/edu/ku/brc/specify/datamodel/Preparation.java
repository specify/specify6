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
import java.util.HashSet;
import java.util.Set;

import edu.ku.brc.dbsupport.AttributeIFace;




/**

 */
public class Preparation extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long preparationId;
     protected String text1;
     protected String text2;
     protected Integer count;
     protected String storageLocation;
     protected String remarks;
     protected Calendar preparedDate;
     protected Set<LoanPhysicalObject> loanPhysicalObjects;
     protected Set<AttributeIFace> attrs;
     protected PrepType prepType;
     protected CollectionObject collectionObject;
     protected Agent preparedByAgent;
     protected Location location;
     protected Set<Attachment>          attachments;


    // Constructors

    /** default constructor */
    public Preparation() {
        // do nothing
    }
    
    /** constructor with id */
    public Preparation(Long preparationId) {
        this.preparationId = preparationId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        preparationId = null;
        text1 = null;
        text2 = null;
        count = null;
        storageLocation = null;
        remarks = null;
        preparedDate = null;
        timestampCreated = new Date();
        timestampModified = null;
        lastEditedBy = null;
        loanPhysicalObjects = new HashSet<LoanPhysicalObject>();
        attrs = new HashSet<AttributeIFace>();
        prepType = null;
        collectionObject = null;
        preparedByAgent = null;
        location = null;
        attachments = new HashSet<Attachment>();
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Long getPreparationId() {
        return this.preparationId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.preparationId;
    }
    
    public void setPreparationId(Long preparationId) {
        this.preparationId = preparationId;
    }

    /**
     *      * User definable
     */
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    public String getText2() {
        return this.text2;
    }
    
    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      * The number of objects (specimens, slides, pieces) prepared
     */
    public Integer getCount() {
        return this.count;
    }
    
    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * 
     */
    public String getStorageLocation() {
        return this.storageLocation;
    }
    
    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
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
     * 
     */
    public Calendar getPreparedDate() {
        return this.preparedDate;
    }
    
    public void setPreparedDate(Calendar preparedDate) {
        this.preparedDate = preparedDate;
    }

    /**
     * 
     */
    public Set<LoanPhysicalObject> getLoanPhysicalObjects() {
        return this.loanPhysicalObjects;
    }
    
    public void setLoanPhysicalObjects(Set<LoanPhysicalObject> loanPhysicalObjects) {
        this.loanPhysicalObjects = loanPhysicalObjects;
    }

    /**
     * 
     */
    public Set<AttributeIFace> getAttrs() {
        return this.attrs;
    }
    
    public void setAttrs(Set<AttributeIFace> attrs) {
        this.attrs = attrs;
    }

    /**
     * 
     */
    public PrepType getPrepType() {
        return this.prepType;
    }
    
    public void setPrepType(PrepType prepType) {
        this.prepType = prepType;
    }

    /**
     * 
     */
    public CollectionObject getCollectionObject() {
        return this.collectionObject;
    }
    
    public void setCollectionObject(CollectionObject collectionObject) {
        this.collectionObject = collectionObject;
    }

    /**
     * 
     */
    public Agent getPreparedByAgent() {
        return this.preparedByAgent;
    }
    
    public void setPreparedByAgent(Agent preparedByAgent) {
        this.preparedByAgent = preparedByAgent;
    }

    public Set<Attachment> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(Set<Attachment> attachments)
    {
        this.attachments = attachments;
    }

    /**
     * 
     */
    public Location getLocation() {
        return this.location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }

 

    // Add Methods

    public void addLoanPhysicalObjects(final LoanPhysicalObject loanPhysicalObject)
    {
        this.loanPhysicalObjects.add(loanPhysicalObject);
        loanPhysicalObject.setPreparation(this);
    }

    public void addAttrs(final PreparationAttr attr)
    {
        this.attrs.add(attr);
        attr.setPreparation(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeLoanPhysicalObjects(final LoanPhysicalObject loanPhysicalObject)
    {
        this.loanPhysicalObjects.remove(loanPhysicalObject);
        loanPhysicalObject.setPreparation(null);
    }

    public void removeAttrs(final PreparationAttr attr)
    {
        this.attrs.remove(attr);
        attr.setPreparation(null);
    }

    // Delete Add Methods
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    public Integer getTableId()
    {
        return 63;
    }

}
