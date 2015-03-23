/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.persistence.Transient;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.specify.ui.treetables.TreeNodePopupMenu;
import edu.ku.brc.specify.ui.treetables.TreeTableViewer;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

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
        
        treeClass         = Taxon.class;
        treeDefClass      = TaxonTreeDef.class;
        
        commandTypeString = TAXON;
        
        initialize();
	}
	
    @Transient
    @Override
    protected TaxonTreeDef getCurrentTreeDef()
    {
        return (TaxonTreeDef )((SpecifyAppContextMgr )AppContextMgr.getInstance()).getTreeDefForClass(Taxon.class);
    }
    
    /**
     * Get all the CollectionObjectIDs that that use this Taxon as the Current determination and put
     * them into the RecordSet.
     * @param taxon the Taxon
     * @param recordSet the RecordSet to be filled.
     */
    protected void fillRecordSet(final Taxon taxon, final RecordSet recordSet)
    {
        // The old way using Hibernate relationships was too slow, 
        // so instead I am using straight SQL it is a lot faster.
        String sql = "SELECT DISTINCT co.CollectionObjectID FROM taxon as tx INNER JOIN determination as dt ON tx.TaxonID = " +
        			 (taxon.getIsAccepted() ? "dt.PreferredTaxonID " : "dt.TaxonID ") +
                     "INNER JOIN collectionobject as co ON dt.CollectionObjectID = co.CollectionObjectID " +
                     "WHERE tx.TaxonID = "+taxon.getId()+" AND co.CollectionMemberID = COLMEMID";
        
        Vector<Integer> list = new Vector<Integer>();
        
        fillListWithIds(sql, list);
        
        for (Integer id : list)
        {
            recordSet.addItem(id);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.BaseTreeTask#createTreeViewer(boolean)
     */
    @Override
    protected TreeTableViewer<Taxon,TaxonTreeDef,TaxonTreeDefItem> createTreeViewer(final String titleArg, final boolean isEditMode)
    {
        final TreeTableViewer<Taxon, TaxonTreeDef, TaxonTreeDefItem> ttv = super.createTreeViewer(titleArg, isEditMode);

        if (ttv != null)
        {
            final TreeNodePopupMenu popup = ttv.getPopupMenu();
            // install custom popup menu items
            
            //Removing ITIS link now. Possibly will link to COL in future releases.
            /*
            JMenuItem getITIS = new JMenuItem("View ITIS Page");
            getITIS.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    StringBuilder itisURL = new StringBuilder("http://www.cbif.gc.ca/pls/itisca/taxastep?p_action=containing&p_format=html&taxa="); // XXX Externalize URL
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
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TaxonTreeTask.class, e1);
                        String errorMessage = getResourceString("ERROR_CANT_OPEN_WEBPAGE") + ": " + itisURL;
                        log.warn(errorMessage, e1);
                        UIRegistry.getStatusBar().setErrorMessage(errorMessage, e1);
                    }
                }
            });
            popup.add(getITIS, true);*/

            JMenuItem getDeters = new JMenuItem(getResourceString("TTV_ASSOC_COS"));
            getDeters.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    Taxon taxon = ttv.getSelectedNode(popup.getList());

                    // this call initializes all of the linked objects
                    // it only initializes the immediate links, not objects that are multiple hops away
                    //ttv.initializeNodeAssociations(taxon);
                    
                    if (taxon.getDeterminationCount(false) == 0)
                    {
                        UIRegistry.displayInfoMsgDlgLocalized("TTV_TAXON_NO_DETERS_FOR_NODE");
                        UIRegistry.getStatusBar().setLocalizedText("TTV_TAXON_NO_DETERS_FOR_NODE");
                        return;
                    }

                   final RecordSet recordSet = createColObjRSFromTaxon(taxon);

                    UIRegistry.getStatusBar().setText(getResourceString("TTV_OPENING_CO_FORM"));
                    // This is needed so the StatusBar gets updated
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run()
                        {
                            CommandDispatcher.dispatch(new CommandAction(DataEntryTask.DATA_ENTRY, DataEntryTask.EDIT_DATA, recordSet));
                        }
                    });
                }
            });
            popup.add(getDeters, true);
/* Removing lifemapper due to worldwind Java8 issues.. */             
            JMenuItem lifeMapperDisplay = new JMenuItem("Lifemapper");
            lifeMapperDisplay.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    Taxon     taxon     = ttv.getSelectedNode(popup.getList());
                    RecordSet recordSet = createColObjRSFromTaxon(taxon);
                    final Pair<Taxon, RecordSet> pair = new Pair<Taxon, RecordSet>(taxon, recordSet);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run()
                        {
                            UsageTracker.incrUsageCount("LM.TreeSearchReq");
                            CommandDispatcher.dispatch(new CommandAction("Lifemapper", "Display", pair));
                        }
                    });
                }
            });
            popup.add(lifeMapperDisplay, true);
            popup.setLifeMapperDisplayMenuItem(lifeMapperDisplay);
/*... Removing lifemapper due to worldwind java8 issues */            
            if (!isEditMode)
            {
                JMenuItem taxonMenu = new JMenuItem(getResourceString("TTV_NEW_CHILD"));
                taxonMenu.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        final Taxon taxon = ttv.getSelectedNode(popup.getList());
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run()
                            {
                                UsageTracker.incrUsageCount("TREE.AddKidFrView");
                                Pair<Object, Object> pair = new Pair<Object, Object>(taxon, "createchild");
                                CommandDispatcher.dispatch(new CommandAction("Data_Entry", "OpenNewView", pair));
                            }
                        });
                    }
                });
                popup.add(taxonMenu, true);
                popup.setNewChildInViewModeMenuItem(taxonMenu);
            }
        }
        
        return ttv;
    }
    
    /**
     * @param taxon
     * @return
     */
    private RecordSet createColObjRSFromTaxon(final Taxon taxonObj)
    {
        RecordSet recordSet = new RecordSet();
        recordSet.initialize();
        recordSet.set("TTV", CollectionObject.getClassTableId(), RecordSet.GLOBAL);

        fillRecordSet(taxonObj, recordSet);
        
        return recordSet;
    }
    
	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.BaseTask#getHelpTarget()
	 */
	@Override
	public String getHelpTarget()
	{
		return "Trees_About"; 
	}
    
    
}
