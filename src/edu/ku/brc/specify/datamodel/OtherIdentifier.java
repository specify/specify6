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




/**

 */
public class OtherIdentifier extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long otherIdentifierId;
     protected String identifier;
     protected String remarks;
     protected CollectionObject collectionObject;


    // Constructors

    /** default constructor */
    public OtherIdentifier() {
    }
    
    /** constructor with id */
    public OtherIdentifier(Long otherIdentifierId) {
        this.otherIdentifierId = otherIdentifierId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        otherIdentifierId = null;
        identifier = null;
        remarks = null;
        timestampModified = null;
        timestampCreated = new Date();
        lastEditedBy = null;
        collectionObject = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Long getOtherIdentifierId() {
        return this.otherIdentifierId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.otherIdentifierId;
    }
    
    public void setOtherIdentifierId(Long otherIdentifierId) {
        this.otherIdentifierId = otherIdentifierId;
    }

    /**
     * 
     */
    public String getIdentifier() {
        return this.identifier;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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
     *      * ID of object identified by Identifier
     */
    public CollectionObject getCollectionObject() {
        return this.collectionObject;
    }
    
    public void setCollectionObject(CollectionObject collectionObject) {
        this.collectionObject = collectionObject;
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
        return 61;
    }

}
