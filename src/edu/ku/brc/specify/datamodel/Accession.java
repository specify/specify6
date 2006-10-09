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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.ui.forms.FormDataObjIFace;




/**

 */
public class Accession extends DataModelObjBase implements java.io.Serializable {

    // Fields

     protected Long accessionId;
     protected String type;
     protected String status;
     protected String number;
     protected String verbatimDate;
     protected Calendar dateAccessioned;
     protected Calendar dateReceived;
     protected String text1;
     protected String text2;
     protected String text3;
     protected Float number1;
     protected Float number2;
     protected String remarks;
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     protected Set<CollectionObject> collectionObjects;
     protected Set<AccessionAuthorizations> accessionAuthorizations;
     protected Set<AccessionAgents> accessionAgents;
     protected RepositoryAgreement repositoryAgreement;


    // Constructors

    /** default constructor */
    public Accession()
    {
        // do nothing
    }

    /** constructor with id */
    public Accession(Long accessionId) 
    {
        this.accessionId = accessionId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        accessionId = null;
        type = null;
        status = null;
        number = null;
        verbatimDate = null;
        dateAccessioned = null;
        dateReceived = null;
        text1 = null;
        text2 = null;
        text3 = null;
        number1 = null;
        number2 = null;
        remarks = null;
        timestampCreated = new Date();
        timestampModified = null;
        lastEditedBy = null;
        yesNo1 = null;
        yesNo2 = null;
        collectionObjects = new HashSet<CollectionObject>();
        accessionAuthorizations = new HashSet<AccessionAuthorizations>();
        accessionAgents = new HashSet<AccessionAgents>();
        repositoryAgreement = null;
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    public Long getAccessionId() {
        return this.accessionId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    public Long getId()
    {
        return this.accessionId;
    }

    public void setAccessionId(Long accessionId) {
        this.accessionId = accessionId;
    }

    /**
     *      * Source of Accession, e.g. 'Collecting', 'Gift',  'Bequest' ...
     */
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     *      * Status of Accession, e.g. 'In process', 'Complete' ...
     */
    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * A user-visible identifier of the Accession. Typically an integer, but may include alphanumeric characters as prefix, suffix, and separators
     */
    public String getNumber() {
        return this.number;
    }

    public void setNumber(final String number) 
    {
        firePropertyChange("Number", this.number, number);
        this.number = number;
    }

    /**
     *      * accomodates historical accessions.
     */
    public String getVerbatimDate() {
        return this.verbatimDate;
    }

    public void setVerbatimDate(String verbatimDate) {
        this.verbatimDate = verbatimDate;
    }

    /**
     *      * Date of Accession
     */
    public Calendar getDateAccessioned() {
        return this.dateAccessioned;
    }

    public void setDateAccessioned(Calendar dateAccessioned) {
        this.dateAccessioned = dateAccessioned;
    }

    /**
     *      * Date material was received
     */
    public Calendar getDateReceived() {
        return this.dateReceived;
    }

    public void setDateReceived(Calendar dateReceived) {
        this.dateReceived = dateReceived;
    }

    /**
     *      * User definable
     */
    public String getText1() {
        return this.text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    public String getText2() {
        return this.text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      * User definable
     */
    public String getText3() {
        return this.text3;
    }

    public void setText3(String text3) {
        this.text3 = text3;
    }

    /**
     *      * User definable
     */
    public Float getNumber1() {
        return this.number1;
    }

    public void setNumber1(Float number1) {
        this.number1 = number1;
    }

    /**
     *      * User definable
     */
    public Float getNumber2() {
        return this.number2;
    }

    public void setNumber2(Float number2) {
        this.number2 = number2;
    }

    /**
     *      * Comments
     */
    public String getRemarks() {
        return this.remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *      * User definable
     */
    public Boolean getYesNo1() {
        return this.yesNo1;
    }

    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      * User definable
     */
    public Boolean getYesNo2() {
        return this.yesNo2;
    }

    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     *
     */
    public Set<CollectionObject> getCollectionObjects() {
        return this.collectionObjects;
    }

    public void setCollectionObjects(Set<CollectionObject> collectionObjects) {
        this.collectionObjects = collectionObjects;
    }

    /**
     *
     */
    public Set<AccessionAuthorizations> getAccessionAuthorizations() {
        return this.accessionAuthorizations;
    }

    public void setAccessionAuthorizations(Set<AccessionAuthorizations> accessionAuthorizations) {
        this.accessionAuthorizations = accessionAuthorizations;
    }

    /**
     *
     */
    public Set<AccessionAgents> getAccessionAgents() {
        return this.accessionAgents;
    }

    public void setAccessionAgents(Set<AccessionAgents> accessionAgents) {
        this.accessionAgents = accessionAgents;
    }


    /**
     * RepositoryAgreement for this Accession
     */
    public RepositoryAgreement getRepositoryAgreement() {
        return this.repositoryAgreement;
    }
    
    public void setRepositoryAgreement(RepositoryAgreement repositoryAgreement) {
        this.repositoryAgreement = repositoryAgreement;
    }



    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#addReference(edu.ku.brc.ui.forms.FormDataObjIFace, java.lang.String)
     */
    public void addReference(FormDataObjIFace ref, String refType)
    {
        System.err.println(ref+" "+ refType);
        if (StringUtils.isNotEmpty(refType))
        {
            if (refType.equals("collectionObjects"))
            {
                if (ref instanceof CollectionObject)
                {
                    collectionObjects.add((CollectionObject)ref);
                    ((CollectionObject)ref).setAccession(this);
                    
                } else
                {
                    throw new RuntimeException("ref ["+ref.getClass().getSimpleName()+"] is not an instance of CollectionObject");
                }
                
            } else if (refType.equals("accessionAuthorizations"))
            {
                if (ref instanceof AccessionAuthorizations)
                {
                    accessionAuthorizations.add((AccessionAuthorizations)ref);
                    ((AccessionAuthorizations)ref).setAccession(this);
                    
                } else
                {
                    throw new RuntimeException("ref ["+ref.getClass().getSimpleName()+"] is not an instance of AccessionAuthorizations");
                }
                
            } else if (refType.equals("accessionAgents"))
            {
                if (ref instanceof AccessionAgents)
                {
                    accessionAgents.add((AccessionAgents)ref);
                    ((AccessionAgents)ref).setAccession(this);
                    
                } else
                {
                    throw new RuntimeException("ref ["+ref.getClass().getSimpleName()+"] is not an instance of AccessionAgents");
                }
                
            } else if (refType.equals("repositoryAgreement"))
            {
                if (ref instanceof RepositoryAgreement)
                {
                    repositoryAgreement = (RepositoryAgreement)ref;
                    ((RepositoryAgreement)ref).getAccessions().add(this);
                    
                } else
                {
                    throw new RuntimeException("ref ["+ref.getClass().getSimpleName()+"] is not an instance of RepositoryAgreement");
                }
            }
        } else
        {
            throw new RuntimeException("Adding Object ["+ref.getClass().getSimpleName()+"] and the refType is null.");
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#removeReference(edu.ku.brc.ui.forms.FormDataObjIFace, java.lang.String)
     */
    public void removeReference(FormDataObjIFace ref, String refType)
    {
        System.err.println(ref+" "+ refType);
        
        if (StringUtils.isNotEmpty(refType))
        {
            if (refType.equals("collectionObjects"))
            {
                if (ref instanceof CollectionObject)
                {
                    collectionObjects.remove(ref);
                    ((CollectionObject)ref).setAccession(null);
                    
                } else
                {
                    throw new RuntimeException("ref ["+ref.getClass().getSimpleName()+"] is not an instance of CollectionObject");
                }
                
            } else if (refType.equals("accessionAuthorizations"))
            {
                if (ref instanceof AccessionAuthorizations)
                {
                    accessionAuthorizations.remove(ref);
                    ((AccessionAuthorizations)ref).setAccession(null);
                    
                } else
                {
                    throw new RuntimeException("ref ["+ref.getClass().getSimpleName()+"] is not an instance of AccessionAuthorizations");
                }
                
            } else if (refType.equals("accessionAgents"))
            {
                if (ref instanceof AccessionAgents)
                {
                    accessionAgents.remove(ref);
                    ((AccessionAgents)ref).setAccession(null);
                    
                } else
                {
                    throw new RuntimeException("ref ["+ref.getClass().getSimpleName()+"] is not an instance of AccessionAgents");
                }
                
            } else if (refType.equals("repositoryAgreement"))
            {
                if (ref instanceof RepositoryAgreement)
                {
                    repositoryAgreement = null;
                    ((RepositoryAgreement)ref).getAccessions().remove(this);
                    
                } else
                {
                    throw new RuntimeException("ref ["+ref.getClass().getSimpleName()+"] is not an instance of RepositoryAgreement");
                }
            }
        } else
        {
            throw new RuntimeException("Removing Object ["+ref.getClass().getSimpleName()+"] and the refType is null.");
        }
    }

    //---------------------------------------------------------------------------
    // Overrides DataModelObjBase
    //---------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    public String getIdentityTitle()
    {
        return number != null ? number : super.getIdentityTitle();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    public Integer getTableId()
    {
        return 7;
    }
}
