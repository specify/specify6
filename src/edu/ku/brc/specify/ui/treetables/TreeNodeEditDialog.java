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
 * Comments must be updated.  Most of this code, including comments, was taken from DBObjDisplayDialog.java
 * 
 * This is a "generic" or more specifically "configurable" search dialog class. This enables you to specify a form to be used to enter the search criteria
 * and then the search definition it is to use to do the search and display the results as a table in the dialog. The resulting class is to be passed in
 * on construction so the results of the search can actually yield a Hibernate object.

 * @code_status Code Freeze
 * @author rods, jstewart
 *
 */
@SuppressWarnings("serial")
public class TreeNodeEditDialog <T extends Treeable<T,D,I>,
									D extends TreeDefIface<T,D,I>,
									I extends TreeDefItemIface<T,D,I>>
									extends EditFormDialog<T>
{
    private static final String DEF_ITEM_CB_ID = "defItemComboBox";
    
    public TreeNodeEditDialog(String viewSetName, String viewName, String title, EditDialogCallback<T> callback) throws HeadlessException
	{
		super(viewSetName,viewName,title,callback);
		// TODO Auto-generated constructor stub
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
    	ValComboBox cb = (ValComboBox)form.getCompById(DEF_ITEM_CB_ID);
    	DefaultComboBoxModel model = (DefaultComboBoxModel)cb.getModel();
    	T parent = node.getParent();
    	
    	// if we are editing the root node, just put one def item in
    	// the item selection box
    	if( parent == null )
    	{
    		model.addElement(node.getDefinitionItem().getName());
    		cb.setEnabled(false);
    		form.setDataObj(node);
    		return;
    	}
    	
    	I parentDefItem = parent.getDefinitionItem();
    	I defaultItem = null;
    	boolean done = false;
    	while( !done )
    	{
    		I item = parentDefItem.getChild();
    		if( item != null )
    		{
    			model.addElement(item.getName());
    			if( node.getDefinitionItem() == item )
    			{
    				cb.setValue(item.getName(), null);
    			}
    			if( item.getIsEnforced() != null && item.getIsEnforced().booleanValue() == true )
    			{
    				defaultItem = item;
    				done = true;
    			}
    			parentDefItem = item;
    		}
    		else
    		{
    			done = true;
    		}
    	}
    	if(node.getDefinitionItem()==null && defaultItem!=null)
    	{
    		cb.setValue(defaultItem.getName(),null);
    	}
        form.setDataObj(node);
    }

    protected void setDefItemByName( T node, String defItemName )
    {
    	I item = node.getDefinition().getDefItemByName(defItemName);
    	node.setDefinitionItem(item);
    	node.setRankId(item.getRankId());
    }

    @SuppressWarnings("unchecked")
	@Override
	protected void getData()
    {
    	super.getData();
    	
        ValComboBox cb = (ValComboBox)form.getCompById(DEF_ITEM_CB_ID);
        String defItemName = (String)cb.getValue();
        T node = (T)form.getDataObj();
        
        setDefItemByName(node, defItemName);
    }
}
