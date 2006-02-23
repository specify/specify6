package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.Set;




/**
 *        @hibernate.class
 *         table="referencework"
 *     
 */
public class ReferenceWork  implements java.io.Serializable {

    // Fields    

     protected Integer referenceWorkId;
     protected Integer containingReferenceWorkId;
     protected Byte referenceWorkType;
     protected String title;
     protected String publisher;
     protected String placeOfPublication;
     protected String dateField;
     protected String volume;
     protected String pages;
     protected String url;
     protected String libraryNumber;
     protected String remarks;
     protected String text1;
     protected String text2;
     protected Float number1;
     protected Float number2;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected Short published;
     protected Short yesNo1;
     protected Short yesNo2;
     private Set localityCitations;
     private Set collectionObjectCitations;
     private Set taxonCitations;
     private Set determinationCitations;
     private Journal journal;
     private Set authors;


    // Constructors

    /** default constructor */
    public ReferenceWork() {
    }
    
    /** constructor with id */
    public ReferenceWork(Integer referenceWorkId) {
        this.referenceWorkId = referenceWorkId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="ReferenceWorkID"
     *         
     */
    public Integer getReferenceWorkId() {
        return this.referenceWorkId;
    }
    
    public void setReferenceWorkId(Integer referenceWorkId) {
        this.referenceWorkId = referenceWorkId;
    }

    /**
     *      *            @hibernate.property
     *             column="ContainingReferenceWorkID"
     *             length="10"
     *         
     */
    public Integer getContainingReferenceWorkId() {
        return this.containingReferenceWorkId;
    }
    
    public void setContainingReferenceWorkId(Integer containingReferenceWorkId) {
        this.containingReferenceWorkId = containingReferenceWorkId;
    }

    /**
     *      *            @hibernate.property
     *             column="ReferenceWorkType"
     *             length="3"
     *             not-null="true"
     *         
     */
    public Byte getReferenceWorkType() {
        return this.referenceWorkType;
    }
    
    public void setReferenceWorkType(Byte referenceWorkType) {
        this.referenceWorkType = referenceWorkType;
    }

    /**
     *      *            @hibernate.property
     *             column="Title"
     *             length="255"
     *         
     */
    public String getTitle() {
        return this.title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *      *            @hibernate.property
     *             column="Publisher"
     *             length="50"
     *         
     */
    public String getPublisher() {
        return this.publisher;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    /**
     *      *            @hibernate.property
     *             column="PlaceOfPublication"
     *             length="50"
     *         
     */
    public String getPlaceOfPublication() {
        return this.placeOfPublication;
    }
    
    public void setPlaceOfPublication(String placeOfPublication) {
        this.placeOfPublication = placeOfPublication;
    }

    /**
     *      *            @hibernate.property
     *             column="DateField"
     *             length="25"
     *         
     */
    public String getDateField() {
        return this.dateField;
    }
    
    public void setDateField(String dateField) {
        this.dateField = dateField;
    }

    /**
     *      *            @hibernate.property
     *             column="Volume"
     *             length="25"
     *         
     */
    public String getVolume() {
        return this.volume;
    }
    
    public void setVolume(String volume) {
        this.volume = volume;
    }

    /**
     *      *            @hibernate.property
     *             column="Pages"
     *             length="50"
     *         
     */
    public String getPages() {
        return this.pages;
    }
    
    public void setPages(String pages) {
        this.pages = pages;
    }

    /**
     *      *            @hibernate.property
     *             column="URL"
     *             length="300"
     *         
     */
    public String getUrl() {
        return this.url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     *      *            @hibernate.property
     *             column="LibraryNumber"
     *             length="50"
     *         
     */
    public String getLibraryNumber() {
        return this.libraryNumber;
    }
    
    public void setLibraryNumber(String libraryNumber) {
        this.libraryNumber = libraryNumber;
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
     *             column="LastEditedBy"
     *             length="50"
     *             not-null="true"
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
     *             column="Published"
     *             length="5"
     *         
     */
    public Short getPublished() {
        return this.published;
    }
    
    public void setPublished(Short published) {
        this.published = published;
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
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="ReferenceWorkID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.LocalityCitation"
     *         
     */
    public Set getLocalityCitations() {
        return this.localityCitations;
    }
    
    public void setLocalityCitations(Set localityCitations) {
        this.localityCitations = localityCitations;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="ReferenceWorkID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.CollectionObjectCitation"
     *         
     */
    public Set getCollectionObjectCitations() {
        return this.collectionObjectCitations;
    }
    
    public void setCollectionObjectCitations(Set collectionObjectCitations) {
        this.collectionObjectCitations = collectionObjectCitations;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="ReferenceWorkID"
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
     *             column="ReferenceWorkID"
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
     *            @hibernate.column name="JournalID"         
     *         
     */
    public Journal getJournal() {
        return this.journal;
    }
    
    public void setJournal(Journal journal) {
        this.journal = journal;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="ReferenceWorkID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.Author"
     *         
     */
    public Set getAuthors() {
        return this.authors;
    }
    
    public void setAuthors(Set authors) {
        this.authors = authors;
    }




}