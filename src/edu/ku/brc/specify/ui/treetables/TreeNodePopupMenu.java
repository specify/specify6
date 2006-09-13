package edu.ku.brc.specify.ui.treetables;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.ku.brc.specify.datamodel.Treeable;

/**
 * A JPopupMenu customized for use within a TreeTableViewer for displaying
 * actions that can be performed on nodes.
 *
 * @code_status Beta
 * @author jstewart
 */
public class TreeNodePopupMenu extends JPopupMenu
{
	protected JList list;
	@SuppressWarnings("unchecked")
	protected TreeTableViewer ttv;
	
	/**
	 *
	 *
	 */
	@SuppressWarnings("unchecked")
	public TreeNodePopupMenu(TreeTableViewer owner)
	{
		this.ttv = owner;
		
		JMenuItem subtree = new JMenuItem("Subtree");
		JMenuItem expand = new JMenuItem("Expand all descendants");
		JMenuItem find = new JMenuItem("Find next");
		JMenuItem edit = new JMenuItem("Edit...");
		JMenuItem delete = new JMenuItem("Delete");
		JMenuItem newChild = new JMenuItem("New child...");
		
		subtree.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				ttv.showSubtreeOfSelection(list);
			}
		});

		expand.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				ttv.expandAllDescendantsOfSelection(list);
			}
		});

		find.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				Object selection = list.getSelectedValue();
				if( selection == null )
				{
					return;
				}
				Treeable<?, ?, ?> node = (Treeable<?, ?, ?>)selection;
				ttv.findNext(list,true,node);
			}
		});

		edit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				ttv.editSelectedNode(list);
			}
		});

		delete.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				ttv.deleteSelectedNode(list);
			}
		});

		newChild.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				ttv.addChildToSelectedNode(list);
			}
		});
		
		this.add(subtree);
		this.add(expand);
		this.add(find);
		this.add(edit);
		this.add(delete);
		this.add(newChild);
	}
	
	public void setList(JList list)
	{
		this.list = list;
	}
	
	public JList getList()
	{
		return list;
	}
}
