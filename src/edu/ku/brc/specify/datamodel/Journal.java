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
public class Journal extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long journalId;
     protected String journalName;
     protected String journalAbbreviation;
     protected String remarks;
     protected Set<ReferenceWork> referenceWorks;


    // Constructors

    /** default constructor */
    public Journal() {
    }
    
    /** constructor with id */
    public Journal(Long journalId) {
        this.journalId = journalId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        journalId = null;
        journalName = null;
        journalAbbreviation = null;
        remarks = null;
        timestampCreated = new Date();
        timestampModified = null;
        lastEditedBy = null;
        referenceWorks = new HashSet<ReferenceWork>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    public Long getJournalId() {
        return this.journalId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.journalId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    public Class getDataClass()
    {
        return Journal.class;
    }
    
    public void setJournalId(Long journalId) {
        this.journalId = journalId;
    }

    /**
     *      * Full name of the journal
     */
    public String getJournalName() {
        return this.journalName;
    }
    
    public void setJournalName(String journalName) {
        this.journalName = journalName;
    }

    /**
     * 
     */
    public String getJournalAbbreviation() {
        return this.journalAbbreviation;
    }
    
    public void setJournalAbbreviation(String journalAbbreviation) {
        this.journalAbbreviation = journalAbbreviation;
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
    public Set<ReferenceWork> getReferenceWorks() {
        return this.referenceWorks;
    }
    
    public void setReferenceWorks(Set<ReferenceWork> referenceWorks) {
        this.referenceWorks = referenceWorks;
    }





    // Add Methods

    public void addReferenceWorks(final ReferenceWork referenceWork)
    {
        this.referenceWorks.add(referenceWork);
        referenceWork.setJournal(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeReferenceWorks(final ReferenceWork referenceWork)
    {
        this.referenceWorks.remove(referenceWork);
        referenceWork.setJournal(null);
    }

    // Delete Add Methods
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    public Integer getTableId()
    {
        return 51;
    }

}
