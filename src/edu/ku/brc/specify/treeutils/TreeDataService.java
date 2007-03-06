/**
 * 
 */
package edu.ku.brc.specify.treeutils;

import java.util.List;
import java.util.Set;

import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;

/**
 * This interface defines a session facade for all clients needing access to tree-structured data.
 *
 * @code_status Beta
 * @author jstewart
 */
public interface TreeDataService <T extends Treeable<T,D,I>,
									D extends TreeDefIface<T,D,I>,
									I extends TreeDefItemIface<T,D,I>>
{
    // various finding methods
	public List<T> findByName(D treeDef, String name);
	public T getRootNode(D treeDef);
	public List<D> getAllTreeDefs(Class<D> treeDefClass);
	public D getTreeDef(Class<D> defClass, long defId);
    public Set<T> getChildNodes(T parent);
    public int getDescendantCount(T node);
    
    // manipulation and inspection of tree nodes
    /**
     * Checks the business rules for the given node (and its children) to see if the
     * node can be deleted.
     * 
     * @param node the node to check
     * @return true if the node can be deleted w/o violating any business rules
     */
    public boolean canDeleteNode(T node);
    
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
    public void moveTreeNode(T node, T newParent, T rootNode);
    
    public boolean addNewTreeDefItem(I newDefItem, I parentDefItem);
    public boolean deleteTreeDefItem(I defItem);
}
