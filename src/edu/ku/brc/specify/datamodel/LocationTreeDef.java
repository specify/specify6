package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class LocationTreeDef  implements TreeDefinitionIface,java.io.Serializable {

    // Fields    

     protected Integer locationTreeDefId;
     protected String name;
     protected String remarks;
     protected Set<Location> treeEntries;
     protected Set<LocationTreeDefItem> treeDefItems;
     protected Set<CollectionObjDef> collObjDefs;

    // Constructors

    /** default constructor */
    public LocationTreeDef() {
    }
    
    /** constructor with id */
    public LocationTreeDef(Integer locationTreeDefId) {
        this.locationTreeDefId = locationTreeDefId;
    }

    // Initializer
    public void initialize()
    {
    	locationTreeDefId = null;
        name = null;
        remarks = null;
        treeEntries = new HashSet<Location>();
        treeDefItems = new HashSet<LocationTreeDefItem>();
        collObjDefs = new HashSet<CollectionObjDef>();
    }
    // End Initializer 

    // Property accessors

    /**
     * 
     */
    public Integer getLocationTreeDefId() {
        return this.locationTreeDefId;
    }
    
    public void setLocationTreeDefId(Integer locationTreeDefId) {
        this.locationTreeDefId = locationTreeDefId;
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
    public Set<CollectionObjDef> getCollObjDefs() {
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
		return getLocationTreeDefId();
	}

	public void setTreeDefId(Integer id)
	{
		setLocationTreeDefId(id);
	}

	public void addTreeEntry( Location loc )
	{
		treeEntries.add(loc);
		loc.setTreeDef(this);
	}
	
	public void removeTreeEntry( Location loc )
	{
		treeEntries.remove(loc);
		loc.setTreeDef(null);
	}
	
	public void addTreeDefItem( LocationTreeDefItem item )
	{
		treeDefItems.add(item);
		item.setTreeDef(this);
	}
	
	public void removeTreeDefItem( LocationTreeDefItem item )
	{
		treeDefItems.remove(item);
		item.setTreeDef(null);
	}
	
	public void addCollectionObjDef( CollectionObjDef def )
	{
		collObjDefs.add(def);
		def.setLocationTreeDef(this);
	}
	
	public void removeCollectionObjDef( CollectionObjDef def )
	{
		collObjDefs.remove(def);
		def.setLocationTreeDef(null);
	}
}