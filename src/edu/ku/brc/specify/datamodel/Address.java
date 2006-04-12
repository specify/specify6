package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;




/**

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
     protected Set<AgentAddress> agentAddresses;


    // Constructors

    /** default constructor */
    public Address() {
    }

    /** constructor with id */
    public Address(Integer addressId) {
        this.addressId = addressId;
    }




    // Initializer
    public void initialize()
    {
        addressId = null;
        address = null;
        address2 = null;
        city = null;
        state = null;
        country = null;
        postalCode = null;
        remarks = null;
        timestampModified = null;
        timestampCreated = Calendar.getInstance().getTime();
        lastEditedBy = null;
        agentAddresses = new HashSet<AgentAddress>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * PrimaryKey
     */
    public Integer getAddressId() {
        return this.addressId;
    }

    public void setAddressId(Integer addressId) {
        this.addressId = addressId;
    }

    /**
     *      * Address as it should appear on mailing labels
     */
    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /**
     *
     */
    public String getAddress2() {
        return this.address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    /**
     *
     */
    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    /**
     *
     */
    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     *
     */
    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    /**
     *
     */
    public String getPostalCode() {
        return this.postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
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
    public Set<AgentAddress> getAgentAddresses() {
        return this.agentAddresses;
    }

    public void setAgentAddresses(Set<AgentAddress> agentAddresses) {
        this.agentAddresses = agentAddresses;
    }




    // Add Methods

    public void addAgentAddress(final AgentAddress agentAddresses)
    {
        this.agentAddresses.add(agentAddresses);
    }

    // Done Add Methods
}
