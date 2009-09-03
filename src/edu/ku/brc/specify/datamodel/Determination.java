/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;

@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@Table(name = "determination")
@org.hibernate.annotations.Proxy(lazy = false)
@org.hibernate.annotations.Table(appliesTo="determination", indexes =
    {   @Index (name="DeterminedDateIDX", columnNames={"DeterminedDate"}),
        @Index (name="DetMemIDX", columnNames={"CollectionMemberID"}),
        @Index (name="AlterNameIDX", columnNames={"AlternateName"}),
        @Index (name="TypeStatusNameIDX", columnNames={"TypeStatusName"})
    })
public class Determination extends CollectionMember implements java.io.Serializable, 
                                                               Comparable<Determination>,
                                                               Cloneable
{
     // Fields    
     protected Integer             determinationId;
     protected Boolean             isCurrent;
     protected String              typeStatusName;
     protected Calendar            determinedDate;
     protected Byte                determinedDatePrecision;   // Accurate to Year, Month, Day

     protected String              qualifier;
     protected String              addendum;
     protected String              confidence;
     protected String              method;
     protected String              alternateName;
     protected String              nameUsage; 
     protected String              featureOrBasis;
     protected String              remarks;
     protected String              text1;
     protected String              text2;
     protected Float               number1;
     protected Float               number2;
     protected Boolean             yesNo1;
     protected Boolean             yesNo2;
     protected Taxon               preferredTaxon; //= taxon.acceptedTaxon or taxon
     protected Taxon               taxon;
     protected CollectionObject    collectionObject;
     protected Set<DeterminationCitation> determinationCitations;
     protected Agent               determiner;
     

    // Constructors

    /** default constructor */
    public Determination() {
        //
    }
    
    /** constructor with id */
    public Determination(Integer determinationId) {
        this.determinationId = determinationId;
    }
   
    
    

    // Initializer
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        determinationId = null;
        isCurrent = false;
        typeStatusName = null;
        determinedDate = null;
        determinedDatePrecision = null;
        confidence = null;
        qualifier  = null;
        addendum = null;
        alternateName = null;
        nameUsage = null;
        method = null;
        featureOrBasis = null;
        remarks = null;
        text1 = null;
        text2 = null;
        number1 = null;
        number2 = null;
        yesNo1 = null;
        yesNo2 = null;
        preferredTaxon = null;
        taxon = null;
        collectionObject = null;
        determinationCitations = new HashSet<DeterminationCitation>();
        determiner = null;
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "DeterminationID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getDeterminationId() 
    {
        return this.determinationId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.determinationId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Determination.class;
    }
    
    public void setDeterminationId(Integer determinationId) 
    {
        this.determinationId = determinationId;
    }

    /**
     * @return current
     */
    @Column(name="IsCurrent",unique=false,nullable=false,updatable=true,insertable=true)
    public Boolean getIsCurrent() 
    {
        return this.isCurrent;
    }
    
    /**
     * @param isCurrent the isCurrent to set
     */
    public void setIsCurrent(Boolean isCurrent) 
    {
        this.isCurrent = isCurrent;
    }

    
    @Transient
    public boolean isCurrentDet()
    {
        return this.isCurrent == null ? false : this.isCurrent;
    }

    /**
     *      * e.g. 'Holotype', 'Paratype'...
     */
    @Column(name = "TypeStatusName", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getTypeStatusName() 
    {
        return this.typeStatusName;
    }
    
    public void setTypeStatusName(String typeStatusName) 
    {
        this.typeStatusName = typeStatusName;
    }

    /**
     * 
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DeterminedDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDeterminedDate() 
    {
        return this.determinedDate;
    }
    
    public void setDeterminedDate(Calendar determinedDate) 
    {
        this.determinedDate = determinedDate;
    }

    /**
     * @return the determinedDatePrecision
     */
    @Column(name = "DeterminedDatePrecision", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getDeterminedDatePrecision()
    {
        return this.determinedDatePrecision != null ? this.determinedDatePrecision : (byte)UIFieldFormatterIFace.PartialDateEnum.Full.ordinal();
    }

    /**
     * @param determinedDatePrecision the determinedDatePrecision to set
     */
    public void setDeterminedDatePrecision(Byte determinedDatePrecision)
    {
        this.determinedDatePrecision = determinedDatePrecision;
    }

    /**
     *      * Confidence of determination (value from PickList)
     */
    @Column(name = "Confidence", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getConfidence() 
    {
        return this.confidence;
    }
    
    public void setConfidence(String confidence) 
    {
        this.confidence = confidence;
    }

    /**
     * @return the qualifier
     */
    @Column(name = "Qualifier", unique = false, nullable = true, insertable = true, updatable = true, length = 16)
    public String getQualifier()
    {
        return qualifier;
    }

    /**
     * @param qualifier the qualifier to set
     */
    public void setQualifier(String qualifier)
    {
        this.qualifier = qualifier;
    }

    /**
     * @return the addendum
     */
    @Column(name = "Addendum", unique = false, nullable = true, insertable = true, updatable = true, length = 16)
    public String getAddendum()
    {
        return addendum;
    }

    /**
     * @param addendum the addendum to set
     */
    public void setAddendum(String addendum)
    {
        this.addendum = addendum;
    }

    /**
     * @return the alternateName
     */
    @Column(name = "AlternateName", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getAlternateName()
    {
        return alternateName;
    }

    /**
     * @param alternateName the alternateName to set
     */
    public void setAlternateName(String alternateName)
    {
        this.alternateName = alternateName;
    }

    /**
     * @return the nameUsage
     */
    @Column(name = "NameUsage", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getNameUsage()
    {
        return nameUsage;
    }

    /**
     * @param nameUsage the nameUsage to set
     */
    public void setNameUsage(String nameUsage)
    {
        this.nameUsage = nameUsage;
    }

    /**
     *      * Method of determination (value from PickList)
     */
    @Column(name = "Method", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getMethod() 
    {
        return this.method;
    }
    
    
    
    public void setMethod(String method) 
    {
        this.method = method;
    }

    /**
     *      * Body part, or characteristic used as the basis of a determination.
     */
    @Column(name = "FeatureOrBasis", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getFeatureOrBasis() 
    {
        return this.featureOrBasis;
    }
    
    public void setFeatureOrBasis(String featureOrBasis) 
    {
        this.featureOrBasis = featureOrBasis;
    }

    /**
     * 
     */
    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks() 
    {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) 
    {
        this.remarks = remarks;
    }

    /**
     *      * User definable
     */
    @Column(name = "Text1", length=300, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText1() 
    {
        return this.text1;
    }
    
    public void setText1(String text1) 
    {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    @Column(name = "Text2", length=300, unique = false, nullable = true, insertable = true, updatable = true)
    public String getText2() 
    {
        return this.text2;
    }
    
    public void setText2(String text2) 
    {
        this.text2 = text2;
    }

    /**
     *      * User definable
     */
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
    public Float getNumber1() 
    {
        return this.number1;
    }
    
    public void setNumber1(Float number1) 
    {
        this.number1 = number1;
    }

    /**
     *      * User definable
     */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
    public Float getNumber2() 
    {
        return this.number2;
    }
    
    public void setNumber2(Float number2) 
    {
        this.number2 = number2;
    }

    /**
     *      * User definable
     */
    @Column(name="YesNo1",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo1() 
    {
        return this.yesNo1;
    }
    
    public void setYesNo1(Boolean yesNo1) 
    {
        this.yesNo1 = yesNo1;
    }

    /**
     *      * User definable
     */
    @Column(name="YesNo2",unique=false,nullable=true,updatable=true,insertable=true)
    public Boolean getYesNo2() 
    {
        return this.yesNo2;
    }
    
    public void setYesNo2(Boolean yesNo2) 
    {
        this.yesNo2 = yesNo2;
    }

    /**
     * @return the taxon.
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "TaxonID", unique = false, nullable = true, insertable = true, updatable = true)
    public Taxon getTaxon() 
    {
        return this.taxon;
    }
    
    /**
     * @param taxon the taxon to set.
     */
    public void setTaxon(Taxon taxon) 
    {
        this.taxon = taxon;
        if (taxon == null || taxon.getIsAccepted())
        {
        	preferredTaxon = taxon;
        }
        else
        {
        	preferredTaxon = taxon.getAcceptedTaxon();
        }        
    }

    /**
     * @return the preferredTaxon.
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "PreferredTaxonID", unique = false, nullable = true, insertable = true, updatable = true)
    public Taxon getPreferredTaxon() 
    {
        return this.preferredTaxon;
    }

    /**
     * @param preferredTaxon the preferredTaxon to set.
     * 
     * This method should only be called by the taxon synonymization code.
     * 
     * setTaxon() should be used instead.
     */
    public void setPreferredTaxon(Taxon preferredTaxon)
    {
        this.preferredTaxon = preferredTaxon;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "CollectionObjectID", unique = false, nullable = false, insertable = true, updatable = true)
    public CollectionObject getCollectionObject() 
    {
        return this.collectionObject;
    }
    
    public void setCollectionObject(CollectionObject collectionObject) 
    {
        this.collectionObject = collectionObject;
    }

    /**
     * 
     */
    @OneToMany(cascade = { javax.persistence.CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "determination")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<DeterminationCitation> getDeterminationCitations() 
    {
        return this.determinationCitations;
    }
    
    public void setDeterminationCitations(Set<DeterminationCitation> determinationCitations) 
    {
        this.determinationCitations = determinationCitations;
    }

    /**
     *      * id of the Person making the determination
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "DeterminerID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getDeterminer() 
    {
        return this.determiner;
    }
    
    public void setDeterminer(Agent determiner) 
    {
        this.determiner = determiner;
    }





    // Add Methods

    public void addDeterminationCitations(final DeterminationCitation determinationCitation)
    {
        this.determinationCitations.add(determinationCitation);
        determinationCitation.setDetermination(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeDeterminationCitations(final DeterminationCitation determinationCitation)
    {
        this.determinationCitations.remove(determinationCitation);
        determinationCitation.setDetermination(null);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.FormDataObjIFace#forceLoad()
     */
    @Override
    public void forceLoad()
    {
//        DeterminationStatus ds = getStatus();
//        if (ds != null)
//        {
//            ds.getId(); // make sure it is loaded;
//        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return CollectionObject.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return collectionObject != null ? collectionObject.getId() : null;
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
        return 9;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        Determination 
        obj = (Determination)super.clone();
        obj.init();
        obj.determinationId        = null;
        obj.collectionObject       = null;
        obj.determinationCitations = new HashSet<DeterminationCitation>();
        
        for (DeterminationCitation dc : determinationCitations)
        {
            DeterminationCitation newDC = (DeterminationCitation)dc.clone();
            newDC.setDetermination(obj);
            obj.determinationCitations.add(newDC);
        }
         
        return obj;
    }

    //----------------------------------------------------------------------
    //-- Comparable Interface
    //----------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Determination obj)
    {
        int result = isCurrentDet() == obj.isCurrentDet() ? 0 : 1;
        if (result == 1)
        {
            return isCurrentDet() ? -1 : 1;
        }
        //else
        if (determinedDate != null && obj != null && obj.determinedDate != null)
        {
            return obj.determinedDate.compareTo(determinedDate); //reverse order- recent first
        }
        // else
        return obj.timestampCreated.compareTo(timestampCreated); //reverse order- recent first
    }


}
