package edu.ku.brc.specify.datamodel;

import java.util.Date;




/**

 */
public class PreparationAttr  implements AttributeIFace,java.io.Serializable {

    // Fields    

     protected Integer attrId;
     protected String strValue;
     protected Double dblValue;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected AttributeDef definition;
     protected Preparation preparation;


    // Constructors

    /** default constructor */
    public PreparationAttr() {
    }
    
    /** constructor with id */
    public PreparationAttr(Integer attrId) {
        this.attrId = attrId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        attrId = null;
        strValue = null;
        dblValue = null;
        timestampCreated = new Date();
        timestampModified = new Date();
        definition = null;
        preparation = null;
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Integer getAttrId() {
        return this.attrId;
    }
    
    public void setAttrId(Integer attrId) {
        this.attrId = attrId;
    }

    /**
     * 
     */
    public String getStrValue() {
        return this.strValue;
    }
    
    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }

    /**
     * 
     */
    public Double getDblValue() {
        return this.dblValue;
    }
    
    public void setDblValue(Double dblValue) {
        this.dblValue = dblValue;
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
    public AttributeDef getDefinition() {
        return this.definition;
    }
    
    public void setDefinition(AttributeDef definition) {
        this.definition = definition;
    }

    /**
     * 
     */
    public Preparation getPreparation() {
        return this.preparation;
    }
    
    public void setPreparation(Preparation preparation) {
        this.preparation = preparation;
    }





    // Add Methods

    // Done Add Methods

    // Delete Methods

    // Delete Add Methods
}
