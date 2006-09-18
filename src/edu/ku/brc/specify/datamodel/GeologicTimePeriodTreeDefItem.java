package edu.ku.brc.specify.datamodel;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class GeologicTimePeriodTreeDefItem implements Serializable, TreeDefItemIface<GeologicTimePeriod,GeologicTimePeriodTreeDef,GeologicTimePeriodTreeDefItem>
{

	// Fields    

	protected Long				    taxonTreeDefItemId;
	protected String				name;
	protected String				remarks;
	protected Integer				rankId;
	protected Boolean				isEnforced;
	protected Boolean				isInFullName;
	protected GeologicTimePeriodTreeDef			treeDef;
	protected GeologicTimePeriodTreeDefItem		parent;
	protected Set<GeologicTimePeriod>			treeEntries;
	protected Set<GeologicTimePeriodTreeDefItem>	children;

	// Constructors

	/** default constructor */
	public GeologicTimePeriodTreeDefItem()
	{
		// do nothing
	}

	/** constructor with id */
	public GeologicTimePeriodTreeDefItem(Long taxonTreeDefItemId)
	{
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
		treeEntries = new HashSet<GeologicTimePeriod>();
		parent = null;
		children = new HashSet<GeologicTimePeriodTreeDefItem>();
	}

	// End Initializer

	// Property accessors

	/**
	 * 
	 */
	public Long getGeologicTimePeriodTreeDefItemId()
	{
		return this.taxonTreeDefItemId;
	}

	public void setGeologicTimePeriodTreeDefItemId(Long taxonTreeDefItemId)
	{
		this.taxonTreeDefItemId = taxonTreeDefItemId;
	}

	/**
	 * 
	 */
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * 
	 */
	public String getRemarks()
	{
		return this.remarks;
	}

	public void setRemarks(String remarks)
	{
		this.remarks = remarks;
	}

	/**
	 * 
	 */
	public Integer getRankId()
	{
		return this.rankId;
	}

	public void setRankId(Integer rankId)
	{
		this.rankId = rankId;
	}

	/**
	 * 
	 */
	public Boolean getIsEnforced()
	{
		return this.isEnforced;
	}

	public void setIsEnforced(Boolean isEnforced)
	{
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
	public GeologicTimePeriodTreeDef getTreeDef()
	{
		return this.treeDef;
	}

	public void setTreeDef(GeologicTimePeriodTreeDef treeDef)
	{
		this.treeDef = treeDef;
	}

	/**
	 * 
	 */
	public GeologicTimePeriodTreeDefItem getParent()
	{
		return this.parent;
	}

	public void setParent(GeologicTimePeriodTreeDefItem parent)
	{
		this.parent = parent;
	}

	/**
	 * 
	 */
	public Set<GeologicTimePeriod> getTreeEntries()
	{
		return this.treeEntries;
	}

	public void setTreeEntries(Set<GeologicTimePeriod> treeEntries)
	{
		this.treeEntries = treeEntries;
	}

	/**
	 * 
	 */
	public Set<GeologicTimePeriodTreeDefItem> getChildren()
	{
		return this.children;
	}

	public void setChildren(Set<GeologicTimePeriodTreeDefItem> children)
	{
		this.children = children;
	}

	// Code added to implement TreeDefinitionItemIface

	public Long getTreeDefItemId()
	{
		return getGeologicTimePeriodTreeDefItemId();
	}

	public void setTreeDefItemId(Long id)
	{
		setGeologicTimePeriodTreeDefItemId(id);
	}

	public void setChild(GeologicTimePeriodTreeDefItem child)
	{
		if( child==null )
		{
			children = new HashSet<GeologicTimePeriodTreeDefItem>();
			return;
		}

		children = new HashSet<GeologicTimePeriodTreeDefItem>();
		children.add(child);
	}
	
	public GeologicTimePeriodTreeDefItem getChild()
	{
		if(children.isEmpty())
		{
			return null;
		}
		return children.iterator().next();
	}

	public void addTreeEntry(GeologicTimePeriod entry)
	{
		treeEntries.add(entry);
		entry.setDefinitionItem(this);
	}

	public void removeTreeEntry(GeologicTimePeriod entry)
	{
		treeEntries.remove(entry);
		entry.setDefinitionItem(null);
	}

	public void removeChild(GeologicTimePeriodTreeDefItem child)
	{
		children.remove(child);
		child.setParent(null);
	}
	
	public boolean canBeDeleted()
	{
		if(treeEntries.isEmpty())
		{
			return true;
		}
		return false;
	}
}
