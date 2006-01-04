package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="TaxonName"
 *     
 */
public class TaxonName  implements java.io.Serializable {

    // Fields    

     protected Integer taxonNameId;
     protected Integer parentTaxonNameId;
     protected String taxonomicSerialNumber;
     protected String taxonName;
     protected String unitInd1;
     protected String unitName1;
     protected String unitInd2;
     protected String unitName2;
     protected String unitInd3;
     protected String unitName3;
     protected String unitInd4;
     protected String unitName4;
     protected String fullTaxonName;
     protected String commonName;
     protected String author;
     protected String source;
     protected Integer groupPermittedToView;
     protected String environmentalProtectionStatus;
     protected String remarks;
     protected Integer nodeNumber;
     protected Integer highestChildNodeNumber;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected Short accepted;
     protected Integer rankId;
     protected String groupNumber;
     private Set taxonNames;
     private TaxonName taxonName1;
     private Set habitats;
     private Set taxonCitations;
     private Set preparations;
     private Set determinations;
     private TaxonomyType taxonomyType;
     private TaxonomicUnitType taxonomicUnitType;


    // Constructors

    /** default constructor */
    public TaxonName() {
    }
    
    /** constructor with id */
    public TaxonName(Integer taxonNameId) {
        this.taxonNameId = taxonNameId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="TaxonNameID"
     *         
     */
    public Integer getTaxonNameId() {
        return this.taxonNameId;
    }
    
    public void setTaxonNameId(Integer taxonNameId) {
        this.taxonNameId = taxonNameId;
    }

    /**
     *      *            @hibernate.property
     *             column="ParentTaxonNameID"
     *             length="10"
     *         
     */
    public Integer getParentTaxonNameId() {
        return this.parentTaxonNameId;
    }
    
    public void setParentTaxonNameId(Integer parentTaxonNameId) {
        this.parentTaxonNameId = parentTaxonNameId;
    }

    /**
     *      *            @hibernate.property
     *             column="TaxonomicSerialNumber"
     *             length="50"
     *         
     */
    public String getTaxonomicSerialNumber() {
        return this.taxonomicSerialNumber;
    }
    
    public void setTaxonomicSerialNumber(String taxonomicSerialNumber) {
        this.taxonomicSerialNumber = taxonomicSerialNumber;
    }

    /**
     *      *            @hibernate.property
     *             column="TaxonName"
     *             length="50"
     *         
     */
    public String getTaxonName() {
        return this.taxonName;
    }
    
    public void setTaxonName(String taxonName) {
        this.taxonName = taxonName;
    }

    /**
     *      *            @hibernate.property
     *             column="UnitInd1"
     *             length="50"
     *         
     */
    public String getUnitInd1() {
        return this.unitInd1;
    }
    
    public void setUnitInd1(String unitInd1) {
        this.unitInd1 = unitInd1;
    }

    /**
     *      *            @hibernate.property
     *             column="UnitName1"
     *             length="50"
     *         
     */
    public String getUnitName1() {
        return this.unitName1;
    }
    
    public void setUnitName1(String unitName1) {
        this.unitName1 = unitName1;
    }

    /**
     *      *            @hibernate.property
     *             column="UnitInd2"
     *             length="50"
     *         
     */
    public String getUnitInd2() {
        return this.unitInd2;
    }
    
    public void setUnitInd2(String unitInd2) {
        this.unitInd2 = unitInd2;
    }

    /**
     *      *            @hibernate.property
     *             column="UnitName2"
     *             length="50"
     *         
     */
    public String getUnitName2() {
        return this.unitName2;
    }
    
    public void setUnitName2(String unitName2) {
        this.unitName2 = unitName2;
    }

    /**
     *      *            @hibernate.property
     *             column="UnitInd3"
     *             length="50"
     *         
     */
    public String getUnitInd3() {
        return this.unitInd3;
    }
    
    public void setUnitInd3(String unitInd3) {
        this.unitInd3 = unitInd3;
    }

    /**
     *      *            @hibernate.property
     *             column="UnitName3"
     *             length="50"
     *         
     */
    public String getUnitName3() {
        return this.unitName3;
    }
    
    public void setUnitName3(String unitName3) {
        this.unitName3 = unitName3;
    }

    /**
     *      *            @hibernate.property
     *             column="UnitInd4"
     *             length="50"
     *         
     */
    public String getUnitInd4() {
        return this.unitInd4;
    }
    
    public void setUnitInd4(String unitInd4) {
        this.unitInd4 = unitInd4;
    }

    /**
     *      *            @hibernate.property
     *             column="UnitName4"
     *             length="50"
     *         
     */
    public String getUnitName4() {
        return this.unitName4;
    }
    
    public void setUnitName4(String unitName4) {
        this.unitName4 = unitName4;
    }

    /**
     *      *            @hibernate.property
     *             column="FullTaxonName"
     *             length="255"
     *         
     */
    public String getFullTaxonName() {
        return this.fullTaxonName;
    }
    
    public void setFullTaxonName(String fullTaxonName) {
        this.fullTaxonName = fullTaxonName;
    }

    /**
     *      *            @hibernate.property
     *             column="CommonName"
     *             length="100"
     *         
     */
    public String getCommonName() {
        return this.commonName;
    }
    
    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    /**
     *      *            @hibernate.property
     *             column="Author"
     *             length="100"
     *         
     */
    public String getAuthor() {
        return this.author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     *      *            @hibernate.property
     *             column="Source"
     *             length="50"
     *         
     */
    public String getSource() {
        return this.source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }

    /**
     *      *            @hibernate.property
     *             column="GroupPermittedToView"
     *             length="10"
     *         
     */
    public Integer getGroupPermittedToView() {
        return this.groupPermittedToView;
    }
    
    public void setGroupPermittedToView(Integer groupPermittedToView) {
        this.groupPermittedToView = groupPermittedToView;
    }

    /**
     *      *            @hibernate.property
     *             column="EnvironmentalProtectionStatus"
     *             length="50"
     *         
     */
    public String getEnvironmentalProtectionStatus() {
        return this.environmentalProtectionStatus;
    }
    
    public void setEnvironmentalProtectionStatus(String environmentalProtectionStatus) {
        this.environmentalProtectionStatus = environmentalProtectionStatus;
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
     *             column="NodeNumber"
     *             length="10"
     *         
     */
    public Integer getNodeNumber() {
        return this.nodeNumber;
    }
    
    public void setNodeNumber(Integer nodeNumber) {
        this.nodeNumber = nodeNumber;
    }

    /**
     *      *            @hibernate.property
     *             column="HighestChildNodeNumber"
     *             length="10"
     *         
     */
    public Integer getHighestChildNodeNumber() {
        return this.highestChildNodeNumber;
    }
    
    public void setHighestChildNodeNumber(Integer highestChildNodeNumber) {
        this.highestChildNodeNumber = highestChildNodeNumber;
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
     *             column="Accepted"
     *             length="5"
     *         
     */
    public Short getAccepted() {
        return this.accepted;
    }
    
    public void setAccepted(Short accepted) {
        this.accepted = accepted;
    }

    /**
     *      *            @hibernate.property
     *             column="RankID"
     *             length="10"
     *         
     */
    public Integer getRankId() {
        return this.rankId;
    }
    
    public void setRankId(Integer rankId) {
        this.rankId = rankId;
    }

    /**
     *      *            @hibernate.property
     *             column="GroupNumber"
     *             length="20"
     *         
     */
    public String getGroupNumber() {
        return this.groupNumber;
    }
    
    public void setGroupNumber(String groupNumber) {
        this.groupNumber = groupNumber;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="AcceptedID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.TaxonName"
     *         
     */
    public Set getTaxonNames() {
        return this.taxonNames;
    }
    
    public void setTaxonNames(Set taxonNames) {
        this.taxonNames = taxonNames;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="AcceptedID"         
     *         
     */
    public TaxonName getTaxonName1() {
        return this.taxonName1;
    }
    
    public void setTaxonName1(TaxonName taxonName1) {
        this.taxonName1 = taxonName1;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="HostTaxonID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Habitat"
     *         
     */
    public Set getHabitats() {
        return this.habitats;
    }
    
    public void setHabitats(Set habitats) {
        this.habitats = habitats;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="TaxonNameID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.TaxonCitation"
     *         
     */
    public Set getTaxonCitations() {
        return this.taxonCitations;
    }
    
    public void setTaxonCitations(Set taxonCitations) {
        this.taxonCitations = taxonCitations;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="ParasiteTaxonNameID"
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
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="TaxonNameID"
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
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="TaxonomyTypeID"         
     *         
     */
    public TaxonomyType getTaxonomyType() {
        return this.taxonomyType;
    }
    
    public void setTaxonomyType(TaxonomyType taxonomyType) {
        this.taxonomyType = taxonomyType;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="TaxonomicUnitTypeID"         
     *         
     */
    public TaxonomicUnitType getTaxonomicUnitType() {
        return this.taxonomicUnitType;
    }
    
    public void setTaxonomicUnitType(TaxonomicUnitType taxonomicUnitType) {
        this.taxonomicUnitType = taxonomicUnitType;
    }




}