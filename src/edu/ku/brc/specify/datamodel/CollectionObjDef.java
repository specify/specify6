package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;




/**

 */
public class CollectionObjDef  implements java.io.Serializable {

    // Fields

     protected Integer collectionObjDefId;
     protected String name;
     protected DataType dataType;
     protected Set<CatalogSeries> catalogSeries;
     protected SpecifyUser user;
     protected Set<AttributeDef> attributeDefs;
     protected GeographyTreeDef geographyTreeDef;
     protected GeologicTimePeriodTreeDef geologicTimePeriodTreeDef;
     protected LocationTreeDef locationTreeDef;
     protected TaxonTreeDef taxonTreeDef;
     protected Set<Locality> localities;


    // Constructors

    /** default constructor */
    public CollectionObjDef() {
    }

    /** constructor with id */
    public CollectionObjDef(Integer collectionObjDefId) {
        this.collectionObjDefId = collectionObjDefId;
    }




    // Initializer
    public void initialize()
    {
        collectionObjDefId = null;
        name = null;
        dataType = null;
        catalogSeries = new HashSet<CatalogSeries>();
        user = null;
        attributeDefs = new HashSet<AttributeDef>();
        geographyTreeDef = null;
        geologicTimePeriodTreeDef = null;
        locationTreeDef = null;
        taxonTreeDef = null;
        localities = new HashSet<Locality>();
    }
    // End Initializer

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
    public Set<CatalogSeries> getCatalogSeries() {
        return this.catalogSeries;
    }

    public void setCatalogSeries(Set<CatalogSeries> catalogSeries) {
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
    public Set<AttributeDef> getAttributeDefs() {
        return this.attributeDefs;
    }

    public void setAttributeDefs(Set<AttributeDef> attributeDefs) {
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
    public Set<Locality> getLocalities() {
        return this.localities;
    }

    public void setLocalities(Set<Locality> localities) {
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



    // Add Methods

    public void addCatalogSeries(final CatalogSeries catalogSeries)
    {
        this.catalogSeries.add(catalogSeries);
    }

    public void addAttributeDef(final AttributeDef attributeDef)
    {
        this.attributeDefs.add(attributeDef);
    }

    public void addLocalitie(final Locality localities)
    {
        this.localities.add(localities);
    }

    // Done Add Methods
}
