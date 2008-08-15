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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "journal")
@org.hibernate.annotations.Table(appliesTo="journal", indexes =
    {   @Index (name="JournalNameIDX", columnNames={"JournalName"}),
        @Index (name="JournalGUIDIDX", columnNames={"GUID"}),
    })
public class Journal extends DataModelObjBase implements java.io.Serializable 
{

    // Fields    

     protected Integer journalId;
     protected String  journalName;
     protected String  journalAbbreviation;
     protected String  iSSN;
     protected String  text1;
     protected String  guid;
     protected String  remarks;
     protected Set<ReferenceWork> referenceWorks;


    // Constructors

    /** default constructor */
    public Journal() 
    {
        //
    }
    
    /** constructor with id */
    public Journal(Integer journalId) {
        this.journalId = journalId;
    }
   
    
    

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        journalId   = null;
        journalName = null;
        journalAbbreviation = null;
        guid        = null;
        iSSN        = null;
        text1       = null;
        remarks     = null;
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
    public Integer getJournalId() {
        return this.journalId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
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
    
    public void setJournalId(Integer journalId) {
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
     * @return the guid
     */
    @Column(name = "GUID", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getGuid()
    {
        return guid;
    }

    /**
     * @param guid the guid to set
     */
    public void setGuid(String guid)
    {
        this.guid = guid;
    }

    /**
     * @return the iSSN
     */
    @Column(name = "ISSN", unique = false, nullable = true, insertable = true, updatable = true, length = 16)
    public String getISSN()
    {
        return iSSN;
    }

    /**
     * @param issn the iSSN to set
     */
    public void setISSN(String issn)
    {
        iSSN = issn;
    }

    /**
     * @return the text1
     */
    @Column(name = "Text1", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getText1()
    {
        return text1;
    }

    /**
     * @param text1 the text1 to set
     */
    public void setText1(String text1)
    {
        this.text1 = text1;
    }

    /**
     * 
     */
    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * 
     */
    //@OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "journal")
    //@Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @OneToMany(cascade = { javax.persistence.CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "journal")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<ReferenceWork> getReferenceWorks() {
        return this.referenceWorks;
    }
    
    public void setReferenceWorks(Set<ReferenceWork> referenceWorks) {
        this.referenceWorks = referenceWorks;
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
