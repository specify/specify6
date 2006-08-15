package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class GeographyTreeDefItem extends AbstractTreeDefItem implements java.io.Serializable {

    // Fields    

     protected Integer geographyTreeDefItemId;
     protected String name;
     protected String remarks;
     protected Integer rankId;
     protected Boolean isEnforced;
     protected Boolean isInFullName;
     protected GeographyTreeDef treeDef;
     protected GeographyTreeDefItem parent;
     protected Set<Geography> treeEntries;
     protected Set<GeographyTreeDefItem> children;

    // Constructors

    /** default constructor */
    public GeographyTreeDefItem() {
    }
    
    /** constructor with id */
    public GeographyTreeDefItem(Integer geographyTreeDefItemId) {
        this.geographyTreeDefItemId = geographyTreeDefItemId;
    }
   
    // Initializer
    public void initialize()
    {
    	geographyTreeDefItemId = null;
        name = null;
        remarks = null;
        rankId = null;
        isEnforced = null;
        isInFullName = null;
        treeDef = null;
        parent = null;
        treeEntries = new HashSet<Geography>();
        children = new HashSet<GeographyTreeDefItem>();
    }
    // End Initializer
    
    // Property accessors

    /**
     * 
     */
    public Integer getGeographyTreeDefItemId() {
        return this.geographyTreeDefItemId;
    }
    
    public void setGeographyTreeDefItemId(Integer geographyTreeDefItemId) {
        this.geographyTreeDefItemId = geographyTreeDefItemId;
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

    public String getRemarks()
	{
		return remarks;
	}

	public void setRemarks(String remarks)
	{
		this.remarks = remarks;
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

    public Boolean getIsInFullName()
	{
		return isInFullName;
	}

	public void setIsInFullName(Boolean isInFullName)
	{
		this.isInFullName = isInFullName;
	}

	/**
     * 
     */
    public GeographyTreeDef getTreeDef() {
        return this.treeDef;
    }
    
    public void setTreeDef(GeographyTreeDef treeDef) {
        this.treeDef = treeDef;
    }

    /**
     * 
     */
    public GeographyTreeDefItem getParent() {
        return this.parent;
    }
    
    public void setParent(GeographyTreeDefItem parent) {
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
    
    public TreeDefinitionIface getTreeDefinition()
    {
        return getTreeDef();
    }
    
    public void setTreeDefinition(TreeDefinitionIface treeDef)
    {
        if( treeDef==null || !(treeDef instanceof GeographyTreeDef) )
        {
            throw new IllegalArgumentException("Argument must be an instance of GeographyTreeDef");
        }
        setTreeDef((GeographyTreeDef)treeDef);
    }
    
    public TreeDefinitionItemIface getParentItem()
    {
        return getParent();
    }
    
    public void setParentItem(TreeDefinitionItemIface parent)
    {
        if( parent==null || !(parent instanceof GeographyTreeDefItem) )
        {
            throw new IllegalArgumentException("Argument must be an instance of GeographyTreeDefItem");
        }
        setParent((GeographyTreeDefItem)parent);
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
    	if(child==null)
    	{
    		Set children = new HashSet<GeographyTreeDefItem>();
    		setChildren(children);
    		return;
    	}
    	
        if( !(child instanceof GeographyTreeDefItem) )
        {
            throw new IllegalArgumentException("Argument must be an instance of GeographyTreeDefItem");
        }
        Set children = new HashSet<GeographyTreeDefItem>();
        children.add(child);
        setChildren(children);
    }
    
	public Integer getTreeDefItemId()
	{
		return getGeographyTreeDefItemId();
	}

	public void setTreeDefItemId(Integer id)
	{
		setGeographyTreeDefItemId(id);
	}

	public void addTreeEntry( Geography entry )
	{
		treeEntries.add(entry);
		entry.setDefinitionItem(this);
	}
	
	public void removeTreeEntry( Geography entry )
	{
		treeEntries.remove(entry);
		entry.setDefinitionItem(null);
	}
	
	public void setChild( GeographyTreeDefItem child )
	{
		for( GeographyTreeDefItem item: children )
		{
			removeChild( item );
		}
		
		children.add(child);
		child.setParent(this);
	}
	
	public void removeChild( GeographyTreeDefItem child )
	{
		children.remove(child);
		child.setParent(null);
	}
}