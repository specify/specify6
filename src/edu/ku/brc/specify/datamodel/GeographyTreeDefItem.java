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

@SuppressWarnings("serial")
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@Table(name = "geographytreedefitem")
public class GeographyTreeDefItem extends DataModelObjBase implements Serializable, TreeDefItemIface<Geography,GeographyTreeDef,GeographyTreeDefItem>
{

	// Fields    

	protected Long			    	geographyTreeDefItemId;
	protected String				name;
	protected String				remarks;
	protected Integer				rankId;
	protected Boolean				isEnforced;
	protected Boolean				isInFullName;
    protected String                textBefore;
    protected String                textAfter;
    protected String                fullNameSeparator;
	protected GeographyTreeDef			treeDef;
	protected GeographyTreeDefItem		parent;
	protected Set<Geography>			treeEntries;
	protected Set<GeographyTreeDefItem>	children;

	// Constructors

	/** default constructor */
	public GeographyTreeDefItem()
	{
		// do nothing
	}

	/** constructor with id */
	public GeographyTreeDefItem(Long geographyTreeDefItemId)
	{
		this.geographyTreeDefItemId = geographyTreeDefItemId;
	}

	// Initializer
    @Override
    public void initialize()
	{
        super.init();
		geographyTreeDefItemId = null;
		name = null;
		remarks = null;
		rankId = null;
		isEnforced = null;
		isInFullName = null;
        textBefore = null;
        textAfter = null;
        fullNameSeparator = null;
		treeDef = null;
		treeEntries = new HashSet<Geography>();
		parent = null;
		children = new HashSet<GeographyTreeDefItem>();
	}

	// End Initializer

	// Property accessors

    @Id
    @GeneratedValue
    @Column(name = "GeographyTreeDefItemID", unique = false, nullable = false, insertable = true, updatable = true, length = 10)
	public Long getGeographyTreeDefItemId()
	{
		return this.geographyTreeDefItemId;
	}

    @Transient
    @Override
    public Long getId()
    {
        return this.geographyTreeDefItemId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return GeographyTreeDefItem.class;
    }

	public void setGeographyTreeDefItemId(Long geographyTreeDefItemId)
	{
		this.geographyTreeDefItemId = geographyTreeDefItemId;
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

    @Column(name = "RankID", unique = false, nullable = true, insertable = true, updatable = true)
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

    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.LOCK })
    @JoinColumn(name = "GeographyTreeDefID", unique = false, nullable = false, insertable = true, updatable = true)
	public GeographyTreeDef getTreeDef()
	{
		return this.treeDef;
	}

	public void setTreeDef(GeographyTreeDef treeDef)
	{
		this.treeDef = treeDef;
	}

    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.LOCK })
    @JoinColumn(name = "ParentItemID", unique = false, nullable = true, insertable = true, updatable = true)
	public GeographyTreeDefItem getParent()
	{
		return this.parent;
	}

	public void setParent(GeographyTreeDefItem parent)
	{
		this.parent = parent;
	}

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "definitionItem")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.LOCK })
	public Set<Geography> getTreeEntries()
	{
		return this.treeEntries;
	}

	public void setTreeEntries(Set<Geography> treeEntries)
	{
		this.treeEntries = treeEntries;
	}

    @OneToMany(cascade = { javax.persistence.CascadeType.ALL }, fetch = FetchType.EAGER, mappedBy = "parent")
	public Set<GeographyTreeDefItem> getChildren()
	{
		return this.children;
	}

	public void setChildren(Set<GeographyTreeDefItem> children)
	{
		this.children = children;
	}

    @Transient
	public Long getTreeDefItemId()
	{
		return getGeographyTreeDefItemId();
	}

	public void setTreeDefItemId(Long id)
	{
		setGeographyTreeDefItemId(id);
	}

	public void setChild(GeographyTreeDefItem child)
	{
        if (!children.isEmpty())
        {
            GeographyTreeDefItem currentChild = children.iterator().next();
            currentChild.setParent(null);
        }
        
		children.clear();
        
        if(child!=null)
        {
            children.add(child);
        }
	}
	
    @Transient
	public GeographyTreeDefItem getChild()
	{
		if(children.isEmpty())
		{
			return null;
		}
		return children.iterator().next();
	}

	public void addTreeEntry(Geography entry)
	{
		treeEntries.add(entry);
		entry.setDefinitionItem(this);
	}

	public void removeTreeEntry(Geography entry)
	{
		treeEntries.remove(entry);
		entry.setDefinitionItem(null);
	}

	public void removeChild(GeographyTreeDefItem child)
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
        return 45;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return name;
    }

}
