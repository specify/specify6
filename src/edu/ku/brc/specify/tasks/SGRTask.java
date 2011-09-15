/* Copyright (C) 2009, University of Kansas Center for Research
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

import java.awt.Color;
import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import com.google.common.base.Function;

import edu.ku.brc.af.auth.BasicPermisionPanel;
import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxButton;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.PreferencesDlg;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.sgr.datamodel.BatchMatchResultSet;
import edu.ku.brc.sgr.datamodel.DataModel;
import edu.ku.brc.sgr.datamodel.MatchConfiguration;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.plugins.sgr.BatchResultPropertyEditor;
import edu.ku.brc.specify.plugins.sgr.BatchResultsMgr;
import edu.ku.brc.specify.plugins.sgr.HistogramChart;
import edu.ku.brc.specify.plugins.sgr.RecordSetBatchMatch;
import edu.ku.brc.specify.plugins.sgr.SGRBatchScenario;
import edu.ku.brc.specify.plugins.sgr.SGRMatcherUI;
import edu.ku.brc.specify.plugins.sgr.SGRPluginImpl;
import edu.ku.brc.specify.plugins.sgr.WorkBenchBatchMatch;
import edu.ku.brc.specify.tasks.subpane.wb.WorkbenchPaneSS;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.SimpleGlassPane;
import edu.ku.brc.ui.dnd.Trash;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Mar 17, 2011
 *
 */
public class SGRTask extends BaseTask
{
    private static class NavBoxMapping extends WeakHashMap<SubPaneIFace, Set<NavBoxItemIFace>> {}
    
    public static final int           GLASSPANE_FONT_SIZE  = 20;

    public static final DataFlavor    TOOLS_FLAVOR         = new DataFlavor(SGRTask.class,
                                                                   "SGRTask");
    public static final DataFlavor    BATCH_RESULTS_FLAVOR = new DataFlavor(
                                                                   BatchMatchResultSet.class,
                                                                   "Batch Match Results");
    private static final DataFlavor   MATCHER_FLAVOR       = new DataFlavor(
                                                                   MatchConfiguration.class,
                                                                   "SGR Matcher");

    public static final String        SGR                  = "SGR";

    public static final String        EXPORT_RS            = "ExportRecordSet";
    public static final String        EXPORT_LIST          = "ExportList";
    public static final String        EXPORT_JTABLE        = "ExportJTable";
    private static final String       SGR_PROCESS          = "SGR.Process";

    protected Vector<NavBoxIFace>     extendedNavBoxes     = new Vector<NavBoxIFace>();
    protected ToolBarDropDownBtn      toolBarBtn           = null;

    protected Vector<NavBoxItemIFace> toolsNavBoxList      = new Vector<NavBoxItemIFace>();
    private NavBox                    batchMatchResultsBox;
    private NavBox                    matchersNavBox;
    protected NavBox                  workbenchNavBox;

    private final NavBoxMapping       selectedNavBoxItems  = new NavBoxMapping();

    public SGRTask()
    {
        super(SGR, getResourceString("SGR"));
        
        CommandDispatcher.register(SGR, this);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    @Override
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false
            
            DataModel.startDbSession(new Function<Object, Connection>() {
                @Override public Connection apply(Object arg0) {
                    return DBConnection.getInstance().createConnection();
            }});
            
            // create an instance of each registered exporter
            toolsNavBoxList.clear();

            // if visible, create a nav box button for each exporter
            if (isVisible)
            {
                UIRegistry.loadAndPushResourceBundle("specify_plugins");
                
                extendedNavBoxes.clear();
                
//                navBoxes.add(makeActionsNavBox());
                
                matchersNavBox = makeMatchersNavBox();
                navBoxes.add(matchersNavBox);
                
                batchMatchResultsBox = makeBatchResultsNavBox();
                navBoxes.add(batchMatchResultsBox);
                
                WorkbenchTask wbTask = (WorkbenchTask)ContextMgr.getTaskByClass(WorkbenchTask.class);
                workbenchNavBox = wbTask.datasetNavBoxMgr.createWorkbenchNavBox(SGR);
                
                UIRegistry.popResourceBundle();
            }
        }
        isShowDefault = true;
    }
    
    private NavBox makeMatchersNavBox()
    {
        NavBox matchersBox = new NavBox(getResourceString("SGR_MATCHERS_TITLE"));
        List<MatchConfiguration> matchers = DataModel.getMatcherConfigurations();
        
        for (MatchConfiguration mc : matchers)
        {
             NavBoxItemIFace nbi = addMatcherToNavBox(mc, matchersBox, true);
        }
        
        final RolloverCommand roc = 
            (RolloverCommand)makeDnDNavBtn(matchersBox, 
                    getResourceString("SGR_CREATE_MATCHER"), "PlusSign", 
                    new CommandAction(SGR, "new_matcher"), 
                    null, false, false);
        
        roc.addDropDataFlavor(RecordSetTask.RECORDSET_FLAVOR);
        roc.addDropDataFlavor(WorkbenchTask.DATASET_FLAVOR);
        return matchersBox;
    }
            
    private NavBoxItemIFace addMatcherToNavBox(MatchConfiguration mc, NavBox nb, boolean addSorted)
    {
        final NavBoxItemIFace nbi = makeDnDNavBtn(nb, mc.name(), "SGR", null,
                new CommandAction("SGR", SGR_PROCESS, mc, null, mc), null, true, 0, addSorted);
        
        nbi.setData(mc);

        ((RolloverCommand) nbi).addDragDataFlavor(MATCHER_FLAVOR);
        
        JPopupMenu popupMenu = new JPopupMenu();

        UIHelper.createLocalizedMenuItem(popupMenu, "SGR_EDIT_MATCH_CONFIGURATION", "", null, true, 
                new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        final SGRMatcherUI mui = 
                            SGRMatcherUI.dialogForEditing((Frame)UIRegistry.getTopWindow(), nbi);
                        
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                mui.setVisible(true);
                            }
                        });
                    }
        });
        
        if (permissions == null || permissions.canDelete())
        {
            CommandAction delCmdAction = new CommandAction("SGR", DELETE_CMD_ACT, nbi);
            ((NavBoxButton) nbi).setDeleteCommandAction(delCmdAction);            
            ((RolloverCommand) nbi).addDragDataFlavor(Trash.TRASH_FLAVOR);
        }
        
        ((RolloverCommand) nbi).setPopupMenu(popupMenu);
        
        ((RolloverCommand) nbi).addDropDataFlavor(RecordSetTask.RECORDSET_FLAVOR);
        ((RolloverCommand) nbi).addDropDataFlavor(WorkbenchTask.DATASET_FLAVOR);
        
        return nbi;
    }
    
    private NavBox makeBatchResultsNavBox()
    {
        NavBox batchResultsBox = new NavBox(getResourceString("SGR_BATCH_RESULTS"));
        
        List<BatchMatchResultSet> resultSets = DataModel.getBatchMatchResultSets();
        
        for (BatchMatchResultSet rs: resultSets)
        {
            NavBoxItemIFace nbi = new BatchResultsMgr(rs, permissions);
            batchResultsBox.insertSorted(nbi);
            
            String toolTip = "" + rs.nItems() + " " + getResourceString("SGR_BATCH_ITEMS");
            
            nbi.setToolTip(toolTip);
        }

        return batchResultsBox;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#requestContext()
     */
    @Override
    public void requestContext()
    {
        ContextMgr.requestContext(this);

        if (starterPane == null)
        {
            super.requestContext();
            
        } else
        {
            SubPaneMgr.getInstance().showPane(starterPane);
        }
        
        UsageTracker.incrUsageCount("SGR.StartSGR");
    }

    @Override
    public void subPaneShown(SubPaneIFace subPane) 
    {
        super.subPaneShown(subPane);
        if (subPane == starterPane)
        {
            updateNavBoxes(new HashSet<NavBoxItemIFace>(0));
        }
        else
        {
            Set<NavBoxItemIFace> nbis = selectedNavBoxItems.get(subPane);
            if (nbis != null)
                updateNavBoxes(nbis);
        }
    }
    
    @Override
    public void subPaneRemoved(SubPaneIFace subPane) 
    {
        if (subPane == starterPane) 
            starterPane = null;
    }
    
    @Override
    protected SubPaneIFace addSubPaneToMgr(SubPaneIFace subPane) 
    {
        selectedNavBoxItems.put(subPane, new HashSet<NavBoxItemIFace>());
        return super.addSubPaneToMgr(subPane);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        if (starterPane == null)
        {
            starterPane = StartUpTask.createFullImageSplashPanel(title, this);
        }

        return starterPane;
    }


    //-------------------------------------------------------
    // Taskable
    //-------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getNavBoxes()
     */
    @Override
    public java.util.List<NavBoxIFace> getNavBoxes()
    {
        initialize();

        extendedNavBoxes.clear();
        extendedNavBoxes.addAll(navBoxes);

//        RecordSetTask rsTask = (RecordSetTask)ContextMgr.getTaskByClass(RecordSetTask.class);
//        List<NavBoxIFace> nbs = rsTask.getNavBoxes();
//        if (nbs != null)
//        {
//            extendedNavBoxes.addAll(nbs);
//        }
//        
        extendedNavBoxes.add(workbenchNavBox);
        return extendedNavBoxes;
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        String label    = getResourceString("SGR");
        String hint     = getResourceString("export_hint");
        toolBarBtn      = createToolbarButton(label, iconName, hint);
        
        toolbarItems = new Vector<ToolBarItemDesc>();
        if (AppPreferences.getLocalPrefs().getBoolean("ENABLE_SGR", false))
        {
            toolbarItems.add(new ToolBarItemDesc(toolBarBtn));
        }
        return toolbarItems;
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getMenuItems()
     */
    @Override
    public List<MenuItemDesc> getMenuItems()
    {
        menuItems = new Vector<MenuItemDesc>();
        
        if (AppPreferences.getLocalPrefs().getBoolean("ENABLE_SGR", false))
        {
            String menuDesc = "Specify.SYSTEM_MENU";
            
            if (permissions == null || permissions.canModify())
            {
                String    menuTitle = "SGRTask.PLUGIN_MENU"; //$NON-NLS-1$
                String    mneu      = "SGRTask.PLUGIN_MNEU"; //$NON-NLS-1$
                String    desc      = "SGRTask.PLUGIN_DESC"; //$NON-NLS-1$
                JMenuItem mi        = UIHelper.createLocalizedMenuItem(menuTitle, mneu, desc, true, null);
                mi.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent ae)
                    {
                        SGRTask.this.requestContext();
                    }
                });
                MenuItemDesc miDesc = new MenuItemDesc(mi, menuDesc);
                miDesc.setPosition(MenuItemDesc.Position.Bottom);
                miDesc.setSepPosition(MenuItemDesc.Position.Before);
                menuItems.add(miDesc);
            }
        }
        
        return menuItems;

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getTaskClass()
     */
    @Override
    public Class<? extends BaseTask> getTaskClass()
    {
        return this.getClass();
    }

    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------

    /**
     * @param cmdAction the command to be processed
     */
    protected void processCommand(CommandAction cmdAction)
    {
        SGRBatchScenario scenario = null;
        if (cmdAction.getData() instanceof RecordSetIFace)
        {
            RecordSetIFace recordSet = (RecordSetIFace)cmdAction.getData();
            MatchConfiguration matchConfig = (MatchConfiguration)cmdAction.getSrcObj();
            scenario = RecordSetBatchMatch.newScenario(recordSet, matchConfig);
        }
        else if (cmdAction.getData() instanceof CommandAction)
        {
            CommandAction internal = (CommandAction)cmdAction.getData();
            if (internal.getAction().equals(WorkbenchTask.SELECTED_WORKBENCH))
            {
                RecordSetIFace recordSet = (RecordSetIFace)internal.getProperty("workbench");
                MatchConfiguration matchConfig = (MatchConfiguration)cmdAction.getSrcObj();
                scenario = WorkBenchBatchMatch.newScenario(recordSet, matchConfig);
            }
        }
        else if (cmdAction.getData() instanceof MatchConfiguration)
        {
            MatchConfiguration selectedMatcher = (MatchConfiguration)cmdAction.getData();
            setMatcher(selectedMatcher);
        }
            
        if (scenario != null)
        {
            BatchMatchResultSet resultSet = scenario.getResultSet(); 
            scenario.start();
            NavBoxItemIFace nbi = new BatchResultsMgr(resultSet, scenario, permissions);
            batchMatchResultsBox.insertSorted(nbi);
            
            final BatchResultPropertyEditor editor = new BatchResultPropertyEditor(nbi);
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    editor.setVisible(true);
                }
            });
            
            UsageTracker.incrUsageCount("SGR.BatchMatch");
        }
    }
    
    public void setMatcher(MatchConfiguration matcher)
    {
        SubPaneIFace subPane = SubPaneMgr.getInstance().getCurrentSubPane();

        Set<NavBoxItemIFace> nbis = selectedNavBoxItems.get(subPane);
        if (nbis == null) return;
        for (NavBoxItemIFace nbi : nbis)
            if (nbi.getData() == matcher)
                return;
        
        nbis.clear();
        addNbiForMatcher(nbis, matcher);
        
        WorkbenchPaneSS workbenchPane = null;
        try
        {
            workbenchPane = (WorkbenchPaneSS)subPane;
        } catch (ClassCastException e) {}
        
        if (workbenchPane != null)
        {
            SGRPluginImpl sgr = 
                (SGRPluginImpl) workbenchPane.getPlugin(SGRPluginImpl.class);
            sgr.setMatcherConfiguration(matcher);
            sgr.getColorizer().stopColoring();
            workbenchPane.showHideSgrCol(false);
            
            addNbiForWorkbench(nbis, workbenchPane.getWorkbench());
        }
        
        updateNavBoxes(nbis);
    }
    
    protected void deleteBatchMatchResultSet(final NavBoxItemIFace nbi)
    {
        BatchMatchResultSet rs = (BatchMatchResultSet) nbi.getData();
        rs.delete();
        deleteDnDBtn(batchMatchResultsBox, nbi);
    }
    
    protected void deleteMatchConfiguration(final NavBoxItemIFace nbi)
    {
        MatchConfiguration mc = (MatchConfiguration) nbi.getData();
        try
        {
            mc.delete();
        }
        catch (RuntimeException e)
        {
            if (!Pattern.compile("foreign key constraint fails").matcher(e.getMessage()).find())
            {
                throw e;
            }
            UIRegistry.loadAndPushResourceBundle("specify_plugins");
            UIRegistry.showLocalizedError("SGR_CANT_DELETE_MATCHER");
            UIRegistry.popResourceBundle();
            return;
        }
        deleteDnDBtn(matchersNavBox, nbi);
    }
    

    public void addHistogram(BatchMatchResultSet resultSet, SGRBatchScenario scenario, float f)
    {
        SubPaneIFace histoPane = new HistogramChart(resultSet.name(), this, resultSet, scenario, f);
        addSubPaneToMgr(histoPane);
        Set<NavBoxItemIFace> nbis = selectedNavBoxItems.get(histoPane);
        addNbisForResultSet(nbis, resultSet);
        updateNavBoxes(nbis);
        UsageTracker.incrUsageCount("SGR.Histogram");
    }
    
    /**
     * 
     */
    protected void prefsChanged(final CommandAction cmdAction)
    {
        AppPreferences appPrefs = (AppPreferences)cmdAction.getData();
        
        if (appPrefs == AppPreferences.getRemote())
        {
            // Note: The event send with the name of pref from the form
            // not the name that was saved. So we don't need to append the discipline name on the end
            Object value = cmdAction.getProperties().get("Exporttask.OnTaskbar");
            if (value != null && value instanceof Boolean)
            {
                /*
                 * This doesn't work because it isn't added to the Toolbar correctly
                 * */
                JToolBar toolBar = (JToolBar)UIRegistry.get(UIRegistry.TOOLBAR);
                
                Boolean isChecked = (Boolean)value;
                if (isChecked)
                {
                    TaskMgr.addToolbarBtn(toolBarBtn, toolBar.getComponentCount()-1);
                } else
                {
                    TaskMgr.removeToolbarBtn(toolBarBtn);
                }
                toolBar.validate();
                toolBar.repaint();
                 
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getPermEditorPanel()
     */
    @Override
    public PermissionEditorIFace getPermEditorPanel()
    {
        return new BasicPermisionPanel(SGR, "ENABLE", null, null, null);
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(final CommandAction cmdAction)
    {
        if (cmdAction.isType(SGR))
        {
            if (cmdAction.isAction(SGR_PROCESS))
            {
                processCommand(cmdAction);
            } 
            else if (cmdAction.isAction(DELETE_CMD_ACT))
            {
                NavBoxItemIFace nbi = (NavBoxItemIFace) cmdAction.getData();
                Object toDelete = nbi.getData();
                if (toDelete instanceof BatchMatchResultSet)
                {
                    deleteBatchMatchResultSet(nbi);
                }
                else if (toDelete instanceof MatchConfiguration)
                {
                    deleteMatchConfiguration(nbi);
                }
            }
            else if (cmdAction.isAction("new_matcher"))
            {
                final SGRMatcherUI mui = 
                    SGRMatcherUI.dialogForNewConfig((Frame)UIRegistry.getTopWindow(), 
                        new SGRMatcherUIFinished(cmdAction));
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mui.setVisible(true);
                    }
                });
                UsageTracker.incrUsageCount("SGR.NewMatcher");
            }
            else if (cmdAction.isAction(WorkbenchTask.SELECTED_WORKBENCH))
            {
                Object cmdData = cmdAction.getData();
                Workbench workbench = null;
                if (cmdData instanceof RecordSetIFace)
                {
                    workbench = WorkbenchTask.loadWorkbench((RecordSetIFace)cmdData);
                } else 
                {
                    // This is for when the user clicks directly on the workbench
                    workbench = WorkbenchTask.loadWorkbench(
                            (RecordSetIFace)cmdAction.getProperty("workbench"));
                }                
                if (workbench != null)
                {
                    createEditorForWorkbench(workbench, null, false, true);
                }
            }
            else if (cmdAction.isAction("selected_resultset"))
            {
                BatchMatchResultSet resultSet = (BatchMatchResultSet) cmdAction.getData();
                Workbench workbench = WorkbenchTask.loadWorkbench(
                        (int)(long)resultSet.getRecordSetId(), null);
                if (workbench != null)
                {
                    createEditorForWorkbench(workbench, null, false, true, resultSet);
                }
            }
        } else if (cmdAction.isType(PreferencesDlg.PREFERENCES))
        {
            prefsChanged(cmdAction);
        } 
    }

    /**
     * Creates the Pane for editing a Workbench.
     * @param workbench the workbench to be edited
     * @param session a session to use to load the workbench (can be null)
     * @param showImageView shows image window when first showing the window
     */
    protected void createEditorForWorkbench(final Workbench workbench, 
                                            final DataProviderSessionIFace session,
                                            final boolean showImageView,
                                            final boolean doInbackground)
    {
        createEditorForWorkbench(workbench, session, showImageView, doInbackground, null);   
    }
    
    protected void createEditorForWorkbench(final Workbench workbench, 
                                            final DataProviderSessionIFace session,
                                            final boolean showImageView,
                                            final boolean doInbackground,
                                            final BatchMatchResultSet resultSet)
    {
        if (workbench == null) return;

        final SimpleGlassPane glassPane = doInbackground ? 
                UIRegistry.writeSimpleGlassPaneMsg(String.format(
                        getResourceString("WB_LOADING_DATASET"), 
                        new Object[] {workbench.getName()}), GLASSPANE_FONT_SIZE) : null;

        WorkbenchTask wbTask = (WorkbenchTask)ContextMgr.getTaskByClass(WorkbenchTask.class);   
        
        WorkbenchEditorCreator wbec = new WorkbenchEditorCreator(workbench,
                session, showImageView, this, !wbTask.isPermitted())
        {
            @Override
            public void progressUpdated(java.util.List<Integer> chunks) 
            {
                if (glassPane != null)
                    glassPane.setProgress(chunks.get(chunks.size() - 1));
            }
            
            @Override
            public void completed(WorkbenchPaneSS workbenchPane)
            {
                addSubPaneToMgr(workbenchPane);
                Set<NavBoxItemIFace> nbis = selectedNavBoxItems.get(workbenchPane);
                addNbiForWorkbench(nbis, workbench);
                
                if (resultSet != null)
                {
                    SGRPluginImpl sgr = 
                        (SGRPluginImpl) workbenchPane.getPlugin(SGRPluginImpl.class);
                    sgr.setMatcherConfiguration(resultSet.getMatchConfiguration());
                    sgr.getColorizer().setBatchResults(resultSet);
                    workbenchPane.showHideSgrCol(true);
                    workbenchPane.sgrSort();
                    
                    addNbisForResultSet(nbis, resultSet);
                }
                
                if (glassPane != null)
                {
                    UIRegistry.clearSimpleGlassPaneMsg();
                }
                
                updateNavBoxes(nbis);
             }
        };
        
        if (doInbackground)
            wbec.runInBackground();
        else
            wbec.runInForeground();
    }
    
    private void addNbiForWorkbench(Set<NavBoxItemIFace> nbis, Workbench workbench)
    {
        NavBoxItemIFace wbnbi = 
            (NavBoxItemIFace) WorkbenchTask.getNavBtnById(workbenchNavBox, 
                    workbench.getWorkbenchId(), "workbench");
        
        if (wbnbi != null)
        {
            nbis.add(wbnbi);
        }
    }
    
    private void addNbisForResultSet(Set<NavBoxItemIFace> nbis, BatchMatchResultSet resultSet)
    {
        for (NavBoxItemIFace nbi : batchMatchResultsBox.getItems())
            if (nbi.getData() == resultSet)
                 nbis.add(nbi);
 
        addNbiForMatcher(nbis, resultSet.getMatchConfiguration());
    }
    
    private void addNbiForMatcher(Set<NavBoxItemIFace> nbis, MatchConfiguration matcher)
    {
        for (NavBoxItemIFace nbi : matchersNavBox.getItems())
        {
            MatchConfiguration matcherInBox;
            try 
            {
                matcherInBox = (MatchConfiguration) nbi.getData();
            } catch (ClassCastException e) { continue; }
            
            if (matcherInBox.id() == matcher.id())
                nbis.add(nbi);
        }
    }
    
    private void updateNavBoxes(Set<NavBoxItemIFace> nbis)
    {
        updateNavBox(matchersNavBox, nbis);
        updateNavBox(workbenchNavBox, nbis);
        updateNavBox(batchMatchResultsBox, nbis);
    }
    
    private void updateNavBox(NavBox nb, Set<NavBoxItemIFace> nbis)
    {
        for (NavBoxItemIFace nbi : nb.getItems())
            if (nbis.contains(nbi))
            {
                nbi.getUIComponent().setBackground(Color.LIGHT_GRAY);
            }
            else
            {
                nbi.getUIComponent().setBackground(Color.WHITE);
            }
    }

    private class SGRMatcherUIFinished implements Function<MatchConfiguration, Void>
    {
        private final CommandAction cmdAction;

        public SGRMatcherUIFinished(CommandAction cmdAction)
        {
            this.cmdAction = cmdAction;
        }

        @Override
        public Void apply(MatchConfiguration matchConfiguration)
        {
            UIRegistry.loadAndPushResourceBundle("specify_plugins");
            addMatcherToNavBox(matchConfiguration, matchersNavBox, false);
            UIRegistry.popResourceBundle();
            
            NavBox.refresh(matchersNavBox);
            // kludgie
            cmdAction.setSrcObj(matchConfiguration);
            processCommand(cmdAction);
            return null;
        }
    }
}