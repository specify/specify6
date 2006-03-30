package edu.ku.brc.specify.datamodel;

import java.util.Date;
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
     private Set authors;
     private Set loanReturnPhysicalObjects;
     private Set borrowReturnMaterials;
     private Set exchangeIns;
     private Set members;
     private Agent organization;
     private Set projects;
     private Set preparations;
     private Set groupPersonsByGroup;
     private Set groupPersonsByMember;
     private Set determinations;
     private Set agentAddressesByOrganization;
     private Set agentAddressesByAgent;
     private Set shipments;
     private Set collectors;
     private Set exchangeOuts;
     private Set externalResources;
     private Set repositoryAgreements;


    // Constructors

    /** default constructor */
    public Agent() {
    }
    
    /** constructor with id */
    public Agent(Integer agentId) {
        this.agentId = agentId;
    }
   
    
    

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
    public Set getAuthors() {
        return this.authors;
    }
    
    public void setAuthors(Set authors) {
        this.authors = authors;
    }

    /**
     * 
     */
    public Set getLoanReturnPhysicalObjects() {
        return this.loanReturnPhysicalObjects;
    }
    
    public void setLoanReturnPhysicalObjects(Set loanReturnPhysicalObjects) {
        this.loanReturnPhysicalObjects = loanReturnPhysicalObjects;
    }

    /**
     * 
     */
    public Set getBorrowReturnMaterials() {
        return this.borrowReturnMaterials;
    }
    
    public void setBorrowReturnMaterials(Set borrowReturnMaterials) {
        this.borrowReturnMaterials = borrowReturnMaterials;
    }

    /**
     * 
     */
    public Set getExchangeIns() {
        return this.exchangeIns;
    }
    
    public void setExchangeIns(Set exchangeIns) {
        this.exchangeIns = exchangeIns;
    }

    /**
     * 
     */
    public Set getMembers() {
        return this.members;
    }
    
    public void setMembers(Set members) {
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
    public Set getProjects() {
        return this.projects;
    }
    
    public void setProjects(Set projects) {
        this.projects = projects;
    }

    /**
     * 
     */
    public Set getPreparations() {
        return this.preparations;
    }
    
    public void setPreparations(Set preparations) {
        this.preparations = preparations;
    }

    /**
     * 
     */
    public Set getGroupPersonsByGroup() {
        return this.groupPersonsByGroup;
    }
    
    public void setGroupPersonsByGroup(Set groupPersonsByGroup) {
        this.groupPersonsByGroup = groupPersonsByGroup;
    }

    /**
     * 
     */
    public Set getGroupPersonsByMember() {
        return this.groupPersonsByMember;
    }
    
    public void setGroupPersonsByMember(Set groupPersonsByMember) {
        this.groupPersonsByMember = groupPersonsByMember;
    }

    /**
     * 
     */
    public Set getDeterminations() {
        return this.determinations;
    }
    
    public void setDeterminations(Set determinations) {
        this.determinations = determinations;
    }

    /**
     * 
     */
    public Set getAgentAddressesByOrganization() {
        return this.agentAddressesByOrganization;
    }
    
    public void setAgentAddressesByOrganization(Set agentAddressesByOrganization) {
        this.agentAddressesByOrganization = agentAddressesByOrganization;
    }

    /**
     * 
     */
    public Set getAgentAddressesByAgent() {
        return this.agentAddressesByAgent;
    }
    
    public void setAgentAddressesByAgent(Set agentAddressesByAgent) {
        this.agentAddressesByAgent = agentAddressesByAgent;
    }

    /**
     * 
     */
    public Set getShipments() {
        return this.shipments;
    }
    
    public void setShipments(Set shipments) {
        this.shipments = shipments;
    }

    /**
     * 
     */
    public Set getCollectors() {
        return this.collectors;
    }
    
    public void setCollectors(Set collectors) {
        this.collectors = collectors;
    }

    /**
     * 
     */
    public Set getExchangeOuts() {
        return this.exchangeOuts;
    }
    
    public void setExchangeOuts(Set exchangeOuts) {
        this.exchangeOuts = exchangeOuts;
    }

    /**
     * 
     */
    public Set getExternalResources() {
        return this.externalResources;
    }
    
    public void setExternalResources(Set externalResources) {
        this.externalResources = externalResources;
    }

    /**
     * 
     */
    public Set getRepositoryAgreements() {
        return this.repositoryAgreements;
    }
    
    public void setRepositoryAgreements(Set repositoryAgreements) {
        this.repositoryAgreements = repositoryAgreements;
    }




}