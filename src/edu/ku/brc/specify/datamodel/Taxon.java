package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class Taxon  implements java.io.Serializable,Treeable {

    // Fields    

     protected Integer taxonId;
     protected String taxonomicSerialNumber;
     protected String guid;
     protected String name;
     protected String remarks;
     protected String unitInd1;
     protected String unitName1;
     protected String unitInd2;
     protected String unitName2;
     protected String unitInd3;
     protected String unitName3;
     protected String unitInd4;
     protected String unitName4;
     protected String fullName;
     protected String commonName;
     protected String author;
     protected String source;
     protected Integer groupPermittedToView;
     protected String environmentalProtectionStatus;
     protected Integer nodeNumber;
     protected Integer highestChildNodeNumber;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected Short accepted;
     protected Integer rankId;
     protected String groupNumber;
     private Set<Taxon> acceptedChildren;
     private Taxon acceptedTaxon;
     private Set<TaxonCitation> taxonCitations;
     private TaxonTreeDef definition;
     private TaxonTreeDefItem definitionItem;
     private Taxon parent;
     private Set<ExternalResource> externalResources;
     protected Set<Taxon> children;

    // Constructors

    /** default constructor */
    public Taxon() {
    }
    
    /** constructor with id */
    public Taxon(Integer taxonId) {
        this.taxonId = taxonId;
    }
   
    // Initializer
    public void initialize()
    {
    	taxonId = null;
        taxonomicSerialNumber = null;
        guid = null;
        name = null;
        remarks = null;
        unitInd1 = null;
        unitName1 = null;
        unitInd2 = null;
        unitName2 = null;
        unitInd3 = null;
        unitName3 = null;
        unitInd4 = null;
        unitName4 = null;
        fullName = null;
        commonName = null;
        author = null;
        source = null;
        groupPermittedToView = null;
        environmentalProtectionStatus = null;
        nodeNumber = null;
        highestChildNodeNumber = null;
        timestampCreated = new Date();
        timestampModified = new Date();
        lastEditedBy = null;
        accepted = null;
        rankId = null;
        groupNumber = null;
        acceptedChildren = new HashSet<Taxon>();
        acceptedTaxon = null;
        taxonCitations = new HashSet<TaxonCitation>();
        definition = null;
        definitionItem = null;
        parent = null;
        externalResources = new HashSet<ExternalResource>();
        children = new HashSet<Taxon>();
    }
    // End Initializer   

    // Property accessors

    /**
     * 
     */
    public Integer getTaxonId() {
        return this.taxonId;
    }
    
    public void setTaxonId(Integer taxonId) {
        this.taxonId = taxonId;
    }

    /**
     * 
     */
    public String getTaxonomicSerialNumber() {
        return this.taxonomicSerialNumber;
    }
    
    public void setTaxonomicSerialNumber(String taxonomicSerialNumber) {
        this.taxonomicSerialNumber = taxonomicSerialNumber;
    }

    /**
     * 
     */
    public String getGuid() {
        return this.guid;
    }
    
    public void setGuid(String guid) {
        this.guid = guid;
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
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * 
     */
    public String getUnitInd1() {
        return this.unitInd1;
    }
    
    public void setUnitInd1(String unitInd1) {
        this.unitInd1 = unitInd1;
    }

    /**
     * 
     */
    public String getUnitName1() {
        return this.unitName1;
    }
    
    public void setUnitName1(String unitName1) {
        this.unitName1 = unitName1;
    }

    /**
     * 
     */
    public String getUnitInd2() {
        return this.unitInd2;
    }
    
    public void setUnitInd2(String unitInd2) {
        this.unitInd2 = unitInd2;
    }

    /**
     * 
     */
    public String getUnitName2() {
        return this.unitName2;
    }
    
    public void setUnitName2(String unitName2) {
        this.unitName2 = unitName2;
    }

    /**
     * 
     */
    public String getUnitInd3() {
        return this.unitInd3;
    }
    
    public void setUnitInd3(String unitInd3) {
        this.unitInd3 = unitInd3;
    }

    /**
     * 
     */
    public String getUnitName3() {
        return this.unitName3;
    }
    
    public void setUnitName3(String unitName3) {
        this.unitName3 = unitName3;
    }

    /**
     * 
     */
    public String getUnitInd4() {
        return this.unitInd4;
    }
    
    public void setUnitInd4(String unitInd4) {
        this.unitInd4 = unitInd4;
    }

    /**
     * 
     */
    public String getUnitName4() {
        return this.unitName4;
    }
    
    public void setUnitName4(String unitName4) {
        this.unitName4 = unitName4;
    }

    /**
     * 
     */
    public String getFullName() {
        return this.fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
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
    public String getAuthor() {
        return this.author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * 
     */
    public String getSource() {
        return this.source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * 
     */
    public Integer getGroupPermittedToView() {
        return this.groupPermittedToView;
    }
    
    public void setGroupPermittedToView(Integer groupPermittedToView) {
        this.groupPermittedToView = groupPermittedToView;
    }

    /**
     * 
     */
    public String getEnvironmentalProtectionStatus() {
        return this.environmentalProtectionStatus;
    }
    
    public void setEnvironmentalProtectionStatus(String environmentalProtectionStatus) {
        this.environmentalProtectionStatus = environmentalProtectionStatus;
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
    public String getLastEditedBy() {
        return this.lastEditedBy;
    }
    
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     * 
     */
    public Short getAccepted() {
        return this.accepted;
    }
    
    public void setAccepted(Short accepted) {
        this.accepted = accepted;
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
    public String getGroupNumber() {
        return this.groupNumber;
    }
    
    public void setGroupNumber(String groupNumber) {
        this.groupNumber = groupNumber;
    }

    /**
     * 
     */
    public Set<Taxon> getAcceptedChildren() {
        return this.acceptedChildren;
    }
    
    public void setAcceptedChildren(Set<Taxon> acceptedChildren) {
        this.acceptedChildren = acceptedChildren;
    }

    /**
     * 
     */
    public Taxon getAcceptedTaxon() {
        return this.acceptedTaxon;
    }
    
    public void setAcceptedTaxon(Taxon acceptedTaxon) {
        this.acceptedTaxon = acceptedTaxon;
    }

    /**
     * 
     */
    public Set<TaxonCitation> getTaxonCitations() {
        return this.taxonCitations;
    }
    
    public void setTaxonCitations(Set<TaxonCitation> taxonCitations) {
        this.taxonCitations = taxonCitations;
    }

    /**
     * 
     */
    public TaxonTreeDef getDefinition() {
        return this.definition;
    }
    
    public void setDefinition(TaxonTreeDef definition) {
        this.definition = definition;
    }

    /**
     * 
     */
    public TaxonTreeDefItem getDefinitionItem() {
        return this.definitionItem;
    }
    
    public void setDefinitionItem(TaxonTreeDefItem definitionItem) {
        this.definitionItem = definitionItem;
    }

    /**
     * 
     */
    public Taxon getParent() {
        return this.parent;
    }
    
    public void setParent(Taxon parent) {
        this.parent = parent;
    }

    /**
     * 
     */
    public Set<ExternalResource> getExternalResources() {
        return this.externalResources;
    }
    
    public void setExternalResources(Set<ExternalResource> externalResources) {
        this.externalResources = externalResources;
    }

    /**
     * 
     */
    public Set<Taxon> getChildren() {
        return this.children;
    }
    
    public void setChildren(Set<Taxon> children) {
        this.children = children;
    }

	/* Code added in order to implement Treeable */
    
	public Integer getTreeId()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void setTreeId(Integer id)
	{
		// TODO Auto-generated method stub
		
	}
    
	/**
	 * @return the parent Taxon object
	 */
	public Treeable getParentNode()
	{
		return getParent();
	}
	
	/**
	 * @param parent the new parent Taxon object
	 *
	 * @throws IllegalArgumentException if treeDef is not instance of Taxon
	 */
	public void setParentNode(Treeable parent)
	{
	    if( parent == null )
	    {
	        setParent(null);
	        return;
	    }
	    
		if( !(parent instanceof Taxon) )
		{
			throw new IllegalArgumentException("Argument must be an instance of Taxon");
		}
		setParent((Taxon)parent);
	}
	
	/**
	 * @return the parent TaxonTreeDef object
	 */
	public TreeDefinitionIface getTreeDef()
	{
		return getDefinition();
	}
	
	/**
	 * @param treeDef the new TaxonTreeDef object
	 *
	 * @throws IllegalArgumentException if treeDef is not instance of TaxonTreeDef
	 */
	public void setTreeDef(TreeDefinitionIface treeDef)
	{
		if( !(treeDef instanceof TaxonTreeDef) )
		{
			throw new IllegalArgumentException("Argument must be an instance of TaxonTreeDef");
		}
		
		setDefinition((TaxonTreeDef)treeDef);
	}
	
	/**
	 *
	 */
	public TreeDefinitionItemIface getDefItem()
	{
		return getDefinitionItem();
	}
	
	/**
	 * @param defItem the new TaxonTreeDefItem object representing this items level
	 *
	 * @throws IllegalArgumentException if defItem is not instance of TaxonTreeDefItem
	 */
	public void setDefItem(TreeDefinitionItemIface defItem)
	{
		if( !(defItem instanceof TaxonTreeDefItem) )
		{
			throw new IllegalArgumentException("Argument must be an instance of TaxonTreeDefItem");
		}
		
		setDefinitionItem((TaxonTreeDefItem)defItem);
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
		if( !(child instanceof Taxon) )
		{
			throw new IllegalArgumentException("New child must be an instance of " + getClass().getName());
		}
		
		addChild((Taxon)child);
	}
	
	public void removeChild( Treeable child )
	{
		if( !(child instanceof Taxon) )
		{
			throw new IllegalArgumentException("Child must be an instance of " + getClass().getName());
		}

		removeChild((Taxon)child);
	}

	public void addChild( Taxon child )
	{
		Taxon oldParent = child.getParent();
		if( oldParent != null )
		{
			oldParent.removeChild(child);
		}
		
		children.add(child);
		child.setParentNode(this);
	}
	
	public void removeChild( Taxon child )
	{
		children.remove(child);
		child.setParentNode(null);
	}

	public void addAcceptedChild( Taxon child )
	{
		acceptedChildren.add(child);
		child.setAcceptedTaxon(this);
	}
	
	public void removeAcceptedChild( Taxon child )
	{
		acceptedChildren.remove(child);
		child.setAcceptedTaxon(null);
	}

    // Add Methods

    public void addTaxonCitations(final TaxonCitation taxonCitation)
    {
        this.taxonCitations.add(taxonCitation);
        taxonCitation.setTaxon(this);
    }

    public void addExternalResources(final ExternalResource externalResource)
    {
        this.externalResources.add(externalResource);
        externalResource.getTaxonomy().add(this);
    }

    // Done Add Methods

    // Delete Methods

    public void removeTaxonCitations(final TaxonCitation taxonCitation)
    {
        this.taxonCitations.remove(taxonCitation);
        taxonCitation.setTaxon(null);
    }

    public void removeExternalResources(final ExternalResource externalResource)
    {
        this.externalResources.remove(externalResource);
        externalResource.getTaxonomy().remove(this);
    }

    // Delete Add Methods
}
