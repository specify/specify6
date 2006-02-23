package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.Set;




/**
 *        @hibernate.class
 *         table="taxon"
 *     
 */
public class Taxon  implements java.io.Serializable {

    // Fields    

     protected Integer taxonId;
     protected String taxonomicSerialNumber;
     protected String name;
     protected String unitInd1;
     protected String unitName1;
     protected String unitInd2;
     protected String unitName2;
     protected String unitInd3;
     protected String unitName3;
     protected String unitInd4;
     protected String unitName4;
     protected String fullTaxon;
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
     private Set acceptedChildren;
     private Taxon acceptedTaxon;
     private Set taxonCitations;
     private Set externalFiles;
     private TaxonomyTreeDef definition;
     private Taxon parent;


    // Constructors

    /** default constructor */
    public Taxon() {
    }
    
    /** constructor with id */
    public Taxon(Integer taxonId) {
        this.taxonId = taxonId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="TaxonID"
     *         
     */
    public Integer getTaxonId() {
        return this.taxonId;
    }
    
    public void setTaxonId(Integer taxonId) {
        this.taxonId = taxonId;
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
     *             column="name"
     *             length="50"
     *         
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
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
     *             column="FullTaxon"
     *             length="255"
     *         
     */
    public String getFullTaxon() {
        return this.fullTaxon;
    }
    
    public void setFullTaxon(String fullTaxon) {
        this.fullTaxon = fullTaxon;
    }

    /**
     *      *            @hibernate.property
     *             column="CommonName"
     *             length="128"
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
     *             length="128"
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
     *             length="64"
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
     *             length="64"
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
     *             index="IX_TXN_NodeNumber"
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
     *             index="IX_TXN_HighestChildNodeNumber"
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
     *             class="edu.ku.brc.specify.datamodel.Taxon"
     *         
     */
    public Set getAcceptedChildren() {
        return this.acceptedChildren;
    }
    
    public void setAcceptedChildren(Set acceptedChildren) {
        this.acceptedChildren = acceptedChildren;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="AcceptedID"         
     *         
     */
    public Taxon getAcceptedTaxon() {
        return this.acceptedTaxon;
    }
    
    public void setAcceptedTaxon(Taxon acceptedTaxon) {
        this.acceptedTaxon = acceptedTaxon;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="TaxonID"
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
     * 
     */
    public Set getExternalFiles() {
        return this.externalFiles;
    }
    
    public void setExternalFiles(Set externalFiles) {
        this.externalFiles = externalFiles;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="TaxonomyTreeDefID"         
     *         
     */
    public TaxonomyTreeDef getDefinition() {
        return this.definition;
    }
    
    public void setDefinition(TaxonomyTreeDef definition) {
        this.definition = definition;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="ParentID"         
     *         
     */
    public Taxon getParent() {
        return this.parent;
    }
    
    public void setParent(Taxon parent) {
        this.parent = parent;
    }




}