package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;




/**

 */
public class Agent  implements java.io.Serializable {

    // Fields

     protected Integer agentId;
     protected Byte agentType;
     protected String firstName;
     protected String lastName;
     protected String middleInitial;
     protected String title;
     protected String interests;
     protected String abbreviation;
     protected String name;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     protected Set<Author> authors;
     protected Set<LoanReturnPhysicalObject> loanReturnPhysicalObjects;
     protected Set<BorrowReturnMaterial> borrowReturnMaterials;
     protected Set<ExchangeIn> exchangeIns;
     protected Set<Agent> members;
     private Agent organization;
     protected Set<Project> projects;
     protected Set<Preparation> preparations;
     protected Set<GroupPerson> groupPersonsByGroup;
     protected Set<GroupPerson> groupPersonsByMember;
     protected Set<Determination> determinations;
     protected Set<AgentAddress> agentAddressesByOrganization;
     protected Set<AgentAddress> agentAddressesByAgent;
     protected Set<Shipment> shipments;
     protected Set<Collector> collectors;
     protected Set<ExchangeOut> exchangeOuts;
     protected Set<ExternalResource> externalResources;
     protected Set<RepositoryAgreement> repositoryAgreements;


    // Constructors

    /** default constructor */
    public Agent() {
    }

    /** constructor with id */
    public Agent(Integer agentId) {
        this.agentId = agentId;
    }




    // Initializer
    public void initialize()
    {
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
        timestampModified = null;
        timestampCreated = Calendar.getInstance().getTime();
        lastEditedBy = null;
        authors = new HashSet<Author>();
        loanReturnPhysicalObjects = new HashSet<LoanReturnPhysicalObject>();
        borrowReturnMaterials = new HashSet<BorrowReturnMaterial>();
        exchangeIns = new HashSet<ExchangeIn>();
        members = new HashSet<Agent>();
        organization = null;
        projects = new HashSet<Project>();
        preparations = new HashSet<Preparation>();
        groupPersonsByGroup = new HashSet<GroupPerson>();
        groupPersonsByMember = new HashSet<GroupPerson>();
        determinations = new HashSet<Determination>();
        agentAddressesByOrganization = new HashSet<AgentAddress>();
        agentAddressesByAgent = new HashSet<AgentAddress>();
        shipments = new HashSet<Shipment>();
        collectors = new HashSet<Collector>();
        exchangeOuts = new HashSet<ExchangeOut>();
        externalResources = new HashSet<ExternalResource>();
        repositoryAgreements = new HashSet<RepositoryAgreement>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    public Integer getAgentId() {
        return this.agentId;
    }

    public void setAgentId(Integer agentId) {
        this.agentId = agentId;
    }

    /**
     *
     */
    public Byte getAgentType() {
        return this.agentType;
    }

    public void setAgentType(Byte agentType) {
        this.agentType = agentType;
    }

    /**
     *      * of Person
     */
    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     *      * of Person
     */
    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     *      * of Person
     */
    public String getMiddleInitial() {
        return this.middleInitial;
    }

    public void setMiddleInitial(String middleInitial) {
        this.middleInitial = middleInitial;
    }

    /**
     *      * of Person
     */
    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *      * of Person or Organization
     */
    public String getInterests() {
        return this.interests;
    }

    public void setInterests(String interests) {
        this.interests = interests;
    }

    /**
     *      * of organization
     */
    public String getAbbreviation() {
        return this.abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    /**
     *      * of organization/group/Folks (and maybe persons)
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     */
    public String getRemarks() {
        return this.remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *
     */
    public Date getTimestampModified() {
        return this.timestampModified;
    }

    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
    }

    /**
     *
     */
    public Date getTimestampCreated() {
        return this.timestampCreated;
    }

    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    /**
     *
     */
    public String getLastEditedBy() {
        return this.lastEditedBy;
    }

    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     *
     */
    public Set<Author> getAuthors() {
        return this.authors;
    }

    public void setAuthors(Set<Author> authors) {
        this.authors = authors;
    }

    /**
     *
     */
    public Set<LoanReturnPhysicalObject> getLoanReturnPhysicalObjects() {
        return this.loanReturnPhysicalObjects;
    }

    public void setLoanReturnPhysicalObjects(Set<LoanReturnPhysicalObject> loanReturnPhysicalObjects) {
        this.loanReturnPhysicalObjects = loanReturnPhysicalObjects;
    }

    /**
     *
     */
    public Set<BorrowReturnMaterial> getBorrowReturnMaterials() {
        return this.borrowReturnMaterials;
    }

    public void setBorrowReturnMaterials(Set<BorrowReturnMaterial> borrowReturnMaterials) {
        this.borrowReturnMaterials = borrowReturnMaterials;
    }

    /**
     *
     */
    public Set<ExchangeIn> getExchangeIns() {
        return this.exchangeIns;
    }

    public void setExchangeIns(Set<ExchangeIn> exchangeIns) {
        this.exchangeIns = exchangeIns;
    }

    /**
     *
     */
    public Set<Agent> getMembers() {
        return this.members;
    }

    public void setMembers(Set<Agent> members) {
        this.members = members;
    }

    /**
     *      * of organization
     */
    public Agent getOrganization() {
        return this.organization;
    }

    public void setOrganization(Agent organization) {
        this.organization = organization;
    }

    /**
     *
     */
    public Set<Project> getProjects() {
        return this.projects;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }

    /**
     *
     */
    public Set<Preparation> getPreparations() {
        return this.preparations;
    }

    public void setPreparations(Set<Preparation> preparations) {
        this.preparations = preparations;
    }

    /**
     *
     */
    public Set getGroupPersonsByGroup() {
        return this.groupPersonsByGroup;
    }

    public void setGroupPersonsByGroup(Set<GroupPerson> groupPersonsByGroup) {
        this.groupPersonsByGroup = groupPersonsByGroup;
    }

    /**
     *
     */
    public Set<GroupPerson> getGroupPersonsByMember() {
        return this.groupPersonsByMember;
    }

    public void setGroupPersonsByMember(Set<GroupPerson> groupPersonsByMember) {
        this.groupPersonsByMember = groupPersonsByMember;
    }

    /**
     *
     */
    public Set<Determination> getDeterminations() {
        return this.determinations;
    }

    public void setDeterminations(Set<Determination> determinations) {
        this.determinations = determinations;
    }

    /**
     *
     */
    public Set<AgentAddress> getAgentAddressesByOrganization() {
        return this.agentAddressesByOrganization;
    }

    public void setagentAddressesByOrganization(Set<AgentAddress> agentAddressesByOrganization) {
        this.agentAddressesByOrganization = agentAddressesByOrganization;
    }

    /**
     *
     */
    public Set<AgentAddress> getAgentAddressesByAgent() {
        return this.agentAddressesByAgent;
    }

    public void setAgentAddressesByAgent(Set<AgentAddress> agentAddressesByAgent) {
        this.agentAddressesByAgent = agentAddressesByAgent;
    }

    /**
     *
     */
    public Set<Shipment> getShipments() {
        return this.shipments;
    }

    public void setShipments(Set<Shipment> shipments) {
        this.shipments = shipments;
    }

    /**
     *
     */
    public Set<Collector> getCollectors() {
        return this.collectors;
    }

    public void setCollectors(Set<Collector> collectors) {
        this.collectors = collectors;
    }

    /**
     *
     */
    public Set<ExchangeOut> getExchangeOuts() {
        return this.exchangeOuts;
    }

    public void setExchangeOuts(Set<ExchangeOut> exchangeOuts) {
        this.exchangeOuts = exchangeOuts;
    }

    /**
     *
     */
    public Set<ExternalResource> getExternalResources() {
        return this.externalResources;
    }

    public void setExternalResources(Set<ExternalResource> externalResources) {
        this.externalResources = externalResources;
    }

    /**
     *
     */
    public Set<RepositoryAgreement> getRepositoryAgreements() {
        return this.repositoryAgreements;
    }

    public void setRepositoryAgreements(Set<RepositoryAgreement> repositoryAgreements) {
        this.repositoryAgreements = repositoryAgreements;
    }




    // Add Methods

    public void addAuthor(final Author author)
    {
        this.authors.add(author);
    }

    public void addLoanReturnPhysicalObject(final LoanReturnPhysicalObject loanReturnPhysicalObject)
    {
        this.loanReturnPhysicalObjects.add(loanReturnPhysicalObject);
    }

    public void addBorrowReturnMaterial(final BorrowReturnMaterial borrowReturnMaterial)
    {
        this.borrowReturnMaterials.add(borrowReturnMaterial);
    }

    public void addExchangeIn(final ExchangeIn exchangeIn)
    {
        this.exchangeIns.add(exchangeIn);
    }

    public void addMembers(final Agent member)
    {
        this.members.add(member);
    }

    public void addProject(final Project project)
    {
        this.projects.add(project);
    }

    public void addPreparation(final Preparation preparation)
    {
        this.preparations.add(preparation);
    }

    public void groupPersonsByGroup(final GroupPerson groupPersonsByGroup)
    {
        this.groupPersonsByGroup.add(groupPersonsByGroup);
    }

    public void addGroupPersonsByMember(final GroupPerson groupPersonsByMember)
    {
        this.groupPersonsByMember.add(groupPersonsByMember);
    }

    public void addDetermination(final Determination determination)
    {
        this.determinations.add(determination);
    }

    public void addAgentAddressesByOrganization(final AgentAddress agentAddressesByOrganization)
    {
        this.agentAddressesByOrganization.add(agentAddressesByOrganization);
    }

    public void addAgentAddressesByAgent(final AgentAddress agentAddressesByAgent)
    {
        this.agentAddressesByAgent.add(agentAddressesByAgent);
    }

    public void addShipment(final Shipment shipment)
    {
        this.shipments.add(shipment);
    }

    public void addCollector(final Collector collector)
    {
        this.collectors.add(collector);
    }

    public void addExchangeOut(final ExchangeOut exchangeOut)
    {
        this.exchangeOuts.add(exchangeOut);
    }

    public void addExternalResource(final ExternalResource externalResource)
    {
        this.externalResources.add(externalResource);
    }

    public void addRepositoryAgreement(final RepositoryAgreement repositoryAgreement)
    {
        this.repositoryAgreements.add(repositoryAgreement);
    }

    // Done Add Methods
}
