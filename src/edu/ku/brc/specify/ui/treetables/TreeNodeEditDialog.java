/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.ui.treetables;

import java.awt.HeadlessException;

import javax.swing.DefaultComboBoxModel;

import edu.ku.brc.specify.datamodel.TreeDefinitionItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.ui.validation.ValComboBox;

/**
 * Comments must be updated.  Most of this code, including comments, was taken from DBObjDisplayDialog.java
 * 
 * This is a "generic" or more specifically "configurable" search dialog class. This enables you to specify a form to be used to enter the search criteria
 * and then the search definition it is to use to do the search and display the results as a table in the dialog. The resulting class is to be passed in
 * on construction so the results of the search can actually yield a Hibernate object.
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods, jstewart
 *
 */
@SuppressWarnings("serial")
public class TreeNodeEditDialog extends EditFormDialog
{
    private static final String DEF_ITEM_CB_ID = "defItemComboBox";
    
    public TreeNodeEditDialog(String viewSetName, String viewName, String title, String className, String idFieldName, EditDialogCallback callback) throws HeadlessException
	{
		super(viewSetName,viewName,title,className,idFieldName,callback);
		// TODO Auto-generated constructor stub
	}
    
    /**
     * Sets data into the dialog.
     * 
     * @param dataObj the data object
     */
    public void setData(final Object dataObj)
    {
    	if(!(dataObj instanceof Treeable))
    	{
    		return;
    	}
    	
    	Treeable node = (Treeable)dataObj;
    	ValComboBox cb = (ValComboBox)form.getCompById(DEF_ITEM_CB_ID);
    	DefaultComboBoxModel model = (DefaultComboBoxModel)cb.getModel();
    	Treeable parent = node.getParentNode();
    	
    	// if we are editing the root node, just put one def item in
    	// the item selection box
    	if( parent == null )
    	{
    		model.addElement(node.getDefItem().getName());
    		cb.setEnabled(false);
    		form.setDataObj(node);
    		return;
    	}
    	
    	TreeDefinitionItemIface parentDefItem = parent.getDefItem();
    	TreeDefinitionItemIface defaultItem = null;
    	boolean done = false;
    	while( !done )
    	{
    		TreeDefinitionItemIface item = parentDefItem.getChildItem();
    		if( item != null )
    		{
    			model.addElement(item.getName());
    			if( node.getDefItem() == item )
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
    	if(node.getDefItem()==null && defaultItem!=null)
    	{
    		cb.setValue(defaultItem.getName(),null);
    	}
        form.setDataObj(node);
    }

    protected void setDefItemByName( Treeable node, String defItemName )
    {
    	TreeDefinitionItemIface item = node.getTreeDef().getDefItemByName(defItemName);
    	node.setDefItem(item);
    	node.setRankId(item.getRankId());
    }

    protected void getData()
    {
    	super.getData();
    	
        ValComboBox cb = (ValComboBox)form.getCompById(DEF_ITEM_CB_ID);
        String defItemName = (String)cb.getValue();
        Treeable node = (Treeable)form.getDataObj();
        
        setDefItemByName(node, defItemName);
    }
}
