/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@SuppressWarnings("serial")
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "locationtreedef")
public class LocationTreeDef extends DataModelObjBase implements java.io.Serializable, TreeDefIface<Location, LocationTreeDef, LocationTreeDefItem>
{
	protected Long				   locationTreeDefId;
	protected String				name;
	protected String				remarks;
    protected Integer               fullNameDirection;
	protected Set<CollectionType>   collectionTypes;
	protected Set<Location>			treeEntries;
	protected Set<LocationTreeDefItem>	treeDefItems;

	/** default constructor */
	public LocationTreeDef()
	{
		// do nothing
	}

	/** constructor with id */
	public LocationTreeDef(Long locationTreeDefId)
	{
		this.locationTreeDefId = locationTreeDefId;
	}

	@Override
    public void initialize()
	{
        super.init();
		locationTreeDefId = null;
		name = null;
		remarks = null;
        fullNameDirection = null;
		collectionTypes = new HashSet<CollectionType>();
		treeEntries = new HashSet<Location>();
		treeDefItems = new HashSet<LocationTreeDefItem>();
	}

	/**
	 * 
	 */
    @Id
    @GeneratedValue
    @Column(name = "LocationTreeDefID", unique = false, nullable = false, insertable = true, updatable = true)
	public Long getLocationTreeDefId()
	{
		return this.locationTreeDefId;
	}

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Override
    @Transient
    public Long getId()
    {
        return this.locationTreeDefId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return LocationTreeDef.class;
    }

	public void setLocationTreeDefId(Long locationTreeDefId)
	{
		this.locationTreeDefId = locationTreeDefId;
	}

    @Column(name = "Name", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

    @Lob
    @Column(name="Remarks", unique=false, nullable=true, updatable=true, insertable=true)
	public String getRemarks()
	{
		return this.remarks;
	}

	public void setRemarks(String remarks)
	{
		this.remarks = remarks;
	}

    @Column(name = "FullNameDirection", unique = false, nullable = true, insertable = true, updatable = true)
	public Integer getFullNameDirection()
    {
        return fullNameDirection;
    }

    public void setFullNameDirection(Integer fullNameDirection)
    {
        this.fullNameDirection = fullNameDirection;
    }

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "locationTreeDef")
    public Set<CollectionType> getCollectionTypes()
	{
		return this.collectionTypes;
	}

	public void setCollectionTypes(Set<CollectionType> collObjDefs)
	{
		this.collectionTypes = collObjDefs;
	}

    public void addCollObjDef( CollectionType ct )
    {
        this.collectionTypes.add(ct);
        ct.setLocationTreeDef(this);
    }
    
    public void removeCollObjDef( CollectionType ct )
    {
        this.collectionTypes.remove(ct);
        ct.setLocationTreeDef(null);
    }

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "definition")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.LOCK })
	public Set<Location> getTreeEntries()
	{
		return this.treeEntries;
	}

	public void setTreeEntries(Set<Location> treeEntries)
	{
		this.treeEntries = treeEntries;
	}

    @OneToMany(cascade = {}, fetch = FetchType.EAGER, mappedBy = "treeDef")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.LOCK })
	public Set<LocationTreeDefItem> getTreeDefItems()
	{
		return this.treeDefItems;
	}

	public void setTreeDefItems(Set<LocationTreeDefItem> treeDefItems)
	{
		this.treeDefItems = treeDefItems;
	}

    @Transient
	public Long getTreeDefId()
	{
		return getLocationTreeDefId();
	}

	public void setTreeDefId(Long id)
	{
		setLocationTreeDefId(id);
	}

	public void addTreeEntry(Location loc)
	{
		treeEntries.add(loc);
        loc.setDefinition(this);
	}

	public void removeTreeEntry(Location loc)
	{
		treeEntries.remove(loc);
        loc.setDefinition(null);
	}

	public void addTreeDefItem(LocationTreeDefItem item)
	{
		treeDefItems.add(item);
		item.setTreeDef(this);
	}

	public void removeTreeDefItem(LocationTreeDefItem item)
	{
		treeDefItems.remove(item);
		item.setTreeDef(null);
	}

    @Transient
	public Class<Location> getNodeClass()
	{
		return Location.class;
	}
	
	/**
	 * Returns the <code>TreeDefinitionItemIface</code> object associated with the called
	 * <code>TreeDefinitionIface</code> object and having the given rank.
	 * 
	 * @param rank the rank of the returned def item
	 * @return the definition item
	 */
    public LocationTreeDefItem getDefItemByRank(Integer rank)
	{
		for( LocationTreeDefItem item: treeDefItems )
		{
			if( item.getRankId().equals(rank) )
			{
				return item;
			}
		}
		return null;
	}
	
//	/**
//	 * Returns the <code>TreeDefinitionItemIface</code> object associated with the called
//	 * <code>TreeDefinitionIface</code> object and having the given name.
//	 * 
//	 * @param name the name of the returned def item
//	 * @return the definition item
//	 */
//	public LocationTreeDefItem getDefItemByName(String defItemName)
//	{
//		for( LocationTreeDefItem item: treeDefItems )
//		{
//			if( item.getName().equals(defItemName) )
//			{
//				return item;
//			}
//		}
//		return null;
//	}
	
	/**
	 * Determines if the child node can be reparented to newParent while not
	 * violating any of the business rules.  Currently, the only rule on
	 * reparenting is that the new parent must be of rank equal to or less than
	 * the next higher enforced rank in the child's tree definition.
	 * 
	 * @param child the node to be reparented
	 * @param newParent the prospective new parent node
	 * 
	 * @return <code>true</code> if the action will not violate any reparenting rules, false otherwise
	 */
	public boolean canChildBeReparentedToNode( Location child, Location newParent )
	{
		if( newParent.getRankId().intValue() >= child.getRankId().intValue() )
		{
			// a node cannot have a parent that is a peer or of lower rank (larger rank id)
			return false;
		}
		
		Integer nextEnforcedRank = getRankOfNextHighestEnforcedLevel(child);
		if( nextEnforcedRank == null )
		{
			// no higher ranks are being enforced
			// the node can be reparented all the way up to the root
			return true;
		}
		
		if( nextEnforcedRank.intValue() <= newParent.getRankId().intValue() )
		{
			// the next enforced rank is equal to or above the new parent rank
			return true;
		}
		
		return false;
	}
	
	/**
	 * Returns the next highest rank in the tree that is enforced by the
	 * tree definition.
	 * 
	 * @param node the node to find the next highest enforced rank for
	 * @return the next highest rank
	 */
	public Integer getRankOfNextHighestEnforcedLevel( Location node )
	{
		LocationTreeDefItem defItem = node.getDefinitionItem();
		while( defItem.getParent() != null )
		{
			defItem = defItem.getParent();
			if( defItem.getIsEnforced() != null && defItem.getIsEnforced().booleanValue() == true )
			{
				return defItem.getRankId();
			}
		}
		
		return null;
	}
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public int getTableId()
    {
        return getClassTableId();
    }
    
    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 59;
    }

    @Override
    public String toString()
    {
        return getIdentityTitle();
    }
}
