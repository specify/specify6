package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class GeologicTimePeriodTreeDef  implements TreeDefinitionIface,java.io.Serializable {

    // Fields    

     protected Integer geologicTimePeriodTreeDefId;
     protected String name;
     protected String remarks;
     protected Set<GeologicTimePeriod> treeEntries;
     protected Set<GeologicTimePeriodTreeDefItem> treeDefItems;
     protected Set<CollectionObjDef> collObjDefs;

    // Constructors

    /** default constructor */
    public GeologicTimePeriodTreeDef() {
    }
    
    /** constructor with id */
    public GeologicTimePeriodTreeDef(Integer geologicTimePeriodTreeDefId) {
        this.geologicTimePeriodTreeDefId = geologicTimePeriodTreeDefId;
    }
   
    // Initializer
    public void initialize()
    {
    	geologicTimePeriodTreeDefId = null;
        name = null;
        remarks = null;
        treeEntries = new HashSet<GeologicTimePeriod>();
        treeDefItems = new HashSet<GeologicTimePeriodTreeDefItem>();
        collObjDefs = new HashSet<CollectionObjDef>();
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Integer getGeologicTimePeriodTreeDefId() {
        return this.geologicTimePeriodTreeDefId;
    }
    
    public void setGeologicTimePeriodTreeDefId(Integer geologicTimePeriodTreeDefId) {
        this.geologicTimePeriodTreeDefId = geologicTimePeriodTreeDefId;
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

    /**
     * 
     */
    public Set getCollObjDefs() {
        return this.collObjDefs;
    }
    
    public void setCollObjDefs(Set<CollectionObjDef> collObjDefs) {
        this.collObjDefs = collObjDefs;
    }

    //
    // Methods added to implement TreeDefinitionIface
    //

	public Integer getTreeDefId()
	{
		return getGeologicTimePeriodTreeDefId();
	}

	public void setTreeDefId(Integer id)
	{
		setGeologicTimePeriodTreeDefId(id);
	}

	public void addTreeEntry( GeologicTimePeriod geo )
	{
		treeEntries.add(geo);
		geo.setTreeDef(this);
	}
	
	public void removeTreeEntry( GeologicTimePeriod geo )
	{
		treeEntries.remove(geo);
		geo.setTreeDef(null);
	}
	
	public void addTreeDefItem( GeologicTimePeriodTreeDefItem item )
	{
		treeDefItems.add(item);
		item.setGeologicTimePeriodTreeDef(this);
	}
	
	public void removeTreeDefItem( GeologicTimePeriodTreeDefItem item )
	{
		treeDefItems.remove(item);
		item.setGeologicTimePeriodTreeDef(null);
	}
	
	public void addCollectionObjDef( CollectionObjDef def )
	{
		collObjDefs.add(def);
		def.setGeologicTimePeriodTreeDef(this);
	}
	
	public void removeCollectionObjDef( CollectionObjDef def )
	{
		collObjDefs.remove(def);
		def.setGeologicTimePeriodTreeDef(null);
	}
}