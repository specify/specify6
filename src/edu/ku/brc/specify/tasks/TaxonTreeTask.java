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
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.persistence.Transient;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDefItem;
import edu.ku.brc.specify.ui.treetables.TreeNodePopupMenu;
import edu.ku.brc.specify.ui.treetables.TreeTableViewer;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.UIRegistry;
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
        
        treeClass         = Taxon.class;
        treeDefClass      = TaxonTreeDef.class;
        
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
        return AppContextMgr.getInstance().getClassObject(Discipline.class).getTaxonTreeDef();
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
        String sql = "SELECT co.CollectionObjectID FROM taxon as tx INNER JOIN determination as dt ON tx.TaxonID = dt.TaxonID " +
                     "INNER JOIN determinationstatus as ds ON dt.DeterminationStatusID = ds.DeterminationStatusID " +
                     "INNER JOIN collectionobject as co ON dt.CollectionObjectID = co.CollectionObjectID " +
                     "WHERE tx.TaxonID = %d AND co.CollectionMemberID = COLMEMID AND ds.Type = 1";
        sql = QueryAdjusterForDomain.getInstance().adjustSQL(String.format(sql, taxon.getTaxonId()));
        
        Connection conn = null;        
        Statement  stmt = null;
        try
        {
            conn = DBConnection.getInstance().createConnection();
            stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                recordSet.addItem(rs.getInt(1));
            }
            rs.close();

        } 
        catch (SQLException ex)
        {
            log.error("SQLException: " + ex.toString()); //$NON-NLS-1$
            log.error(ex.getMessage());
            
        } finally
        {
            try 
            {
                if (stmt != null)
                {
                    stmt.close();
                }
                if (conn != null)
                {
                    conn.close();
                }
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
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
                        String errorMessage = getResourceString("ERROR_CANT_OPEN_WEBPAGE") + ": " + itisURL;
                        log.warn(errorMessage, e1);
                        UIRegistry.getStatusBar().setErrorMessage(errorMessage, e1);
                    }
                }
            });
            popup.add(getITIS, true);

            JMenuItem getDeters = new JMenuItem(getResourceString("TTV_ASSOC_COS"));
            getDeters.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    Taxon taxon = ttv.getSelectedNode(popup.getList());

                    // this call initializes all of the linked objects
                    // it only initializes the immediate links, not objects that are multiple hops away
                    ttv.initializeNodeAssociations(taxon);
                    
                    if (taxon.getDeterminations().size() == 0)
                    {
                        UIRegistry.getStatusBar().setText(getResourceString("TTV_TAXON_NO_DETERS_FOR_NODE"));
                        return;
                    }

                    final RecordSet recordSet = new RecordSet();
                    recordSet.initialize();
                    recordSet.set(UIRegistry.getResourceString("TTV.showCollectionObjects"), CollectionObject.getClassTableId(), RecordSet.GLOBAL);

                    fillRecordSet(taxon, recordSet);

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
        }
        
        return ttv;
    }
}
