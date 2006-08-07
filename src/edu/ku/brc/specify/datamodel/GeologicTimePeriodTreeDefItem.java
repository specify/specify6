package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class GeologicTimePeriodTreeDefItem extends AbstractTreeDefItem implements java.io.Serializable {

    // Fields    

     protected Integer geologicTimePeriodTreeDefItemId;
     protected String name;
     protected String remarks;
     protected Integer rankId;
     protected Boolean isEnforced;
     protected Boolean isInFullName;
     protected GeologicTimePeriodTreeDef geologicTimePeriodTreeDef;
     protected GeologicTimePeriodTreeDefItem parent;
     protected Set<GeologicTimePeriod> treeEntries;
     protected Set<GeologicTimePeriodTreeDefItem> children;

    // Constructors

    /** default constructor */
    public GeologicTimePeriodTreeDefItem() {
    }
    
    /** constructor with id */
    public GeologicTimePeriodTreeDefItem(Integer geologicTimePeriodTreeDefItemId) {
        this.geologicTimePeriodTreeDefItemId = geologicTimePeriodTreeDefItemId;
    }
   
    // Initializer
    public void initialize()
    {
    	geologicTimePeriodTreeDefItemId = null;
        name = null;
        remarks = null;
        rankId = null;
        isEnforced = null;
        isInFullName = null;
        geologicTimePeriodTreeDef = null;
        parent = null;
        treeEntries = new HashSet<GeologicTimePeriod>();
        children = new HashSet<GeologicTimePeriodTreeDefItem>();
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Integer getGeologicTimePeriodTreeDefItemId() {
        return this.geologicTimePeriodTreeDefItemId;
    }
    
    public void setGeologicTimePeriodTreeDefItemId(Integer geologicTimePeriodTreeDefItemId) {
        this.geologicTimePeriodTreeDefItemId = geologicTimePeriodTreeDefItemId;
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
    public GeologicTimePeriodTreeDef getGeologicTimePeriodTreeDef() {
        return this.geologicTimePeriodTreeDef;
    }
    
    public void setGeologicTimePeriodTreeDef(GeologicTimePeriodTreeDef treeDef) {
        this.geologicTimePeriodTreeDef = treeDef;
    }

    /**
     * 
     */
    public GeologicTimePeriodTreeDefItem getParent() {
        return this.parent;
    }
    
    public void setParent(GeologicTimePeriodTreeDefItem parent) {
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
        return getGeologicTimePeriodTreeDef();
    }
    
    public void setTreeDefinition(TreeDefinitionIface treeDef)
    {
        if( treeDef!=null && !(treeDef instanceof GeologicTimePeriodTreeDef) )
        {
            throw new IllegalArgumentException("Argument must be an instance of GeologicTimePeriodTreeDef");
        }
        setGeologicTimePeriodTreeDef((GeologicTimePeriodTreeDef)treeDef);
    }
    
    public TreeDefinitionItemIface getParentItem()
    {
        return getParent();
    }
    
    public void setParentItem(TreeDefinitionItemIface parent)
    {
        if( parent!=null && !(parent instanceof GeologicTimePeriodTreeDefItem) )
        {
            throw new IllegalArgumentException("Argument must be an instance of GeologicTimePeriodTreeDefItem");
        }
        setParent((GeologicTimePeriodTreeDefItem)parent);
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
    		Set children = new HashSet<GeologicTimePeriodTreeDefItem>();
    		setChildren(children);
    		return;
    	}
    	
        if( !(child instanceof GeologicTimePeriodTreeDefItem) )
        {
            throw new IllegalArgumentException("Argument must be an instance of GeologicTimePeriodTreeDefItem");
        }
        Set children = new HashSet<GeologicTimePeriodTreeDefItem>();
        children.add(child);
        setChildren(children);
    }

	public Integer getTreeDefItemId()
	{
		return getGeologicTimePeriodTreeDefItemId();
	}

	public void setTreeDefItemId(Integer id)
	{
		setGeologicTimePeriodTreeDefItemId(id);
	}

	public void addTreeEntry( GeologicTimePeriod entry )
	{
		treeEntries.add(entry);
		entry.setDefinitionItem(this);
	}
	
	public void removeTreeEntry( GeologicTimePeriod entry )
	{
		treeEntries.remove(entry);
		entry.setDefinitionItem(null);
	}
	
	public void setChild( GeologicTimePeriodTreeDefItem child )
	{
		for( GeologicTimePeriodTreeDefItem item: children )
		{
			removeChild( item );
		}
		
		children.add(child);
		child.setParent(this);
	}
	
	public void removeChild( GeologicTimePeriodTreeDefItem child )
	{
		children.remove(child);
		child.setParent(null);
	}
}