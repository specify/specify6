/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.ERTIJoinColInfo;
import edu.ku.brc.af.core.expresssearch.ExpressResultsTableInfo;
import edu.ku.brc.af.core.expresssearch.ExpressSearchConfigCache;
import edu.ku.brc.af.core.expresssearch.ExpressSearchConfigDlg;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL;
import edu.ku.brc.af.core.expresssearch.QueryForIdResultsSQL;
import edu.ku.brc.af.core.expresssearch.SearchConfig;
import edu.ku.brc.af.core.expresssearch.SearchConfigService;
import edu.ku.brc.af.core.expresssearch.SearchTableConfig;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.af.ui.ESTermParser;
import edu.ku.brc.af.ui.SearchBox;
import edu.ku.brc.af.ui.SearchTermField;
import edu.ku.brc.af.ui.db.ERTICaptionInfo;
import edu.ku.brc.af.ui.db.JAutoCompTextField;
import edu.ku.brc.af.ui.db.PickListDBAdapterFactory;
import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.dbsupport.CustomQueryIFace;
import edu.ku.brc.dbsupport.CustomQueryListener;
import edu.ku.brc.dbsupport.JPAQuery;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.SQLExecutionListener;
import edu.ku.brc.dbsupport.SQLExecutionProcessor;
import edu.ku.brc.specify.SpecifyUserTypes.UserType;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.dbsupport.SpecifyQueryAdjusterForDomain;
import edu.ku.brc.specify.tasks.subpane.ESResultsSubPane;
import edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace;
import edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace;
import edu.ku.brc.specify.tasks.subpane.ExpressTableResultsFromQuery;
import edu.ku.brc.specify.tasks.subpane.SIQueryForIdResults;
import edu.ku.brc.specify.tasks.subpane.qb.QBQueryForIdResultsHQL;
import edu.ku.brc.specify.tasks.subpane.qb.QBResultsSubPane;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.specify.web.ExplorerESPanel;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconEntry;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.SearchBoxComponent;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
/**
 * This task will enable the user to index the database and preform express searches. This is where the Express Search starts.
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public class ExpressSearchTask extends BaseTask implements CommandListener, SQLExecutionListener, CustomQueryListener
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(ExpressSearchTask.class);
    public static final  String GLOBAL_SEARCH_AVAIL = "GLOBAL_SEARCH_AVAIL";
    public static final  String GLOBAL_SEARCH       = "GLOBAL_SEARCH";
    

    public static final int    RESULTS_THRESHOLD_LARGE  = 50000;
    public static final int    RESULTS_THRESHOLD_STD = 20000;
    public static final int    RESULTS_THRESHOLD = 3500000000L <= Runtime.getRuntime().maxMemory()
            ? RESULTS_THRESHOLD_LARGE : RESULTS_THRESHOLD_STD;
    public static final String EXPRESSSEARCH      = "Express_Search";
    public static final String CHECK_INDEXER_PATH = "CheckIndexerPath";
    
    protected Color selectionFG = UIManager.getColor("TextField.selectionForeground");
    protected Color selectionBG = UIManager.getColor("TextField.selectionBackground");
    
    // Static Data Members
    protected static ExpressSearchTask      instance    = null;
    protected static final String           LAST_SEARCH = "lastsearch"; 
    
    // Data Members
    protected JCheckBoxMenuItem             globalSearchCheckBoxMI;
    protected SearchBoxComponent            searchBoxComp;
    protected SearchBox                     searchBox;
    protected JTextField                    searchText;
    protected JButton                       searchBtn;
    protected Color                         textBGColor      = null;
    protected Color                         badSearchColor   = new Color(255,235,235);
    
    protected Vector<ESResultsSubPane>      paneCache        = new Vector<ESResultsSubPane>();
    
    protected Vector<SQLExecutionProcessor> sqlProcessorList = new Vector<SQLExecutionProcessor>();
    protected boolean                       sqlHasResults    = false;
    protected Vector<CustomQueryIFace>      queryList        = new Vector<CustomQueryIFace>();
    protected int                           queryCount       = 0;
    protected int                           queryCountDone   = 0;
    
    
    protected ESResultsSubPane              queryResultsPane      = null;
    protected ESResultsSubPane              batchEditResultsPane  = null;
    protected ESResultsSubPane              rsQbResultsPane       = null;
    protected SIQueryForIdResults           searchWarningsResults = null;
    protected JStatusBar                    statusBar             = null;
    protected boolean                       doingDebug            = false;


    /**
     * Default Constructor.
     */
    public ExpressSearchTask()
    {
        super(EXPRESSSEARCH, getResourceString(EXPRESSSEARCH));
        iconName = "Search";
        closeOnLastPane = true;
        CommandDispatcher.register(EXPRESSSEARCH, this);
        instance = this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#initialize()
     */
    @Override
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false
            
            statusBar = UIRegistry.getStatusBar();
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        if (searchBox != null)
        {
            searchBox.setEnabled(enabled);
        }
    }

    /**
     * Returns true if the talk has been started and false if it hasn't.
     * @return true if the talk has been started and false if it hasn't.
     */
    public static boolean isStarted()
    {
        return instance != null;
    }

    
    /**
     * Check to see of the index has been run and then enables the express search controls.
     *
     */
    /**
     * 
     */
    public void appContextHasChanged()
    {
        if (searchText != null)
        {
            if (searchText instanceof JAutoCompTextField)
            {
                ((JAutoCompTextField)searchText).setPickListAdapter(PickListDBAdapterFactory.getInstance().create("ExpressSearch", true));
            }
            
            AppPreferences localPrefs = AppPreferences.getLocalPrefs();
            searchText.setText(localPrefs.get(getLastSearchKey(), ""));
        }
        
        if (searchBtn != null)
        {
            searchBtn.setEnabled(true);
            searchText.setEnabled(true);
        }
        
        if (textBGColor != null)
        {
            searchText.setBackground(textBGColor);
            searchText.setForeground(UIManager.getColor("TextField.foreground"));
        }
        
        SearchConfigService.getInstance().reset();
        
        if (searchBox != null && searchBox.getMenuCreator() != null)
        {
            searchBox.getMenuCreator().reset();
        }
    }
    
    /**
     * @return a discipline based pref name.
     */
    protected String getLastSearchKey()
    {
        Discipline discp          = AppContextMgr.getInstance().getClassObject(Discipline.class);
        String     disciplineName = discp != null ? ("_" + discp.getType()) : "";
        return LAST_SEARCH + disciplineName;
    }

    /**
     * Performs the express search and returns the results.
     */
    protected void doQuery()
    {
        sqlHasResults = false;
        
        if (true)
        {
            searchText.setBackground(textBGColor);
            searchText.setForeground(UIManager.getColor("TextField.foreground"));

            String searchTerm = searchText.getText();
            if (isNotEmpty(searchTerm))
            {
                if (searchTerm.length() == 2 && (searchTerm.endsWith("*") || searchTerm.startsWith("*")))
                {
                    UIRegistry.showLocalizedError("ExpressSearchTask.NO_SINGLE_CHAR");
                    
                } else if (QueryAdjusterForDomain.getInstance().isUserInputNotInjectable(searchTerm.toLowerCase()))
                {
                    boolean isOK = true;
                    if (searchTerm.startsWith("*") && searchTerm.endsWith("*") && searchTerm.length() > 3)
                    {
                        int inx = searchTerm.length() - 2;
                        if (searchTerm.indexOf(' ', 2) > 1 && 
                            searchTerm.lastIndexOf(' ') < searchTerm.length()-2 &&
                            searchTerm.charAt(1) != '"' && searchTerm.charAt(1) != '\'' && searchTerm.charAt(1) != '`' &&
                            searchTerm.charAt(inx) != '"' && searchTerm.charAt(inx) != '\'' && searchTerm.charAt(inx) != '`')
                        {
                            JLabel label = UIHelper.createI18NLabel("ExpressSearchTask.MISSING_QUOTES");
                            PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"));
                            pb.add(label, (new CellConstraints()).xy(1, 1));
                            pb.setDefaultDialogBorder();
                            CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getMostRecentWindow(), getResourceString("HINT"), true, pb.getPanel());
                            dlg.setOkLabel(getResourceString("ExpressSearchTask.SRCH_ANYWAY"));
                            dlg.setCancelLabel(getResourceString("ExpressSearchTask.DNT_SRCH"));
                            dlg.setVisible(true);
                            isOK = !dlg.isCancelled();
                        }
                    }
                    
                    if (isOK)
                    {
                        ESResultsSubPane expressSearchPane = new ESResultsSubPane(searchTerm, this, true);
                        if (!doQuery(searchText, null, expressSearchPane))
                        {
                            setUserInputToNotFound("BAD_SEARCH_TERMS", true);
                            
                        } else
                        {
                            AppPreferences.getLocalPrefs().put(getLastSearchKey(), searchTerm);
                        }
                    }
                    
                } else
                {
                    setUserInputToNotFound("ES_SUSPICIOUS_SQL", true);
                }
            }
        } else
        {
            String searchTerm = searchText.getText();
            
            ExplorerESPanel explorerSearch = new ExplorerESPanel();
            
            doQuery(searchTerm, explorerSearch);
            
            explorerSearch.done();
        }
    }
    
    /**
     * @param searchTerm
     * @param esrp
     */
    public boolean doQuery(final String searchTerm, 
                           final ExpressSearchResultsPaneIFace esrp)
    {
        if (isNotEmpty(searchTerm))
        {
            if (QueryAdjusterForDomain.getInstance().isUserInputNotInjectable(searchTerm.toLowerCase()))
            {
                return doQuery(null, searchTerm, esrp);
            }
        } 
        return false;
    }
    
    /**
     * @param esrPane the main display pane
     * @param searchTableConfig the configuration information
     * @param searchTerm the search term for the search
     * @return the JPAQuery
     */
    protected synchronized JPAQuery startSearchJPA(final ExpressSearchResultsPaneIFace esrPane,
                                                   final SearchTableConfig searchTableConfig,
                                                   final String            searchTerm,
                                                   final List<SearchTermField> terms)
    {
        JPAQuery jpaQuery = null;
        
        String sqlStr = searchTableConfig.getSQL(terms, true, true);
        if (sqlStr != null)
        {
            jpaQuery = new JPAQuery(sqlStr, this);
            jpaQuery.setData(new Object[] {searchTableConfig, esrPane, searchTerm});
            queryList.add(jpaQuery);
            queryCount++;
            
            if (statusBar != null)
            {
                statusBar.setProgressRange(EXPRESSSEARCH, 0, queryCount, queryCountDone);
            }
            
            if (esrPane.doQueriesSynchronously())
            {
                jpaQuery.execute();
                
            } else
            {
                jpaQuery.start();
            }
        }
        return jpaQuery;
    }

    /**
     * Performs the express search and returns the results to the ExpressSearchResultsPaneIFace.
     * If the control is null then it will use the string.
     *
     * @param searchTextArg the Text Control that contains the search string (can be null)
     * @param searchTextStr the Text Control that contains the search string (can be null)
     * @param tables ExpressResultsTableInfo hash
     * @param esrPane the pane that the results will be set into
     * @return true if results were found, false if not results
     */
    public synchronized boolean doQuery(final JTextField searchTextArg,
                                        final String     searchTextStr,
                                        final ExpressSearchResultsPaneIFace esrPane)
    {
        
        doingDebug = AppPreferences.getLocalPrefs().getBoolean("esdebug", false);
        searchWarningsResults = null;
        
        String searchTerm = (searchTextArg != null ? searchTerm = searchTextArg.getText() : searchTextStr);
        
        if (!ESTermParser.getInstance().parse(searchTerm, false))
        {
            setUserInputToNotFound("BAD_SEARCH_TERMS", false);
            return false;
        }

        //boolean hasResults = true;
        if (StringUtils.isNotEmpty(searchTerm))
        {
            SearchTableConfig context = SearchConfigService.getInstance().getSearchContext();
            if (context == null)
            {
                SearchConfig config = SearchConfigService.getInstance().getSearchConfig();

                if (config.getTables().size() > 0)
                {
                    sqlHasResults    = false;
                    if (searchTextArg != null)
                    {
                        searchTextArg.setEnabled(false);
                    }                    
                    if (searchBtn != null)
                    {
                        searchBtn.setEnabled(false);
                    }
    
                    queryList.clear(); // Just in case
                    queryCount     = 0;
                    queryCountDone = 0;
                    
                    int cnt = 0;
                    for (SearchTableConfig table : config.getTables())
                    {
                        if (!table.getTableInfo().isHidden())
                        {
                            if (AppContextMgr.isSecurityOn())
                            {
                                if (table.getTableInfo().getPermissions().canView())
                                {
                                    if (startSearchJPA(esrPane, table, searchTerm, ESTermParser.getInstance().getFields()) != null)
                                    {
                                        cnt++;
                                    }
                                } else
                                {
                                    log.debug("Skipping ["+table.getTableInfo().getTitle()+"]");
                                }
                            } else
                            {
                                if (startSearchJPA(esrPane, table, searchTerm, ESTermParser.getInstance().getFields()) != null)
                                {
                                    cnt++;
                                }
                            }
                        }
                    }
                    
                    if (cnt == 0)
                    {
                        setUserInputToNotFound("NO_FIELDS_TO_SEARCH", true);
                        searchText.setEnabled(true);
                        searchBtn.setEnabled(true);
                        return false;
                    }
                    
                    // Check to see if any queries got started.
                    if (queryList.size() == 0)
                    {
                        completionUIHelper(null);
                    } else
                    {
                        return true;
                    }
                    
                } else
                {
                    searchTextArg.getToolkit().beep();
                }
                
            } else
            {
                if (startSearchJPA(esrPane, context, searchTerm, ESTermParser.getInstance().getFields()) != null)
                {
                    return true;
                }
                setUserInputToNotFound("NO_FIELDS_TO_SEARCH", true);
                return true; // if we return false then this error gets overwritten with a different error
            }
        } else
        {
            //System.err.println("SearchTableConfig service has not been loaded or is empty.");
            log.error("SearchTableConfig service has been loaded or is empty.");
        }
        return false;
    }
    
    /**
     * Traverses the individual result record ids and maps them into the result tables.
     * @param config
     * @param tableId
     * @param searchTerm
     * @param resultSet
     * @param id
     * @param joinIdToTableInfoHash
     * @param resultsForJoinsHash the related results table
     */
    protected void collectResults(final SearchConfig           config,
                                  final String                 tableId,
                                  final String                 searchTerm,
                                  final ResultSet              resultSet,
                                  final Integer                id,
                                  final Hashtable<String, List<ExpressResultsTableInfo>> joinIdToTableInfoHash,
                                  final Hashtable<String, QueryForIdResultsSQL>          resultsForJoinsHash)
    {
        try
        {
            Integer recId;
            if (resultSet != null)
            {
                recId = resultSet.getInt(1);
            } else
            {
                recId = id;
            }
            
            //System.out.println("id "+id+"  "+recId);
            
            //log.debug("Find any Joins for TableID ["+tblInfo.getTableId()+"]");
            
            // Start by getting the List of next round of 'view' queries 
            // Before getting here we have constructed list of the queries that will need to be run
            // when we get a hit from the current table against one of the joins in the view query
            // 
            List<ExpressResultsTableInfo> list = joinIdToTableInfoHash.get(tableId);
            if (list != null)
            {
                // Now loop through each of the view queries
                for (ExpressResultsTableInfo erti : list)
                {
                    //log.debug("Not Active: "+erti.getId()+"  "+erti.getTitle()+"  "+config.isActiveForRelatedQueryId(erti.getId()));
                    if (!config.isActiveForRelatedQueryId(erti.getId()))
                    {
                        continue;
                    }
                    
                    //log.debug("Id: "+erti.getId()+"  "+erti.getTitle()+"  "+erti.getTableInfo().getPermissions().canView());
                    //SecurityMgr.dumpPermissions(erti.getTableInfo().getTitle(), erti.getTableInfo().getPermissions().getOptions());
                    if (AppContextMgr.isSecurityOn())
                    {
                        if (!erti.getTableInfo().getPermissions().canView())
                        {
                            continue;
                        }
                        
                        boolean hasUnviewableCol = false;
                        for (ERTICaptionInfo capInfo : erti.getCaptionInfo())
                        {
                            DBFieldInfo fldInfo = capInfo.getFieldInfo();
                            if (fldInfo != null)
                            {
                                if (!fldInfo.getTableInfo().getPermissions().canView())
                                {
                                    hasUnviewableCol = true;
                                    break;
                                }
                                
                            } else if (capInfo.getAggClass() != null)
                            {
                                DBTableInfo aggTblInfo = DBTableIdMgr.getInstance().getByShortClassName(capInfo.getAggClass().getSimpleName());
                                if (aggTblInfo != null && !aggTblInfo.getPermissions().canAdd())
                                {
                                    hasUnviewableCol = true;
                                    break; 
                                }
                            } else
                            {
                                log.error("*********** NO FIELD: "+capInfo.getColName());
                            }
                        }
                        if (hasUnviewableCol)
                        {
                            continue;
                        }
                    }
                    
                    QueryForIdResultsSQL results = resultsForJoinsHash.get(erti.getId());
                    if (results == null)
                    {
                        Integer         joinColTableId = null;
                        ERTIJoinColInfo joinCols[]     = erti.getJoins();
                        if (joinCols != null)
                        {
                            for (ERTIJoinColInfo jci :  joinCols)
                            {
                                if (tableId.equals(jci.getJoinTableId()))
                                {
                                    joinColTableId = jci.getJoinTableIdAsInt();
                                    //log.debug("CHK: "+jci.getTableInfo().getTitle()+"  "+jci.getTableInfo().getPermissions().canView());
                                    if (AppContextMgr.isSecurityOn() && !jci.getTableInfo().getPermissions().canView())
                                    {
                                        continue;
                                    }
                                    break;
                                }
                            }
                        }
                        if (joinColTableId == null)
                        {
                            throw new RuntimeException("Shouldn't have got here!");
                        }
                        Integer displayOrder = SearchConfigService.getInstance().getSearchConfig().getOrderForRelatedQueryId(erti.getId());
                        //log.debug("ExpressSearchResults erti.getId()["+erti.getId()+"] joinColTableId["+joinColTableId+"] displayOrder["+displayOrder+"]");
                        results = new QueryForIdResultsSQL(erti.getId(), joinColTableId, erti, displayOrder, searchTerm);
                        resultsForJoinsHash.put(erti.getId(), results);
                    }
                    
                    if (doingDebug)
                    {
                        log.debug("*** Adding recId["+recId+"] to erti["+erti.getId()+"] ");
                    }
                    results.add(recId);
                }
            }
        } catch (SQLException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ExpressSearchTask.class, ex);
            ex.printStackTrace();
        }
    }
    
    
    /**
     * Traverses through the results and adds to the panel to be displayed.
     * @param resultsMap the primary result tables
     * @param resultsForJoinsMap the related results table
     */
    protected void displayResults(final ExpressSearchResultsPaneIFace           esrPane,
                                  final QueryForIdResultsIFace                  queryResults,
                                  final Hashtable<String, QueryForIdResultsSQL> resultsForJoinsMap)
    {
        // For Debug Only
        if (false && resultsForJoinsMap != null)
        {
            for (Enumeration<QueryForIdResultsSQL> e=resultsForJoinsMap.elements();e.hasMoreElements();)
            {
                QueryForIdResultsSQL qfirsql = e.nextElement();
                if (qfirsql.getRecIds().size() > 0)
                {
                    log.debug("\n\n------------------------------------");
                    log.debug("------------------------------------");
                    log.debug("Search Id "+qfirsql.getTableInfo().getId() + 
                                       " Table Id "+qfirsql.getTableInfo().getTableId() + 
                                       " Column Name "+qfirsql.getJoinColTableId());
                    log.debug("------------------------------------");
                    for (Integer l : qfirsql.getRecIds())
                    {
                        log.debug(l+" ");
                    }
                }
            }
        }
        
        if (queryResults.size() > 0)
        {
            esrPane.addSearchResults(queryResults);
        }
        
        if (resultsForJoinsMap != null)
        {
            for (QueryForIdResultsSQL rs : resultsForJoinsMap.values())
            {
                if (rs.getRecIds().size() > 0)
                {
                    esrPane.addSearchResults(rs);
                }
            }
            resultsForJoinsMap.clear();
        }
    }
    
    /**
     * @param recordSet
     */
    public void displayRecordSet(final RecordSetIFace recordSet, final boolean isEditable)
    {
        if (recordSet.getNumItems() > 0)
        {
            SearchConfig      config            = SearchConfigService.getInstance().getSearchConfig();
            SearchTableConfig searchTableConfig = config.getSearchTableConfigById(recordSet.getDbTableId());
            if (searchTableConfig != null)
            {
                String rsName = UIRegistry.getLocalizedMessage("ES_RS_TAB_NM", recordSet.getName());
                
                SubPaneIFace sp = SubPaneMgr.getInstance().getSubPaneByName(rsName);
                if (sp != null)
                {
                    SubPaneMgr.getInstance().showPane(sp);
                } else
                {
                    ESResultsSubPane esrPane = new ESResultsSubPane(rsName, this, true);
                    esrPane.setIcon(IconManager.getIcon("Record_Set", IconManager.IconSize.Std16));
                    Hashtable<String, QueryForIdResultsSQL> resultsForJoinsHash = new Hashtable<String, QueryForIdResultsSQL>();
                    QueryForIdResultsHQL results = new QueryForIdResultsHQL(searchTableConfig, new Color(255, 158, 6), recordSet);
                    results.setEditable(isEditable);
                    results.setExpanded(true);
                    displayResults(esrPane, results, resultsForJoinsHash);
                    addSubPaneToMgr(esrPane);
                }
            } else
            {
                String tblTitle = DBTableIdMgr.getInstance().getTitleForId(recordSet.getDbTableId());
                UIRegistry.showLocalizedError("RS_CONFIG_SEARCH", tblTitle);
            }
        } else
        {
            UIRegistry.showLocalizedError("RS_HAS_NO_ITEMS", recordSet.getName());
        }
    }

    /**
     * @param searchName
     */
    public void doBasicSearch(final String searchName)
    {
        Hashtable<String, ExpressResultsTableInfo> idToTableInfoMap = ExpressSearchConfigCache.getSearchIdToTableInfoHash();
        for (ExpressResultsTableInfo erti : idToTableInfoMap.values())
        {
            //log.debug("["+erti.getName()+"]["+searchName+"]");
            if (erti.getName().equals(searchName))
            {
                // This needs to be fixed in that it might not return any results
                // and we are always adding the pane.
                ESResultsSubPane     expressSearchPane = new ESResultsSubPane(erti.getTitle(), this, true);
                QueryForIdResultsSQL         esr               = new QueryForIdResultsSQL(erti.getTitle(), null, erti, 0, "");
                @SuppressWarnings("unused")
                ExpressTableResultsFromQuery esrfq             = new ExpressTableResultsFromQuery(expressSearchPane, esr, true);
                addSubPaneToMgr(expressSearchPane);
                return;
            }
        }
        log.error("Can't find a search definition for name ["+searchName+"]");
    }
    
    /**
     * Executes and displays an HQL Query.
     * 
     * @param hqlStr the HQL query string
     */
    protected void doHQLQuery(final QueryForIdResultsIFace  results, final Boolean reusePanel, final Boolean isBatchEdit,
                              final Boolean isRSView,
                              final String tabText) {
        if (reusePanel == null || !reusePanel) {
            ESResultsSubPane expressSearchPane = new ESResultsSubPane(getResourceString("ES_QUERY_RESULTS"), this, true);
            addSubPaneToMgr(expressSearchPane);
            expressSearchPane.addSearchResults(results);
            
        } else {
            Boolean isRs = isRSView == null ? false : true;
            ESResultsSubPane pane = isRs ? queryResultsPane : isBatchEdit ? batchEditResultsPane : queryResultsPane;
            if (pane == null) {
                String txt = isRs ? tabText : getResourceString("ES_QUERY_RESULTS");
                pane = new QBResultsSubPane(txt, this, true);
                pane.setIcon(IconManager.getIcon(isRs ? "RecordSet" : isBatchEdit ? "BatchEdit" : "Query", IconManager.IconSize.Std16));
                if (isBatchEdit) {
                    batchEditResultsPane = pane;
                } else if (isRs) {
                    rsQbResultsPane = pane;
                } else {
                    queryResultsPane = pane;
                }
            } else {
                pane.reset();
            }
            pane.addSearchResults(results);
        }
    }

    //-------------------------------------------------------
    // Taskable
    //-------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        return starterPane = new SimpleDescPane(name, this, "This is the Express Search Pane");
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getNavBoxes()
     */
    @Override
    public java.util.List<NavBoxIFace> getNavBoxes()
    {
        initialize();

        Vector<NavBoxIFace>     extendedNavBoxes = new Vector<NavBoxIFace>();

        extendedNavBoxes.clear();
        extendedNavBoxes.addAll(navBoxes);

        RecordSetTask rsTask = (RecordSetTask)ContextMgr.getTaskByClass(RecordSetTask.class);

        List<NavBoxIFace> nbs = rsTask.getNavBoxes();
        if (nbs != null)
        {
            extendedNavBoxes.addAll(nbs);
        }
        
        return extendedNavBoxes;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        toolbarItems = new Vector<ToolBarItemDesc>();

        ActionListener doQuery = new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                doQuery();
            }
        };
        
        searchBoxComp  = new SearchBoxComponent(new SearchBoxMenuCreator(), doQuery, false,
                                                PickListDBAdapterFactory.getInstance().create("ExpressSearch", true));
        searchBoxComp.createUI();
        searchBox      = searchBoxComp.getSearchBox();
        searchText     = searchBoxComp.getSearchText();
        searchBtn      = searchBoxComp.getSearchBtn();
        textBGColor    = searchBoxComp.getTextBGColor();
        badSearchColor = searchBoxComp.getBadSearchColor();
        
        searchBtn.setToolTipText(getResourceString("ExpressSearchTT"));
        HelpMgr.setHelpID(searchBtn, "Express_Search");
        HelpMgr.registerComponent(searchText, "Express_Search");
        
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        searchText.setText(localPrefs.get(getLastSearchKey(), ""));
        textBGColor = searchText.getBackground();

        searchText.addMouseListener(new MouseAdapter() 
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                showContextMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                showContextMenu(e);

            }
        });

        toolbarItems.add(new ToolBarItemDesc(searchBoxComp, ToolBarItemDesc.Position.AdjustRightLastComp));

        return toolbarItems;
    }
    
    /**
     * Shows the Reset menu.
     * @param e the mouse event
     */
    protected void showContextMenu(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            JPopupMenu popup = new JPopupMenu();
            JMenuItem menuItem = new JMenuItem(UIRegistry.getResourceString("ES_TEXT_RESET"));
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ex)
                {
                    searchText.setEnabled(true);
                    searchText.setBackground(textBGColor);
                    searchText.setText("");
                    
                    if (statusBar != null)
                    {
                        statusBar.setProgressDone(EXPRESSSEARCH);
                    }
                }
            });
            popup.add(menuItem);
            popup.show(e.getComponent(), e.getX(), e.getY());

        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getMenuItems()
     */
    @Override
    public List<MenuItemDesc> getMenuItems()
    {
        return new Vector<MenuItemDesc>();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getTaskClass()
     */
    @Override
    public Class<? extends BaseTask> getTaskClass()
    {
        return this.getClass();
    }
    
    //----------------------------------------------------------------
    //-- CommandListener Interface
    //----------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doProcessAppCommands(edu.ku.brc.ui.CommandAction)
     */
    @Override
    protected void doProcessAppCommands(CommandAction cmdAction)
    {
        super.doProcessAppCommands(cmdAction);
        
        if (cmdAction.isAction(APP_RESTART_ACT))
        {
            appContextHasChanged();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(CommandAction cmdAction)
    {
        super.doCommand(cmdAction);
        
        if (cmdAction.isType(EXPRESSSEARCH))
        {
            UsageTracker.incrUsageCount("ES."+cmdAction.getType());
            
            if (cmdAction.isAction("HQL"))
            {
                doHQLQuery((QueryForIdResultsIFace)cmdAction.getData(), (Boolean)cmdAction.getProperty("reuse_panel"),
                        (Boolean)cmdAction.getProperty("is_batch_edit"), (Boolean)cmdAction.getProperty("is_qb_rs_view"), (String)cmdAction.getProperty("tab_text"));
                
            } else if (cmdAction.isAction("ExpressSearch"))
            {
                String searchTerm = cmdAction.getData().toString();
                ESResultsSubPane expressSearchPane = new ESResultsSubPane(searchTerm, this, true);
                doQuery(null, searchTerm, expressSearchPane);
                
            } else if (cmdAction.isAction("Search"))
            {
                doBasicSearch(cmdAction.getData().toString());
                
            } else if (cmdAction.isAction("SearchComplete"))
            {
                doSearchComplete(cmdAction);
                
            } else if (cmdAction.isAction("ViewRecordSet"))
            {
                RecordSetIFace recordSet = null;
                if (cmdAction.getData() instanceof RecordSetIFace)
                {
                    recordSet = (RecordSetIFace)cmdAction.getData();
                    
                } else if (cmdAction.getData() instanceof RolloverCommand)
                {
                    RolloverCommand roc = (RolloverCommand)cmdAction.getData();
                    if (roc.getData() instanceof RecordSetIFace)
                    {
                        recordSet = (RecordSetIFace)roc.getData();
                    }
                }
                if (recordSet != null)
                {
                    displayRecordSet(recordSet, (Boolean)cmdAction.getProperty("canModify"));
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#subPaneRemoved(edu.ku.brc.af.core.SubPaneIFace)
     */
    @Override
    public void subPaneRemoved(SubPaneIFace subPane)
    {
        if (subPane == queryResultsPane) {
            queryResultsPane = null;
        } else if (subPane == batchEditResultsPane) {
            batchEditResultsPane = null;
        }
        super.subPaneRemoved(subPane);
    }

    //-------------------------------------------------------------
    // SQLExecutionListener Interface
    //-------------------------------------------------------------
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.SQLExecutionListener#exectionDone(edu.ku.brc.dbsupport.SQLExecutionProcessor, java.sql.ResultSet)
     */
    //@Override
    public synchronized void exectionDone(final SQLExecutionProcessor process, final ResultSet resultSet)
    {
        if (!sqlHasResults)
        {
            try
            {
                sqlHasResults = resultSet.first();
                
            } catch (SQLException ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ExpressSearchTask.class, ex);
            }
        }
        sqlProcessorList.remove(process);
        
        Object[] data = (Object[])process.getData();
        if (data != null)
        {
            if (data.length == 3)
            {
                SearchTableConfig             searchTableConfig = (SearchTableConfig)data[0];
                ExpressSearchResultsPaneIFace esrPane           = (ExpressSearchResultsPaneIFace)data[1];
                String                        searchTerm        = (String)data[2];
                
                Hashtable<String, ExpressResultsTableInfo>       idToTableInfoHash     = ExpressSearchConfigCache.getSearchIdToTableInfoHash();
                Hashtable<String, List<ExpressResultsTableInfo>> joinIdToTableInfohash = ExpressSearchConfigCache.getJoinIdToTableInfoHash();
                Hashtable<String, QueryForIdResultsSQL>          resultsForJoinsHash    = new Hashtable<String, QueryForIdResultsSQL>();
                
                try
                {
                    if (resultSet.next())
                    {
                        String                  searchIdStr = Integer.toString(searchTableConfig.getTableInfo().getTableId());
                        ExpressResultsTableInfo tblInfo = idToTableInfoHash.get(searchIdStr);
                        if (tblInfo == null)
                        {
                            throw new RuntimeException("Bad id from search["+searchIdStr+"]");
                        }
                        
                        SearchConfig config = SearchConfigService.getInstance().getSearchConfig();

                        int cnt = 0;
                        do
                        {
                            if (cnt < RESULTS_THRESHOLD)
                            {
                                collectResults(config, tblInfo.getTableId(), searchTerm, resultSet, null, joinIdToTableInfohash, resultsForJoinsHash);
                            }
                            cnt++;
                        } while(resultSet.next());
                        
                        //System.err.println("SQLExecutionProcessor: "+cnt);
                        
                        if (cnt >= RESULTS_THRESHOLD)
                        {
                            String reason = String.format(getResourceString("ExpressSearchTask.MAX_SEARCH_RESULTS_EXCEEDED"), RESULTS_THRESHOLD, cnt);
                            if (searchWarningsResults == null)
                            {
                                searchWarningsResults = new SIQueryForIdResults();
                                searchWarningsResults.addReason(searchTableConfig, reason);
                                displayResults(esrPane, searchWarningsResults, resultsForJoinsHash);
                                
                            } else
                            {
                                searchWarningsResults.addReason(searchTableConfig, reason);
                            }
                        }
                        
                        if (resultsForJoinsHash.size() > 0)
                        {
                            QueryForIdResultsSQL queryResults = new QueryForIdResultsSQL(searchIdStr, null, tblInfo, searchTableConfig.getDisplayOrder(), searchTerm);
                            displayResults(esrPane, queryResults, resultsForJoinsHash);
                        }
                    }
                } catch (SQLException ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrSQLUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ExpressSearchTask.class, ex);
                    ex.printStackTrace();
                }

            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.SQLExecutionListener#executionError(edu.ku.brc.dbsupport.SQLExecutionProcessor, java.lang.Exception)
     */
    //@Override
    public synchronized void executionError(SQLExecutionProcessor process, Exception ex)
    {
        sqlProcessorList.remove(process);
        
    }
    
    /**
     * Changes the UI component to show that nothing was found.
     */
    protected void setUserInputToNotFound(final String msgKey, final boolean isInError)
    {
        if (isInError)
        {
            if (badSearchColor != null)
            {
                searchText.setBackground(badSearchColor);
            }
            searchText.setSelectionStart(0);
            searchText.setSelectionEnd(searchText.getText().length());

        } else
        {
            //searchText.setBackground(selectionBG);
            
        }
        searchText.getToolkit().beep();
        searchText.repaint();
        searchText.requestFocus();
        
        if (isInError)
        {
            if (statusBar != null)
            {
                statusBar.setLocalizedErrorMessage(msgKey);
            }
            UIRegistry.writeTimedSimpleGlassPaneMsg(getResourceString(msgKey), Color.RED);
            
        } else
        {
            displayNoResults(msgKey);
        }
    }
    
    /**
     * Displays "No Results Message
     * @param msgKey the localized string key
     */
    protected void displayNoResults(final String msgKey)
    {
        String fullMsg = UIRegistry.getLocalizedMessage(StringUtils.isNotEmpty(msgKey) ? msgKey : "NoExpressSearchResults", searchText.getText());
        UIRegistry.writeTimedSimpleGlassPaneMsg(fullMsg, null, null, 24, true);
    }
    
    //-------------------------------------------------------------------------
    //-- CustomQueryListener Interface
    //-------------------------------------------------------------------------
    
    protected synchronized void completionUIHelper(final CustomQueryIFace customQuery)
    {

        if (customQuery != null)
        {
            queryList.remove(customQuery);
            queryCountDone++;
        }
        
        if (statusBar != null)
        {
            statusBar.setValue(EXPRESSSEARCH, queryCountDone);
        }
        
        //System.err.println(customQuery.hashCode()+"  "+queryList.size());
        if (queryList.size() == 0)
        {
            if (searchText != null)
            {
                searchText.setEnabled(true);
                searchBtn.setEnabled(true);
            
                if (!sqlHasResults)
                {
                    setUserInputToNotFound("NoExpressSearchResults", false);
                    
                } else
                {
                    searchText.setSelectionStart(searchText.getText().length());
                    searchText.setSelectionEnd(searchText.getText().length());
                    searchText.repaint();
                    
                    if (statusBar != null)
                    {
                        statusBar.setText("");
                    }
                }
            }
            CommandDispatcher.dispatch(new CommandAction(EXPRESSSEARCH, "SearchComplete"));
        } 
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryListener#exectionDone(edu.ku.brc.dbsupport.CustomQuery)
     */
    //@Override
    public synchronized void exectionDone(final CustomQueryIFace customQuery)
    {
        boolean  addPane  = false;
        JPAQuery jpaQuery = (JPAQuery)customQuery;
        List<?>  list     = jpaQuery.getDataObjects();
        
        if (!sqlHasResults)
        {
            sqlHasResults = !jpaQuery.isInError() && list != null && list.size() > 0;
            addPane = sqlHasResults;
            //System.err.println("AddPane: "+addPane+"  Err "+jpaQuery.isInError()+"  "+(list != null && list.size() > 0));
        }
        
        if (list != null)
        {
            if (list.size() > 0)
            {
                Object[]                      data              = (Object[])jpaQuery.getData();
                SearchTableConfig             searchTableConfig = (SearchTableConfig)data[0];
                final ExpressSearchResultsPaneIFace esrPane     = (ExpressSearchResultsPaneIFace)data[1];
                String                        searchTerm        = (String)data[2];
                
                if (addPane && esrPane instanceof SubPaneIFace)
                {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run()
                        {
                            addSubPaneToMgr((SubPaneIFace)esrPane);
                        }
                    });
                }
        
                Hashtable<String, List<ExpressResultsTableInfo>> joinIdToTableInfoHash = ExpressSearchConfigCache.getJoinIdToTableInfoHash();
                Hashtable<String, QueryForIdResultsSQL>          resultsForJoinsHash   = new Hashtable<String, QueryForIdResultsSQL>();
                
                //log.debug("TID["+searchTableConfig.getTableInfo().getTableId() + "] Table Order ["+searchTableConfig.getDisplayOrder()+"] "+list.size());
                SearchConfig config = SearchConfigService.getInstance().getSearchConfig();
                
                String tableIdStr = Integer.toString(searchTableConfig.getTableInfo().getTableId());
                
                int cnt = 0;
                for (Object idObj : list)
                {
                    if (cnt < RESULTS_THRESHOLD)
                    {
                        collectResults(config, tableIdStr, searchTerm, null, (Integer)idObj, joinIdToTableInfoHash, resultsForJoinsHash);
                    }
                    cnt++;
                }
                
                //System.err.println("CustomQueryIFace: "+cnt);
                
                if (cnt >= RESULTS_THRESHOLD)
                {
                    //String reason = String.format("%d hits returned of %d maximum allowed. ", RESULTS_THRESHOLD, cnt); // I18N
                    //String reason = String.format(getResourceString("QB_HITS_MAX_ALLOWED"), RESULTS_THRESHOLD, cnt); // I18N
                    String reason = UIRegistry.getFormattedResStr("QB_HITS_MAX_ALLOWED", RESULTS_THRESHOLD, cnt);
                    if (searchWarningsResults == null)
                    {
                        searchWarningsResults = new SIQueryForIdResults();
                        searchWarningsResults.addReason(searchTableConfig, reason);
                        displayResults(esrPane, searchWarningsResults, resultsForJoinsHash);
                        
                    } else
                    {
                        searchWarningsResults.addReason(searchTableConfig, reason);
                    }
                }
                
                QueryForIdResultsIFace results = new QueryForIdResultsHQL(searchTableConfig, new Color(30, 144, 255), searchTerm, list);
                results.setMultipleSelection(true);
                displayResults(esrPane, results, resultsForJoinsHash);
                
                //joinIdToTableInfoHash.clear();
            }
           
        } else
        {
            log.error("List was null and cant't be");
        }
        
        completionUIHelper(customQuery);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.CustomQueryListener#executionError(edu.ku.brc.dbsupport.CustomQuery)
     */
    //@Override
    public synchronized void executionError(final CustomQueryIFace customQuery)
    {
        log.error("Error running query!");
        completionUIHelper(customQuery);
    }
    
    
    /**
     * @return the permissions array
     */
    @Override
    protected boolean[][] getPermsArray()
    {
        return new boolean[][] {{true, true, true, true},
                                {true, true, true, true},
                                {true, true, false, false},
                                {true, false, false, false}};
    }
    
    //-------------------------------------------------------------------------
    //-- CustomQueryListener Interface
    //-------------------------------------------------------------------------
    public class SearchBoxMenuCreator implements SearchBox.MenuCreator
    {
        protected List<JComponent>    menus       = null;
        protected SearchConfigService scService;
        protected JMenuItem           allMenuItem  = null;
        protected JMenuItem           configMenuItem  = null;
        protected ActionListener      action;
        
        /**
         * Constructor.
         */
        public SearchBoxMenuCreator()
        {
            scService = SearchConfigService.getInstance();
            
            action = new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    if (e.getSource() == allMenuItem)
                    {
                        scService.setSearchContext(null);
                        
                    } else if (e.getSource() == configMenuItem)
                    {
                        ExpressSearchConfigDlg dlg = new ExpressSearchConfigDlg();
                        dlg.createUI();
                        UIHelper.centerAndShow(dlg); // modal
                        dlg.cleanUp();
                        
                    } else
                    {
                        String miTitle = ((JMenuItem)e.getSource()).getText();
                        for (SearchTableConfig stc : scService.getSearchConfig().getTables())
                        {
                            if (stc.getTitle().equals(miTitle))
                            {
                                scService.setSearchContext(stc);
                            }
                        }
                    }
                }
            };
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.ui.SearchBox.MenuCreator#createPopupMenus()
         */
        public List<JComponent> createPopupMenus()
        {
            if (menus == null)
            {
                menus       = new Vector<JComponent>();
                allMenuItem = new JMenuItem(getResourceString("ALL"), SearchBox.getSearchIcon());
                allMenuItem.addActionListener(action);
                menus.add(allMenuItem);
                
                for (SearchTableConfig stc : scService.getSearchConfig().getTables())
                {
                    // This needed for the first time in before there are any settings
                    if (AppContextMgr.isSecurityOn())
                    {
                        PermissionSettings perm = stc.getTableInfo().getPermissions(); 
                        if (!perm.canView())
                        {
                            continue;
                        }
                    }
                    
                    JMenuItem menuItem = new JMenuItem(stc.getTitle(), IconManager.getIcon(stc.getIconName(), IconManager.IconSize.Std16));
                    menuItem.addActionListener(action);
                    menus.add(menuItem);
                    
                    if (stc.getTableInfo().getTableId() == CollectingEvent.getClassTableId())
                    {
                        boolean isEmbedded = AppContextMgr.getInstance().getClassObject(Collection.class).getIsEmbeddedCollectingEvent();
                        IconEntry ciEntry = IconManager.getIconEntryByName(isEmbedded ? "collectinginformation" : "ce_restore");
                        menuItem.setIcon(ciEntry.getIcon(IconManager.IconSize.Std16));
                    }
                }
                
                if (!AppContextMgr.isSecurityOn() || SpecifyUser.isCurrentUserType(UserType.Manager))
                {
                    boolean permsOKForGlobalSearch = ((SpecifyQueryAdjusterForDomain)QueryAdjusterForDomain.getInstance()).isPermsOKForGlobalSearch();
                    boolean isGlobalSearchAvail    = permsOKForGlobalSearch && AppPreferences.getLocalPrefs().getBoolean("GLOBAL_SEARCH_AVAIL", false);
                    if (isGlobalSearchAvail)
                    {
                        boolean isGSOn = AppPreferences.getLocalPrefs().getBoolean("GLOBAL_SEARCH", false);
                        
                        ActionListener globalSearchAL = new ActionListener()
                        {
                            @Override
                            public void actionPerformed(ActionEvent e)
                            {
                                doGlobalSearchSetup();
                            }
                        };
                        
                        globalSearchCheckBoxMI = new JCheckBoxMenuItem(getResourceString(GLOBAL_SEARCH), 
                                                                       IconManager.getIcon("GlobalSearch", IconManager.IconSize.Std16),
                                                                       isGSOn);
                        globalSearchCheckBoxMI.addActionListener(globalSearchAL);
                        
                        menus.add(new JSeparator());
                        menus.add(globalSearchCheckBoxMI);
                    }
                } else
                {
                    AppPreferences.getLocalPrefs().remove(GLOBAL_SEARCH);
                }

                if (globalSearchCheckBoxMI == null)
                {
                    menus.add(new JSeparator());
                }
                configMenuItem = new JMenuItem(getResourceString("ESConfig"), IconManager.getIcon("SystemSetup", IconManager.IconSize.Std16));
                configMenuItem.addActionListener(action);
                menus.add(configMenuItem);
            }
            
            return menus;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.SearchBox.MenuCreator#reset()
         */
        public void reset()
        {
            this.menus = null;
        }
    }
    
    /**
     * 
     */
    private void doGlobalSearchSetup()
    {
        if (globalSearchCheckBoxMI.isSelected())
        {
            AppPreferences.getLocalPrefs().putBoolean(GLOBAL_SEARCH, true);
        } else
        {
            AppPreferences.getLocalPrefs().remove(GLOBAL_SEARCH);
            searchBox.resetSearchIcon();
        }
    }

    protected ESResultsSubPane getQResultsPane(final QueryForIdResultsIFace results) {
        //see earlier revision for comments if problems arise with contains(results) calls.
        if (queryResultsPane != null && results != null && queryResultsPane.contains(results)) {
            return queryResultsPane;
        } else if (batchEditResultsPane != null && results != null && batchEditResultsPane.contains(results)) {
            return batchEditResultsPane;
        } else if (rsQbResultsPane != null && results != null && rsQbResultsPane.contains(results)) {
            return rsQbResultsPane;
        } else {
            return null;
        }
    }
    /**
     * @param cmdAction
     */
    protected void doSearchComplete(final CommandAction cmdAction) {
        if (statusBar != null) {
            statusBar.setProgressDone(EXPRESSSEARCH);
            if (cmdAction.getData() instanceof JPAQuery) {
                QueryForIdResultsIFace   results = (QueryForIdResultsIFace)cmdAction.getProperty("QueryForIdResultsIFace");
                ESResultsTablePanelIFace esrto   = (ESResultsTablePanelIFace)cmdAction.getProperty("ESResultsTablePanelIFace");

                ESResultsSubPane qResults = getQResultsPane(results);
                if (qResults == null && esrto != null && !esrto.hasResults()) {
                    results.complete();
                    if (!(results instanceof QBQueryForIdResultsHQL && results.isCount())) {
                        displayNoResults("QB_NO_RESULTS");
                    }
                    return;
                }

                //Only execute this block for QueryBuilder results...
                if (qResults != null) {
                    int     rowCount = ((JPAQuery) cmdAction.getData()).getDataObjects().size();
                    boolean isError  = ((JPAQuery) cmdAction.getData()).isInError();
                    boolean isCancelled = ((JPAQuery) cmdAction.getData()).isCancelled();
                    boolean showPane = !isError && !isCancelled && rowCount > 0;
                    //print status bar msg if error or rowCount == 0, 
                    //else show the queryResults pane if rowCount > 0
                    int index = SubPaneMgr.getInstance().indexOfComponent(qResults.getUIComponent());
                    if (index == -1) {
                        if (showPane) {
                            addSubPaneToMgr(qResults);
                        }
                    } else {
                        if (showPane) {
                            SubPaneMgr.getInstance().showPane(qResults);
                        } else {
                            SubPaneMgr.getInstance().removePane(qResults, false);
                        }
                    }
                    results.complete();
                    if (isError) {
                        statusBar.setErrorMessage(getResourceString("QB_RUN_ERROR"));
                    } else if (rowCount == 0) {
                        displayNoResults("QB_NO_RESULTS");
                    } else {
                        statusBar.setText(null);
                    }
                }
            }
        }
    }
}
