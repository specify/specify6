/**
 * 
 */
package edu.ku.brc.specify.ui.treetables;

import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

import javax.swing.AbstractListModel;

import edu.ku.brc.specify.datamodel.TreeDefinitionItemIface;
import edu.ku.brc.util.RankBasedComparator;

/**
 *
 *
 * @author jstewart
 * @version %I% %G%
 */
public class TreeDefEditorListModel extends AbstractListModel
{
	protected Vector<TreeDefinitionItemIface> defItems;
	
	/**
	 *
	 *
	 */
	public TreeDefEditorListModel(Collection<TreeDefinitionItemIface> defItems)
	{
		this.defItems = new Vector<TreeDefinitionItemIface>(defItems);
		Collections.sort(this.defItems,new RankBasedComparator());
	}

	/**
	 *
	 *
	 * @see javax.swing.ListModel#getElementAt(int)
	 * @param index
	 * @return
	 */
	public Object getElementAt(int index)
	{
		return defItems.get(index);
	}

	/**
	 *
	 *
	 * @see javax.swing.ListModel#getSize()
	 * @return
	 */
	public int getSize()
	{
		return defItems.size();
	}

}
