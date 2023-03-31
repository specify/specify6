/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

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

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.specify.conversion.BasicSQLUtils;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 16, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "appraisal")
@org.hibernate.annotations.Table(appliesTo="appraisal", indexes =
    {   @Index (name="AppraisalNumberIDX", columnNames={"AppraisalNumber"}),
        @Index (name="AppraisalDateIDX", columnNames={"AppraisalDate"})
    })
public class Appraisal extends DataModelObjBase
{
    protected Integer    appraisalId;
    protected Calendar   appraisalDate;
    protected String     appraisalNumber;
    protected BigDecimal appraisalValue;
    protected String     monetaryUnitType;
    protected String     notes;
    
    protected Set<CollectionObject> collectionObjects;
    protected Accession             accession;
    protected Agent                 agent;
    
    
    /**
     * 
     */
    public Appraisal()
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        
        appraisalId       = null;
        appraisalDate     = null;
        appraisalNumber   = null;
        appraisalValue    = null;
        monetaryUnitType  = null;
        notes             = null;
        agent             = null;
        
        collectionObjects = new HashSet<CollectionObject>();
        accession         = null;

    }
    
    /**
     * @param appraisalId the appraisalId to set
     */
    public void setAppraisalId(Integer appraisalId)
    {
        this.appraisalId = appraisalId;
    }

    /**
     * @param appraisalDate the appraisalDate to set
     */
    public void setAppraisalDate(Calendar appraisalDate)
    {
        this.appraisalDate = appraisalDate;
    }

    /**
     * @param appraisalNumber the appraisalNumber to set
     */
    public void setAppraisalNumber(String appraisalNumber)
    {
        this.appraisalNumber = appraisalNumber;
    }

    /**
     * @param appraisalValue the appraisalValue to set
     */
    public void setAppraisalValue(BigDecimal appraisalValue)
    {
        this.appraisalValue = appraisalValue;
    }

    /**
     * @param monetaryUnitType the monetaryUnitType to set
     */
    public void setMonetaryUnitType(String monetaryUnitType)
    {
        this.monetaryUnitType = monetaryUnitType;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes)
    {
        this.notes = notes;
    }

    /**
     * @param collectionObjects the collectionObjects to set
     */
    public void setCollectionObjects(Set<CollectionObject> collectionObjects)
    {
        this.collectionObjects = collectionObjects;
    }

    /**
     * @param accessions the accessions to set
     */
    public void setAccession(Accession accession)
    {
        this.accession = accession;
    }

    /**
     * @return the appraisalId
     */
    @Id
    @GeneratedValue
    @Column(name = "AppraisalID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getAppraisalId()
    {
        return appraisalId;
    }

    /**
     * @return the appraisalDate
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "AppraisalDate", unique = false, nullable = false, insertable = true, updatable = true)
    public Calendar getAppraisalDate()
    {
        return appraisalDate;
    }

    /**
     * @return the appraisalNumber
     */
    @Column(name = "AppraisalNumber", unique = true, nullable = false, insertable = true, updatable = true, length = 64)
    public String getAppraisalNumber()
    {
        return appraisalNumber;
    }

    /**
     * @return the appraisalValue
     */
    @Column(name = "AppraisalValue", unique = false, nullable = true, insertable = true, updatable = true, precision = 12, scale = 2)
    public BigDecimal getAppraisalValue()
    {
        return appraisalValue;
    }

    /**
     * @return the monetaryUnitType
     */
    @Column(name = "MonetaryUnitType", unique = false, nullable = true, insertable = true, updatable = true, length = 8)
    public String getMonetaryUnitType()
    {
        return monetaryUnitType;
    }

    /**
     * @return the notes
     */
    @Lob
    @Column(name = "Notes", unique = false, nullable = true, insertable = true, updatable = true, length = 2048)
    public String getNotes()
    {
        return notes;
    }

    /**
     * @return the collectionObjects
     */
    @OneToMany(cascade = {}, mappedBy = "appraisal")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<CollectionObject> getCollectionObjects()
    {
        return collectionObjects;
    }

    /**
     * @return the accession
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "AccessionID", unique = false, nullable = true, insertable = true, updatable = true)
    public Accession getAccession()
    {
        return accession;
    }

    /**
     * @return the agent
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AgentID", unique = false, nullable = false, insertable = true, updatable = true)
    public Agent getAgent()
    {
        return agent;
    }

    /**
     * @param agent the agent to set
     */
    public void setAgent(Agent agent)
    {
        this.agent = agent;
    }

    //---------------------------------------------------------------------------
    // Overrides DataModelObjBase
    //---------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return Appraisal.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return appraisalId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        if (accession != null)
        {
            parentTblId = Accession.getClassTableId();
            return accession.getId();
        } 
        
        Vector<Object> ids = BasicSQLUtils.querySingleCol("SELECT CollectionObjectID FROM collectionobject WHERE AppraisalID = "+ appraisalId);
        if (ids.size() == 1)
        {
            parentTblId = CollectionObject.getClassTableId();
            return (Integer)ids.get(0);
        }
        parentTblId = null;
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        return StringUtils.isNotEmpty(appraisalNumber) ? appraisalNumber : super.getIdentityTitle();
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
