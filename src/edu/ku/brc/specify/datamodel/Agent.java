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

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.specify.dbsupport.TypeCode;
import edu.ku.brc.specify.dbsupport.TypeCodeItem;
import edu.ku.brc.ui.UIRegistry;

/**

 */
@SuppressWarnings("serial")
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "agent")
@org.hibernate.annotations.Table(appliesTo="agent", indexes =
    {   @Index (name="AgentLastNameIDX", columnNames={"LastName"}),
        @Index (name="AgentFirstNameIDX", columnNames={"FirstName"}),
        @Index (name="AgentGuidIDX", columnNames={"GUID"})  
    })
public class Agent extends DataModelObjBase implements java.io.Serializable, 
                                                       AttachmentOwnerIFace<AgentAttachment>,
                                                       Cloneable
{

    // Fields
    private static Agent                    userAgent = null;
    
    public static final byte                ORG    = 0;
    public static final byte                PERSON = 1;
    public static final byte                OTHER  = 2;
    public static final byte                GROUP  = 3;

    protected Integer                       agentId;
    /** Organization (0), Person (1), Other (2) or Group (3) */
    protected Byte                          agentType;
    
    protected String                        firstName;
    protected String                        lastName;
    protected String                        middleInitial;
    protected String                        title;               // Mr., Mrs., Dr.
    protected Calendar                      dateOfBirth;
    protected Calendar                      dateOfDeath;
    protected String                        interests;
    protected String                        abbreviation;
    protected String                        initials;
    protected String                        remarks;
    
    protected String                        guid; 
    
    protected Set<Agent>                    orgMembers;
    protected Agent                         organization;
    protected Set<GroupPerson>              groups;
    protected Set<GroupPerson>              members;
    protected Set<Collector>                collectors;
    
    
    
    protected Division                      division;
    protected Institution                   instTechContact;
    protected Institution                   instContentContact;
    protected Collection                    collTechContact;
    protected Collection                    collContentContact;
    
    protected SpecifyUser                   specifyUser;

    // From AgentAddress
    protected String                        jobTitle;
    protected String                        email;
    protected String                        url;
     
    protected Set<Address>                  addresses;
    protected Set<AgentVariant>             variants;
    
    protected Set<Discipline>               disciplines;

    
    /*
    protected Set<Project>                  projects;
    protected Set<Author>                   authors;
    protected Set<LoanReturnPreparation>    loanReturnPreparations;
    protected Set<BorrowReturnMaterial>     borrowReturnMaterials;
    protected Set<ExchangeIn>               exchangeInCatalogedBys;
    protected Set<Preparation>              preparations;
    protected Set<Determination>            determinations;
    protected Set<Shipment>                 shipments;
    protected Set<ExchangeOut>              exchangeOutCatalogedBys;
    protected Set<RepositoryAgreement>      repositoryAgreements;
    protected Set<GeoCoordDetail>           geoCoordDetail;
    protected Set<CollectionObject>         catalogers;
    protected Set<LoanAgent>                loanAgents;
    protected Set<Shipment>                 shipmentsByShipper;
    protected Set<Shipment>                 shipmentsByShippedTo;
    protected Set<DeaccessionAgent>         deaccessionAgents;
    protected Set<ExchangeIn>               exchangeInFromOrganizations;
    protected Set<Permit>                   permitsIssuedTo;
    protected Set<Permit>                   permitsIssuedBy;
    protected Set<BorrowAgent>              borrowAgents;
    protected Set<AccessionAgent>           accessionAgents;
    protected Set<ExchangeOut>              exchangeOutSentToOrganizations;
    
    protected Set<InfoRequest>              infoRequests;
    protected Set<ConservEvent>             examinedByAgentConservEvents;
    protected Set<ConservEvent>             treatedByAgentConservEvents;
    protected Set<ConservDescription>       conservDescriptions;
    
    protected Set<Collection>               collectionContacts;
    protected Set<Collection>               collectionCurators;
    
    protected Set<FieldNotebook>            fieldNotebookOwners;
    protected Set<DNASequence>              dnaSequencers;
    protected Set<FieldNotebookPageSet>     pageSetSourceAgents;
    protected Set<DataModelObjBase>         lastEditedBys;
    */

    protected Set<AgentAttachment>          agentAttachments;
    protected Set<AgentGeography>           agentGeographies;
    protected Set<AgentSpecialty>           agentSpecialties;
    
    protected static Agent                  currentUserAgent = null;

    // Constructors

    /** default constructor */
    public Agent() 
    {
        //
        // do nothing
    }

    /** constructor with id */
    public Agent(Integer agentId) 
    {
        this.agentId = agentId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        agentId                   = null;
        agentType                 = PERSON;
        firstName                 = null;
        lastName                  = null;
        middleInitial             = null;
        title                     = null;
        dateOfBirth               = null;
        dateOfDeath               = null;
        interests                 = null;
        abbreviation              = null;
        initials                  = null;
        remarks                   = null;
        guid                      = null;
        orgMembers                = new HashSet<Agent>();
        organization              = null;
        groups                    = new HashSet<GroupPerson>();
        members                   = new HashSet<GroupPerson>();
        collectors                = new HashSet<Collector>();
        
        division                  = null;
        instTechContact           = null;
        instContentContact        = null;
        collTechContact           = null;
        collContentContact        = null;
        disciplines               = new HashSet<Discipline>();
        specifyUser               = null;
       
        // Agent
        jobTitle                       = null;
        email                          = null;
        url                            = null;
        remarks                        = null;
        organization                   = null;
        addresses                      = new HashSet<Address>();
        agentAttachments               = new HashSet<AgentAttachment>();
        variants                       = new HashSet<AgentVariant>();
        agentGeographies               = new HashSet<AgentGeography>();
        agentSpecialties               = new HashSet<AgentSpecialty>();

        /*
        projects                  = new HashSet<Project>();
        authors                   = new HashSet<Author>();
        loanReturnPreparations    = new HashSet<LoanReturnPreparation>();
        borrowReturnMaterials     = new HashSet<BorrowReturnMaterial>();
        exchangeInCatalogedBys    = new HashSet<ExchangeIn>();
        preparations              = new HashSet<Preparation>();
        determinations            = new HashSet<Determination>();
        shipments                 = new HashSet<Shipment>();
        exchangeOutCatalogedBys   = new HashSet<ExchangeOut>();
        repositoryAgreements      = new HashSet<RepositoryAgreement>();
        geoCoordDetail            = new HashSet<GeoCoordDetail>();
        catalogers                = new HashSet<CollectionObject>();
        loanAgents                     = new HashSet<LoanAgent>();
        shipmentsByShipper             = new HashSet<Shipment>();
        shipmentsByShippedTo           = new HashSet<Shipment>();
        deaccessionAgents              = new HashSet<DeaccessionAgent>();
        exchangeInFromOrganizations    = new HashSet<ExchangeIn>();
        permitsIssuedTo                = new HashSet<Permit>();
        permitsIssuedBy                = new HashSet<Permit>();
        borrowAgents                   = new HashSet<BorrowAgent>();
        accessionAgents                = new HashSet<AccessionAgent>();
        exchangeOutSentToOrganizations = new HashSet<ExchangeOut>();
        
        infoRequests                   = new HashSet<InfoRequest>();
        examinedByAgentConservEvents   = new HashSet<ConservEvent>();
        treatedByAgentConservEvents    = new HashSet<ConservEvent>();
        
        conservDescriptions            = new HashSet<ConservDescription>();
        
        
        
        specifyUsers                   = new HashSet<SpecifyUser>();
        
        collectionContacts             = new HashSet<Collection>();
        collectionCurators             = new HashSet<Collection>();
        fieldNotebookOwners            = new HashSet<FieldNotebook>();
        dnaSequencers                  = new HashSet<DNASequence>();
        pageSetSourceAgents            = new HashSet<FieldNotebookPageSet>();
        lastEditedBys                  = new HashSet<DataModelObjBase>();
        */
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "AgentID")
    public Integer getAgentId() {
        return this.agentId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.agentId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Agent.class;
    }

    public void setAgentId(Integer agentId) {
        this.agentId = agentId;
    }

    /**
     *
     */
    @Column(name = "AgentType", nullable = false)
    public Byte getAgentType() {
        return this.agentType;
    }

    public void setAgentType(Byte agentType) 
    {
        this.agentType = agentType;
    }

    /**
     *      * of Person
     */
    @Column(name = "FirstName", length = 50)
    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     *      * of Person
     */
    @Column(name = "LastName", length = 50)
    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     *      * of Person
     */
    @Column(name = "MiddleInitial", length = 50)
    public String getMiddleInitial() {
        return this.middleInitial;
    }

    public void setMiddleInitial(String middleInitial) {
        this.middleInitial = middleInitial;
    }

    /**
     *      * of Person
     */
    @Column(name = "Title", length = 50)
    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the dateOfBirth
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DateOfBirth")
    public Calendar getDateOfBirth()
    {
        return dateOfBirth;
    }

    /**
     * @param dateOfBirth the dateOfBirth to set
     */
    public void setDateOfBirth(Calendar dateOfBirth)
    {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * @return the dateOfDeath
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DateOfDeath")
    public Calendar getDateOfDeath()
    {
        return dateOfDeath;
    }

    /**
     * @param dateOfDeath the dateOfDeath to set
     */
    public void setDateOfDeath(Calendar dateOfDeath)
    {
        this.dateOfDeath = dateOfDeath;
    }

    /**
     *      * of Person or Organization
     */
    @Column(name = "Interests", length = 255)
    public String getInterests() {
        return this.interests;
    }

    public void setInterests(String interests) {
        this.interests = interests;
    }

    /**
     *      * of organization
     */
    @Column(name = "Abbreviation", length = 50)
    public String getAbbreviation() {
        return this.abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    /**
     * @return the initials
     */
    @Column(name = "Initials", length = 8)
    public String getInitials()
    {
        return initials;
    }

    /**
     * @param initials the initials to set
     */
    public void setInitials(String initials)
    {
        this.initials = initials;
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
    @OneToMany(mappedBy = "organization")
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
    public Set<Agent> getOrgMembers() 
    {
        return this.orgMembers;
    }

    public void setOrgMembers(Set<Agent> orgMembers) 
    {
        this.orgMembers = orgMembers;
    }

    /**
     *      * of organization
     */
    @ManyToOne
    @JoinColumn(name = "ParentOrganizationID")
    public Agent getOrganization() {
        return this.organization;
    }

    public void setOrganization(Agent organization) 
    {
        this.organization = organization;
    }

    /**
     * @return the specifyUser
     */
    @ManyToOne
    @JoinColumn(name = "SpecifyUserID", unique = false, nullable = true, insertable = true, updatable = true)
    public SpecifyUser getSpecifyUser()
    {
        return specifyUser;
    }

    /**
     * @param specifyUser the specifyUser to set
     */
    public void setSpecifyUser(SpecifyUser specifyUser)
    {
        this.specifyUser = specifyUser;
    }

    /**
     *
     */
    /*@OneToMany(mappedBy = "agent")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<Project> getProjects() 
    {
        return this.projects;
    }

    public void setProjects(Set<Project> projects) 
    {
        this.projects = projects;
    }*/

    /**
     *
     */
    @OneToMany(mappedBy = "group")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<GroupPerson> getGroups() 
    {
        return this.groups;
    }

    public void setGroups(Set<GroupPerson> groupPersonByGroup) 
    {
        this.groups = groupPersonByGroup;
    }

    /**
     *
     */
    @OneToMany(mappedBy = "member")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<GroupPerson> getMembers() 
    {
        return this.members;
    }

    public void setMembers(Set<GroupPerson> groupPersonByMember) 
    {
        this.members = groupPersonByMember;
    }

    /*
    @OneToMany(mappedBy = "cataloger")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<CollectionObject> getCatalogers()
    {
        return catalogers;
    }

    public void setCatalogers(Set<CollectionObject> catalogers)
    {
        this.catalogers = catalogers;
    }
    
    @OneToMany(mappedBy = "agent")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<Author> getAuthors() {
        return this.authors;
    }

    public void setAuthors(Set<Author> authors) {
        this.authors = authors;
    }

    @OneToMany(mappedBy = "receivedBy")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<LoanReturnPreparation> getLoanReturnPreparations() 
    {
        return this.loanReturnPreparations;
    }

    public void setLoanReturnPreparations(Set<LoanReturnPreparation> loanReturnPreparations) 
    {
        this.loanReturnPreparations = loanReturnPreparations;
    }

    @OneToMany(mappedBy = "agent")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<BorrowReturnMaterial> getBorrowReturnMaterials() 
    {
        return this.borrowReturnMaterials;
    }

    public void setBorrowReturnMaterials(Set<BorrowReturnMaterial> borrowReturnMaterials) 
    {
        this.borrowReturnMaterials = borrowReturnMaterials;
    }

    @OneToMany(mappedBy = "agentCatalogedBy")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<ExchangeIn> getExchangeInCatalogedBys() 
    {
        return this.exchangeInCatalogedBys;
    }

    public void setExchangeInCatalogedBys(Set<ExchangeIn> exchangeInCatalogedBys) 
    {
        this.exchangeInCatalogedBys = exchangeInCatalogedBys;
    }

   @OneToMany(mappedBy = "determiner")
   @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
   public Set<Determination> getDeterminations() 
   {
       return this.determinations;
   }

   public void setDeterminations(Set<Determination> determinations) 
   {
       this.determinations = determinations;
   }

    @OneToMany(mappedBy = "geoRefDetBy")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<GeoCoordDetail> getGeoCoordDetail() 
    {
        return this.geoCoordDetail;
    }

    public void setGeoCoordDetail(Set<GeoCoordDetail> geoCoordDetail) 
    {
        this.geoCoordDetail = geoCoordDetail;
    }

    @OneToMany(mappedBy = "shippedBy")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<Shipment> getShipments() 
    {
        return this.shipments;
    }

    public void setShipments(Set<Shipment> shipments) 
    {
        this.shipments = shipments;
    }
    */
    
    /**
     *
     */
    @OneToMany(mappedBy = "agent")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<Collector> getCollectors() 
    {
        return this.collectors;
    }

    public void setCollectors(Set<Collector> collectors) 
    {
        this.collectors = collectors;
    }


   /**
    *  The Division this Agent belongs to.
    */
   @ManyToOne
   @JoinColumn(name = "DivisionID", unique = false, nullable = true, insertable = true, updatable = true)
   public Division getDivision() 
   {
       return this.division;
   }
   
   public void setDivision(Division division) 
   {
       this.division = division;
   }

   /**
     * @return the discipline
     */
   @ManyToMany(cascade = {}, fetch = FetchType.LAZY)
   @JoinTable(name = "agent_discipline", joinColumns = 
           { 
               @JoinColumn(name = "AgentID", unique = false, nullable = false, insertable = true, updatable = false) 
           }, 
           inverseJoinColumns = 
           { 
               @JoinColumn(name = "DisciplineID", unique = false, nullable = false, insertable = true, updatable = false) 
           })
    public Set<Discipline> getDisciplines()
    {
        return disciplines;
    }
    
    /**
     * @param discipline the discipline to set
     */
    public void setDisciplines(Set<Discipline> disciplines)
    {
        this.disciplines = disciplines;
    }
    
    /**
    *  The Institution for Technical Contact.
    */
   @ManyToOne
   @JoinColumn(name = "InstitutionTCID")
   public Institution getInstTechContact() 
   {
       return this.instTechContact;
   }
   
   public void setInstTechContact(Institution instTechContact) 
   {
       this.instTechContact = instTechContact;
   }

   /**
    *  The Institution for Technical Contact.
    */
   @ManyToOne
   @JoinColumn(name = "InstitutionCCID")
   public Institution getInstContentContact() 
   {
       return this.instContentContact;
   }
   
   public void setInstContentContact(Institution instContentContact) 
   {
       this.instContentContact = instContentContact;
   }
   
   /**
    * @return the collTechContact
    */
   @ManyToOne
   @JoinColumn(name = "CollectionTCID")
   public Collection getCollTechContact()
   {
       return collTechContact;
   }
   
   /**
    * @param collTechContact the collTechContact to set
    */
   public void setCollTechContact(Collection collTechContact)
   {
       this.collTechContact = collTechContact;
   }
   
   /**
    * @return the collContentContact
    */
   @ManyToOne
   @JoinColumn(name = "CollectionCCID")
   public Collection getCollContentContact()
   {
       return collContentContact;
   }
   
   /**
    * @param collContentContact the collContentContact to set
    */
   public void setCollContentContact(Collection collContentContact)
   {
       this.collContentContact = collContentContact;
   }


    //----------------------------------------------------
    // Agent Address
    //----------------------------------------------------
    
    /**
     *      * Agent's (person) job title at specified address and organization
     */
    @Column(name = "JobTitle", length = 50)
    public String getJobTitle() {
        return this.jobTitle;
    }

    public void setJobTitle(String jobTitle) 
    {
        this.jobTitle = jobTitle;
    }

    /**
     *
     */
    @Column(name = "Email", length = 50)
    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) 
    {
        this.email = email;
    }

    /**
     *
     */
    @Column(name = "URL", length=1024)
    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) 
    {
        this.url = url;
    }


    @Column(name = "GUID", length = 128)
    public String getGuid()
    {
        return this.guid;
    }

    public void setGuid(String guid)
    {
        this.guid = guid;
    }
    
    /**
     *      * Associated record in Address table
     */
    @OneToMany(mappedBy = "agent")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<Address> getAddresses() 
    {
        return this.addresses;
    }

    public void setAddresses(Set<Address> addresses) 
    {
        this.addresses = addresses;
    }

    
    //@OneToMany(mappedBy = "agent")
    //@Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK, CascadeType.DELETE} )
    @OneToMany(mappedBy = "agent")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @OrderBy("ordinal ASC")
    public Set<AgentAttachment> getAgentAttachments()
    {
        return agentAttachments;
    }

    public void setAgentAttachments(Set<AgentAttachment> agentAttachments)
    {
        this.agentAttachments = agentAttachments;
    }
    
    /**
     * @return the variants
     */
    @OneToMany(mappedBy = "agent")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<AgentVariant> getVariants()
    {
        return variants;
    }

    /**
     * @param variants the variants to set
     */
    public void setVariants(Set<AgentVariant> variants)
    {
        this.variants = variants;
    }

    /**
     * @return the geographies
     */
    @OneToMany(mappedBy = "agent")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<AgentGeography> getAgentGeographies()
    {
        return agentGeographies;
    }

    /**
     * @param geographies the geographies to set
     */
    public void setAgentGeographies(Set<AgentGeography> agentGeographies)
    {
        this.agentGeographies = agentGeographies;
    }
    
    
    /**
     * @return the specialties
     */
    @OneToMany(mappedBy = "agent")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<AgentSpecialty> getAgentSpecialties()
    {
        return agentSpecialties;
    }

    /**
     * @param specialties the specialties to set
     */
    public void setAgentSpecialties(Set<AgentSpecialty> agentSpecialties)
    {
        this.agentSpecialties = agentSpecialties;
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
        return 5;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        // XXX I wonder if we should be using the formatter here?
        if (StringUtils.isNotEmpty(lastName))
        {
            if (StringUtils.isNotEmpty(firstName))
            {
                return lastName + ", " + firstName;
            }
            return lastName;
            
        }
        
        if (StringUtils.isEmpty(lastName))
        {
            DBTableInfo ti = DBTableIdMgr.getInstance().getByClassName(Agent.class.getName());
            return ti.getTitle();
        }
        
        return super.getIdentityTitle();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        // The agentType is used in the formatter  so we need to skip the formatting if it isn't available
        if (agentType == null)
        {
            return super.toString();
        }
        return DataObjFieldFormatMgr.getInstance().format(this, getClass());
    }

    @Transient
    public Set<AgentAttachment> getAttachmentReferences()
    {
        return agentAttachments;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        Agent obj = (Agent)super.clone();
        
        obj.agentId = null;
        obj.timestampCreated     = new Timestamp(System.currentTimeMillis());
        obj.timestampModified    = timestampCreated;
        
        obj.orgMembers                = new HashSet<Agent>();
        obj.groups                    = new HashSet<GroupPerson>();
        obj.members                   = new HashSet<GroupPerson>();
        obj.collectors                = new HashSet<Collector>();
        
        obj.disciplines               = new HashSet<Discipline>();
       
        // Agent
        obj.addresses                      = new HashSet<Address>();
        obj.agentAttachments               = new HashSet<AgentAttachment>();
        obj.variants                       = new HashSet<AgentVariant>();
        obj.agentGeographies               = new HashSet<AgentGeography>();
        obj.agentSpecialties               = new HashSet<AgentSpecialty>();
        
        return obj;
    }
    
    /**
     * @return the userAgent
     */
    public static Agent getUserAgent()
    {
        return userAgent;
    }

    /**
     * Make all agents in a set point to a single specifyUser 
     * @param user User that agents will point to
     * @param agents Agents that will all point to the given user
     */
    public static void setUserAgent(SpecifyUser user, Set<Agent> agents)
    {
        for (Agent uAgent : agents)
        {
        	SpecifyUser spu = uAgent.getSpecifyUser();
        	if (spu != null && spu.getSpecifyUserId().equals(user.getSpecifyUserId()))
        	{
        		Agent.setUserAgent(uAgent);
        		break;
        	}
        }
    }
    
    /**
     * @param userAgent the userAgent to set
     */
    public static void setUserAgent(Agent userAgent)
    {
        Agent.userAgent = userAgent;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Agent)
        {
            Agent item = (Agent)obj;
            if (item.agentId != null)
            {
                if (item.agentId.equals(this.agentId))
                {
                    return true;
                }
                // else
                return false;
            }
            // else
            return super.equals(obj);
        }
        return false;
    }

    /**
     * @return List of pick lists for predefined system type codes.
     * 
     * The QueryBuilder function is used to generate picklist criteria controls for querying,
     * and to generate text values for the typed fields in query results and reports.
     * 
     * The WB uploader will also need this function.
     * 
     */
    @Transient
    public static List<PickListDBAdapterIFace> getSpSystemTypeCodes()
    {
        List<PickListDBAdapterIFace> result = new Vector<PickListDBAdapterIFace>(1);
        Vector<PickListItemIFace> stats = new Vector<PickListItemIFace>(4);
        stats.add(new TypeCodeItem(UIRegistry.getResourceString("Agent_ORG"), Agent.ORG));
        stats.add(new TypeCodeItem(UIRegistry.getResourceString("Agent_PERSON"),
                        Agent.PERSON));
        stats.add(new TypeCodeItem(UIRegistry.getResourceString("Agent_OTHER"), Agent.OTHER));
        stats.add(new TypeCodeItem(UIRegistry.getResourceString("Agent_GROUP"), Agent.GROUP));
        result.add(new TypeCode(stats, "agentType"));
        return result;
    }

    /**
     * @return a list (probably never containing more than one element) of fields
     * with predefined system type codes.
     */
    @Transient
    public static String[] getSpSystemTypeCodeFlds()
    {
        String[] result = {"agentType"};
        return result;
    }

}
