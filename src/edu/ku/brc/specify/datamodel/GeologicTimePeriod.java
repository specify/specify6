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

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.math.BigDecimal;

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

import edu.ku.brc.specify.treeutils.GeologicTimePeriodComparator;

@SuppressWarnings("serial")
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "geologictimeperiod")
@org.hibernate.annotations.Table(appliesTo="geologictimeperiod", indexes =
    {   @Index (name="GTPNameIDX", columnNames={"Name"}),
        @Index (name="GTPFullNameIDX", columnNames={"FullName"}),
        @Index (name="GTPGuidIDX", columnNames={"GUID"})
    })
public class GeologicTimePeriod extends DataModelObjBase implements java.io.Serializable, Treeable<GeologicTimePeriod,GeologicTimePeriodTreeDef,GeologicTimePeriodTreeDefItem>{

    /**
     * A <code>Logger</code> object used for all log messages eminating from
     * this class.
     */
    protected static final Logger log = Logger.getLogger(GeologicTimePeriod.class);

    protected Integer                       geologicTimePeriodId;
    protected Integer                       rankId;
    protected String                        name;
    protected String                        fullName;
    protected String                        remarks;
    protected String                        guid;
    protected Integer                       nodeNumber;
    protected Integer                       highestChildNodeNumber;
    protected String                        standard;
    protected BigDecimal                         startPeriod;
    protected BigDecimal                         startUncertainty;
    protected BigDecimal                         endPeriod;
    protected BigDecimal                         endUncertainty;
    protected String                        text1;
    protected String                        text2;
    protected Boolean                       isBioStrat;
    protected GeologicTimePeriodTreeDef     definition;
    protected GeologicTimePeriodTreeDefItem definitionItem;
    protected GeologicTimePeriod            parent;
    protected Set<GeologicTimePeriod>       children;
    
    protected Set<PaleoContext>             bioStratsPaleoContext;
    protected Set<PaleoContext>             chronosStratsPaleoContext;

    // for synonym support
    protected Boolean                       isAccepted;
    protected GeologicTimePeriod            acceptedGeologicTimePeriod;
    protected Set<GeologicTimePeriod>       acceptedChildren;

    // Constructors

    /** default constructor */
    public GeologicTimePeriod()
    {
        // do nothing
    }

    /** constructor with id */
    public GeologicTimePeriod(Integer geologicTimePeriodId)
    {
        this.geologicTimePeriodId = geologicTimePeriodId;
    }

    // Initializer
    @Override
    public void initialize()
    {
        super.init();
        geologicTimePeriodId = null;
        rankId = null;
        name = null;
        fullName = null;
        remarks = null;
        guid = null;
        nodeNumber = null;
        highestChildNodeNumber = null;
        standard = null;
        startPeriod = null;
        startUncertainty = null;
        endPeriod = null;
        endUncertainty = null;
        definition = null;
        definitionItem = null;
        parent = null;
        isBioStrat = false;
        text1 = null;
        text2 = null;

        children                  = new HashSet<GeologicTimePeriod>();
        bioStratsPaleoContext     = new HashSet<PaleoContext>();
        chronosStratsPaleoContext = new HashSet<PaleoContext>();
        
        isAccepted          = true;
        acceptedGeologicTimePeriod  = null;
        acceptedChildren    = new HashSet<GeologicTimePeriod>();
        
        hasGUIDField = true;
        setGUID();
    }

    // End Initializer
    /**
     * -
     * @param originalObj
     * @param deep  if true then copy and clone children
     * @param session
     * @return
     */
    @Override
    public boolean initializeClone(DataModelObjBase originalObj, boolean deep, final DataProviderSessionIFace session) {
        geologicTimePeriodId = null;
        if (deep) {
            log.error(getClass().getName() + ": initializeClone not supported when deep parameter is true.");
            return false;
        } else {
            children = new HashSet<>();
            acceptedChildren = new HashSet<>();
            bioStratsPaleoContext = new HashSet<>();
            chronosStratsPaleoContext = new HashSet<>();
            setGUID();
            return true;
        }
    }

    // Property accessors

    /**
     * 
     */
    @Id
    @GeneratedValue
    @Column(name = "GeologicTimePeriodID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getGeologicTimePeriodId()
    {
        return this.geologicTimePeriodId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId()
    {
        return this.geologicTimePeriodId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return GeologicTimePeriod.class;
    }

    public void setGeologicTimePeriodId(Integer geologicTimePeriodId)
    {
        this.geologicTimePeriodId = geologicTimePeriodId;
    }

    /**
     * 
     */
    @Column(name = "RankID", nullable=false)
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
     * @param fullName the fullName to set
     */
    public void setFullName(String fullName)
    {
        this.fullName = fullName;
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
    @Column(name = "Standard", length = 64)
    public String getStandard()
    {
        return this.standard;
    }

    public void setStandard(String standard)
    {
        this.standard = standard;
    }

    @Column(name = "EndPeriod", precision = 20, scale = 10)
    public BigDecimal getEndPeriod()
    {
        return endPeriod;
    }

    public void setEndPeriod(BigDecimal end)
    {
        this.endPeriod = end;
    }

    @Column(name = "EndUncertainty", precision = 20, scale = 10)
    public BigDecimal getEndUncertainty()
    {
        return endUncertainty;
    }

    public void setEndUncertainty(BigDecimal endUncertainty)
    {
        this.endUncertainty = endUncertainty;
    }

    @Column(name = "StartPeriod", precision = 20, scale = 10)
    public BigDecimal getStartPeriod()
    {
        return startPeriod;
    }

    public void setStartPeriod(BigDecimal start)
    {
        this.startPeriod = start;
    }

    @Column(name = "StartUncertainty", precision = 20, scale = 10)
    public BigDecimal getStartUncertainty()
    {
        return startUncertainty;
    }

    public void setStartUncertainty(BigDecimal startUncertainty)
    {
        this.startUncertainty = startUncertainty;
    }

    /**
     * 
     */
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
    
    /**
     *
     */
    @Column(name = "GUID", unique = false, nullable = true, insertable = true, updatable = false, length = 128)
    public String getGuid() {
        return this.guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     * @return the text1
     */
    @Column(name = "Text1", length = 128)
    public String getText1()
    {
        return text1;
    }

    /**
     * @param text1 the text1 to set
     */
    public void setText1(String text1)
    {
        this.text1 = text1;
    }

    /**
     * @return the text2
     */
    @Column(name = "Text2", length = 128)
    public String getText2()
    {
        return text2;
    }

    /**
     * @param text2 the text2 to set
     */
    public void setText2(String text2)
    {
        this.text2 = text2;
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

    /**
     * @return the isBioStrat
     */
    @Column(name="IsBioStrat")
    public Boolean getIsBioStrat()
    {
        return isBioStrat == null ? false : isBioStrat;
    }

    /**
     * @param isBioStrat the isBioStrat to set
     */
    public void setIsBioStrat(Boolean isBioStrat)
    {
        this.isBioStrat = isBioStrat;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "acceptedGeologicTimePeriod")
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
    public Set<GeologicTimePeriod> getAcceptedChildren()
    {
        return this.acceptedChildren;
    }

    public void setAcceptedChildren(Set<GeologicTimePeriod> acceptedChildren)
    {
        this.acceptedChildren = acceptedChildren;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AcceptedID")
    public GeologicTimePeriod getAcceptedGeologicTimePeriod()
    {
        return this.acceptedGeologicTimePeriod;
    }

    public void setAcceptedGeologicTimePeriod(GeologicTimePeriod acceptedGeologicTimePeriod)
    {
        this.acceptedGeologicTimePeriod = acceptedGeologicTimePeriod;
    }
    
    @Transient
    public GeologicTimePeriod getAcceptedParent()
    {
        return getAcceptedGeologicTimePeriod();
    }
    
    public void setAcceptedParent(GeologicTimePeriod acceptedParent)
    {
        setAcceptedGeologicTimePeriod(acceptedParent);
    }

    /**
     * 
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "GeologicTimePeriodTreeDefID", nullable = false)
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
    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @JoinColumn(name = "GeologicTimePeriodTreeDefItemID", nullable = false)
    public GeologicTimePeriodTreeDefItem getDefinitionItem()
    {
        return this.definitionItem;
    }

    public void setDefinitionItem(GeologicTimePeriodTreeDefItem definitionItem)
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
    public GeologicTimePeriod getParent()
    {
        return this.parent;
    }

    public void setParent(GeologicTimePeriod parent)
    {
        this.parent = parent;
    }

    //@OneToMany(cascade = {javax.persistence.CascadeType.ALL}, fetch = FetchType.LAZY, mappedBy = "parent")
    @OneToMany(mappedBy = "parent")
    @Cascade( {CascadeType.ALL} )
    public Set<GeologicTimePeriod> getChildren()
    {
        return this.children;
    }

    public void setChildren(Set<GeologicTimePeriod> children)
    {
        this.children = children;
    }

    /**
     * @return the bioStratsPaleoContext
     */
    @OneToMany(mappedBy = "bioStrat")
    @Cascade( {CascadeType.ALL })
    public Set<PaleoContext> getBioStratsPaleoContext()
    {
        return bioStratsPaleoContext;
    }

    /**
     * @param bioStratsPaleoContext the bioStratsPaleoContext to set
     */
    public void setBioStratsPaleoContext(Set<PaleoContext> bioStratsPaleoContext)
    {
        this.bioStratsPaleoContext = bioStratsPaleoContext;
    }


    /* Code added in order to implement Treeable */

    /**
     * @return the chronosStratsPaleoContext
     */
    @OneToMany(mappedBy = "chronosStrat")
    @Cascade( {CascadeType.ALL} )
    public Set<PaleoContext> getChronosStratsPaleoContext()
    {
        return chronosStratsPaleoContext;
    }

    /**
     * @param chronosStratsPaleoContext the chronosStratsPaleoContext to set
     */
    public void setChronosStratsPaleoContext(Set<PaleoContext> chronosStratsPaleoContext)
    {
        this.chronosStratsPaleoContext = chronosStratsPaleoContext;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return GeologicTimePeriod.getClassTableId();
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
    
    @Transient
    public Integer getTreeId()
    {
        return getGeologicTimePeriodId();
    }

    public void setTreeId(Integer id)
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

    @Override
    public String toString()
    {
        return (fullName!=null) ? fullName : super.toString();
    }

    // methods to complete implementation of AbstractTreeable

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
     * @return the full name
     */
    public String fixFullName()
    {
        Vector<GeologicTimePeriod> parts = new Vector<GeologicTimePeriod>();
        parts.add(this);
        GeologicTimePeriod node = getParent();
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
                    GeologicTimePeriod part = parts.get(j);
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
                    GeologicTimePeriod part = parts.get(j);
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
        for( GeologicTimePeriod child: getChildren() )
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
    
    @Transient
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

    public boolean isDescendantOf(GeologicTimePeriod node)
    {
        if( node==null )
        {
            throw new NullPointerException();
        }
        
        GeologicTimePeriod i = getParent();
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
    public Comparator<? super GeologicTimePeriod> getComparator()
    {
        return new GeologicTimePeriodComparator();
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
        return 46;
    }

}
