package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;




/**

 */
public class Deaccession  implements java.io.Serializable {

    // Fields    

     protected Integer deaccessionId;
     protected String type;
     protected String deaccessionNumber;
     protected Calendar deaccessionDate;
     protected String remarks;
     protected String text1;
     protected String text2;
     protected Float number1;
     protected Float number2;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected String lastEditedBy;
     protected Boolean yesNo1;
     protected Boolean yesNo2;
     protected Set<DeaccessionAgents> deaccessionAgents;
     protected Set<DeaccessionCollectionObject> deaccessionCollectionObjects;


    // Constructors

    /** default constructor */
    public Deaccession() {
    }
    
    /** constructor with id */
    public Deaccession(Integer deaccessionId) {
        this.deaccessionId = deaccessionId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        deaccessionId = null;
        type = null;
        deaccessionNumber = null;
        deaccessionDate = null;
        remarks = null;
        text1 = null;
        text2 = null;
        number1 = null;
        number2 = null;
        timestampModified = new Date();
        timestampCreated = new Date();
        lastEditedBy = null;
        yesNo1 = null;
        yesNo2 = null;
        deaccessionAgents = new HashSet<DeaccessionAgents>();
        deaccessionCollectionObjects = new HashSet<DeaccessionCollectionObject>();
    }
    // End Initializer

    // Property accessors

    /**
     *      * Primary key
     */
    public Integer getDeaccessionId() {
        return this.deaccessionId;
    }
    
    public void setDeaccessionId(Integer deaccessionId) {
        this.deaccessionId = deaccessionId;
    }

    /**
     *      * Description of the Type of deaccession; i.e. Gift, disposal, lost
     */
    public String getType() {
        return this.type;
    }
    
    public void setType(String type) {
        this.type = type;
    }

    /**
     *      * Name institution assigns to the deacession
     */
    public String getDeaccessionNumber() {
        return this.deaccessionNumber;
    }
    
    public void setDeaccessionNumber(String deaccessionNumber) {
        this.deaccessionNumber = deaccessionNumber;
    }

    /**
     * 
     */
    public Calendar getDeaccessionDate() {
        return this.deaccessionDate;
    }
    
    public void setDeaccessionDate(Calendar deaccessionDate) {
        this.deaccessionDate = deaccessionDate;
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
    public Set<DeaccessionAgents> getDeaccessionAgents() {
        return this.deaccessionAgents;
    }
    
    public void setDeaccessionAgents(Set<DeaccessionAgents> deaccessionAgents) {
        this.deaccessionAgents = deaccessionAgents;
    }

    /**
     * 
     */
    public Set<DeaccessionCollectionObject> getDeaccessionCollectionObjects() {
        return this.deaccessionCollectionObjects;
    }
    
    public void setDeaccessionCollectionObjects(Set<DeaccessionCollectionObject> deaccessionCollectionObjects) {
        this.deaccessionCollectionObjects = deaccessionCollectionObjects;
    }





    // Add Methods

    public void addDeaccessionAgent(final DeaccessionAgents deaccessionAgent)
    {
        this.deaccessionAgents.add(deaccessionAgent);
        deaccessionAgent.setDeaccession(this);
    }

    public void addDeaccessionCollectionObject(final DeaccessionCollectionObject deaccessionCollectionObject)
    {
        this.deaccessionCollectionObjects.add(deaccessionCollectionObject);
        deaccessionCollectionObject.setDeaccession(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeDeaccessionAgent(final DeaccessionAgents deaccessionAgent)
    {
        this.deaccessionAgents.remove(deaccessionAgent);
        deaccessionAgent.setDeaccession(null);
    }

    public void removeDeaccessionCollectionObject(final DeaccessionCollectionObject deaccessionCollectionObject)
    {
        this.deaccessionCollectionObjects.remove(deaccessionCollectionObject);
        deaccessionCollectionObject.setDeaccession(null);
    }

    // Delete Add Methods
}
