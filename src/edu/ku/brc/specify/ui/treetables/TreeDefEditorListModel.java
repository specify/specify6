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

	public int indexOf(Object elem)
	{
		return defItems.indexOf(elem);
	}

	public void add(int index, TreeDefinitionItemIface element)
	{
		defItems.add(index,element);
		fireIntervalAdded(this,index,index);
	}

	public void add(TreeDefinitionItemIface o)
	{
		defItems.add(o);
		int index = defItems.size()-1;
		fireIntervalAdded(this,index,index);
	}
	
	public TreeDefinitionItemIface remove(int index)
	{
		TreeDefinitionItemIface removed = defItems.remove(index);
		fireIntervalRemoved(this,index,index);
		return removed;
	}

	public boolean remove(Object o)
	{
		int index = defItems.indexOf(o);
		boolean removed = defItems.remove(o);
		if(removed)
		{
			fireIntervalRemoved(this,index,index);
		}
		return removed;
	}
}
