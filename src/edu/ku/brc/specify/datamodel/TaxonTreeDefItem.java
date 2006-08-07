package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class TaxonTreeDefItem extends AbstractTreeDefItem implements java.io.Serializable {

    // Fields    

     protected Integer taxonTreeDefItemId;
     protected String name;
     protected String remarks;
     protected Integer rankId;
     protected Boolean isEnforced;
     protected Boolean isInFullName;
     protected TaxonTreeDef treeDef;
     protected TaxonTreeDefItem parent;
     protected Set<Taxon> treeEntries;
     protected Set<TaxonTreeDefItem> children;

    // Constructors

    /** default constructor */
    public TaxonTreeDefItem() {
    }
    
    /** constructor with id */
    public TaxonTreeDefItem(Integer taxonTreeDefItemId) {
        this.taxonTreeDefItemId = taxonTreeDefItemId;
    }
   
    // Initializer
    public void initialize()
    {
    	taxonTreeDefItemId = null;
        name = null;
        remarks = null;
        rankId = null;
        isEnforced = null;
        isInFullName = null;
        treeDef = null;
        treeEntries = new HashSet<Taxon>();
        parent = null;
        children = new HashSet<TaxonTreeDefItem>();
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Integer getTaxonTreeDefItemId() {
        return this.taxonTreeDefItemId;
    }
    
    public void setTaxonTreeDefItemId(Integer taxonTreeDefItemId) {
        this.taxonTreeDefItemId = taxonTreeDefItemId;
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
    public TaxonTreeDef getTreeDef() {
        return this.treeDef;
    }
    
    public void setTreeDef(TaxonTreeDef treeDef) {
        this.treeDef = treeDef;
    }

    /**
     * 
     */
    public TaxonTreeDefItem getParent() {
        return this.parent;
    }
    
    public void setParent(TaxonTreeDefItem parent) {
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
		return getTaxonTreeDefItemId();
	}

	public void setTreeDefItemId(Integer id)
	{
		setTaxonTreeDefItemId(id);
	}

    public TreeDefinitionIface getTreeDefinition()
    {
        return getTreeDef();
    }
    
    public void setTreeDefinition(TreeDefinitionIface treeDef)
    {
        if( treeDef!=null && !(treeDef instanceof TaxonTreeDef) )
        {
            throw new IllegalArgumentException("Argument must be an instance of TaxonTreeDef");
        }
        setTreeDef((TaxonTreeDef)treeDef);
    }
    
    public TreeDefinitionItemIface getParentItem()
    {
        return getParent();
    }
    
    public void setParentItem(TreeDefinitionItemIface parent)
    {
        if( parent!=null && !(parent instanceof TaxonTreeDefItem) )
        {
            throw new IllegalArgumentException("Argument must be an instance of TaxonTreeDefItem");
        }
        setParent((TaxonTreeDefItem)parent);
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
    		Set children = new HashSet<TaxonTreeDefItem>();
    		setChildren(children);
    		return;
    	}
    	
        if( !(child instanceof TaxonTreeDefItem) )
        {
            throw new IllegalArgumentException("Argument must be an instance of TaxonTreeDefItem");
        }
        Set children = new HashSet<TaxonTreeDefItem>();
        children.add(child);
        setChildren(children);
    }

	public void addTreeEntry( Taxon entry )
	{
		treeEntries.add(entry);
		entry.setDefinitionItem(this);
	}
	
	public void removeTreeEntry( Taxon entry )
	{
		treeEntries.remove(entry);
		entry.setDefinitionItem(null);
	}
	
	public void setChild( TaxonTreeDefItem child )
	{
		for( TaxonTreeDefItem item: children )
		{
			removeChild( item );
		}
		
		children.add(child);
		child.setParent(this);
	}
	 
	public void removeChild( TaxonTreeDefItem child )
	{
		children.remove(child);
		child.setParent(null);
	}
}