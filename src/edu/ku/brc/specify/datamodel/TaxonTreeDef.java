/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.datamodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@Table(name="taxontreedef")
public class TaxonTreeDef extends DataModelObjBase implements java.io.Serializable, TreeDefIface<Taxon, TaxonTreeDef, TaxonTreeDefItem>
{
	protected Long				    taxonTreeDefId;
	protected String				name;
	protected String				remarks;
    protected Integer               fullNameDirection;
	protected CollectionObjDef		collObjDef;
	protected Set<Taxon>			treeEntries;
	protected Set<TaxonTreeDefItem>	treeDefItems;

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

    @Override
    public void initialize()
	{
        super.init();
		taxonTreeDefId = null;
		name = null;
		remarks = null;
        fullNameDirection = null;
		collObjDef = null;
		treeEntries = new HashSet<Taxon>();
		treeDefItems = new HashSet<TaxonTreeDefItem>();
	}

    @Id
    @GeneratedValue
    @Column(name="TaxonTreeDefID", unique=false, nullable=false, insertable=true, updatable=true)
    public Long getTaxonTreeDefId()
	{
		return this.taxonTreeDefId;
	}

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Long getId()
    {
        return this.taxonTreeDefId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return TaxonTreeDef.class;
    }

	public void setTaxonTreeDefId(Long taxonTreeDefId)
	{
		this.taxonTreeDefId = taxonTreeDefId;
	}

    @Column(name="Name", unique=false, nullable=true, insertable=true, updatable=true, length=64)
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

    @Column(name="Remarks", length=65535, unique=false, nullable=true, insertable=true, updatable=true)
	public String getRemarks()
	{
		return this.remarks;
	}

	public void setRemarks(String remarks)
	{
		this.remarks = remarks;
	}

    @Column(name="FullNameDirection", unique=false, nullable=true, insertable=true, updatable=true)
	public Integer getFullNameDirection()
    {
        return fullNameDirection;
    }

    public void setFullNameDirection(Integer fullNameDirection)
    {
        this.fullNameDirection = fullNameDirection;
    }

    @OneToOne(mappedBy="taxonTreeDef")
    public CollectionObjDef getCollObjDef()
	{
		return this.collObjDef;
	}

	public void setCollObjDef(CollectionObjDef collObjDef)
	{
		this.collObjDef = collObjDef;
	}

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="definition")
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.LOCK} )
	public Set<Taxon> getTreeEntries()
	{
		return this.treeEntries;
	}

	public void setTreeEntries(Set<Taxon> treeEntries)
	{
		this.treeEntries = treeEntries;
	}

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="treeDef")
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.LOCK} )
	public Set<TaxonTreeDefItem> getTreeDefItems()
	{
		return this.treeDefItems;
	}

	public void setTreeDefItems(Set<TaxonTreeDefItem> treeDefItems)
	{
		this.treeDefItems = treeDefItems;
	}

    @Transient
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

    @Transient
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public Integer getTableId()
    {
        return 76;
    }

}
