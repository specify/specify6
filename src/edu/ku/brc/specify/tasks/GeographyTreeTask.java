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
import java.sql.ResultSet;

import javax.persistence.Transient;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.dbsupport.SQLExecutionListener;
import edu.ku.brc.dbsupport.SQLExecutionProcessor;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.GeographyTreeDefItem;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.busrules.GeographyBusRules;
import edu.ku.brc.specify.ui.treetables.TreeNodePopupMenu;
import edu.ku.brc.specify.ui.treetables.TreeTableViewer;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;

/**
 * Task that handles the UI for viewing geography data.
 * 
 * @code_status Beta
 * @author jstewart
 */
public class GeographyTreeTask extends BaseTreeTask<Geography,GeographyTreeDef,GeographyTreeDefItem> implements SQLExecutionListener
{
	public static final String GEOGRAPHY = "GeographyTree";
	
	/**
	 * Constructor.
	 */
	public GeographyTreeTask()
	{
        super(GEOGRAPHY, getResourceString(GEOGRAPHY));
        
        treeClass         = Geography.class;
        treeDefClass      = GeographyTreeDef.class;
        icon              = IconManager.getIcon(GEOGRAPHY,IconManager.IconSize.Std16);
        
        menuItemText      = getResourceString("GeographyMenu");
        menuItemMnemonic  = getResourceString("GeographyMnemonic");
        starterPaneText   = getResourceString("GeographyStarterPaneText");
        commandTypeString = GEOGRAPHY;
        
        businessRules     = new GeographyBusRules();
        
        initialize();
	}
    
    @Transient
    @Override
    protected GeographyTreeDef getCurrentTreeDef()
    {
        return Discipline.getCurrentDiscipline().getGeographyTreeDef();
    }
    
    /**
     * @param ttv
     * @param list
     */
    protected void showCollectionObjects(final TreeTableViewer<Geography, GeographyTreeDef, GeographyTreeDefItem> ttv,
                                    final JList list)
    {
        Geography geography = ttv.getSelectedNode(list);

        // this call initializes all of the linked objects
        // it only initializes the immediate links, not objects that are multiple hops away
        ttv.initializeNodeAssociations(geography);
        
        String sql = "SELECT collectionobject.CollectionObjectID " + 
                     "FROM geography INNER JOIN locality ON geography.GeographyID = locality.GeographyID " + 
                     "INNER JOIN collectingevent ON locality.LocalityID = collectingevent.LocalityID " + 
                     "INNER JOIN collectionobject ON collectingevent.CollectingEventID = collectionobject.CollectingEventID " + 
                     "WHERE CollectionMemberID = COLMEMID AND geography.GeographyID = "+ geography.getId();
        sql = QueryAdjusterForDomain.getInstance().adjustSQL(sql);
        
        SQLExecutionProcessor sqlProc = new SQLExecutionProcessor(this, sql);
        sqlProc.start();

    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.BaseTreeTask#createTreeViewer(boolean)
     */
    @Override
    protected TreeTableViewer<Geography, GeographyTreeDef, GeographyTreeDefItem> createTreeViewer(final String titleArg, boolean isEditMode)
    {
        final TreeTableViewer<Geography, GeographyTreeDef, GeographyTreeDefItem> ttv = super.createTreeViewer(titleArg, isEditMode);

        if (ttv != null)
        {
            final TreeNodePopupMenu popup = ttv.getPopupMenu();
            // install custom popup menu items

            JMenuItem coMenuItem = new JMenuItem(getResourceString("TTV_ASSOC_COS"));
            coMenuItem.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    showCollectionObjects(ttv, popup.getList());
                }
            });
            popup.add(coMenuItem, true);
        }
        
        return ttv;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.SQLExecutionListener#exectionDone(edu.ku.brc.dbsupport.SQLExecutionProcessor, java.sql.ResultSet)
     */
    public void exectionDone(SQLExecutionProcessor process, ResultSet resultSet)
    {
        final RecordSet recordSet = new RecordSet("TTV.showCollectionObjects", CollectionObject.getClassTableId());
        try
        {
            while (resultSet.next())
            {
                recordSet.addItem(resultSet.getInt(1));
            }
            
        } catch (Exception ex)
        {
            log.error(ex);
            ex.printStackTrace();
        }
        
        UIRegistry.getStatusBar().setText(getResourceString("TTV_OPENING_CO_FORM"));
        // This is needed so the StatusBar gets updated
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                CommandDispatcher.dispatch(new CommandAction(DataEntryTask.DATA_ENTRY, DataEntryTask.EDIT_DATA, recordSet));
            }
        });
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.SQLExecutionListener#executionError(edu.ku.brc.dbsupport.SQLExecutionProcessor, java.lang.Exception)
     */
    public void executionError(SQLExecutionProcessor process, Exception ex)
    {
    }
    
}
