/* Copyright (C) 2009, University of Kansas Center for Research
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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import edu.ku.brc.specify.conversion.BasicSQLUtils;

@SuppressWarnings("serial")
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "taxontreedefitem")
public class TaxonTreeDefItem extends DataModelObjBase implements Serializable, 
                                                                  TreeDefItemIface<Taxon,TaxonTreeDef,TaxonTreeDefItem>,
                                                                  Comparable<TaxonTreeDefItem>
{
	protected Integer				taxonTreeDefItemId;
	protected String				name;
	protected String				remarks;
	protected Integer				rankId;
	protected Boolean				isEnforced;
	protected Boolean				isInFullName;
    protected String                textBefore;
    protected String                textAfter;
    protected String                fullNameSeparator;
    protected String                formatToken;
	protected TaxonTreeDef			treeDef;
	protected TaxonTreeDefItem		parent;
	protected Set<Taxon>			treeEntries;
	protected Set<TaxonTreeDefItem>	children;

	/** default constructor */
	public TaxonTreeDefItem()
	{
		// do nothing
	}

	/** constructor with id */
	public TaxonTreeDefItem(Integer taxonTreeDefItemId)
	{
		this.taxonTreeDefItemId = taxonTreeDefItemId;
	}

	@Override
    public void initialize()
	{
        super.init();
		taxonTreeDefItemId = null;
		name = null;
		remarks = null;
		rankId = null;
		isEnforced = null;
		isInFullName = null;
        textBefore = null;
        textAfter = null;
        fullNameSeparator = " ";
		treeDef = null;
		treeEntries = new HashSet<Taxon>();
		parent = null;
		children = new HashSet<TaxonTreeDefItem>();
	}

    @Id
    @GeneratedValue
    @Column(name = "TaxonTreeDefItemID", unique = false, nullable = false, insertable = true, updatable = true, length = 10)
	public Integer getTaxonTreeDefItemId()
	{
		return this.taxonTreeDefItemId;
	}

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.taxonTreeDefItemId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return TaxonTreeDefItem.class;
    }

	public void setTaxonTreeDefItemId(Integer taxonTreeDefItemId)
	{
		this.taxonTreeDefItemId = taxonTreeDefItemId;
	}

    @Column(name = "Name", nullable=false, length = 64)
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

    @Lob
    @Column(name = "Remarks", length = 4096)
	public String getRemarks()
	{
		return this.remarks;
	}

	public void setRemarks(String remarks)
	{
		this.remarks = remarks;
	}

    @Column(name = "RankID", nullable=false)
	public Integer getRankId()
	{
		return this.rankId;
	}

	public void setRankId(Integer rankId)
	{
		this.rankId = rankId;
	}

    @Column(name = "IsEnforced", unique = false, nullable = true, insertable = true, updatable = true)
	public Boolean getIsEnforced()
	{
		return this.isEnforced;
	}

	public void setIsEnforced(Boolean isEnforced)
	{
		this.isEnforced = isEnforced;
	}

    @Column(name = "IsInFullName", unique = false, nullable = true, insertable = true, updatable = true)
	public Boolean getIsInFullName()
	{
		return isInFullName;
	}

	public void setIsInFullName(Boolean isInFullName)
	{
		this.isInFullName = isInFullName;
	}

    @Column(name = "TextAfter", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
	public String getTextAfter()
    {
        return textAfter;
    }

    public void setTextAfter(String textAfter)
    {
        this.textAfter = textAfter;
    }

    @Column(name = "TextBefore", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getTextBefore()
    {
        return textBefore;
    }

    public void setTextBefore(String textBefore)
    {
        this.textBefore = textBefore;
    }

    @Column(name = "FullNameSeparator", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getFullNameSeparator()
    {
        return fullNameSeparator;
    }

    public void setFullNameSeparator(String fullNameSeparator)
    {
        this.fullNameSeparator = fullNameSeparator;
    }

    @Column(name = "FormatToken", length = 32)
    public String getFormatToken()
    {
        return formatToken;
    }

    public void setFormatToken(String formatToken)
    {
        this.formatToken = formatToken;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @JoinColumn(name = "TaxonTreeDefID", unique = false, nullable = false, insertable = true, updatable = true)
    public TaxonTreeDef getTreeDef()
	{
		return this.treeDef;
	}

	public void setTreeDef(TaxonTreeDef treeDef)
	{
		this.treeDef = treeDef;
	}

    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @JoinColumn(name = "ParentItemID", unique = false, nullable = true, insertable = true, updatable = true)
	public TaxonTreeDefItem getParent()
	{
		return this.parent;
	}

	public void setParent(TaxonTreeDefItem parent)
	{
		this.parent = parent;
	}

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "definitionItem")
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
	public Set<Taxon> getTreeEntries()
	{
		return this.treeEntries;
	}

	public void setTreeEntries(Set<Taxon> treeEntries)
	{
		this.treeEntries = treeEntries;
	}

    @OneToMany(cascade = { javax.persistence.CascadeType.ALL }, fetch = FetchType.EAGER, mappedBy = "parent")
	public Set<TaxonTreeDefItem> getChildren()
	{
		return this.children;
	}

	public void setChildren(Set<TaxonTreeDefItem> children)
	{
		this.children = children;
	}

    @Transient
	public Integer getTreeDefItemId()
	{
		return getTaxonTreeDefItemId();
	}

	public void setTreeDefItemId(Integer id)
	{
		setTaxonTreeDefItemId(id);
	}

	public void setChild(TaxonTreeDefItem child)
	{
        if (!children.isEmpty())
        {
            TaxonTreeDefItem currentChild = children.iterator().next();
            currentChild.setParent(null);
        }
        
        children.clear();
        
        if(child!=null)
        {
            children.add(child);
        }
	}
	
    @Transient
	public TaxonTreeDefItem getChild()
	{
		if(children.isEmpty())
		{
			return null;
		}
		return children.iterator().next();
	}

	public void addTreeEntry(Taxon entry)
	{
		treeEntries.add(entry);
		entry.setDefinitionItem(this);
	}

	public void removeTreeEntry(Taxon entry)
	{
		treeEntries.remove(entry);
		entry.setDefinitionItem(null);
	}

	public void removeChild(TaxonTreeDefItem child)
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
   public Short getParentTableId()
    {
        return (short)TaxonTreeDef.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return parent != null ? parent.getId() : null;
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
        return 77;
    }
    
    /* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.TreeDefItemIface#hasTreeEntries()
	 */
	@Override
	public boolean hasTreeEntries()
	{
		if (getId() == null)
		{
			return false;
		}
		String sql = "select distinct TaxonTreeDefItemID from taxon where TaxonTreeDefItemID = "
			+ getId();
		return BasicSQLUtils.getNumRecords(sql) > 0;
	}

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof TaxonTreeDefItem)
        {
            TaxonTreeDefItem item = (TaxonTreeDefItem)obj;
            if (item.taxonTreeDefItemId != null)
            {
                if (item.taxonTreeDefItemId.equals(this.taxonTreeDefItemId))
                {
                    return true;
                }
                // else
                return false;
            }
            // else
            return super.equals(obj);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(TaxonTreeDefItem o)
    {
        return rankId.compareTo(o.rankId);
    }

}
