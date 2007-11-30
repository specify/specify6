/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.datamodel;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

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

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.specify.treeutils.TreeOrderSiblingComparator;

@SuppressWarnings("serial")
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "location")
@org.hibernate.annotations.Table(appliesTo="location", indexes =
    {   @Index (name="LocNameIDX", columnNames={"Name"}),
        @Index (name="LocFullNameIDX", columnNames={"FullName"})
    })
public class Location extends DataModelObjBase implements Serializable, Treeable<Location,LocationTreeDef,LocationTreeDefItem>{

    /**
     * A <code>Logger</code> object used for all log messages eminating from
     * this class.
     */
    protected static final Logger log = Logger.getLogger(Location.class);

	protected Integer			    locationId;
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

    // for synonym support
    protected Boolean             isAccepted;
    protected Location            acceptedLocation;
    protected Set<Location>       acceptedChildren;


	/** default constructor */
	public Location()
	{
		// do nothing
	}

	/** constructor with id */
	public Location(Integer locationId)
	{
		this.locationId = locationId;
	}

    @Override
    public void initialize()
	{
        super.init();
		locationId = null;
		name = null;
        fullName = null;
		remarks = null;
		rankId = null;
		nodeNumber = null;
		highestChildNodeNumber = null;
		abbrev = null;
		text1 = null;
		text2 = null;
		number1 = null;
		number2 = null;
		timestampVersion = null;
		definition = null;
		definitionItem = null;
		parent = null;
		preparations = new HashSet<Preparation>();
		containers = new HashSet<Container>();
		children = new HashSet<Location>();
	}

    @Id
    @GeneratedValue
    @Column(name = "LocationID")
	public Integer getLocationId()
	{
		return this.locationId;
	}

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Override
    @Transient
    public Integer getId()
    {
        return this.locationId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Location.class;
    }

	public void setLocationId(Integer locationId)
	{
		this.locationId = locationId;
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

	/**
	 * @return the fullName
	 */
    @Column(name = "FullName", length = 255)
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

    @Lob
    @Column(name = "Remarks", length = 4096)
	public String getRemarks()
	{
		return remarks;
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

    @Column(name = "NodeNumber", updatable = false)
	public Integer getNodeNumber()
	{
		return this.nodeNumber;
	}

	public void setNodeNumber(Integer nodeNumber)
	{
		this.nodeNumber = nodeNumber;
	}

    @Column(name = "HighestChildNodeNumber", updatable = false)
	public Integer getHighestChildNodeNumber()
	{
		return this.highestChildNodeNumber;
	}

	public void setHighestChildNodeNumber(Integer highestChildNodeNumber)
	{
		this.highestChildNodeNumber = highestChildNodeNumber;
	}

    @Column(name = "Abbrev", length = 16)
	public String getAbbrev()
	{
		return this.abbrev;
	}

	public void setAbbrev(String abbrev)
	{
		this.abbrev = abbrev;
	}

    @Column(name = "Text1", length = 32)
	public String getText1()
	{
		return this.text1;
	}

	public void setText1(String text1)
	{
		this.text1 = text1;
	}

    @Column(name = "Text2", length = 32)
	public String getText2()
	{
		return this.text2;
	}

	public void setText2(String text2)
	{
		this.text2 = text2;
	}

    @Column(name = "Number1")
	public Integer getNumber1()
	{
		return this.number1;
	}

	public void setNumber1(Integer number1)
	{
		this.number1 = number1;
	}

    @Column(name = "Number2")
	public Integer getNumber2()
	{
		return this.number2;
	}

	public void setNumber2(Integer number2)
	{
		this.number2 = number2;
	}

    @Column(name = "TimestampVersion")
	public Date getTimestampVersion()
	{
		return this.timestampVersion;
	}

	public void setTimestampVersion(Date timestampVersion)
	{
		this.timestampVersion = timestampVersion;
	}
    
    @Column(name="IsAccepted")
    public Boolean getIsAccepted()
    {
        return this.isAccepted;
    }

    public void setIsAccepted(Boolean accepted)
    {
        this.isAccepted = accepted;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "acceptedLocation")
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
    public Set<Location> getAcceptedChildren()
    {
        return this.acceptedChildren;
    }

    public void setAcceptedChildren(Set<Location> acceptedChildren)
    {
        this.acceptedChildren = acceptedChildren;
    }

    @ManyToOne
    @JoinColumn(name = "AcceptedID")
    public Location getAcceptedLocation()
    {
        return this.acceptedLocation;
    }

    public void setAcceptedLocation(Location acceptedLocation)
    {
        this.acceptedLocation = acceptedLocation;
    }
    
    @Transient
    public Location getAcceptedParent()
    {
        return getAcceptedLocation();
    }
    
    public void setAcceptedParent(Location acceptedParent)
    {
        setAcceptedLocation(acceptedParent);
    }

    @ManyToOne
    @JoinColumn(name = "LocationTreeDefID", nullable = false)
	public LocationTreeDef getDefinition()
	{
		return this.definition;
	}

	public void setDefinition(LocationTreeDef definition)
	{
		this.definition = definition;
	}

    @ManyToOne
    @JoinColumn(name = "LocationTreeDefItemID", nullable = false)
	public LocationTreeDefItem getDefinitionItem()
	{
		return this.definitionItem;
	}

	public void setDefinitionItem(LocationTreeDefItem definitionItem)
	{
        this.definitionItem = definitionItem;
        if (definitionItem!=null && definitionItem.getRankId()!=null)
        {
            this.rankId = this.definitionItem.getRankId();
        }
	}

    @ManyToOne
    @JoinColumn(name = "ParentID")
	public Location getParent()
	{
		return this.parent;
	}

	public void setParent(Location parent)
	{
		this.parent = parent;
	}

    @OneToMany(mappedBy = "location")
    @Cascade( {CascadeType.ALL} )
	public Set<Preparation> getPreparations()
	{
		return this.preparations;
	}

	public void setPreparations(Set<Preparation> preparations)
	{
		this.preparations = preparations;
	}

    @OneToMany(mappedBy = "location")
    @Cascade( {CascadeType.ALL} )
	public Set<Container> getContainers()
	{
		return this.containers;
	}

	public void setContainers(Set<Container> containers)
	{
		this.containers = containers;
	}

    @OneToMany(mappedBy = "parent")
    @Cascade( {CascadeType.ALL} )
	public Set<Location> getChildren()
	{
		return this.children;
	}

	public void setChildren(Set<Location> children)
	{
		this.children = children;
	}

    @Transient
	public Integer getTreeId()
	{
		return getLocationId();
	}

	public void setTreeId(Integer id)
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
        return (fullName!=null) ? fullName : super.toString();
    }

    @Transient
    public int getFullNameDirection()
    {
        return definition.getFullNameDirection();
    }

    @Transient
    public String getFullNameSeparator()
    {
        return definitionItem.getFullNameSeparator();
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
                    Location part = parts.get(j);
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
                    Location part = parts.get(j);
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
        
        return fullNameBuilder.toString().trim();
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
    @Transient
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
	
    @Transient
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

	public boolean isDescendantOf(Location node)
	{
		if( node==null )
		{
			throw new NullPointerException();
		}
		
		Location i = getParent();
		while( i != null )
		{
			if( i.getId() == getId() )
			{
				return true;
			}
			
			i = i.getParent();
		}
		return false;
	}
	
    @Transient
	public Comparator<? super Location> getComparator()
	{
		return new TreeOrderSiblingComparator();
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
        return 58;
    }

}
