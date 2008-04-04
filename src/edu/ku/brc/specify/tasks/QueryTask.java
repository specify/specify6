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

import java.awt.Color;
import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.core.DroppableNavBox;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.NavBoxMgr;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpReport;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.tasks.subpane.ESResultsTablePanel;
import edu.ku.brc.specify.tasks.subpane.SQLQueryPane;
import edu.ku.brc.specify.tasks.subpane.qb.QBLiveJRDataSource;
import edu.ku.brc.specify.tasks.subpane.qb.QBResultReportServiceCmdData;
import edu.ku.brc.specify.tasks.subpane.qb.QueryBldrPane;
import edu.ku.brc.specify.ui.db.ResultSetTableModel;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.DataFlavorTableExt;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.ToggleButtonChooserPanel;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.ui.forms.FormHelper;
import edu.ku.brc.ui.forms.MultiView;

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
    public static final String QUERY      = "Query";
    public static final String SAVE_QUERY = "Save";
    public static final String QUERY_RESULTS_REPORT = "QueryResultsReport";
    
    public static final DataFlavor QUERY_FLAVOR = new DataFlavor(QueryTask.class, QUERY);
    
    protected QueryBldrPane              queryBldrPane    = null;

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
        CommandDispatcher.register(APP_CMD_TYPE, this);
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
    
    
//    /**
//     * (Could be refactored with WorkBench Task)
//     * @param workbench
//     * @param title
//     * @return
//     */
//    protected boolean fillInQueryNameAndAttrs(final SpQuery query, 
//                                              final String  queryName, 
//                                              final boolean skipFirstCheck,
//                                              final DBTableInfo tableInfo)
//    {
//        boolean skip = skipFirstCheck;
//        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
//        
//        try
//        {
//            String newQueryName = queryName;
//            
//            boolean   alwaysAsk   = false;
//            SpQuery   fndQuery    = null;
//            boolean   shouldCheck = false;
//            do
//            {
//                if (StringUtils.isEmpty(newQueryName))
//                {
//                    alwaysAsk = true;
//                    
//                } else
//                {
//                    fndQuery = session.getData(SpQuery.class, "name", newQueryName, DataProviderSessionIFace.CompareType.Equals);
//                    if (fndQuery != null && !skip)
//                    {
//                        UIRegistry.getStatusBar().setErrorMessage(String.format(getResourceString("QB_QUERY_EXISTS"), newQueryName));
//                        query.setName("");
//                    }
//                    skip = false;
//                }
//                
//                String oldName = query.getName();
//                if ((fndQuery != null || (StringUtils.isNotEmpty(newQueryName) && newQueryName.length() > 64) && query.isNamed()) || alwaysAsk)
//                {
//                    alwaysAsk = false;
//                    
//                    // We found the same name and it must be unique
//                    if (QueryTask.askUserForInfo("Query", getResourceString("QB_DATASET_INFO"), query))
//                    {
//                        newQueryName = query.getName();
//                        // This Part here needs to be moved into an <enablerule/>
//                        if (StringUtils.isNotEmpty(newQueryName) && newQueryName.length() > 64)
//                        {
//                            UIRegistry.getStatusBar().setErrorMessage(getResourceString("WB_NAME_TOO_LONG"));
//                        }
//                        fndQuery = query;
//                    } else
//                    {
//                        UIRegistry.getStatusBar().setText("");
//                        return false;
//                    }
//                }
//                
//                shouldCheck = oldName == null || !oldName.equals(newQueryName);
//                
//            } while (shouldCheck);
//            
//        } catch (Exception ex)
//        {
//            log.error(ex);
//            
//        } finally
//        {
//            session.close();    
//        }
//        UIRegistry.getStatusBar().setText("");
//        
//        query.setContextTableId((short)tableInfo.getTableId());
//        
//        return true;
//    }

    
    /**
     * Creates a new Query Data Object.
     * @param wbName
     * @return
     */
    protected SpQuery createNewQueryDataObj(final DBTableInfo tableInfo)
    {
        SpQuery query = new SpQuery();
        query.initialize();
        query.setSpecifyUser(SpecifyUser.getCurrentUser());
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
        PanelBuilder    display = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "f:p:g,p,150px,f:p:g"));
        CellConstraints cc      = new CellConstraints();

        display.add(new JLabel(IconManager.getIcon("SpecifySplash")), cc.xy(2, 2));
        
        if (UIHelper.getOSType() != UIHelper.OSTYPE.MacOSX)
        {
            display.getPanel().setBackground(Color.WHITE);
        }
        
        return starterPane = new SimpleDescPane(title, this, display.getPanel());
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
        actionNavBox = new NavBox(getResourceString("QB_NEW_QUERY"));
        addNewQCreators();
    }
    
    /**
     * Reads the Query Lists from the database.
     */
    protected void readOrgLists()
    {
        freqQueries  = readResourceForList("QueryFreqList");
        extraQueries = readResourceForList("QueryExtraList");
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
                    RecordSet rs = new RecordSet(query.getName(), SpQuery.getClassTableId());
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
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            Integer count = (Integer)session.getData("SELECT count(spQueryId) From SpQuery as sq Inner Join sq.specifyUser as user where sq.isFavorite = false AND user.specifyUserId = "+SpecifyUser.getCurrentUser().getSpecifyUserId());
            if (count != null && count > 0)
            {
                NavBoxItemIFace nbi = NavBox.createBtnWithTT(getResourceString("QY_OTHER_QUERIES"),
                        name, 
                        getResourceString("QY_OTHER_QUERIES_TT"), 
                        IconManager.STD_ICON_SIZE, new ActionListener() 
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#isConfigurable()
     */
    @Override
    public boolean isConfigurable()
    {
        return true;
    }
    
    /* (non-Javadoc)
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
            NavBoxItemIFace nbi = NavBox.createBtnWithTT(getResourceString("QY_EXTRA_QUERIES"),
                                                         name, 
                                                         getResourceString("QY_EXTRA_QUERIES_TT"), 
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
                    "QY_EXTRA_QUERIES", 
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
            rows    = session.getDataList("FROM SpQuery as sq Inner Join sq.specifyUser as user where sq.isFavorite = false AND user.specifyUserId = "+SpecifyUser.getCurrentUser().getSpecifyUserId() + " ORDER BY sq.name");
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
            Object[] row = (Object[])rows.iterator().next();
            editQuery(((SpQuery)row[0]).getId().intValue());
            
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
                editQuery(dlg.getSelectedObject().getId().intValue());
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
        final DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByShortClassName(shortClassName);
        actionNavBox.add(NavBox.createBtnWithTT(String.format(getResourceString("QB_CREATE_NEWQUERY"), 
                tableInfo.getTitle()), 
                name, 
                getResourceString("QB_CREATE_NEWQUERY_TT"), 
                IconManager.STD_ICON_SIZE, new ActionListener() 
        {
            public void actionPerformed(ActionEvent e)
            {
                createNewQuery(tableInfo);
            }
        }
        ));
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
                stdQueries.add(sName);
                
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
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();

        String label    = getResourceString(name);
        String iconName = name;
        String hint     = getResourceString("search_hint");
        ToolBarDropDownBtn btn = createToolbarButton(label,iconName,hint,menus);
        if (tbList.size() == 0)
        {
            tbList.add(btn);
        }
        list.add(new ToolBarItemDesc(btn));

        return list;

    }
    
    /**
     * Adds a Query to the Left Pane NavBox (Refactor this with Workbench)
     * @param query the Query to be added
     * @return the nav box
     */
    protected NavBoxItemIFace addToNavBox(final RecordSet recordSet)
    {
        final RolloverCommand roc = (RolloverCommand)makeDnDNavBtn(navBox, recordSet.getName(), "Query", null, 
                                                                   new CommandAction(QUERY, DELETE_CMD_ACT, recordSet), 
                                                                   true, true);// true means make it draggable
        roc.setData(recordSet);
        roc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                RolloverCommand queryNavBtn = (RolloverCommand)e.getSource();
                if (queryBldrPane == null || queryBldrPane.aboutToShutdown())
                {
                    editQuery(recordSet.getOnlyItem().getRecordId());
                    queryNavBtn.setEnabled(false);
                    queryBldrPane.setQueryNavBtn(queryNavBtn);
                }
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
        
        return nbi;
    }
    
    /**
     * Loads the Queries from the Database
     */
    protected void loadQueries()
    {
        String sqlStr = "From SpQuery as sq Inner Join sq.specifyUser as user where sq.isFavorite = true AND user.specifyUserId = "+SpecifyUser.getCurrentUser().getSpecifyUserId() + " ORDER BY ordinal";

        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            List<?> queries = session.getDataList(sqlStr);

            navBox = new DroppableNavBox(getResourceString("Queries"), QUERY_FLAVOR, QUERY, SAVE_QUERY);

            for (Iterator<?> iter = queries.iterator(); iter.hasNext();)
            {
                Object[] obj = (Object[]) iter.next();
                SpQuery query = (SpQuery) obj[0];
                RecordSet rs = new RecordSet(query.getName(), SpQuery.getClassTableId());
                rs.addItem(query.getSpQueryId());
                addToNavBox(rs);
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
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#getMenuItems()
     */
    @Override
    public List<MenuItemDesc> getMenuItems()
    {
        Vector<MenuItemDesc> list = new Vector<MenuItemDesc>();

        return list;

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
     * @param recordSet the RecordSet
     */
    protected void persistRecordSet(final SpQuery query)
    {
        // TODO Add StaleObject Code from FormView
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            session.beginTransaction();
            session.saveOrUpdate(query);
            session.commit();
            session.flush();
            
            FormHelper.updateLastEdittedInfo(query);

            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.error(ex);
        }
        session.close();
    }
    /**
     * Save a record set.
     * @param recordSet the rs to be saved
     */
    public RolloverCommand saveNewQuery(final SpQuery query, final boolean enabled)
    {        
        query.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        query.setSpecifyUser(SpecifyUser.getCurrentUser());
        if (query.getIsFavorite() == null)
        {
            query.setIsFavorite(true);
        }
        
        persistRecordSet(query);
        
        RecordSet rs = new RecordSet(query.getName(), SpQuery.getClassTableId());
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
                JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), 
                        String.format(UIRegistry.getResourceString("QB_UNDELETABLE.REPS"), query.getName()), 
                        UIRegistry.getResourceString("QB_UNDELETABLE_TITLE"), 
                        JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
            session.beginTransaction();
            transOpen = true;
            session.delete(query);
            session.commit();
            transOpen = false;
            return true;
            
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
     * @param recordSet the record set that is "owned" by some UI object that needs to be deleted (used for secondary lookup
     */
    protected void deleteQueryFromUI(final NavBoxItemIFace boxItem, final RecordSet rs)
    {
        deleteDnDBtn(navBox, boxItem != null ? boxItem : getBoxByTitle(navBox, rs.getName()));
    }
    
       /**
     * Processes all Commands of type QUERY.
     * @param cmdAction the command to be processed
     */
    protected void processQueryCommands(final CommandAction cmdAction)
    {
        if (cmdAction.isAction(DELETE_CMD_ACT) && cmdAction.getData() instanceof RecordSet)
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
               JTable dataTbl = ((ESResultsTablePanel)srvData.getData()).getTable();
               ResultSetTableModel rsm = (ResultSetTableModel)dataTbl.getModel();
               QBLiveJRDataSource src = new QBLiveJRDataSource(rsm, srvData.getInfo().getVisibleCaptionInfo());
               final CommandAction cmd = new CommandAction(ReportsBaseTask.REPORTS,
                       ReportsBaseTask.PRINT_REPORT, src);
               cmd.setProperty("title", srvData.getInfo().getTitle());
               String fileName = null;
               List<SpReport> reps = new Vector<SpReport>(srvData.getInfo().getReports());
               if (reps.size() == 0)
               {
                   log.error("no reports for query. Should't have gotten here.");
               }
               else if (reps.size() == 1)
               {
                   fileName = reps.get(0).getName();// + ".jrxml";
               }
               else
               {
                   ChooseFromListDlg<SpReport> dlg = new ChooseFromListDlg<SpReport>((Frame) UIRegistry
                           .getTopWindow(), UIRegistry.getResourceString("REP_CHOOSE_SP_REPORT"),
                           reps);
                   dlg.setVisible(true);
                   if (dlg.isCancelled()) { return; }
                   fileName = dlg.getSelectedObject().getName();// + ".jrxml";
                   dlg.dispose();
               }
               if (fileName == null) { return; }

               cmd.setProperty("file", fileName);
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
     * @see edu.ku.brc.specify.ui.CommandListener#doCommand(edu.ku.brc.specify.ui.CommandAction)
     */
    @Override
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isType(QUERY))
        {
            processQueryCommands(cmdAction);
            
        } else if (cmdAction.isType(APP_CMD_TYPE) && cmdAction.isAction(APP_RESTART_ACT))
        {
            isInitialized = false;
            this.initialize();
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


}
