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

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.busrules.AgentBusRules;
import edu.ku.brc.specify.dbsupport.TypeCode;
import edu.ku.brc.specify.dbsupport.TypeCodeItem;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.UIRegistry;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import java.util.*;

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
        @Index (name="AgentGuidIDX", columnNames={"GUID"}),
        @Index (name="AgentTypeIDX", columnNames={"AgentType"}),
        @Index (name="AbbreviationIDX", columnNames={"Abbreviation"})        
    })
public class Agent extends DataModelObjBase implements java.io.Serializable, 
                                                       AttachmentOwnerIFace<AgentAttachment>,
                                                       Cloneable
{
    protected static final Logger log = Logger.getLogger(Agent.class);

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
    protected Byte                          dateType;
    protected Calendar                      dateOfBirth;
    protected Byte                          dateOfBirthPrecision;   // Accurate to Year, Month, Day
    protected Calendar                      dateOfDeath;
    protected Byte                          dateOfDeathPrecision;   // Accurate to Year, Month, Day
    protected String                        interests;
    protected String                        abbreviation;
    protected String                        initials;
    protected String                        suffix;
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
    protected Set<AgentIdentifier>          identifiers;

    protected String text1;
    protected String text2;
    protected String text3;
    protected String text4;
    protected String text5;
    protected Integer integer1;
    protected Integer integer2;
    protected Calendar date1;
    protected Calendar date2;
    protected Byte date1Precision;
    protected Byte date2Precision;
    protected String verbatimDate1;
    protected String verbatimDate2;

    @Lob
    @Column(name = "Text1", unique = false, nullable = true, insertable = true, updatable = true, length = 65535)
    public String getText1() {
        return text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    @Lob
    @Column(name = "Text3", unique = false, nullable = true, insertable = true, updatable = true, length = 65535)
    public String getText3() {
        return text3;
    }

    public void setText3(String text3) {
        this.text3 = text3;
    }

    @Lob
    @Column(name = "Text4", unique = false, nullable = true, insertable = true, updatable = true, length = 65535)
    public String getText4() {
        return text4;
    }

    public void setText4(String text4) {
        this.text4 = text4;
    }

    @Lob
    @Column(name = "Text5", unique = false, nullable = true, insertable = true, updatable = true, length = 65535)
    public String getText5() {
        return text5;
    }

    public void setText5(String text5) {
        this.text5 = text5;
    }

    @Lob
    @Column(name = "Text2", unique = false, nullable = true, insertable = true, updatable = true, length = 65535)
    public String getText2() {
        return text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    @Column(name = "Integer1", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getInteger1() {
        return integer1;
    }

    public void setInteger1(Integer integer1) {
        this.integer1 = integer1;
    }

    @Column(name = "Integer2", unique = false, nullable = true, insertable = true, updatable = true)
    public Integer getInteger2() {
        return integer2;
    }

    public void setInteger2(Integer integer2) {
        this.integer2 = integer2;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "Date1", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate1() {
        return date1;
    }

    public void setDate1(Calendar date1) {
        this.date1 = date1;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "Date2", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getDate2() {
        return date2;
    }

    public void setDate2(Calendar date2) {
        this.date2 = date2;
    }

    @Column(name = "Date1Precision", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getDate1Precision() {
        return date1Precision;
    }

    public void setDate1Precision(Byte date1Precision) {
        this.date1Precision = date1Precision;
    }

    @Column(name = "Date2Precision", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getDate2Precision() {
        return date2Precision;
    }

    public void setDate2Precision(Byte date2Precision) {
        this.date2Precision = date2Precision;
    }

    @Column(name = "VerbatimDate1", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getVerbatimDate1() {
        return verbatimDate1;
    }

    public void setVerbatimDate1(String verbatimDate1) {
        this.verbatimDate1 = verbatimDate1;
    }

    @Column(name = "VerbatimDate2", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getVerbatimDate2() {
        return verbatimDate2;
    }

    public void setVerbatimDate2(String verbatimDate2) {
        this.verbatimDate2 = verbatimDate2;
    }
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
    
    /**
     * @param a
     */
    /*public Agent(final Agent a) throws CloneNotSupportedException
    {
        DataGetterForObj getter = new DataGetterForObj();
        DataSetterForObj setter = new DataSetterForObj();
        
        for (Field field : Agent.class.getDeclaredFields())
        {
            Object val = getter.getFieldValue(a, field.getName());
            if (!(val instanceof Set<?>))
            {
                setter.setFieldValue(this, field.getName(), val);
            }
        }
        
        cloneSets(a, this);
    }*/

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
        dateType                  = null;
        dateOfBirth               = null;
        dateOfBirthPrecision      = null;
        dateOfDeath               = null;
        dateOfDeathPrecision      = null;
        interests                 = null;
        abbreviation              = null;
        initials                  = null;
        suffix                    = null;
        remarks                   = null;
        guid                      = null;
        orgMembers                = new HashSet<Agent>();
        organization              = null;
        groups                    = new HashSet<GroupPerson>();
        members                   = new HashSet<GroupPerson>();
        collectors                = new HashSet<Collector>();
        text1 = null;
        integer1 = null;
        date1 = null;
        date1Precision = null;
        verbatimDate1 = null;
        text2 = null;
        text3 = null;
        text4 = null;
        text5 = null;
        integer2 = null;
        date2 = null;
        date2Precision = null;
        verbatimDate2 = null;


        division                  = null;
        instTechContact           = null;
        instContentContact        = null;
        collTechContact           = null;
        collContentContact        = null;
        specifyUser               = null;
       
        // Agent
        jobTitle                       = null;
        email                          = null;
        url                            = null;
        remarks                        = null;
        addresses                      = new HashSet<Address>();
        agentAttachments               = new HashSet<AgentAttachment>();
        variants                       = new HashSet<AgentVariant>();
        agentGeographies               = new HashSet<AgentGeography>();
        agentSpecialties               = new HashSet<AgentSpecialty>();
        identifiers                    = new HashSet<AgentIdentifier>();

        hasGUIDField = true;
        setGUID();
        
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#forceLoad()
     */
    @Override
    public void forceLoad()
    {
        // Do not override this method
        super.forceLoad();
        
        agentAttachments.size();
        
        if (groups != null)
        {
            groups.size();
        }
        if (members != null)
        {
            members.size();
        }
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
    @Column(name = "LastName", length = 256)
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
     * @return the dateType
     */
    @Column(name = "DateType", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getDateType()
    {
        return dateType;
    }

    /**
     * @param dateType the dateType to set
     */
    public void setDateType(Byte dateType)
    {
        this.dateType = dateType;
    }

    /**
     * @return the dateOfBirth
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DateOfBirth", unique = false, nullable = true, insertable = true, updatable = true)
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
     * @return the dateOfBirthPrecision
     */
    @Column(name = "DateOfBirthPrecision", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getDateOfBirthPrecision()
    {
        return dateOfBirthPrecision;
    }

    /**
     * @param dateOfBirthPrecision the dateOfBirthPrecision to set
     */
    public void setDateOfBirthPrecision(Byte dateOfBirthPrecision)
    {
        this.dateOfBirthPrecision = dateOfBirthPrecision;
    }

    /**
     * @return the dateOfDeath
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "DateOfDeath", unique = false, nullable = true, insertable = true, updatable = true)
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
     * @return the dateOfDeathPrecision
     */
    @Column(name = "DateOfDeathPrecision", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getDateOfDeathPrecision()
    {
        return dateOfDeathPrecision;
    }

    /**
     * @param dateOfDeathPrecision the dateOfDeathPrecision to set
     */
    public void setDateOfDeathPrecision(Byte dateOfDeathPrecision)
    {
        this.dateOfDeathPrecision = dateOfDeathPrecision;
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
	 * @return the suffix
	 */
    @Column(name = "Suffix", length = 50)
	public String getSuffix() {
		return suffix;
	}

	/**
	 * @param suffix the suffix to set
	 */
	public void setSuffix(String suffix) {
		this.suffix = suffix;
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
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
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
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
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
   @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
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
    *  The Institution for Technical Contact.
    */
   @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
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
   @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
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
   @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
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
   @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
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


    @Column(name = "GUID", unique = false, nullable = true, insertable = true, updatable = false, length = 128)
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
     * @return the identifiers
     */
    @OneToMany(mappedBy = "agent")
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<AgentIdentifier> getIdentifiers()
    {
        return identifiers;
    }

    /**
     * @param identifiers the identifiers to set
     */
    public void setIdentifiers(Set<AgentIdentifier> identifiers)
    {
        this.identifiers = identifiers;
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentTableId()
     */
    @Override
    @Transient
    public int getAttachmentTableId()
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentReferences()
     */
    @Transient
    @Override
    public Set<AgentAttachment> getAttachmentReferences()
    {
        return agentAttachments;
    }
    
    /**
     * @param src
     * @param dst
     */
    private void cloneSets(final Agent src, final Agent dst) throws CloneNotSupportedException
    {
        for (Address addr : src.addresses)
        {
            Address newAddr = (Address)addr.clone();
            dst.addresses.add(newAddr);
            newAddr.setAgent(dst);
        }
        
        for (AgentVariant cObj : src.variants)
        {
            AgentVariant newObj = (AgentVariant)cObj.clone();
            dst.variants.add(newObj);
            newObj.setAgent(dst);
        }
        
        for (AgentIdentifier cObj : src.identifiers)
        {
            AgentIdentifier newObj = (AgentIdentifier)cObj.clone();
            dst.identifiers.add(newObj);
            newObj.setAgent(dst);
        }
        
        for (AgentGeography cObj : src.agentGeographies)
        {
            AgentGeography newObj = (AgentGeography)cObj.clone();
            dst.agentGeographies.add(newObj);
            newObj.setAgent(dst);
        }
        
        for (AgentSpecialty cObj : src.agentSpecialties)
        {
            AgentSpecialty newObj = (AgentSpecialty)cObj.clone();
            dst.agentSpecialties.add(newObj);
            newObj.setAgent(dst);
        }
        
        for (Collector cObj : src.collectors)
        {
            Collector newObj = (Collector)cObj.clone();
            dst.collectors.add(newObj);
            newObj.setAgent(dst);
        }
        
        for (GroupPerson cObj : src.members)
        {
            GroupPerson newObj = (GroupPerson)cObj.clone();
            dst.members.add(newObj);
            newObj.setMember(dst);
        }
        
        for (GroupPerson cObj : src.groups)
        {
            GroupPerson newObj = (GroupPerson)cObj.clone();
            dst.groups.add(newObj);
            newObj.setGroup(dst);
        }
        
        for (Agent cObj : src.orgMembers)
        {
            Agent newObj = (Agent)cObj.clone();
            dst.orgMembers.add(newObj);
            newObj.setOrganization(dst);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        Agent obj = (Agent)super.clone();
        
        obj.agentId = null;
        
        initializeSets(obj);
        
        cloneSets(this, obj);
        
        return obj;
    }

    /**
     *
     * @param originalObj
     * @param deep  if true then copy and clone children
     * @param session
     * @return
     */
    @Override
    public boolean initializeClone(DataModelObjBase originalObj, boolean deep, DataProviderSessionIFace session) {
        if (deep) {
            log.error(getClass().getName() + ": initializeClone is not implemented for deep = true.");
            return false;
        }
        this.specifyUser = null;
        this.collectors.clear();
        setGUID();
        return true;
    }

    /**
     * @param obj
     */
    public static void initializeSets(final Agent obj)
    {
        obj.orgMembers                = new HashSet<Agent>();
        obj.groups                    = new HashSet<GroupPerson>();
        obj.members                   = new HashSet<GroupPerson>();
        obj.collectors                = new HashSet<Collector>();
        
        // Agent
        obj.addresses                 = new HashSet<Address>();
        obj.agentAttachments          = new HashSet<AgentAttachment>();
        obj.variants                  = new HashSet<AgentVariant>();
        obj.identifiers               = new HashSet<AgentIdentifier>();
        obj.agentGeographies          = new HashSet<AgentGeography>();
        obj.agentSpecialties          = new HashSet<AgentSpecialty>();
    }
    
    /**
     * @return the userAgent
     */
    public static Agent getUserAgent()
    {
        return userAgent;
    }

    /**
     * Make all agents in a set point to a single specifyUser.
     * @param user User that Agent will point to
     * @param division current Division
     * @return true if one was found or created
     */
    public static boolean setUserAgent(final SpecifyUser user, 
                                       final Division division)
    {
        
        String sql = "SELECT a.AgentID FROM agent AS a WHERE a.DivisionID = " + division.getId() + " AND a.SpecifyUserID = " + user.getId();
        
        boolean notFndErr = false;
        userAgent = null;
        
        Integer agentId = BasicSQLUtils.getCount(sql); // gets the AgentId
        if (agentId != null)
        {
            userAgent = getDataObj(Agent.class, agentId);
            if (userAgent == null)
            {
                UIRegistry.showError("A user agent was not found for the SpecifyUser for division["+division.getName()+"] and Agent id ["+agentId+"]");
                notFndErr = true;
            }
        } else
        {
            UIRegistry.showError("A user agent was not found for the SpecifyUser for division["+division.getName()+"]");
            notFndErr = true;
        }
        
        if (notFndErr)
        {
            AgentBusRules.createUserAgent(user, division);
            
            UIRegistry.showLocalizedMsg("Specify.ABT_EXIT");
            CommandDispatcher.dispatch(new CommandAction("App", "AppReqExit"));
            return false;
        }
        return true;
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
