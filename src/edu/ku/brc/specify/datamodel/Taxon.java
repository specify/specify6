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
import edu.ku.brc.ui.forms.FormDataObjIFace;

@SuppressWarnings("serial")
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "taxon")
@org.hibernate.annotations.Table(appliesTo="taxon", indexes =
    {   @Index (name="TaxonGuidIDX", columnNames={"Guid"}),
        @Index (name="TaxonomicSerialNumberIDX", columnNames={"TaxonomicSerialNumber"}),
        @Index (name="TaxonCommonNameIDX", columnNames={"CommonName"}),
        @Index (name="TaxonNameIDX", columnNames={"Name"}),
        @Index (name="TaxonFullNameIDX", columnNames={"FullName"})
    })
public class Taxon extends DataModelObjBase implements Serializable, Treeable<Taxon,TaxonTreeDef,TaxonTreeDefItem>
{
    /**
     * A <code>Logger</code> object used for all log messages emanating from
     * this class.
     */
    protected static final Logger log = Logger.getLogger(Taxon.class);

    // ID
	protected Long                 taxonId;
	
    // names
	protected String               name;
    protected String               fullName;
    protected String               commonName;
    
    // scientific identifiers
	protected String               taxonomicSerialNumber;
	protected String               guid;
	
	// ITIS fields
	protected String               unitInd1;
	protected String               unitName1;
	protected String               unitInd2;
	protected String               unitName2;
	protected String               unitInd3;
	protected String               unitName3;
	protected String               unitInd4;
	protected String               unitName4;
	
	// reference info
	protected String               author;
	protected String               source;
	
    protected String               remarks;
	protected String               environmentalProtectionStatus;
	
    // for hybrid support
    protected Boolean              isHybrid;
    protected Taxon                hybridParent1;
    protected Taxon                hybridParent2;
    protected Set<Taxon>           hybridChildren1;
    protected Set<Taxon>           hybridChildren2;

    // for synonym support
    protected Boolean              isAccepted;
    protected Taxon                acceptedTaxon;
    protected Set<Taxon>           acceptedChildren;

    // tree structure fields
    protected Taxon                parent;
    protected Set<Taxon>           children;
    protected TaxonTreeDef         definition;
    protected TaxonTreeDefItem     definitionItem;
    
    // relationships with other tables
	protected Set<Determination>   determinations;
	protected Set<TaxonCitation>   taxonCitations;
	protected Set<Attachment>      attachments;

    // non-user fields
    protected Integer              nodeNumber;
    protected Integer              highestChildNodeNumber;
    protected Integer              rankId;
    protected String               groupNumber;
    protected Integer              visibility;
    protected String               visibilitySetBy;
    
	/** default constructor */
	public Taxon()
	{
		// do nothing
	}

	/** constructor with id */
	public Taxon(Long taxonId)
	{
		this.taxonId = taxonId;
	}

	public Taxon(String name)
	{
		initialize();
		this.name = name;
	}

	public Taxon(String name, Taxon parent, int rank)
	{
		this.name = name;
		this.parent = parent;
		this.rankId = rank;
	}

	// Initializer
	@Override
    public void initialize()
	{
        super.init();
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
		environmentalProtectionStatus = null;
		nodeNumber = null;
		highestChildNodeNumber = null;
		isAccepted = null;
		rankId = null;
		groupNumber = null;
        visibility = null;
        hybridParent1 = null;
        hybridParent2 = null;
        hybridChildren1 = new HashSet<Taxon>();
        hybridChildren2 = new HashSet<Taxon>();
		acceptedChildren = new HashSet<Taxon>();
		determinations = new HashSet<Determination>();
		acceptedTaxon = null;
		taxonCitations = new HashSet<TaxonCitation>();
		definition = null;
		definitionItem = null;
		parent = null;
		attachments = new HashSet<Attachment>();
		children = new HashSet<Taxon>();
	}

    @Id
    @GeneratedValue
    @Column(name = "TaxonID", unique = false, nullable = false, insertable = true, updatable = true)
	public Long getTaxonId()
	{
		return this.taxonId;
	}

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Override
    @Transient
    public Long getId()
    {
        return this.taxonId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    @Transient
    public String getIdentityTitle()
    {
        String title = ((fullName != null) && (fullName.length() > 0)) ? fullName : name;
        return ((title != null) && (title.length() > 0)) ? title : super.getIdentityTitle();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Taxon.class;
    }

	public void setTaxonId(Long taxonId)
	{
		this.taxonId = taxonId;
	}

    @Column(name = "Name", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Column(name = "FullName", unique = false, nullable = true, insertable = true, updatable = true)
    public String getFullName()
    {
        return this.fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    @Column(name = "CommonName", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getCommonName()
    {
        return this.commonName;
    }

    public void setCommonName(String commonName)
    {
        this.commonName = commonName;
    }

    @Column(name = "TaxonomicSerialNumber", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
	public String getTaxonomicSerialNumber()
	{
		return this.taxonomicSerialNumber;
	}

	public void setTaxonomicSerialNumber(String taxonomicSerialNumber)
	{
		this.taxonomicSerialNumber = taxonomicSerialNumber;
	}

    @Column(name = "GUID", unique = false, nullable = true, insertable = true, updatable = true)
	public String getGuid()
	{
		return this.guid;
	}

	public void setGuid(String guid)
	{
		this.guid = guid;
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

    @Column(name = "UnitInd1", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
	public String getUnitInd1()
	{
		return this.unitInd1;
	}

	public void setUnitInd1(String unitInd1)
	{
		this.unitInd1 = unitInd1;
	}

    @Column(name = "UnitName1", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
	public String getUnitName1()
	{
		return this.unitName1;
	}

	public void setUnitName1(String unitName1)
	{
		this.unitName1 = unitName1;
	}

    @Column(name = "UnitInd2", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
	public String getUnitInd2()
	{
		return this.unitInd2;
	}

	public void setUnitInd2(String unitInd2)
	{
		this.unitInd2 = unitInd2;
	}

    @Column(name = "UnitName2", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
	public String getUnitName2()
	{
		return this.unitName2;
	}

	public void setUnitName2(String unitName2)
	{
		this.unitName2 = unitName2;
	}

    @Column(name = "UnitInd3", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
	public String getUnitInd3()
	{
		return this.unitInd3;
	}

	public void setUnitInd3(String unitInd3)
	{
		this.unitInd3 = unitInd3;
	}

    @Column(name = "UnitName3", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
	public String getUnitName3()
	{
		return this.unitName3;
	}

	public void setUnitName3(String unitName3)
	{
		this.unitName3 = unitName3;
	}

    @Column(name = "UnitInd4", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
	public String getUnitInd4()
	{
		return this.unitInd4;
	}

	public void setUnitInd4(String unitInd4)
	{
		this.unitInd4 = unitInd4;
	}

    @Column(name = "UnitName4", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
	public String getUnitName4()
	{
		return this.unitName4;
	}

	public void setUnitName4(String unitName4)
	{
		this.unitName4 = unitName4;
	}

    @Column(name = "Author", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
	public String getAuthor()
	{
		return this.author;
	}

	public void setAuthor(String author)
	{
		this.author = author;
	}

    @Column(name = "Source", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
	public String getSource()
	{
		return this.source;
	}

	public void setSource(String source)
	{
		this.source = source;
	}

    @Column(name = "EnvironmentalProtectionStatus", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
	public String getEnvironmentalProtectionStatus()
	{
		return this.environmentalProtectionStatus;
	}

	public void setEnvironmentalProtectionStatus(String environmentalProtectionStatus)
	{
		this.environmentalProtectionStatus = environmentalProtectionStatus;
	}

    @Column(name = "NodeNumber", unique = false, nullable = true, insertable = true, updatable = false, length = 10)
	public Integer getNodeNumber()
	{
		return this.nodeNumber;
	}

	public void setNodeNumber(Integer nodeNumber)
	{
		this.nodeNumber = nodeNumber;
	}

    @Column(name = "HighestChildNodeNumber", unique = false, nullable = true, insertable = true, updatable = false, length = 10)
	public Integer getHighestChildNodeNumber()
	{
		return this.highestChildNodeNumber;
	}

	public void setHighestChildNodeNumber(Integer highestChildNodeNumber)
	{
		this.highestChildNodeNumber = highestChildNodeNumber;
	}

    @Column(name="IsAccepted", unique=false, nullable=true, insertable=true, updatable=true)
    public Boolean getIsAccepted()
	{
		return this.isAccepted;
	}

	public void setIsAccepted(Boolean accepted)
	{
		this.isAccepted = accepted;
	}

    @Column(name = "RankID", unique = false, nullable = true, insertable = true, updatable = true, length = 10)
	public Integer getRankId()
	{
		return this.rankId;
	}

	public void setRankId(Integer rankId)
	{
		this.rankId = rankId;
	}

    @Column(name = "GroupNumber", unique = false, nullable = true, insertable = true, updatable = true, length = 20)
	public String getGroupNumber()
	{
		return this.groupNumber;
	}

	public void setGroupNumber(String groupNumber)
	{
		this.groupNumber = groupNumber;
	}
    /**
     *      * Indicates whether this record can be viewed - by owner, by instituion, or by all
     */
    @Column(name = "Visibility", unique = false, nullable = true, insertable = true, updatable = true, length = 10)
    public Integer getVisibility() {
        return this.visibility;
    }
    
    public void setVisibility(Integer visibility) {
        this.visibility = visibility;
    }
    
    @Transient
    @Override
    public boolean isRestrictable()
    {
        return true;
    }
    
    @Column(name="IsHybrid", unique=false, nullable=true, insertable=true, updatable=true)
    public Boolean getIsHybrid()
    {
        return isHybrid;
    }

    public void setIsHybrid(Boolean isHybrid)
    {
        this.isHybrid = isHybrid;
    }

    /**
     * 
     */
    @Column(name = "VisibilitySetBy", unique = false, nullable = true, insertable = true, updatable = true, length = 50)
    public String getVisibilitySetBy() {
        return this.visibilitySetBy;
    }
    
    public void setVisibilitySetBy(String visibilitySetBy) {
        this.visibilitySetBy = visibilitySetBy;
    }
    
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "acceptedTaxon")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
	public Set<Taxon> getAcceptedChildren()
	{
		return this.acceptedChildren;
	}

	public void setAcceptedChildren(Set<Taxon> acceptedChildren)
	{
		this.acceptedChildren = acceptedChildren;
	}

    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "AcceptedID", unique = false, nullable = true, insertable = true, updatable = true)
	public Taxon getAcceptedTaxon()
	{
		return this.acceptedTaxon;
	}

	public void setAcceptedTaxon(Taxon acceptedTaxon)
	{
		this.acceptedTaxon = acceptedTaxon;
	}

    /**
     * If this object represents a hybrid taxon, this returns the primary parent of the taxon.
     * 
     * @returnthe the primary parent of the taxon, or null if the object doesn't represent a hybrid taxon
     */
    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "HybridParent1ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Taxon getHybridParent1()
    {
        return hybridParent1;
    }

    public void setHybridParent1(Taxon hybridParent1)
    {
        this.hybridParent1 = hybridParent1;
    }

    /**
     * If this object represents a hybrid taxon, this returns the secondary parent of the taxon.
     * 
     * @return the the secondary parent of the taxon, or null if the object doesn't represent a hybrid taxon
     */
    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "HybridParent2ID", unique = false, nullable = true, insertable = true, updatable = true)
    public Taxon getHybridParent2()
    {
        return hybridParent2;
    }

    public void setHybridParent2(Taxon hybridParent2)
    {
        this.hybridParent2 = hybridParent2;
    }

    /**
     * Returns the set of Taxon objects where this object is the hybridParent1 value.
     * 
     * @return the set of Taxon objects where this object is the hybridParent1 value.
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "hybridParent1")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<Taxon> getHybridChildren1()
    {
        return hybridChildren1;
    }

    public void setHybridChildren1(Set<Taxon> hybridChildren1)
    {
        this.hybridChildren1 = hybridChildren1;
    }

    /**
     * Returns the set of Taxon objects where this object is the hybridParent2 value.
     * 
     * @return the set of Taxon objects where this object is the hybridParent2 value.
     */
    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "hybridParent2")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<Taxon> getHybridChildren2()
    {
        return hybridChildren2;
    }

    public void setHybridChildren2(Set<Taxon> hybridChildren2)
    {
        this.hybridChildren2 = hybridChildren2;
    }

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "taxon")
	public Set<Determination> getDeterminations()
	{
		return determinations;
	}

	public void setDeterminations(Set<Determination> determinations)
	{
		this.determinations = determinations;
	}

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "taxon")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
	public Set<TaxonCitation> getTaxonCitations()
	{
		return this.taxonCitations;
	}

	public void setTaxonCitations(Set<TaxonCitation> taxonCitations)
	{
		this.taxonCitations = taxonCitations;
	}

    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "TaxonTreeDefID", unique = false, nullable = false, insertable = true, updatable = true)
	public TaxonTreeDef getDefinition()
	{
		return this.definition;
	}

	public void setDefinition(TaxonTreeDef definition)
	{
		this.definition = definition;
	}

    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "TaxonTreeDefItemID", unique = false, nullable = false, insertable = true, updatable = true)
	public TaxonTreeDefItem getDefinitionItem()
	{
		return this.definitionItem;
	}

	public void setDefinitionItem(TaxonTreeDefItem definitionItem)
	{
        this.definitionItem = definitionItem;
        if (definitionItem!=null && definitionItem.getRankId()!=null)
        {
            this.rankId = this.definitionItem.getRankId();
        }
	}

    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    @JoinColumn(name = "ParentID", unique = false, nullable = true, insertable = true, updatable = true)
	public Taxon getParent()
	{
		return this.parent;
	}

	public void setParent(Taxon parent)
	{
		this.parent = parent;
	}


    @OneToMany(cascade = { javax.persistence.CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "parent")
	public Set<Taxon> getChildren()
	{
		return this.children;
	}

	public void setChildren(Set<Taxon> children)
	{
		this.children = children;
	}

    @Transient
	public Long getTreeId()
	{
		return this.taxonId;
	}

	public void setTreeId(Long id)
	{
        this.taxonId = id;
	}

    @OneToMany(cascade = {}, fetch = FetchType.LAZY, mappedBy = "taxon")
    @Cascade( { CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK })
    public Set<Attachment> getAttachments()
    {
        return attachments;
    }

    public void setAttachments(Set<Attachment> attachments)
    {
        this.attachments = attachments;
    }
    
    public void addChild(Taxon child)
	{
		Taxon oldParent = child.getParent();
		if( oldParent!=null )
		{
			oldParent.removeChild(child);
		}

		children.add(child);
		child.setParent(this);
	}

	public void removeChild(Taxon child)
	{
		children.remove(child);
		child.setParent(null);
	}

	public void addAcceptedChild(Taxon child)
	{
		acceptedChildren.add(child);
		child.setAcceptedTaxon(this);
	}

	public void removeAcceptedChild(Taxon child)
	{
		acceptedChildren.remove(child);
		child.setAcceptedTaxon(null);
	}

	public void addTaxonCitations(final TaxonCitation taxonCitation)
	{
		this.taxonCitations.add(taxonCitation);
		taxonCitation.setTaxon(this);
	}

	public void addDetermination(final Determination determination)
	{
		determinations.add(determination);
		determination.setTaxon(this);
	}

	public void removeDetermination(final Determination determination)
	{
		determinations.remove(determination);
		determination.setTaxon(null);
	}

	public void removeTaxonCitations(final TaxonCitation taxonCitation)
	{
		this.taxonCitations.remove(taxonCitation);
		taxonCitation.setTaxon(null);
	}
    
    public void addAttachment(Attachment attachment)
    {
        this.attachments.add(attachment);
        attachment.setTaxon(this);
    }
    
    public void removeAttachment(Attachment attachment)
    {
        this.attachments.remove(attachment);
        attachment.setTaxon(null);
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

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.DataModelObjBase#addReference(edu.ku.brc.ui.forms.FormDataObjIFace, java.lang.String)
	 */
	@Override
    public void addReference(FormDataObjIFace ref, String type)
    {
        if (type.equals("child") && ref instanceof Taxon)
        {
            addChild((Taxon)ref);
            return;
        }
        
        if (type.equals("accepted child") && ref instanceof Taxon)
        {
            addAcceptedChild((Taxon)ref);
            return;
        }
        
        if (ref instanceof Determination)
        {
            addDetermination((Determination)ref);
            return;
        }
        
        log.error("Unfinished implementation");
        super.addReference(ref, type);
        //TODO: finish this impl
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
        Vector<Taxon> parts = new Vector<Taxon>();
        parts.add(this);
        Taxon node = getParent();
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
                    Taxon part = parts.get(j);
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
                    Taxon part = parts.get(j);
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
		for( Taxon child: getChildren() )
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
	public List<Taxon> getAllDescendants()
	{
		Vector<Taxon> descendants = new Vector<Taxon>();
		for( Taxon child: getChildren() )
		{
			descendants.add(child);
			descendants.addAll(child.getAllDescendants());
		}
		return descendants;
	}
	
    @Transient
	public List<Taxon> getAllAncestors()
	{
		Vector<Taxon> ancestors = new Vector<Taxon>();
		Taxon parentNode = parent;
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
		for( Taxon child: getChildren() )
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

	public boolean isDescendantOf(Taxon node)
	{
		if( node==null )
		{
			throw new NullPointerException();
		}
		
		Taxon i = getParent();
		while( i != null )
		{
            if (i==node)
            {
                return true;
            }
            
			if( i.getId().longValue() == node.getId().longValue() )
			{
				return true;
			}
			
			i = i.getParent();
		}
		return false;
	}
	
    @Transient
	public Comparator<? super Taxon> getComparator()
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
        return 4;
    }

}
