/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class LocationTreeDef implements java.io.Serializable, TreeDefIface<Location, LocationTreeDef, LocationTreeDefItem>
{
	protected Long				   locationTreeDefId;
	protected String				name;
	protected String				remarks;
	protected Set<CollectionObjDef>		collObjDefs;
	protected Set<Location>			treeEntries;
	protected Set<LocationTreeDefItem>	treeDefItems;

	// Constructors

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

	// Initializer
	public void initialize()
	{
		locationTreeDefId = null;
		name = null;
		remarks = null;
		collObjDefs = new HashSet<CollectionObjDef>();
		treeEntries = new HashSet<Location>();
		treeDefItems = new HashSet<LocationTreeDefItem>();
	}

	// End Initializer

	// Property accessors

	/**
	 * 
	 */
	public Long getLocationTreeDefId()
	{
		return this.locationTreeDefId;
	}

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.locationTreeDefId;
    }

	public void setLocationTreeDefId(Long locationTreeDefId)
	{
		this.locationTreeDefId = locationTreeDefId;
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
	public Set<CollectionObjDef> getCollObjDefs()
	{
		return this.collObjDefs;
	}

	public void setCollObjDefs(Set<CollectionObjDef> collObjDefs)
	{
		this.collObjDefs = collObjDefs;
	}

	/**
	 * 
	 */
	public Set<Location> getTreeEntries()
	{
		return this.treeEntries;
	}

	public void setTreeEntries(Set<Location> treeEntries)
	{
		this.treeEntries = treeEntries;
	}

	/**
	 * 
	 */
	public Set<LocationTreeDefItem> getTreeDefItems()
	{
		return this.treeDefItems;
	}

	public void setTreeDefItems(Set<LocationTreeDefItem> treeDefItems)
	{
		this.treeDefItems = treeDefItems;
	}

	//
	// Methods added to implement TreeDefinitionIface
	//

	public Long getTreeDefId()
	{
		return getLocationTreeDefId();
	}

	public void setTreeDefId(Long id)
	{
		setLocationTreeDefId(id);
	}

	public void addTreeEntry(Location taxon)
	{
		treeEntries.add(taxon);
		taxon.setDefinition(this);
	}

	public void removeTreeEntry(Location taxon)
	{
		treeEntries.remove(taxon);
		taxon.setDefinition(null);
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

	// methods to "complete" the implementation of TreeDefinitionIface
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
	
	/**
	 * Returns the <code>TreeDefinitionItemIface</code> object associated with the called
	 * <code>TreeDefinitionIface</code> object and having the given name.
	 * 
	 * @param name the name of the returned def item
	 * @return the definition item
	 */
	public LocationTreeDefItem getDefItemByName(String defItemName)
	{
		for( LocationTreeDefItem item: treeDefItems )
		{
			if( item.getName().equals(defItemName) )
			{
				return item;
			}
		}
		return null;
	}
	
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
}
