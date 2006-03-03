package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.Set;




/**
 *        @hibernate.class
 *         table="agent"
 *     
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
     private Set agents;
     private Agent agent;
     private Set projects;
     private Set preparations;
     private Set groupPersonsByGroupId;
     private Set groupPersonsByMemberId;
     private Set determinations;
     private Set agentAddressesByOrganizationId;
     private Set agentAddressesByAgentId;
     private Set shipments;
     private Set collectors;
     private Set exchangeOuts;
     private Set externalResources;


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
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="AgentID"
     *         
     */
    public Integer getAgentId() {
        return this.agentId;
    }
    
    public void setAgentId(Integer agentId) {
        this.agentId = agentId;
    }

    /**
     *      *            @hibernate.property
     *             column="AgentType"
     *             length="3"
     *             not-null="true"
     *         
     */
    public Byte getAgentType() {
        return this.agentType;
    }
    
    public void setAgentType(Byte agentType) {
        this.agentType = agentType;
    }

    /**
     *      *            @hibernate.property
     *             column="FirstName"
     *             length="50"
     *         
     */
    public String getFirstName() {
        return this.firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     *      *            @hibernate.property
     *             column="LastName"
     *             length="50"
     *         
     */
    public String getLastName() {
        return this.lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     *      *            @hibernate.property
     *             column="MiddleInitial"
     *             length="50"
     *         
     */
    public String getMiddleInitial() {
        return this.middleInitial;
    }
    
    public void setMiddleInitial(String middleInitial) {
        this.middleInitial = middleInitial;
    }

    /**
     *      *            @hibernate.property
     *             column="Title"
     *             length="50"
     *         
     */
    public String getTitle() {
        return this.title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *      *            @hibernate.property
     *             column="Interests"
     *             length="255"
     *         
     */
    public String getInterests() {
        return this.interests;
    }
    
    public void setInterests(String interests) {
        this.interests = interests;
    }

    /**
     *      *            @hibernate.property
     *             column="Abbreviation"
     *             length="50"
     *         
     */
    public String getAbbreviation() {
        return this.abbreviation;
    }
    
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    /**
     *      *            @hibernate.property
     *             column="Name"
     *             length="120"
     *         
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     *      *            @hibernate.property
     *             column="Remarks"
     *             length="1073741823"
     *         
     */
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *      *            @hibernate.property
     *             column="TimestampModified"
     *             length="23"
     *             not-null="true"
     *         
     */
    public Date getTimestampModified() {
        return this.timestampModified;
    }
    
    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
    }

    /**
     *      *            @hibernate.property
     *             column="TimestampCreated"
     *             length="23"
     *             not-null="true"
     *         
     */
    public Date getTimestampCreated() {
        return this.timestampCreated;
    }
    
    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    /**
     *      *            @hibernate.property
     *             column="LastEditedBy"
     *             length="50"
     *         
     */
    public String getLastEditedBy() {
        return this.lastEditedBy;
    }
    
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="AgentID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Author"
     *         
     */
    public Set getAuthors() {
        return this.authors;
    }
    
    public void setAuthors(Set authors) {
        this.authors = authors;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="ReceivedByID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.LoanReturnPhysicalObject"
     *         
     */
    public Set getLoanReturnPhysicalObjects() {
        return this.loanReturnPhysicalObjects;
    }
    
    public void setLoanReturnPhysicalObjects(Set loanReturnPhysicalObjects) {
        this.loanReturnPhysicalObjects = loanReturnPhysicalObjects;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="ReturnedByID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.BorrowReturnMaterial"
     *         
     */
    public Set getBorrowReturnMaterials() {
        return this.borrowReturnMaterials;
    }
    
    public void setBorrowReturnMaterials(Set borrowReturnMaterials) {
        this.borrowReturnMaterials = borrowReturnMaterials;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="CatalogedByID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.ExchangeIn"
     *         
     */
    public Set getExchangeIns() {
        return this.exchangeIns;
    }
    
    public void setExchangeIns(Set exchangeIns) {
        this.exchangeIns = exchangeIns;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="ParentOrganizationID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Agent"
     *         
     */
    public Set getAgents() {
        return this.agents;
    }
    
    public void setAgents(Set agents) {
        this.agents = agents;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="ParentOrganizationID"         
     *         
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="ProjectAgentID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Project"
     *         
     */
    public Set getProjects() {
        return this.projects;
    }
    
    public void setProjects(Set projects) {
        this.projects = projects;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="PreparedByID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Preparation"
     *         
     */
    public Set getPreparations() {
        return this.preparations;
    }
    
    public void setPreparations(Set preparations) {
        this.preparations = preparations;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="GroupID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.GroupPerson"
     *         
     */
    public Set getGroupPersonsByGroupId() {
        return this.groupPersonsByGroupId;
    }
    
    public void setGroupPersonsByGroupId(Set groupPersonsByGroupId) {
        this.groupPersonsByGroupId = groupPersonsByGroupId;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="MemberID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.GroupPerson"
     *         
     */
    public Set getGroupPersonsByMemberId() {
        return this.groupPersonsByMemberId;
    }
    
    public void setGroupPersonsByMemberId(Set groupPersonsByMemberId) {
        this.groupPersonsByMemberId = groupPersonsByMemberId;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="DeterminerID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Determination"
     *         
     */
    public Set getDeterminations() {
        return this.determinations;
    }
    
    public void setDeterminations(Set determinations) {
        this.determinations = determinations;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="OrganizationID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.AgentAddress"
     *         
     */
    public Set getAgentAddressesByOrganizationId() {
        return this.agentAddressesByOrganizationId;
    }
    
    public void setAgentAddressesByOrganizationId(Set agentAddressesByOrganizationId) {
        this.agentAddressesByOrganizationId = agentAddressesByOrganizationId;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="AgentID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.AgentAddress"
     *         
     */
    public Set getAgentAddressesByAgentId() {
        return this.agentAddressesByAgentId;
    }
    
    public void setAgentAddressesByAgentId(Set agentAddressesByAgentId) {
        this.agentAddressesByAgentId = agentAddressesByAgentId;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="ShippedByID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Shipment"
     *         
     */
    public Set getShipments() {
        return this.shipments;
    }
    
    public void setShipments(Set shipments) {
        this.shipments = shipments;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="AgentID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Collector"
     *         
     */
    public Set getCollectors() {
        return this.collectors;
    }
    
    public void setCollectors(Set collectors) {
        this.collectors = collectors;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="CatalogedByID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.ExchangeOut"
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




}