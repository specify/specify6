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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@Table(name = "localitycitation", uniqueConstraints = { @UniqueConstraint(columnNames = { "ReferenceWorkID", "LocalityID" }) })
public class LocalityCitation extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long localityCitationId;
     protected String remarks;
     protected ReferenceWork referenceWork;
     protected Locality locality;


    // Constructors

    /** default constructor */
    public LocalityCitation() {
        //
    }
    
    /** constructor with id */
    public LocalityCitation(Long localityCitationId) {
        this.localityCitationId = localityCitationId;
    }
   
    
    

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        localityCitationId = null;
        remarks = null;
        referenceWork = null;
        locality = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "LocalityCitationID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getLocalityCitationId() {
        return this.localityCitationId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.localityCitationId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return LocalityCitation.class;
    }
    
    public void setLocalityCitationId(Long localityCitationId) {
        this.localityCitationId = localityCitationId;
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
     *      * ID of work citing locality
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "ReferenceWorkID", unique = false, nullable = false, insertable = true, updatable = true)
    public ReferenceWork getReferenceWork() {
        return this.referenceWork;
    }
    
    public void setReferenceWork(ReferenceWork referenceWork) {
        this.referenceWork = referenceWork;
    }

    /**
     *      * ID of locality cited
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "LocalityID", unique = false, nullable = false, insertable = true, updatable = true)
    public Locality getLocality() {
        return this.locality;
    }
    
    public void setLocality(Locality locality) {
        this.locality = locality;
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
        return 57;
    }

}
