package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.Set;




/**
 *        @hibernate.class
 *         table="address"
 *     
 */
public class Address  implements java.io.Serializable {

    // Fields    

     protected Integer addressId;
     protected String address;
     protected String address2;
     protected String city;
     protected String state;
     protected String country;
     protected String postalCode;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     private Set agentAddresses;


    // Constructors

    /** default constructor */
    public Address() {
    }
    
    /** constructor with id */
    public Address(Integer addressId) {
        this.addressId = addressId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="native"
     *             type="java.lang.Integer"
     *             column="AddressID"
     *         
     */
    public Integer getAddressId() {
        return this.addressId;
    }
    
    public void setAddressId(Integer addressId) {
        this.addressId = addressId;
    }

    /**
     *      *             @hibernate.property
     *             column="Address"
     *             not-null="true"
     *             length="255"
     *         
     */
    public String getAddress() {
        return this.address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     *      *             @hibernate.property
     *             column="Address"
     *             not-null="true"
     *             length="255"
     *         
     */
    public String getAddress2() {
        return this.address2;
    }
    
    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    /**
     *      *            @hibernate.property
     *             column="City"
     *             length="64"
     *         
     */
    public String getCity() {
        return this.city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }

    /**
     *      *            @hibernate.property
     *             column="State"
     *             length="64"
     *         
     */
    public String getState() {
        return this.state;
    }
    
    public void setState(String state) {
        this.state = state;
    }

    /**
     *      *            @hibernate.property
     *             column="Country"
     *             length="64"
     *         
     */
    public String getCountry() {
        return this.country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     *      *            @hibernate.property
     *             column="PostalCode"
     *             length="32"
     *         
     */
    public String getPostalCode() {
        return this.postalCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    /**
     *      *            @hibernate.property
     *             column="Remarks"
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
     *             update="false"
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
     *             column="AddressID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.AgentAddress"
     *         
     */
    public Set getAgentAddresses() {
        return this.agentAddresses;
    }
    
    public void setAgentAddresses(Set agentAddresses) {
        this.agentAddresses = agentAddresses;
    }




}