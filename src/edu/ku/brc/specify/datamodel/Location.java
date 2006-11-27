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

import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.treeutils.TreeOrderSiblingComparator;

@SuppressWarnings("serial")
public class Location extends DataModelObjBase implements java.io.Serializable, Treeable<Location,LocationTreeDef,LocationTreeDefItem>{

    /**
     * A <code>Logger</code> object used for all log messages eminating from
     * this class.
     */
    protected static final Logger log = Logger.getLogger(Location.class);

	protected Long			    locationId;
	protected String			name;
	protected String			fullName;
	protected String			remarks;
	protected Integer			rankId;
	protected Integer			nodeNumber;
	protected Integer			highestChildNodeNumber;
	protected String			abbrev;
	protected String			text1;
	protected String			text2;
	protected Integer			number1;
	protected Integer			number2;
	protected Date				timestampVersion;
	protected LocationTreeDef		definition;
	protected LocationTreeDefItem	definitionItem;
	protected Location			parent;
	protected Set<Preparation>	preparations;
	protected Set<Container>	containers;
	protected Set<Location>		children;

	/** default constructor */
	public Location()
	{
		// do nothing
	}

	/** constructor with id */
	public Location(Long locationId)
	{
		this.locationId = locationId;
	}

	@Override
    public void initialize()
	{
		locationId = null;
		name = null;
		remarks = null;
		rankId = null;
		nodeNumber = null;
		highestChildNodeNumber = null;
		abbrev = null;
		text1 = null;
		text2 = null;
		number1 = null;
		number2 = null;
		timestampCreated = new Date();
        timestampModified = null;
		timestampVersion = null;
		lastEditedBy = null;
		definition = null;
		definitionItem = null;
		parent = null;
		preparations = new HashSet<Preparation>();
		containers = new HashSet<Container>();
		children = new HashSet<Location>();
	}

	public Long getLocationId()
	{
		return this.locationId;
	}

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Override
    public Long getId()
    {
        return this.locationId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    public Class getDataClass()
    {
        return Location.class;
    }

	public void setLocationId(Long locationId)
	{
		this.locationId = locationId;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the fullName
	 */
	public String getFullName()
	{
		return fullName;
	}

	/**
	 * @param fullName the fullname to set
	 */
	public void setFullName(String fullName)
	{
		this.fullName = fullName;
	}

	public String getRemarks()
	{
		return remarks;
	}

	public void setRemarks(String remarks)
	{
		this.remarks = remarks;
	}

	public Integer getRankId()
	{
		return this.rankId;
	}

	public void setRankId(Integer rankId)
	{
		this.rankId = rankId;
	}

	public Integer getNodeNumber()
	{
		return this.nodeNumber;
	}

	public void setNodeNumber(Integer nodeNumber)
	{
		this.nodeNumber = nodeNumber;
	}

	public Integer getHighestChildNodeNumber()
	{
		return this.highestChildNodeNumber;
	}

	public void setHighestChildNodeNumber(Integer highestChildNodeNumber)
	{
		this.highestChildNodeNumber = highestChildNodeNumber;
	}

	public String getAbbrev()
	{
		return this.abbrev;
	}

	public void setAbbrev(String abbrev)
	{
		this.abbrev = abbrev;
	}

	public String getText1()
	{
		return this.text1;
	}

	public void setText1(String text1)
	{
		this.text1 = text1;
	}

	public String getText2()
	{
		return this.text2;
	}

	public void setText2(String text2)
	{
		this.text2 = text2;
	}

	public Integer getNumber1()
	{
		return this.number1;
	}

	public void setNumber1(Integer number1)
	{
		this.number1 = number1;
	}

	public Integer getNumber2()
	{
		return this.number2;
	}

	public void setNumber2(Integer number2)
	{
		this.number2 = number2;
	}

	public Date getTimestampVersion()
	{
		return this.timestampVersion;
	}

	public void setTimestampVersion(Date timestampVersion)
	{
		this.timestampVersion = timestampVersion;
	}

	public LocationTreeDef getDefinition()
	{
		return this.definition;
	}

	public void setDefinition(LocationTreeDef definition)
	{
		this.definition = definition;
	}

	public LocationTreeDefItem getDefinitionItem()
	{
		return this.definitionItem;
	}

	public void setDefinitionItem(LocationTreeDefItem definitionItem)
	{
		this.definitionItem = definitionItem;
		if( this.definitionItem!=null )
		{
			this.rankId = this.definitionItem.getRankId();
		}
	}

	public Location getParent()
	{
		return this.parent;
	}

	public void setParent(Location parent)
	{
		this.parent = parent;
	}

	public Set<Preparation> getPreparations()
	{
		return this.preparations;
	}

	public void setPreparations(Set<Preparation> preparations)
	{
		this.preparations = preparations;
	}

	public Set<Container> getContainers()
	{
		return this.containers;
	}

	public void setContainers(Set<Container> containers)
	{
		this.containers = containers;
	}

	public Set<Location> getChildren()
	{
		return this.children;
	}

	public void setChildren(Set<Location> children)
	{
		this.children = children;
	}

	public Long getTreeId()
	{
		return getLocationId();
	}

	public void setTreeId(Long id)
	{
		setLocationId(id);
	}

	public void addChild(Location child)
	{
		Location oldParent = child.getParent();
		if( oldParent!=null )
		{
			oldParent.removeChild(child);
		}

		children.add(child);
		child.setParent(this);
	}

	public void removeChild(Location child)
	{
		children.remove(child);
		child.setParent(null);
	}

	public void addPreparations(final Preparation preparation)
	{
		this.preparations.add(preparation);
		preparation.setLocation(this);
	}

	public void addContainers(final Container container)
	{
		this.containers.add(container);
		container.setLocation(this);
	}

	public void removePreparations(final Preparation preparation)
	{
		this.preparations.remove(preparation);
		preparation.setLocation(null);
	}

	public void removeContainers(final Container container)
	{
		this.containers.remove(container);
		container.setLocation(null);
	}

	@Override
	public String toString()
	{
		String parentName = getParent()!=null ? getParent().getName() : "none";
		return "Location "+locationId+": "+name+", child of "+parentName+", "+rankId+", "
				+nodeNumber+", "+highestChildNodeNumber;
	}

    public int getFullNameDirection()
    {
        return definition.getFullNameDirection();
    }

    public String getFullNameSeparator()
    {
        return definitionItem.getFullNameSeparator();
    }

	/**
	 * Determines if the Location can be deleted.  This method checks whether or not
	 * the given Treeable is referenced by any foreign key contraints.  If no FKs are
	 * currently referring to this node, <code>true</code> is returned.
	 * 
	 * @return <code>true</code> if deletable
	 */
	public boolean canBeDeleted()
	{
		// force all collections to be loaded
		boolean noConts = getContainers().isEmpty();
		boolean noPreps = getPreparations().isEmpty();

		boolean descendantsDeletable = true;
		for( Location child : getChildren() )
		{
			if( !child.canBeDeleted() )
			{
				descendantsDeletable = false;
				break;
			}
		}

		if( noConts&&noPreps&&descendantsDeletable )
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
        Vector<Location> parts = new Vector<Location>();
        parts.add(this);
        Location node = getParent();
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
                    fullNameBuilder.append(parts.get(j).getName());
                    if(j!=0)
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
                    fullNameBuilder.append(parts.get(j).getName());
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
	public int getDescendantCount()
	{
		int totalDescendants = 0;
		for( Location child: getChildren() )
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
	public List<Location> getAllDescendants()
	{
		Vector<Location> descendants = new Vector<Location>();
		for( Location child: getChildren() )
		{
			descendants.add(child);
			descendants.addAll(child.getAllDescendants());
		}
		return descendants;
	}
	
	public List<Location> getAllAncestors()
	{
		Vector<Location> ancestors = new Vector<Location>();
		Location parentNode = parent;
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
		for( Location child: getChildren() )
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

	public boolean isDescendantOf(Location node)
	{
		if( node==null )
		{
			throw new NullPointerException();
		}
		
		Location i = getParent();
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
	
	public Comparator<? super Location> getComparator()
	{
		return new TreeOrderSiblingComparator();
	}
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    public Integer getTableId()
    {
        return 58;
    }

}
