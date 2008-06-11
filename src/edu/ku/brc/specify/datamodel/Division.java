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
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;



/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jun 14, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "division")
@org.hibernate.annotations.Table(appliesTo="division", indexes =
    {   @Index (name="DivisionNameIDX", columnNames={"Name"})
    })
public class Division extends DataModelObjBase implements java.io.Serializable 
{
    // Fields    
     protected Integer                  divisionId;
     protected String                   name;
     protected String                   title;
     protected String                   abbrev;
     protected String                   uri;
     protected String                   iconURI;
     protected String                   discipline;
     protected String                   description;
     protected String                   remarks;
     
     protected Address                  address;
     protected Institution              institution;
     protected Set<Discipline>          disciplines;
     
     protected Set<Agent>               members;
     protected Set<ConservDescription>  conservDescriptions;
     protected Set<Loan>                loans;
     protected Set<TreatmentEvent>      treatmentEvents;
     protected Set<Accession>           accessions;
     protected Set<RepositoryAgreement> repositoryAgreements;
     
     protected static Division          currentDivision = null;

    // Constructors

    /** default constructor */
    public Division() 
    {
    }
    
    /** constructor with id */
    public Division(Integer divisionId) 
    {
        this.divisionId = divisionId;
    }
   
    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        
        divisionId          = null;
        name                = null;
        title               = null;
        abbrev              = null;
        uri                 = null;
        iconURI             = null;
        discipline          = null;
        description         = null;
        remarks             = null;
        members             = new HashSet<Agent>();
        conservDescriptions = new HashSet<ConservDescription>();
        loans               = new HashSet<Loan>();
        treatmentEvents     = new HashSet<TreatmentEvent>();
        accessions          = new HashSet<Accession>();
        repositoryAgreements = new HashSet<RepositoryAgreement>();
        institution         = null;
        address             = null;
        disciplines         = new HashSet<Discipline>();
    }
    
    /**
     * @return the divisionId
     */
    @Id
    @GeneratedValue
    @Column(name = "DivisionID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getDivisionId()
    {
        return divisionId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return divisionId;
    }
    
    /**
     * @return the abbrev
     */
    @Column(name = "Abbrev", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getAbbrev()
    {
        return abbrev;
    }

    /**
     * @return the iconURI
     */
    @Column(name = "IconURI", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getIconURI()
    {
        return iconURI;
    }

    /**
     * DisciplineType.
     * @return the ipr
     */
    @Column(name = "DisciplineType", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getDiscipline()
    {
        return discipline;
    }

    /**
     * @return the name
     */
    @Column(name = "Name", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getName()
    {
        return name;
    }

    /**
     * @return the remarks
     */
    @Lob
    @Column(name = "Remarks", length = 8192)
    public String getRemarks()
    {
        return remarks;
    }

    /**
     * @return the description
     */
    @Lob
    @Column(name = "Description", length = 8192)
    public String getDescription()
    {
        return description;
    }

    /**
     * @return the members
     */
    @OneToMany(cascade = { javax.persistence.CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "division")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<Agent> getMembers()
    {
        return members;
    }

    /**
     * @return the treatmentEvents
     */
    @OneToMany(cascade = { javax.persistence.CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "division")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<TreatmentEvent> getTreatmentEvents()
    {
        return treatmentEvents;
    }

    /**
     * @return the accessions
     */
    @OneToMany(cascade = { javax.persistence.CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "division")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<Accession> getAccessions()
    {
        return accessions;
    }

    /**
     * @return the repositoryAgreements
     */
    @OneToMany(cascade = { javax.persistence.CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "division")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<RepositoryAgreement> getRepositoryAgreements()
    {
        return repositoryAgreements;
    }

    /**
     * @return the title
     */
    @Column(name = "Title", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getTitle()
    {
        return title;
    }

    /**
     * @return the uri
     */
    @Column(name = "Uri", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getUri()
    {
        return uri;
    }

    /**
     * @param abbrev the abbrev to set
     */
    @Column(name = "Abbrev", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public void setAbbrev(String abbrev)
    {
        this.abbrev = abbrev;
    }

    /**
     *
     */
    @OneToMany(cascade = { }, fetch = FetchType.LAZY, mappedBy = "division")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<ConservDescription> getConservDescriptions()
    {
        return this.conservDescriptions;
    }
    
    /**
     * @return the address
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AddressID", unique = false, nullable = true, insertable = true, updatable = true)
    public Address getAddress()
    {
        return address;
    }

    /**
     * @return the loans
     */
    @OneToMany(cascade = { }, fetch = FetchType.LAZY, mappedBy = "division")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<Loan> getLoans()
    {
        return loans;
    }

    /**
     * @return the disciplines
     */
    @OneToMany(cascade = { }, fetch = FetchType.LAZY, mappedBy = "division")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<Discipline> getDisciplines()
    {
        return disciplines;
    }

    /**
     * @param iconURI the iconURI to set
     */
    public void setIconURI(String iconURI)
    {
        this.iconURI = iconURI;
    }

    /**
     * @param divisionId the divisionId to set
     */
    public void setDivisionId(Integer divisionId)
    {
        this.divisionId = divisionId;
    }

    /**
     * @param ipr the disciplineType to set
     */
    public void setDiscipline(String discipline)
    {
        this.discipline = discipline;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @param remarks the remarks to set
     */
    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @param members the members to set
     */
    public void setMembers(Set<Agent> members)
    {
        this.members = members;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri)
    {
        this.uri = uri;
    }


    public void setConservDescriptions(final Set<ConservDescription> conservDescriptions)
    {
        this.conservDescriptions = conservDescriptions;
    }

    /**
     * @return the institution
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "InstitutionID", unique = false, nullable = false, insertable = true, updatable = true)
    public Institution getInstitution()
    {
        return institution;
    }

    /**
     * @param institution the institution to set
     */
    public void setInstitution(Institution institution)
    {
        this.institution = institution;
    }


    /**
     * @param treatmentEvents the treatmentEvents to set
     */
    public void setTreatmentEvents(Set<TreatmentEvent> treatmentEvents)
    {
        this.treatmentEvents = treatmentEvents;
    }

    /**
     * @param accessions the accessions to set
     */
    public void setAccessions(Set<Accession> accessions)
    {
        this.accessions = accessions;
    }
    
    /**
     * @param repositoryAgreements the repositoryAgreements to set
     */
    public void setRepositoryAgreements(Set<RepositoryAgreement> repositoryAgreements)
    {
        this.repositoryAgreements = repositoryAgreements;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(Address address)
    {
        this.address = address;
    }
    
    /**
     * @param loans the loans to set
     */
    public void setLoans(Set<Loan> loans)
    {
        this.loans = loans;
    }

    /**
     * @param disciplines the disciplines to set
     */
    public void setDisciplines(Set<Discipline> disciplines)
    {
        this.disciplines = disciplines;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return Division.class;
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
        return 96;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        return StringUtils.isNotEmpty(name) ? name : super.getIdentityTitle();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getIdentityTitle();
    }

    /**
     * @return the currentDivision
     */
    public static Division getCurrentDivision()
    {
        return currentDivision;
    }

    /**
     * @param currentDivision the currentDivision to set
     */
    public static void setCurrentDivision(Division currentDivision)
    {
        Division.currentDivision = currentDivision;
    }

 }
