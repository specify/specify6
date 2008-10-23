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
    protected JMenuItem unSyn;
    protected JMenuItem find;
    protected JMenuItem edit;
    protected JMenuItem newChild;
    protected JMenuItem subtree;
    protected List<AbstractButton> selectionSensativeButtons;
	
	/**
	 * Constructor.
	 * @param owner the popup owner (the tree)
	 * @param isEditMode whether it is in edit mode or not
	 */
	@SuppressWarnings("unchecked")
	public TreeNodePopupMenu(final TreeTableViewer owner, final boolean isEditMode)
	{
		this.ttv = owner;
		
		subtree  = new JMenuItem(getResourceString("TTV_ZOOM_IN"));
        find     = new JMenuItem(getResourceString("TTV_FIND_NEXT"));
        
        selectionSensativeButtons = new Vector<AbstractButton>();
        
        selectionSensativeButtons.add(subtree);
        selectionSensativeButtons.add(find);
        
        
        if (isEditMode)
        {
            edit     = new JMenuItem(getResourceString("TTV_EDITING"));
            delete   = new JMenuItem(getResourceString("TTV_DELETE"));
            unSyn    = new JMenuItem(getResourceString("TTV_UNSYN"));
            newChild = new JMenuItem(getResourceString("TTV_NEW_CHILD"));
            
            selectionSensativeButtons.add(edit);
            selectionSensativeButtons.add(newChild);
            selectionSensativeButtons.add(delete);
            selectionSensativeButtons.add(unSyn);
        } else
        {
            edit     = null;
            delete   = null;
            unSyn    = null;
            newChild = null;
        }
        
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

	      
        this.add(subtree);
        this.add(find);

		if (isEditMode)
		{
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
    
            unSyn.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    ttv.unSynSelectedNode(list);
                }
            });

    		newChild.addActionListener(new ActionListener()
    		{
    			public void actionPerformed(ActionEvent ae)
    			{
    				ttv.addChildToSelectedNode(list);
    			}
    		});
            this.add(edit);
            this.add(delete);
            this.add(newChild);
            this.add(unSyn);
		}

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
        if (delete != null)
        {
            delete.setEnabled(enable);
        }
    }

    public void setUnSynEnabled(boolean enable)
    {
        if (unSyn != null)
        {
            unSyn.setEnabled(enable);
        }
    }

    public void setNewEnabled(boolean enable)
    {
        if (newChild != null)
        {
            newChild.setEnabled(enable);
        }
    }
    
    public void setEditEnabled(boolean enable)
    {
        if (edit != null)
        {
            edit.setEnabled(enable);
        }
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
