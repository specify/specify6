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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.HashSet;
import java.util.Set;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@Table(name = "journal")
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
        //
    }
    
    /** constructor with id */
    public Journal(Long journalId) {
        this.journalId = journalId;
    }
   
    
    

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        journalId = null;
        journalName = null;
        journalAbbreviation = null;
        remarks = null;
        referenceWorks = new HashSet<ReferenceWork>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "JournalID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getJournalId() {
        return this.journalId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.journalId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Journal.class;
    }
    
    public void setJournalId(Long journalId) {
        this.journalId = journalId;
    }

    /**
     *      * Full name of the journal
     */
    @Column(name = "JournalName", unique = false, nullable = true, insertable = true, updatable = true)
    public String getJournalName() {
        return this.journalName;
    }
    
    public void setJournalName(String journalName) {
        this.journalName = journalName;
    }

    /**
     * 
     */
    @Column(name = "JournalAbbreviation", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getJournalAbbreviation() {
        return this.journalAbbreviation;
    }
    
    public void setJournalAbbreviation(String journalAbbreviation) {
        this.journalAbbreviation = journalAbbreviation;
    }

    /**
     * 
     */
    @Column(name = "Remarks", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "journal")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
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
        return 51;
    }

}
