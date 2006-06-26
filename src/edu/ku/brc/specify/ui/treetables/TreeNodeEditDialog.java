/*
 * Filename:    $RCSfile: GenericDisplayDialog.java,v $
 * Author:      $Author: rods, jstewart $
 * Revision:    $Revision: 1.3 $
 * Date:        $Date: 2005/10/20 12:53:02 $
 *
 * This library is free software; you can redistribute it and/or
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
import java.beans.PropertyChangeListener;
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
 * @author rods
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
    
    protected PropertyChangeListener propertyChangeListener = null;

    // Members needed for creating results
    protected String         className;
    protected String         idFieldName;

    // UI
    protected JButton        okBtn;

    protected JPanel         contentPanel;
    
    protected TreeTableViewer treeViewer;


    /**
     * Constructs a taxon node edit dialog from form info
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
                                final TreeTableViewer treeViewer) throws HeadlessException
    {
        super((Frame)UICacheManager.get(UICacheManager.FRAME), title, true);
        
        this.treeViewer = treeViewer;
        
        this.className   = className;
        this.idFieldName = idFieldName;

        createUI(viewSetName, viewName, title);

        setLocationRelativeTo((JFrame)(Frame)UICacheManager.get(UICacheManager.FRAME));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setModal(false);
    }

    /**
     * Creates the Default UI
     *
     */
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

        ButtonBarBuilder btnBuilder = new ButtonBarBuilder();
        btnBuilder.addGlue();
        btnBuilder.addGriddedButtons(new JButton[] { okBtn });

        panel.add(btnBuilder.getPanel(), BorderLayout.SOUTH);

        setContentPane(panel);
        pack();
    }
    
    /**
     * Set a listener to know when the dialog is closed
     * @param propertyChangeListener the listener
     */
    public void setCloseListener(final PropertyChangeListener propertyChangeListener)
    {
        this.propertyChangeListener = propertyChangeListener;
    }
    
    /**
     * Sets data into the dialog
     * @param dataObj the data object
     */
    public void setData(final Treeable dataObj)
    {
    	ValComboBox cb = (ValComboBox)form.getCompById(DEF_ITEM_CB_ID);
    	DefaultComboBoxModel model = (DefaultComboBoxModel)cb.getModel();
    	TreeDefinitionItemIface parentDefItem = dataObj.getParentNode().getDefItem();
    	boolean done = false;
    	while( !done )
    	{
    		TreeDefinitionItemIface item = parentDefItem.getChildItem();
    		if( item != null )
    		{
    			model.addElement(item.getName());
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
    	TreeDefinitionItemIface parentItem = node.getParentNode().getDefItem();
    	boolean done = false;
    	while( !done )
    	{
    		TreeDefinitionItemIface item = parentItem.getChildItem();
    		if( item == null )
    		{
    			throw new RuntimeException("No def item by this name below parent's def item");
    		}
    		
    		if( item.getName().equals(defItemName) )
    		{
    			node.setDefItem(item);
    			node.setRankId(item.getRankId());
    			done = true;
    		}
    		
    		parentItem = item;
    	}
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        // Handle clicks on the OK and Cancel buttons.
        setVisible(false);
        if( propertyChangeListener != null )
        {
        	propertyChangeListener.propertyChange(null);
            propertyChangeListener = null;
        }
        
        form.getDataFromUI();
        ValComboBox cb = (ValComboBox)form.getCompById(DEF_ITEM_CB_ID);
        String defItemName = (String)cb.getValue();
        Treeable node = (Treeable)form.getDataObj();
        setDefItemByName(node, defItemName);
        treeViewer.newNodeEntryComplete(node);
    }
}
