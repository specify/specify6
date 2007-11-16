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
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.tasks.subpane.SQLQueryPane;
import edu.ku.brc.specify.tasks.subpane.qb.QueryBldrPane;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.DataFlavorTableExt;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.RolloverCommand;
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
    
    public static final DataFlavor QUERY_FLAVOR = new DataFlavor(QueryTask.class, QUERY);
    
    protected QueryBldrPane              queryBldrPane    = null;

    protected Vector<ToolBarDropDownBtn> tbList           = new Vector<ToolBarDropDownBtn>();
    protected Vector<JComponent>         menus            = new Vector<JComponent>();
    protected Vector<NavBoxIFace>        extendedNavBoxes = new Vector<NavBoxIFace>();
    protected DroppableNavBox            navBox           = null;
    protected NavBox                     actionNavBox     = null;
    
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
    
    
    /**
     * (Could be refactored with WorkBench Task)
     * @param workbench
     * @param title
     * @return
     */
    protected boolean fillInQueryNameAndAttrs(final SpQuery query, 
                                              final String  queryName, 
                                              final boolean skipFirstCheck)
    {
        boolean skip = skipFirstCheck;
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        
        try
        {
            String newQueryName = queryName;
            
            boolean   alwaysAsk   = true;
            SpQuery   fndQuery    = null;
            boolean   shouldCheck = false;
            do
            {
                if (StringUtils.isEmpty(newQueryName))
                {
                    alwaysAsk = true;
                    
                } else
                {
                    fndQuery = session.getData(SpQuery.class, "name", newQueryName, DataProviderSessionIFace.CompareType.Equals);
                    if (fndQuery != null && !skip)
                    {
                        UIRegistry.getStatusBar().setErrorMessage(String.format(getResourceString("WB_DATASET_EXISTS"), new Object[] { newQueryName}));
                        query.setName("");
                    }
                    skip = false;
                }
                
                String oldName = query.getName();
                if ((fndQuery != null || (StringUtils.isNotEmpty(newQueryName) && newQueryName.length() > 64)) || alwaysAsk)
                {
                    alwaysAsk = false;
                    
                    // We found the same name and it must be unique
                    if (QueryTask.askUserForInfo("Query", getResourceString("QB_DATASET_INFO"), query))
                    {
                        newQueryName = query.getName();
                        // This Part here needs to be moved into an <enablerule/>
                        if (StringUtils.isNotEmpty(newQueryName) && newQueryName.length() > 64)
                        {
                            UIRegistry.getStatusBar().setErrorMessage(getResourceString("WB_NAME_TOO_LONG"));
                        }
                        fndQuery = query;
                    } else
                    {
                        UIRegistry.getStatusBar().setText("");
                        return false;
                    }
                }
                
                shouldCheck = oldName == null || !oldName.equals(newQueryName);
                
            } while (shouldCheck);
            
        } catch (Exception ex)
        {
            log.error(ex);
            
        } finally
        {
            session.close();    
        }
        UIRegistry.getStatusBar().setText("");
        return true;
    }

    
    /**
     * Creates a new Query Data Object.
     * @param wbName
     * @return
     */
    protected SpQuery createNewQueryDataObj(final String wbName)
    {
        SpQuery query = new SpQuery();
        query.initialize();
        query.setSpecifyUser(SpecifyUser.getCurrentUser());
        
        if (StringUtils.isNotEmpty(wbName))
        {
            query.setName(wbName);
        }
        
        if (fillInQueryNameAndAttrs(query, wbName, false))
        {
            return query;
        }

        return null;
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
        actionNavBox = new NavBox(getResourceString("Actions"));
        actionNavBox.add(NavBox.createBtnWithTT(getResourceString("QB_CREATE_NEWQUERY"), name, getResourceString("QB_CREATE_NEWQUERY_TT"), IconManager.IconSize.Std16, new ActionListener() {
            public void actionPerformed(ActionEvent arg0)
            {
                createNewQuery();
            }
        }
        ));
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
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
                editQuery((Integer)recordSet.getOnlyItem().getRecordId());
                queryNavBtn.setEnabled(false);
                queryBldrPane.setQueryNavBtn(queryNavBtn);
            }
            
        });
        
        NavBoxItemIFace nbi = (NavBoxItemIFace)roc;
        
        DBTableInfo tblInfo = DBTableIdMgr.getInstance().getInfoById(recordSet.getTableId());
        if (tblInfo != null)
        {
            ImageIcon rsIcon = tblInfo.getIcon(IconManager.IconSize.Std16);
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
        String sqlStr = "From SpQuery as sq Inner Join sq.specifyUser as user where user.specifyUserId = "+SpecifyUser.getCurrentUser().getSpecifyUserId();

        DataProviderSessionIFace session    = DataProviderFactory.getInstance().createSession();
        List<?> queries = session.getDataList(sqlStr);

        navBox = new DroppableNavBox(getResourceString("Queries"), QUERY_FLAVOR, QUERY, SAVE_QUERY);

        for (Iterator<?> iter=queries.iterator();iter.hasNext();)
        {
            Object[] obj = (Object[])iter.next();
            SpQuery query = (SpQuery)obj[0];
            RecordSet rs = new RecordSet(query.getName(), SpQuery.getClassTableId());
            rs.addItem(query.getSpQueryId());
            addToNavBox(rs);
        }
        session.close();
    }
    
    /**
     * 
     */
    protected void createNewQuery()
    {
        SpQuery query = createNewQueryDataObj(null);
        if (query != null)
        {
            editQuery(query);
        }
    }
    
    /**
     * @param queryId
     */
    protected void editQuery(Integer queryId)
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        Object                   dataObj = session.getData(SpQuery.class, "spQueryId", queryId, DataProviderSessionIFace.CompareType.Equals);
        if (dataObj != null)
        {
            editQuery((SpQuery)dataObj);
        }
        session.close();
    }
    
    /**
     * @param query
     */
    protected void editQuery(final SpQuery query)
    {
        if (queryBldrPane != null)
        {
            queryBldrPane.setQuery(query);
            
        } else
        {
            queryBldrPane = new QueryBldrPane(name, this, query);
        }
        
        if (starterPane != null)
        {
            SubPaneMgr.getInstance().replacePane(starterPane, queryBldrPane);
            starterPane = null;
        }
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#getNavBoxes()
     */
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
        RecordSet rs = new RecordSet(query.getName(), SpQuery.getClassTableId());
        rs.addItem(query.getSpQueryId());
        
        RolloverCommand roc = (RolloverCommand)addToNavBox(rs);
        roc.setEnabled(enabled);
        
        query.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        query.setSpecifyUser(SpecifyUser.getCurrentUser());
        
        persistRecordSet(query);
        

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
    protected void deleteQuery(final RecordSet rs)
    {
        // delete from database
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        
        SpQuery query = session.get(SpQuery.class, rs.getOnlyItem().getRecordId());
        try
        {
            session.beginTransaction();
            session.delete(query);
            session.commit();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.error(ex);
        }
        session.close();
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
            deleteQuery(recordSet);
            deleteQueryFromUI(null, recordSet);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.CommandListener#doCommand(edu.ku.brc.specify.ui.CommandAction)
     */
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
