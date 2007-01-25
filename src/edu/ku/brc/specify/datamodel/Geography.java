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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.treeutils.TreeOrderSiblingComparator;

@SuppressWarnings("serial")
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@Table(name = "geography")
public class Geography extends DataModelObjBase implements java.io.Serializable, Treeable<Geography,GeographyTreeDef,GeographyTreeDefItem>{

    /**
     * A <code>Logger</code> object used for all log messages eminating from
     * this class.
     */
    protected static final Logger log = Logger.getLogger(Geography.class);

	protected Long			    	geographyId;
	protected String				name;
	protected String				remarks;
	protected String				commonName;
	protected String				fullName;
	protected String				geographyCode;
	protected Integer				rankId;
	protected Integer				nodeNumber;
	protected Integer				highestChildNodeNumber;
	protected String				abbrev;
	protected String				text1;
	protected String				text2;
	protected Integer				number1;
	protected Integer				number2;
	protected Date					timestampVersion;
	protected Boolean				isCurrent;
	protected Set<Locality>			localities;
	protected GeographyTreeDef		definition;
	protected GeographyTreeDefItem	definitionItem;
	protected Geography				parent;
	protected Set<Geography>		children;

	// Constructors

	/** default constructor */
	public Geography()
	{
		// do nothing
	}

	/** constructor with id */
	public Geography(Long geographyId)
	{
		this.geographyId = geographyId;
	}

	// Initializer
    @Override
    public void initialize()
	{
        super.init();
		geographyId = null;
		name = null;
		remarks = null;
		commonName = null;
		geographyCode = null;
		rankId = null;
		nodeNumber = null;
		highestChildNodeNumber = null;
		abbrev = null;
		text1 = null;
		text2 = null;
		number1 = null;
		number2 = null;
		timestampVersion = null;
		isCurrent = null;
		localities = new HashSet<Locality>();
		definition = null;
		definitionItem = null;
		parent = null;
		children = new HashSet<Geography>();
	}

	// End Initializer

	// Property accessors

	/**
	 *
	 */
    @Id
    @GeneratedValue
    @Column(name = "GeographyID", unique = false, nullable = false, insertable = true, updatable = true)
	public Long getGeographyId()
	{
		return this.geographyId;
	}

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Override
    @Transient
    public Long getId()
    {
        return this.geographyId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return Geography.class;
    }

	public void setGeographyId(Long geographyId)
	{
		this.geographyId = geographyId;
	}

	/**
	 *
	 */
    @Column(name = "Name", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

    @Column(name = "Remarks", length=65535, unique = false, nullable = true, insertable = true, updatable = true)
	public String getRemarks()
	{
		return remarks;
	}

	public void setRemarks(String remarks)
	{
		this.remarks = remarks;
	}

	/**
	 *
	 */
    @Column(name = "CommonName", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
	public String getCommonName()
	{
		return this.commonName;
	}

	public void setCommonName(String commonName)
	{
		this.commonName = commonName;
	}

	/**
	 * @return the fullName
	 */
    @Column(name = "FullName", unique = false, nullable = true, insertable = true, updatable = true)
	public String getFullName()
	{
		return fullName;
	}

	/**
	 * @param fullName the fullName to set
	 */
	public void setFullName(String fullName)
	{
		this.fullName = fullName;
	}

	/**
	 *
	 */
    @Column(name = "GeographyCode", unique = false, nullable = true, insertable = true, updatable = true, length = 8)
	public String getGeographyCode()
	{
		return this.geographyCode;
	}

	public void setGeographyCode(String geographyCode)
	{
		this.geographyCode = geographyCode;
	}

	/**
	 *
	 */
    @Column(name = "RankID", unique = false, nullable = false, insertable = true, updatable = true, length = 10)
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
    @Column(name = "NodeNumber", unique = false, nullable = true, insertable = true, updatable = false, length = 10)
	public Integer getNodeNumber()
	{
		return this.nodeNumber;
	}

	public void setNodeNumber(Integer nodeNumber)
	{
		this.nodeNumber = nodeNumber;
	}

	/**
	 *
	 */
    @Column(name = "HighestChildNodeNumber", unique = false, nullable = true, insertable = true, updatable = false, length = 10)
	public Integer getHighestChildNodeNumber()
	{
		return this.highestChildNodeNumber;
	}

	public void setHighestChildNodeNumber(Integer highestChildNodeNumber)
	{
		this.highestChildNodeNumber = highestChildNodeNumber;
	}

	/**
	 *
	 */
    @Column(name = "Abbrev", unique = false, nullable = true, insertable = true, updatable = true, length = 16)
	public String getAbbrev()
	{
		return this.abbrev;
	}

	public void setAbbrev(String abbrev)
	{
		this.abbrev = abbrev;
	}

	/**
	 *
	 */
    @Column(name = "Text1", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
	public String getText1()
	{
		return this.text1;
	}

	public void setText1(String text1)
	{
		this.text1 = text1;
	}

	/**
	 *
	 */
    @Column(name = "Text2", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
	public String getText2()
	{
		return this.text2;
	}

	public void setText2(String text2)
	{
		this.text2 = text2;
	}

	/**
	 *
	 */
    @Column(name = "Number1", unique = false, nullable = true, insertable = true, updatable = true, length = 10)
	public Integer getNumber1()
	{
		return this.number1;
	}

	public void setNumber1(Integer number1)
	{
		this.number1 = number1;
	}

	/**
	 *
	 */
    @Column(name = "Number2", unique = false, nullable = true, insertable = true, updatable = true, length = 10)
	public Integer getNumber2()
	{
		return this.number2;
	}

	public void setNumber2(Integer number2)
	{
		this.number2 = number2;
	}

	/**
	 *
	 */
    @Column(name = "TimestampVersion", unique = false, nullable = true, insertable = true, updatable = true, length = 16)
	public Date getTimestampVersion()
	{
		return this.timestampVersion;
	}

	public void setTimestampVersion(Date timestampVersion)
	{
		this.timestampVersion = timestampVersion;
	}

	/**
	 *
	 */
    @Column(name = "IsCurrent", unique = false, nullable = true, insertable = true, updatable = true)
	public Boolean getIsCurrent()
	{
		return this.isCurrent;
	}

	public void setIsCurrent(Boolean isCurrent)
	{
		this.isCurrent = isCurrent;
	}

	/**
	 *
	 */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "geography")
	public Set<Locality> getLocalities()
	{
		return this.localities;
	}

	public void setLocalities(Set<Locality> localities)
	{
		this.localities = localities;
	}

	/**
	 *
	 */
    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @Cascade( { CascadeType.LOCK })
    @JoinColumn(name = "GeographyTreeDefID", unique = false, nullable = false, insertable = true, updatable = true)
	public GeographyTreeDef getDefinition()
	{
		return this.definition;
	}

	public void setDefinition(GeographyTreeDef definition)
	{
		this.definition = definition;
	}

	/**
	 *
	 */
    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.LOCK })
    @JoinColumn(name = "GeographyTreeDefItemID", unique = false, nullable = false, insertable = true, updatable = true)
	public GeographyTreeDefItem getDefinitionItem()
	{
		return this.definitionItem;
	}

	public void setDefinitionItem(GeographyTreeDefItem definitionItem)
	{
		this.definitionItem = definitionItem;
		if (definitionItem!=null && definitionItem.getRankId()!=null)
		{
			this.rankId = this.definitionItem.getRankId();
		}
	}

	/**
	 *
	 */
    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.LOCK })
    @JoinColumn(name = "ParentID", unique = false, nullable = true, insertable = true, updatable = true)
	public Geography getParent()
	{
		return this.parent;
	}

	public void setParent(Geography parent)
	{
		this.parent = parent;
	}

	/**
	 *
	 */
    @OneToMany(cascade = { javax.persistence.CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "parent")
	public Set<Geography> getChildren()
	{
		return this.children;
	}

	public void setChildren(Set<Geography> children)
	{
		this.children = children;
	}

	/* Code added in order to implement Treeable */

    @Transient
	public Long getTreeId()
	{
		return getGeographyId();
	}

	public void setTreeId(Long id)
	{
		setGeographyId(id);
	}

	public void addChild(Geography child)
	{
		Geography oldParent = child.getParent();
		if( oldParent!=null )
		{
			oldParent.removeChild(child);
		}

		children.add(child);
		child.setParent(this);
	}
    
	public void removeChild(Geography child)
	{
		children.remove(child);
		child.setParent(null);
	}

	// Add Methods

	public void addLocality(final Locality loc)
	{
		localities.add(loc);
		loc.setGeography(this);
	}

	// Done Add Methods

	// Delete Methods

	public void removeLocality(final Locality loc)
	{
		localities.remove(loc);
		loc.setGeography(null);
	}

	// Delete Add Methods

	@Override
	public String toString()
	{
		String parentName = getParent()!=null ? getParent().getName() : "none";
		return "Geography "+geographyId+": "+name+", child of "+parentName+", "+rankId+", "
				+nodeNumber+", "+highestChildNodeNumber;
	}

	// methods to complete implementation of AbstractTreeable

    @Transient
	public int getFullNameDirection()
	{
        Integer dir = definition.getFullNameDirection();
        
        // if it's anything other than FORWARD, set it to REVERSE to avoid 'null' or undefined values
        if (dir==null || dir!=TreeDefIface.FORWARD)
        {
            dir = TreeDefIface.REVERSE;
        }
		return dir;
	}

    @Transient
	public String getFullNameSeparator()
	{
		return definitionItem.getFullNameSeparator();
	}

	/**
	 * Determines if the given Geography can be deleted.  This method checks wether or not
	 * the given Treeable is referenced by any foreign key contraints.  If no FKs are
	 * currently referring to this node, <code>true</code> is returned.
	 * 
	 * @see #canBeDeleted(Treeable)
	 * @param geo the node to check
	 * @return <code>true</code> if deletable
	 */
	public boolean canBeDeleted()
	{
		// force all collections to be loaded
		boolean noLocs = getLocalities().isEmpty();

		boolean descendantsDeletable = true;
		for( Geography child : getChildren() )
		{
			if( !child.canBeDeleted() )
			{
				descendantsDeletable = false;
				break;
			}
		}

		if( noLocs&&descendantsDeletable )
		{
			return true;
		}

		return false;
	}
	
	/**
	 * Generates the 'full name' of a node using the <code>IsInFullName</code> field from the tree
	 * definition items and following the parent pointer until we hit the root node.  Also used
	 * in the process is a "direction indicator" for the tree determining whether the name
	 * should start with the higher nodes and work down to the given node or vice versa.
	 * 
	 * @param node the node to get the full name for
	 * @return the full name
	 */
	public String fixFullName()
	{
		Vector<Geography> parts = new Vector<Geography>();
        parts.add(this);
		Geography node = getParent();
		while( node != null )
		{
			Boolean include = node.getDefinitionItem().getIsInFullName();
			if( include != null && include.booleanValue() == true )
			{
				parts.add(node);
			}
			
            node = node.getParent();
		}
		int direction = getFullNameDirection();
		
		StringBuilder fullNameBuilder = new StringBuilder(parts.size() * 10);
		
		switch( direction )
		{
			case FORWARD:
			{
				for( int j = parts.size()-1; j > -1; --j )
				{
                    Geography part = parts.get(j);
                    String before = part.getDefinitionItem().getTextBefore();
                    String after = part.getDefinitionItem().getTextAfter();

                    if (before!=null)
                    {
                        fullNameBuilder.append(part.getDefinitionItem().getTextBefore());
                    }
                    fullNameBuilder.append(part.getName());
                    if (after!=null)
                    {
                        fullNameBuilder.append(part.getDefinitionItem().getTextAfter());
                    }
                    if(j!=parts.size()-1)
                    {
                        fullNameBuilder.append(parts.get(j).getFullNameSeparator());
                    }
				}
				break;
			}
			case REVERSE:
			{
				for( int j = 0; j < parts.size(); ++j )
				{
                    Geography part = parts.get(j);
                    String before = part.getDefinitionItem().getTextBefore();
                    String after = part.getDefinitionItem().getTextAfter();

                    if (before!=null)
                    {
                        fullNameBuilder.append(part.getDefinitionItem().getTextBefore());
                    }
                    fullNameBuilder.append(part.getName());
                    if (after!=null)
                    {
                        fullNameBuilder.append(part.getDefinitionItem().getTextAfter());
                    }
					if(j!=parts.size()-1)
                    {
                        fullNameBuilder.append(parts.get(j).getFullNameSeparator());
                    }
				}
				break;
			}
			default:
			{
				log.error("Invalid tree walk direction (for creating fullname field) found in tree definition");
				return null;
			}
		}
		
		return fullNameBuilder.toString();
	}
	
	/**
	 * Returns the number of proper descendants for node.
	 * 
	 * @param node the node to count descendants for
	 * @return the number of proper descendants
	 */
    @Transient
	public int getDescendantCount()
	{
		int totalDescendants = 0;
		for( Geography child: getChildren() )
		{
			totalDescendants += 1 + child.getDescendantCount();
		}
		return totalDescendants;
	}
	
	/**
	 * Determines if children are allowed for the given node.
	 * 
	 * @param item the node to examine
	 * @return <code>true</code> if children are allowed as defined by the node's tree definition, false otherwise
	 */
	public boolean childrenAllowed()
	{
		if( definitionItem == null || definitionItem.getChild() == null )
		{
			return false;
		}
		return true;
	}

	/**
	 * Returns a <code>List</code> of all descendants of the called <code>node</code>.
	 * 
	 * @return all descendants of <code>node</code>
	 */
    @Transient
	public List<Geography> getAllDescendants()
	{
		Vector<Geography> descendants = new Vector<Geography>();
		for( Geography child: getChildren() )
		{
			descendants.add(child);
			descendants.addAll(child.getAllDescendants());
		}
		return descendants;
	}
	
    @Transient
	public List<Geography> getAllAncestors()
	{
		Vector<Geography> ancestors = new Vector<Geography>();
		Geography parentNode = parent;
		while(parentNode != null)
		{
			ancestors.add(0,parentNode);
			parentNode = parentNode.getParent();
		}
		
		return ancestors;
	}

	/**
	 * Fixes the fullname for the given node and all of its descendants.
	 */
	public void fixFullNameForAllDescendants()
	{
		setFullName(fixFullName());
		for( Geography child: getChildren() )
		{
			child.fixFullNameForAllDescendants();
		}
	}
	
	/**
	 * Updates the created and modified timestamps to now.  Also
	 * updates the <code>lastEditedBy</code> field to the current
	 * value of the <code>user.name</code> system property.
	 */
	public void setTimestampsToNow()
	{
		Date now = new Date();
		setTimestampCreated(now);
		setTimestampModified(now);

		//TODO: fix this somehow
		String user = System.getProperty("user.name");
		setLastEditedBy(user);
	}
	
	/**
	 * Updates the modified timestamp to now.  Also updates the
	 * <code>lastEditedBy</code> field to the current value
	 * of the <code>user.name</code> system property.
	 */
	public void updateModifiedTimeAndUser()
	{
		Date now = new Date();
		setTimestampModified(now);
		
		//TODO: fix this somehow
		String user = System.getProperty("user.name");
		setLastEditedBy(user);
	}

	public boolean isDescendantOf(Geography node)
	{
		if( node==null )
		{
			throw new NullPointerException();
		}
		
		Geography i = getParent();
		while( i != null )
		{
			if( i == node )
			{
				return true;
			}
			
			i = i.getParent();
		}
		return false;
	}
	
    @Transient
	public Comparator<? super Geography> getComparator()
	{
		return new TreeOrderSiblingComparator();
	}
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public Integer getTableId()
    {
        return 3;
    }

}
