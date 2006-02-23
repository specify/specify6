package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.Set;




/**
 *        @hibernate.class
 *         table="determination"
 *     
 */
public class Determination  implements java.io.Serializable {

    // Fields    

     protected Integer determinationId;
     protected Boolean isCurrent;
     protected String typeStatusName;
     protected Integer dateField;
     protected String confidence;
     protected String method;
     protected String featureOrBasis;
     protected String remarks;
     protected String text1;
     protected String text2;
     protected Float number1;
     protected Float number2;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected Integer typeStatusNameId;
     protected Integer confidenceId;
     protected Integer methodId;
     protected Short yesNo1;
     protected Short yesNo2;
     protected Taxon Taxon;
     protected CollectionObject collectionObject;
     protected Preparation preparations;
     private Set determinationCitations;
     private Agent determiner;


    // Constructors

    /** default constructor */
    public Determination() {
    }
    
    /** constructor with id */
    public Determination(Integer determinationId) {
        this.determinationId = determinationId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="DeterminationID"
     *         
     */
    public Integer getDeterminationId() {
        return this.determinationId;
    }
    
    public void setDeterminationId(Integer determinationId) {
        this.determinationId = determinationId;
    }

    /**
     *      *            @hibernate.property
     *             column="IsCurrent"
     *             length="1"
     *             not-null="true"
     *         
     */
    public Boolean getIsCurrent() {
        return this.isCurrent;
    }
    
    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    /**
     *      *            @hibernate.property
     *             column="TypeStatusName"
     *             length="50"
     *         
     */
    public String getTypeStatusName() {
        return this.typeStatusName;
    }
    
    public void setTypeStatusName(String typeStatusName) {
        this.typeStatusName = typeStatusName;
    }

    /**
     *      *            @hibernate.property
     *             column="DateField"
     *             length="10"
     *         
     */
    public Integer getDateField() {
        return this.dateField;
    }
    
    public void setDateField(Integer dateField) {
        this.dateField = dateField;
    }

    /**
     *      *            @hibernate.property
     *             column="Confidence"
     *             length="50"
     *         
     */
    public String getConfidence() {
        return this.confidence;
    }
    
    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    /**
     *      *            @hibernate.property
     *             column="Method"
     *             length="50"
     *         
     */
    public String getMethod() {
        return this.method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     *      *            @hibernate.property
     *             column="FeatureOrBasis"
     *             length="50"
     *         
     */
    public String getFeatureOrBasis() {
        return this.featureOrBasis;
    }
    
    public void setFeatureOrBasis(String featureOrBasis) {
        this.featureOrBasis = featureOrBasis;
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
     *             column="TypeStatusNameID"
     *             length="10"
     *         
     */
    public Integer getTypeStatusNameId() {
        return this.typeStatusNameId;
    }
    
    public void setTypeStatusNameId(Integer typeStatusNameId) {
        this.typeStatusNameId = typeStatusNameId;
    }

    /**
     *      *            @hibernate.property
     *             column="ConfidenceID"
     *             length="10"
     *         
     */
    public Integer getConfidenceId() {
        return this.confidenceId;
    }
    
    public void setConfidenceId(Integer confidenceId) {
        this.confidenceId = confidenceId;
    }

    /**
     *      *            @hibernate.property
     *             column="MethodID"
     *             length="10"
     *         
     */
    public Integer getMethodId() {
        return this.methodId;
    }
    
    public void setMethodId(Integer methodId) {
        this.methodId = methodId;
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
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="TaxonID"         
     *         
     */
    public Taxon getTaxon() {
        return this.Taxon;
    }
    
    public void setTaxon(Taxon Taxon) {
        this.Taxon = Taxon;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     * 			cascade="delete"
     *            @hibernate.column name="CollectionObjectID"         
     *         
     */
    public CollectionObject getCollectionObject() {
        return this.collectionObject;
    }
    
    public void setCollectionObject(CollectionObject collectionObject) {
        this.collectionObject = collectionObject;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     * 			cascade="delete"
     *            @hibernate.column name="PreparationID"         
     *         
     */
    public Preparation getPreparations() {
        return this.preparations;
    }
    
    public void setPreparations(Preparation preparations) {
        this.preparations = preparations;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     * 			cascade="delete"
     *            @hibernate.collection-key
     *             column="DeterminationID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.DeterminationCitation"
     *         
     */
    public Set getDeterminationCitations() {
        return this.determinationCitations;
    }
    
    public void setDeterminationCitations(Set determinationCitations) {
        this.determinationCitations = determinationCitations;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="DeterminerID"         
     *         
     */
    public Agent getDeterminer() {
        return this.determiner;
    }
    
    public void setDeterminer(Agent determiner) {
        this.determiner = determiner;
    }




}