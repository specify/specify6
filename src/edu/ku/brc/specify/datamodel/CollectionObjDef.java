package edu.ku.brc.specify.datamodel;

import java.util.Set;




/**
 *        @hibernate.class
 *         table="collectionobjdef"
 *     
 */
public class CollectionObjDef  implements java.io.Serializable {

    // Fields    

     protected Integer collectionObjDefId;
     protected String name;
     protected DataType dataType;
     private Set catalogSeries;
     protected SpecifyUser user;
     private Set attributeDefs;
     private GeographyTreeDef geographyTreeDef;
     private GeologicTimePeriodTreeDef geologicTimePeriodTreeDef;
     private LocationTreeDef locationTreeDef;
     protected TaxonTreeDef taxonTreeDef;
     private Set localities;


    // Constructors

    /** default constructor */
    public CollectionObjDef() {
    }
    
    /** constructor with id */
    public CollectionObjDef(Integer collectionObjDefId) {
        this.collectionObjDefId = collectionObjDefId;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Integer getCollectionObjDefId() {
        return this.collectionObjDefId;
    }
    
    public void setCollectionObjDefId(Integer collectionObjDefId) {
        this.collectionObjDefId = collectionObjDefId;
    }

    /**
     *      *            @hibernate.property
     *             column="Name"
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
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="DataTypeID"
     *         
     */
    public DataType getDataType() {
        return this.dataType;
    }
    
    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    /**
     * 
     */
    public Set getCatalogSeries() {
        return this.catalogSeries;
    }
    
    public void setCatalogSeries(Set catalogSeries) {
        this.catalogSeries = catalogSeries;
    }

    /**
     *      *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="SpecifyUserID"
     *         
     */
    public SpecifyUser getUser() {
        return this.user;
    }
    
    public void setUser(SpecifyUser user) {
        this.user = user;
    }

    /**
     *      *            @hibernate.set
     *             lazy="true"
     *             inverse="true"
     *             cascade="delete"
     *            @hibernate.collection-key
     *             column="CollectionObjDefID"
     *            @hibernate.collection-one-to-many
     *             class="edu.ku.brc.specify.datamodel.AttributeDef"
     *         
     */
    public Set getAttributeDefs() {
        return this.attributeDefs;
    }
    
    public void setAttributeDefs(Set attributeDefs) {
        this.attributeDefs = attributeDefs;
    }

    /**
     *      *  @hibernate.many-to-one not-null="true"
     *             @hibernate.column name="GeographyTreeDefID"
     */
    public GeographyTreeDef getGeographyTreeDef() {
        return this.geographyTreeDef;
    }
    
    public void setGeographyTreeDef(GeographyTreeDef geographyTreeDef) {
        this.geographyTreeDef = geographyTreeDef;
    }

    /**
     *      *  @hibernate.many-to-one not-null="true"
     *             @hibernate.column name="GeologicTimePeriodTreeDefID"
     */
    public GeologicTimePeriodTreeDef getGeologicTimePeriodTreeDef() {
        return this.geologicTimePeriodTreeDef;
    }
    
    public void setGeologicTimePeriodTreeDef(GeologicTimePeriodTreeDef geologicTimePeriodTreeDef) {
        this.geologicTimePeriodTreeDef = geologicTimePeriodTreeDef;
    }

    /**
     *      *  @hibernate.many-to-one not-null="true"
     *             @hibernate.column name="LocationTreeDefID"
     */
    public LocationTreeDef getLocationTreeDef() {
        return this.locationTreeDef;
    }
    
    public void setLocationTreeDef(LocationTreeDef locationTreeDef) {
        this.locationTreeDef = locationTreeDef;
    }

    /**
     *      *             @hibernate.one-to-one
     *         
     */
    public TaxonTreeDef getTaxonTreeDef() {
        return this.taxonTreeDef;
    }
    
    public void setTaxonTreeDef(TaxonTreeDef taxonTreeDef) {
        this.taxonTreeDef = taxonTreeDef;
    }

    /**
     * 
     */
    public Set getLocalities() {
        return this.localities;
    }
    
    public void setLocalities(Set localities) {
        this.localities = localities;
    }

  /**
	 * toString
	 * @return String
	 */
  public String toString() {
	  StringBuffer buffer = new StringBuffer();

      buffer.append(getClass().getName()).append("@").append(Integer.toHexString(hashCode())).append(" [");
      buffer.append("name").append("='").append(getName()).append("' ");			
      buffer.append("]");
      
      return buffer.toString();
	}



}