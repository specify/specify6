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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**
 * 
 * 
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Oct 16, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "appraisal")
public class Appraisal extends DataModelObjBase implements java.io.Serializable
{
    // Fields    
    protected Integer         appraisalId;
    protected Calendar        appraisalDate;
    protected String          appraisalNumber;
    protected BigDecimal      appraisalValue;
    protected String          notes;
    protected Set<Collection> collectionObjects;
    protected Set<Accession>  accessions;


    // Constructors

    /** default constructor */
    public Appraisal()
    {
        //
    }

    /** constructor with id */
    public Appraisal(Integer appraisalId)
    {
        this.appraisalId = appraisalId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        appraisalId        = null;
        appraisalDate      = null;
        appraisalNumber    = null;
        appraisalValue     = null;
        notes              = null;
        collectionObjects  = new HashSet<Collection>();
        accessions         = new HashSet<Accession>();

    }

    // End Initializer

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "AppraisalID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getConservEventId()
    {
        return this.appraisalId;
    }

    public void setAppraisalId(Integer appraisalId)
    {
        this.appraisalId = appraisalId;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "AppraisalID", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getAppraisalId()
    {
        return this.appraisalId;
    }

    public void setAppraisalId(final Integer appraisalId)
    {
        this.appraisalId = appraisalId;
    }

    /**
     *
     */
    @Temporal(TemporalType.DATE)
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "AppraisalID", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getAppraisalDate()
    {
        return this.appraisalDate;
    }

    public void setAppraisalDate(final Calendar appraisalDate)
    {
        this.appraisalDate = appraisalDate;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "AppraisalID", unique = false, nullable = true, insertable = true, updatable = true)
    public String getAppraisalNumber()
    {
        return this.appraisalNumber;
    }

    public void setAppraisalNumber(final String appraisalNumber)
    {
        this.appraisalNumber = appraisalNumber;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "AppraisalID", unique = false, nullable = true, insertable = true, updatable = true)
    public BigDecimal getAppraisalValue()
    {
        return this.appraisalValue;
    }

    public void setAppraisalValue(final BigDecimal appraisalValue)
    {
        this.appraisalValue = appraisalValue;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "AppraisalID", unique = false, nullable = true, insertable = true, updatable = true)
    public String getNotes()
    {
        return this.notes;
    }

    public void setNotes(final String notes)
    {
        this.notes = notes;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "collectionObject")
    public Set<Collection> getCollectionObjects()
    {
        return this.collectionObjects;
    }

    public void setCollectionObjects(final Set<Collection> collectionObjects)
    {
        this.collectionObjects = collectionObjects;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "accession")
    public Set<Accession> getAccessions()
    {
        return this.accessions;
    }

    public void setAccessions(final Set<Accession> accessions)
    {
        this.accessions = accessions;
    }


    
    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.appraisalId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Appraisal.class;
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
        return 67;
    }

}
/* ---------------------------------------------


    // Copy this Code into Class Collection

    protected Appraisal appraisal;


    // Copy this Code into Class Accession

    protected Appraisal  appraisal;


    // Copy this Code into Class Collection

        appraisal  = null;


    // Copy this Code into Class Accession

        appraisal         = null;


    // Copy this Code into Class Collection

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "collectionObjects")
    public Appraisal getAppraisal()
    {
        return this.appraisal;
    }

    public void setAppraisal(final Appraisal appraisal)
    {
        this.appraisal = appraisal;
    }



    // Copy this Code into Class Accession

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "accessions")
    public Appraisal getAppraisal()
    {
        return this.appraisal;
    }

    public void setAppraisal(final Appraisal appraisal)
    {
        this.appraisal = appraisal;
    }


*/