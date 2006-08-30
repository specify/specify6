/**
 * 
 */
package edu.ku.brc.specify.treeutils;

import java.util.List;
import java.util.Set;

import edu.ku.brc.specify.datamodel.TreeDefinitionIface;
import edu.ku.brc.specify.datamodel.TreeDefinitionItemIface;
import edu.ku.brc.specify.datamodel.Treeable;

/**
 *
 *
 * @author jstewart
 * @version %I% %G%
 */
public interface TreeDataService
{
	public void init();
	public void fini();
	public List<Treeable> findByName(TreeDefinitionIface treeDef, String name);
	public void saveTree(Treeable rootNode, boolean fixNodeNumbers, Set<Treeable> addedNodes, Set<Treeable> deletedNodes);
	public void saveTreeDef(TreeDefinitionIface treeDef,List<TreeDefinitionItemIface> deletedItems);
	public Set<Treeable> getTreeNodes(TreeDefinitionItemIface defItem);
	public Treeable getRootNode(TreeDefinitionIface treeDef);
	public List<TreeDefinitionIface> getAllTreeDefs(Class treeDefClass);
	public void loadAllDescendants(Treeable node);
}
