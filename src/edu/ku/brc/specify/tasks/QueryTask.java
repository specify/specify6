/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.ref.SoftReference;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.DroppableNavBox;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.NavBoxMgr;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;

import edu.ku.brc.af.ui.db.ERTICaptionInfo;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.forms.FormHelper;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpReport;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.tasks.subpane.ESResultsTablePanel;
import edu.ku.brc.specify.tasks.subpane.SQLQueryPane;
import edu.ku.brc.specify.tasks.subpane.qb.ERTICaptionInfoQB;
import edu.ku.brc.specify.tasks.subpane.qb.QBLiveJRDataSource;
import edu.ku.brc.specify.tasks.subpane.qb.QBReportInfoPanel;
import edu.ku.brc.specify.tasks.subpane.qb.QBResultReportServiceCmdData;
import edu.ku.brc.specify.tasks.subpane.qb.QBResultReportServiceInfo;
import edu.ku.brc.specify.tasks.subpane.qb.QueryBldrPane;
import edu.ku.brc.specify.tasks.subpane.qb.TableTree;
import edu.ku.brc.specify.tasks.subpane.qb.TreeLevelQRI;
import edu.ku.brc.specify.tools.schemalocale.SchemaLocalizerDlg;
import edu.ku.brc.specify.ui.db.ResultSetTableModel;
import edu.ku.brc.specify.ui.treetables.TreeDefinitionEditor;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DataFlavorTableExt;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.ToggleButtonChooserPanel;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.Trash;
import edu.ku.brc.util.Pair;

/**
 * This task will enable the user to create queries, save them and execute them.
 *
 * @code_status Alpha
 *
 * @author rods
 *
 */
public class QueryTask extends BaseTask 
{
    private static final Logger log = Logger.getLogger(QueryTask.class);
    
    // Static Data Members
    public static final String QUERY                = "Query";
    public static final String SAVE_QUERY           = "Save";
    public static final String QUERY_RESULTS_REPORT = "QueryResultsReport";
    protected static final String XML_PATH_PREF     = "Query.XML.Dir";
    
    public static final DataFlavor QUERY_FLAVOR = new DataFlavor(QueryTask.class, QUERY);
    
    protected QueryBldrPane                               queryBldrPane             = null;
    protected SoftReference<TableTree>                    tableTree                 = null;
    protected SoftReference<Hashtable<String, TableTree>> tableTreeHash             = null;
    protected final AtomicBoolean                         localizationOrTreeDefEdit = new AtomicBoolean(false);
    
    protected Vector<ToolBarDropDownBtn> tbList           = new Vector<ToolBarDropDownBtn>();
    protected Vector<JComponent>         menus            = new Vector<JComponent>();
    protected Vector<NavBoxIFace>        extendedNavBoxes = new Vector<NavBoxIFace>();
    protected DroppableNavBox            navBox           = null;
    protected NavBox                     actionNavBox     = null;
    
    protected Vector<String>             favQueries       = new Vector<String>();
    protected Vector<String>             freqQueries;
    protected Vector<String>             extraQueries;
    protected Vector<String>             stdQueries       = new Vector<String>();
    protected int                        nonFavCount      = 0;
    
    //protected List<DBTableInfo>               tableInfos       = new ArrayList<DBTableInfo>();
    
    /**
     * Default Constructor
     *
     */
    public QueryTask()
    {
        super(QUERY, getResourceString(QUERY));
        
        CommandDispatcher.register(QUERY, this);   
        CommandDispatcher.register(TreeDefinitionEditor.TREE_DEF_EDITOR, this);
        CommandDispatcher.register(SchemaLocalizerDlg.SCHEMA_LOCALIZER, this);
    }
    
    
    /**
     * Ask the user for information needed to fill in the data object. (Could be refactored with WorkBench Task)
     * @param data the data object
     * @return true if OK, false if cancelled
     */
    public static boolean askUserForInfo(final String viewSetName, 
                                         final String dlgTitle,
                                         final SpQuery query)
    {
        ViewBasedDisplayDialog editorDlg = new ViewBasedDisplayDialog(
                (Frame)UIRegistry.getTopWindow(),
                "Global",
                viewSetName,
                null,
                dlgTitle,
                getResourceString("OK"),
                null, // className,
                null, // idFieldName,
                true, // isEdit,
                MultiView.HIDE_SAVE_BTN);
        
        editorDlg.preCreateUI();
        editorDlg.setData(query);
        editorDlg.getMultiView().preValidate();
        editorDlg.setModal(true);
        editorDlg.setVisible(true);

        if (!editorDlg.isCancelled())
        {
            editorDlg.getMultiView().getDataFromUI();
        }
        editorDlg.dispose();
        
        return !editorDlg.isCancelled();
    }
    
    /**
     * Creates a new Query Data Object.
     * @param tableInfo the table information
     * @return the query
     */
    protected SpQuery createNewQueryDataObj(final DBTableInfo tableInfo)
    {
        SpQuery query = new SpQuery();
        query.initialize();
        query.setSpecifyUser(AppContextMgr.getInstance().getClassObject(SpecifyUser.class));
        query.setName(String.format(getResourceString("QB_NEW_QUERY_NAME"), tableInfo.getTitle()));
        query.setNamed(false);
        query.setContextTableId((short)tableInfo.getTableId());
        return query;
    }


    /**
     * Creates pane and executes a query.
     * @param sqlStr SQL to be executed
     */
    public void createAndExecute(final String sqlStr)
    {
        SQLQueryPane sqlPane = new SQLQueryPane(name, this, false, false);//true, true);
        addSubPaneToMgr(sqlPane);
        sqlPane.setSQLStr(sqlStr);
        sqlPane.doQuery();
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        return starterPane = StartUpTask.createFullImageSplashPanel(title, this);
    }
    
    //-------------------------------------------------------
    // Taskable
    //-------------------------------------------------------
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#preInitialize()
     */
    @Override
    public void preInitialize()
    {
        // Create and add the Actions NavBox first so it is at the top at the top
        actionNavBox = new NavBox(getResourceString("QB_CREATE_QUERY"));
        addNewQCreators();
    }
    
    /**
     * Reads the Query Lists from the database.
     */
    protected void readOrgLists()
    {
        freqQueries  = readResourceForList("QueryFreqList");
        extraQueries = readResourceForList("QueryExtraList");
        if (UIHelper.isSecurityOn())
        {
            securityFilter(freqQueries);
            securityFilter(extraQueries);
        }
    }
    
    /**
     * @param list
     * 
     * Removes tables from list if they are not viewable.
     * Assumes security is ON.
     */
    protected void securityFilter(Vector<String> list)
    {
        for (int t = list.size()-1; t >= 0; t--)
        {
            String tblName = list.get(t);
            if (!DBTableIdMgr.getInstance().getByShortClassName(tblName).getPermissions().canView())
            {
                list.remove(t);
            }
        }
    }
    
    /**
     * Reads a single list from the database.
     * @param resName the name of the resource to use to save it.
     * @return the list 
     */
    @SuppressWarnings("unchecked")
    protected Vector<String> readResourceForList(final String resourceName)
    {
        Vector<String>   list   = null;
        String           xmlStr = null;
        AppResourceIFace appRes = AppContextMgr.getInstance().getResourceFromDir("Personal", resourceName);
        if (appRes != null)
        {
            xmlStr = appRes.getDataAsString();
            
        } else
        {
            // Get the default resource by name and copy it to a new User Area Resource
            AppResourceIFace newAppRes = AppContextMgr.getInstance().copyToDirAppRes("Personal", resourceName);
            if (newAppRes != null)
            {
                // Save it in the User Area
                AppContextMgr.getInstance().saveResource(newAppRes);
                xmlStr = newAppRes.getDataAsString();
            } else
            {
                xmlStr = "";
            }
        }
        
        if (StringUtils.isNotEmpty(xmlStr))
        {
            XStream xstream = new XStream();
            list = (Vector<String>)xstream.fromXML(xmlStr);
        }
        //log.debug(xmlStr);

        if (list == null)
        {
            list = new Vector<String>();
        }
        return list;
    }
    
    /**
     * Saves a single list to the database.
     * @param resourceName the name of the resource to use to save it.
     * @param list the list to be saved.
     */
    protected void saveQueryList(final String resourceName,
                                 final Vector<String> list)
    {
        XStream xstream = new XStream();
        AppResourceIFace uaAppRes = AppContextMgr.getInstance().getResourceFromDir("Personal", resourceName);
        if (uaAppRes != null)
        {
            uaAppRes.setDataAsString(xstream.toXML(list));
            AppContextMgr.getInstance().saveResource(uaAppRes);
            
        } else
        {
            AppContextMgr.getInstance().putResourceAsXML(resourceName, xstream.toXML(list));     
        }
    }
    
    /**
     * Saves the Lists to the database.
     */
    protected void saveQueryListConfiguration()
    {
        saveQueryList("QueryFreqList", freqQueries);
        saveQueryList("QueryExtraList", extraQueries);
    }
    
    /**
     * Configure the Query Creators.
     */
    protected void configureCreatorQueries()
    {
        QueryCreatorsConfigureDlg dlg = new QueryCreatorsConfigureDlg(QueryTask.this, freqQueries, extraQueries, stdQueries);
        UIHelper.centerAndShow(dlg);
        if (!dlg.isCancelled())
        {
            actionNavBox.clear();
            
            Vector<String> freqs  = dlg.getFreqQueries();
            Vector<String> extras = dlg.getExtraQueries();
            
            buildNavBoxes(freqs, extras);
            
            actionNavBox.validate();
            actionNavBox.doLayout();
            NavBoxMgr.getInstance().validate();
            NavBoxMgr.getInstance().doLayout();
            NavBoxMgr.getInstance().repaint();
            
            freqQueries  = freqs;
            extraQueries = extras;
            
            // Persist out to database
            saveQueryListConfiguration();
        }
    }
    
    /**
     * Confgiure Queries that show in the sidebar.
     */
    protected void configureFavoriteQueries()
    {
        QueryConfigureDlg dlg = new QueryConfigureDlg(QueryTask.this);
        UIHelper.centerAndShow(dlg);
        if (!dlg.isCancelled())
        {
            Vector<SpQuery> favs  = dlg.getFavQueries();
            Vector<SpQuery> extras = dlg.getOtherQueries();
            
            Hashtable<Integer, Integer> hash = new Hashtable<Integer, Integer>();
            for (SpQuery q : favs)
            {
                int id = q.getId();
                hash.put(id, id);
            }
            
            for (NavBoxItemIFace nbi : new Vector<NavBoxItemIFace>(navBox.getItems()))
            {
                RecordSetIFace     rs  = (RecordSetIFace)nbi.getData();
                if (rs != null)
                {
                    RecordSetItemIFace rsi = rs.getOnlyItem();
                    
                    Integer idInt = rsi.getRecordId();
                    
                    if (idInt != null)
                    {
                        int id = idInt;
                        if (hash.get(id) == null)
                        {
                            navBox.remove(nbi);
                        }
                    }
                }
            }
            
            hash.clear();
            for (NavBoxItemIFace nbi : navBox.getItems())
            {
                RecordSetIFace     rs  = (RecordSetIFace)nbi.getData();
                if (rs != null)
                {
                    RecordSetItemIFace rsi = rs.getOnlyItem();
                    int id = rsi.getRecordId();
                    hash.put(id, id);
                }
            }
            
            for (SpQuery query : favs)
            {
                int id = query.getId();
                if (hash.get(id) == null)
                {
                    RecordSet rs = new RecordSet();
                    rs.initialize();
                    rs.set(query.getName(), SpQuery.getClassTableId(), RecordSet.GLOBAL);
                    rs.addItem(query.getSpQueryId());
                    addToNavBox(rs);
                }
            }
            // Persist out to database
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                session.beginTransaction();
                short order = 0;
                for (SpQuery query : favs)
                {
                    query.setIsFavorite(true);
                    query.setOrdinal(order++);
                    session.saveOrUpdate(query);
                }
                for (SpQuery query : extras)
                {
                    query.setIsFavorite(false);
                    query.setOrdinal(null);
                    session.saveOrUpdate(query);
                }
                session.commit();
                
            } catch (Exception ex)
            {
                // XXX Error dialog
                session.rollback();
            }
            finally
            {
                if (session != null)
                {
                    session.close();
                }
            }
            
            // Now Reorder list
            Hashtable<Integer, NavBoxItemIFace> nbiHash = new Hashtable<Integer, NavBoxItemIFace>();
            for (NavBoxItemIFace nbi : navBox.getItems())
            {
                RecordSetIFace     rs  = (RecordSetIFace)nbi.getData();
                if (rs != null)
                {
                    RecordSetItemIFace rsi = rs.getOnlyItem();
                    int id = rsi.getRecordId();
                    nbiHash.put(id, nbi);
                }
            }
            
            navBox.clear();
            Collections.sort(favs, new Comparator<SpQuery>() {
                public int compare(SpQuery q1, SpQuery q2)
                {
                    return q1.getOrdinal().compareTo(q2.getOrdinal());
                }
            });
            for (SpQuery query : favs)
            {
                int id = query.getId();
                navBox.add(nbiHash.get(id));
            }
            
            checkForOtherNavBtn();
            
            navBox.validate();
            navBox.doLayout();
            NavBoxMgr.getInstance().validate();
            NavBoxMgr.getInstance().doLayout();
            NavBoxMgr.getInstance().repaint();
        }
    }
    
    /**
     * Checks to see if the Other btn for "Other Queries" should be added.
     */
    protected void checkForOtherNavBtn()
    {
        // XXX Users will probably want to share queries??
        String sqlStr = "From SpQuery as sq Inner Join sq.specifyUser as user where sq.isFavorite = false AND user.specifyUserId = "
                + AppContextMgr.getInstance().getClassObject(SpecifyUser.class).getSpecifyUserId();
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            List<?> queries = session.getDataList(sqlStr);
            int count = 0;
            for (Iterator<?> iter = queries.iterator(); iter.hasNext();)
            {
                Object[] obj = (Object[]) iter.next();
                SpQuery query = (SpQuery) obj[0];
                if (!UIHelper.isSecurityOn()
                        || DBTableIdMgr.getInstance().getInfoById(query.getContextTableId())
                                .getPermissions().canView())
                {
                    count = 1;
                    break;
                }
            }
            if (count > 0)
            {
                NavBoxItemIFace nbi = NavBox.createBtnWithTT(getResourceString("QY_OTHER_QUERIES"),
                        name, getResourceString("QY_OTHER_QUERIES_TT"), IconManager.STD_ICON_SIZE,
                        new ActionListener()
                        {
                            public void actionPerformed(ActionEvent e)
                            {
                                showOtherViewsDlg();
                            }
                        });

                navBox.add(nbi);
            }
        }
        finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.af.tasks.BaseTask#isConfigurable()
     */
    @Override
    public boolean isConfigurable()
    {
        return true;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.af.tasks.BaseTask#doConfigure()
     */
    @Override
    public void doConfigure()
    {
        String configCreators = UIRegistry.getResourceString("QY_CONFIGURE_CREATORS");
        String configQueries  = UIRegistry.getResourceString("QY_CONFIGURE_QUERIES");
        
        Vector<String> configNames = new Vector<String>();
        Collections.addAll(configNames, configCreators, configQueries);
        
        ToggleButtonChooserDlg<String> dlg = new ToggleButtonChooserDlg<String>((Frame)UIRegistry.getTopWindow(), 
                "QY_CHOOSE_CONFIG", 
                configNames, 
                ToggleButtonChooserPanel.Type.RadioButton);
        dlg.setUseScrollPane(true);
        dlg.setVisible(true);
        if (!dlg.isCancelled())
        {
            if (dlg.getSelectedObject().equals(configCreators))
            {
                configureCreatorQueries();
            } else
            {
                configureFavoriteQueries();
            }
        }
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getPopupMenu()
     */
    @Override
    public JPopupMenu getPopupMenu()
    {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem mi = new JMenuItem(UIRegistry.getResourceString("QY_CONFIGURE_CREATORS"));
        popupMenu.add(mi);
        
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                configureCreatorQueries();
            }
        });
        
        mi = new JMenuItem(UIRegistry.getResourceString("QY_CONFIGURE_QUERIES"));
        popupMenu.add(mi);
        
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                configureFavoriteQueries();
            }
        });
        
        mi = new JMenuItem(UIRegistry.getResourceString("QY_IMPORT_QUERIES"));
        popupMenu.add(mi);
        
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                importQueries();
            }
        });
        
        mi = new JMenuItem(UIRegistry.getResourceString("QY_EXPORT_QUERIES"));
        popupMenu.add(mi);
        
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                exportQueries();
            }
        });
        
        return popupMenu;
    }

    
    /**
     * Builds the NavBtns for the grequently used list and the extra list.
     * @param freqList the frequently used list of names
     * @param extraList the list of extra (hidden) names
     */
    protected void buildNavBoxes(final Vector<String> freqList,
                                 final Vector<String> extraList)
    {
        createCreateQueryNavBtns(freqList);
        
        if (extraList != null && !extraList.isEmpty())
        {
            NavBoxItemIFace nbi = NavBox.createBtnWithTT(getResourceString("QY_EXTRA_TABLES"),
                                                         "MoreTables", 
                                                         getResourceString("QY_EXTRA_TABLES_TT"), 
                                                         IconManager.STD_ICON_SIZE, new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    showMiscViewsDlg();
                }
            });
            
            actionNavBox.add(nbi);
        }

    }
    
    
    /**
     * Show a dialog letting them choose from a list of available misc views.   
     */
    protected void showMiscViewsDlg()
    {
        String shortClassName = null;
        if (extraQueries.size() == 1)
        {
            shortClassName = extraQueries.get(0);
            DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByShortClassName(shortClassName);
            if (tableInfo != null)
            {
                createNewQuery(tableInfo);
            }
            
        } else
        {
            final Hashtable<String, DBTableInfo> tiHash = new Hashtable<String, DBTableInfo>();
            Vector<String> names = new Vector<String>();
            
            for (String sName : extraQueries)
            {
                DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByShortClassName(sName);
                names.add(tableInfo.getTitle());
                tiHash.put(tableInfo.getTitle(), tableInfo);
                
            }
            ToggleButtonChooserDlg<String> dlg = new ToggleButtonChooserDlg<String>((Frame)UIRegistry.getTopWindow(), 
                    "QY_EXTRA_TABLES", 
                    names, 
                    ToggleButtonChooserPanel.Type.RadioButton);
            dlg.setUseScrollPane(true);
            dlg.setVisible(true);
            if (!dlg.isCancelled())
            {
                String      sName     = dlg.getSelectedObject();
                DBTableInfo tableInfo = tiHash.get(sName);
                if (tableInfo != null)
                {
                    createNewQuery(tableInfo);
                }
            }
        }
    }
    
    /**
     * Show a dialog letting them choose from a list of available misc views.   
     */
    @SuppressWarnings("unchecked")
    protected void showOtherViewsDlg()
    {
        List<SpQuery> queryList = new Vector<SpQuery>();
        List<?>       rows      = null;
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            rows    = session.getDataList("FROM SpQuery as sq Inner Join sq.specifyUser as user where sq.isFavorite = false AND user.specifyUserId = "+AppContextMgr.getInstance().getClassObject(SpecifyUser.class).getSpecifyUserId() + " ORDER BY sq.name");
        }
        finally
        {
            if (session != null)
            {
                session.close();
            }
        }

        
        if (rows.size() == 1)
        {
            final Object[] row = (Object[])rows.iterator().next();
            new EditOtherQueryWorker(((SpQuery)row[0]).getId()).start();
            
        } else
        {
            for (Object obj : rows)
            {
                Object[] row = (Object[])obj;
                queryList.add((SpQuery)row[0]); 
            }
            ToggleButtonChooserDlg<SpQuery> dlg = new ToggleButtonChooserDlg<SpQuery>((Frame)UIRegistry.getTopWindow(), 
                    "QY_OTHER_QUERIES", 
                    queryList, 
                    ToggleButtonChooserPanel.Type.RadioButton);
            dlg.setUseScrollPane(true);
            dlg.setVisible(true);
            if (!dlg.isCancelled())
            {
                new EditOtherQueryWorker(dlg.getSelectedObject().getId()).start();
            }
        }
    }
    

    /**
     * Creates all the NavBtns from a list of Queries create names.
     * @param list the list of names
     */
    protected void createCreateQueryNavBtns(final Vector<String> list)
    {
        for (String shortClassName : list)
        {
            createQueryCreateNB(shortClassName);
        }
    }
    
    /**
     * Creates a single btn for a new query
     * @param shortClassName the class name
     */
    protected void createQueryCreateNB(final String shortClassName)
    {
        final DBTableInfo tableInfo = DBTableIdMgr.getInstance()
                .getByShortClassName(shortClassName);
        actionNavBox.add(NavBox.createBtnWithTT(String.format(
                getResourceString("QB_CREATE_NEWQUERY"), tableInfo.getTitle()), tableInfo
                .getName(),
                // name,
                getResourceString("QB_CREATE_NEWQUERY_TT"), IconManager.STD_ICON_SIZE,
                new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        new NewQueryWorker(tableInfo).start();
                    }
                }));
    }

    /**
     * Adds the NavBtns for creating new queries.
     */
    protected void addNewQCreators()
    {
        readOrgLists();
        buildNavBoxes(freqQueries, extraQueries);
        
        try
        {
            Element root       = XMLHelper.readDOMFromConfigDir("querybuilder.xml");
            List<?> tableNodes = root.selectNodes("/database/table");
            for (Object obj : tableNodes)
            {
                String sName = XMLHelper.getAttr((Element)obj, "name", null);
                if (!UIHelper.isSecurityOn() || DBTableIdMgr.getInstance().getByShortClassName(sName).getPermissions().canView())
                {
                    stdQueries.add(sName);
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
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

            loadQueries();
            
            navBoxes.add(actionNavBox);
            navBoxes.add(navBox);
        }
        isShowDefault = true;
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        toolbarItems = new Vector<ToolBarItemDesc>();

        String label    = getResourceString(name);
        String localIconName = name;
        String hint     = getResourceString("search_hint");
        ToolBarDropDownBtn btn = createToolbarButton(label,localIconName,hint,menus);
        if (tbList.size() == 0)
        {
            tbList.add(btn);
        }
        toolbarItems.add(new ToolBarItemDesc(btn));

        return toolbarItems;

    }
    
    /**
     * Adds a Query to the Left Pane NavBox (Refactor this with Workbench)
     * @param query the Query to be added
     * @return the nav box
     */
    protected NavBoxItemIFace addToNavBox(final RecordSet recordSet)
    {
        boolean canDelete = UIHelper.isSecurityOn() ? getPermissions().canDelete() : true;
        
        final RolloverCommand roc = (RolloverCommand) makeDnDNavBtn(navBox, recordSet.getName(),
                "Query", null,
                canDelete ? new CommandAction(QUERY, DELETE_CMD_ACT, recordSet) : null, 
                true, true);
        roc.setToolTip(getResourceString("QY_CLICK2EDIT"));
        roc.setData(recordSet);
        roc.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                new EditQueryWorker(recordSet.getOnlyItem().getRecordId(), (RolloverCommand) e
                        .getSource()).start();
            }
        });
        NavBoxItemIFace nbi = (NavBoxItemIFace)roc;
        
        DBTableInfo tblInfo = DBTableIdMgr.getInstance().getInfoById(recordSet.getTableId());
        if (tblInfo != null)
        {
            ImageIcon rsIcon = tblInfo.getIcon(IconManager.STD_ICON_SIZE);
            if (rsIcon != null)
            {
                nbi.setIcon(rsIcon);
            }
        }
        
        roc.addDragDataFlavor(new DataFlavorTableExt(QueryTask.class, QUERY, recordSet.getTableId()));
        if (canDelete)
        {
            roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
        }        
        return nbi;
    }
    
    /**
     * Loads the Queries from the Database
     */
    protected void loadQueries()
    {
        navBox = new DroppableNavBox(getResourceString("QUERIES"), QUERY_FLAVOR, QUERY, SAVE_QUERY);
        // XXX Users will probably want to share queries??
        String sqlStr = "From SpQuery as sq Inner Join sq.specifyUser as user where sq.isFavorite = true AND user.specifyUserId = "
                + AppContextMgr.getInstance().getClassObject(SpecifyUser.class).getSpecifyUserId()
                + " ORDER BY ordinal";

        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            List<?> queries = session.getDataList(sqlStr);

            for (Iterator<?> iter = queries.iterator(); iter.hasNext();)
            {
                Object[] obj = (Object[]) iter.next();
                SpQuery query = (SpQuery) obj[0];
                if (!UIHelper.isSecurityOn()
                        || DBTableIdMgr.getInstance().getInfoById(query.getContextTableId())
                                .getPermissions().canView())
                {
                    RecordSet rs = new RecordSet();
                    rs.initialize();
                    rs.set(query.getName(), SpQuery.getClassTableId(), RecordSet.GLOBAL);
                    rs.addItem(query.getSpQueryId());
                    addToNavBox(rs);
                }
            }

        }
        finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        checkForOtherNavBtn();
    }
    
    /**
     * 
     */
    protected void createNewQuery(final DBTableInfo tableInfo)
    {
        if (queryBldrPane == null || queryBldrPane.aboutToShutdown())
        {
            SpQuery query = createNewQueryDataObj(tableInfo);
            if (query != null)
            {
                editQuery(query);
            }
        }
    }
    
    /**
     * @param queryId
     */
    protected void editQuery(Integer queryId)
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            Object dataObj = session.getData(SpQuery.class, "spQueryId", queryId, DataProviderSessionIFace.CompareType.Equals);
            if (dataObj != null)
            {
                editQuery((SpQuery)dataObj);
            }
        } catch (Exception ex)
        {
            throw new RuntimeException(ex);
            
        }
        finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#requestContext()
     */
    public void requestContext()
    {
        ContextMgr.requestContext(this);

        if (starterPane == null)
        {
            if (queryBldrPane == null)
            {
                super.requestContext();
                
            } else
            {
                SubPaneMgr.getInstance().showPane(queryBldrPane);
            }
            
        } else  
        {
            if (SubPaneMgr.getInstance().indexOfComponent(starterPane.getUIComponent()) > -1)
            {
                SubPaneMgr.getInstance().showPane(starterPane);
            }
        }
    }
    
    /**
     * @param query
     */
    protected void editQuery(final SpQuery query)
    {
        QueryBldrPane newPane = new QueryBldrPane(query.getName(), this, query);
        
        if (starterPane != null)
        {
            SubPaneMgr.getInstance().replacePane(starterPane, newPane);
            starterPane = null;
        }
        else if (queryBldrPane != null)
        {
            SubPaneMgr.getInstance().replacePane(queryBldrPane, newPane);
        }
        queryBldrPane = newPane;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.specify.core.Taskable#getNavBoxes()
     */
    @Override
    public java.util.List<NavBoxIFace> getNavBoxes()
    {
        initialize();

        extendedNavBoxes.clear();
        extendedNavBoxes.addAll(navBoxes);

        return extendedNavBoxes;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#isSingletonPane()
     */
    @Override
    public boolean isSingletonPane()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getTaskClass()
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
     * Save it out to persistent storage.
     * @param query the SpQuery
     */
    protected void persistQuery(final SpQuery query)
    {
        // TODO Add StaleObject Code from FormView
        if (DataModelObjBase.save(true, query))
        {
            FormHelper.updateLastEdittedInfo(query);
        }
    }
    
    /**
     * Save a record set.
     * @param recordSets the rs to be saved
     */
    public RolloverCommand saveNewQuery(final SpQuery query, final boolean enabled)
    {        
        query.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        query.setSpecifyUser(AppContextMgr.getInstance().getClassObject(SpecifyUser.class));
        if (query.getIsFavorite() == null)
        {
            query.setIsFavorite(true);
        }
        
        persistQuery(query);
        
        RecordSet rs = new RecordSet();
        rs.initialize();
        rs.set(query.getName(), SpQuery.getClassTableId(), RecordSet.GLOBAL);
        rs.addItem(query.getSpQueryId());
        
        RolloverCommand roc = (RolloverCommand)addToNavBox(rs);
        roc.setEnabled(enabled);

        NavBoxMgr.getInstance().addBox(navBox);

        // XXX this is pathetic and needs to be generized
        navBox.invalidate();
        navBox.setSize(navBox.getPreferredSize());
        navBox.doLayout();
        navBox.repaint();
        NavBoxMgr.getInstance().invalidate();
        NavBoxMgr.getInstance().doLayout();
        NavBoxMgr.getInstance().repaint();
        UIRegistry.forceTopFrameRepaint();
        
        
        String msg = String.format(getResourceString("WB_SAVED"), new Object[] { query.getName()} );
        UIRegistry.getStatusBar().setText(msg);

        return roc;
    }
    
    /**
     * Delete a record set
     * @param rs the recordSet to be deleted
     */
    protected boolean deleteQuery(final RecordSet rs)
    {
        // delete from database
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        boolean transOpen = false;
        SpQuery query = session.get(SpQuery.class, rs.getOnlyItem().getRecordId());
        try
        {
            query.forceLoad(true);
            if (query.getReports().size() > 0)
            {
                CustomDialog cd = new CustomDialog((Frame )UIRegistry.getTopWindow(), UIRegistry.getResourceString("REP_CONFIRM_DELETE_TITLE"),
                        true, CustomDialog.OK_BTN, new QBReportInfoPanel(query, UIRegistry.getResourceString("QB_UNDELETABLE_REPS")));
                UIHelper.centerAndShow(cd);
                cd.dispose();
                return false;
            }
            int option = JOptionPane.showOptionDialog(UIRegistry.getMostRecentWindow(), 
                    String.format(UIRegistry.getResourceString("REP_CONFIRM_DELETE"), query.getName()),
                    UIRegistry.getResourceString("REP_CONFIRM_DELETE_TITLE"), 
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.NO_OPTION); // I18N
            
            
            if (option == JOptionPane.YES_OPTION)
            {
                session.beginTransaction();
                transOpen = true;
                session.delete(query);
                session.commit();
                transOpen = false;
                return true;
            }
            
        } catch (Exception ex)
        {
            if (transOpen)
            {
                session.rollback();
            }
            ex.printStackTrace();
            log.error(ex);
        }
        finally
        {
            session.close();
        }
        return false;
    }


    /**
     * Delete the RecordSet from the UI, which really means remove the NavBoxItemIFace.
     * This method first checks to see if the boxItem is not null and uses that, if
     * it is null then it looks the box up by name and used that
     * @param boxItem the box item to be deleted
     * @param recordSets the record set that is "owned" by some UI object that needs to be deleted (used for secondary lookup
     */
    protected void deleteQueryFromUI(final NavBoxItemIFace boxItem, final RecordSet rs)
    {
        deleteDnDBtn(navBox, boxItem != null ? boxItem : getBoxByTitle(navBox, rs.getName()));
    }
    
       /**
     * Processes all Commands of type QUERY.
     * @param cmdAction the command to be processed
     */
    @SuppressWarnings("unchecked")
    protected void processQueryCommands(final CommandAction cmdAction)
    {
        if (cmdAction.isAction(DELETE_CMD_ACT) && cmdAction.getData() instanceof RecordSetIFace)
        {
            RecordSet recordSet = (RecordSet)cmdAction.getData();
            if (deleteQuery(recordSet))
            {
                deleteQueryFromUI(null, recordSet);
            }
        }
        if (cmdAction.isAction(QUERY_RESULTS_REPORT))
        {
            if (cmdAction.getData() instanceof QBResultReportServiceCmdData)
            {
               QBResultReportServiceCmdData srvData = (QBResultReportServiceCmdData)cmdAction.getData();
               List<QBResultReportServiceInfo> reps = new Vector<QBResultReportServiceInfo>(srvData.getInfo().getReports());
               QBResultReportServiceInfo selectedRep = null;
               if (reps.size() == 0)
               {
                   log.error("no reports for query. Should't have gotten here.");
               }
               else
               {
                   ChooseFromListDlg<QBResultReportServiceInfo> dlg = new ChooseFromListDlg<QBResultReportServiceInfo>((Frame) UIRegistry
                           .getTopWindow(), UIRegistry.getResourceString("REP_CHOOSE_SP_REPORT"),
                           reps);
                   dlg.setVisible(true);
                   if (dlg.isCancelled()) { return; }
                   selectedRep = dlg.getSelectedObject();
                   dlg.dispose();
               }
               if (selectedRep == null || selectedRep.getFileName() == null) { return; }

               JTable dataTbl = ((ESResultsTablePanel)srvData.getData()).getTable();
               ResultSetTableModel rsm = (ResultSetTableModel)dataTbl.getModel();
               Object src;
               if (selectedRep.isLiveData())
               {
                   //XXX - probably a smoother way to handle these generic issues. (type safety warning)
                   List<? extends ERTICaptionInfo> captions = srvData.getInfo().getVisibleCaptionInfo();
                   src = new QBLiveJRDataSource(rsm, (List<ERTICaptionInfoQB> )captions, selectedRep.getRepeats());
               }
               else
               {
                   src = rsm.getRecordSet(null, true);
               }
               final CommandAction cmd = new CommandAction(ReportsBaseTask.REPORTS,
                       ReportsBaseTask.PRINT_REPORT, src);
               cmd.setProperty("title", srvData.getInfo().getTitle());
               cmd.setProperty("file", selectedRep.getFileName());
               if (selectedRep.isRequiresNewConnection())
               {
                   RecordSet repRS  = new RecordSet();
                   repRS.initialize();
                   repRS.set(selectedRep.getReportName(), SpReport.getClassTableId(), RecordSet.GLOBAL);
                   repRS.addItem(selectedRep.getSpReportId());
                   cmd.setProperty("spreport", repRS);
               }
               CommandDispatcher.dispatch(cmd);
            }
            else
            {
                //blow up
                throw new RuntimeException("Invalid data for QueryResultsReport command.");
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doProcessAppCommands(edu.ku.brc.ui.CommandAction)
     */
    @Override
    protected void doProcessAppCommands(CommandAction cmdAction)
    {
        super.doProcessAppCommands(cmdAction);
        
        if (cmdAction.isAction(APP_RESTART_ACT))
        {
            isInitialized = false;
            this.initialize();
        }
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.CommandListener#doCommand(edu.ku.brc.specify.ui.CommandAction)
     */
    @Override
    public void doCommand(CommandAction cmdAction)
    {
        super.doCommand(cmdAction);
        
        if (cmdAction.isType(QUERY))
        {
            processQueryCommands(cmdAction);
            
        }
        else if (cmdAction.isType(TreeDefinitionEditor.TREE_DEF_EDITOR))
        {
            //all we care to know is that a treeDefintion got changed somehow 
            this.localizationOrTreeDefEdit.set(true);
        }
        else if (cmdAction.isType(SchemaLocalizerDlg.SCHEMA_LOCALIZER))
        {
            //XXX should check whether changed schema actually is the schema in use? 
            // e.g. If German schema was saved when English is in use then ignore??
            this.localizationOrTreeDefEdit.set(true);
            SwingUtilities.invokeLater(new Runnable(){
                public void run()
                {
                    if (SubPaneMgr.getInstance().getCurrentSubPane() == queryBldrPane)
                    {
                        if (queryBldrPane != null)
                        {
                            queryBldrPane.showingPane(true);
                        }
                    }                    
                }
            });
        }
    }


    //--------------------------------------------------------------
    // Inner Classes
    //--------------------------------------------------------------

    /**
     *
     * @author rods
     *
     */
    class QueryAction implements ActionListener
    {
        private String queryStr;
        private String viewSetName;
        private String viewName;

        public QueryAction(final String queryStr, final String viewSetName, final String viewName)
        {
            this.queryStr    = queryStr;
            this.viewSetName = viewSetName;
            this.viewName    = viewName;
        }
        public QueryAction(final String queryStr)
        {
            this(queryStr, null, null);
        }
        public QueryAction(final String viewSetName, final String viewName)
        {
            this(null, viewSetName, viewName);
        }
        public void actionPerformed(ActionEvent e)
        {
            if (StringUtils.isNotEmpty(queryStr))
            {
                createAndExecute(queryStr);

            } else if (StringUtils.isNotEmpty(viewSetName) && StringUtils.isNotEmpty(viewName))
            {
                //createSearchForm(viewSetName, viewName);
            }
        }
    }

    /**
     * Constructs tableTree and tableTreeHash members.
     */
    protected void bldTableTrees()
    {        
        if (tableTree == null || tableTree.get() == null)
        {
            tableTreeHash = null;
            tableTree = new SoftReference<TableTree>(readTables());
        }
        if (tableTreeHash == null || tableTreeHash.get() == null)
        {
            tableTreeHash = new SoftReference<Hashtable<String, TableTree>>(buildTableTreeHash(tableTree.get()));
        }
        localizationOrTreeDefEdit.set(false);
    }
    
    
    /**
     * @param tree
     * 
     * Clears tree of changes resulting from previous use.
     */
    protected void clearTableTree(final TableTree tree)
    {
        if (tree.getTableQRI() != null)
        {
            tree.getTableQRI().setIsInUse(false);
            for (int f = 0; f < tree.getTableQRI().getFields(); f++)
            {
                tree.getTableQRI().getField(f).setIsInUse(false);
            }
        }
        for (int k = 0; k < tree.getKids(); k++)
        {
            clearTableTree(tree.getKid(k));
        }
    }
    
    
    /**
     * @return tableTree paired with tableTreeHash
     */
    public synchronized Pair<TableTree, Hashtable<String, TableTree>> getTableTrees()
    {
        if (needToRebuildTableTree())
        {
            bldTableTrees();
        }
        else
        {
            clearTableTree(tableTree.get());
        }
        return new Pair<TableTree, Hashtable<String, TableTree>>(tableTree.get(), tableTreeHash.get());
        
    }
        
    /**
     * @return true if the table tree objects need to be rebuilt.
     */
    public synchronized boolean needToRebuildTableTree()
    {
        return tableTree == null || tableTree.get() == null || tableTreeHash == null || tableTreeHash.get() == null
            || localizationOrTreeDefEdit.get();
    }
    
    /**
     * @return TableTree defined by "querybuilder.xml" schema.
     */
    protected TableTree readTables()
    {
        TableTree treeRoot = new TableTree("root", "root", "root", null);
        try
        {
            Element root = XMLHelper.readDOMFromConfigDir("querybuilder.xml");
            List<?> tableNodes = root.selectNodes("/database/table");
            for (Object obj : tableNodes)
            {
                Element tableElement = (Element) obj;
                processForTables(tableElement, treeRoot);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return treeRoot;
    }

    /**
     * @param parent
     * @param parentTT
     * 
     * Recursively constructs tableTree defined by "querybuilder.xml" schema.
     */
    @SuppressWarnings("unchecked")
    protected void processForTables(final Element parent, final TableTree parentTT)
    {
        String tableName = XMLHelper.getAttr(parent, "name", null);
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByShortClassName(tableName);
        if (!tableInfo.isHidden() && (!UIHelper.isSecurityOn() || tableInfo.getPermissions().canView()))
        {
            String fieldName = XMLHelper.getAttr(parent, "field", null);
            if (StringUtils.isEmpty(fieldName))
            {
                fieldName = tableName.substring(0, 1).toLowerCase() + tableName.substring(1);
            }

            String abbrev = XMLHelper.getAttr(parent, "abbrev", null);
            TableTree newTreeNode = parentTT.addKid(new TableTree(tableName, fieldName, abbrev,
                    tableInfo));
            if (Treeable.class.isAssignableFrom(tableInfo.getClassObj()))
            {
                try
                {
                   SpecifyAppContextMgr mgr = (SpecifyAppContextMgr )AppContextMgr.getInstance();
                   TreeDefIface<?, ?, ?> treeDef = mgr.getTreeDefForClass((Class<? extends Treeable<?,?,?>>) tableInfo.getClassObj());
                   SortedSet<TreeDefItemIface<?, ?, ?>> defItems = new TreeSet<TreeDefItemIface<?, ?, ?>>(
                            new Comparator<TreeDefItemIface<?, ?, ?>>()
                            {
                                public int compare(TreeDefItemIface<?, ?, ?> o1,
                                                   TreeDefItemIface<?, ?, ?> o2)
                                {
                                    Integer r1 = o1.getRankId();
                                    Integer r2 = o2.getRankId();
                                    return r1.compareTo(r2);
                                }

                            });
                    defItems.addAll(treeDef.getTreeDefItems());
                    for (TreeDefItemIface<?, ?, ?> defItem : defItems)
                    {
                        if (defItem.getRankId() > 0)//skip root, just because.
                        {
                            try
                            {
                                newTreeNode.getTableQRI().addField(
                                        new TreeLevelQRI(newTreeNode.getTableQRI(), null, defItem
                                                .getRankId()));
                            }
                            catch (Exception ex)
                            {
                                // if there is no TreeDefItem for the rank then just skip it.
                                if (ex instanceof TreeLevelQRI.NoTreeDefItemException)
                                {
                                    log.error(ex);
                                }
                                // else something is really messed up
                                else
                                {
                                    throw new RuntimeException(ex);
                                }
                            }
                        }
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            }

            for (Object kidObj : parent.selectNodes("table"))
            {
                Element kidElement = (Element) kidObj;
                processForTables(kidElement, newTreeNode);
            }

            for (Object obj : parent.selectNodes("alias"))
            {
                Element kidElement = (Element) obj;
                String kidClassName = XMLHelper.getAttr(kidElement, "name", null);
                tableInfo = DBTableIdMgr.getInstance().getByShortClassName(kidClassName);
                if (!tableInfo.isHidden() && (!UIHelper.isSecurityOn() || tableInfo.getPermissions().canView()))
                {
                    tableName = XMLHelper.getAttr(kidElement, "name", null);
                    fieldName = XMLHelper.getAttr(kidElement, "field", null);
                    if (StringUtils.isEmpty(fieldName))
                    {
                        fieldName = tableName.substring(0, 1).toLowerCase()
                                + tableName.substring(1);
                    }
                    newTreeNode.addKid(new TableTree(kidClassName, fieldName, true));
                }
            }
        }
    }
    
    
    /**
     * 
     */
    protected void importQueries()
    {
        String path = AppPreferences.getLocalPrefs().get(XML_PATH_PREF, null);
        
        FileDialog fDlg = new FileDialog(((Frame)UIRegistry.getTopWindow()), "Open", FileDialog.LOAD);
        if (path != null)
        {
            fDlg.setDirectory(path);
        }
        fDlg.setVisible(true);
        
        String dirStr   = fDlg.getDirectory();
        String fileName = fDlg.getFile();
        if (StringUtils.isEmpty(dirStr) || StringUtils.isEmpty(fileName))
        {
            return;
        }
        path = dirStr + fileName;
        AppPreferences.getLocalPrefs().put(XML_PATH_PREF, path);
        
        Vector<SpQuery> queries = new Vector<SpQuery>();
        try
        {
        
            Element root = XMLHelper.readFileToDOM4J(new File(path));
            for (Object obj : root.selectNodes("/queries/query"))
            {
                Element el = (Element)obj;
                SpQuery query = new SpQuery();
                query.initialize();
                query.fromXML(el);
                query.setSpecifyUser(AppContextMgr.getInstance().getClassObject(SpecifyUser.class));
                queries.add(query);
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            return;
        }
        
        ToggleButtonChooserDlg<SpQuery> dlg = new ToggleButtonChooserDlg<SpQuery>((Frame)UIRegistry.getMostRecentWindow(),
                getResourceString("QY_IMPORT_QUERIES"),
                getResourceString("QY_SEL_QUERIES_IMP"),
                queries,
                getIcon(24),
                CustomDialog.OKCANCEL,
                ToggleButtonChooserPanel.Type.Checkbox);
        
        dlg.setAddSelectAll(true);
        dlg.setUseScrollPane(true);
        UIHelper.centerAndShow(dlg);
        List<SpQuery> queriesList = dlg.getSelectedObjects();
        if (queriesList == null || queriesList.size() == 0)
        {
            return;
        }
        
        Hashtable<String, Boolean> namesHash = new Hashtable<String, Boolean>();
        for (NavBoxItemIFace nbi : navBox.getItems())
        {
            namesHash.put(nbi.getTitle(), true);
        }
        
        for (SpQuery query : queriesList)
        {
            String origName = query.getName();
            int    cnt      = 0;
            String qName    = origName;
            while (namesHash.get(qName) != null)
            {
                cnt++;
                qName = origName + cnt;
            }
            query.setName(qName);
        }
        
        if (DataModelObjBase.save(true, queriesList))
        {
            for (SpQuery query : queriesList)
            {
                RecordSet rs = new RecordSet();
                rs.initialize();
                rs.set(query.getName(), SpQuery.getClassTableId(), RecordSet.GLOBAL);
                rs.addItem(query.getSpQueryId());
                addToNavBox(rs);
            }
            
            navBox.validate();
            navBox.repaint();  
        }
    }
    
    /**
     * 
     */
    protected void exportQueries()
    {
        Vector<String> list = new Vector<String>();
        for (NavBoxItemIFace nbi : navBox.getItems())
        {
            list.add(nbi.getTitle());
        }
        
        List<String> selectedList = null;
        if (list.size() == 1)
        {
            selectedList = list;
        } else
        {
            ToggleButtonChooserDlg<String> dlg = new ToggleButtonChooserDlg<String>((Frame)UIRegistry.getMostRecentWindow(),
                    getResourceString("QY_EXPORT_QUERIES"),
                    getResourceString("QY_SEL_QUERIES_EXP"),
                    list,
                    getIcon(24),
                    CustomDialog.OKCANCEL,
                    ToggleButtonChooserPanel.Type.Checkbox);
            dlg.setAddSelectAll(true);
            dlg.setUseScrollPane(true);
            UIHelper.centerAndShow(dlg);
            selectedList = dlg.getSelectedObjects();
        }
        
        String path = AppPreferences.getLocalPrefs().get(XML_PATH_PREF, null);
        
        FileDialog fDlg = new FileDialog(((Frame)UIRegistry.getTopWindow()), "Save", FileDialog.SAVE);
        if (path != null)
        {
            fDlg.setDirectory(path);
        }
        fDlg.setVisible(true);
        
        String dirStr   = fDlg.getDirectory();
        String fileName = fDlg.getFile();
        if (StringUtils.isEmpty(dirStr) || StringUtils.isEmpty(fileName))
        {
            return;
        }
        
        if (StringUtils.isEmpty(FilenameUtils.getExtension(fileName)))
        {
            fileName += ".xml";
        }
        path = dirStr + fileName;
        AppPreferences.getLocalPrefs().put(XML_PATH_PREF, path);
        
        Hashtable<String, Boolean> hash = new Hashtable<String, Boolean>();
        for (String qTitle : selectedList)
        {
            hash.put(qTitle, true);
        }
        
        Vector<SpQuery> queries = new Vector<SpQuery>();
        
        // Persist out to database
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            for (NavBoxItemIFace nbi : navBox.getItems())
            {
                if (hash.get(nbi.getTitle()) != null)
                {
                    RecordSetIFace rs  = (RecordSetIFace)nbi.getData();
                    if (rs != null)
                    {
                        SpQuery query = session.get(SpQuery.class, rs.getOnlyItem().getRecordId());
                        queries.add(query);
                    }
                }
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("<queries>\n");
            for (SpQuery q : queries)
            {
                q.toXML(sb);
            }
            sb.append("</queries>");
            FileUtils.writeStringToFile(new File(path), sb.toString());
            
        } catch (Exception ex)
        {
            // XXX Error dialog
            ex.printStackTrace();
        }
        finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
    
    

    /**
     * @param treeRoot
     * @return a Hashtable of top-level tables in tableTree.
     */
    protected Hashtable<String, TableTree> buildTableTreeHash(final TableTree treeRoot)
    {
        Hashtable<String, TableTree> result = new Hashtable<String, TableTree>();
        try
        {
            for (int t = 0; t < treeRoot.getKids(); t++)
            {
                TableTree tt = treeRoot.getKid(t);
                result.put(tt.getName(), tt);
                log.debug("Adding[" + tt.getName() + "] to hash");
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return result;
    }
    
    /**
     * @author timbo
     *
     * @code_status Alpha
     *
     *  Launches TableTree build with status message and progress bar then opens a Query
     *  in the finished() method. 
     */
    protected class OpenQueryWorker extends SwingWorker
    {
        @Override
        public Object construct()
        {
            UIRegistry.displayStatusBarText(UIRegistry.getResourceString("QB_LOADING"));
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    UIRegistry.getStatusBar().setIndeterminate("QUERYTASK", true);
                }
            });
            getTableTrees();
            UIRegistry.displayStatusBarText("");
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    UIRegistry.getStatusBar().setProgressDone("QUERYTASK");
                }
            });
            return null;
        }
    }
    
    /**
     * Reload the current query.
     * 
     * This method is not currently used.
     * 
     * With adjusments for saving and navBtn updating and for case of not-yet-saved current query it
     * could be used to automatically refresh query after Localization or TreeDef edits.
     */
    public void reloadQuery()
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                new EditQueryWorker(queryBldrPane.getQuery().getId(), queryBldrPane.getQueryNavBtn()).start();
            }
        });
    }
    protected class NewQueryWorker extends OpenQueryWorker
    {
        protected final DBTableInfo tableInfo;
        
        public NewQueryWorker(final DBTableInfo tableInfo)
        {
            super();
            this.tableInfo = tableInfo;
        }

        @Override
        public void finished()
        {
            super.finished();
            createNewQuery(tableInfo);
        }
    }
    
    protected class EditQueryWorker extends OpenQueryWorker
    {
        protected final Integer queryId;
        protected final RolloverCommand queryNavBtn;
        
        public EditQueryWorker(final Integer queryId, final RolloverCommand queryNavBtn)
        {
            super();
            this.queryId = queryId;
            this.queryNavBtn = queryNavBtn;
        }

        @Override
        public void finished()
        {
            super.finished();
            if (queryBldrPane == null || queryBldrPane.aboutToShutdown())
            {
                editQuery(queryId);
                queryNavBtn.setEnabled(false);
                queryBldrPane.setQueryNavBtn(queryNavBtn);
            }
        }
        
    }
    
    protected class EditOtherQueryWorker extends OpenQueryWorker
    {
        protected final Integer queryId;
        
        public EditOtherQueryWorker(final Integer queryId)
        {
            super();
            this.queryId = queryId;
        }
        
        @Override
        public void finished()
        {
            super.finished();
            editQuery(queryId);
        }

    }
}
