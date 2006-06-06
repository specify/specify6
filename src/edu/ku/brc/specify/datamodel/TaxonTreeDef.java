package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class TaxonTreeDef implements TreeDefinitionIface,java.io.Serializable {

    // Fields    

     protected Integer taxonTreeDefId;
     protected String name;
     protected String remarks;
     protected CollectionObjDef collObjDef;
     protected Set<Taxon> treeEntries;
     protected Set<TaxonTreeDefItem> treeDefItems;

    // Constructors

    /** default constructor */
    public TaxonTreeDef() {
    }
    
    /** constructor with id */
    public TaxonTreeDef(Integer taxonTreeDefId) {
        this.taxonTreeDefId = taxonTreeDefId;
    }
   
    
    // Initializer
    public void initialize()
    {
    	taxonTreeDefId = null;
        name = null;
        remarks = null;
        collObjDef = null;
        treeEntries = new HashSet<Taxon>();
        treeDefItems = new HashSet<TaxonTreeDefItem>();
    }
    // End Initializer
 
    // Property accessors

    /**
     * 
     */
    public Integer getTaxonTreeDefId() {
        return this.taxonTreeDefId;
    }
    
    public void setTaxonTreeDefId(Integer taxonTreeDefId) {
        this.taxonTreeDefId = taxonTreeDefId;
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
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * 
     */
    public CollectionObjDef getCollObjDef() {
        return this.collObjDef;
    }
    
    public void setCollObjDef(CollectionObjDef collObjDef) {
        this.collObjDef = collObjDef;
    }

    /**
     * 
     */
    public Set getTreeEntries() {
        return this.treeEntries;
    }
    
    @SuppressWarnings("unchecked")
	public void setTreeEntries(Set treeEntries) {
        this.treeEntries = treeEntries;
    }

    /**
     * 
     */
    public Set getTreeDefItems() {
        return this.treeDefItems;
    }
    
    @SuppressWarnings("unchecked")
	public void setTreeDefItems(Set treeDefItems) {
        this.treeDefItems = treeDefItems;
    }
    
    //
    // Methods added to implement TreeDefinitionIface
    //

	public Integer getTreeDefId()
	{
		return getTaxonTreeDefId();
	}

	public void setTreeDefId(Integer id)
	{
		setTaxonTreeDefId(id);
	}

	public void addTreeEntry( Taxon taxon )
	{
		treeEntries.add(taxon);
		taxon.setTreeDef(this);
	}
	
	public void removeTreeEntry( Taxon taxon )
	{
		treeEntries.remove(taxon);
		taxon.setTreeDef(null);
	}
	
	public void addTreeDefItem( TaxonTreeDefItem item )
	{
		treeDefItems.add(item);
		item.setTreeDef(this);
	}
	
	public void removeTreeDefItem( TaxonTreeDefItem item )
	{
		treeDefItems.remove(item);
		item.setTreeDef(null);
	}
}