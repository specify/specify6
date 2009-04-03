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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;

import edu.ku.brc.dbsupport.DBConnection;



/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jun 14, 2007
 *
 */
@Entity
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "division")
@org.hibernate.annotations.Table(appliesTo="division", indexes =
    {   @Index (name="DivisionNameIDX", columnNames={"Name"})
    })
public class Division extends UserGroupScope implements java.io.Serializable, Comparable<Division> 
{
    // Fields    
     protected String                   name;
     protected String                   altName;
     protected String                   abbrev;
     protected String                   uri;
     protected String                   iconURI;
     protected String                   discipline;
     protected String                   description;
     protected String                   remarks;
     protected Boolean                  isAccessionBound;  // Whether the Accession is bound to the Collection
     protected String                   regNumber;

     
     protected Address                  address;
     protected Institution              institution;
     protected Set<Discipline>          disciplines;
     
     protected Set<Agent>               members;
     protected Set<AutoNumberingScheme> numberingSchemes;

     
     protected static Division          currentDivision = null;

    // Constructors

    /** default constructor */
    public Division() 
    {
    }
    
    /** constructor with id */
    public Division(Integer divisionId) 
    {
        super(divisionId);
    }


    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        
        name                = null;
        altName             = null;
        abbrev              = null;
        uri                 = null;
        iconURI             = null;
        discipline          = null;
        description         = null;
        remarks             = null;
        isAccessionBound    = true;
        regNumber           = null;
        members             = new HashSet<Agent>();
        /*
        conservDescriptions = new HashSet<ConservDescription>();
        loans               = new HashSet<Loan>();
        gifts               = new HashSet<Gift>();
        treatmentEvents     = new HashSet<TreatmentEvent>();
        accessions          = new HashSet<Accession>();
        repositoryAgreements = new HashSet<RepositoryAgreement>();
        */
        numberingSchemes    = new HashSet<AutoNumberingScheme>();
        institution         = null;
        address             = null;
        disciplines         = new HashSet<Discipline>();
    }
    
    /**
     * @return the divisionId
     */
    public Integer getDivisionId()
    {
        return getUserGroupScopeId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return getUserGroupScopeId();
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
     * @return the isAccessionBound
     */
    @Column(name = "IsAccessionBound", unique = false, nullable = false, insertable = true, updatable = true)
    public Boolean getIsAccessionBound()
    {
        return isAccessionBound;
    }

    /**
     * @param isAccessionBound the isAccessionBound to set
     */
    public void setIsAccessionBound(Boolean isAccessionBound)
    {
        this.isAccessionBound = isAccessionBound;
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
     * @return the isRegistered
     */
    @Column(name = "RegNumber", unique = false, nullable = true, insertable = true, updatable = true, length = 24)
    public String getRegNumber()
    {
        return regNumber;
    }

    /**
     * @param isRegistered the isRegistered to set
     */
    public void setRegNumber(String regNumber)
    {
        this.regNumber = regNumber;
    }
    /**
     * @return the members
     */
    @OneToMany(cascade = { }, fetch = FetchType.LAZY, mappedBy = "division")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL,  org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    public Set<Agent> getMembers()
    {
        return members;
    }

    /*@OneToMany(cascade = { javax.persistence.CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "division")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<TreatmentEvent> getTreatmentEvents()
    {
        return treatmentEvents;
    }

    @OneToMany(cascade = { javax.persistence.CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "division")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<Accession> getAccessions()
    {
        return accessions;
    }

    @OneToMany(cascade = { javax.persistence.CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "division")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<RepositoryAgreement> getRepositoryAgreements()
    {
        return repositoryAgreements;
    }*/
    
    /**
     * @return the numberingSchemes
     */
    @ManyToMany(cascade = {}, fetch = FetchType.LAZY)
    @JoinTable(name = "autonumsch_div", 
            joinColumns = { @JoinColumn(name = "DivisionID", unique = false, nullable = false, insertable = true, updatable = false) }, 
            inverseJoinColumns = { @JoinColumn(name = "AutoNumberingSchemeID", unique = false, nullable = false, insertable = true, updatable = false) })
    public Set<AutoNumberingScheme> getNumberingSchemes()
    {
        return numberingSchemes;
    }

    /**
     * @param numberingSchemes the numberingSchemes to set
     */
    public void setNumberingSchemes(Set<AutoNumberingScheme> numberingSchemes)
    {
        this.numberingSchemes = numberingSchemes;
    }

    /**
     * @return the altName
     */
    @Column(name = "AltName", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getAltName()
    {
        return altName;
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
    /*@OneToMany(cascade = { }, fetch = FetchType.LAZY, mappedBy = "division")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<ConservDescription> getConservDescriptions()
    {
        return this.conservDescriptions;
    }*/
    
    /**
     * @return the address
     */
    @ManyToOne(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AddressID", unique = false, nullable = true, insertable = true, updatable = true)
    public Address getAddress()
    {
        return address;
    }

    /**
     * @return the loans
     */
    /*@OneToMany(cascade = { }, fetch = FetchType.LAZY, mappedBy = "division")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<Loan> getLoans()
    {
        return loans;
    }*/

    /**
     * @return the gifts
     */
    /*@OneToMany(cascade = { }, fetch = FetchType.LAZY, mappedBy = "division")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public Set<Gift> getGifts()
    {
        return gifts;
    }*/

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
    	setUserGroupScopeId(divisionId);
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
        this.altName = title;
    }


    /**
     * @param altName the altName to set
     */
    public void setAltName(String altName)
    {
        this.altName = altName;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri)
    {
        this.uri = uri;
    }


    /*public void setConservDescriptions(final Set<ConservDescription> conservDescriptions)
    {
        this.conservDescriptions = conservDescriptions;
    }*/

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
    /*public void setTreatmentEvents(Set<TreatmentEvent> treatmentEvents)
    {
        this.treatmentEvents = treatmentEvents;
    }*/

    /**
     * @param accessions the accessions to set
     */
    /*public void setAccessions(Set<Accession> accessions)
    {
        this.accessions = accessions;
    }*/
    
    /**
     * @param repositoryAgreements the repositoryAgreements to set
     */
    /*public void setRepositoryAgreements(Set<RepositoryAgreement> repositoryAgreements)
    {
        this.repositoryAgreements = repositoryAgreements;
    }*/

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
    /*public void setLoans(Set<Loan> loans)
    {
        this.loans = loans;
    }*/

    /**
     * @param gifts the gifts to set
     */
    /*public void setGifts(Set<Gift> gifts)
    {
        this.gifts = gifts;
    }*/

    /**
     * @param disciplines the disciplines to set
     */
    public void setDisciplines(Set<Discipline> disciplines)
    {
        this.disciplines = disciplines;
    }
    
    
    /**
     * Asks the Object to force load and child object. This must be done within a Session. 
     */
    public void forceLoad()
    {
        for (AutoNumberingScheme ans : numberingSchemes) // Force Load of Numbering Schemes
        {
            ans.getTableNumber();
        }
    }
    
    /**
     * @return a list of title (or names) of the Disciplines that the Division owns.
     */
    @Transient
    public List<String> getDisciplineList()
    {
        List<String> list = new Vector<String>();
        Connection conn = null;        
        Statement  stmt = null;
        try
        {
            conn = DBConnection.getInstance().createConnection();
            stmt = conn.createStatement();

            String sql = "SELECT Title,Name FROM discipline where DivisionID = "+ getId();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next())
            {
                String dspTitle = rs.getString(1);
                if (StringUtils.isEmpty(dspTitle))
                {
                    dspTitle = rs.getString(2);
                }
                list.add(dspTitle);
            }
            rs.close();
            Collections.sort(list);
            return list;
        } 
        catch (SQLException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(Division.class, ex);
            System.err.println("SQLException: " + ex.toString()); //$NON-NLS-1$
            System.err.println(ex.getMessage());
            
        } finally
        {
            try 
            {
                if (stmt != null)
                {
                    stmt.close();
                }
                if (conn != null)
                {
                    conn.close();
                }
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(Division.class, ex);
                ex.printStackTrace();
            }
        }
        return list;
    }
    
    /**
     * @param schemeType
     * @return
     */
    @Transient
    public AutoNumberingScheme getNumberingSchemesByType(final Integer schemeType)
    {
        for (AutoNumberingScheme scheme : numberingSchemes)
        {
            if (scheme.getTableNumber().equals(schemeType))
            {
                return scheme;
            }
        }
        return null;
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
    
    
    //----------------------------------------------------------------------
    //-- Comparable Interface
    //----------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Division obj)
    {
        if (altName != null && obj != null && StringUtils.isNotEmpty(obj.altName))
        {
            return altName.compareTo(obj.altName);
        }
        
        if (name != null && obj != null && StringUtils.isNotEmpty(obj.name))
        {
            return name.compareTo(obj.name);
        }
        // else
        return timestampCreated.compareTo(obj.timestampCreated);
    }
 }
