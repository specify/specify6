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

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.ButtonBarBuilder;

import edu.ku.brc.specify.core.NavBoxLayoutManager;
import edu.ku.brc.specify.datamodel.TreeDefinitionItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.treeutils.TreeTableUtils;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.forms.MultiView;
import edu.ku.brc.specify.ui.forms.ViewMgr;
import edu.ku.brc.specify.ui.forms.Viewable;
import edu.ku.brc.specify.ui.forms.persist.AltView;
import edu.ku.brc.specify.ui.forms.persist.View;
import edu.ku.brc.specify.ui.validation.ValComboBox;

/**
 * Comments must be updated.  Most of this code, including comments, was taken from GenericDisplayDialog.java
 * 
 * This is a "generic" or more specifically "configurable" search dialog class. This enables you to specify a form to be used to enter the search criteria
 * and then the search definition it is to use to do the search and display the results as a table in the dialog. The resulting class is to be passed in
 * on construction so the results of the search can actually yield a Hibernate object.
 *
 * @author rods, jstewart
 *
 */
@SuppressWarnings("serial")
public class TreeNodeEditDialog extends JDialog implements ActionListener
{
    private static final Logger log  = Logger.getLogger(TreeNodeEditDialog.class);

    private static final String DEF_ITEM_CB_ID = "defItemComboBox";
    
    // Form Stuff
    protected MultiView      multiView;
    protected View           formView;
    protected Viewable       form;
    protected List<String>   fieldNames;
    
    // Members needed for creating results
    protected String         className;
    protected String         idFieldName;

    // UI
    protected JButton        okBtn;
    protected JButton		 cancelBtn;

    protected JPanel         contentPanel;
    
    protected TreeNodeDialogCallback callback;
    
    protected String initialName;

    /**
     * Constructs a {@link Treeable} node edit dialog from form info
     * @param viewSetName the viewset name
     * @param viewName the form name from the viewset
     * @param title the title (should be already localized before passing in)
     * @param className the name of the class to be created from the selected results
     * @param idFieldName the name of the field in the clas that is the primary key which is filled in from the search table id
     * @throws HeadlessException an exception
     */
    public TreeNodeEditDialog(final String viewSetName,
                                final String viewName,
                                final String title,
                                final String className,
                                final String idFieldName,
                                final TreeNodeDialogCallback callback) throws HeadlessException
    {
        super((Frame)UICacheManager.get(UICacheManager.FRAME), title, true);
        
        this.callback = callback;
        
        this.className   = className;
        this.idFieldName = idFieldName;

        createUI(viewSetName, viewName, title);

        setLocationRelativeTo((JFrame)(Frame)UICacheManager.get(UICacheManager.FRAME));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setModal(false);
    }

    protected void createUI(final String viewSetName,
                            final String viewName,
                            final String title)
    {
        formView = ViewMgr.getView(viewSetName, viewName);
        if (formView != null)
        {
            multiView   = new MultiView(null, formView, AltView.CreationMode.Edit, false, true);
            form = multiView.getCurrentView();

        } else
        {
            log.error("Couldn't load form with name ["+viewSetName+"] Id ["+viewName+"]");
        }
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));

        panel.add(multiView, BorderLayout.NORTH);
        contentPanel = new JPanel(new NavBoxLayoutManager(0,2));

        okBtn = new JButton(getResourceString("Close"));
        okBtn.addActionListener(this);
        getRootPane().setDefaultButton(okBtn);
        
        cancelBtn = new JButton(getResourceString("Cancel"));
        cancelBtn.addActionListener(this);

        ButtonBarBuilder btnBuilder = new ButtonBarBuilder();
        btnBuilder.addGlue();
        btnBuilder.addGriddedButtons(new JButton[] { cancelBtn, okBtn });

        panel.add(btnBuilder.getPanel(), BorderLayout.SOUTH);

        setContentPane(panel);
        pack();
    }
    
    /**
     * Sets data into the dialog.
     * 
     * @param dataObj the data object
     */
    public void setData(final Treeable dataObj)
    {
    	initialName = dataObj.getName();
    	
    	ValComboBox cb = (ValComboBox)form.getCompById(DEF_ITEM_CB_ID);
    	DefaultComboBoxModel model = (DefaultComboBoxModel)cb.getModel();
    	Treeable parent = dataObj.getParentNode();
    	
    	// if we are editing the root node, just put one def item in
    	// the item selection box
    	if( parent == null )
    	{
    		model.addElement(dataObj.getDefItem().getName());
    		cb.setEnabled(false);
    		form.setDataObj(dataObj);
    		return;
    	}
    	
    	TreeDefinitionItemIface parentDefItem = parent.getDefItem();
    	boolean done = false;
    	while( !done )
    	{
    		TreeDefinitionItemIface item = parentDefItem.getChildItem();
    		if( item != null )
    		{
    			model.addElement(item.getName());
    			if( dataObj.getDefItem() == item )
    			{
    				cb.setValue(item.getName(), null);
    			}
    			parentDefItem = item;
    			if( item.getIsEnforced() != null  && item.getIsEnforced().booleanValue() == true )
    			{
    				done = true;
    			}
    		}
    		else
    		{
    			done = true;
    		}
    	}
        form.setDataObj(dataObj);
    }

    protected void setDefItemByName( Treeable node, String defItemName )
    {
    	TreeDefinitionItemIface item = TreeTableUtils.getDefItemByName(node.getTreeDef(),defItemName);
    	node.setDefItem(item);
    }

    public void actionPerformed(ActionEvent e)
    {
        // Handle clicks on the OK buttons.
    	if( e.getSource().equals(okBtn) )
    	{
    		okAction(e);
    	}
    	else if( e.getSource().equals(cancelBtn) )
    	{
    		cancelAction(e);
    	}
    	return;
    }
    
    public void okAction(ActionEvent e)
    {
        // Handle clicks on the OK buttons.
        setVisible(false);
        
        form.getDataFromUI();
        
        ValComboBox cb = (ValComboBox)form.getCompById(DEF_ITEM_CB_ID);
        String defItemName = (String)cb.getValue();
        Treeable node = (Treeable)form.getDataObj();
        
        if( !node.getName().equals(initialName) )
        {
        	TreeTableUtils.fixAllDescendantFullNames(node);
        }
        setDefItemByName(node, defItemName);
        callback.editCompleted(node);
    }
    
    public void cancelAction(ActionEvent e)
    {
        setVisible(false);

    	Treeable node = (Treeable)form.getDataObj();
    	callback.editCancelled(node);
    }
    
    public interface TreeNodeDialogCallback
    {
    	public void editCompleted(Treeable node);
    	public void editCancelled(Treeable node);
    }
}
