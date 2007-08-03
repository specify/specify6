/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.treeutils;

import java.util.List;
import java.util.Set;

import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.ui.treetables.TreeNode;

/**
 * This interface defines a session facade for all clients needing access to tree-structured data.
 * 
 * @author jstewart
 * @code_status Complete
 * @param <T> an implementation class of {@link Treeable}
 * @param <D> an implementation class of {@link TreeDefIface}
 * @param <I> an implementation class of {@link TreeDefItemIface}
 */
public interface TreeDataService <T extends Treeable<T,D,I>,
									D extends TreeDefIface<T,D,I>,
									I extends TreeDefItemIface<T,D,I>>
{
	/**
     * Finds a tree node in the given tree with the given name.
     * 
	 * @param treeDef the tree to search
	 * @param name the node name to search for
	 * @return a {@link List} of matching nodes
	 */
	public List<T> findByName(D treeDef, String name);
	
    /**
     * Returns the root node of the given tree.
     * 
	 * @param treeDef the tree to inspect
	 * @return the root node
	 */
	public T getRootNode(D treeDef);
	
    public T getNodeById(Class<?> clazz, long id);
    
    /**
     * Returns all of the trees of the given class.
     * 
	 * @param treeDefClass a class implementing {@link TreeDefIface}
	 * @return a {@link List} of trees of the given class
	 */
	public List<D> getAllTreeDefs(Class<D> treeDefClass);
	
    /**
     * Returns the tree having the given class and ID.
     * 
	 * @param defClass the tree class
	 * @param defId the tree ID
	 * @return the tree definition
	 */
	public D getTreeDef(Class<D> defClass, long defId);
    
    /**
     * Returns the {@link Set} of the children of the given node.
     * 
     * @param parent the node for which to gather the children
     * @return the {@link Set} of child nodes
     */
    public Set<T> getChildNodes(T parent);
    
    public List<TreeNode> getChildTreeNodes(T parent);
    
    /**
     * Returns the number of descendants of the given node.
     * 
     * @param node the node to inspect
     * @return the number of descendants
     */
    public int getDescendantCount(T node);
    
    /**
     * Checks the business rules for the given object to see if it can be deleted.
     * 
     * @param o the object to check
     * @return true if the object can be deleted w/o violating any business rules
     */
    public boolean canDelete(Object o);
    
    /**
     * Determines if a child node can be added to this node.
     * 
     * @param node the node to check
     * @return true if a child can be added to the given node
     */
    public boolean canAddChildToNode(T node);
    
    /**
     * Deletes the given node from the DB.
     * 
     * @param node the node to be deleted
     */
    public void deleteTreeNode(T node);

    public void refresh(Object ... objects);
    
    public boolean updateNodeNumbersAfterNodeAddition(T newNode);
    
    public boolean updateNodeNumbersAfterNodeDeletion(T deletedNode);
    
    /**
     * Adds the given child node to the DB as a child of the given parent node.  The parent
     * node must already exist in the DB.
     * 
     * @param parent the parent node
     * @param child the new child node
     */
    public void addNewChild(T parent, T child);
    
    /**
     * Moves the given node from its current parent to the given new parent node.  This can
     * also be used to attach a new node to a parent.  The node to be moved doesn't not need
     * to have a non-null parent before the call.
     * 
     * @param node the node to be moved
     * @param newParent the new parent node
     */
    //public void moveTreeNode(T node, T newParent, T rootNode);
    public boolean moveTreeNode(T node, T newParent);
    
    /**
     * Creates a logical link between two nodes.  This link's meaning is dependent on the type, T.  Some
     * implementations of T will ignore this call.  Others might setup any sort of association between the
     * two nodes <code>source</code> and <code>destination</code>.
     * 
     * @param source any node
     * @param destination any node
     * @return a localized, human-readable status message, or null
     */
    public String createNodeLink(T source, T destination);
    
    /**
     * Adds a new {@link TreeDefItemIface} as a child of the given
     * {@link TreeDefItemIface}.
     * 
     * @param newDefItem the new child item
     * @param parentDefItem the parent item
     * @return true on success, false on failure
     */
    public boolean addNewTreeDefItem(I newDefItem, I parentDefItem);
    
    /**
     * Deletes the given {@link TreeDefItemIface}.
     * 
     * @param defItem the {@link TreeDefItemIface} to delete
     * @return true on success, false on failure
     */
    public boolean deleteTreeDefItem(I defItem);
    
    /**
     * Initializes any data associated with the given node.
     * 
     * @param node any tree node
     */
    public void initializeRelatedObjects(T node);
}
