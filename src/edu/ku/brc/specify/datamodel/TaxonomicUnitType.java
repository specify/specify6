package edu.ku.brc.specify.datamodel;

import java.util.*;




/**
 *        @hibernate.class
 *         table="taxonomicunittype"
 *     
 */
public class TaxonomicUnitType  implements java.io.Serializable {

    // Fields    

     protected Integer taxonomicUnitTypeId;
     protected Byte kingdom;
     protected Short rankId;
     protected String rankName;
     protected Short directParentRankId;
     protected Short requiredParentRankId;
     private TaxonomyType taxonomyType;
     private Set taxonNames;


    // Constructors

    /** default constructor */
    public TaxonomicUnitType() {
    }
    
    /** constructor with id */
    public TaxonomicUnitType(Integer taxonomicUnitTypeId) {
        this.taxonomicUnitTypeId = taxonomicUnitTypeId;
    }
   
    
    

    // Property accessors

    /**
     *      *            @hibernate.id
     *             generator-class="assigned"
     *             type="java.lang.Integer"
     *             column="TaxonomicUnitTypeID"
     *         
     */
    public Integer getTaxonomicUnitTypeId() {
        return this.taxonomicUnitTypeId;
    }
    
    public void setTaxonomicUnitTypeId(Integer taxonomicUnitTypeId) {
        this.taxonomicUnitTypeId = taxonomicUnitTypeId;
    }

    /**
     *      *            @hibernate.property
     *             column="Kingdom"
     *             length="3"
     *         
     */
    public Byte getKingdom() {
        return this.kingdom;
    }
    
    public void setKingdom(Byte kingdom) {
        this.kingdom = kingdom;
    }

    /**
     *      *            @hibernate.property
     *             column="RankID"
     *             length="5"
     *         
     */
    public Short getRankId() {
        return this.rankId;
    }
    
    public void setRankId(Short rankId) {
        this.rankId = rankId;
    }

    /**
     *      *            @hibernate.property
     *             column="RankName"
     *             length="15"
     *         
     */
    public String getRankName() {
        return this.rankName;
    }
    
    public void setRankName(String rankName) {
        this.rankName = rankName;
    }

    /**
     *      *            @hibernate.property
     *             column="DirectParentRankID"
     *             length="5"
     *         
     */
    public Short getDirectParentRankId() {
        return this.directParentRankId;
    }
    
    public void setDirectParentRankId(Short directParentRankId) {
        this.directParentRankId = directParentRankId;
    }

    /**
     *      *            @hibernate.property
     *             column="RequiredParentRankID"
     *             length="5"
     *         
     */
    public Short getRequiredParentRankId() {
        return this.requiredParentRankId;
    }
    
    public void setRequiredParentRankId(Short requiredParentRankId) {
        this.requiredParentRankId = requiredParentRankId;
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
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="none"
     *            @hibernate.collection-key
     *             column="TaxonomicUnitTypeID"
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




}