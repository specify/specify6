/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.datamodel;

import java.io.Serializable;
import java.util.Comparator;
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
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.treeutils.TreeOrderSiblingComparator;

@SuppressWarnings("serial")
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "taxon")
@org.hibernate.annotations.Table(appliesTo="taxon", indexes =
    {   @Index (name="TaxonGuidIDX", columnNames={"GUID"}),
        @Index (name="TaxonomicSerialNumberIDX", columnNames={"TaxonomicSerialNumber"}),
        @Index (name="TaxonCommonNameIDX", columnNames={"CommonName"}),
        @Index (name="TaxonNameIDX", columnNames={"Name"}),
        @Index (name="TaxonFullNameIDX", columnNames={"FullName"})
    })
public class Taxon extends DataModelObjBase implements AttachmentOwnerIFace<TaxonAttachment>, 
                                                       Serializable, 
                                                       Treeable<Taxon,TaxonTreeDef,TaxonTreeDefItem>
{
    /**
     * A <code>Logger</code> object used for all log messages emanating from
     * this class.
     */ 
    protected static final Logger log = Logger.getLogger(Taxon.class);

    // ID
	protected Integer              taxonId;
	
    // names
	protected String               name;
    protected String               fullName;
    protected String               commonName;
    protected String               cultivarName;
    
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
	
    protected String               esaStatus;
    protected String               citesStatus;
    protected String               usfwsCode;
    protected String               isisNumber;
    protected String               ncbiTaxonNumber;
    
	// reference info
	protected String               author;
	protected String               source;
	
    protected String               remarks;
	protected String               environmentalProtectionStatus;
	protected String               labelFormat;
	
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
    
    protected String               text1;
    protected String               text2;
    protected Integer              number1;
    protected Integer              number2;
    
    // relationships with other tables
	protected Set<Determination>   determinations;
	protected Set<TaxonCitation>   taxonCitations;
    protected Set<CommonNameTx>    commonNames;

    // non-user fields
    protected Integer              nodeNumber;
    protected Integer              highestChildNodeNumber;
    protected Integer              rankId;
    protected String               groupNumber;
    protected Integer              visibility;
    protected String               visibilitySetBy;
    
    protected List<Taxon>          ancestors; 

    private Set<TaxonAttachment> taxonAttachments;
    
	/** default constructor */
	public Taxon()
	{
		// do nothing
	}

	/** constructor with id */
	public Taxon(Integer taxonId)
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
        taxonId                       = null;
        taxonomicSerialNumber         = null;
        guid                          = null;
        name                          = null;
        remarks                       = null;
        unitInd1                      = null;
        unitName1                     = null;
        unitInd2                      = null;
        unitName2                     = null;
        unitInd3                      = null;
        unitName3                     = null;
        unitInd4                      = null;
        unitName4                     = null;
        fullName                      = null;
        commonName                    = null;
        cultivarName                  = null;
        author                        = null;
        source                        = null;
        environmentalProtectionStatus = null;
        labelFormat                   = null;
        nodeNumber                    = null;
        highestChildNodeNumber        = null;
        isAccepted                    = true; // null for isAccepted means the same as true.  true is more clear.  So, I put true in here.
        rankId                        = null;
        groupNumber                   = null;
        visibility                    = null;
        hybridParent1                 = null;
        hybridParent2                 = null;
        hybridChildren1               = new HashSet<Taxon>();
        hybridChildren2               = new HashSet<Taxon>();
        acceptedChildren              = new HashSet<Taxon>();
        determinations                = new HashSet<Determination>();
        acceptedTaxon                 = null;
        taxonCitations                = new HashSet<TaxonCitation>();
        commonNames                   = new HashSet<CommonNameTx>();
        definition                    = null;
        definitionItem                = null;
        parent                        = null;
        children                      = new HashSet<Taxon>();
        ancestors                     = null;
        taxonAttachments              = new HashSet<TaxonAttachment>();
	}

    @Id
    @GeneratedValue
    @Column(name = "TaxonID")
	public Integer getTaxonId()
	{
		return this.taxonId;
	}

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Override
    @Transient
    public Integer getId()
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

	public void setTaxonId(Integer taxonId)
	{
		this.taxonId = taxonId;
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

    @Column(name = "FullName", length = 255)
    public String getFullName()
    {
        return this.fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    @Column(name = "CommonName", length = 128)
    public String getCommonName() 
    {
        return this.commonName;
    }

    public void setCommonName(String commonName)
    {
        this.commonName = commonName;
    }

    /**
     * @return the cultivarName
     */
    @Column(name = "CultivarName", length = 32)
    public String getCultivarName()
    {
        return cultivarName;
    }

    /**
     * @param cultivarName the cultivarName to set
     */
    public void setCultivarName(String cultivarName)
    {
        this.cultivarName = cultivarName;
    }

    @Column(name = "TaxonomicSerialNumber", length = 50)
	public String getTaxonomicSerialNumber()
	{
		return this.taxonomicSerialNumber;
	}

	public void setTaxonomicSerialNumber(String taxonomicSerialNumber)
	{
		this.taxonomicSerialNumber = taxonomicSerialNumber;
	}

    @Column(name = "GUID", length = 128)
	public String getGuid()
	{
		return this.guid;
	}

	public void setGuid(String guid)
	{
		this.guid = guid;
	}

    @Lob
    @Column(name = "Remarks", length = 4096)
	public String getRemarks()
	{
		return this.remarks;
	}

	public void setRemarks(String remarks)
	{
		this.remarks = remarks;
	}

    @Column(name = "UnitInd1", length = 50)
	public String getUnitInd1()
	{
		return this.unitInd1;
	}

	public void setUnitInd1(String unitInd1)
	{
		this.unitInd1 = unitInd1;
	}

    @Column(name = "UnitName1", length = 50)
	public String getUnitName1()
	{
		return this.unitName1;
	}

	public void setUnitName1(String unitName1)
	{
		this.unitName1 = unitName1;
	}

    @Column(name = "UnitInd2", length = 50)
	public String getUnitInd2()
	{
		return this.unitInd2;
	}

	public void setUnitInd2(String unitInd2)
	{
		this.unitInd2 = unitInd2;
	}

    @Column(name = "UnitName2", length = 50)
	public String getUnitName2()
	{
		return this.unitName2;
	}

	public void setUnitName2(String unitName2)
	{
		this.unitName2 = unitName2;
	}

    @Column(name = "UnitInd3", length = 50)
	public String getUnitInd3()
	{
		return this.unitInd3;
	}

	public void setUnitInd3(String unitInd3)
	{
		this.unitInd3 = unitInd3;
	}

    @Column(name = "UnitName3", length = 50)
	public String getUnitName3()
	{
		return this.unitName3;
	}

	public void setUnitName3(String unitName3)
	{
		this.unitName3 = unitName3;
	}

    @Column(name = "UnitInd4", length = 50)
	public String getUnitInd4()
	{
		return this.unitInd4;
	}

	public void setUnitInd4(String unitInd4)
	{
		this.unitInd4 = unitInd4;
	}

    @Column(name = "UnitName4", length = 50)
	public String getUnitName4()
	{
		return this.unitName4;
	}

	public void setUnitName4(String unitName4)
	{
		this.unitName4 = unitName4;
	}

    @Column(name = "Author", length = 128)
	public String getAuthor()
	{
		return this.author;
	}

	public void setAuthor(String author)
	{
		this.author = author;
	}

    @Column(name = "Source", length = 64)
	public String getSource()
	{
		return this.source;
	}

	public void setSource(String source)
	{
		this.source = source;
	}

    @Column(name = "EnvironmentalProtectionStatus", length = 64)
	public String getEnvironmentalProtectionStatus()
	{
		return this.environmentalProtectionStatus;
	}

	public void setEnvironmentalProtectionStatus(String environmentalProtectionStatus)
	{
		this.environmentalProtectionStatus = environmentalProtectionStatus;
	}

    /**
     * @return the labelFormat
     */
    @Column(name = "LabelFormat", length = 64)
    public String getLabelFormat()
    {
        return labelFormat;
    }

    /**
     * @param labelFormat the labelFormat to set
     */
    public void setLabelFormat(String labelFormat)
    {
        this.labelFormat = labelFormat;
    }

    @Column(name="CitesStatus", length = 32)
    public String getCitesStatus()
    {
        return citesStatus;
    }

    public void setCitesStatus(String citesStatus)
    {
        this.citesStatus = citesStatus;
    }

    @Column(name="EsaStatus", length = 64)
    public String getEsaStatus()
    {
        return esaStatus;
    }

    public void setEsaStatus(String esaStatus)
    {
        this.esaStatus = esaStatus;
    }

    @Column(name="IsisNumber", length = 16)
    public String getIsisNumber()
    {
        return isisNumber;
    }

    public void setIsisNumber(String isisNumber)
    {
        this.isisNumber = isisNumber;
    }

    @Column(name="NcbiTaxonNumber", length = 8)
    public String getNcbiTaxonNumber()
    {
        return ncbiTaxonNumber;
    }

    public void setNcbiTaxonNumber(String ncbiTaxonNumber)
    {
        this.ncbiTaxonNumber = ncbiTaxonNumber;
    }

    @Column(name="Number1")
    public Integer getNumber1()
    {
        return number1;
    }

    public void setNumber1(Integer number1)
    {
        this.number1 = number1;
    }

    @Column(name="Number2")
    public Integer getNumber2()
    {
        return number2;
    }

    public void setNumber2(Integer number2)
    {
        this.number2 = number2;
    }

    @Column(name="Text1", length = 32)
    public String getText1()
    {
        return text1;
    }

    public void setText1(String text1)
    {
        this.text1 = text1;
    }

    @Column(name="Text2", length = 32)
    public String getText2()
    {
        return text2;
    }

    public void setText2(String text2)
    {
        this.text2 = text2;
    }

    @Column(name="UsfwsCode", length = 16)
    public String getUsfwsCode()
    {
        return usfwsCode;
    }

    public void setUsfwsCode(String usfwsCode)
    {
        this.usfwsCode = usfwsCode;
    }

    @Column(name = "NodeNumber")
	public Integer getNodeNumber()
	{
		return this.nodeNumber;
	}

	public void setNodeNumber(Integer nodeNumber)
	{
		this.nodeNumber = nodeNumber;
	}

    @Column(name = "HighestChildNodeNumber")
	public Integer getHighestChildNodeNumber()
	{
		return this.highestChildNodeNumber;
	}

	public void setHighestChildNodeNumber(Integer highestChildNodeNumber)
	{
		this.highestChildNodeNumber = highestChildNodeNumber;
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

    @Column(name = "RankID", nullable=false)
	public Integer getRankId()
	{
		return this.rankId;
	}

	public void setRankId(Integer rankId)
	{
		this.rankId = rankId;
	}

    @Column(name = "GroupNumber", length = 20)
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
    @Column(name = "Visibility")
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
    
    @Column(name="IsHybrid")
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
    @Column(name = "VisibilitySetBy", length = 50)
    public String getVisibilitySetBy() {
        return this.visibilitySetBy;
    }
    
    public void setVisibilitySetBy(String visibilitySetBy) {
        this.visibilitySetBy = visibilitySetBy;
    }
    
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "acceptedTaxon")
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
	public Set<Taxon> getAcceptedChildren()
	{
		return this.acceptedChildren;
	}

	public void setAcceptedChildren(Set<Taxon> acceptedChildren)
	{
		this.acceptedChildren = acceptedChildren;
	}

    @ManyToOne
    @JoinColumn(name = "AcceptedID")
	public Taxon getAcceptedTaxon()
	{
		return this.acceptedTaxon;
	}

	public void setAcceptedTaxon(Taxon acceptedTaxon)
	{
		this.acceptedTaxon = acceptedTaxon;
	}

    @Transient
    public Taxon getAcceptedParent()
    {
        return getAcceptedTaxon();
    }
    
    public void setAcceptedParent(Taxon acceptedParent)
    {
        setAcceptedTaxon(acceptedParent);
    }


    /**
     * If this object represents a hybrid taxon, this returns the primary parent of the taxon.
     * 
     * @returnthe the primary parent of the taxon, or null if the object doesn't represent a hybrid taxon
     */
    @ManyToOne
    @JoinColumn(name = "HybridParent1ID")
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
    @ManyToOne
    @JoinColumn(name = "HybridParent2ID")
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
    @OneToMany(mappedBy = "hybridParent1")
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
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
    @OneToMany(mappedBy = "hybridParent2")
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
    public Set<Taxon> getHybridChildren2()
    {
        return hybridChildren2;
    }

    public void setHybridChildren2(Set<Taxon> hybridChildren2)
    {
        this.hybridChildren2 = hybridChildren2;
    }

    @OneToMany(mappedBy = "taxon")
    @Cascade( {CascadeType.MERGE, CascadeType.LOCK} )
	public Set<Determination> getDeterminations()
	{
		return determinations;
	}

	public void setDeterminations(Set<Determination> determinations)
	{
		this.determinations = determinations;
	}

    /**
     * @return the commonNames
     */
    @OneToMany(mappedBy = "taxon")
    @Cascade( {CascadeType.MERGE, CascadeType.LOCK} )
    public Set<CommonNameTx> getCommonNames()
    {
        return commonNames;
    }

    /**
     * @param commonNames the commonNames to set
     */
    public void setCommonNames(Set<CommonNameTx> commonNames)
    {
        this.commonNames = commonNames;
    }

    @OneToMany(mappedBy = "taxon")
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
	public Set<TaxonCitation> getTaxonCitations()
	{
		return this.taxonCitations;
	}

	public void setTaxonCitations(Set<TaxonCitation> taxonCitations)
	{
		this.taxonCitations = taxonCitations;
	}

    @ManyToOne
    @JoinColumn(name = "TaxonTreeDefID", nullable = false)
	public TaxonTreeDef getDefinition()
	{
		return this.definition;
	}

	public void setDefinition(TaxonTreeDef definition)
	{
		this.definition = definition;
	}

    @ManyToOne
    @JoinColumn(name = "TaxonTreeDefItemID", nullable = false)
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

    @ManyToOne
    @JoinColumn(name = "ParentID")
	public Taxon getParent()
	{
		return this.parent;
	}

	public void setParent(Taxon parent)
	{
		this.parent = parent;
	}

    @OneToMany(mappedBy = "parent")
    @Cascade( {CascadeType.ALL} )
	public Set<Taxon> getChildren()
	{
		return this.children;
	}

	public void setChildren(Set<Taxon> children)
	{
		this.children = children;
	}

    @Transient
	public Integer getTreeId()
	{
		return this.taxonId;
	}

	public void setTreeId(Integer id)
	{
        this.taxonId = id;
	}
	
    //@OneToMany(mappedBy = "taxon")
    //@Cascade( {CascadeType.ALL} )
    @OneToMany(mappedBy = "taxon")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @OrderBy("ordinal ASC")
    public Set<TaxonAttachment> getTaxonAttachments()
    {
        return taxonAttachments;
    }

    /**
     * @param taxonAttachments
     */
    public void setTaxonAttachments(Set<TaxonAttachment> taxonAttachments)
    {
        this.taxonAttachments = taxonAttachments;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.Treeable#addChild(edu.ku.brc.specify.datamodel.Treeable)
     */
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

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.Treeable#removeChild(edu.ku.brc.specify.datamodel.Treeable)
	 */
	public void removeChild(Taxon child)
	{
		children.remove(child);
		child.setParent(null);
	}

	/**
	 * @param child
	 */
	public void addAcceptedChild(Taxon child)
	{
		acceptedChildren.add(child);
		child.setAcceptedTaxon(this);
	}

	/**
	 * @param child
	 */
	public void removeAcceptedChild(Taxon child)
	{
		acceptedChildren.remove(child);
		child.setAcceptedTaxon(null);
	}

	/**
	 * @param taxonCitation
	 */
	public void addTaxonCitations(final TaxonCitation taxonCitation)
	{
		this.taxonCitations.add(taxonCitation);
		taxonCitation.setTaxon(this);
	}

	/**
	 * @param determination
	 */
	public void addDetermination(final Determination determination)
	{
		determinations.add(determination);
		determination.setTaxon(this);
	}

	/**
	 * @param determination
	 */
	public void removeDetermination(final Determination determination)
	{
		determinations.remove(determination);
		determination.setTaxon(null);
	}

	/**
	 * @param taxonCitation
	 */
	public void removeTaxonCitations(final TaxonCitation taxonCitation)
	{
		this.taxonCitations.remove(taxonCitation);
		taxonCitation.setTaxon(null);
	}
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#toString()
     */
    @Override
    public String toString()
    {
        return (fullName != null) ? fullName : ((name != null) ? name : super.toString());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.Treeable#getFullNameDirection()
     */
    @Transient
    public int getFullNameDirection()
    {
        return definition.getFullNameDirection();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.Treeable#getFullNameSeparator()
     */
    @Transient
    public String getFullNameSeparator()
    {
        return definitionItem.getFullNameSeparator();
    }
    
    
    /**
     * @return the count of 'current' Determinations
     */
    @Transient
    public Integer getCurrentDeterminationCount()
    {
        return getDeterminationCount(true);
    }
    
    /**
     * @param current
     * @return the count of Determinations
     */
    @Transient
    public Integer getDeterminationCount(boolean current)
    {
        String sql = "SELECT count(co.CollectionObjectID) FROM taxon as tx INNER JOIN determination as dt ON tx.TaxonID = dt.TaxonID ";
        if (current) 
        {
            sql += " INNER JOIN determinationstatus as ds ON dt.DeterminationStatusID = ds.DeterminationStatusID";
        }
        sql +=  " INNER JOIN collectionobject as co ON dt.CollectionObjectID = co.CollectionObjectID " +
            "WHERE tx.TaxonID = " +getId() + " AND co.CollectionMemberID = COLMEMID";
        if (current)
        {
            sql += " AND ds.Type in (1, 2)";
        }
        return BasicSQLUtils.getNumRecords(QueryAdjusterForDomain.getInstance().adjustSQL(sql));
    }
    
    /*@SuppressWarnings("unchecked")
    @Transient
    public List<Determination> getDeterminations()
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            if (session != null)
            {
                List<Determination> dets = (List<Determination>)session.getDataList("FROM Determination WHERE taxonId = "+getId());
                for (Determination det : dets)
                {
                    det.forceLoad();
                }
                return dets;
            }
        }
        finally
        {
            session.close();
        }
        return null;
    }*/


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
		Vector<Taxon> ancestorsList = new Vector<Taxon>();
		Taxon parentNode = parent;
		while(parentNode != null)
		{
			ancestorsList.add(0,parentNode);
			parentNode = parentNode.getParent();
		}
		
		return ancestorsList;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.Treeable#isDescendantOf(edu.ku.brc.specify.datamodel.Treeable)
	 */
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

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Taxon)
        {
            Taxon item = (Taxon)obj;
            if (item.taxonId != null)
            {
                if (item.taxonId.equals(this.taxonId))
                {
                    return true;
                }
                // else
                return false;
            }
            // else
            return super.equals(obj);
        }
        return false;
    }

    @Transient
    public Set<TaxonAttachment> getAttachmentReferences()
    {
        return taxonAttachments;
    }
    
    @Transient
    public Taxon getKingdom()
    {
        return getLevel(TaxonTreeDef.KINGDOM);
    }

    @Transient
    public Taxon getGenus()
    {
        return getLevel(TaxonTreeDef.GENUS);
    }

    @Transient
    public Taxon getSpecies()
    {
        return getLevel(TaxonTreeDef.SPECIES);
    }

    @Transient
    public Taxon getSubspecies()
    {
        return getLevel(TaxonTreeDef.SUBSPECIES);
    }
    
    @Transient
    public String getKingdomName()
    {
        return getLevelName(TaxonTreeDef.KINGDOM);
    }
    
    @Transient
    public String getGenusName()
    {
        return getLevelName(TaxonTreeDef.GENUS);
    }

    @Transient
    public String getSpeciesName()
    {
        return getLevelName(TaxonTreeDef.SPECIES);
    }

    @Transient
    public String getSubspeciesName()
    {
        return getLevelName(TaxonTreeDef.SUBSPECIES);
    }
    
    public Taxon getLevel(int levelRank)
    {
        Taxon t = this;
        
        while (t != null)
        {
            int rank = (t.getRankId() != null) ? t.getRankId().intValue() : Integer.MAX_VALUE;
            if (rank == levelRank)
            {
                return t;
            }
            if (rank < levelRank)
            {
                return null;
            }
            t = t.getParent();
        }
        return null;
    }
    
    /**
     * @param levelRank
     * @return
     */
    public String getLevelName(int levelRank)
    {
        Taxon t = getLevel(levelRank);
        if (t != null)
        {
            return t.getName();
        }
        return null;
    }

    
    //-------------------------------------------------------------------
    //-- WebLinkProviderIFace
    //-------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.weblink.WebLinkDataProviderIFace#getWebLinkData(java.lang.String)
     */
    @Override
    public String getWebLinkData(String dataName)
    {
        if (StringUtils.isNotEmpty(dataName))
        {
            if (dataName.equals("species"))
            {
                return getSpeciesName();
                
            } else if (dataName.equals("genus"))
            {
                return getGenusName();
            }
            return super.getWebLinkData(dataName);
        }
        return null;
    }
    
}
