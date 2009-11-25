/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

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

import edu.ku.brc.specify.conversion.BasicSQLUtils;

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
public class AddressOfRecord extends DataModelObjBase
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
    @OneToMany( mappedBy = "addressOfRecord")
    //@Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
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
    @OneToMany( mappedBy = "addressOfRecord")
    //@Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
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
    @OneToMany( mappedBy = "addressOfRecord")
    //@Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
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
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        Vector<Object> ids = BasicSQLUtils.querySingleCol("SELECT AccessionID FROM accession WHERE AddressOfRecordID = "+ addressOfRecordId);
        if (ids.size() == 1)
        {
            parentTblId = Accession.getClassTableId();
            return (Integer)ids.get(0);
        }
        ids = BasicSQLUtils.querySingleCol("SELECT RepositoryAgreementID FROM repositoryagreement WHERE AddressOfRecordID = "+ addressOfRecordId);
        if (ids.size() == 1)
        {
            parentTblId = RepositoryAgreement.getClassTableId();
            return (Integer)ids.get(0);
        }
        ids = BasicSQLUtils.querySingleCol("SELECT LoanID FROM loan WHERE AddressOfRecordID = "+ addressOfRecordId);
        if (ids.size() == 1)
        {
            parentTblId = Loan.getClassTableId();
            return (Integer)ids.get(0);
        }
        ids = BasicSQLUtils.querySingleCol("SELECT ExchangeInID FROM exchangein WHERE AddressOfRecordID = "+ addressOfRecordId);
        if (ids.size() == 1)
        {
            parentTblId = ExchangeIn.getClassTableId();
            return (Integer)ids.get(0);
        }
        ids = BasicSQLUtils.querySingleCol("SELECT ExchangeOutID FROM exchangeout WHERE AddressOfRecordID = "+ addressOfRecordId);
        if (ids.size() == 1)
        {
            parentTblId = ExchangeOut.getClassTableId();
            return (Integer)ids.get(0);
        }
        parentTblId = null;
        return null;
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
