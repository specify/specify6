package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Geography  implements java.io.Serializable,Treeable {

    // Fields    

     protected Integer treeId;
     protected String name;
     protected String commonName;
     protected String geographyCode;
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
     protected Boolean isCurrent;
     protected Set<Locality> localities;
     protected GeographyTreeDef definition;
     protected GeographyTreeDefItem definitionItem;
     protected Geography parent;
     protected Set<Geography> children;

    // Constructors

    /** default constructor */
    public Geography() {
    }
    
    /** constructor with id */
    public Geography(Integer treeId) {
        this.treeId = treeId;
    }
   
    // Initializer
    public void initialize()
    {
        treeId = null;
        name = null;
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
        timestampCreated = new Date();
        timestampModified = new Date();
        timestampVersion = null;
        lastEditedBy = null;
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
    public Integer getTreeId() {
        return this.treeId;
    }
    
    public void setTreeId(Integer treeId) {
        this.treeId = treeId;
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
    public String getCommonName() {
        return this.commonName;
    }
    
    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    /**
     * 
     */
    public String getGeographyCode() {
        return this.geographyCode;
    }
    
    public void setGeographyCode(String geographyCode) {
        this.geographyCode = geographyCode;
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
    public Boolean getIsCurrent() {
        return this.isCurrent;
    }
    
    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    /**
     * 
     */
    public Set<Locality> getLocalities() {
        return this.localities;
    }
    
    public void setLocalities(Set<Locality> localities) {
        this.localities = localities;
    }

    /**
     * 
     */
    public GeographyTreeDef getDefinition() {
        return this.definition;
    }
    
    public void setDefinition(GeographyTreeDef definition) {
        this.definition = definition;
    }

    /**
     * 
     */
    public GeographyTreeDefItem getDefinitionItem() {
        return this.definitionItem;
    }
    
    public void setDefinitionItem(GeographyTreeDefItem definitionItem) {
        this.definitionItem = definitionItem;
    }

    /**
     * 
     */
    public Geography getParent() {
        return this.parent;
    }
    
    public void setParent(Geography parent) {
        this.parent = parent;
    }

    /**
     * 
     */
    public Set<Geography> getChildren() {
        return this.children;
    }
    
    public void setChildren(Set<Geography> children) {
        this.children = children;
    }
    
    
	/* Code added in order to implement Treeable */
    
	/**
	 * @return the parent Geography object
	 */
	public Treeable getParentNode()
	{
		return getParent();
	}
	
	/**
	 * @param parent the new parent Geography object
	 *
	 * @throws IllegalArgumentException if treeDef is not instance of Geography
	 */
	public void setParentNode(Treeable parent)
	{
	    if( parent == null )
	    {
	        setParent(null);
	        return;
	    }
	    
		if( !(parent instanceof Geography) )
		{
			throw new IllegalArgumentException("Argument must be an instance of Geography");
		}
		setParent((Geography)parent);
	}
	
	/**
	 * @return the parent GeographyTreeDef object
	 */
	public TreeDefinitionIface getTreeDef()
	{
		return getDefinition();
	}
	
	/**
	 * @param treeDef the new GeographyTreeDef object
	 *
	 * @throws IllegalArgumentException if treeDef is not instance of GeographyTreeDef
	 */
	public void setTreeDef(TreeDefinitionIface treeDef)
	{
		if( !(treeDef instanceof GeographyTreeDef) )
		{
			throw new IllegalArgumentException("Argument must be an instance of GeographyTreeDef");
		}
		
		setDefinition((GeographyTreeDef)treeDef);
	}
	
	/**
	 *
	 */
	public TreeDefinitionItemIface getDefItem()
	{
		return getDefinitionItem();
	}
	
	/**
	 * @param defItem the new GeographyTreeDefItem object representing this items level
	 *
	 * @throws IllegalArgumentException if defItem is not instance of GeographyTreeDefItem
	 */
	public void setDefItem(TreeDefinitionItemIface defItem)
	{
		if( !(defItem instanceof GeographyTreeDefItem) )
		{
			throw new IllegalArgumentException("Argument must be an instance of GeographyTreeDefItem");
		}
		
		setDefinitionItem((GeographyTreeDefItem)defItem);
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

	public void addChildNode(Treeable child)
	{
		if( !(child instanceof Geography) )
		{
			throw new IllegalArgumentException("Argument must be an instance of Geography");
		}
		children.add((Geography)child);
	}

    // Add Methods

    public void addLocalities(final Locality localities)
    {
        this.localities.add(localities);
        localities.setGeography(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeLocalities(final Locality localities)
    {
        this.localities.remove(localities);
        localities.setGeography(null);
    }

     // Delete Add Methods
}
