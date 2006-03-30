package edu.ku.brc.specify.datamodel;

import java.util.Set;




/**

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
     * 
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
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
     * 
     */
    public SpecifyUser getUser() {
        return this.user;
    }
    
    public void setUser(SpecifyUser user) {
        this.user = user;
    }

    /**
     * 
     */
    public Set getAttributeDefs() {
        return this.attributeDefs;
    }
    
    public void setAttributeDefs(Set attributeDefs) {
        this.attributeDefs = attributeDefs;
    }

    /**
     * 
     */
    public GeographyTreeDef getGeographyTreeDef() {
        return this.geographyTreeDef;
    }
    
    public void setGeographyTreeDef(GeographyTreeDef geographyTreeDef) {
        this.geographyTreeDef = geographyTreeDef;
    }

    /**
     * 
     */
    public GeologicTimePeriodTreeDef getGeologicTimePeriodTreeDef() {
        return this.geologicTimePeriodTreeDef;
    }
    
    public void setGeologicTimePeriodTreeDef(GeologicTimePeriodTreeDef geologicTimePeriodTreeDef) {
        this.geologicTimePeriodTreeDef = geologicTimePeriodTreeDef;
    }

    /**
     * 
     */
    public LocationTreeDef getLocationTreeDef() {
        return this.locationTreeDef;
    }
    
    public void setLocationTreeDef(LocationTreeDef locationTreeDef) {
        this.locationTreeDef = locationTreeDef;
    }

    /**
     *      * @hibernate.one-to-one
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