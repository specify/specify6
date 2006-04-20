package edu.ku.brc.specify.datamodel;

import java.util.Date;




/**

 */
public class Address  implements java.io.Serializable {

    // Fields

    protected Integer           addressId;
    protected String            address;
    protected String            address2;
    protected String            city;
    protected String            state;
    protected String            country;
    protected String            postalCode;
    protected String            remarks;
    protected Date              timestampModified;
    protected Date              timestampCreated;
    protected String            lastEditedBy;
    protected Agent             agent;

    // From Agent
    protected Boolean           isPrimary;
    protected String            phone1;
    protected String            phone2;
    protected String            fax;
    protected String            roomOrBuilding;
     

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
        timestampModified = new Date();
        timestampCreated = new Date();
        lastEditedBy = null;
        agent = null;
        
        // Agent
        isPrimary = false;
        phone1 = null;
        phone2 = null;
        fax = null;
        roomOrBuilding = null;
        
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
    public String getPhone1() {
        return this.phone1;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    /**
     *
     */
    public String getPhone2() {
        return this.phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    /**
     *
     */
    public String getFax() {
        return this.fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    /**
     *
     */
    public String getRoomOrBuilding() {
        return this.roomOrBuilding;
    }

    public void setRoomOrBuilding(String roomOrBuilding) {
        this.roomOrBuilding = roomOrBuilding;
    }
    

    /**
     *      * Is the agent currently located at this address?
     */
    public Boolean getIsPrimary() {
        return this.isPrimary;
    }

    public void setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

}
