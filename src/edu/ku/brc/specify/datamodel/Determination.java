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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;


import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;




/**

 */
@Entity
@Table(name = "determination")
public class Determination extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long determinationId;
     protected DeterminationStatus status;
     protected String typeStatusName;
     protected Calendar determinedDate;
     protected String confidence;
     protected String method;
     protected String featureOrBasis;
     protected String remarks;
     protected String text1;
     protected String text2;
     protected Float number1;
     protected Float number2;
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     protected Taxon taxon;
     protected CollectionObject collectionObject;
     protected Set<DeterminationCitation> determinationCitations;
     protected Agent determiner;


    // Constructors

    /** default constructor */
    public Determination() {
        //
    }
    
    /** constructor with id */
    public Determination(Long determinationId) {
        this.determinationId = determinationId;
    }
   
    
    

    // Initializer
    @Override
    public void initialize()
    {
        determinationId = null;
        status = null;
        typeStatusName = null;
        determinedDate = null;
        confidence = null;
        method = null;
        featureOrBasis = null;
        remarks = null;
        text1 = null;
        text2 = null;
        number1 = null;
        number2 = null;
        timestampCreated = new Date();
        timestampModified = null;
        lastEditedBy = null;
        yesNo1 = null;
        yesNo2 = null;
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
    public Long getDeterminationId() 
    {
        return this.determinationId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
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
    
    public void setDeterminationId(Long determinationId) 
    {
        this.determinationId = determinationId;
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE })
    @JoinColumn(name = "DeterminationStatusID", unique = false, nullable = false, insertable = true, updatable = true)
    public DeterminationStatus getStatus() 
    {
        return this.status;
    }
    
    public void setStatus(DeterminationStatus status) 
    {
        this.status = status;
    }
    
    @Transient
    public boolean isCurrent()
    {
    	//TODO: is this a final implementation?
    	// XXX: is this good for production
    	// What about i18n?
    	return (status.getName().equalsIgnoreCase("current") ? true : false );
    	
    	// perhaps this is better (for i18n reasons)
    	// return (status.getDeterminationStatusId() == 2 ) ? true : false;
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
    @Column(name = "Remarks", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
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
    @Column(name = "Text1", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
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
    @Column(name = "Text2", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
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
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE })
    @JoinColumn(name = "TaxonID", unique = false, nullable = true, insertable = true, updatable = true)
    public Taxon getTaxon() 
    {
        return this.taxon;
    }
    
    public void setTaxon(Taxon taxon) 
    {
        this.taxon = taxon;
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
    @Cascade( { CascadeType.SAVE_UPDATE })
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

    // Delete Add Methods
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public Integer getTableId()
    {
        return 9;
    }

    @Override
    @Transient
    public String getIdentityTitle()
    {
        // TODO Auto-generated method stub
        return "Determination: " + this.getTaxon().getName();
    }

}
