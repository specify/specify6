/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
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
@SuppressWarnings("serial")
public class TreeNodePopupMenu extends JPopupMenu
{
	protected JList list;
	@SuppressWarnings("unchecked")
	protected TreeTableViewer ttv;
    protected JMenuItem delete;
    protected JMenuItem find;
    protected JMenuItem edit;
    protected JMenuItem newChild;
    protected JMenuItem subtree;
	
	/**
	 *
	 *
	 */
	@SuppressWarnings("unchecked")
	public TreeNodePopupMenu(TreeTableViewer owner)
	{
		this.ttv = owner;
		
		subtree = new JMenuItem("Subtree");
        find = new JMenuItem("Find next");
        edit = new JMenuItem("Edit...");
        delete = new JMenuItem("Delete");
        newChild = new JMenuItem("New child...");
        
        subtree.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				ttv.showSubtreeOfSelection(list);
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
    
    public void setDeleteEnabled(boolean enable)
    {
        delete.setEnabled(enable);
    }

    public void setNewEnabled(boolean enable)
    {
        newChild.setEnabled(enable);
    }
}
