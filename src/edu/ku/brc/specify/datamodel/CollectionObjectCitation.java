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
public class CollectionObjectCitation extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long collectionObjectCitationId;
     protected String remarks;
     protected ReferenceWork referenceWork;
     protected CollectionObject collectionObject;


    // Constructors

    /** default constructor */
    public CollectionObjectCitation() {
    }
    
    /** constructor with id */
    public CollectionObjectCitation(Long collectionObjectCitationId) {
        this.collectionObjectCitationId = collectionObjectCitationId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        collectionObjectCitationId = null;
        remarks = null;
        timestampModified = null;
        timestampCreated = new Date();
        lastEditedBy = null;
        referenceWork = null;
        collectionObject = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Long getCollectionObjectCitationId() {
        return this.collectionObjectCitationId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.collectionObjectCitationId;
    }
    
    public void setCollectionObjectCitationId(Long collectionObjectCitationId) {
        this.collectionObjectCitationId = collectionObjectCitationId;
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
     *      * The associated reference
     */
    public ReferenceWork getReferenceWork() {
        return this.referenceWork;
    }
    
    public void setReferenceWork(ReferenceWork referenceWork) {
        this.referenceWork = referenceWork;
    }

    /**
     *      * Biological Object cited
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
}
