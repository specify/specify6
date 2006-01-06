package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="collection"
 *     
 */
public class Collection  implements java.io.Serializable {

    // Fields    

     protected Integer collectionId;
     protected String collectionName;
     protected String collectionIdentifier;
     protected String description;
     protected String remarks;
     protected String text1;
     protected String text2;
     protected Float number1;
     protected Float number2;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     protected Short yesNo1;
     protected Short yesNo2;
     private Set catalogSeries;
     private Set deaccessions;
     private Set accessions;
     private Set exchangeIns;
     private Set collectionTaxonomyTypes;
     private Set exchangeOuts;
     private Agent agent;


    // Constructors

    /** default constructor */
    public Collection() {
    }
    
    /** constructor with id */
    public Collection(Integer collectionId) {
        this.collectionId = collectionId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="CollectionID"
     *         
     */
    public Integer getCollectionId() {
        return this.collectionId;
    }
    
    public void setCollectionId(Integer collectionId) {
        this.collectionId = collectionId;
    }

    /**
     *      *            @hibernate.property
     *             column="CollectionName"
     *             length="50"
     *         
     */
    public String getCollectionName() {
        return this.collectionName;
    }
    
    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    /**
     *      *            @hibernate.property
     *             column="CollectionIdentifier"
     *             length="50"
     *         
     */
    public String getCollectionIdentifier() {
        return this.collectionIdentifier;
    }
    
    public void setCollectionIdentifier(String collectionIdentifier) {
        this.collectionIdentifier = collectionIdentifier;
    }

    /**
     *      *            @hibernate.property
     *             column="Description"
     *             length="1073741823"
     *         
     */
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
     *             column="Text1"
     *             length="300"
     *         
     */
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      *            @hibernate.property
     *             column="Text2"
     *             length="300"
     *         
     */
    public String getText2() {
        return this.text2;
    }
    
    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      *            @hibernate.property
     *             column="Number1"
     *             length="24"
     *         
     */
    public Float getNumber1() {
        return this.number1;
    }
    
    public void setNumber1(Float number1) {
        this.number1 = number1;
    }

    /**
     *      *            @hibernate.property
     *             column="Number2"
     *             length="24"
     *         
     */
    public Float getNumber2() {
        return this.number2;
    }
    
    public void setNumber2(Float number2) {
        this.number2 = number2;
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
     *      *            @hibernate.property
     *             column="YesNo1"
     *             length="5"
     *         
     */
    public Short getYesNo1() {
        return this.yesNo1;
    }
    
    public void setYesNo1(Short yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      *            @hibernate.property
     *             column="YesNo2"
     *             length="5"
     *         
     */
    public Short getYesNo2() {
        return this.yesNo2;
    }
    
    public void setYesNo2(Short yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="CollectionID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.CatalogSeries"
     *         
     */
    public Set getCatalogSeries() {
        return this.catalogSeries;
    }
    
    public void setCatalogSeries(Set catalogSeries) {
        this.catalogSeries = catalogSeries;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="CollectionID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Deaccession"
     *         
     */
    public Set getDeaccessions() {
        return this.deaccessions;
    }
    
    public void setDeaccessions(Set deaccessions) {
        this.deaccessions = deaccessions;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="CollectionID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Accession"
     *         
     */
    public Set getAccessions() {
        return this.accessions;
    }
    
    public void setAccessions(Set accessions) {
        this.accessions = accessions;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="CollectionID"
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
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="CollectionID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.CollectionTaxonomyType"
     *         
     */
    public Set getCollectionTaxonomyTypes() {
        return this.collectionTaxonomyTypes;
    }
    
    public void setCollectionTaxonomyTypes(Set collectionTaxonomyTypes) {
        this.collectionTaxonomyTypes = collectionTaxonomyTypes;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="CollectionID"
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
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="OrganizationID"         
     *         
     */
    public Agent getAgent() {
        return this.agent;
    }
    
    public void setAgent(Agent agent) {
        this.agent = agent;
    }




}