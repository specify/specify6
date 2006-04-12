package edu.ku.brc.specify.datamodel;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class GeologicTimePeriod  implements java.io.Serializable,Treeable {

    // Fields    

     protected Integer treeId;
     protected Integer rankId;
     protected String name;
     protected Integer nodeNumber;
     protected Integer highestChildNodeNumber;
     protected String standard;
     protected Float age;
     protected Float ageUncertainty;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected Date timestampVersion;
     protected String lastEditedBy;
     private GeologicTimePeriodTreeDef definition;
     private GeologicTimePeriodTreeDefItem definitionItem;
     private GeologicTimePeriod parent;
     protected Set<GeologicTimePeriod> children;

    // Constructors

    /** default constructor */
    public GeologicTimePeriod() {
    }
    
    /** constructor with id */
    public GeologicTimePeriod(Integer treeId) {
        this.treeId = treeId;
    }
   
    // Initializer
    public void initialize()
    {
        treeId = null;
        rankId = null;
        name = null;
        nodeNumber = null;
        highestChildNodeNumber = null;
        standard = null;
        age = null;
        ageUncertainty = null;
        remarks = null;
        timestampModified = null;
        timestampCreated = Calendar.getInstance().getTime();
        timestampVersion = null;
        lastEditedBy = null;
        definition = null;
        definitionItem = null;
        parent = null;
        children = new HashSet<GeologicTimePeriod>();
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Integer getTreeId() {
        return this.treeId;
    }
    
    public void setTreeId(Integer treeId) {
        this.treeId = treeId;
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

    /**
     * 
     */
    public Float getAge() {
        return this.age;
    }
    
    public void setAge(Float age) {
        this.age = age;
    }

    /**
     * 
     */
    public Float getAgeUncertainty() {
        return this.ageUncertainty;
    }
    
    public void setAgeUncertainty(Float ageUncertainty) {
        this.ageUncertainty = ageUncertainty;
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

	/* Code added in order to implement Treeable */
    
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

	public Set getChildNodes()
	{
		return children;
	}

	public void setChildNodes(Set children)
	{
		//TODO: type checking: make sure children contains the correct types of objects
		setChildren(children);
	}

	public void addChildNode(Treeable child)
	{
		if( !(child instanceof GeologicTimePeriod) )
		{
			throw new IllegalArgumentException("Argument must be an instance of GeologicTimePeriod");
		}
		children.add((GeologicTimePeriod)child);
	}
}