package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;




/**

 */
public class Accession  implements java.io.Serializable {

    // Fields

     protected Integer accessionId;
     protected String type;
     protected String status;
     protected String number;
     protected String verbatimDate;
     protected Calendar dateAccessioned;
     protected Calendar dateReceived;
     protected String text1;
     protected String text2;
     protected String text3;
     protected Float number1;
     protected Float number2;
     protected String remarks;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     protected Set<CollectionObject> collectionObjects;
     protected Set<AccessionAuthorizations> accessionAuthorizations;
     protected Set<AccessionAgent> accessionAgents;


    // Constructors

    /** default constructor */
    public Accession() {
    }

    /** constructor with id */
    public Accession(Integer accessionId) {
        this.accessionId = accessionId;
    }




    // Initializer
    public void initialize()
    {
        accessionId = null;
        type = null;
        status = null;
        number = null;
        verbatimDate = null;
        dateAccessioned = null;
        dateReceived = null;
        text1 = null;
        text2 = null;
        text3 = null;
        number1 = null;
        number2 = null;
        remarks = null;
        timestampCreated = Calendar.getInstance().getTime();
        timestampModified = null;
        lastEditedBy = null;
        yesNo1 = null;
        yesNo2 = null;
        collectionObjects = new HashSet<CollectionObject>();
        accessionAuthorizations = new HashSet<AccessionAuthorizations>();
        accessionAgents = new HashSet<AccessionAgent>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    public Integer getAccessionId() {
        return this.accessionId;
    }

    public void setAccessionId(Integer accessionId) {
        this.accessionId = accessionId;
    }

    /**
     *      * Source of Accession, e.g. 'Collecting', 'Gift',  'Bequest' ...
     */
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     *      * Status of Accession, e.g. 'In process', 'Complete' ...
     */
    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     *      * A user-visible identifier of the Accession. Typically an integer, but may include alphanumeric characters as prefix, suffix, and separators
     */
    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    /**
     *      * accomodates historical accessions.
     */
    public String getVerbatimDate() {
        return this.verbatimDate;
    }

    public void setVerbatimDate(String verbatimDate) {
        this.verbatimDate = verbatimDate;
    }

    /**
     *      * Date of Accession
     */
    public Calendar getDateAccessioned() {
        return this.dateAccessioned;
    }

    public void setDateAccessioned(Calendar dateAccessioned) {
        this.dateAccessioned = dateAccessioned;
    }

    /**
     *      * Date material was received
     */
    public Calendar getDateReceived() {
        return this.dateReceived;
    }

    public void setDateReceived(Calendar dateReceived) {
        this.dateReceived = dateReceived;
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
    public String getText3() {
        return this.text3;
    }

    public void setText3(String text3) {
        this.text3 = text3;
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
     *      * Comments
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
     *      * Login name of user who last edited the record
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
    public Set<CollectionObject> getCollectionObjects() {
        return this.collectionObjects;
    }

    public void setCollectionObjects(Set<CollectionObject> collectionObjects) {
        this.collectionObjects = collectionObjects;
    }

    /**
     *
     */
    public Set getAccessionAuthorizations() {
        return this.accessionAuthorizations;
    }

    public void setAccessionAuthorizations(Set<AccessionAuthorizations> accessionAuthorizations) {
        this.accessionAuthorizations = accessionAuthorizations;
    }

    /**
     *
     */
    public Set<AccessionAgent> getAccessionAgents() {
        return this.accessionAgents;
    }

    public void setAccessionAgents(Set<AccessionAgent> accessionAgents) {
        this.accessionAgents = accessionAgents;
    }




    // Add Methods

    public void addCollectionObject(final CollectionObject collectionObject)
    {
        this.collectionObjects.add(collectionObject);
    }

    public void addAccessionAuthorizations(final AccessionAuthorizations accessionAuthorizations)
    {
        this.accessionAuthorizations.add(accessionAuthorizations);
    }

    public void addAccessionAgent(final AccessionAgent accessionAgent)
    {
        this.accessionAgents.add(accessionAgent);
    }

    // Done Add Methods
}
