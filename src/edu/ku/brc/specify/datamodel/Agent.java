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
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;


import java.io.File;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import edu.ku.brc.ui.forms.FormDataObjIFace;
import edu.ku.brc.util.AttachmentUtils;

/**

 */
@Entity
@Table(name = "agent")
public class Agent extends DataModelObjBase implements java.io.Serializable {

    // Fields
    
    public static final byte                ORG    = 0;
    public static final byte                PERSON = 1;
    public static final byte                OTHER  = 2;
    public static final byte                GROUP  = 3;

    protected Long                       agentId;
    /** Organization (0), Person (1), Other (2) or Group (3) */
    protected Byte                          agentType;
    protected String                        firstName;
    protected String                        lastName;
    protected String                        middleInitial;
    protected String                        title;
    protected String                        interests;
    protected String                        abbreviation;
    protected String                        name;
    protected String                        remarks;
    protected Integer                       visibility;
    protected String                        visibilitySetBy;
    protected Set<Authors>                   authors;
    protected Set<LoanReturnPhysicalObject> loanReturnPhysicalObjects;
    protected Set<BorrowReturnMaterial>     borrowReturnMaterials;
    protected Set<ExchangeIn>               exchangeInCatalogedBys;
    protected Set<Agent>                    orgMembers;
    protected Agent                         organization;
    protected Set<Project>                  projects;
    protected Set<Preparation>              preparations;
    protected Set<GroupPersons>              groups;
    protected Set<GroupPersons>              members;
    protected Set<Determination>            determinations;
    protected Set<Shipment>                 shipments;
    protected Set<Collectors>                collectors;
    protected Set<ExchangeOut>              exchangeOutCatalogedBys;
    protected Set<Attachment>               attachments;
    protected Set<RepositoryAgreement>      repositoryAgreements;
     
    // From AgentAddress
    protected String                        jobTitle;
    protected String                        email;
    protected String                        url;
     
    protected Set<Address>                  addresses;
    protected Set<LoanAgents>                loanAgents;
    protected Set<Shipment>                 shipmentsByShipper;
    protected Set<Shipment>                 shipmentsByShippedTo;
    protected Set<DeaccessionAgents>         deaccessionAgents;
    protected Set<ExchangeIn>               exchangeInFromOrganizations;
    protected Set<Permit>                   permitsIssuedTo;
    protected Set<Permit>                   permitsIssuedBy;
    protected Set<BorrowAgents>              borrowAgents;
    protected Set<AccessionAgent>           accessionAgents;
    protected Set<ExchangeOut>              exchangeOutSentToOrganizations;
    protected Set<SpecifyUser> specifyUsers;

    // Constructors

    /** default constructor */
    public Agent() {
        //
        // do nothing
    }

    /** constructor with id */
    public Agent(Long agentId) {
        this.agentId = agentId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        agentId = null;
        agentType = null;
        firstName = null;
        lastName = null;
        middleInitial = null;
        title = null;
        interests = null;
        abbreviation = null;
        name = null;
        remarks = null;
        visibility = null;
        authors = new HashSet<Authors>();
        loanReturnPhysicalObjects = new HashSet<LoanReturnPhysicalObject>();
        borrowReturnMaterials = new HashSet<BorrowReturnMaterial>();
        exchangeInCatalogedBys = new HashSet<ExchangeIn>();
        orgMembers = new HashSet<Agent>();
        organization = null;
        projects = new HashSet<Project>();
        preparations = new HashSet<Preparation>();
        groups = new HashSet<GroupPersons>();
        members = new HashSet<GroupPersons>();
        determinations = new HashSet<Determination>();
        shipments = new HashSet<Shipment>();
        collectors = new HashSet<Collectors>();
        exchangeOutCatalogedBys = new HashSet<ExchangeOut>();
        attachments = new HashSet<Attachment>();
        repositoryAgreements = new HashSet<RepositoryAgreement>();
        
        // Agent
        jobTitle = null;
        email = null;
        url = null;
        remarks = null;
        addresses = new HashSet<Address>();
        loanAgents = new HashSet<LoanAgents>();
        shipmentsByShipper = new HashSet<Shipment>();
        shipmentsByShippedTo = new HashSet<Shipment>();
        deaccessionAgents = new HashSet<DeaccessionAgents>();
        exchangeInFromOrganizations = new HashSet<ExchangeIn>();
        permitsIssuedTo = new HashSet<Permit>();
        permitsIssuedBy = new HashSet<Permit>();
        borrowAgents = new HashSet<BorrowAgents>();
        accessionAgents = new HashSet<AccessionAgent>();
        exchangeOutSentToOrganizations = new HashSet<ExchangeOut>();
        organization = null;
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    @Id
    @GeneratedValue
    @Column(name = "AgentID", unique = false, nullable = false, insertable = true, updatable = true)
    public Long getAgentId() {
        return this.agentId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
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

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "agent")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<Attachment> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(Set<Attachment> attachments)
    {
        this.attachments = attachments;
    }
    
    @Transient
    public String getImageURL()
    {
        for (Attachment a: attachments)
        {
            if (a.getMimeType().startsWith("image"))
            {
                File thumb = AttachmentUtils.getAttachmentManager().getThumbnail(a);
                try
                {
                    return thumb.toURI().toURL().toString();
                }
                catch (MalformedURLException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    public void setImageURL(String url)
    {
        System.err.println("Implement this: setImageURL(" + url + ")");

        Attachment newImage = new Attachment();
        newImage.initialize();
        newImage.setOrigFilename(url);
        newImage.setAgent(this);
        this.attachments.add(newImage);
        return;
    }

    /**
     *
     */
    @Column(name = "AgentType", unique = false, nullable = false, insertable = true, updatable = true, length = 3)
    public Byte getAgentType() {
        return this.agentType;
    }

    public void setAgentType(Byte agentType) {
        this.agentType = agentType;
    }

    /**
     *      * of Person
     */
    @Column(name = "FirstName", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     *      * of Person
     */
    @Column(name = "LastName", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     *      * of Person
     */
    @Column(name = "MiddleInitial", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getMiddleInitial() {
        return this.middleInitial;
    }

    public void setMiddleInitial(String middleInitial) {
        this.middleInitial = middleInitial;
    }

    /**
     *      * of Person
     */
    @Column(name = "Title", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *      * of Person or Organization
     */
    @Column(name = "Interests", unique = false, nullable = true, insertable = true, updatable = true)
    public String getInterests() {
        return this.interests;
    }

    public void setInterests(String interests) {
        this.interests = interests;
    }

    /**
     *      * of organization
     */
    @Column(name = "Abbreviation", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getAbbreviation() {
        return this.abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    /**
     *      * of organization/group/Folks (and maybe persons)
     */
    @Column(name = "Name", unique = false, nullable = true, insertable = true, updatable = true, length = 120)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     */
    @Column(name = "Remarks", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
    public String getRemarks() {
        return this.remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    /**
     *      * Indicates whether this record can be viewed - by owner, by instituion, or by all
     */
    @Column(name = "Visibility", unique = false, nullable = true, insertable = true, updatable = true, length = 10)
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
    @Column(name = "VisibilitySetBy", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getVisibilitySetBy() {
        return this.visibilitySetBy;
    }
    
    public void setVisibilitySetBy(String visibilitySetBy) {
        this.visibilitySetBy = visibilitySetBy;
    }
    
    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "agent")
    public Set<Authors> getAuthors() {
        return this.authors;
    }

    public void setAuthors(Set<Authors> authors) {
        this.authors = authors;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "receivedBy")
    public Set<LoanReturnPhysicalObject> getLoanReturnPhysicalObjects() {
        return this.loanReturnPhysicalObjects;
    }

    public void setLoanReturnPhysicalObjects(Set<LoanReturnPhysicalObject> loanReturnPhysicalObjects) {
        this.loanReturnPhysicalObjects = loanReturnPhysicalObjects;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "agent")
    public Set<BorrowReturnMaterial> getBorrowReturnMaterials() {
        return this.borrowReturnMaterials;
    }

    public void setBorrowReturnMaterials(Set<BorrowReturnMaterial> borrowReturnMaterials) {
        this.borrowReturnMaterials = borrowReturnMaterials;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "agentCatalogedBy")
    public Set<ExchangeIn> getExchangeInCatalogedBys() {
        return this.exchangeInCatalogedBys;
    }

    public void setExchangeInCatalogedBys(Set<ExchangeIn> exchangeInCatalogedBys) {
        this.exchangeInCatalogedBys = exchangeInCatalogedBys;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "organization")
    @Cascade( { CascadeType.SAVE_UPDATE })
    public Set<Agent> getOrgMembers() {
        return this.orgMembers;
    }

    public void setOrgMembers(Set<Agent> members) {
        this.orgMembers = members;
    }

    /**
     *      * of organization
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @Cascade( { CascadeType.SAVE_UPDATE })
    @JoinColumn(name = "ParentOrganizationID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getOrganization() {
        return this.organization;
    }

    public void setOrganization(Agent organization) {
        this.organization = organization;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "agent")
    public Set<Project> getProjects() {
        return this.projects;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "preparedByAgent")
    public Set<Preparation> getPreparations() {
        return this.preparations;
    }

    public void setPreparations(Set<Preparation> preparations) {
        this.preparations = preparations;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "member")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<GroupPersons> getGroups() {
        return this.groups;
    }

    public void setGroups(Set<GroupPersons> groupPersonsByGroup) {
        this.groups = groupPersonsByGroup;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "group")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<GroupPersons> getMembers() {
        return this.members;
    }

    public void setMembers(Set<GroupPersons> groupPersonsByMember) {
        this.members = groupPersonsByMember;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "determiner")
    public Set<Determination> getDeterminations() {
        return this.determinations;
    }

    public void setDeterminations(Set<Determination> determinations) {
        this.determinations = determinations;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "shippedBy")
    public Set<Shipment> getShipments() {
        return this.shipments;
    }

    public void setShipments(Set<Shipment> shipments) {
        this.shipments = shipments;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "agent")
    public Set<Collectors> getCollectors() {
        return this.collectors;
    }

    public void setCollectors(Set<Collectors> collectors) {
        this.collectors = collectors;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "agentCatalogedBy")
    public Set<ExchangeOut> getExchangeOutCatalogedBys() {
        return this.exchangeOutCatalogedBys;
    }

    public void setExchangeOutCatalogedBys(Set<ExchangeOut> exchangeOutCatalogedBys) {
        this.exchangeOutCatalogedBys = exchangeOutCatalogedBys;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "originator")
    public Set<RepositoryAgreement> getRepositoryAgreements() {
        return this.repositoryAgreements;
    }

    public void setRepositoryAgreements(Set<RepositoryAgreement> repositoryAgreements) {
        this.repositoryAgreements = repositoryAgreements;
    }

    //----------------------------------------------------
    // Agent Address
    //----------------------------------------------------
    
    /**
     *      * Agent's (person) job title at specified address and organization
     */
    @Column(name = "JobTitle", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getJobTitle() {
        return this.jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    /**
     *
     */
    @Column(name = "Email", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     *
     */
    @Column(name = "URL", length=1024, unique = false, nullable = true, insertable = true, updatable = true)
    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "agent")
    public Set<LoanAgents> getLoanAgents() {
        return this.loanAgents;
    }

    public void setLoanAgents(Set<LoanAgents> loanAgents) {
        this.loanAgents = loanAgents;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "shipper")
    public Set<Shipment> getShipmentsByShipper() {
        return this.shipmentsByShipper;
    }

    public void setShipmentsByShipper(Set<Shipment> shipmentsByShipper) {
        this.shipmentsByShipper = shipmentsByShipper;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "shippedTo")
    public Set<Shipment> getShipmentsByShippedTo() {
        return this.shipmentsByShippedTo;
    }

    public void setShipmentsByShippedTo(Set<Shipment> shipmentsByShippedTo) {
        this.shipmentsByShippedTo = shipmentsByShippedTo;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "agent")
    public Set<DeaccessionAgents> getDeaccessionAgents() {
        return this.deaccessionAgents;
    }

    public void setDeaccessionAgents(Set<DeaccessionAgents> deaccessionAgents) {
        this.deaccessionAgents = deaccessionAgents;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "agentReceivedFrom")
    public Set<ExchangeIn> getExchangeInFromOrganizations() {
        return this.exchangeInFromOrganizations;
    }

    public void setExchangeInFromOrganizations(Set<ExchangeIn> exchangeInFromOrganizations) {
        this.exchangeInFromOrganizations = exchangeInFromOrganizations;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "issuedTo")
    public Set<Permit> getPermitsIssuedTo() {
        return this.permitsIssuedTo;
    }

    public void setPermitsIssuedTo(Set<Permit> permitsByIssuee) {
        this.permitsIssuedTo = permitsByIssuee;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "issuedBy")
    public Set<Permit> getPermitsIssuedBy() {
        return this.permitsIssuedBy;
    }

    public void setPermitsIssuedBy(Set<Permit> permitsByIssuer) {
        this.permitsIssuedBy = permitsByIssuer;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "agent")
    public Set<BorrowAgents> getBorrowAgents() {
        return this.borrowAgents;
    }

    public void setBorrowAgents(Set<BorrowAgents> borrowAgents) {
        this.borrowAgents = borrowAgents;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "agent")
    public Set<AccessionAgent> getAccessionAgents() {
        return this.accessionAgents;
    }

    public void setAccessionAgents(Set<AccessionAgent> accessionAgents) {
        this.accessionAgents = accessionAgents;
    }

    /**
     *
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "agentSentTo")
    public Set<ExchangeOut> getExchangeOutSentToOrganizations() {
        return this.exchangeOutSentToOrganizations;
    }

    public void setExchangeOutSentToOrganizations(Set<ExchangeOut> exchangeOutSentToOrganizations) {
        this.exchangeOutSentToOrganizations = exchangeOutSentToOrganizations;
    }

    /**
     *      * Associated record in Address table
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "agent")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<Address> getAddresses() {
        return this.addresses;
    }

    public void setAddresses(Set<Address> addresses) {
        this.addresses = addresses;
    }
    /**
     * 
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "agent")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<SpecifyUser> getSpecifyUsers() {
        return this.specifyUsers;
    }
    
    public void setSpecifyUsers(Set<SpecifyUser> specifyUser) {
        this.specifyUsers = specifyUser;
    }  


    // Add Methods

    public void addAuthors(final Authors author)
    {
        this.authors.add(author);
        author.setAgent(this);
    }

    public void addLoanReturnPhysicalObjects(final LoanReturnPhysicalObject loanReturnPhysicalObject)
    {
        this.loanReturnPhysicalObjects.add(loanReturnPhysicalObject);
        loanReturnPhysicalObject.setReceivedBy(this);
    }

    public void addBorrowReturnMaterials(final BorrowReturnMaterial borrowReturnMaterial)
    {
        this.borrowReturnMaterials.add(borrowReturnMaterial);
        borrowReturnMaterial.setAgent(this);
    }

    public void addOrgMembers(final Agent orgMember)
    {
        this.orgMembers.add(orgMember);
        orgMember.getOrgMembers().add(this);
    }

    public void addProjects(final Project project)
    {
        this.projects.add(project);
        project.setAgent(this);
    }
    
    public void addAddresses(final Address address)
    {
        this.addresses.add(address);
        address.setAgent(this);
    }



    // Done Add Methods

    // Delete Methods

    public void removeOrgMembers(final Agent orgMember)
    {
        this.orgMembers.remove(orgMember);
        orgMember.getOrgMembers().remove(this);
    }

    public void removeProjects(final Project project)
    {
        this.projects.remove(project);
        project.setAgent(null);
    }

    public void removeAddresses(final Address address)
    {
        this.addresses.remove(address);
        address.setAgent(null);
    }

     // Delete Add Methods
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public Integer getTableId()
    {
        return 5;
    }

    @Override
    @Transient
    public String getIdentityTitle()
    {
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

    @Override
    public void addReference(FormDataObjIFace ref, String refType)
    {
        if (ref instanceof Address)
        {
            Address addr = (Address)ref;
            addresses.add(addr);
            addr.setAgent(this);
            return;
        }
        if (ref instanceof AccessionAgent)
        {
            AccessionAgent aa = (AccessionAgent)ref;
            accessionAgents.add(aa);
            aa.setAgent(this);
            return;
        }
        super.addReference(ref, refType);
    }

    @Override
    public void removeReference(FormDataObjIFace ref, String refType)
    {
        if (ref instanceof Address)
        {
            addresses.remove(ref);
            return;
        }
        super.removeReference(ref, refType);
    }

}
