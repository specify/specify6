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

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import edu.ku.brc.util.Nameable;
import edu.ku.brc.util.Rankable;

/**
 * Describes any class where a collection of its objects can be modeled as
 * a tree.  Each instance of the implementing class represents a single node
 * in a tree.  Database tables can contain multiple trees simultaneously as
 * long as each node in a given tree has a common identifier, the tree definition.
 * Each node in the tree must have a unique ID, which is the primary key of
 * the corresponding database table.  Each node must also be numbered (the
 * node number) using a depth-first traversal of the tree.  The highest child
 * node number field contains the largest node number in the tree that is a
 * descendant of the given node.  The rank id represents the node's depth in
 * the tree.  Possible depths are defined in the tree definition.
 * 
 * A few of the methods defined in this interface are expected, at times,
 * to throw IllegalArgumentException.  This occurs when a setter
 * method is called on a Treeable object, but the passed in argument
 * represents a tree definition or tree definition item of another type of 
 * Treeable object.  For example, if setTreeDef(TreeDefinitionIface)
 * is called on a Taxon object, but the argument given is an instance of
 * GeographyTreeDef, an IllegalArgumentException will be thrown.
 *
 * @code_status Unknown (auto-generated)
 * 
 * @author jstewart
 */
public interface Treeable<N extends Treeable<N,D,I>,
                          D extends TreeDefIface<N,D,I>,
                          I extends TreeDefItemIface<N,D,I>>
                            extends Rankable, Nameable
{
	/**
	 * An indicator that node full names should start with highest order
	 * nodes and continue to the lowest order nodes.
	 * @see #REVERSE
	 */
	public static final int FORWARD = 1;
	/**
	 * An indicator that node full names should start with lowest order
	 * nodes and continue to the highest order nodes.
	 * @see #FORWARD
	 */
	public static final int REVERSE = -1;

	public void initialize();
	
	/**
	 * @return the ID (primary key) of this node
	 */
	public Long getTreeId();
	
	/**
	 * Sets the ID of this node
	 * 
	 * @param id the new ID value
	 */
	public void setTreeId(Long id);
	
	/**
	 * Returns the parent node object.  If called on the root node of
	 * the tree, returns null.
	 * 
	 * @return the parent node object
	 */
	public N getParent();
	
	/**
	 * Re-parents the node by setting its parent to <code>node</code>.
	 * 
	 * @param node the new parent
	 */
	public void setParent(N parent);
	
	public Set<N> getChildren();
	
	public void setChildren( Set<N> children );
	
	public void addChild( N child );

	public void removeChild( N child );
	/**
	 * @return the node number as determined by a depth-first traversal of the containing tree
	 */
	public Integer getNodeNumber();
	
	/**
	 * Sets the depth-first traversal node number of this object
	 * 
	 * @param nodeNumber
	 */
	public void setNodeNumber(Integer nodeNumber);
	
	/**
	 * @return the node number of the descdendant having the largest node number
	 */
	public Integer getHighestChildNodeNumber();
	
	/**
	 * @param nodeNumber the node number of the descdendant having the largest node number
	 */
	public void setHighestChildNodeNumber(Integer nodeNumber);
		
	/**
	 * @return the remarks of this node
	 */
	public String getRemarks();
	
	/**
	 * @param name the new remarks of the node
	 */
	public void setRemarks(String remarks);
	
	/**
	 * @return the series ID of the tree containing this node
	 */
	public D getDefinition();
	
	/**
	 * @param id the new series ID of the tree that this node is contained in
	 * 
	 * @throws IllegalArgumentException if treeDef isn't an object of the correct type to represent this Treeable's tree definition item
	 */
	public void setDefinition(D treeDef);
	
	/**
	 * @return the TreeDefinitionItemIface object representing this Treeable's location in the tree
	 */
	public I getDefinitionItem();
	
	/**
	 * @param defItem the new TreeDefinitionItemIface object representing this Treeable's location in the tree
	 * 
	 * @throws IllegalArgumentException if defItem isn't an object of the correct type to represent this Treeable's tree definition
	 */
	public void setDefinitionItem(I defItem);

	public String getFullName();
	public void setFullName(String fullName);
	
	public Date getTimestampCreated();
	public void setTimestampCreated(Date created);
	
	public Date getTimestampModified();
	public void setTimestampModified(Date modified);
	
	public String getLastEditedBy();
	public void setLastEditedBy(String user);
	
	public int getFullNameDirection();
	public String getFullNameSeparator();
	
	public boolean childrenAllowed();
	public List<N> getAllDescendants();
    public int getDescendantCount();
	public List<N> getAllAncestors();
	public void fixFullNameForAllDescendants();
	
	public boolean isDescendantOf(N node);
	
	public Comparator<? super N> getComparator();
}
