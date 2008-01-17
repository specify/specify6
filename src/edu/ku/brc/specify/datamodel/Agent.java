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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.ui.forms.formatters.DataObjFieldFormatMgr;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "agent")
@org.hibernate.annotations.Table(appliesTo="agent", indexes =
    {   @Index (name="AgentLastNameIDX", columnNames={"LastName"}),
        @Index (name="AgentFirstNameIDX", columnNames={"FirstName"}),
        @Index (name="AgentNameIDX", columnNames={"Name"}),
        @Index (name="AuthorNameIDX", columnNames={"AuthorName"}), 
        @Index (name="CollectorNameIDX", columnNames={"CollectorName"}),
        @Index (name="AgentGuidIDX", columnNames={"GUID"})  
    })
public class Agent extends CollectionMember implements java.io.Serializable, AttachmentOwnerIFace<AgentAttachment> {

    // Fields
    
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
    protected String                        interests;
    protected String                        abbreviation;
    protected String                        name;
    protected String                        initials;
    protected String                        remarks;
    protected Integer                       visibility;
    protected String                        visibilitySetBy;
    
    // Roles
    protected String                        authorName;
    protected String                        labelName;
    protected String                        collectorName;
    
    protected String                        guid; 
    
    protected Set<Author>                   authors;
    protected Set<LoanReturnPreparation>    loanReturnPreparations;
    protected Set<BorrowReturnMaterial>     borrowReturnMaterials;
    protected Set<ExchangeIn>               exchangeInCatalogedBys;
    protected Set<Agent>                    orgMembers;
    protected Agent                         organization;
    protected Set<Project>                  projects;
    protected Set<Preparation>              preparations;
    protected Set<GroupPerson>              groups;
    protected Set<GroupPerson>              members;
    protected Set<Determination>            determinations;
    protected Set<Shipment>                 shipments;
    protected Set<Collector>                collectors;
    protected Set<ExchangeOut>              exchangeOutCatalogedBys;
    protected Set<RepositoryAgreement>      repositoryAgreements;
    protected Set<GeoCoordDetail>           geoCoordDetail;
    protected Set<CollectionObject>         catalogers;
    
    
    protected Division                      division;
    protected Institution                   instTechContact;
    protected Institution                   instContentContact;

    // From AgentAddress
    protected String                        jobTitle;
    protected String                        email;
    protected String                        url;
     
    protected Set<Address>                  addresses;
    protected Set<AgentVariant>             variants;

    
    /*
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
    protected Set<SpecifyUser>              specifyUsers;
    
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
        agentType                 = null;
        firstName                 = null;
        lastName                  = null;
        middleInitial             = null;
        title                     = null;
        interests                 = null;
        abbreviation              = null;
        name                      = null;
        initials                  = null;
        remarks                   = null;
        visibility                = null;
        labelName                 = null;
        authorName                = null;
        collectorName             = null;
        guid                      = null;
        authors                   = new HashSet<Author>();
        loanReturnPreparations = new HashSet<LoanReturnPreparation>();
        borrowReturnMaterials     = new HashSet<BorrowReturnMaterial>();
        exchangeInCatalogedBys    = new HashSet<ExchangeIn>();
        orgMembers                = new HashSet<Agent>();
        organization              = null;
        projects                  = new HashSet<Project>();
        preparations              = new HashSet<Preparation>();
        groups                    = new HashSet<GroupPerson>();
        members                   = new HashSet<GroupPerson>();
        determinations            = new HashSet<Determination>();
        shipments                 = new HashSet<Shipment>();
        collectors                = new HashSet<Collector>();
        exchangeOutCatalogedBys   = new HashSet<ExchangeOut>();
        repositoryAgreements      = new HashSet<RepositoryAgreement>();
        geoCoordDetail            = new HashSet<GeoCoordDetail>();
        catalogers                = new HashSet<CollectionObject>();
        
        division                  = null;
        instTechContact           = null;
        instContentContact        = null;
        
        // Agent
        jobTitle                       = null;
        email                          = null;
        url                            = null;
        remarks                        = null;
        organization                   = null;
        addresses                      = new HashSet<Address>();
        agentAttachments               = new HashSet<AgentAttachment>();
        variants                       = new HashSet<AgentVariant>();

        /*
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
    
    /**
     *
     */
    @Column(name = "AgentType", nullable = false)
    public Byte getAgentType() {
        return this.agentType;
    }

    public void setAgentType(Byte agentType) {
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
     *      * of organization/group/Folks (and maybe persons)
     */
    @Column(name = "Name", length = 120)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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
     *      * Indicates whether this record can be viewed - by owner, by instituion, or by all
     */
    @Column(name = "Visibility")
    public Integer getVisibility() {
        return this.visibility;
    }
    
    public void setVisibility(Integer visibility) {
        this.visibility = visibility;
    }
    
    @Transient
    @Override
    public boolean isRestrictable()
    {
        return true;
    }
    
    /**
     * 
     */
    @Column(name = "VisibilitySetBy", length = 50)
    public String getVisibilitySetBy() {
        return this.visibilitySetBy;
    }
    
    public void setVisibilitySetBy(String visibilitySetBy) 
    {
        this.visibilitySetBy = visibilitySetBy;
    }
    
    /**
     *
     */
    @OneToMany(mappedBy = "agent")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<Author> getAuthors() {
        return this.authors;
    }

    public void setAuthors(Set<Author> authors) {
        this.authors = authors;
    }

    /**
     *
     */
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

    /**
     *
     */
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

    /**
     *
     */
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
     *
     */
    @OneToMany(mappedBy = "agent")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<Project> getProjects() 
    {
        return this.projects;
    }

    public void setProjects(Set<Project> projects) 
    {
        this.projects = projects;
    }

    /**
     *
     */
    @OneToMany(mappedBy = "preparedByAgent")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<Preparation> getPreparations() 
    {
        return this.preparations;
    }

    public void setPreparations(Set<Preparation> preparations) 
    {
        this.preparations = preparations;
    }

    /**
     *
     */
    @OneToMany(mappedBy = "member")
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
    @OneToMany(mappedBy = "group")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<GroupPerson> getMembers() 
    {
        return this.members;
    }

    public void setMembers(Set<GroupPerson> groupPersonByMember) 
    {
        this.members = groupPersonByMember;
    }

    /**
    *
    */
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

   /**
    *
    */
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

    /**
     *
     */
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
     *
     */
    @OneToMany(mappedBy = "agentCatalogedBy")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
    public Set<ExchangeOut> getExchangeOutCatalogedBys() 
    {
        return this.exchangeOutCatalogedBys;
    }

    public void setExchangeOutCatalogedBys(Set<ExchangeOut> exchangeOutCatalogedBys) 
    {
        this.exchangeOutCatalogedBys = exchangeOutCatalogedBys;
    }

    /**
    *
    */
   @OneToMany(mappedBy = "originator")
   @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN} )
   public Set<RepositoryAgreement> getRepositoryAgreements() 
   {
       return this.repositoryAgreements;
   }

   public void setRepositoryAgreements(Set<RepositoryAgreement> repositoryAgreements) 
   {
       this.repositoryAgreements = repositoryAgreements;
   }

   /**
    *  The Division this Agent belongs to.
    */
   @ManyToOne
   @JoinColumn(name = "DivisionID")
   public Division getDivision() 
   {
       return this.division;
   }
   
   public void setDivision(Division division) 
   {
       this.division = division;
   }

   /**
    *  The Institution for Technical Contact.
    */
   @ManyToOne
   @JoinColumn(name = "InstitutionID")
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

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    /**
     *
     */
    @Column(name = "Email", length = 50)
    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     *
     */
    @Column(name = "URL", length=1024)
    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
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
     * @return the authorName
     */
    @Column(name = "AuthorName", length = 64)
    public String getAuthorName()
    {
        return authorName;
    }

    /**
     * @param authorName the authorName to set
     */
    public void setAuthorName(String authorName)
    {
        this.authorName = authorName;
    }

    /**
     * @return the labelName
     */
    @Column(name = "LabelName", length = 64)
    public String getLabelName()
    {
        return labelName;
    }

    /**
     * @param labelName the labelName to set
     */
    public void setLabelName(String labelName)
    {
        this.labelName = labelName;
    }

    /**
     * @return the collectorName
     */
    @Column(name = "CollectorName", length = 64)
    public String getCollectorName()
    {
        return collectorName;
    }

    /**
     * @param collectorName the collectorName to set
     */
    public void setCollectorName(String collectorName)
    {
        this.collectorName = collectorName;
    }

    /**
     *      * Associated record in Address table
     */
    @OneToMany(mappedBy = "agent")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<Address> getAddresses() {
        return this.addresses;
    }

    public void setAddresses(Set<Address> addresses) {
        this.addresses = addresses;
    }

    
    @OneToMany(mappedBy = "agent")
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK, CascadeType.DELETE} )
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
//
//    /**
//     *
//     */
//    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "agent")
//    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
//    public Set<LoanAgent> getLoanAgents() {
//        return this.loanAgents;
//    }
//
//    public void setLoanAgents(Set<LoanAgent> loanAgents) {
//        this.loanAgents = loanAgents;
//    }
//
//    /**
//     *
//     */
//    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "shipper")
//    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
//    public Set<Shipment> getShipmentsByShipper() {
//        return this.shipmentsByShipper;
//    }
//
//    public void setShipmentsByShipper(Set<Shipment> shipmentsByShipper) {
//        this.shipmentsByShipper = shipmentsByShipper;
//    }
//
//    /**
//     *
//     */
//    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "shippedTo")
//    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
//    public Set<Shipment> getShipmentsByShippedTo() {
//        return this.shipmentsByShippedTo;
//    }
//
//    public void setShipmentsByShippedTo(Set<Shipment> shipmentsByShippedTo) {
//        this.shipmentsByShippedTo = shipmentsByShippedTo;
//    }
//
//    /**
//     *
//     */
//    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "agent")
//    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
//    public Set<DeaccessionAgent> getDeaccessionAgents() {
//        return this.deaccessionAgents;
//    }
//
//    public void setDeaccessionAgents(Set<DeaccessionAgent> deaccessionAgents) {
//        this.deaccessionAgents = deaccessionAgents;
//    }
//
//    /**
//     *
//     */
//    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "agentReceivedFrom")
//    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
//    public Set<ExchangeIn> getExchangeInFromOrganizations() {
//        return this.exchangeInFromOrganizations;
//    }
//
//    public void setExchangeInFromOrganizations(Set<ExchangeIn> exchangeInFromOrganizations) {
//        this.exchangeInFromOrganizations = exchangeInFromOrganizations;
//    }
//
//    /**
//     *
//     */
//    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "issuedTo")
//    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
//    public Set<Permit> getPermitsIssuedTo() {
//        return this.permitsIssuedTo;
//    }
//
//    public void setPermitsIssuedTo(Set<Permit> permitsIssuedTo) {
//        this.permitsIssuedTo = permitsIssuedTo;
//    }
//
//    /**
//     *
//     */
//    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "issuedBy")
//    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
//    public Set<Permit> getPermitsIssuedBy() {
//        return this.permitsIssuedBy;
//    }
//
//    public void setPermitsIssuedBy(Set<Permit> permitsByIssuer) {
//        this.permitsIssuedBy = permitsByIssuer;
//    }
//
//    /**
//     *
//     */
//    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "agent")
//    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
//    public Set<BorrowAgent> getBorrowAgents() {
//        return this.borrowAgents;
//    }
//
//    public void setBorrowAgents(Set<BorrowAgent> borrowAgents) {
//        this.borrowAgents = borrowAgents;
//    }
//
//    /**
//     *
//     */
//    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "agent")
//    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
//    public Set<AccessionAgent> getAccessionAgents() {
//        return this.accessionAgents;
//    }
//
//    public void setAccessionAgents(Set<AccessionAgent> accessionAgents) {
//        this.accessionAgents = accessionAgents;
//    }
//
//    /**
//     *
//     */
//    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "agentSentTo")
//    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
//    public Set<ExchangeOut> getExchangeOutSentToOrganizations() {
//        return this.exchangeOutSentToOrganizations;
//    }
//
//    public void setExchangeOutSentToOrganizations(Set<ExchangeOut> exchangeOutSentToOrganizations) {
//        this.exchangeOutSentToOrganizations = exchangeOutSentToOrganizations;
//    }
//    /**
//     * 
//     */
//    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "agent")
//    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
//    public Set<SpecifyUser> getSpecifyUsers() {
//        return this.specifyUsers;
//    }
//    
//    public void setSpecifyUsers(Set<SpecifyUser> specifyUser) {
//        this.specifyUsers = specifyUser;
//    } 
//    
//    /**
//    *
//    */
//   @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "agent")
//   @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
//   public Set<InfoRequest> getInfoRequests()
//   {
//       return this.infoRequests;
//   }
//
//   public void setInfoRequests(final Set<InfoRequest> infoRequests)
//   {
//       this.infoRequests = infoRequests;
//   }
//
//    /**
//    *
//    */
//   @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "examinedByAgent")
//   @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
//   public Set<ConservEvent> getExaminedByAgentConservEvents()
//   {
//       return this.examinedByAgentConservEvents;
//   }
//
//   public void setExaminedByAgentConservEvents(final Set<ConservEvent> examinedByAgentConservEvents)
//   {
//       this.examinedByAgentConservEvents = examinedByAgentConservEvents;
//   }
//
//   /**
//    *
//    */
//   @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "treatedByAgent")
//   @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
//   public Set<ConservEvent> getTreatedByAgentConservEvents()
//   {
//       return this.treatedByAgentConservEvents;
//   }
//
//   public void setTreatedByAgentConservEvents(final Set<ConservEvent> treatedByAgentConservEvents)
//   {
//       this.treatedByAgentConservEvents = treatedByAgentConservEvents;
//   }
//   
//
//    /**
//     *
//     */
//    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "curator")
//    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
//    public Set<ConservDescription> getConservDescriptions()
//    {
//        return this.conservDescriptions;
//    }
//
//    public void setConservDescriptions(final Set<ConservDescription> conservDescriptions)
//    {
//        this.conservDescriptions = conservDescriptions;
//    }
//
//
//    
//    /**
//     * @return the collectionContacts
//     */
//    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "contactAgent")
//    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
//    public Set<Collection> getCollectionContacts()
//    {
//        return collectionContacts;
//    }
//
//    /**
//     * @param collectionContacts the collectionContacts to set
//     */
//    public void setCollectionContacts(Set<Collection> collectionContacts)
//    {
//        this.collectionContacts = collectionContacts;
//    }
//
//    /**
//     * @return the collectionCurators
//     */
//    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "curatorAgent")
//    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
//    public Set<Collection> getCollectionCurators()
//    {
//        return collectionCurators;
//    }
//
//    /**
//     * @param collectionCurators the collectionCurators to set
//     */
//    public void setCollectionCurators(Set<Collection> collectionCurators)
//    {
//        this.collectionCurators = collectionCurators;
//    }
//
//    /**
//     * @return the fieldNotebookOwners
//     */
//    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "ownerAgent")
//    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
//    public Set<FieldNotebook> getFieldNotebookOwners()
//    {
//        return fieldNotebookOwners;
//    }
//
//    /**
//     * @param fieldNotebookOwners the fieldNotebookOwners to set
//     */
//    public void setFieldNotebookOwners(Set<FieldNotebook> fieldNotebookOwners)
//    {
//        this.fieldNotebookOwners = fieldNotebookOwners;
//    }
//
//    /**
//     * @return the dnaSequencers
//     */
//    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "sequencer")
//    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
//    public Set<DNASequence> getDnaSequencers()
//    {
//        return dnaSequencers;
//    }
//
//    /**
//     * @param dnaSequencers the dnaSequencers to set
//     */
//    public void setDnaSequencers(Set<DNASequence> dnaSequencers)
//    {
//        this.dnaSequencers = dnaSequencers;
//    }
//
//    /**
//     * @return the pageSetSourceAgents
//     */
//    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "sourceAgent")
//    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
//    public Set<FieldNotebookPageSet> getPageSetSourceAgents()
//    {
//        return pageSetSourceAgents;
//    }
//
//    /**
//     * @param pageSetSourceAgents the pageSetSourceAgents to set
//     */
//    public void setPageSetSourceAgents(Set<FieldNotebookPageSet> pageSetSourceAgents)
//    {
//        this.pageSetSourceAgents = pageSetSourceAgents;
//    }
//
//    /**
//     * @return the lastEditedBys
//     */
//    //@OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "lastEditedBy")
//    //@Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
//    @Transient
//    public Set<DataModelObjBase> getLastEditedBys()
//    {
//        return lastEditedBys;
//    }
//
//    /**
//     * @param lastEditedBys the lastEditedBys to set
//     */
//    public void setLastEditedBys(Set<DataModelObjBase> lastEditedBys)
//    {
//        this.lastEditedBys = lastEditedBys;
//    }


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
        if (lastName != null)
        {
            if (firstName != null)
            {
                return lastName + ", " + firstName;
            }
            return lastName;
        }
        
        if (name!=null)
        {
            return name;
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
        return DataObjFieldFormatMgr.format(this, getClass());
    }

    @Transient
    public Set<AgentAttachment> getAttachmentReferences()
    {
        return agentAttachments;
    }

}
