/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.datamodel;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.treeutils.TreeOrderSiblingComparator;
import edu.ku.brc.ui.forms.FormDataObjIFace;

@SuppressWarnings("serial")
public class Taxon extends DataModelObjBase implements Serializable, Treeable<Taxon,TaxonTreeDef,TaxonTreeDefItem>
{
    /**
     * A <code>Logger</code> object used for all log messages eminating from
     * this class.
     */
    protected static final Logger log = Logger.getLogger(Taxon.class);

	protected Long				taxonId;
	protected String				taxonomicSerialNumber;
	protected String				guid;
	protected String				name;
	protected String				remarks;
	protected String				unitInd1;
	protected String				unitName1;
	protected String				unitInd2;
	protected String				unitName2;
	protected String				unitInd3;
	protected String				unitName3;
	protected String				unitInd4;
	protected String				unitName4;
	protected String				fullName;
	protected String				commonName;
	protected String				author;
	protected String				source;
	protected Integer				groupPermittedToView;
	protected String				environmentalProtectionStatus;
	protected Integer				nodeNumber;
	protected Integer				highestChildNodeNumber;
	protected Short					accepted;
	protected Integer				rankId;
	protected String				groupNumber;
	protected Set<Taxon>			acceptedChildren;
	protected Taxon					acceptedTaxon;
	protected Set<Determination>	determinations;
	protected Set<TaxonCitation>	taxonCitations;
	protected TaxonTreeDef			definition;
	protected TaxonTreeDefItem		definitionItem;
	protected Taxon					parent;
    protected Set<Attachment>          attachments;
	protected Set<Taxon>			children;

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
        timestampModified = null;
		lastEditedBy = null;
		accepted = null;
		rankId = null;
		groupNumber = null;
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

	public Long getTaxonId()
	{
		return this.taxonId;
	}

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    @Override
    public Long getId()
    {
        return this.taxonId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getIdentityTitle()
     */
    @Override
    public String getIdentityTitle()
    {
        String title = fullName != null && fullName.length() > 0 ? fullName : name;
        return title != null && title.length() > 0 ? title : super.getIdentityTitle();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Override
    public Class<?> getDataClass()
    {
        return Taxon.class;
    }

	public void setTaxonId(Long taxonId)
	{
		this.taxonId = taxonId;
	}

	public String getTaxonomicSerialNumber()
	{
		return this.taxonomicSerialNumber;
	}

	public void setTaxonomicSerialNumber(String taxonomicSerialNumber)
	{
		this.taxonomicSerialNumber = taxonomicSerialNumber;
	}

	public String getGuid()
	{
		return this.guid;
	}

	public void setGuid(String guid)
	{
		this.guid = guid;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getRemarks()
	{
		return this.remarks;
	}

	public void setRemarks(String remarks)
	{
		this.remarks = remarks;
	}

	public String getUnitInd1()
	{
		return this.unitInd1;
	}

	public void setUnitInd1(String unitInd1)
	{
		this.unitInd1 = unitInd1;
	}

	public String getUnitName1()
	{
		return this.unitName1;
	}

	public void setUnitName1(String unitName1)
	{
		this.unitName1 = unitName1;
	}

	public String getUnitInd2()
	{
		return this.unitInd2;
	}

	public void setUnitInd2(String unitInd2)
	{
		this.unitInd2 = unitInd2;
	}

	public String getUnitName2()
	{
		return this.unitName2;
	}

	public void setUnitName2(String unitName2)
	{
		this.unitName2 = unitName2;
	}

	public String getUnitInd3()
	{
		return this.unitInd3;
	}

	public void setUnitInd3(String unitInd3)
	{
		this.unitInd3 = unitInd3;
	}

	public String getUnitName3()
	{
		return this.unitName3;
	}

	public void setUnitName3(String unitName3)
	{
		this.unitName3 = unitName3;
	}

	public String getUnitInd4()
	{
		return this.unitInd4;
	}

	public void setUnitInd4(String unitInd4)
	{
		this.unitInd4 = unitInd4;
	}

	public String getUnitName4()
	{
		return this.unitName4;
	}

	public void setUnitName4(String unitName4)
	{
		this.unitName4 = unitName4;
	}

	public String getFullName()
	{
		return this.fullName;
	}

	public void setFullName(String fullName)
	{
		this.fullName = fullName;
	}

	public String getCommonName()
	{
		return this.commonName;
	}

	public void setCommonName(String commonName)
	{
		this.commonName = commonName;
	}

	public String getAuthor()
	{
		return this.author;
	}

	public void setAuthor(String author)
	{
		this.author = author;
	}

	public String getSource()
	{
		return this.source;
	}

	public void setSource(String source)
	{
		this.source = source;
	}

	public Integer getGroupPermittedToView()
	{
		return this.groupPermittedToView;
	}

	public void setGroupPermittedToView(Integer groupPermittedToView)
	{
		this.groupPermittedToView = groupPermittedToView;
	}

	public String getEnvironmentalProtectionStatus()
	{
		return this.environmentalProtectionStatus;
	}

	public void setEnvironmentalProtectionStatus(String environmentalProtectionStatus)
	{
		this.environmentalProtectionStatus = environmentalProtectionStatus;
	}

	public Integer getNodeNumber()
	{
		return this.nodeNumber;
	}

	public void setNodeNumber(Integer nodeNumber)
	{
		this.nodeNumber = nodeNumber;
	}

	public Integer getHighestChildNodeNumber()
	{
		return this.highestChildNodeNumber;
	}

	public void setHighestChildNodeNumber(Integer highestChildNodeNumber)
	{
		this.highestChildNodeNumber = highestChildNodeNumber;
	}

	public Short getAccepted()
	{
		return this.accepted;
	}

	public void setAccepted(Short accepted)
	{
		this.accepted = accepted;
	}

	public Integer getRankId()
	{
		return this.rankId;
	}

	public void setRankId(Integer rankId)
	{
		this.rankId = rankId;
	}

	public String getGroupNumber()
	{
		return this.groupNumber;
	}

	public void setGroupNumber(String groupNumber)
	{
		this.groupNumber = groupNumber;
	}

	public Set<Taxon> getAcceptedChildren()
	{
		return this.acceptedChildren;
	}

	public void setAcceptedChildren(Set<Taxon> acceptedChildren)
	{
		this.acceptedChildren = acceptedChildren;
	}

	public Taxon getAcceptedTaxon()
	{
		return this.acceptedTaxon;
	}

	public void setAcceptedTaxon(Taxon acceptedTaxon)
	{
		this.acceptedTaxon = acceptedTaxon;
	}

	public Set<Determination> getDeterminations()
	{
		return determinations;
	}

	public void setDeterminations(Set<Determination> determinations)
	{
		this.determinations = determinations;
	}

	public Set<TaxonCitation> getTaxonCitations()
	{
		return this.taxonCitations;
	}

	public void setTaxonCitations(Set<TaxonCitation> taxonCitations)
	{
		this.taxonCitations = taxonCitations;
	}

	public TaxonTreeDef getDefinition()
	{
		return this.definition;
	}

	public void setDefinition(TaxonTreeDef definition)
	{
		this.definition = definition;
	}

	public TaxonTreeDefItem getDefinitionItem()
	{
		return this.definitionItem;
	}

	public void setDefinitionItem(TaxonTreeDefItem definitionItem)
	{
		this.definitionItem = definitionItem;
		if( this.definitionItem!=null )
		{
			this.rankId = this.definitionItem.getRankId();
		}
	}

	public Taxon getParent()
	{
		return this.parent;
	}

	public void setParent(Taxon parent)
	{
		this.parent = parent;
	}


	public Set<Taxon> getChildren()
	{
		return this.children;
	}

	public void setChildren(Set<Taxon> children)
	{
		this.children = children;
	}

	public Long getTreeId()
	{
		return this.taxonId;
	}

	public void setTreeId(Long id)
	{
        this.taxonId = id;
	}

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
		String parentName = getParent()!=null ? getParent().getName() : "none";
		return "Taxon "+taxonId+": "+name+", child of "+parentName+", "+rankId+", "+nodeNumber+", "
				+highestChildNodeNumber;
	}

    public int getFullNameDirection()
    {
        return definition.getFullNameDirection();
    }

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
	 * Determines if the Taxon can be deleted.  This method checks whether or not
	 * the given Taxon is referenced by any foreign key contraints.  If no FKs are
	 * currently referring to this node, <code>true</code> is returned.
	 * 
	 * @return <code>true</code> if deletable
	 */
	public boolean canBeDeleted()
	{
		// force all collections to be loaded
		boolean noCitations = getTaxonCitations().isEmpty();
		boolean noAcceptedChildren = getAcceptedChildren().isEmpty();
		boolean noAttachments = getAttachments().isEmpty();
		boolean noDeter = getDeterminations().isEmpty();

		boolean descendantsDeletable = true;
		for( Taxon child : getChildren() )
		{
			if( !child.canBeDeleted() )
			{
				descendantsDeletable = false;
				break;
			}
		}

		if( noCitations&&noAcceptedChildren&&noAttachments&&noDeter&&descendantsDeletable )
		{
			return true;
		}

		return false;
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
        
        return fullNameBuilder.toString();
    }
	
	/**
	 * Returns the number of proper descendants for node.
	 * 
	 * @param node the node to count descendants for
	 * @return the number of proper descendants
	 */
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
			if( i == node )
			{
				return true;
			}
			
			i = i.getParent();
		}
		return false;
	}
	
	public Comparator<? super Taxon> getComparator()
	{
		return new TreeOrderSiblingComparator();
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    public Integer getTableId()
    {
        return 4;
    }

}
