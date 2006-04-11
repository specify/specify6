package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;




/**

 */
public class Determination  implements java.io.Serializable {

    // Fields    

     protected Integer determinationId;
     protected Boolean isCurrent;
     protected String typeStatusName;
     protected Calendar determinedDate;
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
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     protected Taxon taxon;
     protected CollectionObject collectionObject;
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
     *      * Primary key
     */
    public Integer getDeterminationId() {
        return this.determinationId;
    }
    
    public void setDeterminationId(Integer determinationId) {
        this.determinationId = determinationId;
    }

    /**
     * 
     */
    public Boolean getIsCurrent() {
        return this.isCurrent;
    }
    
    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    /**
     *      * e.g. 'Holotype', 'Paratype'...
     */
    public String getTypeStatusName() {
        return this.typeStatusName;
    }
    
    public void setTypeStatusName(String typeStatusName) {
        this.typeStatusName = typeStatusName;
    }

    /**
     * 
     */
    public Calendar getDeterminedDate() {
        return this.determinedDate;
    }
    
    public void setDeterminedDate(Calendar determinedDate) {
        this.determinedDate = determinedDate;
    }

    /**
     *      * Confidence of determination (value from PickList)
     */
    public String getConfidence() {
        return this.confidence;
    }
    
    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    /**
     *      * Method of determination (value from PickList)
     */
    public String getMethod() {
        return this.method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     *      * Body part, or characteristic used as the basis of a determination.
     */
    public String getFeatureOrBasis() {
        return this.featureOrBasis;
    }
    
    public void setFeatureOrBasis(String featureOrBasis) {
        this.featureOrBasis = featureOrBasis;
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
     *      * User definable
     */
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      * User definable
     */
    public String getText2() {
        return this.text2;
    }
    
    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      * User definable
     */
    public Float getNumber1() {
        return this.number1;
    }
    
    public void setNumber1(Float number1) {
        this.number1 = number1;
    }

    /**
     *      * User definable
     */
    public Float getNumber2() {
        return this.number2;
    }
    
    public void setNumber2(Float number2) {
        this.number2 = number2;
    }

    /**
     *      * Date/Time the record was created
     */
    public Date getTimestampCreated() {
        return this.timestampCreated;
    }
    
    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    /**
     *      * Date/Time the record was modified
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
    public String getLastEditedBy() {
        return this.lastEditedBy;
    }
    
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     *      * User definable
     */
    public Boolean getYesNo1() {
        return this.yesNo1;
    }
    
    public void setYesNo1(Boolean yesNo1) {
        this.yesNo1 = yesNo1;
    }

    /**
     *      * User definable
     */
    public Boolean getYesNo2() {
        return this.yesNo2;
    }
    
    public void setYesNo2(Boolean yesNo2) {
        this.yesNo2 = yesNo2;
    }

    /**
     * 
     */
    public Taxon getTaxon() {
        return this.taxon;
    }
    
    public void setTaxon(Taxon taxon) {
        this.taxon = taxon;
    }

    /**
     * 
     */
    public CollectionObject getCollectionObject() {
        return this.collectionObject;
    }
    
    public void setCollectionObject(CollectionObject collectionObject) {
        this.collectionObject = collectionObject;
    }

    /**
     * 
     */
    public Set getDeterminationCitations() {
        return this.determinationCitations;
    }
    
    public void setDeterminationCitations(Set determinationCitations) {
        this.determinationCitations = determinationCitations;
    }

    /**
     *      * id of the Person making the determination
     */
    public Agent getDeterminer() {
        return this.determiner;
    }
    
    public void setDeterminer(Agent determiner) {
        this.determiner = determiner;
    }




}