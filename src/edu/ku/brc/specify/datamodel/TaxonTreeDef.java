/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import edu.ku.brc.ui.UIRegistry;

@SuppressWarnings("serial") //$NON-NLS-1$
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name="taxontreedef") //$NON-NLS-1$
public class TaxonTreeDef extends BaseTreeDef<Taxon, TaxonTreeDef, TaxonTreeDefItem> implements java.io.Serializable
{
	//Standard levels. If these are changed then the getStandardLevels() and getLocalizedLevelName() methods will need to be updated.
    public static final int TAXONOMY_ROOT = 0;
    public static final int KINGDOM       = 10;
    public static final int SUBKINGDOM    = 20;
    public static final int DIVISION      = 30;
    public static final int PHYLUM        = 30;
    public static final int SUBDIVISION   = 40;
    public static final int SUBPHYLUM     = 40;
    public static final int SUPERCLASS    = 50;
    public static final int CLASS         = 60;
    public static final int SUBCLASS      = 70;
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
    // end standard levels


    protected Integer               taxonTreeDefId;
    protected String                name;
    protected String                remarks;
    protected Integer               fullNameDirection;
    protected Discipline            discipline;
    protected Set<Taxon>            treeEntries;
    protected Set<TaxonTreeDefItem> treeDefItems;
    
    /** default constructor */
    public TaxonTreeDef()
    {
        // do nothing
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.BaseTreeDef#getStandardLevels()
     */
    @Override
    @Transient
    public List<TreeDefItemStandardEntry> getStandardLevels()
    {
        List<TreeDefItemStandardEntry> result = new LinkedList<TreeDefItemStandardEntry>();
       
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(TAXONOMY_ROOT), TAXONOMY_ROOT));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(KINGDOM), KINGDOM));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(SUBKINGDOM), SUBKINGDOM));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(DIVISION), DIVISION));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(PHYLUM+1), PHYLUM));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(SUBDIVISION), SUBDIVISION));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(SUBPHYLUM+1), SUBPHYLUM));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(SUPERCLASS), SUPERCLASS));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(CLASS), CLASS));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(SUBCLASS), SUBCLASS));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(INFRACLASS), INFRACLASS));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(SUPERORDER), SUPERORDER));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(ORDER), ORDER));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(SUBORDER), SUBORDER));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(INFRAORDER), INFRAORDER));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(SUPERFAMILY), SUPERFAMILY));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(FAMILY), FAMILY));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(SUBFAMILY), SUBFAMILY));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(TRIBE), TRIBE));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(SUBTRIBE), SUBTRIBE));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(GENUS), GENUS));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(SUBGENUS), SUBGENUS));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(SECTION), SECTION));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(SUBSECTION), SUBSECTION));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(SPECIES), SPECIES));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(SUBSPECIES), SUBSPECIES));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(VARIETY), VARIETY));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(SUBVARIETY), SUBVARIETY));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(FORMA), FORMA));
        result.add(new TreeDefItemStandardEntry(getLocalizedLevelName(SUBFORMA), SUBFORMA));
        
        return result;
    }

    /**
     * @param levelRank
     * @return localized text corresponding to levelRank.
     */
    @Transient
    protected String getLocalizedLevelName(final int levelRank)
    {
        switch (levelRank)
        {
            case TAXONOMY_ROOT: return UIRegistry.getResourceString("TaxonTreeDef.2"); //$NON-NLS-1$
            case KINGDOM: return UIRegistry.getResourceString("TaxonTreeDef.3"); //$NON-NLS-1$
            case SUBKINGDOM: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.0"));  //$NON-NLS-1$
            case DIVISION: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.1"));  //$NON-NLS-1$
            case PHYLUM+1: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.28"));  //$NON-NLS-1$
            case SUBDIVISION: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.29"));  //$NON-NLS-1$
            case SUBPHYLUM+1: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.4"));  //$NON-NLS-1$
            case SUPERCLASS: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.5"));  //$NON-NLS-1$
            case CLASS: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.6"));  //$NON-NLS-1$
            case SUBCLASS: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.7"));  //$NON-NLS-1$
            case INFRACLASS: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.8"));  //$NON-NLS-1$
            case SUPERORDER: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.9"));  //$NON-NLS-1$
            case ORDER: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.10"));  //$NON-NLS-1$
            case SUBORDER: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.11"));  //$NON-NLS-1$
            case INFRAORDER: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.12"));  //$NON-NLS-1$
            case SUPERFAMILY: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.13"));  //$NON-NLS-1$
            case FAMILY: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.14"));  //$NON-NLS-1$
            case SUBFAMILY: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.15"));  //$NON-NLS-1$
            case TRIBE: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.16"));  //$NON-NLS-1$
            case SUBTRIBE: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.17"));  //$NON-NLS-1$
            case GENUS: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.18"));  //$NON-NLS-1$
            case SUBGENUS: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.19"));  //$NON-NLS-1$
            case SECTION: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.20"));  //$NON-NLS-1$
            case SUBSECTION: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.21"));  //$NON-NLS-1$
            case SPECIES: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.22"));  //$NON-NLS-1$
            case SUBSPECIES: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.23"));  //$NON-NLS-1$
            case VARIETY: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.24"));  //$NON-NLS-1$
            case SUBVARIETY: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.25"));  //$NON-NLS-1$
            case FORMA: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.26"));  //$NON-NLS-1$
            case SUBFORMA: return UIRegistry.getResourceString(UIRegistry.getResourceString("TaxonTreeDef.27"));  //$NON-NLS-1$
        }
        return null;
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.BaseTreeDef#getRankIncrement()
     */
    @Override
    @Transient
    public int getRankIncrement()
    {
        return 10;
    }

	/** constructor with id */
	public TaxonTreeDef(Integer taxonTreeDefId)
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
        discipline = null;
		treeEntries = new HashSet<Taxon>();
		treeDefItems = new HashSet<TaxonTreeDefItem>();
	}

    @Id
    @GeneratedValue
    @Column(name="TaxonTreeDefID", unique=false, nullable=false, insertable=true, updatable=true) //$NON-NLS-1$
    public Integer getTaxonTreeDefId()
	{
		return this.taxonTreeDefId;
	}

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
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

	public void setTaxonTreeDefId(Integer taxonTreeDefId)
	{
		this.taxonTreeDefId = taxonTreeDefId;
	}

    @Column(name="Name", nullable=false, length=64) //$NON-NLS-1$
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

    @Column(name="Remarks", length=255, unique=false, nullable=true, insertable=true, updatable=true) //$NON-NLS-1$
	public String getRemarks()
	{
		return this.remarks;
	}

	public void setRemarks(String remarks)
	{
		this.remarks = remarks;
	}

    @Column(name="FullNameDirection", unique=false, nullable=true, insertable=true, updatable=true) //$NON-NLS-1$
	public Integer getFullNameDirection()
    {
        return fullNameDirection;
    }

    public void setFullNameDirection(Integer fullNameDirection)
    {
        this.fullNameDirection = fullNameDirection;
    }

    @OneToOne(mappedBy="taxonTreeDef", fetch=FetchType.EAGER) //$NON-NLS-1$
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
    public Discipline getDiscipline()
	{
		return this.discipline;
	}

	public void setDiscipline(Discipline discipline)
	{
		this.discipline = discipline;
	}

    @OneToMany(cascade={}, fetch=FetchType.LAZY, mappedBy="definition") //$NON-NLS-1$
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
	public Set<Taxon> getTreeEntries()
	{
		return this.treeEntries;
	}

	public void setTreeEntries(Set<Taxon> treeEntries)
	{
		this.treeEntries = treeEntries;
	}

    @OneToMany(cascade={}, fetch=FetchType.EAGER, mappedBy="treeDef") //$NON-NLS-1$
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
	public Integer getTreeDefId()
	{
		return getTaxonTreeDefId();
	}

	public void setTreeDefId(Integer id)
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.TreeDefIface#getSynonymizedLevel()
     */
    @Transient
    public int getSynonymizedLevel()
    {
        return TaxonTreeDef.GENUS;
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.BaseTreeDef#isSynonymySupported()
	 */
	@Override
	@Transient
	public boolean isSynonymySupported() 
	{
		return true;
	}    
    
    
}
