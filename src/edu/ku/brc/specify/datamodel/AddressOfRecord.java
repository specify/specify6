/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
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

import org.hibernate.annotations.Index;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 17, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "addressofrecord")
@org.hibernate.annotations.Table(appliesTo="addressofrecord", indexes =
    {   
        @Index (name="AddrColMemIDX", columnNames={"CollectionMemberID"})
    })
public class AddressOfRecord extends CollectionMember
{
    protected Integer                  addressOfRecordId;
    protected String                   address;
    protected String                   address2;
    protected String                   city;
    protected String                   state;
    protected String                   country;
    protected String                   postalCode;
    protected String                   remarks;

    protected Agent                    agent;
    protected Set<Accession>           accessions;
    protected Set<RepositoryAgreement> repositoryAgreements;
    protected Set<Loan>                loans;
    protected Set<ExchangeIn>          exchangeIns;
    protected Set<ExchangeOut>         exchangeOuts;
     
    /**
     * 
     */
    public AddressOfRecord()
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
        
        addressOfRecordId    = null;
        address              = null;
        address2             = null;
        city                 = null;
        state                = null;
        country              = null;
        postalCode           = null;
        remarks              = null;
        agent                = null;
        
        accessions           = new HashSet<Accession>();
        repositoryAgreements = new HashSet<RepositoryAgreement>();
        loans                = new HashSet<Loan>();
        exchangeIns          = new HashSet<ExchangeIn>();
        exchangeOuts         = new HashSet<ExchangeOut>();
    }
    
    /**
     * @return the addressOfRecordId
     */
    @Id
    @GeneratedValue
    @Column(name = "AddressOfRecordID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getAddressOfRecordId()
    {
        return addressOfRecordId;
    }

    /**
     * @param addressOfRecordId the addressOfRecordId to set
     */
    public void setAddressOfRecordId(Integer addressOfRecordId)
    {
        this.addressOfRecordId = addressOfRecordId;
    }

    /**
     * Address as it should appear on mailing labels
     */
    @Column(name = "Address", unique = false, nullable = true, insertable = true, updatable = true)
    public String getAddress()
    {
        return this.address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    /**
     * 
     */
    @Column(name = "Address2", unique = false, nullable = true, insertable = true, updatable = true)
    public String getAddress2()
    {
        return this.address2;
    }

    public void setAddress2(String address2)
    {
        this.address2 = address2;
    }

    /**
     * 
     */
    @Column(name = "City", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getCity()
    {
        return this.city;
    }

    public void setCity(String city)
    {
        this.city = city;
    }

    /**
     * 
     */
    @Column(name = "State", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getState()
    {
        return this.state;
    }

    public void setState(String state)
    {
        this.state = state;
    }

    /**
     * 
     */
    @Column(name = "Country", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getCountry()
    {
        return this.country;
    }

    public void setCountry(String country)
    {
        this.country = country;
    }

    /**
     * 
     */
    @Column(name = "PostalCode", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getPostalCode()
    {
        return this.postalCode;
    }

    public void setPostalCode(String postalCode)
    {
        this.postalCode = postalCode;
    }

    /**
     * 
     */
    @Lob
    @Column(name = "Remarks", length = 4096)
    public String getRemarks()
    {
        return this.remarks;
    }

    public void setRemarks(String remarks)
    {
        this.remarks = remarks;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AgentID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent()
    {
        return this.agent;
    }

    /**
     * @param agent
     */
    public void setAgent(Agent agent)
    {
        this.agent = agent;
    }
    
    /**
     * @return the accessions
     */
    @OneToMany(cascade = {javax.persistence.CascadeType.ALL}, mappedBy = "addressOfRecord")
    public Set<Accession> getAccessions()
    {
        return accessions;
    }

    /**
     * @param accessions the accessions to set
     */
    public void setAccessions(Set<Accession> accessions)
    {
        this.accessions = accessions;
    }

    /**
     * @return the repositoryAgreements
     */
    @OneToMany(cascade = {javax.persistence.CascadeType.ALL}, mappedBy = "addressOfRecord")
    public Set<RepositoryAgreement> getRepositoryAgreements()
    {
        return repositoryAgreements;
    }

    /**
     * @param repositoryAgreements the repositoryAgreements to set
     */
    public void setRepositoryAgreements(Set<RepositoryAgreement> repositoryAgreements)
    {
        this.repositoryAgreements = repositoryAgreements;
    }

    /**
     * @return the loans
     */
    @OneToMany(cascade = {javax.persistence.CascadeType.ALL}, mappedBy = "addressOfRecord")
    public Set<Loan> getLoans()
    {
        return loans;
    }

    /**
     * @param loans the loans to set
     */
    public void setLoans(Set<Loan> loans)
    {
        this.loans = loans;
    }

    /**
     * @return the exchangeIns
     */
    @OneToMany(cascade = {javax.persistence.CascadeType.ALL}, mappedBy = "addressOfRecord")
    public Set<ExchangeIn> getExchangeIns()
    {
        return exchangeIns;
    }

    /**
     * @param exchangeIns the exchangeIns to set
     */
    public void setExchangeIns(Set<ExchangeIn> exchangeIns)
    {
        this.exchangeIns = exchangeIns;
    }

    /**
     * @return the exchangeOuts
     */
    @OneToMany(cascade = {javax.persistence.CascadeType.ALL}, mappedBy = "addressOfRecord")
    public Set<ExchangeOut> getExchangeOuts()
    {
        return exchangeOuts;
    }

    /**
     * @param exchangeOuts the exchangeOuts to set
     */
    public void setExchangeOuts(Set<ExchangeOut> exchangeOuts)
    {
        this.exchangeOuts = exchangeOuts;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return AddressOfRecord.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    @Override
    @Transient
    public Integer getId()
    {
        return addressOfRecordId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Transient
    @Override
    public int getTableId()
    {
        return getClassTableId();
    }
    
    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 125;
    }
}
