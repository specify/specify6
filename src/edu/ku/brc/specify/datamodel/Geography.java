/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.datamodel;

import java.math.BigDecimal;
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

import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.specify.treeutils.TreeOrderSiblingComparator;

@SuppressWarnings("serial")
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "geography")
@org.hibernate.annotations.Table(appliesTo="geography", indexes =
    {   @Index (name="GeoNameIDX", columnNames={"Name"}),
        @Index (name="GeoFullNameIDX", columnNames={"FullName"})
    })
public class Geography extends DataModelObjBase implements java.io.Serializable, Treeable<Geography,GeographyTreeDef,GeographyTreeDefItem>
{

    /**
     * A <code>Logger</code> object used for all log messages eminating from
     * this class.
     */
    protected static final Logger log = Logger.getLogger(Geography.class);

	protected Integer			    geographyId;
	protected String				name;
	protected String				remarks;
	protected String				commonName;
	protected String				fullName;
	protected String				geographyCode;
	protected Integer				rankId;
	protected Integer				nodeNumber;
	protected Integer				highestChildNodeNumber;
	protected String				abbrev;
    protected BigDecimal            centroidLat;
    protected BigDecimal            centroidLon;
    protected String                gml;
	protected String				text1;
	protected String				text2;
	protected Integer				number1;
	protected Integer				number2;
	protected Date					timestampVersion;
	protected Boolean				isCurrent;
	protected String                guid;
	
	protected Set<Locality>			localities;
	protected GeographyTreeDef		definition;
	protected GeographyTreeDefItem	definitionItem;
	protected Geography				parent;
	protected Set<Geography>		children;

    // for synonym support
    protected Boolean              isAccepted;
    protected Geography            acceptedGeography;
    protected Set<Geography>       acceptedChildren;

	// Constructors

	/** default constructor */
	public Geography()
	{
		// do nothing
	}

	/** constructor with id */
	public Geography(Integer geographyId)
	{
		this.geographyId = geographyId;
	}

	// Initializer
    @Override
    public void initialize()
	{
        super.init();
		geographyId = null;
		name = null;
        fullName = null;
		remarks = null;
		commonName = null;
		geographyCode = null;
		rankId = null;
		nodeNumber = null;
		highestChildNodeNumber = null;
		isAccepted = true;
		abbrev = null;
		text1 = null;
		text2 = null;
		number1 = null;
		number2 = null;
		timestampVersion = null;
		isCurrent = null;
		guid      = null;
		localities = new HashSet<Locality>();
		definition = null;
		definitionItem = null;
		parent = null;
		children = new HashSet<Geography>();
		
        
        isAccepted          = true;
        acceptedGeography   = null;
        acceptedChildren    = new HashSet<Geography>();
        
        // Not using Geography GUIDs at the time 04/09/13 - Database Schema 1.8
        hasGUIDField = false;
        //setGUID();
	}

	// End Initializer

	/**
	 *
	 * @param originalObj
	 * @param deep  if true then copy and clone children
	 * @param session
	 * @return
	 */
	@Override
	public boolean initializeClone(DataModelObjBase originalObj, boolean deep, final DataProviderSessionIFace session) {
		geographyId = null;
		if (deep) {
			log.error(getClass().getName() + ": initializeClone not supported when deep parameter is true.");
			return false;
		} else {
			children = new HashSet<>();
			acceptedChildren = new HashSet<>();
			localities = new HashSet<>();
			return true;
		}
	}

	// Property accessors

	/**
	 *
	 */
    @Id
    @GeneratedValue
    @Column(name = "GeographyID")
	public Integer getGeographyId()
	{
		return this.geographyId;
	}

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Override
    @Transient
    public Integer getId()
    {
        return this.geographyId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return Geography.class;
    }

	public void setGeographyId(Integer geographyId)
	{
		this.geographyId = geographyId;
	}

	/**
	 *
	 */
    @Column(name = "Name", nullable=false, length = 128)
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
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

	/**
	 *
	 */
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
	 * @return the fullName
	 */
    @Column(name = "FullName", length = 500)
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
    @Column(name = "GeographyCode", length = 24)
	public String getGeographyCode()
	{
		return this.geographyCode;
	}

	public void setGeographyCode(String geographyCode)
	{
		this.geographyCode = geographyCode;
	}

	/**
	 *
	 */
    @Column(name = "RankID", nullable = false)
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
    @Column(name = "NodeNumber")
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
    @Column(name = "HighestChildNodeNumber")
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
    @Column(name = "Abbrev", length = 16)
	public String getAbbrev()
	{
		return this.abbrev;
	}

	public void setAbbrev(String abbrev)
	{
		this.abbrev = abbrev;
	}

    @Column(name = "CentroidLat")
	public BigDecimal getCentroidLat()
    {
        return centroidLat;
    }

    public void setCentroidLat(BigDecimal centroidLat)
    {
        this.centroidLat = centroidLat;
    }

    @Column(name = "CentroidLon")
    public BigDecimal getCentroidLon()
    {
        return centroidLon;
    }

    public void setCentroidLon(BigDecimal centroidLon)
    {
        this.centroidLon = centroidLon;
    }

    @Lob
    @Column(name = "GML", length=4096)
    public String getGml()
    {
        return gml;
    }

    public void setGml(String gml)
    {
        this.gml = gml;
    }

    /**
	 *
	 */
    @Column(name = "Text1", length = 32)
	public String getText1()
	{
		return this.text1;
	}

	public void setText1(String text1)
	{
		this.text1 = text1;
	}

	/**
	 *
	 */
    @Column(name = "Text2", length = 32)
	public String getText2()
	{
		return this.text2;
	}

	public void setText2(String text2)
	{
		this.text2 = text2;
	}

	/**
	 *
	 */
    @Column(name = "Number1")
	public Integer getNumber1()
	{
		return this.number1;
	}

	public void setNumber1(Integer number1)
	{
		this.number1 = number1;
	}

	/**
	 *
	 */
    @Column(name = "Number2")
	public Integer getNumber2()
	{
		return this.number2;
	}

	public void setNumber2(Integer number2)
	{
		this.number2 = number2;
	}

	/**
	 *
	 */
    @Column(name = "TimestampVersion")
	public Date getTimestampVersion()
	{
		return this.timestampVersion;
	}

	public void setTimestampVersion(Date timestampVersion)
	{
		this.timestampVersion = timestampVersion;
	}

    @Column(name = "IsCurrent")
	public Boolean getIsCurrent()
	{
		return this.isCurrent;
	}

	public void setIsCurrent(Boolean isCurrent)
	{
		this.isCurrent = isCurrent;
	}

    @Column(name="IsAccepted", nullable=false)
    public Boolean getIsAccepted()
    {
        return this.isAccepted;
    }

    public void setIsAccepted(Boolean accepted)
    {
        this.isAccepted = accepted;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "acceptedGeography")
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
    public Set<Geography> getAcceptedChildren()
    {
        return this.acceptedChildren;
    }

    public void setAcceptedChildren(Set<Geography> acceptedChildren)
    {
        this.acceptedChildren = acceptedChildren;
    }
    
    /**
     * @return the guid
     */
    @Column(name = "GUID", unique = false, nullable = true, insertable = true, updatable = false, length = 128)
    public String getGuid()
    {
        return guid;
    }

    /**
     * @param guid the guid to set
     */
    public void setGuid(String guid)
    {
        this.guid = guid;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AcceptedID")
    public Geography getAcceptedGeography()
    {
        return this.acceptedGeography;
    }

    public void setAcceptedGeography(Geography acceptedGeography)
    {
        this.acceptedGeography = acceptedGeography;
    }
    
    @Transient
    public Geography getAcceptedParent()
    {
        return getAcceptedGeography();
    }
    
    public void setAcceptedParent(Geography acceptedParent)
    {
        setAcceptedGeography(acceptedParent);
    }

	/**
	 *
	 */
    @OneToMany(mappedBy = "geography")
    @Cascade( {CascadeType.ALL} )
	public Set<Locality> getLocalities()
	{
		return this.localities;
	}

	public void setLocalities(Set<Locality> localities)
	{
		this.localities = localities;
	}

	/**
	 *
	 */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "GeographyTreeDefID", nullable = false)
	public GeographyTreeDef getDefinition()
	{
		return this.definition;
	}

	public void setDefinition(GeographyTreeDef definition)
	{
		this.definition = definition;
	}

	/**
	 *
	 */
    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @JoinColumn(name = "GeographyTreeDefItemID", nullable = false)
	public GeographyTreeDefItem getDefinitionItem()
	{
		return this.definitionItem;
	}

	public void setDefinitionItem(GeographyTreeDefItem definitionItem)
	{
		this.definitionItem = definitionItem;
		if (definitionItem!=null && definitionItem.getRankId()!=null)
		{
			this.rankId = this.definitionItem.getRankId();
		}
	}

	/**
	 *
	 */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ParentID")
	public Geography getParent()
	{
		return this.parent;
	}

	public void setParent(Geography parent)
	{
		this.parent = parent;
	}

    @OneToMany(mappedBy = "parent")
    @Cascade( {CascadeType.ALL} )
	public Set<Geography> getChildren()
	{
		return this.children;
	}

	public void setChildren(Set<Geography> children)
	{
		this.children = children;
	}

	/* Code added in order to implement Treeable */

    @Transient
	public Integer getTreeId()
	{
		return getGeographyId();
	}

	public void setTreeId(Integer id)
	{
		setGeographyId(id);
	}

	public void addChild(Geography child)
	{
		Geography oldParent = child.getParent();
		if( oldParent!=null )
		{
			oldParent.removeChild(child);
		}

		children.add(child);
		child.setParent(this);
	}
    
	public void removeChild(Geography child)
	{
		children.remove(child);
		child.setParent(null);
	}

	// Add Methods

	public void addLocality(final Locality loc)
	{
		localities.add(loc);
		loc.setGeography(this);
	}

	// Done Add Methods

	// Delete Methods

	public void removeLocality(final Locality loc)
	{
		localities.remove(loc);
		loc.setGeography(null);
	}

	// Delete Add Methods

	@Override
	public String toString()
	{
		return (fullName!=null) ? fullName : super.toString();
	}

	// methods to complete implementation of AbstractTreeable

    @Transient
	public int getFullNameDirection()
	{
        Integer dir = definition.getFullNameDirection();
        
        // if it's anything other than FORWARD, set it to REVERSE to avoid 'null' or undefined values
        if (dir==null || dir!=TreeDefIface.FORWARD)
        {
            dir = TreeDefIface.REVERSE;
        }
		return dir;
	}
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Geography.getClassTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentId()
     */
    @Override
    @Transient
    public Integer getParentId()
    {
        return parent != null ? parent.getId() : null;
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
	 * Generates the 'full name' of a node using the <code>IsInFullName</code> field from the tree
	 * definition items and following the parent pointer until we hit the root node.  Also used
	 * in the process is a "direction indicator" for the tree determining whether the name
	 * should start with the higher nodes and work down to the given node or vice versa.
	 * 
	 * @return the full name
	 */
	public String fixFullName()
	{
		Vector<Geography> parts = new Vector<Geography>();
        parts.add(this);
		Geography node = getParent();
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
                    Geography part = parts.get(j);
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
                    Geography part = parts.get(j);
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
	 * @return the number of proper descendants
	 */
    @Transient
	public int getDescendantCount()
	{
		int totalDescendants = 0;
		for( Geography child: getChildren() )
		{
			totalDescendants += 1 + child.getDescendantCount();
		}
		return totalDescendants;
	}
	
	/**
	 * Determines if children are allowed for the given node.
	 * 
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
	public List<Geography> getAllDescendants()
	{
		Vector<Geography> descendants = new Vector<Geography>();
		for( Geography child: getChildren() )
		{
			descendants.add(child);
			descendants.addAll(child.getAllDescendants());
		}
		return descendants;
	}
	
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.Treeable#getAllAncestors()
     */
    @Transient
	public List<Geography> getAllAncestors()
	{
		Vector<Geography> ancestors = new Vector<Geography>();
		Geography parentNode = parent;
		while(parentNode != null)
		{
			ancestors.add(0,parentNode);
			parentNode = parentNode.getParent();
		}
		
		return ancestors;
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.datamodel.Treeable#isDescendantOf(edu.ku.brc.specify.datamodel.Treeable)
	 */
	public boolean isDescendantOf(Geography node)
	{
		if( node==null )
		{
			throw new NullPointerException();
		}
		
		Geography i = getParent();
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
	
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.Treeable#getComparator()
     */
    @Transient
	public Comparator<? super Geography> getComparator()
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
        return 3;
    }

}
