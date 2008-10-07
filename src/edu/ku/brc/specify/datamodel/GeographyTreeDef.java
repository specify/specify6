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
import java.util.LinkedList;
import java.util.List;
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

import edu.ku.brc.ui.UIRegistry;

@SuppressWarnings("serial") //$NON-NLS-1$
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "geographytreedef") //$NON-NLS-1$
public class GeographyTreeDef extends BaseTreeDef<Geography, GeographyTreeDef, GeographyTreeDefItem> implements java.io.Serializable
{
	protected Integer				    geographyTreeDefId;
	protected String				    name;
	protected String				    remarks;
    protected Integer                   fullNameDirection;
	protected Set<Discipline>		    disciplines;
	protected Set<Geography>			treeEntries;
	protected Set<GeographyTreeDefItem>	treeDefItems;

	/** default constructor */
	public GeographyTreeDef()
	{
		// do nothing
	}

	/** constructor with id */
	public GeographyTreeDef(Integer geographyTreeDefId)
	{
		this.geographyTreeDefId = geographyTreeDefId;
	}

	@Override
    public void initialize()
	{
        super.init();
		geographyTreeDefId = null;
		name = null;
		remarks = null;
        fullNameDirection = null;
		disciplines = new HashSet<Discipline>();
		treeEntries = new HashSet<Geography>();
		treeDefItems = new HashSet<GeographyTreeDefItem>();
	}

    @Id
    @GeneratedValue
    @Column(name = "GeographyTreeDefID", unique = false, nullable = false, insertable = true, updatable = true) //$NON-NLS-1$
	public Integer getGeographyTreeDefId()
	{
		return this.geographyTreeDefId;
	}

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.geographyTreeDefId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return GeographyTreeDef.class;
    }

	public void setGeographyTreeDefId(Integer geographyTreeDefId)
	{
		this.geographyTreeDefId = geographyTreeDefId;
	}

    @Column(name = "Name", nullable=false, length = 64) //$NON-NLS-1$
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

    @Lob
    @Column(name = "Remarks", length = 4096) //$NON-NLS-1$
	public String getRemarks()
	{
		return this.remarks;
	}

	public void setRemarks(String remarks)
	{
		this.remarks = remarks;
	}

    @Column(name = "FullNameDirection", unique = false, nullable = true, insertable = true, updatable = true) //$NON-NLS-1$
	public Integer getFullNameDirection()
    {
        return fullNameDirection;
    }

    public void setFullNameDirection(Integer fullNameDirection)
    {
        this.fullNameDirection = fullNameDirection;
    }

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "geographyTreeDef") //$NON-NLS-1$
    public Set<Discipline> getDisciplines()
	{
		return this.disciplines;
	}

	public void setDisciplines(Set<Discipline> disciplines)
	{
		this.disciplines = disciplines;
	}
    
    public void addDiscipline( Discipline ct )
    {
        this.disciplines.add(ct);
        ct.setGeographyTreeDef(this);
    }
    
    public void removeDiscipline( Discipline ct )
    {
        this.disciplines.remove(ct);
        ct.setGeographyTreeDef(null);
    }

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "definition") //$NON-NLS-1$
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
	public Set<Geography> getTreeEntries()
	{
		return this.treeEntries;
	}

	public void setTreeEntries(Set<Geography> treeEntries)
	{
		this.treeEntries = treeEntries;
	}

    @OneToMany(cascade = {}, fetch = FetchType.EAGER, mappedBy = "treeDef") //$NON-NLS-1$
    @Cascade( { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
	public Set<GeographyTreeDefItem> getTreeDefItems()
	{
		return this.treeDefItems;
	}

	public void setTreeDefItems(Set<GeographyTreeDefItem> treeDefItems)
	{
		this.treeDefItems = treeDefItems;
	}

    @Transient
	public Integer getTreeDefId()
	{
		return getGeographyTreeDefId();
	}

	public void setTreeDefId(Integer id)
	{
		setGeographyTreeDefId(id);
	}

	public void addTreeEntry(Geography taxon)
	{
		treeEntries.add(taxon);
		taxon.setDefinition(this);
	}

	public void removeTreeEntry(Geography taxon)
	{
		treeEntries.remove(taxon);
		taxon.setDefinition(null);
	}

	public void addTreeDefItem(GeographyTreeDefItem item)
	{
		treeDefItems.add(item);
		item.setTreeDef(this);
	}

	public void removeTreeDefItem(GeographyTreeDefItem item)
	{
		treeDefItems.remove(item);
		item.setTreeDef(null);
	}

    @Transient
	public Class<Geography> getNodeClass()
	{
		return Geography.class;
	}
	
	/**
	 * Returns the <code>TreeDefinitionItemIface</code> object associated with the called
	 * <code>TreeDefinitionIface</code> object and having the given rank.
	 * 
	 * @param rank the rank of the returned def item
	 * @return the definition item
	 */
    public GeographyTreeDefItem getDefItemByRank(Integer rank)
	{
		for( GeographyTreeDefItem item: treeDefItems )
		{
			if( item.getRankId().equals(rank) )
			{
				return item;
			}
		}
		return null;
	}
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#getSynonymizedLevel()
     */
    @Transient
    public int getSynonymizedLevel()
    {
        // can't be "-1" which means do nothing, 
        // and it has to at least be below the root
        return 1; 
    }
    
//	/**
//	 * Returns the <code>TreeDefinitionItemIface</code> object associated with the called
//	 * <code>TreeDefinitionIface</code> object and having the given name.
//	 * 
//	 * @param name the name of the returned def item
//	 * @return the definition item
//	 */
//	public GeographyTreeDefItem getDefItemByName(String defItemName)
//	{
//		for( GeographyTreeDefItem item: treeDefItems )
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
	public boolean canChildBeReparentedToNode( Geography child, Geography newParent )
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
	public Integer getRankOfNextHighestEnforcedLevel( Geography node )
	{
		GeographyTreeDefItem defItem = node.getDefinitionItem();
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
        return 44;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#toString()
     */
    @Override
    public String toString()
    {
        return getIdentityTitle();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.BaseTreeDef#getRankIncrement()
     */
    @Override
    @Transient
    public int getRankIncrement()
    {
        return 100;  //tradition
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.BaseTreeDef#getStandardLevels()
     */
    @Override
    @Transient
    public List<TreeDefItemStandardEntry> getStandardLevels()
    {
        List<TreeDefItemStandardEntry> result = new LinkedList<TreeDefItemStandardEntry>();    
        result.add(new TreeDefItemStandardEntry(UIRegistry.getResourceString("GeographyTreeDef.9"), 100)); //$NON-NLS-1$
        result.add(new TreeDefItemStandardEntry(UIRegistry.getResourceString("GeographyTreeDef.10"), 200)); //$NON-NLS-1$
        result.add(new TreeDefItemStandardEntry(UIRegistry.getResourceString("GeographyTreeDef.11"), 300)); //$NON-NLS-1$
        result.add(new TreeDefItemStandardEntry(UIRegistry.getResourceString("GeographyTreeDef.12"), 400)); //$NON-NLS-1$
        return result;
    }
    
    
}
