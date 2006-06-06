package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class LocationTreeDefItem  implements TreeDefinitionItemIface,java.io.Serializable {

    // Fields    

     protected Integer locationTreeDefItemId;
     protected String name;
     protected Integer rankId;
     protected Boolean isEnforced;
     protected LocationTreeDef treeDef;
     protected LocationTreeDefItem parent;
     protected Set<Location> treeEntries;
     protected Set<LocationTreeDefItem> children;

    // Constructors

    /** default constructor */
    public LocationTreeDefItem() {
    }
    
    /** constructor with id */
    public LocationTreeDefItem(Integer locationTreeDefItemId) {
        this.locationTreeDefItemId = locationTreeDefItemId;
    }
    
    // Initializer
    public void initialize()
    {
    	locationTreeDefItemId = null;
        name = null;
        rankId = null;
        isEnforced = null;
        treeDef = null;
        parent = null;
        treeEntries = new HashSet<Location>();
        children = new HashSet<LocationTreeDefItem>();
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Integer getLocationTreeDefItemId() {
        return this.locationTreeDefItemId;
    }
    
    public void setLocationTreeDefItemId(Integer locationTreeDefItemId) {
        this.locationTreeDefItemId = locationTreeDefItemId;
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
    public Integer getRankId() {
        return this.rankId;
    }
    
    public void setRankId(Integer rankId) {
        this.rankId = rankId;
    }

    /**
     * 
     */
    public Boolean getIsEnforced() {
        return this.isEnforced;
    }
    
    public void setIsEnforced(Boolean isEnforced) {
        this.isEnforced = isEnforced;
    }

    /**
     * 
     */
    public LocationTreeDef getTreeDef() {
        return this.treeDef;
    }
    
    public void setTreeDef(LocationTreeDef treeDef) {
        this.treeDef = treeDef;
    }

    /**
     * 
     */
    public LocationTreeDefItem getParent() {
        return this.parent;
    }
    
    public void setParent(LocationTreeDefItem parent) {
        this.parent = parent;
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
    public Set getChildren() {
        return this.children;
    }
    
    @SuppressWarnings("unchecked")
	public void setChildren(Set children) {
        this.children = children;
    }

    // Code added to implement TreeDefinitionItemIface

	public Integer getTreeDefItemId()
	{
		return getLocationTreeDefItemId();
	}

	public void setTreeDefItemId(Integer id)
	{
		setLocationTreeDefItemId(id);
	}

    public TreeDefinitionIface getTreeDefinition()
    {
        return getTreeDef();
    }
    
    public void setTreeDefinition(TreeDefinitionIface treeDef)
    {
        if( !(treeDef instanceof LocationTreeDef) )
        {
            throw new IllegalArgumentException("Argument must be an instance of LocationTreeDef");
        }
        setTreeDef((LocationTreeDef)treeDef);
    }
    
    public TreeDefinitionItemIface getParentItem()
    {
        return getParent();
    }
    
    public void setParentItem(TreeDefinitionItemIface parent)
    {
        if( !(parent instanceof LocationTreeDefItem) )
        {
            throw new IllegalArgumentException("Argument must be an instance of LocationTreeDefItem");
        }
        setParent((LocationTreeDefItem)parent);
    }

    public TreeDefinitionItemIface getChildItem()
    {
        if( getChildren().isEmpty() )
        {
            return null;
        }
        
        return (TreeDefinitionItemIface)getChildren().iterator().next();
    }
    
    @SuppressWarnings("unchecked")
	public void setChildItem(TreeDefinitionItemIface child)
    {
        if( !(child instanceof LocationTreeDefItem) )
        {
            throw new IllegalArgumentException("Argument must be an instance of LocationTreeDefItem");
        }
        Set children = new HashSet<LocationTreeDefItem>();
        children.add(child);
        setChildren(children);
    }

	public void addTreeEntry( Location entry )
	{
		treeEntries.add(entry);
		entry.setDefinitionItem(this);
	}
	
	public void removeTreeEntry( Location entry )
	{
		treeEntries.remove(entry);
		entry.setDefinitionItem(null);
	}
	
	public void setChild( LocationTreeDefItem child )
	{
		for( LocationTreeDefItem item: children )
		{
			removeChild( item );
		}
		
		children.add(child);
		child.setParent(this);
	}
	
	public void removeChild( LocationTreeDefItem child )
	{
		children.remove(child);
		child.setParent(null);
	}
}