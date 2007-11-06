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
import java.net.URI;
import java.util.Set;

import javax.persistence.Transient;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.specify.datamodel.CollectionType;
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
import edu.ku.brc.util.AttachmentUtils;

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
	
    @Transient
    @Override
    protected TaxonTreeDef getCurrentTreeDef()
    {
        return CollectionType.getCurrentCollectionType().getTaxonTreeDef();
    }

    /* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.BaseTreeTask#showTree(edu.ku.brc.specify.datamodel.TreeDefIface)
	 */
    @Override
    protected TreeTableViewer<Taxon,TaxonTreeDef,TaxonTreeDefItem> createTreeViewer()
    {
        final TreeTableViewer<Taxon, TaxonTreeDef, TaxonTreeDefItem> ttv = super.createTreeViewer();

        if(ttv != null)
        {
            final TreeNodePopupMenu popup = ttv.getPopupMenu();
            // install custom popup menu items
            JMenuItem getITIS = new JMenuItem("View ITIS Page");
            getITIS.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    StringBuilder itisURL = new StringBuilder("http://www.cbif.gc.ca/pls/itisca/taxastep?p_action=containing&p_format=html&taxa=");
                    Taxon taxon = ttv.getSelectedNode(popup.getList());
                    String kingdom = taxon.getLevelName(TaxonTreeDef.KINGDOM);
                    String fullName = taxon.getFullName();
                    fullName = fullName.replaceAll(" ", "%20");
                    itisURL.append(fullName);
                    itisURL.append("&king=");
                    itisURL.append(kingdom);
                    try
                    {
                        AttachmentUtils.openURI(new URI(itisURL.toString()));
                    }
                    catch (Exception e1)
                    {
                        String errorMessage = getResourceString("ERROR_CANT_OPEN_WEBPAGE") + ": " + itisURL;
                        log.warn(errorMessage, e1);
                        UIRegistry.getStatusBar().setErrorMessage(errorMessage, e1);
                    }
                }
            });
            popup.add(getITIS);

            JMenuItem getDeters = new JMenuItem(getResourceString("TTV_TAXON_ASSOC_DETERS"));
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
                        UIRegistry.getStatusBar().setText(getResourceString("TTV_TAXON_NO_DETERS_FOR_NODE"));
                        return;
                    }

                    int deterTblId = DBTableIdMgr.getInstance().getIdByClassName(Determination.class.getName());
                    RecordSet rs = new RecordSet("TTV.showDeterminations", deterTblId);
                    for(Determination deter : deters)
                    {
                        rs.addItem(deter.getDeterminationId());
                    }

                    UIRegistry.getStatusBar().setText(getResourceString("TTV_OPENING_DETERS_FORM"));
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
	
    protected void adjustTreeDefForm(FormViewObj form)
    {
        log.debug("adjustTaxonTreeDefForm(FormViewObj form) " + form);
    }

    protected void adjustTreeDefItemForm(FormViewObj form)
    {
        log.debug("adjustTaxonTreeDefItemForm(FormViewObj form) " + form);
    }

    @Override
    public void adjustForm(FormViewObj form)
    {
        if (form.getDataObj() instanceof Taxon)
        {
            adjustNodeForm(form);
        }
        else if (form.getDataObj() instanceof TaxonTreeDef)
        {
            adjustTreeDefForm(form);
        }
        else if (form.getDataObj() instanceof TaxonTreeDefItem)
        {
            adjustTreeDefItemForm(form);
        }
    }
}
