/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
@Table(name = "lithostrattreedefitem")
public class LithoStratTreeDefItem extends DataModelObjBase implements Serializable, TreeDefItemIface<LithoStrat,LithoStratTreeDef,LithoStratTreeDefItem>
{

	// Fields    

	protected Integer				   lithoStratTreeDefItemId;
	protected String				name;
	protected String				title;
	protected String				remarks;
	protected Integer				rankId;
	protected Boolean				isEnforced;
	protected Boolean				isInFullName;
    protected String                textBefore;
    protected String                textAfter;
    protected String                fullNameSeparator;
	protected LithoStratTreeDef			treeDef;
	protected LithoStratTreeDefItem		parent;
	protected Set<LithoStrat>			treeEntries;
    
    // this is a Set, but should only contain a single child item
	protected Set<LithoStratTreeDefItem>	children;

	// Constructors

	/** default constructor */
	public LithoStratTreeDefItem()
	{
		// do nothing
	}

	/** constructor with id */
	public LithoStratTreeDefItem(Integer lithoStratTreeDefItemId)
	{
		this.lithoStratTreeDefItemId = lithoStratTreeDefItemId;
	}

	// Initializer
    @Override
    public void initialize()
	{
        super.init();
		lithoStratTreeDefItemId = null;
		name = null;
		title = null;
		remarks = null;
		rankId = null;
		isEnforced = null;
		isInFullName = null;
        textBefore = null;
        textAfter = null;
        fullNameSeparator = ", ";
		treeDef = null;
		treeEntries = new HashSet<LithoStrat>();
		parent = null;
		children = new HashSet<LithoStratTreeDefItem>();
	}

	// End Initializer

	// Property accessors

	/**
	 * 
	 */
    @Id
    @GeneratedValue
    @Column(name = "LithoStratTreeDefItemID", unique = false, nullable = false, insertable = true, updatable = true, length = 10)
	public Integer getLithoStratTreeDefItemId()
	{
		return this.lithoStratTreeDefItemId;
	}

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Override
    @Transient
    public Integer getId()
    {
        return this.lithoStratTreeDefItemId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return LithoStratTreeDefItem.class;
    }

	public void setLithoStratTreeDefItemId(Integer lithoStratTreeDefItemId)
	{
		this.lithoStratTreeDefItemId = lithoStratTreeDefItemId;
	}

	/**
	 * 
	 */
    @Column(name = "Name", nullable=false, length = 64)
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

    /**
     * @return the title
     */
    @Column(name = "Title", nullable=true, length = 64)
	public String getTitle()
	{
		return this.title;
	}

    /**
     * @param title the title to set
     */
    public void setTitle(String title)
	{
		this.title = title;
	}

    
    /* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.TreeDefItemIface#getDisplayText()
	 */
	@Override
	@Transient
	public String getDisplayText()
	{
		return (title != null ? title : name);
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.TreeDefItemIface#setDisplayText(java.lang.String)
	 */
	@Override
	public void setDisplayText(String text)
	{
		setTitle(text);
	}

	/**
	 * 
	 */
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

	/**
	 * 
	 */
    @Column(name = "RankID", nullable=false)
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

    /**
	 * 
	 */
    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @JoinColumn(name = "LithoStratTreeDefID", unique = false, nullable = false, insertable = true, updatable = true)
	public LithoStratTreeDef getTreeDef()
	{
		return this.treeDef;
	}

	public void setTreeDef(LithoStratTreeDef treeDef)
	{
		this.treeDef = treeDef;
	}

	/**
	 * 
	 */
    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @JoinColumn(name = "ParentItemID", unique = false, nullable = true, insertable = true, updatable = true)
	public LithoStratTreeDefItem getParent()
	{
		return this.parent;
	}

	public void setParent(LithoStratTreeDefItem parent)
	{
		this.parent = parent;
	}

	/**
	 * 
	 */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "definitionItem")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.LOCK, CascadeType.MERGE })
	public Set<LithoStrat> getTreeEntries()
	{
		return this.treeEntries;
	}

	public void setTreeEntries(Set<LithoStrat> treeEntries)
	{
		this.treeEntries = treeEntries;
	}

	/**
	 * 
	 */
    @OneToMany(cascade = { javax.persistence.CascadeType.ALL }, fetch = FetchType.EAGER, mappedBy = "parent")
	public Set<LithoStratTreeDefItem> getChildren()
	{
		return this.children;
	}

	public void setChildren(Set<LithoStratTreeDefItem> children)
	{
		this.children = children;
	}

	// Code added to implement TreeDefinitionItemIface

    @Transient
	public Integer getTreeDefItemId()
	{
		return getLithoStratTreeDefItemId();
	}

	public void setTreeDefItemId(Integer id)
	{
		setLithoStratTreeDefItemId(id);
	}

	public void setChild(LithoStratTreeDefItem child)
	{
        if (!children.isEmpty())
        {
            LithoStratTreeDefItem currentChild = children.iterator().next();
            currentChild.setParent(null);
        }
        
        children.clear();
        
        if(child!=null)
        {
            children.add(child);
        }
	}
	
    @Transient
	public LithoStratTreeDefItem getChild()
	{
		if(children.isEmpty())
		{
			return null;
		}
		return children.iterator().next();
	}

	public void addTreeEntry(LithoStrat entry)
	{
		treeEntries.add(entry);
		entry.setDefinitionItem(this);
	}

	public void removeTreeEntry(LithoStrat entry)
	{
		treeEntries.remove(entry);
		entry.setDefinitionItem(null);
	}

	public void removeChild(LithoStratTreeDefItem child)
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
    public Integer getParentTableId()
    {
        return LithoStratTreeDef.getClassTableId();
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
        return 48;
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
		String sql = "select distinct LithoStratTreeDefItemID from lithostrat where LithoStratTreeDefItemID = "
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

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof LithoStratTreeDefItem)
        {
            LithoStratTreeDefItem item = (LithoStratTreeDefItem)obj;
            if (item.lithoStratTreeDefItemId != null)
            {
                if (item.lithoStratTreeDefItemId.equals(this.lithoStratTreeDefItemId))
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
}
