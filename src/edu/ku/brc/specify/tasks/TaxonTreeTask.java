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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Set;

import javax.swing.JCheckBox;
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
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.FormViewObj;

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
	
	@Override
    protected void adjustNodeForm(final FormViewObj form)
	{
	    super.adjustNodeForm(form);
        
        // Taxon specific stuff...
        
        // TODO: the form system MUST require the acceptedTaxon widget to be present if the isAccepted checkbox is present
        final JCheckBox acceptedCheckBox = (JCheckBox)form.getControlByName("isAccepted");
        final GetSetValueIFace acceptedParentWidget = (GetSetValueIFace)form.getControlByName("acceptedTaxon");
        if (acceptedCheckBox != null)
        {
            acceptedCheckBox.addItemListener(new ItemListener()
            {
                public void itemStateChanged(ItemEvent e)
                {
                    if (acceptedCheckBox.isSelected())
                    {
                        acceptedParentWidget.setValue(null, null);
                    }
                }
            });
        }
        
        // TODO: the form system MUST require the hybridParent1 and hybridParent2 widgets to be present if the isHybrid checkbox is present
        final JCheckBox hybridCheckBox = (JCheckBox)form.getControlByName("isHybrid");
        final GetSetValueIFace hybrid1Widget = (GetSetValueIFace)form.getControlByName("hybridParent1");
        final GetSetValueIFace hybrid2Widget = (GetSetValueIFace)form.getControlByName("hybridParent2");
        if (hybridCheckBox != null)
        {
            hybridCheckBox.addItemListener(new ItemListener()
            {
                public void itemStateChanged(ItemEvent e)
                {
                    if (!hybridCheckBox.isSelected())
                    {
                        hybrid1Widget.setValue(null, null);
                        hybrid2Widget.setValue(null, null);
                    }
                }
            });
        }
    }
	
//    protected void adjustTreeDefForm(FormViewObj form)
//    {
//        log.debug("adjustTaxonTreeDefForm(FormViewObj form) " + form);
//    }
//
//    protected void adjustTreeDefItemForm(FormViewObj form)
//    {
//        log.debug("adjustTaxonTreeDefItemForm(FormViewObj form) " + form);
//    }

    @Override
    public void adjustForm(FormViewObj form)
    {
        if (form.getDataObj() instanceof Taxon)
        {
//          // shutdown all open views
//          Vector<SubPaneIFace> openViews = new Vector<SubPaneIFace>();
//          openViews.addAll(viewToButtonMap.keySet());
//          
//          for (SubPaneIFace subPane: openViews)
//          {
//              subPane.shutdown();
//          }

            adjustNodeForm(form);
        }
//        else if (form.getDataObj() instanceof TaxonTreeDef)
//        {
//            adjustTreeDefForm(form);
//        }
//        else if (form.getDataObj() instanceof TaxonTreeDefItem)
//        {
//            adjustTreeDefItemForm(form);
//        }
    }
}
