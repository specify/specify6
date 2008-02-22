/*
 * Copyright (C) 2007 The University of Kansas
 * 
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import static edu.ku.brc.ui.UIHelper.createIconBtn;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.NavBoxLayoutManager;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.expresssearch.ERTICaptionInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBRelationshipInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpQueryField;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.tasks.QueryTask;
import edu.ku.brc.specify.tasks.ReportsBaseTask;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 * 
 * @code_status Alpha
 * 
 * Feb 23, 2007
 * 
 */
public class QueryBldrPane extends BaseSubPane
{
    protected static final Logger                            log              = Logger
                                                                                      .getLogger(QueryBldrPane.class);

    protected JList                                          tableList;
    protected Vector<QueryFieldPanel>                        queryFieldItems  = new Vector<QueryFieldPanel>();
    protected QueryFieldPanel                                selectedQFP = null; 
    protected int                                            currentInx       = -1;
    protected JPanel                                         queryFieldsPanel;
    protected JScrollPane                                    queryFieldsScroll;
    
    protected SpQuery                                        query            = null;

    protected JButton                                        addBtn;

    protected ImageIcon                                      blankIcon        = IconManager
                                                                                      .getIcon(
                                                                                              "BlankIcon",
                                                                                              IconManager.IconSize.Std24);

    protected String                                         columnDefStr     = null;

    protected JPanel                                         listBoxPanel;
    protected Vector<JList>                                  listBoxList      = new Vector<JList>();
    protected Vector<TableTree>                              nodeList         = new Vector<TableTree>();
    protected JScrollPane                                    scrollPane;
    protected Vector<JScrollPane>                            spList           = new Vector<JScrollPane>();
    protected JPanel                                         contextPanel;
    protected JButton                                        saveBtn;

    protected Hashtable<String, Boolean>                     fieldsToSkipHash = new Hashtable<String, Boolean>();
    protected QryListRenderer                                qryRenderer      = new QryListRenderer(
                                                                                      IconManager.IconSize.Std16);
    protected int                                            listCellHeight;

    protected TableTree                                      tableTree;
    protected Hashtable<String, TableTree>                   tableTreeHash;    
    protected TableAbbreviator                               tableAbbreviator;
    protected boolean                                        processingLists  = false;

    protected RolloverCommand                                queryNavBtn      = null;

    // Reordering
    protected JButton                       orderUpBtn  = null;
    protected JButton                       orderDwnBtn = null;
    protected boolean                       doOrdering  = false;

    /**
     * Constructor.
     * 
     * @param name name of subpanel
     * @param task the owning task
     */
    public QueryBldrPane(final String name, final Taskable task, final SpQuery query)
    {
        super(name, task);

        this.query = query;

        String[] skipItems = { "TimestampCreated", "LastEditedBy", "TimestampModified" };
        for (String nameStr : skipItems)
        {
            fieldsToSkipHash.put(nameStr, true);
        }

        tableAbbreviator = new TableAbbreviator();
        
        tableTree = readTables();

        createUI();

        setQueryIntoUI();
    }

    /**
     * 
     */
    protected void createUI()
    {
        removeAll();

        saveBtn = new JButton("Save");
        saveBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (saveQuery())
                {
                    saveBtn.setEnabled(false);
                }
            }
        });

        listBoxPanel = new JPanel(new HorzLayoutManager(2, 2));

        Vector<TableQRI> list = new Vector<TableQRI>();
        for (int k=0; k<tableTree.getKids(); k++)
        {
            list.add(tableTree.getKid(k).getTableQRI());
        }

        Collections.sort(list);
        DefaultListModel model = new DefaultListModel();
        for (TableQRI qri : list)
        {
            model.addElement(qri);
        }

        tableList = new JList(model);
        QryListRenderer qr = new QryListRenderer(IconManager.IconSize.Std16);
        qr.setDisplayKidIndicator(false);
        tableList.setCellRenderer(qr);

        JScrollPane spt = new JScrollPane(tableList,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        Dimension pSize = spt.getPreferredSize();
        pSize.height = 200;
        spt.setPreferredSize(pSize);

        JPanel topPanel = new JPanel(new BorderLayout());

        scrollPane = new JScrollPane(listBoxPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        tableList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    nodeList.clear();

                    int inx = tableList.getSelectedIndex();
                    if (inx > -1)
                    {
                        fillNextList(tableList);

                        TableTree node = tableTree.getKid(inx);
                        query.setContextTableId((short) node.getTableInfo().getTableId());
                        query.setContextName(node.getName());

                    }
                    else
                    {
                        listBoxPanel.removeAll();
                    }
                }
            }
        });

        addBtn = new JButton(IconManager.getImage("PlusSign", IconManager.IconSize.Std16));
        addBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                FieldQRI fieldQRI = (FieldQRI) listBoxList.get(currentInx).getSelectedValue();

                SpQueryField qf = new SpQueryField();
                qf.initialize();
                qf.setFieldName(fieldQRI.getFieldName());
                query.addReference(qf, "fields");
                addQueryFieldItem(fieldQRI, qf);
            }
        });

        contextPanel = new JPanel(new BorderLayout());
        contextPanel.add(new JLabel("Search Context", SwingConstants.CENTER), BorderLayout.NORTH);
        contextPanel.add(spt, BorderLayout.CENTER);
        contextPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        JPanel schemaPanel = new JPanel(new BorderLayout());
        schemaPanel.add(new JLabel("Search Fields"), BorderLayout.NORTH);
        schemaPanel.add(scrollPane, BorderLayout.CENTER);

        topPanel.add(contextPanel, BorderLayout.WEST);
        topPanel.add(schemaPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        queryFieldsPanel = new JPanel();
        queryFieldsPanel.setLayout(new NavBoxLayoutManager(0, 2));
        queryFieldsScroll = new JScrollPane(queryFieldsPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        queryFieldsScroll.setBorder(null);
        add(queryFieldsScroll);

        JButton searchBtn = new JButton("Search");
        final JCheckBox distinctChk = new JCheckBox("distinct", false);
        PanelBuilder outer = new PanelBuilder(new FormLayout("f:p:g,p", "p"));
        PanelBuilder builder = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g,f:p:g,f:p:g", "p"));
        CellConstraints cc = new CellConstraints();
        builder.add(searchBtn, cc.xy(2, 1));
        builder.add(distinctChk, cc.xy(3, 1));
        final JPanel mover = bldMoverPanel();
        builder.add(mover, cc.xy(4, 1));

        outer.add(builder.getPanel(), cc.xy(1, 1));
        outer.add(saveBtn, cc.xy(2, 1));
        add(outer.getPanel(), BorderLayout.SOUTH);

        searchBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                doSearch((TableQRI)tableList.getSelectedValue(), distinctChk.isSelected());
            }
        });

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    }

    /**
     * @param fieldName
     * @return fieldName with lower-cased first character.
     */
    public static String fixFieldName(final String fieldName)
    {
        return fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
    }

    /**
     * 
     */
    protected void setQueryIntoUI()
    {
        if (!SwingUtilities.isEventDispatchThread()) { throw new RuntimeException(
                "Method called from invalid thread."); }
        queryFieldsPanel.removeAll();
        queryFieldItems.clear();
        queryFieldsPanel.validate();
        saveBtn.setEnabled(false);
        columnDefStr = null;
        tableList.clearSelection();
        contextPanel.setVisible(query == null);
        tableList.setSelectedIndex(-1);
        if (query != null)
        {
            Short tblId = query.getContextTableId();
            if (tblId != null)
            {
                for (int i = 0; i < tableList.getModel().getSize(); i++)
                {
                    TableQRI qri = (TableQRI) tableList.getModel().getElementAt(i);
                    if (qri.getTableInfo().getTableId() == tblId.intValue())
                    {
                        tableList.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }
        
        if (query != null)
        {
            TableQRI qri = (TableQRI) tableList.getSelectedValue();
            if (qri == null) { throw new RuntimeException("Invalid context for query."); }
            Vector<SpQueryField> fields = new Vector<SpQueryField>(query.getFields());
            Collections.sort(fields);
            currentInx = 0;
            for (SpQueryField field : fields)
            {
                addQueryFieldItem(field);
            }
        }

        for (QueryFieldPanel qfp : queryFieldItems)
        {
            qfp.resetValidator();
        }

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                //Sorry, but a new context can't be selected if any fields are selected from the current context.
                tableList.setEnabled(queryFieldItems.size() == 0);
                selectQFP(queryFieldItems.get(0));
                QueryBldrPane.this.validate();
            }
        });
    }

    /**
     * @param queryArg
     */
    public void setQuery(final SpQuery queryArg)
    {
        query = queryArg;
        name = query.getName();
        setQueryIntoUI();
    }

    protected String buildHQL(final TableQRI rootTable, boolean distinct)
    {
        if (queryFieldItems.size() == 0)
            return null;

        StringBuilder fieldsStr = new StringBuilder();
        Vector<BaseQRI> list = new Vector<BaseQRI>();
        StringBuilder criteriaStr = new StringBuilder();
        StringBuilder orderStr = new StringBuilder();
        boolean debug = true;
        ProcessNode root = new ProcessNode(null);
        int fldPosition = distinct ? 0 : 1;

        for (QueryFieldPanel qfi : queryFieldItems)
        {
            qfi.updateQueryField();

            if (qfi.isForDisplay())
            {
                fldPosition++;
            }

            if (debug)
            {
                System.out.println("\nNode: " + qfi.getFieldName());
            }

            String orderSpec = qfi.getOrderSpec(fldPosition);
            if (orderSpec != null)
            {
                if (orderStr.length() > 0)
                {
                    orderStr.append(", ");
                }
                orderStr.append(orderSpec);
            }

            // Create a Stack (list) of parent from
            // the current node up to the top
            // basically we are creating a path of nodes
            // to determine if we need to create a new node in the tree
            list.clear();
            FieldQRI pqri = qfi.getFieldQRI();
            TableTree parent = pqri.getTableTree();
            list.insertElementAt(pqri, 0);
            while (parent != tableTree)
            {
                list.insertElementAt(parent.getTableQRI(), 0);
                parent = parent.getParent();
            }

            if (debug)
            {
                System.out.println("Path From Top Down:");
                for (BaseQRI qri : list)
                {
                    System.out.println("  " + qri.getTitle());
                }
            }

            // Now walk the stack top (the top most parent)
            // down and if the path form the top down doesn't
            // exist then add a new node
            ProcessNode parentNode = root;
            for (BaseQRI qri : list)
            {
                if (debug)
                {
                    System.out.println("ProcessNode[" + qri.getTitle() + "]");
                }
                if (!parentNode.contains(qri) && qri instanceof TableQRI)
                {
                    ProcessNode newNode = new ProcessNode(qri);
                    parentNode.getKids().add(newNode);
                    if (debug)
                    {
                        System.out.println("Adding new node["
                                + newNode.getQri().getTitle()
                                + "] to Node["
                                + (parentNode.getQri() == null ? "root" : parentNode.getQri()
                                        .getTitle()) + "]");
                    }
                    parentNode = newNode;
                }
                else
                {
                    for (ProcessNode kidNode : parentNode.getKids())
                    {
                        if (kidNode.getQri().equals(qri))
                        {
                            parentNode = kidNode;
                            break;
                        }
                    }
                }
            }

            if (debug)
            {
                System.out.println("Current Tree:");
                printTree(root, 0);
            }

        }
        printTree(root, 0);

        StringBuilder fromStr = new StringBuilder();
        tableAbbreviator.clear();
        processTree(root, fromStr, 0);

        StringBuilder sqlStr = new StringBuilder();
        sqlStr.append("select ");
        if (distinct)
        {
            sqlStr.append(" distinct ");
        }
        if (!distinct)
        {
            fieldsStr.append(tableAbbreviator.getAbbreviation(rootTable.getTableTree()));
            fieldsStr.append(".");
            fieldsStr.append(rootTable.getTableInfo().getIdFieldName());
        }

        SortedSet<String> checkedForSpecialColumns = new TreeSet<String>();
        for (QueryFieldPanel qfi : queryFieldItems)
        {
            if (qfi.isForDisplay())
            {
                String fldSpec = qfi.getFieldQRI().getSQLFldSpec(tableAbbreviator);
                if (StringUtils.isNotEmpty(fldSpec))
                {
                    if (fieldsStr.length() > 0)
                    {
                        fieldsStr.append(", ");
                    }
                    fieldsStr.append(qfi.getFieldQRI().getSQLFldSpec(tableAbbreviator));
                }
            }

            String alias = tableAbbreviator.getAbbreviation(qfi.getFieldQRI().getTableTree());
            if (!checkedForSpecialColumns.contains(alias))
            {
                String specialColumnWhere = QueryAdjusterForDomain.getInstance().getSpecialColumns(
                        qfi.getFieldQRI().getTableInfo(), true, alias);
                checkedForSpecialColumns.add(alias);
                if (StringUtils.isNotEmpty(specialColumnWhere))
                {
                    if (criteriaStr.length() > 0)
                    {
                        criteriaStr.append(" AND ");
                    }
                    criteriaStr.append(specialColumnWhere);
                }
            }
            String criteria = qfi.getCriteriaFormula(tableAbbreviator);
            boolean isDisplayOnly = StringUtils.isEmpty(criteria);
            if (!isDisplayOnly)
            {
                if (!isDisplayOnly && criteriaStr.length() > 0)
                {
                    criteriaStr.append(" AND ");
                }
                criteriaStr.append(criteria);
            }
        }
        sqlStr.append(fieldsStr);

        sqlStr.append(" from ");
        sqlStr.append(fromStr);

        if (criteriaStr.length() > 0)
        {
            sqlStr.append(" where ");
            sqlStr.append(criteriaStr);
        }

        if (orderStr.length() > 0)
        {
            sqlStr.append(" order by ");
            sqlStr.append(orderStr);
        }

        if (debug)
        {
            System.out.println(sqlStr.toString());
        }
        return sqlStr.toString();
    }
    
    /**
     * Performs the Search by building the HQL String.
     */
    protected void doSearch(final TableQRI rootTable, boolean distinct)
    {
        String hql = buildHQL(rootTable, distinct);    
        processSQL(queryFieldItems, hql, rootTable.getTableInfo(), distinct);
        //doReport(rootTable, true /*need to use true to workaround probs with added key column when not distinct*/);
    }

    protected void doReport(final TableQRI rootTable, boolean distinct)
    {
        processReport(queryFieldItems, buildHQL(rootTable, distinct));
    }
    
    /**
     * @return Panel with up and down arrows for moving fields up and down in queryFieldsPanel.
     */
    protected JPanel bldMoverPanel()
    {
        orderUpBtn = createIconBtn("ReorderUp", "QB_FLD_MOVE_UP", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                orderUp();
            }
        });
        orderDwnBtn = createIconBtn("ReorderDown", "QB_FLD_MOVE_DOWN", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                orderDown();
            }
        });
        
        PanelBuilder upDownPanel = new PanelBuilder(new FormLayout("f:p:g, p, 2px, p, f:p:g","p"));        
        CellConstraints cc = new CellConstraints();
        upDownPanel.add(orderUpBtn,       cc.xy(2, 1));
        upDownPanel.add(orderDwnBtn,      cc.xy(4, 1));

        return upDownPanel.getPanel();
    }

    /**
     * Moves selected QFP up in queryFieldsPanel
     */
    protected void orderUp()
    {
        moveField(selectedQFP, queryFieldItems.get(queryFieldItems.indexOf(selectedQFP)-1));
    }
    
    /**
     * Moves selected QFP down in queryFieldsPanel
     */
    protected void orderDown()
    {
        moveField(selectedQFP, queryFieldItems.get(queryFieldItems.indexOf(selectedQFP)+1));
    }
    
    /**
     * @param parent
     * @param sqlStr
     * @param level
     */
    protected void processTree(final ProcessNode parent, final StringBuilder sqlStr, final int level)
    {
        BaseQRI qri = parent.getQri();
        if (qri != null && qri.getTableTree() != tableTree)
        {
            if (qri instanceof TableQRI)
            {
                TableTree tt = qri.getTableTree();
                if (level == 1)
                {
                    sqlStr.append(tt.getName());
                    sqlStr.append(' ');
                    sqlStr.append(tableAbbreviator.getAbbreviation(tt));
                    sqlStr.append(' ');

                }
                else
                {
                    // really should only use left join when necessary...
                    sqlStr.append(" left join ");

                    sqlStr.append(tableAbbreviator.getAbbreviation(tt.getParent()));
                    sqlStr.append('.');
                    sqlStr.append(tt.getField());
                    sqlStr.append(' ');
                    sqlStr.append(tableAbbreviator.getAbbreviation(tt));
                    sqlStr.append(' ');
                }
            }
        }
        for (ProcessNode kid : parent.getKids())
        {
            processTree(kid, sqlStr, level + 1);
        }
    }

    protected List<ERTICaptionInfo> getColumnInfo(final Vector<QueryFieldPanel> queryFieldItemsArg, final boolean fixLabels)
    {
        List<ERTICaptionInfo> result = new Vector<ERTICaptionInfo>();
        for (QueryFieldPanel qfp : queryFieldItemsArg)
        {
            DBFieldInfo fi = qfp.getFieldInfo();
            DBTableInfo ti = null;
            if (fi != null)
            {
               ti = fi.getTableInfo();
            }
            String colName = qfp.getFieldName();
            if (ti != null && fi != null)
            {
                colName = ti.getAbbrev() + '.' + fi.getColumn();
            }
            if (qfp.isForDisplay() && qfp.isDisplayable())
            {
                String lbl = qfp.getLabel();
                if (fixLabels)
                {
                    lbl = lbl.replaceAll(" ", "_");
                }
                ERTICaptionInfo erti = new ERTICaptionInfo(colName, lbl, true, qfp.getFieldQRI().getFormatter(), 0);
                erti.setColClass(qfp.getFieldQRI().getDataClass());
                result.add(erti);
            }
        }
        return result;
    }
    
    protected void processReport(final Vector<QueryFieldPanel> queryFieldItemsArg, final String sql)
    {
        QBJRDataSource src = new QBJRDataSource(sql, getColumnInfo(queryFieldItemsArg, true));
        final CommandAction cmd = new CommandAction(ReportsBaseTask.REPORTS,
                ReportsBaseTask.PRINT_REPORT, src);
        cmd.setProperty("title", query.getName());
        cmd.setProperty("file", "dumpling.jrxml");
        CommandDispatcher.dispatch(cmd);
    }
    
    /**
     * @param queryFieldItemsArg
     * @param sql
     */
    protected void processSQL(final Vector<QueryFieldPanel> queryFieldItemsArg, final String sql, final DBTableInfo rootTable, final boolean distinct)
    {
        List<ERTICaptionInfo> captions = getColumnInfo(queryFieldItemsArg, false);
        List<Integer> list = new Vector<Integer>();
        
        String iconName = distinct ? "BlankIcon" : rootTable.getClassObj().getSimpleName();
        int tblId = distinct ? -1 : rootTable.getTableId();
        QBQueryForIdResultsHQL qri = new QBQueryForIdResultsHQL(new Color(144, 30, 255),
                "Search Results", // XXX I18N
                //rootTable.getTableInfo().getClassObj().getSimpleName(), // Icon Name
                iconName,
                //rootTable.getTableInfo().getTableId(), // table id
                tblId,
                "", // search term
                list);
        //else do something else...
        
        qri.setSQL(sql);
        qri.setCaptions(captions);
        qri.setExpanded(true);

        CommandDispatcher.dispatch(new CommandAction("Express_Search", "HQL", qri));
    }

    /**
     * @param pn
     * @param lvl
     */
    protected void printTree(ProcessNode pn, int lvl)
    {
        for (int i = 0; i < lvl; i++)
        {
            System.out.print(" ");
        }
        System.out.println(pn.getQri() == null ? "Root" : pn.getQri().getTitle());
        for (ProcessNode kid : pn.getKids())
        {
            printTree(kid, lvl + 1);
        }
    }

    /**
     * 
     */
    protected boolean saveQuery()
    {
        if (!query.isNamed())
        {
            if (!getQueryNameFromUser())
            {
                return false;
            }
        }
        
        TableQRI tableQRI = (TableQRI) tableList.getSelectedValue();
        if (tableQRI != null)
        {
            short position = 0;
            for (QueryFieldPanel qfp : queryFieldItems)
            {
                SpQueryField qf = qfp.getQueryField();
                if (qf == null) { throw new RuntimeException("Shouldn't get here!"); }
                qf.setPosition(position);
                qfp.updateQueryField();

                position++;
            }

            if (query.getSpQueryId() == null)
            {
                queryNavBtn = ((QueryTask) task).saveNewQuery(query, false); // false tells it to
                                                                                // disable the
                                                                                // navbtn

                query.setNamed(true);
                SubPaneMgr.getInstance().renamePane(this, query.getName());
            }
            else
            {
                try
                {
                    DataProviderSessionIFace session = DataProviderFactory.getInstance()
                            .createSession();
                    try
                    {
                        session.beginTransaction();
                        session.saveOrUpdate(query);
                        session.commit();
                    }
                    finally
                    {
                        session.close();
                    }
                }
                catch (Exception ex)
                {
                    log.error(ex);
                    ex.printStackTrace();
                }
            }
            return true;
        }
        //else
        {
            log.error("No Context selected!");
            return false;
        }
    }

    /**
     * @return true if a valid query name was obtained from user
     */
    protected boolean getQueryNameFromUser()
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            String newQueryName = query.getName();
            String oldQueryName = query.getName();
            SpQuery fndQuery = null;
            boolean good = false;
            do
            {
                if (QueryTask.askUserForInfo("Query", getResourceString("QB_DATASET_INFO"), query))
                {
                    newQueryName = query.getName();
                    if (StringUtils.isNotEmpty(newQueryName) && newQueryName.length() > 64)
                    {
                        UIRegistry.getStatusBar().setErrorMessage(
                                getResourceString("QB_NAME_TOO_LONG"));
                    }
                    else if (StringUtils.isEmpty(newQueryName))
                    {
                        UIRegistry.getStatusBar().setErrorMessage(
                                getResourceString("QB_ENTER_A_NAME"));
                    }
                    else
                    {
                        fndQuery = session.getData(SpQuery.class, "name", newQueryName,
                                DataProviderSessionIFace.CompareType.Equals);
                        if (fndQuery != null)
                        {
                            UIRegistry.getStatusBar().setErrorMessage(
                                    String.format(getResourceString("QB_QUERY_EXISTS"),
                                            newQueryName));
                        }
                        else
                        {
                            good = true;
                        }
                    }
                }
                else
                {
                    query.setName(oldQueryName);
                    return false;
                }
            } while (!good);
        }
        catch (Exception ex)
        {
            log.error(ex);

        }
        finally
        {
            session.close();
        }
        UIRegistry.getStatusBar().setText("");
        return true;
    }

    /**
     * @param parentTT
     * @param nameArg
     * @return
     */
    protected static TableTree findTableTree(final TableTree parentTT, final String nameArg)
    {
        for (int k=0; k<parentTT.getKids(); k++)
        {
            TableTree tt = parentTT.getKid(k);
            if (tt.getName().equals(nameArg)) { return tt; }
        }
        return null;
    }

    // -----------------------------------------------------------
    // -- Inner Classes
    // -----------------------------------------------------------

    class ProcessNode
    {
        protected Vector<ProcessNode> kids = new Vector<ProcessNode>();
        protected BaseQRI             qri;

        public ProcessNode(BaseQRI qri)
        {
            this.qri = qri;
        }

        public Vector<ProcessNode> getKids()
        {
            return kids;
        }

        public BaseQRI getQri()
        {
            return qri;
        }

        public boolean contains(BaseQRI qriArg)
        {
            for (ProcessNode pn : kids)
            {
                if (pn.getQri().equals(qriArg)) { return true; }
            }
            return false;
        }
    }

    /**
     * @param parentQRI
     * @param tableTree
     * @param model
     */
    protected void createNewList(final TableQRI tblQRI, final DefaultListModel model)
    {
        model.clear();
        if (tblQRI != null)
        {
            for (int f = 0; f < tblQRI.getFields(); f++)
            {
                model.addElement(tblQRI.getField(f));
            }
            for (int k = 0; k < tblQRI.getTableTree().getKids(); k++)
            {
                boolean addIt = true;
                if (tblQRI.getTableTree().getKid(k).isAlias())
                {
                    addIt = fixAliases(tblQRI.getTableTree().getKid(k));
                }
                if (addIt)
                {
                    model.addElement(tblQRI.getTableTree().getKid(k).getTableQRI());
                }
            }
        }
    }

    /**
     * @param aliasTbl
     * @param tblInfo
     * @return true if aliasTbl should be displayed in the fields list for the current
     *         context.
     */
    protected boolean tblIsDisplayable(final TableTree aliasTbl, final DBTableInfo tblInfo)
    {
        if (aliasTbl.isAlias())
        {
            return !isCyclic(aliasTbl, tblInfo.getTableId()) || isCyclicable(aliasTbl, tblInfo);
        }
        //else
        return true;
    }
    
    /**
     * @param alias
     * @param tblId
     * @return true if the specified alias represents a table that is already
     * present in the alias' tabletree.
     */
    protected boolean isCyclic(final TableTree alias, final int tblId)
    {
        TableTree parent = alias.getParent();
        while (parent != null)
        {
            if (parent.getTableInfo() != null && parent.getTableInfo().getTableId() == tblId)
            {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }
    
    /**
     * @param alias
     * @param tblInfo
     * @return true if it is OK for the specified alias to create a cycle.
     */
    protected boolean isCyclicable(final TableTree alias, final DBTableInfo tblInfo)
    {
        return Treeable.class.isAssignableFrom(tblInfo.getClassObj());
            //special conditions... (may be needed. For example for Determination and Taxon, but on the other hand
            //Determination <-> Taxon behavior seems ok for now.
            
            ////assuming isCyclic
            //&& !Taxon.class.isAssignableFrom(tblInfo.getClassObj()) || !isAncestorClass(alias, Determination.class);
    }
    
//    protected boolean isAncestorClass(final TableTree tbl, final Class<?> cls)
//    {
//        TableTree parent = tbl.getParent();
//        while (parent != null)
//        {
//            if (parent.getTableInfo() != null && parent.getTableInfo().getClassObj().equals(cls))
//            {
//                return true;
//            }
//            parent = parent.getParent();
//        }
//        return false;
//    }
    
    
    /**
     * @param parentList
     */
    protected void fillNextList(final JList parentList)
    {
        if (processingLists) { return; }

        processingLists = true;

        final int curInx = listBoxList.indexOf(parentList);
        if (curInx > -1)
        {
            for (int i = curInx + 1; i < listBoxList.size(); i++)
            {
                listBoxPanel.remove(spList.get(i));
            }

        }
        else
        {
            listBoxPanel.removeAll();
        }

        QryListRendererIFace item = (QryListRendererIFace) parentList.getSelectedValue();
        if (!(item instanceof FieldQRI))
        {
            JList newList;
            DefaultListModel model;
            JScrollPane sp;

            if (curInx == listBoxList.size() - 1)
            {
                newList = new JList(model = new DefaultListModel());
                newList.addMouseListener(new MouseAdapter()
                {

                    /* (non-Javadoc)
                     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
                     */
                    @Override
                    public void mouseClicked(MouseEvent e)
                    {
                        if (e.getClickCount() == 2)
                        {
                            if (currentInx != -1)
                            {
                                QryListRendererIFace qriFace = (QryListRendererIFace) listBoxList.get(
                                        currentInx).getSelectedValue();
                                if (BaseQRI.class.isAssignableFrom(qriFace.getClass()))
                                {
                                    BaseQRI qri = (BaseQRI) qriFace;
                                    if (qri.isInUse())
                                    {
                                        //remove the field
                                        for (QueryFieldPanel qfp : QueryBldrPane.this.queryFieldItems)
                                        {
                                            if (qfp.getFieldQRI() == qri)
                                            {
                                                QueryBldrPane.this.removeQueryFieldItem(qfp);
                                                break;
                                            }
                                        }
                                    }
                                    else
                                    {
                                        // add the field
                                        FieldQRI fieldQRI = buildFieldQRI(qri);
                                        //FieldQRI fieldQRI = (FieldQRI)qri;
                                        SpQueryField qf = new SpQueryField();
                                        qf.initialize();
                                        qf.setFieldName(fieldQRI.getFieldName());
                                        query.addReference(qf, "fields");
                                        addQueryFieldItem(fieldQRI, qf);
                                    }
                                }
                            }
                        }
                    }
                });
                newList.setCellRenderer(qryRenderer);
                listBoxList.add(newList);
                sp = new JScrollPane(newList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                spList.add(sp);

                newList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
                {
                    public void valueChanged(ListSelectionEvent e)
                    {
                        if (!e.getValueIsAdjusting())
                        {
                            fillNextList(listBoxList.get(curInx + 1));
                        }
                    }
                });

            }
            else
            {
                newList = listBoxList.get(curInx + 1);
                model = (DefaultListModel) newList.getModel();
                sp = spList.get(curInx + 1);
            }

            if (item instanceof TableQRI)
            {
                createNewList((TableQRI)item, model);

            }
//            else if (item instanceof RelQRI)
//            {
//                createNewList(item, model);
//            }

            listBoxPanel.add(sp);
            listBoxPanel.remove(addBtn);
            currentInx = -1;

        }
        else
        {
            listBoxPanel.add(addBtn);
        }

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                updateAddBtnState();

                // Is all this really necessary
                listBoxPanel.validate();
                listBoxPanel.repaint();
                scrollPane.validate();
                scrollPane.invalidate();
                scrollPane.doLayout();
                scrollPane.repaint();
                validate();
                invalidate();
                doLayout();
                repaint();
                UIRegistry.forceTopFrameRepaint();
            }
        });

        processingLists = false;
        currentInx = curInx;
    }

    
    protected FieldQRI buildFieldQRI(final BaseQRI qri)
    {
        if (qri instanceof FieldQRI) { return (FieldQRI) qri; }
        if (qri instanceof TableQRI)
        {
            DBRelationshipInfo relInfo = null;
            List<DBRelationshipInfo> rels = new LinkedList<DBRelationshipInfo>();
            for (DBRelationshipInfo rel : qri.getTableTree().getParent().getTableInfo().getRelationships())
            {
                if (rel.getDataClass().equals(qri.getTableTree().getTableInfo().getClassObj()))
                {
                    rels.add(rel);
                }
            }
            for (DBRelationshipInfo rel : rels)
            {
                System.out.println(rel.getName());
                System.out.println("   " + rel.getDataClass().getSimpleName());
                System.out.println("   " + rel.getColName());
                System.out.println("   " + rel.getOtherSide());
            }
            if (rels.size() == 1)
            {
                relInfo = rels.get(0);
            }
            else
            {
                throw new RuntimeException("unable to determine relationship.");
            }
            
            return new RelQRI((TableQRI) qri, relInfo);
        }
        throw new RuntimeException("invalid argument: " + qri);
    }

    /**
     * 
     */
    protected void updateAddBtnState()
    {
        if (currentInx != -1)
        {
            QryListRendererIFace qri = (QryListRendererIFace) listBoxList.get(currentInx)
                    .getSelectedValue();
            if (qri instanceof FieldQRI)
            {
                FieldQRI fieldQRI = (FieldQRI) qri;
                addBtn.setEnabled(!fieldQRI.isInUse());
            }
        }
    }

    /**
     * Removes it from the List.
     * 
     * @param qfp QueryFieldPanel to be added
     */
    public void removeQueryFieldItem(final QueryFieldPanel qfp)
    {
        qfp.getFieldQRI().setIsInUse(false);
        qualifyFieldLabels();
        if (qfp.getQueryField() != null)
        {
            query.removeReference(qfp.getQueryField(), "fields");
        }
        queryFieldItems.remove(qfp);

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                if (selectedQFP == qfp)
                {
                    selectQFP(null);
                }
                queryFieldsPanel.getLayout().removeLayoutComponent(qfp);
                queryFieldsPanel.remove(qfp);
                queryFieldsPanel.validate();
                updateAddBtnState();
                
                //Sorry, but a new context can't be selected if any fields are selected from the current context.
                tableList.setEnabled(queryFieldItems.size() == 0);
                
                try
                {
                    listBoxList.get(currentInx).repaint();
                } catch (ArrayIndexOutOfBoundsException ex)
                {
                    log.error(ex);
                }
                queryFieldsPanel.repaint();
                saveBtn.setEnabled(QueryBldrPane.this.queryFieldItems.size() > 0);
            }
        });
    }

    /**
     * @param kids
     * @param field
     * @param tableIds
     * @param level
     * @return
     */
    protected FieldQRI getFieldQRI(final TableTree tbl,
                                   final SpQueryField field,
                                   final int[] tableIds,
                                   final int level)
    {
        int id = tableIds[level];
        for (int k=0; k<tbl.getKids(); k++)
        {
            TableTree kid = tbl.getKid(k);
            boolean checkKid = true;
            if (kid.isAlias()) 
            {
                checkKid = fixAliases(kid);
            }
            if (checkKid)
            {
                if (kid.getTableInfo().getTableId() == id)
                {
                    if (level == (tableIds.length - 1))
                    {
                        for (int f = 0; f < kid.getTableQRI().getFields(); f++)
                        {
                            if (kid.getTableQRI().getField(f).getFieldName().equals(
                                    field.getFieldName())) { return kid.getTableQRI().getField(f); }
                        }
                    }
                    else
                    {
                        FieldQRI fi = getFieldQRI(kid, field, tableIds, level + 1);
                        if (fi != null) { return fi; }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Add QueryFieldItem to the list created with a TableFieldPair.
     * 
     * @param fieldItem the TableFieldPair to be in the list
     */
    protected void addQueryFieldItem(final SpQueryField field)
    {
        if (field != null)
        {
            FieldQRI fieldQRI = getFieldQRI(tableTree, field, field.getTableIds(), 0);
            if (fieldQRI != null)
            {
                addQueryFieldItem(fieldQRI, field);
            }
            else
            {
                log.error("Couldn't find [" + field.getFieldName() + "] [" + field.getTableList()
                        + "]");
            }
        }
    }

    /**
     * Add QueryFieldItem to the list created with a TableFieldPair.
     * 
     * @param fieldItem the TableFieldPair to be in the list
     */
    protected void addQueryFieldItem(final FieldQRI fieldQRI, final SpQueryField queryField)
    {
        if (fieldQRI != null)
        {
            final QueryFieldPanel header;
            if (queryFieldItems.size() == 0 && queryFieldsPanel.getComponentCount() == 0)
            {
                header = new QueryFieldPanel(this, fieldQRI,
                        IconManager.IconSize.Std24, columnDefStr, saveBtn, null);
            }
            else
            {
                header = null;
            }
            
            final QueryFieldPanel qfp = new QueryFieldPanel(this, fieldQRI,
                    IconManager.IconSize.Std24, columnDefStr, saveBtn, queryField);
            qfp.addMouseListener(new MouseInputAdapter()
            {
                @Override
                public void mousePressed(MouseEvent e)
                {
                    selectQFP(qfp);
                }
            });
            queryFieldItems.add(qfp);
            qualifyFieldLabels();
            fieldQRI.setIsInUse(true);
            
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    if (currentInx > -1)
                    {
                        if (header != null)
                        {
                            queryFieldsPanel.add(header);
                        }
                        queryFieldsPanel.add(qfp);
                        queryFieldsPanel.validate();
                        listBoxList.get(currentInx).repaint();
                        updateAddBtnState();
                        selectQFP(qfp);
                        queryFieldsPanel.repaint();
                        saveBtn.setEnabled(true);
                        //Sorry, but a new context can't be selected if any fields are selected from the current context.
                        tableList.setEnabled(queryFieldItems.size() == 0);
                    }
                }
            });
        }
    }

    /**
     * @param qfp
     * 
     * Displays and highlights qfp.
     * 
     * Use runSelectQFP if not calling from Swing thread.
     */
    protected void selectQFP(final QueryFieldPanel qfp)
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            //apparently this never happens, but...
            runSelectQFP(qfp);
        }
        else
        {
            if (selectedQFP != null)
            {
                selectedQFP.setBorder(null);
                selectedQFP.repaint();
            }
            selectedQFP = qfp;
            if (selectedQFP != null)
            {
                selectedQFP.setBorder(new LineBorder(Color.BLACK));
                selectedQFP.repaint();
                scrollQueryFieldsToRect(selectedQFP.getBounds());
            }
            updateMoverBtns();
        }
    }
    
    /**
     * @param qfp
     * 
     * runs selectQFP() in Swing thread.
     */
    private void runSelectQFP(final QueryFieldPanel qfp)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                selectQFP(qfp);
            }
        });
    }
    
    /**
     * Enables field mover buttons as appropriate for position of currently select QueryFieldPanel.
     */
    protected void updateMoverBtns()
    {
        int idx = queryFieldItems.indexOf(selectedQFP);
        orderUpBtn.setEnabled(idx > 0);
        orderDwnBtn.setEnabled(idx > -1 && idx < queryFieldItems.size()-1);
        
    }
    
    /**
     * Adds qualifiers (TableOrRelationship/Field Title) to query fields where necessary.
     * 
     */
    protected void qualifyFieldLabels()
    {
        List<String> labels = new ArrayList<String>(queryFieldItems.size()-1);
        Map<String, List<QueryFieldPanel>> map = new HashMap<String, List<QueryFieldPanel>>();
        for (QueryFieldPanel qfp : queryFieldItems)
        {
            if (qfp.getFieldInfo() != null) //this means tree levels won't get qualified.
            {
                if (!map.containsKey(qfp.getFieldInfo().getTitle()))
                {
                    map.put(qfp.getFieldInfo().getTitle(), new LinkedList<QueryFieldPanel>());
                }
                map.get(qfp.getFieldInfo().getTitle()).add(qfp);
                labels.add(qfp.getFieldInfo().getTitle());
            }
        }
        
        for (Map.Entry<String, List<QueryFieldPanel>> entry : map.entrySet())
        {
            if (entry.getValue().size() > 1 || entry.getValue().get(0).isLabelQualified())
            {
                for (QueryFieldPanel q : entry.getValue())
                {
                    labels.remove(entry.getKey());
                    labels.add(q.qualifyLabel(labels, entry.getValue().size() == 1));
                }
            }
        }
    }

    /**
     * @param parent
     * @param parentTT
     * @param ttKids
     * @param parentQRI
     */
    protected void processForTables(final Element parent,
                                    final TableTree parentTT)
    {
        String tableName = XMLHelper.getAttr(parent, "name", null);
        if (tableName.equals("CollectionObject") || tableName.equals("Taxon"))
        {
            int x = 0;
            x++;
        }
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByShortClassName(tableName);
        if (!tableInfo.isHidden())
        {
            String fieldName = XMLHelper.getAttr(parent, "field", null);
            if (StringUtils.isEmpty(fieldName))
            {
                fieldName = tableName.substring(0, 1).toLowerCase() + tableName.substring(1);
            }

            String abbrev = XMLHelper.getAttr(parent, "abbrev", null);
            TableTree newTreeNode = parentTT.addKid(new TableTree(tableName, fieldName, abbrev, tableInfo));
            if (Treeable.class.isAssignableFrom(tableInfo.getClassObj()))
            {
                try
                {
                    TreeDefIface<?, ?, ?> treeDef = Collection.getCurrentCollection()
                            .getDiscipline().getTreeDef(
                                    UploadTable.capitalize(tableInfo.getClassObj().getSimpleName())
                                            + "TreeDef");
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
                if (!tableInfo.isHidden())
                {
                    tableName = XMLHelper.getAttr(kidElement, "name", null);
                    fieldName = XMLHelper.getAttr(kidElement, "field", null);
                    if (StringUtils.isEmpty(fieldName))
                    {
                        fieldName = tableName.substring(0, 1).toLowerCase() + tableName.substring(1);
                    }
                    newTreeNode.addKid(new TableTree(kidClassName, fieldName, true));
                }
            }
        }
    }

    /**
     * @param tbl
     * @param hash
     */
    protected boolean fixAliases(final TableTree tbl)
    {
        if (tbl.isAlias())
        {
            TableTree tt = tableTreeHash.get(tbl.getName());
            if (tt != null)
            {
                if (tblIsDisplayable(tbl, tt.getTableInfo()))
                {
                    tbl.clearKids();
                    try
                    {
                        for (int k = 0; k < tt.getKids(); k++)
                        {
                            tbl.addKid((TableTree) tt.getKid(k).clone());
                        }
                        tbl.setTableInfo(tt.getTableInfo());
                        tbl.setTableQRIClone(tt.getTableQRI());
                        return true;
                    }
                    catch (CloneNotSupportedException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                }
                return false;
            }
            log.error("Couldn't find [" + tbl.getName() + "] in the hash.");
            return false;
        }
        return true;
    }

    /**
     * @return
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

            tableTreeHash = new Hashtable<String, TableTree>();
            for (int t=0; t<treeRoot.getKids(); t++)
            {
                TableTree tt = treeRoot.getKid(t);
                tableTreeHash.put(tt.getName(), tt);
                log.debug("Adding[" + tt.getName() + "] to hash");
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return treeRoot;
    }

    /**
     * @param columnDefStr the columnDefStr to set
     */
    public void setColumnDefStr(String columnDefStr)
    {
        this.columnDefStr = columnDefStr;
    }

    /**
     * @return the btn that launched the editor
     */
    public RolloverCommand getQueryNavBtn()
    {
        return queryNavBtn;
    }

    /**
     * @param queryNavBtn
     */
    public void setQueryNavBtn(RolloverCommand queryNavBtn)
    {
        this.queryNavBtn = queryNavBtn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#shutdown()
     */
    @Override
    public void shutdown()
    {
        super.shutdown();

        if (saveBtn != null && saveBtn.isEnabled())
        {
            saveBtn.setEnabled(false);
        }
        query = null;
        if (queryNavBtn != null)
        {
            queryNavBtn.setEnabled(true);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#aboutToShutdown()
     */
    @Override
    public boolean aboutToShutdown()
    {
        boolean result = true;
        if (isChanged())
        {
            String msg = String.format(getResourceString("SaveChanges"), getTitle());
            JFrame topFrame = (JFrame)UIRegistry.getTopWindow();

            int rv = JOptionPane.showConfirmDialog(topFrame,
                                                   msg,
                                                   getResourceString("SaveChangesTitle"),
                                                   JOptionPane.YES_NO_CANCEL_OPTION);
            if (rv == JOptionPane.YES_OPTION)
            {
                saveQuery();
            }
            else if (rv == JOptionPane.CANCEL_OPTION || rv == JOptionPane.CLOSED_OPTION)
            {
                return false;
            }
            else if (rv == JOptionPane.NO_OPTION)
            {
                // nothing
            }
        }
        return result;
    }
    
    protected boolean isChanged()
    {
        return saveBtn.isEnabled(); //el cheapo
    }
    
    /**
     * @param toMove
     * @param moveTo
     * 
     * Moves toMove to moveTo's position and shifts other panels to fill toMove's former position.
     */
    protected void moveField(final QueryFieldPanel toMove, final QueryFieldPanel moveTo)
    {
        int fromIdx = queryFieldItems.indexOf(toMove);
        int toIdx = queryFieldItems.indexOf(moveTo);
        if (fromIdx == toIdx)
        {
            return;
        }
        
        queryFieldItems.remove(fromIdx);
        queryFieldItems.insertElementAt(toMove, toIdx);
         
        ((NavBoxLayoutManager)queryFieldsPanel.getLayout()).moveLayoutComponent(toMove, moveTo);
        
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                queryFieldsPanel.doLayout();
                queryFieldsPanel.validate();
                scrollQueryFieldsToRect(toMove.getBounds());
                queryFieldsPanel.repaint();
                updateMoverBtns();
                saveBtn.setEnabled(true);
            }
        });
    }
    
    /**
     * @param rect - the rectangle to make visible.
     * 
     * Wrapper for JViewport.scrollReectToVisible() with a work around for a java bug.
     */
    protected void scrollQueryFieldsToRect(final Rectangle rect)
    {
        //scrollRectToVisible doesn't work when newBounds is above the viewport.
        //This is a java bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6333318
        if (rect.y < queryFieldsScroll.getViewport().getViewPosition().y)
        {
            queryFieldsScroll.getViewport().setViewPosition(new Point(rect.x,rect.y));
        }
        queryFieldsScroll.getViewport().scrollRectToVisible(rect);
    }
    
    public int getFields()
    {
        return queryFieldItems.size();
    }
    
    public QueryFieldPanel getField(int index)
    {
        return queryFieldItems.get(index);
    }
}


