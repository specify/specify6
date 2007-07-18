/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JMenuItem;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.specify.ui.treetables.TreeNodePopupMenu;
import edu.ku.brc.specify.ui.treetables.TreeTableViewer;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.FormViewObj;
import edu.ku.brc.ui.forms.persist.AltView.CreationMode;
import edu.ku.brc.ui.validation.ValComboBox;
import edu.ku.brc.ui.validation.ValComboBoxFromQuery;

/**
 * Task that handles the UI for viewing taxonomy data.
 * 
 * @code_status Beta
 * @author jstewart
 */
public class TaxonTreeTask extends BaseTreeTask<Taxon,TaxonTreeDef,TaxonTreeDefItem>
{
	public static final String TAXON = "TaxonTree";
    
	/**
	 * Constructor.
	 */
	public TaxonTreeTask()
	{
        super(TAXON, getResourceString(TAXON));
        treeDefClass = TaxonTreeDef.class;
        icon = IconManager.getIcon(TAXON,IconManager.IconSize.Std24);
        
        menuItemText      = getResourceString("TaxonMenu");
        menuItemMnemonic  = getResourceString("TaxonMnemonic");
        starterPaneText   = getResourceString("TaxonStarterPaneText");
        commandTypeString = TAXON;
        
        initialize();
	}
	
    /* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.BaseTreeTask#showTree(edu.ku.brc.specify.datamodel.TreeDefIface)
	 */
	@Override
	protected TreeTableViewer<Taxon, TaxonTreeDef, TaxonTreeDefItem> showTree(TaxonTreeDef treeDef)
	{
		final TreeTableViewer<Taxon, TaxonTreeDef, TaxonTreeDefItem> ttv =  super.showTree(treeDef);
		
		if(ttv != null)
		{
			final TreeNodePopupMenu popup = ttv.getPopupMenu();
			// install custom popup menu items
//			JMenuItem getITIS = new JMenuItem("Get ITIS Info");
//			getITIS.addActionListener(new ActionListener()
//			{
//				public void actionPerformed(ActionEvent e)
//				{
//					Taxon taxon = ttv.getSelectedNode(popup.getList());
//					System.out.println("Get ITIS info for " + taxon.getFullName());
//				}
//			});
//			popup.add(getITIS);
			
			JMenuItem getDeters = new JMenuItem("Associated Determinations");
            getDeters.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					Taxon taxon = ttv.getSelectedNode(popup.getList());
					
                    // this call initializes all of the linked objects
                    // it only initializes the immediate links, not objects that are multiple hops away
                    ttv.initializeNodeAssociations(taxon);

                    Set<Determination> deters = taxon.getDeterminations();
                    
                    if (deters.size() == 0)
                    {
                        UIRegistry.getStatusBar().setText("No determinations refer to this taxon node");
                        return;
                    }
                    
                    int deterTblId = DBTableIdMgr.getInstance().getIdByClassName(Determination.class.getName());
                    RecordSet rs = new RecordSet("TTV.showDeterminations", deterTblId);
					for(Determination deter : deters)
					{
                        rs.addItem(deter.getDeterminationId());
					}
                    
                    UIRegistry.getStatusBar().setText("Opening determinations in a form");
                    CommandAction cmd = new CommandAction(DataEntryTask.DATA_ENTRY,DataEntryTask.EDIT_DATA,rs);
                    CommandDispatcher.dispatch(cmd);
				}
			});
			popup.add(getDeters);
		}

		return ttv;
	}
	
	protected void adjustTaxonForm(final FormViewObj form)
	{
	    log.debug("adjustTaxonForm(FormViewObj form)");

	    if (form.getAltView().getMode() != CreationMode.Edit)
	    {
	        return;
	    }

	    final Taxon taxonInForm = (Taxon)form.getDataObj();

	    final ValComboBoxFromQuery parentComboBox = (ValComboBoxFromQuery)form.getControlByName("parent");
	    final ValComboBox          rankComboBox   = (ValComboBox)form.getControlByName("definitionItem");

	    rankComboBox.addFocusListener(new FocusListener()
	    {
	        public void focusGained(FocusEvent e)
	        {
	            // set the contents of this combobox based on the value chosen as the parent

	            DefaultComboBoxModel model = (DefaultComboBoxModel)rankComboBox.getModel();
	            model.removeAllElements();

	            // this is the highest rank the edited item can possibly be
	            TaxonTreeDefItem topItem = null;
	            // this is the lowest rank the edited item can possibly be
	            TaxonTreeDefItem bottomItem = null;

	            Taxon parent = (Taxon)parentComboBox.getValue();
	            if (parent == null)
	            {
	                return;
	            }

	            // grab all the def items from just below the parent's item all the way to the next enforced level
	            // or to the level of the highest ranked child
	            topItem = parent.getDefinitionItem().getChild();

	            // find the child with the highest rank and set that child's def item as the bottom of the range
	            if (!taxonInForm.getChildren().isEmpty())
	            {
	                for (Taxon child: taxonInForm.getChildren())
	                {
	                    if (bottomItem==null || child.getRankId()>bottomItem.getRankId())
	                    {
	                        bottomItem = child.getDefinitionItem().getParent();
	                    }
	                }
	            }

	            TaxonTreeDefItem item = topItem;
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

	            if (model.getSize() == 1)
	            {
	                model.setSelectedItem(model.getElementAt(0));
	            }
	        }
	        public void focusLost(FocusEvent e)
	        {
	            // ignore this event
	        }
	    });

	    // TODO: setup listener to clear the hybrid parents comboboxes when the user turns off the isHybrid checkbox
	    // TODO: setup listener to clear the accepted taxon combobox when the user turns on the isAccepted checkbox
    }

    protected void adjustTaxonTreeDefForm(FormViewObj form)
    {
        log.debug("adjustTaxonTreeDefForm(FormViewObj form) " + form);
    }

    protected void adjustTaxonTreeDefItemForm(FormViewObj form)
    {
        log.debug("adjustTaxonTreeDefItemForm(FormViewObj form) " + form);
    }

    @Override
    protected void adjustForm(FormViewObj form)
    {
        if (form.getDataObj() instanceof Taxon)
        {
            adjustTaxonForm(form);
        }
        else if (form.getDataObj() instanceof TaxonTreeDef)
        {
            adjustTaxonTreeDefForm(form);
        }
        else if (form.getDataObj() instanceof TaxonTreeDefItem)
        {
            adjustTaxonTreeDefItemForm(form);
        }
    }
}
