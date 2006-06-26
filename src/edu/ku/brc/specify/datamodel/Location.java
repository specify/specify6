package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class Location  implements java.io.Serializable,Treeable {

    // Fields    

     protected Integer locationId;
     protected String name;
     protected String fullName;
     protected String remarks;
     protected Integer rankId;
     protected Integer nodeNumber;
     protected Integer highestChildNodeNumber;
     protected String abbrev;
     protected String text1;
     protected String text2;
     protected Integer number1;
     protected Integer number2;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected Date timestampVersion;
     protected String lastEditedBy;
     private LocationTreeDef definition;
     private LocationTreeDefItem definitionItem;
     private Location parent;
     protected Set<Preparation> preparations;
     protected Set<Container> containers;
     protected Set<Location> children;

    // Constructors

    /** default constructor */
    public Location() {
    }
    
    /** constructor with id */
    public Location(Integer locationId) {
        this.locationId = locationId;
    }
   
    // Initializer
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
        timestampModified = new Date();
        timestampVersion = null;
        lastEditedBy = null;
        definition = null;
        definitionItem = null;
        parent = null;
        preparations = new HashSet<Preparation>();
        containers = new HashSet<Container>();
        children = new HashSet<Location>();
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Integer getLocationId() {
        return this.locationId;
    }
    
    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
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
	 * @return the fullname
	 */
	public String getFullName()
	{
		return fullName;
	}

	/**
	 * @param fullname the fullname to set
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
    public String getAbbrev() {
        return this.abbrev;
    }
    
    public void setAbbrev(String abbrev) {
        this.abbrev = abbrev;
    }

    /**
     * 
     */
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     * 
     */
    public String getText2() {
        return this.text2;
    }
    
    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     * 
     */
    public Integer getNumber1() {
        return this.number1;
    }
    
    public void setNumber1(Integer number1) {
        this.number1 = number1;
    }

    /**
     * 
     */
    public Integer getNumber2() {
        return this.number2;
    }
    
    public void setNumber2(Integer number2) {
        this.number2 = number2;
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
    public Date getTimestampModified() {
        return this.timestampModified;
    }
    
    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
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
    public LocationTreeDef getDefinition() {
        return this.definition;
    }
    
    public void setDefinition(LocationTreeDef definition) {
        this.definition = definition;
    }

    /**
     *
     */
    public LocationTreeDefItem getDefinitionItem() {
        return this.definitionItem;
    }
    
    public void setDefinitionItem(LocationTreeDefItem definitionItem) {
        this.definitionItem = definitionItem;
    }

    /**
     * 
     */
    public Location getParent() {
        return this.parent;
    }
    
    public void setParent(Location parent) {
        this.parent = parent;
    }

    /**
     * 
     */
    public Set<Preparation> getPreparations() {
        return this.preparations;
    }
    
    public void setPreparations(Set<Preparation> preparations) {
        this.preparations = preparations;
    }

    /**
     * 
     */
    public Set<Container> getContainers() {
        return this.containers;
    }
    
    public void setContainers(Set<Container> containers) {
        this.containers = containers;
    }

    /**
     * 
     */
    public Set<Location> getChildren() {
        return this.children;
    }
    
    public void setChildren(Set<Location> children) {
        this.children = children;
    }


	/* Code added in order to implement Treeable */
    
	public Integer getTreeId()
	{
		return getLocationId();
	}

	public void setTreeId(Integer id)
	{
		setLocationId(id);
	}

	/**
	 * @return the parent Location object
	 */
	public Treeable getParentNode()
	{
		return getParent();
	}
	
	/**
	 * @param parent the new parent Location object
	 *
	 * @throws IllegalArgumentException if treeDef is not instance of Location
	 */
	public void setParentNode(Treeable parent)
	{
	    if( parent == null )
	    {
	        setParent(null);
	        return;
	    }
	    
		if( !(parent instanceof Location) )
		{
			throw new IllegalArgumentException("Argument must be an instance of Location");
		}
		setParent((Location)parent);
	}
	
	/**
	 * @return the parent LocationTreeDef object
	 */
	public TreeDefinitionIface getTreeDef()
	{
		return getDefinition();
	}
	
	/**
	 * @param treeDef the new LocationTreeDef object
	 *
	 * @throws IllegalArgumentException if treeDef is not instance of LocationTreeDef
	 */
	public void setTreeDef(TreeDefinitionIface treeDef)
	{
		if( !(treeDef instanceof LocationTreeDef) )
		{
			throw new IllegalArgumentException("Argument must be an instance of LocationTreeDef");
		}
		
		setDefinition((LocationTreeDef)treeDef);
	}
	
	/**
	 *
	 */
	public TreeDefinitionItemIface getDefItem()
	{
		return getDefinitionItem();
	}
	
	/**
	 * @param defItem the new LocationTreeDefItem object representing this items level
	 *
	 * @throws IllegalArgumentException if defItem is not instance of LocationTreeDefItem
	 */
	public void setDefItem(TreeDefinitionItemIface defItem)
	{
		if( !(defItem instanceof LocationTreeDefItem) )
		{
			throw new IllegalArgumentException("Argument must be an instance of LocationTreeDefItem");
		}
		
		setDefinitionItem((LocationTreeDefItem)defItem);
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
		if( !(child instanceof Location) )
		{
			throw new IllegalArgumentException("New child must be an instance of " + getClass().getName());
		}
		
		addChild((Location)child);
	}
	
	public void removeChild( Treeable child )
	{
		if( !(child instanceof Location) )
		{
			throw new IllegalArgumentException("Child must be an instance of " + getClass().getName());
		}

		removeChild((Location)child);
	}

	public void addChild( Location child )
	{
		Location oldParent = child.getParent();
		if( oldParent != null )
		{
			oldParent.removeChild(child);
		}

		this.children.add((Location)child);
		child.setParentNode(this);
	}
	
	public void removeChild( Location child )
	{
		children.remove(child);
		child.setParentNode(null);
	}

    // Add Methods

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

    // Done Add Methods

    // Delete Methods

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

    // Delete Add Methods
    
    public String toString()
    {
    	String parentName = getParent() != null ? getParent().getName() : "none";
    	return "Location " + locationId + ": " + name + ", child of " + parentName + ", " + rankId + ", " + nodeNumber + ", " + highestChildNodeNumber;
    }
}
