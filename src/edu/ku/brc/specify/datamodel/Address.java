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

import java.util.Calendar;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.util.Orderable;

/**

 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "address")
public class Address extends DataModelObjBase implements Orderable,
                                                         Comparable<Address>,
                                                         java.io.Serializable,
                                                         Cloneable
{

    // Fields

    protected Integer           addressId;
    protected String            address;
    protected String            address2;
    protected String            address3;
    protected String            address4;
    protected String            address5;
    protected String            city;
    protected String            state;
    protected String            country;
    protected String            postalCode;
    protected String            remarks;
    protected Agent             agent;
    protected Integer           ordinal;

    // From Agent
    protected Boolean           isPrimary;
    protected String            phone1;
    protected String            phone2;
    protected String            fax;
    protected String            roomOrBuilding;
     
    // New Fields
    protected String            typeOfAddr;    // most likely value is "of record" this describes what the address was used for.
    protected Boolean           isCurrent;
    protected Boolean           isShipping;
    protected Calendar          startDate;
    protected Calendar          endDate;
    protected String            positionHeld;  // their previous position held
    protected Set<Institution>  insitutions;
    protected Set<Division>     divisions;

    // Constructors

    /** default constructor */
    public Address() 
    {
        //
    }

    /** constructor with id */
    public Address(Integer addressId) 
    {
        this.addressId = addressId;
    }

    // Initializer
    @Override
    public void initialize()
    {
    	//NOTE: if fields are added to this table, the matches method must be updated accordingly!!!! 
        super.init();
        addressId = null;
        address = null;
        address2 = null;
        address3 = null;
        address4 = null;
        address5 = null;
        city = null;
        state = null;
        country = null;
        postalCode = null;
        remarks = null;
        agent   = null;
        ordinal = null;

        // Agent
        isPrimary = false;
        phone1 = null;
        phone2 = null;
        fax = null;
        roomOrBuilding = null;
        
        // New
        startDate = null;
        endDate = null;
        positionHeld = null;
        isCurrent = null;
        isShipping = null;
        typeOfAddr = null;
        insitutions = new HashSet<Institution>();
        divisions   = new HashSet<Division>();
    	//NOTE: if fields are added to this table, the matches method must be updated accordingly!!!! 
       
    }
    // End Initializer

    // Property accessors

    /**
     *      * PrimaryKey
     */
    @Id
    @GeneratedValue
    @Column(name = "AddressID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getAddressId() {
        return this.addressId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.addressId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Address.class;
    }

    public void setAddressId(Integer addressId) {
        this.addressId = addressId;
    }

    /**
     *      * Address as it should appear on mailing labels
     */
    @Column(name = "Address", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /**
     *
     */
    @Column(name = "Address2", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getAddress2() {
        return this.address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    /**
     *
     */
    @Column(name = "Address3", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getAddress3() {
        return this.address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    /**
     *
     */
    @Column(name = "Address4", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getAddress4() {
        return this.address4;
    }

    public void setAddress4(String address4) {
        this.address4 = address4;
    }

    /**
     *
     */
    @Column(name = "Address5", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getAddress5() {
        return this.address5;
    }

    public void setAddress5(String address5) {
        this.address5 = address5;
    }

    /**
     *
     */
    @Column(name = "City", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    /**
     *
     */
    @Column(name = "State", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     *
     */
    @Column(name = "Country", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    /**
     *
     */
    @Column(name = "PostalCode", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getPostalCode() {
        return this.postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
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
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AgentID", unique = false, nullable = true, insertable = true, updatable = true)
    public Agent getAgent() {
        return this.agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    // Agent

    /**
     *
     */
    @Column(name = "Phone1", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getPhone1() {
        return this.phone1;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    /**
     *
     */
    @Column(name = "Phone2", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getPhone2() {
        return this.phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    /**
     *
     */
    @Column(name = "Fax", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getFax() {
        return this.fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    /**
     *
     */
    @Column(name = "RoomOrBuilding", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getRoomOrBuilding() {
        return this.roomOrBuilding;
    }

    public void setRoomOrBuilding(String roomOrBuilding) {
        this.roomOrBuilding = roomOrBuilding;
    }
    

    /**
     *      * Is the agent currently located at this address?
     */
    @Column(name="IsPrimary",unique=false,nullable=true,insertable=true,updatable=true)
    public Boolean getIsPrimary() 
    {
        return this.isPrimary == null ? false : this.isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }
    
    /**
     * @return the endDate
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "EndDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getEndDate()
    {
        return endDate;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(Calendar endDate)
    {
        this.endDate = endDate;
    }

    /**
     * @return the startDate
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "StartDate", unique = false, nullable = true, insertable = true, updatable = true)
    public Calendar getStartDate()
    {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(Calendar startDate)
    {
        this.startDate = startDate;
    }

    /**
     * @return the position to set
     */
    @Column(name = "PositionHeld", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getPositionHeld()
    {
        return positionHeld;
    }

    /**
     * @param positionHeld position held at the time
     */
    public void setPositionHeld(String positionHeld)
    {
        this.positionHeld = positionHeld;
    }
    
    @Column(name = "Ordinal")
    public Integer getOrdinal()
    {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal)
    {
        this.ordinal = ordinal;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.util.Orderable#getOrderIndex()
     */
    @Transient
    public int getOrderIndex()
    {
        return (this.ordinal != null) ? this.ordinal : 0;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.Orderable#setOrderIndex(int)
     */
    public void setOrderIndex(int ordinal)
    {
        this.ordinal = ordinal;
    }

    /**
     * @param sb
     * @param val
     */
    public static void append(final StringBuilder sb, final String val)
    {
        if (StringUtils.isNotEmpty(val))
        {
            if (sb.length() > 0)
            {
                sb.append("; ");
            }
            sb.append(val);
        }
    }
    
    /**
     * @return the typeOfAddr
     */
    @Column(name = "TypeOfAddr", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getTypeOfAddr()
    {
        return typeOfAddr;
    }

    /**
     * @param typeOfAddr the typeOfAddr to set
     */
    public void setTypeOfAddr(String typeOfAddr)
    {
        this.typeOfAddr = typeOfAddr;
    }

    /**
     * @return the isCurrent
     */
    @Column(name = "IsCurrent", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIsCurrent()
    {
        return isCurrent;
    }

    /**
     * @param isCurrent the isCurrent to set
     */
    public void setIsCurrent(Boolean isCurrent)
    {
        this.isCurrent = isCurrent;
    }

    /**
     * @return the isShipping
     */
    @Column(name = "IsShipping", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIsShipping()
    {
        return isShipping;
    }

    /**
     * @param isShipping the isShipping to set
     */
    public void setIsShipping(Boolean isShipping)
    {
        this.isShipping = isShipping;
    }
    
    /**
     * @return the insitutions
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "address")
    //@Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<Institution> getInsitutions()
    {
        return insitutions;
    }

    /**
     * @param insitutions the insitutions to set
     */
    public void setInsitutions(Set<Institution> insitutions)
    {
        this.insitutions = insitutions;
    }

    /**
     * @return the divisions
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "address")
    //@Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public Set<Division> getDivisions()
    {
        return divisions;
    }

    /**
     * @param divisions the divisions to set
     */
    public void setDivisions(Set<Division> divisions)
    {
        this.divisions = divisions;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        if (agent != null)
        {
            parentTblId = Agent.getClassTableId();
            return agent.getId();
        }
        
        Vector<Object> ids = BasicSQLUtils.querySingleCol("SELECT InstitutionID FROM institution WHERE AddressID = "+ addressId);
        if (ids.size() == 1)
        {
            parentTblId = Institution.getClassTableId();
            return (Integer)ids.get(0);
        }
        
        ids = BasicSQLUtils.querySingleCol("SELECT DivisionID FROM division WHERE AddressID = "+ addressId);
        if (ids.size() == 1)
        {
            parentTblId = Division.getClassTableId();
            return (Integer)ids.get(0);
        }
        parentTblId = null;
        return null;
    }
    
    @Override
    @Transient
    public String getIdentityTitle()
    {
        StringBuilder sb = new StringBuilder();
        
        append(sb, address);
        append(sb, address2);
        append(sb, city);
        append(sb, state);
        append(sb, country);
        append(sb, postalCode);

        if (sb.length() > 0)
        {
            return sb.toString();
        }
        return super.getIdentityTitle();
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
        return 8;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Address obj)
    {
        if (ordinal != null && obj != null && obj.ordinal != null)
        {
            return ordinal.compareTo(obj.ordinal);
        }
        return 0;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        Address obj = (Address)super.clone();
        
        obj.addressId            = null;
        obj.agent                = null;
        obj.insitutions          = new HashSet<Institution>();
        obj.divisions            = new HashSet<Division>();
       
        return obj;
    }
    
    /**
     * @param o
     * @return true if 'non-system' fields all match.
     */
    public boolean matches(Address o)
    {
        if (o == null)
        {
        	return false;
        }
        
        //XXX not comparing agent. For current usage of this method Agent doesn't need to be compared.
    	return
        	((address == null && o.address == null) || ((address != null && o.address != null) && address.equals(o.address))) &&
        	((address2 == null && o.address2 == null) || ((address2 != null && o.address2 != null) && address2.equals(o.address2))) &&
        	((address3 == null && o.address3 == null) || ((address3 != null && o.address3 != null) && address3.equals(o.address3))) &&
        	((address4 == null && o.address4 == null) || ((address4 != null && o.address4 != null) && address4.equals(o.address4))) &&
        	((address5 == null && o.address5 == null) || ((address5 != null && o.address5 != null) && address5.equals(o.address5))) &&
        	((city == null && o.city == null) || ((city != null && o.city != null) && city.equals(o.city))) &&
        	((state == null && o.state == null) || ((state != null && o.state != null) && state.equals(o.state))) &&
        	((country == null && o.country == null) || ((country != null && o.country != null) && country.equals(o.country))) &&
        	((postalCode == null && o.postalCode == null) || ((postalCode != null && o.postalCode != null) && postalCode.equals(o.postalCode))) &&
        	((remarks == null && o.remarks == null) || ((remarks != null && o.remarks != null) && remarks.equals(o.remarks))) &&
        	((isPrimary == null && o.isPrimary == null) || ((isPrimary != null && o.isPrimary != null) && isPrimary.equals(o.isPrimary))) &&
        	((phone1 == null && o.phone1 == null) || ((phone1 != null && o.phone1 != null) && phone1.equals(o.phone1))) &&
        	((phone2 == null && o.phone2 == null) || ((phone2 != null && o.phone2 != null) && phone2.equals(o.phone2))) &&
        	((fax == null && o.fax == null) || ((fax != null && o.fax != null) && fax.equals(o.fax))) &&
        	((roomOrBuilding == null && o.roomOrBuilding == null) || ((roomOrBuilding != null && o.roomOrBuilding != null) && roomOrBuilding.equals(o.roomOrBuilding))) &&
        	((startDate == null && o.startDate == null) || ((startDate != null && o.startDate != null) && startDate.equals(o.startDate))) &&
        	((endDate == null && o.endDate == null) || ((endDate != null && o.endDate != null) && endDate.equals(o.endDate))) &&
        	((positionHeld == null && o.positionHeld == null) || ((positionHeld != null && o.positionHeld != null) && positionHeld.equals(o.positionHeld))) &&
        	((isCurrent == null && o.isCurrent == null) || ((isCurrent != null && o.isCurrent != null) && isCurrent.equals(o.isCurrent))) &&
        	((isShipping == null && o.isShipping == null) || ((isShipping != null && o.isShipping != null) && isShipping.equals(o.isShipping))) &&
        	((typeOfAddr == null && o.typeOfAddr == null) || ((typeOfAddr != null && o.typeOfAddr != null) && typeOfAddr.equals(o.typeOfAddr)));
    }
}
