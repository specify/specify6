package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="prepattrs"
 *     
 */
public class PrepAttrs  implements java.io.Serializable {

    // Fields    

     protected Integer prepAttrsId;
     protected String name;
     protected String value;
     protected Integer fieldType;
     protected Integer unit;
     protected Integer prepDate;
     protected String remarks;
     private PrepsObj prepsObj;
     private TaxonName taxonName;


    // Constructors

    /** default constructor */
    public PrepAttrs() {
    }
    
    /** constructor with id */
    public PrepAttrs(Integer prepAttrsId) {
        this.prepAttrsId = prepAttrsId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="PrepAttrsID"
     *         
     */
    public Integer getPrepAttrsId() {
        return this.prepAttrsId;
    }
    
    public void setPrepAttrsId(Integer prepAttrsId) {
        this.prepAttrsId = prepAttrsId;
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
     *             column="value"
     *             length="128"
     *         
     */
    public String getValue() {
        return this.value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }

    /**
     *      *            @hibernate.property
     *             column="fieldType"
     *             length="10"
     *         
     */
    public Integer getFieldType() {
        return this.fieldType;
    }
    
    public void setFieldType(Integer fieldType) {
        this.fieldType = fieldType;
    }

    /**
     *      *            @hibernate.property
     *             column="unit"
     *             length="2"
     *         
     */
    public Integer getUnit() {
        return this.unit;
    }
    
    public void setUnit(Integer unit) {
        this.unit = unit;
    }

    /**
     *      *            @hibernate.property
     *             column="prepDate"
     *             length="10"
     *         
     */
    public Integer getPrepDate() {
        return this.prepDate;
    }
    
    public void setPrepDate(Integer prepDate) {
        this.prepDate = prepDate;
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
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="PrepsObjID"
     *         
     */
    public PrepsObj getPrepsObj() {
        return this.prepsObj;
    }
    
    public void setPrepsObj(PrepsObj prepsObj) {
        this.prepsObj = prepsObj;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="ParasiteTaxonNameID"         
     *         
     */
    public TaxonName getTaxonName() {
        return this.taxonName;
    }
    
    public void setTaxonName(TaxonName taxonName) {
        this.taxonName = taxonName;
    }




}