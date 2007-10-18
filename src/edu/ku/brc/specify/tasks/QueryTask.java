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
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.Color;
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
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.DroppableNavBox;
import edu.ku.brc.af.core.MenuItemDesc;
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
import edu.ku.brc.ui.DataFlavorTableExt;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.FormHelper;

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

    /**
     * Default Constructor
     *
     */
    public QueryTask()
    {
        super(QUERY, getResourceString(QUERY));

        /*
        // XXX Localize
        // Temporary
        NavBox navBox = new NavBox(name);
        navBox.add(NavBox.createBtn(getResourceString("New"), name, IconManager.IconSize.Std16, new QueryAction("select * from picklist where picklist_id = 3")));
        navBox.add(NavBox.createBtn(getResourceString("Advanced"), name, IconManager.IconSize.Std16));
        navBox.add(NavBox.createBtn(getResourceString("Specimen"), name, IconManager.IconSize.Std16));
        navBox.add(NavBox.createBtn(getResourceString("Taxonomic"), name, IconManager.IconSize.Std16));
        navBox.add(NavBox.createBtn(getResourceString("Geographic"), name, IconManager.IconSize.Std16));
        navBox.add(NavBox.createBtn(getResourceString("CollectionObject"), name, IconManager.IconSize.Std16, new QueryAction(null, "Collection Object Search")));
        navBoxes.add(navBox);

        navBox = new NavBox(getResourceString("Saved_Searches"));
        navBox.add(NavBox.createBtn("Species Counts", name, IconManager.IconSize.Std16, new QueryAction("select SpeciesName,count(tx) as SpeciesCount from (select determination.TaxonNameId, taxonname.TaxonNameID as tx, taxonname.TaxonName as SpeciesName from taxonname,determination where determination.TaxonNameId = taxonname.taxonnameid) as newTable group by tx order by SpeciesCount DESC;")));
        navBox.add(NavBox.createBtn("Picklist", name, IconManager.IconSize.Std16));
        navBoxes.add(navBox);
        */
    }

    /**
     * CReates pane and executes a query
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
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false
            loadQueries();
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
     * Adds a Query to the Left Pane NavBox
     * @param query the Query to be added
     * @return the nav box
     */
    protected NavBoxItemIFace addToNavBox(final SpQuery query)
    {
        final RolloverCommand roc = (RolloverCommand)makeDnDNavBtn(navBox, query.getName(), "Search", null, 
                                                                   new CommandAction(QUERY, DELETE_CMD_ACT, query), 
                                                                   true, true);// true means make it draggable
        roc.setData(query.getSpQueryId());
        roc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                editQuery((Integer)((RolloverCommand)e.getSource()).getData());
            }
            
        });
        
        NavBoxItemIFace nbi = (NavBoxItemIFace)roc;
        
        DBTableInfo tblInfo = DBTableIdMgr.getInstance().getInfoById(query.getContextTableId());
        if (tblInfo != null)
        {
            ImageIcon rsIcon = tblInfo.getIcon(IconManager.IconSize.Std16);
            if (rsIcon != null)
            {
                nbi.setIcon(rsIcon);
            }
        }
        
        roc.addDragDataFlavor(new DataFlavorTableExt(QueryTask.class, QUERY, query.getContextTableId()));
        
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
            addToNavBox(query);
        }
        navBoxes.add(navBox);
        session.close();
    }
    
    /**
     * @param queryId
     */
    protected void editQuery(Integer queryId)
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        Object dataObj = session.getData(SpQuery.class, "spQueryId", queryId, DataProviderSessionIFace.CompareType.Equals);
        if (dataObj != null)
        {
            editQuery((SpQuery)dataObj);
        }
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
    public void saveNewQuery(final SpQuery query)
    {
        addToNavBox(query);
        
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

        // XXX CommandDispatcher.dispatch(new CommandAction("Labels", "NewRecordSet", nbi));
    }
    
    /**
     * Delete a record set
     * @param rs the recordSet to be deleted
     */
    protected void deleteQuery(final SpQuery query)
    {
        // delete from database
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        session.attach(query);
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
     * @param recordSet the record set that is "owned" by some UI object that needs to be deleted (used for seconday lookup
     */
    protected void deleteQueryFromUI(final NavBoxItemIFace boxItem, final SpQuery query)
    {
        deleteDnDBtn(navBox, boxItem != null ? boxItem : getBoxByTitle(navBox, query.getName()));
    }
    
    /**
     * Processes all Commands of type QUERY.
     * @param cmdAction the command to be processed
     */
    protected void processQueryCommands(final CommandAction cmdAction)
    {
        if (cmdAction.isAction(SAVE_QUERY))
        {
            Object data = cmdAction.getData();
            if (data instanceof SpQuery)
            {
                // If there is only one item in the RecordSet then the User will most likely want it named the same
                // as the "identity" of the data object. So this goes and gets the Identity name and
                // pre-sets the name in the dialog.
                String intialName = "";
                SpQuery query = (SpQuery)cmdAction.getData();
                if (query.getFields().size() == 1) // Insurance (shouldn't happen)
                {
                    intialName = query.getName();
                    
                    DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                    List<?> queriesList = session.getDataList(SpQuery.class, "name", query.getName());
                    session.close();
                    
                    if (queriesList != null && queriesList.size() > 0)
                    {
                        String rsName  = JOptionPane.showInputDialog(UIRegistry.get(UIRegistry.FRAME), getResourceString("AskForRSName"), intialName);
                        if (isNotEmpty(rsName))
                        {
                            SpQuery rs = (SpQuery)data;
                            rs.setName(rsName);
                            rs.setModifiedByAgent(SpecifyUser.getCurrentUser().getAgent());
                            saveNewQuery(rs);
                        }
                    }
                }
            }
        } else if (cmdAction.isAction(DELETE_CMD_ACT) && cmdAction.getData() instanceof RecordSet)
        {
            SpQuery recordSet = (SpQuery)cmdAction.getData();
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
