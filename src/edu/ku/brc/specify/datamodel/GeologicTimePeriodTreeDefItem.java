package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class GeologicTimePeriodTreeDefItem  implements TreeDefinitionItemIface,java.io.Serializable {

    // Fields    

     protected Integer geologicTimePeriodTreeDefItemId;
     protected String name;
     protected Integer rankId;
     protected Boolean isEnforced;
     protected GeologicTimePeriodTreeDef treeDef;
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
        rankId = null;
        isEnforced = null;
        treeDef = null;
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
    public GeologicTimePeriodTreeDef getTreeDef() {
        return this.treeDef;
    }
    
    public void setTreeDef(GeologicTimePeriodTreeDef treeDef) {
        this.treeDef = treeDef;
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
        return getTreeDef();
    }
    
    public void setTreeDefinition(TreeDefinitionIface treeDef)
    {
        if( !(treeDef instanceof GeologicTimePeriodTreeDef) )
        {
            throw new IllegalArgumentException("Argument must be an instance of GeologicTimePeriodTreeDef");
        }
        setTreeDef((GeologicTimePeriodTreeDef)treeDef);
    }
    
    public TreeDefinitionItemIface getParentItem()
    {
        return getParent();
    }
    
    public void setParentItem(TreeDefinitionItemIface parent)
    {
        if( !(parent instanceof GeologicTimePeriodTreeDefItem) )
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
}