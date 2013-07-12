/* Copyright (C) 2013, University of Kansas Center for Research
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.BasicPermisionPanel;
import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.PreferencesDlg;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.dbsupport.BuildFromGeonames;
import edu.ku.brc.specify.dbsupport.cleanuptools.AgentCleanupIndexer;
import edu.ku.brc.specify.dbsupport.cleanuptools.AgentCleanupProcessor;
import edu.ku.brc.specify.dbsupport.cleanuptools.AgentNameCleanupParserDlg;
import edu.ku.brc.specify.dbsupport.cleanuptools.AgentNameCleanupParserDlg.DataItem;
import edu.ku.brc.specify.dbsupport.cleanuptools.GeographyAssignISOs;
import edu.ku.brc.specify.dbsupport.cleanuptools.GeographyMerging;
import edu.ku.brc.specify.dbsupport.cleanuptools.LocalityCleanupIndexer;
import edu.ku.brc.specify.dbsupport.cleanuptools.LocalityCleanupProcessor;
import edu.ku.brc.specify.dbsupport.cleanuptools.LocalityGeoBoundsChecker2;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ProgressDialog;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.SimpleGlassPane;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 5, 2012
 *
 */
public class CleanupToolsTask extends BaseTask
{
    private static final Logger  log = Logger.getLogger(CleanupToolsTask.class);
    
    private static final String  CLEANUP           = "CLEANUP";
    private static final String  CLEANUP_TITLE     = "CLEANUP";
    //private static final String  CLEANUP_SECURITY  = "CLEANUPEDIT";
    private static final String  CLEANUP_ICON      = "CleanUp";
    private static final String  ON_TASKBAR        = "CleanupToolsTask.OnTaskbar";
    
    private static final String  GEO               = Geography.class.getSimpleName();
    private static final String  AGENT             = Agent.class.getSimpleName();
    private static final String  LOCALITY          = Locality.class.getSimpleName();
    

    protected ProgressDialog           prgDlg;

    protected Vector<NavBoxIFace>      extendedNavBoxes = new Vector<NavBoxIFace>();
    protected ToolBarDropDownBtn       toolBarBtn       = null;
    protected AgentCleanupProcessor    agentProcessor   = null;
    protected LocalityCleanupProcessor localityProcessor = null;
    
    protected NavBox                   geoNavBox        = null;
    protected NavBox                   agentNavBox      = null;
    protected NavBox                   localityNavBox   = null;


    /**
     * @param name
     * @param title
     */
    public CleanupToolsTask()
    {
        super(CLEANUP, getResourceString(CLEANUP_TITLE));
        this.iconName = CLEANUP_ICON;
        CommandDispatcher.register(PreferencesDlg.PREFERENCES, this);
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
            
            extendedNavBoxes.clear();
            
            geoNavBox.add(NavBox.createBtnWithTT(getResourceString("CLNUP_GEO_CLEANUP_TOOLS"), GEO, getResourceString("CLNUP_GEO_CLEANUP_TOOLS_TT"), IconManager.STD_ICON_SIZE, new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    doGeographyISOCodes();
                }
            })); 
            
            geoNavBox.add(NavBox.createBtnWithTT(getResourceString("CLNUP_GEO_CLEANUP_MERGE"), GEO, getResourceString("CLNUP_GEO_CLEANUP_MERGE_TT"), IconManager.STD_ICON_SIZE, new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    doGeographyMerge();
                }
            })); 
            
            // 
            agentNavBox.add(NavBox.createBtnWithTT(getResourceString("CLNUP_AGENT_MERGE_EX"), AGENT, getResourceString("CLNUP_AGENT_MERGE_EX_TT"), IconManager.STD_ICON_SIZE, new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    doAgentExactMatches();
                }
            })); 
            
            agentNavBox.add(NavBox.createBtnWithTT(getResourceString("CLNUP_AGENT_MERGE_LN"), AGENT, getResourceString("CLNUP_AGENT_MERGE_LN_TT"), IconManager.STD_ICON_SIZE, new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    doLastNameParsing();
                }
            })); 
            
            agentNavBox.add(NavBox.createBtnWithTT(getResourceString("CLNUP_AGENT_MERGE_FZ"), AGENT, getResourceString("CLNUP_AGENT_MERGE_FZ_TT"), IconManager.STD_ICON_SIZE, new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    doAgentFuzzyMatches();
                }
            })); 
            
            boolean inclLocalities = AppPreferences.getLocalPrefs().getBoolean("INCL_LOCALITIES", false);
            
            if (inclLocalities)
            {
                localityNavBox.add(NavBox.createBtnWithTT(getResourceString("CLNUP_LOCALITY_MERGE_EX"), LOCALITY, getResourceString("CLNUP_LOCALITY_MERGE_EX_TT"), IconManager.STD_ICON_SIZE, new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        doLocalityMatching(false);
                    }
                })); 
                
                localityNavBox.add(NavBox.createBtnWithTT(getResourceString("CLNUP_LOCALITY_MERGE_LL"), LOCALITY, getResourceString("CLNUP_LOCALITY_MERGE_LL_TT"), IconManager.STD_ICON_SIZE, new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        doLocalityMatching(true);
                    }
                })); 
                
                localityNavBox.add(NavBox.createBtnWithTT(getResourceString("CLNUP_LOCALITY_MERGE_FZ"), LOCALITY, getResourceString("CLNUP_LOCALITY_MERGE_FZ_TT"), IconManager.STD_ICON_SIZE, new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        doLocalityNameFuzzyMatch();   
                    }
                })); 
                
                localityNavBox.add(NavBox.createBtnWithTT(getResourceString("CLNUP_LOCALITY_VERIFY_FZ"), LOCALITY, getResourceString("CLNUP_LOCALITY_VERIFY_FZ_TT"), IconManager.STD_ICON_SIZE, new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        doLocalityLatLonVerify();   
                    }
                })); 
            }            
            navBoxes.add(geoNavBox);
            navBoxes.add(agentNavBox);
            
            if (inclLocalities) navBoxes.add(localityNavBox);
        }
        isShowDefault = true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#preInitialize()
     */
    @Override
    public void preInitialize()
    {
        CommandDispatcher.register(CLEANUP, this);

        // Create and add the Actions NavBox first so it is at the top at the top
        geoNavBox      = new NavBox(DBTableIdMgr.getInstance().getTitleForId(Geography.getClassTableId()));
        agentNavBox    = new NavBox(DBTableIdMgr.getInstance().getTitleForId(Agent.getClassTableId()));
        localityNavBox = new NavBox(DBTableIdMgr.getInstance().getTitleForId(Locality.getClassTableId()));
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    public SubPaneIFace getStarterPane()
    {
        if (starterPane != null)
        {
            return starterPane;
        }
        return starterPane = StartUpTask.createFullImageSplashPanel(title, this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#subPaneRemoved(edu.ku.brc.af.core.SubPaneIFace)
     */
    @Override
    public void subPaneRemoved(final SubPaneIFace subPane)
    {
        super.subPaneRemoved(subPane);
        
        if (subPane == starterPane)
        {
            starterPane = null;
        }
    }
    
    /**
     * 
     */
    private void doAgentExactMatches()
    {
        agentProcessor = new AgentCleanupProcessor(new AgentCleanupIndexer(), true);
        agentProcessor.loadExactMatchAgents();
    }
    
    
    /**
     * 
     */
    protected void doLocalityMatching(final boolean isLatLon)
    {
        // NOTE: isLatLon indicates whether it is matching using the Latitude1 and Longtude1 values or
        // whether it is using the LocalityName to match
        localityProcessor = new LocalityCleanupProcessor(isLatLon);
        localityProcessor.performExactMatching();
        
    }
    
    /**
     * 
     */
    protected void doLocalityNameFuzzyMatch()
    {
        localityProcessor = new LocalityCleanupProcessor(new LocalityCleanupIndexer());
        localityProcessor.performLuceneMatching();
    }
    
    /**
     * 
     */
    protected void doLocalityLatLonVerify()
    {
        LocalityGeoBoundsChecker2 locBGeoBndsChecker = new LocalityGeoBoundsChecker2(DBConnection.getInstance().getConnection());
        locBGeoBndsChecker.processLocalities();
    }
    
    /**
     * 
     */
    private void doAgentFuzzyMatches()
    {
        int rv       = JOptionPane.YES_OPTION;
        int matchCnt = AgentCleanupProcessor.getExactMatchCount();
        if (matchCnt > 0)
        {
            //String msg = UIRegistry.getLocalizedMessage("CLNUP_HAS_EXACT_AG_MATCHES", matchCnt);
            String msg = String.format("There were %d exact matches.\nIt is recommended to resolve these first before merging using 'fuzzy matches'.\n\nDo you wish to continue with 'fuzzy matches' ?", matchCnt);
            rv = UIRegistry.askYesNoLocalized("Continue", "Close", msg, "CLNUP_HAS_EXACT_AG_MATCHES_TITLE");
        }
        
        if (rv == JOptionPane.YES_OPTION)
        {
            AgentCleanupIndexer ac = new AgentCleanupIndexer();
            //ac.testLastNames();
            agentProcessor = new AgentCleanupProcessor(ac, false);
            agentProcessor.doBuildLuceneIndex();
        }
    }

    /**
     * @param pStmt
     * @param inx
     * @param str
     * @throws SQLException
     */
    private void setColumn(final PreparedStatement pStmt, final int inx, final String str) throws SQLException
    {
        pStmt.setString(inx, StringUtils.isNotEmpty(str) ? str : null);
    }
    
    /**
     * 
     */
    private void updateNames(final Vector<AgentNameCleanupParserDlg.DataItem> dataItemsList)
    {
        final String PRC = "PROCESS";
        final SimpleGlassPane glassPane = UIRegistry.writeSimpleGlassPaneMsg("Processing agents...", 24);
        
        //prgDlg = new ProgressDialog(getResourceString("CLNUP_AG_PRG_TITLE"), true, false);
        //prgDlg.getProcessProgress().setIndeterminate(true);
        //prgDlg.setDesc(getResourceString("CLNUP_AG_INIT_MSG"));
        //UIHelper.centerAndShow(prgDlg);

        final SwingWorker<Object, Object> worker = new SwingWorker<Object, Object>()
        {
            double tot  = 0; // 1 -> 100
            double step = 1.0;
            int    cnt  = 0;
            
            @Override
            protected Object doInBackground() throws Exception
            {
                step = 100.0 / dataItemsList.size();

                Connection conn = null;
                PreparedStatement pStmt = null;
                try
                {
                    String sql = "UPDATE agent SET LastName=?, FirstName=?,MiddleInitial=? WHERE AgentID = ?";
                    conn  = DBConnection.getInstance().createConnection();
                    pStmt = conn.prepareStatement(sql);
                    
                    for (DataItem di : dataItemsList)
                    {
                        if (di.isIncluded())
                        {
                            setColumn(pStmt, 1, di.getLastName());
                            setColumn(pStmt, 2, di.getFirstName());
                            setColumn(pStmt, 3, di.getMidName());
                            pStmt.setInt(4, di.getAgentId());
                            
                            if (pStmt.executeUpdate() != 1)
                            {
                                log.error(String.format("Error updating AgentID %d", di.getAgentId()));
                            }
                        }
                        
                        tot += step;
                        if (((int)tot) > cnt)
                        {
                            cnt = (int)tot;
                            firePropertyChange(PRC, -1, cnt);
                        }
                    }
                    dataItemsList.clear();
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                } finally
                {
                    try
                    {
                        if (pStmt != null) pStmt.close();
                        if (conn != null) conn.close();
                        
                    } catch (SQLException ex){}
                }
                return null;
            }

            @Override
            protected void done()
            {
                UIRegistry.clearSimpleGlassPaneMsg();
                UIRegistry.showLocalizedMsg("Done.");
            }
        };
        worker.addPropertyChangeListener(new PropertyChangeListener() {
            public  void propertyChange(PropertyChangeEvent evt) {
                if (PRC.equals(evt.getPropertyName())) 
                {
                    glassPane.setProgress((Integer)evt.getNewValue());
                }
            }
        });
        worker.execute();
    }

    /**
     * 
     */
    private void doLastNameParsing()
    {
        AgentNameCleanupParserDlg dlg = new AgentNameCleanupParserDlg(DBConnection.getInstance().getConnection());
        UIHelper.centerAndShow(dlg);
        
        if (!dlg.isCancelled())
        {
            updateNames(dlg.getList());
        }
    }
    
    /**
     * 
     */
    private void doGeographyISOCodes()
    {
        final DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();;
        try
        {
            Timestamp now = new Timestamp(System .currentTimeMillis());
            Discipline discipline = AppContextMgr.getInstance().getClassObject(Discipline.class);
            Agent      agent      = AppContextMgr.getInstance().getClassObject(Agent.class);

            ProgressFrame frame = new ProgressFrame("Building Geography Authority...");
            frame.getCloseBtn().setVisible(false);
            frame.pack();
            frame.setSize(450, frame.getBounds().height+10);
            
            discipline = session.get(Discipline.class, discipline.getId());
            BuildFromGeonames bldGeoNames = new BuildFromGeonames(discipline.getGeographyTreeDef(), now, agent, "root", "root", frame);
            
            if (bldGeoNames.loadGeoNamesDB()) // done synchronously
            {
                int earthId = BasicSQLUtils.getCountAsInt("SELECT GeographyID FROM geography WHERE RankID = 0 AND GeographyTreeDefID = "+discipline.getGeographyTreeDef().getId());
                
                GeographyAssignISOs geoAssignISOCodes = new GeographyAssignISOs(discipline.getGeographyTreeDef(), agent, frame);
                geoAssignISOCodes.buildAsync(earthId, new ChangeListener()
                {
                    @Override
                    public void stateChanged(ChangeEvent e)
                    {
                        if (session != null) session.close();
                    }
                });
            } else
            {
                UIRegistry.showError("There was an error loading the Geography reference file.");
            }
            
        } catch (Exception ex)
        {
            //log.error(ex);
            ex.printStackTrace();
        }
    }
    
    /**
     * 
     */
    private void doGeographyMerge()
    {
        GeographyMerging geoMerging = new GeographyMerging();
        if (!geoMerging.start())
        {
            UIRegistry.showLocalizedMsg("There were no 'exact matches' for geography.");
        }
    }
    

    //-------------------------------------------------------
    // Taskable
    //-------------------------------------------------------

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#getNavBoxes()
     */
    @Override
    public java.util.List<NavBoxIFace> getNavBoxes()
    {
        initialize();

        return navBoxes;
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        String label    = getResourceString(CLEANUP);
        String hint     = getResourceString(CLEANUP);
        toolBarBtn      = createToolbarButton(label, iconName, hint);
        
        toolbarItems = new Vector<ToolBarItemDesc>();
        if (AppPreferences.getRemote().getBoolean(ON_TASKBAR, false))
        {
            toolbarItems.add(new ToolBarItemDesc(toolBarBtn));
        }
        return toolbarItems;
    }
    
    //-------------------------------------------------------
    // SecurityOption Interface
    //-------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getAdditionalSecurityOptions()
     */
    /*@Override
    public List<SecurityOptionIFace> getAdditionalSecurityOptions()
    {
        List<SecurityOptionIFace> list = new ArrayList<SecurityOptionIFace>();
        
        SecurityOption secOpt = new SecurityOption(CLEANUP_SECURITY, 
                                                    getResourceString("CLEANUP_TITLE"), 
                                                    securityPrefix,
                                                    new BasicPermisionPanel(CLEANUP_TITLE, 
                                                                            "CLEANUP_VIEW", 
                                                                            "CLEANUP_EDIT"));
        addPerms(secOpt, new boolean[][] 
                   {{true, true, true, true},
                   {false, false, false, false},
                   {false, false, false, false},
                   {false, false, false, false}});
        
        list.add(secOpt);

        return list;
    }*/
    
    //-------------------------------------------------------
    // BaseTask
    //-------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getMenuItems()
     */
    @Override
    public List<MenuItemDesc> getMenuItems()
    {
        String menuDesc = "Specify.SYSTEM_MENU";
        
        menuItems = new Vector<MenuItemDesc>();
        
        if (permissions == null || permissions.canModify())
        {
            String    menuTitle = "CLNUP.CLEANUP_MENU"; //$NON-NLS-1$
            String    mneu      = "CLNUP.CLEANUP_MNEU"; //$NON-NLS-1$
            String    desc      = "CLNUP.CLEANUP_DESC"; //$NON-NLS-1$
            JMenuItem mi        = UIHelper.createLocalizedMenuItem(menuTitle, mneu, desc, true, null);
            mi.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    CleanupToolsTask.this.requestContext();
                }
            });
            MenuItemDesc rsMI = new MenuItemDesc(mi, menuDesc);
            rsMI.setPosition(MenuItemDesc.Position.After);
            rsMI.setSepPosition(MenuItemDesc.Position.Before);
            menuItems.add(rsMI);
        }
        
        return menuItems;
    }

    // -------------------------------------------------------
    // CommandListener Interface
    // -------------------------------------------------------

    /**
     * @param cmdAction
     */
    protected void prefsChanged(final CommandAction cmdAction)
    {
        reAddToolBarItem(cmdAction, toolBarBtn, ON_TASKBAR);
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.af.tasks.BaseTask#getPermEditorPanel()
     */
    @Override
    public PermissionEditorIFace getPermEditorPanel()
    {
        return new BasicPermisionPanel(CLEANUP, "ENABLE", null, null, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(final CommandAction cmdAction)
    {
        if (cmdAction.isType(PreferencesDlg.PREFERENCES))
        {
            prefsChanged(cmdAction);
        }
    }
}
