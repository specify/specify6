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

import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

import edu.ku.brc.af.auth.BasicPermisionPanel;
import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.core.AppContextMgr;
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
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.sgr.datamodel.BatchMatchResultSet;
import edu.ku.brc.sgr.datamodel.DataModel;
import edu.ku.brc.sgr.datamodel.MatchConfiguration;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Institution;
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
import edu.ku.brc.ui.ChooseFromListDlg;
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

    public SGRTask()
    {
        super(SGR, getResourceString("SGR"));
        
        CommandDispatcher.register(SGR, this);
    }
    
    private void loadDefaultMatchers()
    {
        Discipline disc = AppContextMgr.getInstance().getClassObject(Discipline.class);
        File file = 
            XMLHelper.getConfigDir(disc.getType() + File.separator + "default_sgr_matchers.xml");
        if (file.exists())
        {
            java.util.Collection<MatchConfiguration> mcs = DataModel.importMatchConfigurations(file);
            
            // ugly
            Collection coll = AppContextMgr.getInstance().getClassObject(Collection.class);
            Institution inst = AppContextMgr.getInstance().getClassObject(Institution.class);
            
            String fq = "-institution_code:\"" + inst.getCode()
                + "\" -collection_code:\"" + coll.getCode() +"\"";
            for (MatchConfiguration mc : mcs)
            {
                mc.updateFilterQuery(fq);
            }
            AppPreferences.getRemote().putBoolean("SGR_DEFAULT_MATCHERS_LOADED", true);
        }
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

            //if (!AppPreferences.getRemote().getBoolean("SGR_DEFAULT_MATCHERS_LOADED", false))
            {
                loadDefaultMatchers();
            }

            toolsNavBoxList.clear();

            if (isVisible)
            {
                UIRegistry.loadAndPushResourceBundle("specify_plugins");
                
                extendedNavBoxes.clear();
                
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
        
        final RolloverCommand createMatcherBtn = 
            (RolloverCommand)makeDnDNavBtn(matchersBox, 
                    getResourceString("SGR_CREATE_MATCHER"), "PlusSign", 
                    new CommandAction(SGR, "new_matcher"), 
                    null, false, false);
        
        if (AppPreferences.getLocalPrefs().getBoolean("ENABLE_SGR_MATCHER_IMPORT_EXPORT", false))
        {
            final RolloverCommand exportMatcherBtn = 
                (RolloverCommand)makeDnDNavBtn(matchersBox, 
                        getResourceString("SGR_EXPORT_MATCHER"), "PlusSign", 
                        new CommandAction(SGR, "export_matcher"), 
                        null, false, false);
            
            exportMatcherBtn.addDropDataFlavor(MATCHER_FLAVOR);
            
            final RolloverCommand importMatchersBtn = 
                (RolloverCommand)makeDnDNavBtn(matchersBox, 
                        getResourceString("SGR_IMPORT_MATCHERS"), "PlusSign", 
                        new CommandAction(SGR, "import_matchers"), 
                        null, false, false);
        }        
        return matchersBox;
    }
            
    private NavBoxItemIFace addMatcherToNavBox(MatchConfiguration mc, NavBox nb, boolean addSorted)
    {
        final NavBoxItemIFace nbi = makeDnDNavBtn(nb, mc.name(), "SGRMatchers", null,
                new CommandAction(SGR, SGR_PROCESS, mc, null, mc), null, true, 0, addSorted);
        
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
        }
        
        final RolloverCommand createBatchBtn = 
            (RolloverCommand)makeDnDNavBtn(batchResultsBox, 
                    getResourceString("SGR_DO_BATCH"), "PlusSign", 
                    new CommandAction(SGR, "do_batch"), 
                    null, false, false);
        
        createBatchBtn.addDropDataFlavor(RecordSetTask.RECORDSET_FLAVOR);
        createBatchBtn.addDropDataFlavor(WorkbenchTask.DATASET_FLAVOR);
        

        return batchResultsBox;
    }
    
    private void setEnableBatchBtnForWorkbench(Workbench workbench, boolean enabled)
    {
        for (NavBoxItemIFace nbi : batchMatchResultsBox.getItems())
        {
            BatchMatchResultSet resultSet;
            try { resultSet = (BatchMatchResultSet) nbi.getData(); }
            catch (ClassCastException e) { continue; }
            if (resultSet.getRecordSetId() == (long) workbench.getId())
            {
                nbi.setEnabled(enabled);
            }
        }
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
    public void subPaneRemoved(SubPaneIFace subPane) 
    {
        if (subPane == starterPane) 
            starterPane = null;
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
        toolbarItems.add(new ToolBarItemDesc(toolBarBtn));
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
        
        return menuItems;

    }

    @Override
    public void doCommand(final CommandAction cmdAction)
    {
        if (cmdAction.isType(SGR))
        {
            if (cmdAction.isAction(SGR_PROCESS))
            {
                processCommand(cmdAction);
            } 
            else if (cmdAction.isAction("new_matcher"))
            {
                createNewMatcher();
            }
            else if (cmdAction.isAction("export_matcher"))
            {
                exportMatchers(cmdAction);
            }
            else if (cmdAction.isAction("import_matchers"))
            {
                importMatchers();
            }
            else if (cmdAction.isAction("do_batch"))
            {
                doBatchMatch(cmdAction);
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
                    createEditorForWorkbench(workbench, null, false, true, null, null);
                }
            }
            else if (cmdAction.isAction("selected_resultset"))
            {
                BatchMatchResultSet resultSet = (BatchMatchResultSet) cmdAction.getData();
                Workbench workbench = WorkbenchTask.loadWorkbench(
                        (int)(long)resultSet.getRecordSetId(), null);
                if (workbench != null)
                {
                    createEditorForWorkbench(workbench, null, false, true, null, resultSet);
                }
            }
        } 
        else if (cmdAction.isType(PreferencesDlg.PREFERENCES))
        {
            prefsChanged(cmdAction);
        } 
    }

    private void createNewMatcher()
    {
        final SGRMatcherUI mui = 
            SGRMatcherUI.dialogForNewConfig((Frame)UIRegistry.getTopWindow(), 
                new Function<MatchConfiguration, Void>()
                {
                    @Override
                    public Void apply(MatchConfiguration mc)
                    {
                        UIRegistry.loadAndPushResourceBundle("specify_plugins");
                        addMatcherToNavBox(mc, matchersNavBox, false);
                        UIRegistry.popResourceBundle();            
                        NavBox.refresh(matchersNavBox);
                        return null;
                    }
                }
            );
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

    private void importMatchers()
    {
        JFileChooser chooser = new JFileChooser(
                WorkbenchTask.getDefaultDirPath(WorkbenchTask.EXPORT_FILE_PATH));
        chooser.setDialogTitle(getResourceString("SGR_CHOOSE_MATCHER_IMPORT_FILE"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(true);
        if (chooser.showOpenDialog(UIRegistry.get(UIRegistry.FRAME)) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        java.util.Collection<MatchConfiguration> mcs = DataModel.importMatchConfigurations(file);
        UIRegistry.loadAndPushResourceBundle("specify_plugins");
        for (MatchConfiguration matchConfiguration : mcs)
        {
            addMatcherToNavBox(matchConfiguration, matchersNavBox, false);
        }
        UIRegistry.popResourceBundle();
        NavBox.refresh(matchersNavBox);
    }

    private void exportMatchers(final CommandAction cmdAction)
    {
        List<MatchConfiguration> mcs;
        if (cmdAction.getData() == cmdAction)
        {
            mcs = DataModel.getMatcherConfigurations();
            ChooseFromListDlg<MatchConfiguration> dlg =
                new ChooseFromListDlg<MatchConfiguration>(
                        (Frame)UIRegistry.get(UIRegistry.FRAME),
                        getResourceString("SGR_CHOOSE_MATCHERS_TO_EXPORT"),
                        mcs);
            dlg.setMultiSelect(true);
            UIHelper.centerAndShow(dlg);
            if (dlg.isCancelled()) return;

            mcs = dlg.getSelectedObjects();
        }
        else
        {
            mcs = ImmutableList.of((MatchConfiguration) cmdAction.getData());
        }

        JFileChooser chooser = new JFileChooser(
                WorkbenchTask.getDefaultDirPath(WorkbenchTask.EXPORT_FILE_PATH));
        chooser.setDialogTitle(getResourceString("CHOOSE_MATCHER_EXPORT_FILE"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        if (chooser.showSaveDialog(UIRegistry.get(UIRegistry.FRAME)) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        try
        {
            FileWriter fw = new FileWriter(file);
            fw.write(DataModel.exportMatchConfigurations(mcs).toString());
            fw.close();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void processCommand(CommandAction cmdAction)
    {
        MatchConfiguration selectedMatcher;
        Workbench workbench;
        
        if (cmdAction.getData() instanceof MatchConfiguration)
        {
            selectedMatcher = (MatchConfiguration)cmdAction.getData();
            workbench = WorkbenchTask.selectWorkbench(cmdAction, null);
        }
        else
        {
            selectedMatcher = (MatchConfiguration)cmdAction.getSrcObj();
            workbench = WorkbenchTask.selectWorkbench((CommandAction) cmdAction.getData(), 
                    "sgr_matcher_create");
        }
         
        createEditorForWorkbench(workbench, null, false, true, selectedMatcher, null);
    }    
    
    protected void createEditorForWorkbench(final Workbench workbench, 
                                            final DataProviderSessionIFace session,
                                            final boolean showImageView,
                                            final boolean doInbackground,
                                            final MatchConfiguration matchConfiguration,
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
                
                if (resultSet != null)
                {
                    SGRPluginImpl sgr = 
                        (SGRPluginImpl) workbenchPane.getPlugin(SGRPluginImpl.class);
                    sgr.setBatchMatchResults(resultSet);
                }
                else if (matchConfiguration != null)
                {
                    SGRPluginImpl sgr = 
                        (SGRPluginImpl) workbenchPane.getPlugin(SGRPluginImpl.class);
                    sgr.setMatcherConfiguration(matchConfiguration);
                    workbenchPane.showPanel(WorkbenchPaneSS.PanelType.Form);
                }
                
                if (glassPane != null)
                {
                    UIRegistry.clearSimpleGlassPaneMsg();
                }
                
                if (workbenchPane != null && workbenchPane.isDoIncremental())
                {
                    workbenchPane.validateAll(null);
                }
             }
        };
        
        if (doInbackground)
            wbec.runInBackground();
        else
            wbec.runInForeground();
    }
    
    public void opening(WorkbenchPaneSS pane)
    {
        if (workbenchNavBox == null) return;
        Workbench workbench = pane.getWorkbench();
        RolloverCommand roc = WorkbenchTask.getNavBtnById(workbenchNavBox, 
                workbench.getWorkbenchId(), "workbench");
        if (roc != null)
        {
            roc.setEnabled(false);
        }
        
        setEnableBatchBtnForWorkbench(workbench, false);
    }
    
    public void closing(final SubPaneIFace pane)
    {
        Workbench workbench;
        try { workbench = ((WorkbenchPaneSS)pane).getWorkbench(); }
        catch (ClassCastException e) { return; }
        
        setEnableBatchBtnForWorkbench(workbench, true);
        
        if (workbench != null)
        {
            RolloverCommand roc = WorkbenchTask.getNavBtnById(workbenchNavBox, workbench.getWorkbenchId(), "workbench");
            if (roc != null)
            {
                roc.setEnabled(true);
            }
        }
    }
    
    private ChooseFromListDlg<MatchConfiguration> makeSelectMatcherDlg()
    {
        List<MatchConfiguration> mcs = DataModel.getMatcherConfigurations();
        UIRegistry.loadAndPushResourceBundle("specify_plugins");
        String title = UIRegistry.getResourceString("SGR_SELECT_MATCHER");
        UIRegistry.popResourceBundle();
        
        ChooseFromListDlg<MatchConfiguration> selectMatcherDlg = 
            new ChooseFromListDlg<MatchConfiguration>((Frame)UIRegistry.get(UIRegistry.FRAME), 
                    title,
                    ChooseFromListDlg.HELP_BTN | ChooseFromListDlg.OK_BTN | ChooseFromListDlg.CANCEL_BTN,
                    mcs);
        
        selectMatcherDlg.setHelpContext("sgr_matchresults_matcher");
        
        UIHelper.centerAndShow(selectMatcherDlg);
        return selectMatcherDlg;
    }
    
    private void doBatchMatch(CommandAction cmdAction)
    {
        SGRBatchScenario scenario = null;
        if (cmdAction.getData() instanceof RecordSetIFace)
        {
            ChooseFromListDlg<MatchConfiguration> selectMatcherDlg = makeSelectMatcherDlg();
            if (selectMatcherDlg.isCancelled())
                return;
            
            MatchConfiguration matchConfig = selectMatcherDlg.getSelectedObject();
            RecordSetIFace recordSet = (RecordSetIFace)cmdAction.getData();
            scenario = RecordSetBatchMatch.newScenario(recordSet, matchConfig);
        }
        else if (cmdAction.getData() instanceof CommandAction)
        {
            CommandAction internal = (CommandAction)cmdAction.getData();
            if (internal.getAction().equals(WorkbenchTask.SELECTED_WORKBENCH))
            {
                ChooseFromListDlg<MatchConfiguration> selectMatcherDlg = makeSelectMatcherDlg();
                if (selectMatcherDlg.isCancelled())
                    return;
                
                MatchConfiguration matchConfig = selectMatcherDlg.getSelectedObject();
                RecordSetIFace recordSet = (RecordSetIFace)internal.getProperty("workbench");
                scenario = WorkBenchBatchMatch.newScenario(recordSet, matchConfig);
            }
            else 
            {
                Workbench workbench = WorkbenchTask.selectWorkbench(cmdAction, "sgr_matchresults_matcher");
                if (workbench == null)
                    return;
                
                ChooseFromListDlg<MatchConfiguration> selectMatcherDlg = makeSelectMatcherDlg();
                if (selectMatcherDlg.isCancelled())
                    return;
                
                MatchConfiguration matchConfig = selectMatcherDlg.getSelectedObject();
                scenario = WorkBenchBatchMatch.newScenario(workbench.getId(), matchConfig);
            }
        }    
        
        if (scenario != null)
        {
            BatchMatchResultSet resultSet = scenario.getResultSet(); 
            scenario.start();
            NavBoxItemIFace nbi = new BatchResultsMgr(resultSet, scenario, permissions);
            batchMatchResultsBox.insert(nbi, true, false, 0);
            
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
    
    protected void deleteBatchMatchResultSet(final NavBoxItemIFace nbi)
    {
        ((BatchResultsMgr) nbi).delete();
        deleteDnDBtn(batchMatchResultsBox, nbi);
    }
    
    protected void deleteMatchConfiguration(final NavBoxItemIFace nbi)
    {
        MatchConfiguration mc = (MatchConfiguration) nbi.getData();
        for (SubPaneIFace pane : SubPaneMgr.getInstance().getSubPanes())
        {
            WorkbenchPaneSS workbenchPane;
            try { workbenchPane = (WorkbenchPaneSS) pane; }
            catch (ClassCastException e) { continue; }
            
            SGRPluginImpl sgr = 
                (SGRPluginImpl) workbenchPane.getPlugin(SGRPluginImpl.class);
            
            if (sgr.getMatcher() == mc)
            {
                UIRegistry.loadAndPushResourceBundle("specify_plugins");
                UIRegistry.showLocalizedError("SGR_CANT_DELETE_OPEN_MATCHER");
                UIRegistry.popResourceBundle();
                return;
            }
        }
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
        UsageTracker.incrUsageCount("SGR.Histogram");
    }
    
    @Override
    public PermissionEditorIFace getPermEditorPanel()
    {
        return new BasicPermisionPanel(SGR, "ENABLE", null, null, null);
    }
    
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
    
    @Override
    public Class<? extends BaseTask> getTaskClass()
    {
        return this.getClass();
    }
}
