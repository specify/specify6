/* Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
    public static final int PARVORDER     = 125; 		    
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
    protected List<TreeDefItemStandardEntry> stdLevels = null;
    
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
    	if (stdLevels == null)
    	{
    		stdLevels = getStandardLevelsStatic();
    	}
    	return new LinkedList<TreeDefItemStandardEntry>(stdLevels);
    }
    
    /**
     * @return list of standard taxon ranks.
     */
    @Transient
    public static List<TreeDefItemStandardEntry> getStandardLevelsStatic()
	{
		LinkedList<TreeDefItemStandardEntry> result = new LinkedList<TreeDefItemStandardEntry>();

		result.add(new TreeDefItemStandardEntry(getLevelName(TAXONOMY_ROOT),
				TAXONOMY_ROOT));
		result.add(new TreeDefItemStandardEntry(getLevelName(KINGDOM),
				KINGDOM));
		result.add(new TreeDefItemStandardEntry(getLevelName(SUBKINGDOM),
				SUBKINGDOM));
		result.add(new TreeDefItemStandardEntry(getLevelName(DIVISION),
				DIVISION));
		result.add(new TreeDefItemStandardEntry(getLevelName(PHYLUM + 1),
				PHYLUM));
		result.add(new TreeDefItemStandardEntry(getLevelName(SUBDIVISION),
				SUBDIVISION));
		result.add(new TreeDefItemStandardEntry(getLevelName(SUBPHYLUM + 1),
				SUBPHYLUM));
		result.add(new TreeDefItemStandardEntry(getLevelName(SUPERCLASS),
				SUPERCLASS));
		result.add(new TreeDefItemStandardEntry(getLevelName(CLASS), CLASS));
		result.add(new TreeDefItemStandardEntry(getLevelName(SUBCLASS),
				SUBCLASS));
		result.add(new TreeDefItemStandardEntry(getLevelName(INFRACLASS),
				INFRACLASS));
		result.add(new TreeDefItemStandardEntry(getLevelName(SUPERORDER),
				SUPERORDER));
		result.add(new TreeDefItemStandardEntry(getLevelName(ORDER), ORDER));
		result.add(new TreeDefItemStandardEntry(getLevelName(SUBORDER),
				SUBORDER));
		result.add(new TreeDefItemStandardEntry(getLevelName(INFRAORDER),
				INFRAORDER));
		result.add(new TreeDefItemStandardEntry(getLevelName(PARVORDER),
				PARVORDER));
		result.add(new TreeDefItemStandardEntry(getLevelName(SUPERFAMILY),
				SUPERFAMILY));
		result
				.add(new TreeDefItemStandardEntry(getLevelName(FAMILY), FAMILY));
		result.add(new TreeDefItemStandardEntry(getLevelName(SUBFAMILY),
				SUBFAMILY));
		result.add(new TreeDefItemStandardEntry(getLevelName(TRIBE), TRIBE));
		result.add(new TreeDefItemStandardEntry(getLevelName(SUBTRIBE),
				SUBTRIBE));
		result.add(new TreeDefItemStandardEntry(getLevelName(GENUS), GENUS));
		result.add(new TreeDefItemStandardEntry(getLevelName(SUBGENUS),
				SUBGENUS));
		result.add(new TreeDefItemStandardEntry(getLevelName(SECTION),
				SECTION));
		result.add(new TreeDefItemStandardEntry(getLevelName(SUBSECTION),
				SUBSECTION));
		result.add(new TreeDefItemStandardEntry(getLevelName(SPECIES),
				SPECIES));
		result.add(new TreeDefItemStandardEntry(getLevelName(SUBSPECIES),
				SUBSPECIES));
		result.add(new TreeDefItemStandardEntry(getLevelName(VARIETY),
				VARIETY));
		result.add(new TreeDefItemStandardEntry(getLevelName(SUBVARIETY),
				SUBVARIETY));
		result.add(new TreeDefItemStandardEntry(getLevelName(FORMA), FORMA));
		result.add(new TreeDefItemStandardEntry(getLevelName(SUBFORMA),
				SUBFORMA));

		return result;
	}
    
    /* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.BaseTreeDef#isRequiredLevel(int)
	 */
	@Override
	@Transient
    public boolean isRequiredLevel(int levelRank)
    {
        return isStdRequiredLevel(levelRank);
    }

    /**
     * @param levelRank
     * @return
     */
    public static boolean isStdRequiredLevel(int levelRank)
    {
        return levelRank == KINGDOM
            || levelRank == DIVISION
            || levelRank == PHYLUM
            || levelRank == CLASS
            || levelRank == ORDER
            || levelRank == FAMILY
            || levelRank == GENUS
            || levelRank == SPECIES;
    }

	/**
     * @param levelRank
     * @return localized text corresponding to levelRank.
     */
    @Transient
    protected static String getLevelName(final int levelRank)
    {
        switch (levelRank)
        {
            case TAXONOMY_ROOT: return "Root"; //$NON-NLS-1$
            case KINGDOM: return "Kingdom"; //$NON-NLS-1$
            case SUBKINGDOM: return "Subkingdom";  //$NON-NLS-1$
            case DIVISION: return "Division";  //$NON-NLS-1$
            case PHYLUM+1: return "Phylum";  //$NON-NLS-1$
            case SUBDIVISION: return "Subdivision";  //$NON-NLS-1$
            case SUBPHYLUM+1: return "Subphylum";  //$NON-NLS-1$
            case SUPERCLASS: return "Superclass";  //$NON-NLS-1$
            case CLASS: return "Class";  //$NON-NLS-1$
            case SUBCLASS: return "Subclass";  //$NON-NLS-1$
            case INFRACLASS: return "Infraclass";  //$NON-NLS-1$
            case SUPERORDER: return "Superorder";  //$NON-NLS-1$
            case ORDER: return "Order";  //$NON-NLS-1$
            case SUBORDER: return "Suborder";  //$NON-NLS-1$
            case INFRAORDER: return "Infraorder";  //$NON-NLS-1$
            case PARVORDER: return "Parvorder"; //$NON-NLS-1$		
            case SUPERFAMILY: return "Superfamily";  //$NON-NLS-1$
            case FAMILY: return "Family";  //$NON-NLS-1$
            case SUBFAMILY: return "Subfamily";  //$NON-NLS-1$
            case TRIBE: return "Tribe";  //$NON-NLS-1$
            case SUBTRIBE: return "Subtribe";  //$NON-NLS-1$
            case GENUS: return "Genus";  //$NON-NLS-1$
            case SUBGENUS: return "Subgenus";  //$NON-NLS-1$
            case SECTION: return "Section";  //$NON-NLS-1$
            case SUBSECTION: return "Subsection";  //$NON-NLS-1$
            case SPECIES: return "Species";  //$NON-NLS-1$
            case SUBSPECIES: return "Subspecies";  //$NON-NLS-1$
            case VARIETY: return "Variety";  //$NON-NLS-1$
            case SUBVARIETY: return "Subvariety";  //$NON-NLS-1$
            case FORMA: return "Forma";  //$NON-NLS-1$
            case SUBFORMA: return "Subforma";  //$NON-NLS-1$
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
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Discipline.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        if (discipline != null)
        {
            discipline.getId();
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
        return TaxonTreeDef.SPECIES;
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
