package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="address"
 *     
 */
public class Address  implements java.io.Serializable {

    // Fields    

     protected Integer addressId;
     protected String address;
     protected String city;
     protected String state;
     protected String country;
     protected String postalcode;
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
     *             generator-class="assigned"
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
     *      *            @hibernate.property
     *             column="Address"
     *             not-null="true"
     *         
     */
    public String getAddress() {
        return this.address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     *      *            @hibernate.property
     *             column="City"
     *             length="32"
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
     *             length="32"
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
     *             length="32"
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
     *             column="Postalcode"
     *             length="32"
     *         
     */
    public String getPostalcode() {
        return this.postalcode;
    }
    
    public void setPostalcode(String postalcode) {
        this.postalcode = postalcode;
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