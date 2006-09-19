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
public class TaxonTreeDef implements java.io.Serializable, TreeDefIface<Taxon, TaxonTreeDef, TaxonTreeDefItem>
{
	protected Long				    taxonTreeDefId;
	protected String				name;
	protected String				remarks;
	protected CollectionObjDef		collObjDef;
	protected Set<Taxon>			treeEntries;
	protected Set<TaxonTreeDefItem>	treeDefItems;

	// Constructors

	/** default constructor */
	public TaxonTreeDef()
	{
		// do nothing
	}

	/** constructor with id */
	public TaxonTreeDef(Long taxonTreeDefId)
	{
		this.taxonTreeDefId = taxonTreeDefId;
	}

	// Initializer
	public void initialize()
	{
		taxonTreeDefId = null;
		name = null;
		remarks = null;
		collObjDef = null;
		treeEntries = new HashSet<Taxon>();
		treeDefItems = new HashSet<TaxonTreeDefItem>();
	}

	// End Initializer

	// Property accessors

	/**
	 * 
	 */
	public Long getTaxonTreeDefId()
	{
		return this.taxonTreeDefId;
	}

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.taxonTreeDefId;
    }

	public void setTaxonTreeDefId(Long taxonTreeDefId)
	{
		this.taxonTreeDefId = taxonTreeDefId;
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
	public CollectionObjDef getCollObjDef()
	{
		return this.collObjDef;
	}

	public void setCollObjDef(CollectionObjDef collObjDef)
	{
		this.collObjDef = collObjDef;
	}

	/**
	 * 
	 */
	public Set<Taxon> getTreeEntries()
	{
		return this.treeEntries;
	}

	public void setTreeEntries(Set<Taxon> treeEntries)
	{
		this.treeEntries = treeEntries;
	}

	/**
	 * 
	 */
	public Set<TaxonTreeDefItem> getTreeDefItems()
	{
		return this.treeDefItems;
	}

	public void setTreeDefItems(Set<TaxonTreeDefItem> treeDefItems)
	{
		this.treeDefItems = treeDefItems;
	}

	//
	// Methods added to implement TreeDefinitionIface
	//

	public Long getTreeDefId()
	{
		return getTaxonTreeDefId();
	}

	public void setTreeDefId(Long id)
	{
		setTaxonTreeDefId(id);
	}

	public void addTreeEntry(Taxon taxon)
	{
		treeEntries.add(taxon);
		taxon.setDefinition(this);
	}

	public void removeTreeEntry(Taxon taxon)
	{
		treeEntries.remove(taxon);
		taxon.setDefinition(null);
	}

	public void addTreeDefItem(TaxonTreeDefItem item)
	{
		treeDefItems.add(item);
		item.setTreeDef(this);
	}

	public void removeTreeDefItem(TaxonTreeDefItem item)
	{
		treeDefItems.remove(item);
		item.setTreeDef(null);
	}

	// methods to "complete" the implementation of TreeDefinitionIface
	public Class<Taxon> getNodeClass()
	{
		return Taxon.class;
	}
	
	/**
	 * Returns the <code>TreeDefinitionItemIface</code> object associated with the called
	 * <code>TreeDefinitionIface</code> object and having the given rank.
	 * 
	 * @param rank the rank of the returned def item
	 * @return the definition item
	 */
    public TaxonTreeDefItem getDefItemByRank(Integer rank)
	{
		for( TaxonTreeDefItem item: treeDefItems )
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
	public TaxonTreeDefItem getDefItemByName(String defItemName)
	{
		for( TaxonTreeDefItem item: treeDefItems )
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
	public boolean canChildBeReparentedToNode( Taxon child, Taxon newParent )
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
	public Integer getRankOfNextHighestEnforcedLevel( Taxon node )
	{
		TaxonTreeDefItem defItem = node.getDefinitionItem();
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
