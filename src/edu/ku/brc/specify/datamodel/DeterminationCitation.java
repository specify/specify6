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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;


import java.util.Date;




/**

 */
@Entity
@Table(name = "determinationcitation", uniqueConstraints = { @UniqueConstraint(columnNames = { "ReferenceWorkID", "DeterminationID" }) })
public class DeterminationCitation extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long determinationCitationId;
     protected String remarks;
     protected ReferenceWork referenceWork;
     protected Determination determination;


    // Constructors

    /** default constructor */
    public DeterminationCitation() {
        //
    }
    
    /** constructor with id */
    public DeterminationCitation(Long determinationCitationId) {
        this.determinationCitationId = determinationCitationId;
    }
   
    
    

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        determinationCitationId = null;
        remarks = null;
        referenceWork = null;
        determination = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "DeterminationCitationID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getDeterminationCitationId() {
        return this.determinationCitationId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.determinationCitationId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return DeterminationCitation.class;
    }
    
    public void setDeterminationCitationId(Long determinationCitationId) {
        this.determinationCitationId = determinationCitationId;
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
     *      * ID of the publication citing the determination
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE })
    @JoinColumn(name = "ReferenceWorkID", unique = false, nullable = false, insertable = true, updatable = true)
    public ReferenceWork getReferenceWork() {
        return this.referenceWork;
    }
    
    public void setReferenceWork(ReferenceWork referenceWork) {
        this.referenceWork = referenceWork;
    }

    /**
     *      * Determination being cited
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE })
    @JoinColumn(name = "DeterminationID", unique = false, nullable = false, insertable = true, updatable = true)
    public Determination getDetermination() {
        return this.determination;
    }
    
    public void setDetermination(Determination determination) {
        this.determination = determination;
    }





    // Add Methods

    // Done Add Methods

    // Delete Methods

    // Delete Add Methods
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public Integer getTableId()
    {
        return 38;
    }

}
