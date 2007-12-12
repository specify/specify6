/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.ui.treetables;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

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
    protected List<AbstractButton> selectionSensativeButtons;
	
	/**
	 *
	 *
	 */
	@SuppressWarnings("unchecked")
	public TreeNodePopupMenu(TreeTableViewer owner)
	{
		this.ttv = owner;
		
		subtree = new JMenuItem(getResourceString("TTV_ZOOM_IN"));
        find = new JMenuItem("Find next");
        edit = new JMenuItem("Edit...");
        delete = new JMenuItem("Delete");
        newChild = new JMenuItem("New child...");
        
        selectionSensativeButtons = new Vector<AbstractButton>();
        selectionSensativeButtons.add(subtree);
        selectionSensativeButtons.add(find);
        selectionSensativeButtons.add(edit);
        selectionSensativeButtons.add(newChild);
        selectionSensativeButtons.add(delete);
        
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
				TreeNode selection = (TreeNode)list.getSelectedValue();
				if( selection == null )
				{
					return;
				}
                
				ttv.findNext(list,true,selection);
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
    
    public void setEditEnabled(boolean enable)
    {
        edit.setEnabled(enable);
    }
    
    public void setFindEnabled(boolean enable)
    {
        find.setEnabled(enable);
    }
    
    public void setSubtreeEnabled(boolean enable)
    {
        subtree.setEnabled(enable);
    }
    
    public void setSelectionSensativeButtonsEnabled(boolean enable)
    {
        for (AbstractButton ab: selectionSensativeButtons)
        {
            ab.setEnabled(enable);
        }
    }

    public JMenuItem add(JMenuItem menuItem, boolean selectionSensative)
    {
        JMenuItem mi =  super.add(menuItem);
        if (selectionSensative)
        {
            selectionSensativeButtons.add(menuItem);
        }
        return mi;
    }
}
