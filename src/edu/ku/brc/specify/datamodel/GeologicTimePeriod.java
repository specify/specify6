package edu.ku.brc.specify.datamodel;

import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.treeutils.GeologicTimePeriodComparator;

@SuppressWarnings("serial")
public class GeologicTimePeriod implements java.io.Serializable, Treeable<GeologicTimePeriod,GeologicTimePeriodTreeDef,GeologicTimePeriodTreeDefItem>{

    /**
     * A <code>Logger</code> object used for all log messages eminating from
     * this class.
     */
    protected static final Logger log = Logger.getLogger(Geography.class);

	protected Long						    geologicTimePeriodId;
	protected Integer						rankId;
	protected String						name;
	protected String						fullName;
	protected String						remarks;
	protected Integer						nodeNumber;
	protected Integer						highestChildNodeNumber;
	protected String						standard;
	protected Float							start;
	protected Float							startUncertainty;
	protected Float							end;
	protected Float							endUncertainty;
	protected Date							timestampModified;
	protected Date							timestampCreated;
	protected Date							timestampVersion;
	protected String						lastEditedBy;
	private GeologicTimePeriodTreeDef		definition;
	private GeologicTimePeriodTreeDefItem	definitionItem;
	private GeologicTimePeriod				parent;
	protected Set<GeologicTimePeriod>		children;
	protected Set<Stratigraphy>				stratigraphies;

	// Constructors

	/** default constructor */
	public GeologicTimePeriod()
	{
		// do nothing
	}

	/** constructor with id */
	public GeologicTimePeriod(Long geologicTimePeriodId)
	{
		this.geologicTimePeriodId = geologicTimePeriodId;
	}

	// Initializer
	public void initialize()
	{
		geologicTimePeriodId = null;
		rankId = null;
		name = null;
		remarks = null;
		nodeNumber = null;
		highestChildNodeNumber = null;
		standard = null;
		start = null;
		startUncertainty = null;
		end = null;
		endUncertainty = null;
        //timestampModified = null;
		timestampCreated = new Date();
		timestampVersion = new Date();
		lastEditedBy = null;
		definition = null;
		definitionItem = null;
		parent = null;
		children = new HashSet<GeologicTimePeriod>();
		stratigraphies = new HashSet<Stratigraphy>();
	}

	// End Initializer

	// Property accessors

	/**
	 * 
	 */
	public Long getGeologicTimePeriodId()
	{
		return this.geologicTimePeriodId;
	}

	public void setGeologicTimePeriodId(Long geologicTimePeriodId)
	{
		this.geologicTimePeriodId = geologicTimePeriodId;
	}

	/**
	 * 
	 */
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
	 * @param fullName the fullName to set
	 */
	public void setFullName(String fullName)
	{
		this.fullName = fullName;
	}

	/**
	 * 
	 */
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
	public String getStandard()
	{
		return this.standard;
	}

	public void setStandard(String standard)
	{
		this.standard = standard;
	}

	public Float getEnd()
	{
		return end;
	}

	public void setEnd(Float end)
	{
		this.end = end;
	}

	public Float getEndUncertainty()
	{
		return endUncertainty;
	}

	public void setEndUncertainty(Float endUncertainty)
	{
		this.endUncertainty = endUncertainty;
	}

	public Float getStart()
	{
		return start;
	}

	public void setStart(Float start)
	{
		this.start = start;
	}

	public Float getStartUncertainty()
	{
		return startUncertainty;
	}

	public void setStartUncertainty(Float startUncertainty)
	{
		this.startUncertainty = startUncertainty;
	}

	/**
	 * 
	 */
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
	public Date getTimestampModified()
	{
		return this.timestampModified;
	}

	public void setTimestampModified(Date timestampModified)
	{
		this.timestampModified = timestampModified;
	}

	/**
	 * 
	 */
	public Date getTimestampCreated()
	{
		return this.timestampCreated;
	}

	public void setTimestampCreated(Date timestampCreated)
	{
		this.timestampCreated = timestampCreated;
	}

	/**
	 * 
	 */
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
	public String getLastEditedBy()
	{
		return this.lastEditedBy;
	}

	public void setLastEditedBy(String lastEditedBy)
	{
		this.lastEditedBy = lastEditedBy;
	}

	/**
	 * 
	 */
	public GeologicTimePeriodTreeDef getDefinition()
	{
		return this.definition;
	}

	public void setDefinition(GeologicTimePeriodTreeDef definition)
	{
		this.definition = definition;
	}

	/**
	 * 
	 */
	public GeologicTimePeriodTreeDefItem getDefinitionItem()
	{
		return this.definitionItem;
	}

	public void setDefinitionItem(GeologicTimePeriodTreeDefItem definitionItem)
	{
		this.definitionItem = definitionItem;
		if( this.definitionItem!=null )
		{
			this.rankId = this.definitionItem.getRankId();
		}
	}

	/**
	 * 
	 */
	public GeologicTimePeriod getParent()
	{
		return this.parent;
	}

	public void setParent(GeologicTimePeriod parent)
	{
		this.parent = parent;
	}

	/**
	 * 
	 */
	public Set<GeologicTimePeriod> getChildren()
	{
		return this.children;
	}

	public void setChildren(Set<GeologicTimePeriod> children)
	{
		this.children = children;
	}

	public Set<Stratigraphy> getStratigraphies()
	{
		return stratigraphies;
	}

	public void setStratigraphies(Set<Stratigraphy> stratigraphies)
	{
		this.stratigraphies = stratigraphies;
	}

	/* Code added in order to implement Treeable */

	public Long getTreeId()
	{
		return getGeologicTimePeriodId();
	}

	public void setTreeId(Long id)
	{
		setGeologicTimePeriodId(id);
	}

	public void addChild(GeologicTimePeriod child)
	{
		GeologicTimePeriod oldParent = child.getParent();
		if( oldParent!=null )
		{
			oldParent.removeChild(child);
		}

		children.add(child);
		child.setParent(this);
	}

	public void removeChild(GeologicTimePeriod child)
	{
		children.remove(child);
		child.setParent(null);
	}

	public void addStratigraphy(Stratigraphy strat)
	{
		GeologicTimePeriod oldGTP = strat.getGeologicTimePeriod();
		if( oldGTP!=null )
		{
			oldGTP.removeStratigraphy(strat);
		}

		stratigraphies.add(strat);
		strat.setGeologicTimePeriod(this);
	}

	public void removeStratigraphy(Stratigraphy strat)
	{
		stratigraphies.remove(strat);
		strat.setGeologicTimePeriod(null);
	}

	// temporary implementation of toString() for easier debugging
	@Override
	public String toString()
	{
		String parentName = getParent()!=null ? getParent().getName() : "none";
		return "GeologicTimePeriod "+geologicTimePeriodId+": "+name+", child of "+parentName+", "
				+rankId+", "+nodeNumber+", "+highestChildNodeNumber;
	}

	// methods to complete implementation of AbstractTreeable

	public int getFullNameDirection()
	{
		//TODO: move these to prefs
		//XXX: pref
		return REVERSE;
	}

	public String getFullNameSeparator()
	{
		//TODO: move these to prefs
		//XXX: pref
		return ", ";
	}

	/**
	 * Determines if the GeologicTimePeriod can be deleted.  This method checks wether or not
	 * the given Treeable is referenced by any foreign key contraints.  If no FKs are
	 * currently referring to this node, <code>true</code> is returned.
	 * 
	 * @return <code>true</code> if deletable
	 */
	public boolean canBeDeleted()
	{
		// force all collections to be loaded
		boolean noStrats = getStratigraphies().isEmpty();

		boolean descendantsDeletable = true;
		for( GeologicTimePeriod child : getChildren() )
		{
			if( !child.canBeDeleted() )
			{
				descendantsDeletable = false;
				break;
			}
		}

		if( noStrats&&descendantsDeletable )
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
		Vector<String> parts = new Vector<String>();
		parts.add(getName());
		GeologicTimePeriod taxon = getParent();
		while( taxon != null )
		{
			Boolean include = taxon.getDefinitionItem().getIsInFullName();
			if( include != null && include.booleanValue() == true )
			{
				parts.add(taxon.getName());
			}
			
			taxon = taxon.getParent();
		}
		int direction = getFullNameDirection();
		String sep = getFullNameSeparator();
		
		StringBuilder fullNameBuilder = new StringBuilder(parts.size() * 10);
		
		switch( direction )
		{
			case FORWARD:
			{
				for( int j = parts.size()-1; j > -1; --j )
				{
					fullNameBuilder.append(parts.get(j));
					fullNameBuilder.append(sep);
				}
				break;
			}
			case REVERSE:
			{
				for( int j = 0; j < parts.size(); ++j )
				{
					fullNameBuilder.append(parts.get(j));
					fullNameBuilder.append(sep);
				}
				break;
			}
			default:
			{
				log.error("Invalid tree walk direction (for creating fullname field) found in tree definition");
				return null;
			}
		}
		
		fullNameBuilder.delete(fullNameBuilder.length()-sep.length(), fullNameBuilder.length());
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
		for( GeologicTimePeriod child: getChildren() )
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
	public List<GeologicTimePeriod> getAllDescendants()
	{
		Vector<GeologicTimePeriod> descendants = new Vector<GeologicTimePeriod>();
		for( GeologicTimePeriod child: getChildren() )
		{
			descendants.add(child);
			descendants.addAll(child.getAllDescendants());
		}
		return descendants;
	}
	
	public List<GeologicTimePeriod> getAllAncestors()
	{
		Vector<GeologicTimePeriod> ancestors = new Vector<GeologicTimePeriod>();
		GeologicTimePeriod parentNode = parent;
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
		setFullName(getFullName());
		for( GeologicTimePeriod child: getChildren() )
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

	public boolean isDescendantOf(GeologicTimePeriod node)
	{
		if( node==null )
		{
			throw new NullPointerException();
		}
		
		GeologicTimePeriod i = getParent();
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
	
	public Comparator<? super GeologicTimePeriod> getComparator()
	{
		return new GeologicTimePeriodComparator();
	}
}
