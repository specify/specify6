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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@SuppressWarnings("serial")
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name="taxontreedef")
public class TaxonTreeDef extends DataModelObjBase implements java.io.Serializable, TreeDefIface<Taxon, TaxonTreeDef, TaxonTreeDefItem>
{
	public static final int TAXONOMY_ROOT = 0;
    public static final int KINGDOM       = 10;
    public static final int SUBKINGDOM    = 20;
    public static final int DIVISION      = 30;
    public static final int PHYLUM        = 30;
    public static final int SUBDIVISION   = 40;
    public static final int SUBPHYLUM     = 40;
    public static final int SUPERCLASS    = 50;
    public static final int CLASS         = 60;
    public static final int SUBLCASS      = 70;
    public static final int INFRACLASS    = 80;
    public static final int SUPERORDER    = 90;
    public static final int ORDER         = 100;
    public static final int SUBORDER      = 110;
    public static final int INFRAORDER    = 120;
    public static final int SUPERFAMILY   = 130;
    public static final int FAMILY        = 140;
    public static final int SUBFAMILY     = 150;
    public static final int TRIBE         = 160;
    public static final int SUBTRIBE      = 170;
    public static final int GENUS         = 180;
    public static final int SUBGENUS      = 190;
    public static final int SECTION       = 200;
    public static final int SUBSECTION    = 210;
    public static final int SPECIES       = 220;
    public static final int SUBSPECIES    = 230;
    public static final int VARIETY       = 240;
    public static final int SUBVARIETY    = 250;
    public static final int FORMA         = 260;
    public static final int SUBFORMA      = 270;

    protected Long                  taxonTreeDefId;
    protected String                name;
    protected String                remarks;
    protected Integer               fullNameDirection;
    protected CollectionType        collObjDef;
    protected Set<Taxon>            treeEntries;
    protected Set<TaxonTreeDefItem> treeDefItems;
    
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

    @Column(name="Remarks", length=255, unique=false, nullable=true, insertable=true, updatable=true)
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

    @OneToOne(mappedBy="taxonTreeDef", fetch=FetchType.EAGER)
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
    public CollectionType getCollObjDef()
	{
		return this.collObjDef;
	}

	public void setCollObjDef(CollectionType collObjDef)
	{
		this.collObjDef = collObjDef;
	}

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="definition")
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
	public Set<Taxon> getTreeEntries()
	{
		return this.treeEntries;
	}

	public void setTreeEntries(Set<Taxon> treeEntries)
	{
		this.treeEntries = treeEntries;
	}

    @OneToMany(cascade={}, fetch=FetchType.EAGER, mappedBy="treeDef")
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
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
        return 76;
    }

    @Override
    public String toString()
    {
        return getIdentityTitle();
    }
}
