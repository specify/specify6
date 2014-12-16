/* Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
	protected TreeTableViewer<?, ?, ?> ttv;
    protected JMenuItem delete;
    protected JMenuItem unSyn;
    protected JMenuItem find;
    protected JMenuItem edit;
    protected JMenuItem newChild;
    protected JMenuItem newChildInViewMode = null;
    protected JMenuItem lifeMapperDisplay = null;
    protected JMenuItem subtree;
    protected List<AbstractButton> selectionSensativeButtons;
	
	/**
	 * Constructor.
	 * @param owner the popup owner (the tree)
	 * @param isEditMode whether it is in edit mode or not
	 */
	public TreeNodePopupMenu(final TreeTableViewer<?,?,?> owner, final boolean isEditMode, final boolean canAdd, final boolean canDelete)
	{
		this.ttv = owner;
		
		subtree  = new JMenuItem(getResourceString("TTV_ZOOM_IN"));
        find     = new JMenuItem(getResourceString("TTV_FIND_NEXT"));
        
        selectionSensativeButtons = new Vector<AbstractButton>();
        
        selectionSensativeButtons.add(subtree);
        selectionSensativeButtons.add(find);
        
        
        edit     = new JMenuItem(isEditMode ? getResourceString("TTV_EDITING") : getResourceString("TTV_VIEWING"));
        if (isEditMode)
        {
            delete   = canDelete ? new JMenuItem(getResourceString("TTV_DELETE")) : null;
            unSyn    = new JMenuItem(getResourceString("TTV_UNSYN"));
            newChild = canAdd ? new JMenuItem(getResourceString("TTV_NEW_CHILD")) : null;
            
            selectionSensativeButtons.add(edit);
            if (canAdd)
            {
            	selectionSensativeButtons.add(newChild);
            }
            if (canDelete)
            {
            	selectionSensativeButtons.add(delete);
            }
            selectionSensativeButtons.add(unSyn);
        } else
        {
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

		edit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				ttv.editSelectedNode(list);
			}
		});
        this.add(edit);
		if (isEditMode)
		{
    
    		if (canDelete)
			{
				delete.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae)
					{
						ttv.deleteSelectedNode(list);
					}
				});
	            this.add(delete);
			}
    		
            unSyn.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    ttv.unSynSelectedNode(list);
                }
            });

            if (canAdd)
			{
				newChild.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae)
					{
						ttv.addChildToSelectedNode(list);
					}
				});
	            this.add(newChild);
			}
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

    public void setLifeMapperDisplayEnabled(boolean enable)
    {
    	if (lifeMapperDisplay != null)
    	{
    		lifeMapperDisplay.setEnabled(enable);
    	}
    }
    
    public void setNewEnabled(boolean enable)
    {
        if (newChild != null)
        {
            newChild.setEnabled(enable);
        }
        if (newChildInViewMode != null)
        {
        	newChildInViewMode.setEnabled(enable);
        }
    }
    
    public void setNewChildInViewModeMenuItem(JMenuItem menuItem)
    {
    	this.newChildInViewMode = menuItem;
        selectionSensativeButtons.add(menuItem);
    }
    
    public void setLifeMapperDisplayMenuItem(JMenuItem menuItem)
    {
    	/*Removing lifemapper because of worldwind java 8 issues... */
    	this.lifeMapperDisplay = menuItem;
        selectionSensativeButtons.add(menuItem);
    	/* ...Removing lifemapper because of worldwind java 8 issues */
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
