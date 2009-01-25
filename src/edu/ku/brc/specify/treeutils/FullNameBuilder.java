/**
 * 
 */
package edu.ku.brc.specify.treeutils;

import java.util.LinkedList;

import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;

/**
 * @author tnoble
 *
 *Called by FullNameRebuilder to build FullNames during TreeTraversal
 */
public class FullNameBuilder<T extends Treeable<T, D, I>, D extends TreeDefIface<T, D, I>, I extends TreeDefItemIface<T, D, I>> 
	
{
	protected final D treedef;
	
	/**
	 * @param treedef
	 */
	public FullNameBuilder(final D treedef)
	{
		this.treedef = treedef;
	}
	
	/**
	 * @param node
	 * @param parents
	 * @return the FullName for node.
	 */
	public String buildFullName(FullNameRebuilder<T,D,I>.NodeInfo node, LinkedList<FullNameRebuilder<T,D,I>.NodeInfo> parents)
	{
		//XXX need to adapt stuff in TreeHelper.getFullName for this context.
		StringBuilder sb = new StringBuilder();
		for (FullNameRebuilder<T,D,I>.NodeInfo parent : parents)
		{
			sb.append(parent.getName() + " ");
		}
		sb.append(node.getName());
		return sb.toString();
	}
}
