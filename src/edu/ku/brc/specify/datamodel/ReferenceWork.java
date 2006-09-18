package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;




/**

 */
public class ReferenceWork  implements java.io.Serializable {

    // Fields    

     protected Long referenceWorkId;
     protected Integer containingReferenceWorkId;
     protected Byte referenceWorkType;
     protected String title;
     protected String publisher;
     protected String placeOfPublication;
     protected String workDate;
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
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     protected Set<LocalityCitation> localityCitations;
     protected Set<CollectionObjectCitation> collectionObjectCitations;
     protected Set<TaxonCitation> taxonCitations;
     protected Set<DeterminationCitation> determinationCitations;
     protected Journal journal;
     protected Set<Authors> authors;


    // Constructors

    /** default constructor */
    public ReferenceWork() {
    }
    
    /** constructor with id */
    public ReferenceWork(Long referenceWorkId) {
        this.referenceWorkId = referenceWorkId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        referenceWorkId = null;
        containingReferenceWorkId = null;
        referenceWorkType = null;
        title = null;
        publisher = null;
        placeOfPublication = null;
        workDate = null;
        volume = null;
        pages = null;
        url = null;
        libraryNumber = null;
        remarks = null;
        text1 = null;
        text2 = null;
        number1 = null;
        number2 = null;
        timestampCreated = new Date();
        timestampModified = null;
        lastEditedBy = null;
        published = null;
        yesNo1 = null;
        yesNo2 = null;
        localityCitations = new HashSet<LocalityCitation>();
        collectionObjectCitations = new HashSet<CollectionObjectCitation>();
        taxonCitations = new HashSet<TaxonCitation>();
        determinationCitations = new HashSet<DeterminationCitation>();
        journal = null;
        authors = new HashSet<Authors>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * PrimaryKey
     */
    public Long getReferenceWorkId() {
        return this.referenceWorkId;
    }
    
    public void setReferenceWorkId(Long referenceWorkId) {
        this.referenceWorkId = referenceWorkId;
    }

    /**
     *      * Link to Reference containing (if Section)
     */
    public Integer getContainingReferenceWorkId() {
        return this.containingReferenceWorkId;
    }
    
    public void setContainingReferenceWorkId(Integer containingReferenceWorkId) {
        this.containingReferenceWorkId = containingReferenceWorkId;
    }

    /**
     * 
     */
    public Byte getReferenceWorkType() {
        return this.referenceWorkType;
    }
    
    public void setReferenceWorkType(Byte referenceWorkType) {
        this.referenceWorkType = referenceWorkType;
    }

    /**
     *      * Title of reference
     */
    public String getTitle() {
        return this.title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 
     */
    public String getPublisher() {
        return this.publisher;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    /**
     * 
     */
    public String getPlaceOfPublication() {
        return this.placeOfPublication;
    }
    
    public void setPlaceOfPublication(String placeOfPublication) {
        this.placeOfPublication = placeOfPublication;
    }

    /**
     * 
     */
    public String getWorkDate() {
        return this.workDate;
    }
    
    public void setWorkDate(String workDate) {
        this.workDate = workDate;
    }

    /**
     *      * Volume/Issue for Journal articles
     */
    public String getVolume() {
        return this.volume;
    }
    
    public void setVolume(String volume) {
        this.volume = volume;
    }

    /**
     *      * Number of pages or Page range for Journal articles
     */
    public String getPages() {
        return this.pages;
    }
    
    public void setPages(String pages) {
        this.pages = pages;
    }

    /**
     * 
     */
    public String getUrl() {
        return this.url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 
     */
    public String getLibraryNumber() {
        return this.libraryNumber;
    }
    
    public void setLibraryNumber(String libraryNumber) {
        this.libraryNumber = libraryNumber;
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
     * 
     */
    public Short getPublished() {
        return this.published;
    }
    
    public void setPublished(Short published) {
        this.published = published;
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
    public Set<LocalityCitation> getLocalityCitations() {
        return this.localityCitations;
    }
    
    public void setLocalityCitations(Set<LocalityCitation> localityCitations) {
        this.localityCitations = localityCitations;
    }

    /**
     * 
     */
    public Set<CollectionObjectCitation> getCollectionObjectCitations() {
        return this.collectionObjectCitations;
    }
    
    public void setCollectionObjectCitations(Set<CollectionObjectCitation> collectionObjectCitations) {
        this.collectionObjectCitations = collectionObjectCitations;
    }

    /**
     * 
     */
    public Set<TaxonCitation> getTaxonCitations() {
        return this.taxonCitations;
    }
    
    public void setTaxonCitations(Set<TaxonCitation> taxonCitations) {
        this.taxonCitations = taxonCitations;
    }

    /**
     * 
     */
    public Set<DeterminationCitation> getDeterminationCitations() {
        return this.determinationCitations;
    }
    
    public void setDeterminationCitations(Set<DeterminationCitation> determinationCitations) {
        this.determinationCitations = determinationCitations;
    }

    /**
     *      * Link to Journal containing the reference (if applicable)
     */
    public Journal getJournal() {
        return this.journal;
    }
    
    public void setJournal(Journal journal) {
        this.journal = journal;
    }

    /**
     * 
     */
    public Set<Authors> getAuthors() {
        return this.authors;
    }
    
    public void setAuthors(Set<Authors> authors) {
        this.authors = authors;
    }





    // Add Methods

    public void addLocalityCitations(final LocalityCitation localityCitation)
    {
        this.localityCitations.add(localityCitation);
        localityCitation.setReferenceWork(this);
    }

    public void addCollectionObjectCitations(final CollectionObjectCitation collectionObjectCitation)
    {
        this.collectionObjectCitations.add(collectionObjectCitation);
        collectionObjectCitation.setReferenceWork(this);
    }

    public void addTaxonCitations(final TaxonCitation taxonCitation)
    {
        this.taxonCitations.add(taxonCitation);
        taxonCitation.setReferenceWork(this);
    }

    public void addDeterminationCitations(final DeterminationCitation determinationCitation)
    {
        this.determinationCitations.add(determinationCitation);
        determinationCitation.setReferenceWork(this);
    }

    public void addAuthors(final Authors author)
    {
        this.authors.add(author);
        author.setReferenceWork(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeLocalityCitations(final LocalityCitation localityCitation)
    {
        this.localityCitations.remove(localityCitation);
        localityCitation.setReferenceWork(null);
    }

    public void removeCollectionObjectCitations(final CollectionObjectCitation collectionObjectCitation)
    {
        this.collectionObjectCitations.remove(collectionObjectCitation);
        collectionObjectCitation.setReferenceWork(null);
    }

    public void removeTaxonCitations(final TaxonCitation taxonCitation)
    {
        this.taxonCitations.remove(taxonCitation);
        taxonCitation.setReferenceWork(null);
    }

    public void removeDeterminationCitations(final DeterminationCitation determinationCitation)
    {
        this.determinationCitations.remove(determinationCitation);
        determinationCitation.setReferenceWork(null);
    }

    public void removeAuthors(final Authors author)
    {
        this.authors.remove(author);
        author.setReferenceWork(null);
    }

    // Delete Add Methods
}
