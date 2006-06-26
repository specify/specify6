package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class GeologicTimePeriod  implements java.io.Serializable,Treeable {

    // Fields    

     protected Integer geologicTimePeriodId;
     protected Integer rankId;
     protected String name;
     protected String fullName;
     protected String remarks;
     protected Integer nodeNumber;
     protected Integer highestChildNodeNumber;
     protected String standard;
     protected Float start;
     protected Float startUncertainty;
     protected Float end;
     protected Float endUncertainty;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected Date timestampVersion;
     protected String lastEditedBy;
     private GeologicTimePeriodTreeDef definition;
     private GeologicTimePeriodTreeDefItem definitionItem;
     private GeologicTimePeriod parent;
     protected Set<GeologicTimePeriod> children;
     protected Set<Stratigraphy> stratigraphies;

    // Constructors

    /** default constructor */
    public GeologicTimePeriod() {
    }
    
    /** constructor with id */
    public GeologicTimePeriod(Integer geologicTimePeriodId) {
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
        timestampModified = new Date();
        timestampCreated = new Date();
        timestampVersion = null;
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
    public Integer getGeologicTimePeriodId() {
        return this.geologicTimePeriodId;
    }
    
    public void setGeologicTimePeriodId(Integer geologicTimePeriodId) {
        this.geologicTimePeriodId = geologicTimePeriodId;
    }

    /**
     * 
     */
    public Integer getRankId() {
        return this.rankId;
    }
    
    public void setRankId(Integer rankId) {
        this.rankId = rankId;
    }

    /**
     * 
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
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
    public Integer getNodeNumber() {
        return this.nodeNumber;
    }
    
    public void setNodeNumber(Integer nodeNumber) {
        this.nodeNumber = nodeNumber;
    }

    /**
     * 
     */
    public Integer getHighestChildNodeNumber() {
        return this.highestChildNodeNumber;
    }
    
    public void setHighestChildNodeNumber(Integer highestChildNodeNumber) {
        this.highestChildNodeNumber = highestChildNodeNumber;
    }

    /**
     * 
     */
    public String getStandard() {
        return this.standard;
    }
    
    public void setStandard(String standard) {
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
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * 
     */
    public Date getTimestampModified() {
        return this.timestampModified;
    }
    
    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
    }

    /**
     * 
     */
    public Date getTimestampCreated() {
        return this.timestampCreated;
    }
    
    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    /**
     * 
     */
    public Date getTimestampVersion() {
        return this.timestampVersion;
    }
    
    public void setTimestampVersion(Date timestampVersion) {
        this.timestampVersion = timestampVersion;
    }

    /**
     * 
     */
    public String getLastEditedBy() {
        return this.lastEditedBy;
    }
    
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     * 
     */
    public GeologicTimePeriodTreeDef getDefinition() {
        return this.definition;
    }
    
    public void setDefinition(GeologicTimePeriodTreeDef definition) {
        this.definition = definition;
    }

    /**
     * 
     */
    public GeologicTimePeriodTreeDefItem getDefinitionItem() {
        return this.definitionItem;
    }
    
    public void setDefinitionItem(GeologicTimePeriodTreeDefItem definitionItem) {
        this.definitionItem = definitionItem;
    }

    /**
     * 
     */
    public GeologicTimePeriod getParent() {
        return this.parent;
    }
    
    public void setParent(GeologicTimePeriod parent) {
        this.parent = parent;
    }

    /**
     * 
     */
    public Set<GeologicTimePeriod> getChildren() {
        return this.children;
    }
    
    public void setChildren(Set<GeologicTimePeriod> children) {
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

	public Integer getTreeId()
	{
		return getGeologicTimePeriodId();
	}

	public void setTreeId(Integer id)
	{
		setGeologicTimePeriodId(id);
	}
	
	/**
	 * @return the parent GeologicTimePeriod object
	 */
	public Treeable getParentNode()
	{
		return getParent();
	}
	
	/**
	 * @param parent the new parent GeologicTimePeriod object
	 *
	 * @throws IllegalArgumentException if treeDef is not instance of GeologicTimePeriod
	 */
	public void setParentNode(Treeable parent)
	{
	    if( parent == null )
	    {
	        setParent(null);
	        return;
	    }
	    
		if( !(parent instanceof GeologicTimePeriod) )
		{
			throw new IllegalArgumentException("Argument must be an instance of GeologicTimePeriod");
		}
		setParent((GeologicTimePeriod)parent);
	}
	
	/**
	 * @return the parent GeologicTimePeriodTreeDef object
	 */
	public TreeDefinitionIface getTreeDef()
	{
		return getDefinition();
	}
	
	/**
	 * @param treeDef the new GeologicTimePeriodTreeDef object
	 *
	 * @throws IllegalArgumentException if treeDef is not instance of GeologicTimePeriodTreeDef
	 */
	public void setTreeDef(TreeDefinitionIface treeDef)
	{
		if( !(treeDef instanceof GeologicTimePeriodTreeDef) )
		{
			throw new IllegalArgumentException("Argument must be an instance of GeologicTimePeriodTreeDef");
		}
		
		setDefinition((GeologicTimePeriodTreeDef)treeDef);
	}
	
	/**
	 *
	 */
	public TreeDefinitionItemIface getDefItem()
	{
		return getDefinitionItem();
	}
	
	/**
	 * @param defItem the new GeologicTimePeriodTreeDefItem object representing this items level
	 *
	 * @throws IllegalArgumentException if defItem is not instance of GeologicTimePeriodTreeDefItem
	 */
	public void setDefItem(TreeDefinitionItemIface defItem)
	{
		if( !(defItem instanceof GeologicTimePeriodTreeDefItem) )
		{
			throw new IllegalArgumentException("Argument must be an instance of GeologicTimePeriodTreeDefItem");
		}
		
		setDefinitionItem((GeologicTimePeriodTreeDefItem)defItem);
	}

	@SuppressWarnings("unchecked")
	public Set getChildNodes()
	{
		return children;
	}

	@SuppressWarnings("unchecked")
	public void setChildNodes(Set children)
	{
		setChildren(children);
	}

	public void addChild( Treeable child )
	{
		if( !(child instanceof GeologicTimePeriod) )
		{
			throw new IllegalArgumentException("New child must be an instance of " + getClass().getName());
		}
		
		addChild((GeologicTimePeriod)child);
	}
	
	public void removeChild( Treeable child )
	{
		if( !(child instanceof GeologicTimePeriod) )
		{
			throw new IllegalArgumentException("Child must be an instance of " + getClass().getName());
		}

		removeChild((GeologicTimePeriod)child);
	}

	public void addChild( GeologicTimePeriod child )
	{
		GeologicTimePeriod oldParent = child.getParent();
		if( oldParent != null )
		{
			oldParent.removeChild(child);
		}
		
		children.add(child);
		child.setParentNode(this);
	}
	
	public void removeChild( GeologicTimePeriod child )
	{
		children.remove(child);
		child.setParentNode(null);
	}

	public void addStratigraphy( Stratigraphy strat )
	{
		GeologicTimePeriod oldGTP = strat.getGeologicTimePeriod();
		if( oldGTP != null )
		{
			oldGTP.removeStratigraphy(strat);
		}
		
		stratigraphies.add(strat);
		strat.setGeologicTimePeriod(this);
	}
	
	public void removeStratigraphy( Stratigraphy strat )
	{
		stratigraphies.remove(strat);
		strat.setGeologicTimePeriod(null);
	}

	// temporary implementation of toString() for easier debugging
	public String toString()
    {
		String parentName = getParent() != null ? getParent().getName() : "none";
    	return "GeologicTimePeriod " + geologicTimePeriodId + ": " + name + ", child of " + parentName + ", " + rankId + ", " + nodeNumber + ", " + highestChildNodeNumber;
    }
}
