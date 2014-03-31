/* Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;

import edu.ku.brc.specify.treeutils.TreeOrderSiblingComparator;

@SuppressWarnings("serial")
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "storage")
@org.hibernate.annotations.Table(appliesTo="storage", indexes =
    {   @Index (name="StorNameIDX", columnNames={"Name"}),
        @Index (name="StorFullNameIDX", columnNames={"FullName"})
    })
public class Storage extends DataModelObjBase implements AttachmentOwnerIFace<StorageAttachment>, 
	Serializable, Treeable<Storage,StorageTreeDef, StorageTreeDefItem>
{

    /**
     * A <code>Logger</code> object used for all log messages eminating from
     * this class.
     */
    protected static final Logger log = Logger.getLogger(Storage.class);

    protected Integer           storageId;
    protected String            name;
    protected String            fullName;
    protected String            remarks;
    protected Integer           rankId;
    protected Integer           nodeNumber;
    protected Integer           highestChildNodeNumber;
    protected String            abbrev;
    protected String            text1;
    protected String            text2;
    protected Integer           number1;
    protected Integer           number2;
    protected Date              timestampVersion;
    protected StorageTreeDef    definition;
    protected StorageTreeDefItem definitionItem;
    protected Storage            parent;
    protected Set<Preparation>   preparations;
    protected Set<Container>     containers;
    protected Set<Storage>       children;

    // for synonym support
    protected Boolean            isAccepted;
    protected Storage            acceptedStorage;
    protected Set<Storage>       acceptedChildren;


    private Set<StorageAttachment> storageAttachments;

    /** default constructor */
    public Storage()
    {
        // do nothing
    }

    /** constructor with id */
    public Storage(Integer storageId)
    {
        this.storageId = storageId;
    }

    @Override
    public void initialize()
    {
        super.init();
        storageId = null;
        name = null;
        fullName = null;
        remarks = null;
        rankId = null;
        nodeNumber = null;
        highestChildNodeNumber = null;
        abbrev = null;
        text1 = null;
        text2 = null;
        number1 = null;
        number2 = null;
        timestampVersion = null;
        definition = null;
        definitionItem = null;
        parent = null;
        preparations = new HashSet<Preparation>();
        containers = new HashSet<Container>();
        children = new HashSet<Storage>();
        
        isAccepted       = true;
        acceptedStorage  = null;
        acceptedChildren = new HashSet<Storage>();
        
        storageAttachments = new HashSet<StorageAttachment>();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentReferences()
     */
    @Override
    @Transient
    public Set<StorageAttachment> getAttachmentReferences() {
        return storageAttachments;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.AttachmentOwnerIFace#getAttachmentTableId()
     */
    @Override
    @Transient
    public int getAttachmentTableId() {
        return getClassTableId();
    }

    @Id
    @GeneratedValue
    @Column(name = "StorageID")
    public Integer getStorageId()
    {
        return this.storageId;
    }

    @OneToMany(mappedBy = "storage")
    @org.hibernate.annotations.Cascade( { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @OrderBy("ordinal ASC")
    public Set<StorageAttachment> getStorageAttachments()
    {
        return storageAttachments;
    }

    /**
     * @param storageAttachments
     */
    public void setStorageAttachments(Set<StorageAttachment> storageAttachments)
    {
        this.storageAttachments = storageAttachments;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Override
    @Transient
    public Integer getId()
    {
        return this.storageId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass()
    {
        return Storage.class;
    }

    /**
     * @param storageId
     */
    public void setStorageId(Integer storageId)
    {
        this.storageId = storageId;
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

    /**
     * @return the fullName
     */
    @Column(name = "FullName", length = 255)
    public String getFullName()
    {
        return fullName;
    }

    /**
     * @param fullName the fullname to set
     */
    public void setFullName(String fullName)
    {
        this.fullName = fullName;
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

    @Column(name = "RankID", nullable=false)
    public Integer getRankId()
    {
        return this.rankId;
    }

    public void setRankId(Integer rankId)
    {
        this.rankId = rankId;
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

    @Column(name = "Abbrev", length = 16)
    public String getAbbrev()
    {
        return this.abbrev;
    }

    public void setAbbrev(String abbrev)
    {
        this.abbrev = abbrev;
    }

    @Column(name = "Text1", length = 32)
    public String getText1()
    {
        return this.text1;
    }

    public void setText1(String text1)
    {
        this.text1 = text1;
    }

    @Column(name = "Text2", length = 32)
    public String getText2()
    {
        return this.text2;
    }

    public void setText2(String text2)
    {
        this.text2 = text2;
    }

    @Column(name = "Number1")
    public Integer getNumber1()
    {
        return this.number1;
    }

    public void setNumber1(Integer number1)
    {
        this.number1 = number1;
    }

    @Column(name = "Number2")
    public Integer getNumber2()
    {
        return this.number2;
    }

    public void setNumber2(Integer number2)
    {
        this.number2 = number2;
    }

    @Column(name = "TimestampVersion")
    public Date getTimestampVersion()
    {
        return this.timestampVersion;
    }

    public void setTimestampVersion(Date timestampVersion)
    {
        this.timestampVersion = timestampVersion;
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

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "acceptedStorage")
    @Cascade( {CascadeType.SAVE_UPDATE, CascadeType.MERGE, CascadeType.LOCK} )
    public Set<Storage> getAcceptedChildren()
    {
        return this.acceptedChildren;
    }

    public void setAcceptedChildren(Set<Storage> acceptedChildren)
    {
        this.acceptedChildren = acceptedChildren;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "AcceptedID")
    public Storage getAcceptedStorage()
    {
        return this.acceptedStorage;
    }

    public void setAcceptedStorage(Storage acceptedStorage)
    {
        this.acceptedStorage = acceptedStorage;
    }
    
    @Transient
    public Storage getAcceptedParent()
    {
        return getAcceptedStorage();
    }
    
    public void setAcceptedParent(Storage acceptedParent)
    {
        setAcceptedStorage(acceptedParent);
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "StorageTreeDefID", nullable = false)
    public StorageTreeDef getDefinition()
    {
        return this.definition;
    }

    public void setDefinition(StorageTreeDef definition)
    {
        this.definition = definition;
    }

    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @JoinColumn(name = "StorageTreeDefItemID", nullable = false)
    public StorageTreeDefItem getDefinitionItem()
    {
        return this.definitionItem;
    }

    public void setDefinitionItem(StorageTreeDefItem definitionItem)
    {
        this.definitionItem = definitionItem;
        if (definitionItem!=null && definitionItem.getRankId()!=null)
        {
            this.rankId = this.definitionItem.getRankId();
        }
    }

    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "ParentID")
    public Storage getParent()
    {
        return this.parent;
    }

    public void setParent(Storage parent)
    {
        this.parent = parent;
    }

    @OneToMany(mappedBy = "storage")
    @Cascade( {CascadeType.MERGE, CascadeType.LOCK} )
    public Set<Preparation> getPreparations()
    {
        return this.preparations;
    }

    public void setPreparations(Set<Preparation> preparations)
    {
        this.preparations = preparations;
    }

    @OneToMany(mappedBy = "storage")
    @Cascade( {CascadeType.MERGE, CascadeType.LOCK} )
    public Set<Container> getContainers()
    {
        return this.containers;
    }

    public void setContainers(Set<Container> containers)
    {
        this.containers = containers;
    }

    @OneToMany(mappedBy = "parent")
    @Cascade( {CascadeType.ALL} )
    public Set<Storage> getChildren()
    {
        return this.children;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.Treeable#setChildren(java.util.Set)
     */
    public void setChildren(Set<Storage> children)
    {
        this.children = children;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getParentTableId()
     */
    @Override
    @Transient
    public Integer getParentTableId()
    {
        return Storage.getClassTableId();
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
     * @see edu.ku.brc.specify.datamodel.Treeable#getTreeId()
     */
    @Transient
    public Integer getTreeId()
    {
        return getStorageId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.Treeable#setTreeId(java.lang.Integer)
     */
    public void setTreeId(Integer id)
    {
        setStorageId(id);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.Treeable#addChild(edu.ku.brc.specify.datamodel.Treeable)
     */
    public void addChild(Storage child)
    {
        Storage oldParent = child.getParent();
        if (oldParent!=null )
        {
            oldParent.removeChild(child);
        }

        children.add(child);
        child.setParent(this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.Treeable#removeChild(edu.ku.brc.specify.datamodel.Treeable)
     */
    public void removeChild(Storage child)
    {
        children.remove(child);
        child.setParent(null);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#toString()
     */
    @Override
    public String toString()
    {
        return (fullName!=null) ? fullName : super.toString();
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
        Vector<Storage> parts = new Vector<Storage>();
        parts.add(this);
        Storage node = getParent();
        while (node != null )
        {
            Boolean include = node.getDefinitionItem().getIsInFullName();
            if (include != null && include.booleanValue() == true )
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
                for (int j = parts.size()-1; j > -1; --j )
                {
                    Storage part = parts.get(j);
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
                for (int j = 0; j < parts.size(); ++j )
                {
                    Storage part = parts.get(j);
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
                    if (j!=parts.size()-1)
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

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.Treeable#getDescendantCount()
     */
    @Transient
    public int getDescendantCount()
    {
        int totalDescendants = 0;
        for (Storage child: getChildren())
        {
            totalDescendants += 1 + child.getDescendantCount();
        }
        return totalDescendants;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.Treeable#childrenAllowed()
     */
    public boolean childrenAllowed()
    {
        if (definitionItem == null || definitionItem.getChild() == null)
        {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.Treeable#getAllDescendants()
     */
    @Transient
    public List<Storage> getAllDescendants()
    {
        Vector<Storage> descendants = new Vector<Storage>();
        for (Storage child: getChildren() )
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
    public List<Storage> getAllAncestors()
    {
        Vector<Storage> ancestors = new Vector<Storage>();
        Storage parentNode = parent;
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
    public boolean isDescendantOf(Storage node)
    {
        if (node == null)
        {
            throw new NullPointerException();
        }
        
        Storage i = getParent();
        while (i != null )
        {
            if (i.getId() == getId())
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
    public Comparator<? super Storage> getComparator()
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
        return 58;
    }

}
