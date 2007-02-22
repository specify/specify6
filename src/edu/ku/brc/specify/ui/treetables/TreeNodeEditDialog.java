/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.ui.treetables;

import java.awt.HeadlessException;

import javax.swing.DefaultComboBoxModel;

import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.ui.validation.ValComboBox;

/**
 * This is a dialog to be used when editing a tree node.
 * 
 * @code_status Beta
 * @author jstewart
 */
@SuppressWarnings("serial")
public class TreeNodeEditDialog <T extends Treeable<T,D,I>,
									D extends TreeDefIface<T,D,I>,
									I extends TreeDefItemIface<T,D,I>>
									extends EditFormDialog<T>
{
    private static final String DEF_ITEM_CB_ID = "defItemComboBox";
    
    public TreeNodeEditDialog(String viewSetName, String viewName, String title, EditDialogCallback<T> callback, boolean isNewObject) throws HeadlessException
	{
		super(viewSetName,viewName,title,callback,isNewObject);
	}
    
    /**
     * Sets data into the dialog.
     * 
     * @param dataObj the data object
     */
    @Override
	public void setData(final T dataObj)
    {
    	T node = dataObj;
        
        // get the appropriate set of def items for the combobox model
    	ValComboBox cb = (ValComboBox)form.getCompById(DEF_ITEM_CB_ID);
    	DefaultComboBoxModel model = (DefaultComboBoxModel)cb.getModel();
    	
        // this is the highest rank the edited item can possibly be
        I topItem = null;
        I bottomItem = null;
        if (node.getParent()!=null)
        {
            // grab all the def items from just below the parent's item all the way to the next enforced level
            // or to the level of the highest ranked child
            topItem = node.getParent().getDefinitionItem().getChild();
        }
        else
        {
            // this node has no parent, so it's current rank is the highest we can go
            topItem = node.getDefinitionItem();
        }
        
        // find the child with the highest rank and set that child's def item as the bottom of the range
        if (!node.getChildren().isEmpty())
        {
            for (T child: node.getChildren())
            {
                if (bottomItem==null || child.getRankId()>bottomItem.getRankId())
                {
                    bottomItem = child.getDefinitionItem().getParent();
                }
            }
        }
        
    	//I defaultItem = node.getDefinitionItem();
        
        I item = topItem;
    	boolean done = false;
    	while (!done)
    	{
            model.addElement(item);
    		
            if (item.getChild()==null || item.getIsEnforced()==Boolean.TRUE || item==bottomItem)
            {
                done = true;
            }
            item = item.getChild();
    	}
        
        super.setData(dataObj);
    }
}
