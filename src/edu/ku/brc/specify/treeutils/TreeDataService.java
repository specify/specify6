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
 *
 * @code_status Beta
 * @author jstewart
 */
public interface TreeDataService <T extends Treeable<T,D,I>,
									D extends TreeDefIface<T,D,I>,
									I extends TreeDefItemIface<T,D,I>>
{
	public void init();
	public void fini();
	public List<T> findByName(D treeDef, String name);
	public void saveTree(T rootNode, boolean fixNodeNumbers, Set<T> addedNodes, Set<T> deletedNodes);
	public void saveTreeDef(D treeDef,List<I> deletedItems);
	public Set<T> getTreeNodes(I defItem);
	public T getRootNode(D treeDef);
	public List<D> getAllTreeDefs(Class<D> treeDefClass);
	public D getTreeDef(Class<D> defClass, long defId);
	public void loadAllDescendants(T node);
}
