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

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createIconBtn;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.NavBoxLayoutManager;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBRelationshipInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.dbsupport.DBRelationshipInfo.RelationshipType;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpQueryField;
import edu.ku.brc.specify.datamodel.SpReport;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.tasks.QueryTask;
import edu.ku.brc.specify.tasks.ReportsBaseTask;
import edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadTable;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DropDownButton;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.db.ERTICaptionInfo;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 * 
 * @code_status Alpha
 * 
 * Feb 23, 2007
 * 
 */
public class QueryBldrPane extends BaseSubPane implements QueryFieldPanelContainerIFace, CommandListener
{
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#getAddBtn()
     */
    //@Override
    public JButton getAddBtn()
    {
        return addBtn;
    }

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

    protected ImageIcon                                      blankIcon        = IconManager.getIcon("BlankIcon", IconManager.IconSize.Std24);

    protected String                                         columnDefStr     = null;

    protected JPanel                                         listBoxPanel;
    protected Vector<JList>                                  listBoxList      = new Vector<JList>();
    protected Vector<TableTree>                              nodeList         = new Vector<TableTree>();
    protected JScrollPane                                    scrollPane;
    protected Vector<JScrollPane>                            spList           = new Vector<JScrollPane>();
    protected JPanel                                         contextPanel;
//    protected JButton                                        saveBtn;
    protected DropDownButton                                 saveBtn;
    protected JButton                                        searchBtn;
    protected JCheckBox                                      distinctChk;
    protected JCheckBox                                      countOnlyChk;
    /**
     * When countOnly is true, count of matching records is displayed, but the records are not displayed.
     */
    protected boolean                                        countOnly = false;
    
    protected Hashtable<String, Boolean>                     fieldsToSkipHash = new Hashtable<String, Boolean>();
    protected QryListRenderer                                qryRenderer      = new QryListRenderer(IconManager.STD_ICON_SIZE);
    
    protected int                                            listCellHeight;

    protected TableTree                                      tableTree;
    protected Hashtable<String, TableTree>                   tableTreeHash;    
    protected boolean                                        processingLists  = false;

    protected RolloverCommand                                queryNavBtn      = null;

    // Reordering
    protected JButton                                        orderUpBtn  = null;
    protected JButton                                        orderDwnBtn = null;
    protected boolean                                        doOrdering  = false;
    
    protected ExpressSearchResultsPaneIFace                  esrp        = null;
    protected boolean                                        isHeadless  = false; 
    
    protected final AtomicReference<QBQueryForIdResultsHQL> runningResults = new AtomicReference<QBQueryForIdResultsHQL>();
    protected final AtomicReference<QBQueryForIdResultsHQL> completedResults = new AtomicReference<QBQueryForIdResultsHQL>();
    protected final AtomicLong doneTime = new AtomicLong(-1);
    protected final AtomicLong startTime = new AtomicLong(-1);
    
    
    
    /**
     * Constructor.
     * 
     * @param name name of subpanel
     * @param task the owning task
     */
    public QueryBldrPane(final String name, final Taskable task, final SpQuery query)
    {
        this(name, task, query, false);
    }
    
    /**
     * Constructor.
     * 
     * @param name name of subpanel
     * @param task the owning task
     */
    public QueryBldrPane(final String name, 
                         final Taskable task, 
                         final SpQuery query,
                         final boolean isHeadless)
    {
        super(name, task);

        this.query      = query;
        this.isHeadless = isHeadless;

        String[] skipItems = { "TimestampCreated", "LastEditedBy", "TimestampModified" };
        for (String nameStr : skipItems)
        {
            fieldsToSkipHash.put(nameStr, true);
        }
        
        tableTree = readTables();
        tableTreeHash = buildTableTreeHash(tableTree);

        createUI();

        setQueryIntoUI();
        
        CommandDispatcher.register(ReportsBaseTask.REPORTS, this);
        
    }

    /**
     * 
     */
    protected void createUI()
    {
        removeAll();

        JMenuItem saveItem = new JMenuItem(UIRegistry.getResourceString("QB_SAVE"));
        saveItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (saveQuery(false))
                {
                    saveBtn.setEnabled(false);
                }
            }
        });
        JMenuItem saveAsItem = new JMenuItem(UIRegistry.getResourceString("QB_SAVE_AS"));
        saveAsItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (saveQuery(true))
                {
                    saveBtn.setEnabled(false);
                }
            }
        });
        JComponent[] itemSample = { saveItem, saveAsItem };
        saveBtn = new DropDownButton(UIRegistry.getResourceString("QB_SAVE"), null, 1,
                java.util.Arrays.asList(itemSample));
        saveBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (saveQuery(false))
                {
                    saveBtn.setEnabled(false);
                }
            }
        });
        UIHelper.setControlSize(saveBtn);
        //saveBtn.setOverrideBorder(true, BasicBorders.getButtonBorder());
        
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
                BaseQRI qri = (BaseQRI) listBoxList.get(currentInx).getSelectedValue();
                FieldQRI fieldQRI = buildFieldQRI(qri);

                SpQueryField qf = new SpQueryField();
                qf.initialize();
                qf.setFieldName(fieldQRI.getFieldName());
                query.addReference(qf, "fields");
                addQueryFieldItem(fieldQRI, qf, false);
            }
        });

        contextPanel = new JPanel(new BorderLayout());
        contextPanel.add(createLabel("Search Context", SwingConstants.CENTER), BorderLayout.NORTH);
        contextPanel.add(spt, BorderLayout.CENTER);
        contextPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        JPanel schemaPanel = new JPanel(new BorderLayout());
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

        final JPanel mover = buildMoverPanel(false);
        add(mover, BorderLayout.EAST);
        
        searchBtn   = createButton(UIRegistry.getResourceString("QB_SEARCH"));
        searchBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                doSearch();
            }
        });
        distinctChk = createCheckBox(UIRegistry.getResourceString("QB_DISTINCT"));
        distinctChk.setSelected(false);
        countOnlyChk = createCheckBox(UIRegistry.getResourceString("QB_COUNT_ONLY"));
        countOnlyChk.setSelected(false);
        countOnlyChk.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                new SwingWorker() {

                    /* (non-Javadoc)
                     * @see edu.ku.brc.helpers.SwingWorker#construct()
                     */
                    @Override
                    public Object construct()
                    {
                        //Don't allow change while query is running.
                        if (runningResults.get() == null)
                        {
                            countOnly = !countOnly;
                        }
                        else
                        {
                            //This might be awkward and/or klunky...
                            countOnlyChk.setSelected(countOnly);
                        }
                        return null;
                    }
                }.start();
            }
        });
        
        PanelBuilder outer = new PanelBuilder(new FormLayout("p, 2dlu, p, 2dlu, p, 6dlu, p", "p"));
        CellConstraints cc = new CellConstraints();
        outer.add(distinctChk, cc.xy(1, 1));
        outer.add(countOnlyChk, cc.xy(3, 1));
        outer.add(searchBtn, cc.xy(5, 1));
        outer.add(saveBtn, cc.xy(7, 1));
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(outer.getPanel(), BorderLayout.EAST);
        JButton helpBtn = createButton(UIRegistry.getResourceString("HELP"));
        if (ContextMgr.getCurrentContext() != null)
        {
            HelpMgr.registerComponent(helpBtn, ContextMgr.getCurrentContext().getName());
        }
        bottom.add(helpBtn, BorderLayout.WEST);
        add(bottom, BorderLayout.SOUTH);

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }
    
    /**
     * @param esrp the esrp to set
     */
    public void setEsrp(ExpressSearchResultsPaneIFace esrp)
    {
        this.esrp = esrp;
    }

    /**
     * Executes the search that has been loaded into the pane.
     */
    public void doSearch()
    {
        if (canSearch())
        {
            doSearch((TableQRI)tableList.getSelectedValue(), distinctChk.isSelected());
        }
        else 
        {
            cancelSearch();
        }
    }

    protected void cancelSearch()
    {
        if (runningResults.get() != null)
        {
            log.debug("cancelling search");
            runningResults.get().cancel();
        }
    }
    /**
     * @param fieldName
     * @return fieldName with lower-cased first character.
     */
    public static String fixFieldName(final String fieldName)
    {
        return fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
    }

    
    protected static Vector<QueryFieldPanel> getQueryFieldPanels(final SpQuery q,
                                                               final QueryFieldPanelContainerIFace container,
                                                               final TableTree tblTree,
                                                               final Hashtable<String, TableTree> ttHash)
    {
        return getQueryFieldPanels(container, q.getFields(), tblTree, ttHash, null);
    }
    
    /**
     * 
     */
    protected void setQueryIntoUI()
    {
        if (!isHeadless && !SwingUtilities.isEventDispatchThread()) 
        { 
            throw new RuntimeException("Method called from invalid thread."); 
        }
        queryFieldsPanel.removeAll();
        queryFieldItems.clear();
        queryFieldsPanel.validate();
        columnDefStr = null;
        tableList.clearSelection();
        contextPanel.setVisible(query == null);
        tableList.setSelectedIndex(-1);
        if (query != null)
        {
            query.forceLoad(true); 
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
        
        Vector<QueryFieldPanel> qfps = null;
        if (query != null)
        {
            TableQRI qri = (TableQRI) tableList.getSelectedValue();
            if (qri == null) { throw new RuntimeException("Invalid context for query."); }
            qfps = getQueryFieldPanels(this, query.getFields(), tableTree, tableTreeHash, saveBtn);
        }

        boolean header = true;
        for (final QueryFieldPanel qfp : qfps)
        {
            if (header)
            {
                header = false;
                this.queryFieldsScroll.setColumnHeaderView(qfp);
            }
            else
            {
                queryFieldItems.add(qfp);
                qfp.addMouseListener(new MouseInputAdapter()
                {
                    @Override
                    public void mousePressed(MouseEvent e)
                    {
                        selectQFP(qfp);
                    }
                });
                qfp.resetValidator();
                queryFieldsPanel.add(qfp);
            }
        }
        
        if (!isHeadless)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    adjustPanelUI();
                }
            });
        } else
        {
            adjustPanelUI();
        }
    }
    
    /**
     * 
     */
    protected void adjustPanelUI()
    {
        //Sorry, but a new context can't be selected if any fields are selected from the current context.
        tableList.setEnabled(queryFieldItems.size() == 0);
        if (queryFieldItems.size() > 0)
        {
            selectQFP(queryFieldItems.get(0));
        }
        for (JList list : listBoxList)
        {
            list.repaint();
        }
        saveBtn.setEnabled(false);
        updateSearchBtn();
        QueryBldrPane.this.validate();
    }

    /**
     * @param rootTable
     * @param distinct
     * @param qfps
     * @param tblTree
     * @param keysToRetrieve
     * @return the hql paired with a list of parameters/objects to assign.
     */
    protected static Pair<String, List<Pair<String, Object>>> buildHQL(final TableQRI rootTable, 
                                                                       final boolean distinct, 
                                                                       final Vector<QueryFieldPanel> qfps,
                                                                       final TableTree tblTree, 
                                                                       final RecordSetIFace keysToRetrieve) throws ParseException
    {
        if (qfps.size() == 0)
            return null;
        
        if (keysToRetrieve != null && keysToRetrieve.getNumItems() == 0)
            return null;
        
        StringBuilder fieldsStr = new StringBuilder();
        Vector<BaseQRI> list = new Vector<BaseQRI>();
        StringBuilder criteriaStr = new StringBuilder();
        StringBuilder orderStr = new StringBuilder();
        boolean debug = true;
        ProcessNode root = new ProcessNode(null);
        int fldPosition = distinct ? 0 : 1;

        for (QueryFieldPanel qfi : qfps)
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
            if (pqri instanceof RelQRI)
            {
                RelQRI relQRI = (RelQRI)pqri;
                RelationshipType relType = relQRI.getRelationshipInfo().getType();
                if (!relType.equals(RelationshipType.ManyToOne) 
                        && !relType.equals(RelationshipType.ManyToMany)/*treat manytomany as onetomany*/) //Maybe need to consider some types of OneToOne also?????????
                {
                    //parent will initially point to the related table
                    //and don't need to add related table unless it has children displayed/queried,
                    parent = parent.getParent();
                }
            }
            else
            {
                list.insertElementAt(pqri, 0);
            }
            while (parent != tblTree)
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
        TableAbbreviator tableAbbreviator = new TableAbbreviator();
        List<Pair<DBTableInfo,String>> fromTbls = new LinkedList<Pair<DBTableInfo,String>>();
        processTree(root, fromStr, fromTbls, 0, tableAbbreviator, tblTree);

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

        List<Pair<String,Object>> paramsToSet = new LinkedList<Pair<String, Object>>();
        for (QueryFieldPanel qfi : qfps)
        {
            if (qfi.isForDisplay())
            {
                String fldSpec = qfi.getFieldQRI().getSQLFldSpec(tableAbbreviator, false);
                if (StringUtils.isNotEmpty(fldSpec))
                {
                    if (fieldsStr.length() > 0)
                    {
                        fieldsStr.append(", ");
                    }
                    fieldsStr.append(fldSpec);
                }
            }
            if (keysToRetrieve == null || qfi.isEnforced())
            {
                String criteria = qfi.getCriteriaFormula(tableAbbreviator, paramsToSet);
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
        }
        sqlStr.append(fieldsStr);

        sqlStr.append(" from ");
        sqlStr.append(fromStr);

        if (keysToRetrieve != null)
        {
            if (!StringUtils.isEmpty(criteriaStr.toString()))
            {
                criteriaStr.append(" and ");
            }
            criteriaStr.append(tableAbbreviator.getAbbreviation(rootTable.getTableTree()) + "." 
                    + rootTable.getTableInfo().getIdFieldName() + " in(");
            boolean comma = false;
            for (RecordSetItemIFace item : keysToRetrieve.getOrderedItems())
            {
                if (comma)
                {
                    criteriaStr.append(",");
                }
                else
                {
                    comma = true;
                }
                criteriaStr.append(item.getRecordId());
            }
            criteriaStr.append(")");
        }
        else
        {
            //Assuming that this not necessary when keysToRetrieve is non-null because
            //the keys will already been filtered properly. (???)
            
        	// Add extra where's for system fields for each table in the from clause...
            boolean isRootTbl = true;
            for (Pair<DBTableInfo, String> fromTbl : fromTbls)
            {
                String specialColumnWhere = QueryAdjusterForDomain.getInstance().getSpecialColumns(
                        fromTbl.getFirst(), true,
                        !isRootTbl && true/* XXX should only use left join when necessary */, fromTbl.getSecond());
                isRootTbl = false;
                if (StringUtils.isNotEmpty(specialColumnWhere))
                {
                    if (criteriaStr.length() > 0)
                    {
                        criteriaStr.append(" AND ");
                    }
                    criteriaStr.append(specialColumnWhere);
                }
                //Actually, assuming data is valid, it should only be necessary to add the Adjustments for the root table?
                //XXX if this works, fix this loop. Also, join parameter code in getSpecialColumns will probably be irrelevant.
                break;
            }
            //...done adding system whereses
        }
        
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
        
        String result = sqlStr.toString();
        
        return new Pair<String, List<Pair<String, Object>>>(result, paramsToSet);
    }
    
    /**
     * Performs the Search by building the HQL String.
     */
    protected void doSearch(final TableQRI rootTable, boolean distinct)
    {
        try
        {
            Pair<String, List<Pair<String, Object>>> hql = buildHQL(rootTable, distinct, queryFieldItems, tableTree, null);    
            processSQL(queryFieldItems, hql.getFirst(), hql.getSecond(), rootTable.getTableInfo(), distinct);
        }
        catch (Exception ex)
        {
            UIRegistry.getStatusBar().setErrorMessage(ex.getLocalizedMessage(), ex);
            runningResults.set(null);
            completedResults.set(null);
        }
    }
    
    /**
     * @return Panel with up and down arrows for moving fields up and down in queryFieldsPanel.
     */
    protected JPanel buildMoverPanel(final boolean horizontal)
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
        
        PanelBuilder upDownPanel;
        if (horizontal)
        {
            upDownPanel = new PanelBuilder(new FormLayout("f:p:g, p, 2px, p, f:p:g","p"));        
            CellConstraints cc = new CellConstraints();
            upDownPanel.add(orderUpBtn,       cc.xy(2, 1));
            upDownPanel.add(orderDwnBtn,      cc.xy(4, 1));
        }
        else
        {
            upDownPanel = new PanelBuilder(new FormLayout("p", "f:p:g, p, 2px, p, f:p:g"));        
            CellConstraints cc = new CellConstraints();
            upDownPanel.add(orderUpBtn,       cc.xy(1, 2));
            upDownPanel.add(orderDwnBtn,      cc.xy(1, 4));
        }
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
     * @param node
     * @return " left join " unless it can be determined that data returned on other side of the join
     * cannot be null in which case " inner join " is returned.
     */
    protected static String getJoin(final ProcessNode node)
    {
        //XXX really should only use left join when necessary.
        return " left join ";
    }
    /**
     * @param parent
     * @param sqlStr
     * @param level
     */
    protected static void processTree(final ProcessNode parent, final StringBuilder sqlStr, final List<Pair<DBTableInfo,String>> fromTbls,
                               final int level, 
                               final TableAbbreviator tableAbbreviator, final TableTree tblTree)
    {
        BaseQRI qri = parent.getQri();
        if (qri != null && qri.getTableTree() != tblTree)
        {
            if (qri instanceof TableQRI)
            {
                TableTree tt = qri.getTableTree();
                String alias = tableAbbreviator.getAbbreviation(tt);
                fromTbls.add(new Pair<DBTableInfo, String>(tt.getTableInfo(), alias));
                if (level == 1)
                {
                    sqlStr.append(tt.getName());
                    sqlStr.append(' ');
                    sqlStr.append(alias);
                    sqlStr.append(' ');

                }
                else
                {
                    sqlStr.append(getJoin(parent));

                    sqlStr.append(tableAbbreviator.getAbbreviation(tt.getParent()));
                    sqlStr.append('.');
                    sqlStr.append(tt.getField());
                    sqlStr.append(' ');
                    sqlStr.append(alias);
                    sqlStr.append(' ');
                    
                }
                //XXX - should only use left joins when necessary (see 4th param below)
                String extraJoin = QueryAdjusterForDomain.getInstance().getJoinClause(tt.getTableInfo(), true, alias, true);
                
                if (StringUtils.isNotEmpty(extraJoin))
                {
                    sqlStr.append(extraJoin + " ");
                }
            }
        }
        for (ProcessNode kid : parent.getKids())
        {
            processTree(kid, sqlStr, fromTbls, level + 1, tableAbbreviator, tblTree);
        }
    }

    /**
     * @param fldName
     * @return fldName formatted for use as a field in a JasperReports data source or
     *  JasperReports data connection.
     */
    public static String fixFldNameForJR(final String fldName)
    {
        //not totally sure this is necessary.
        //other transformations may eventually be necessary.
        return fldName.trim().replaceAll(" ", "_");
    }
    
    /**
     * @param queryFieldItemsArg
     * @param fixLabels
     * @return ERTICaptionInfo for the visible columns returned by a query.
     */
    protected static List<ERTICaptionInfoQB> getColumnInfo(final Vector<QueryFieldPanel> queryFieldItemsArg, final boolean fixLabels)
    {
        List<ERTICaptionInfoQB> result = new Vector<ERTICaptionInfoQB>();
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
            if (qfp.isForDisplay())
            {
                String lbl = qfp.getLabel();
                if (fixLabels)
                {
                    lbl = fixFldNameForJR(lbl);
                }
                ERTICaptionInfoQB erti;
                if (qfp.getFieldQRI() instanceof RelQRI)
                {
                    erti = new ERTICaptionInfoRel(colName, lbl, true, qfp.getFieldQRI().getFormatter(), 0,
                            qfp.getStringId(),
                            ((RelQRI)qfp.getFieldQRI()).getRelationshipInfo());
                }
                else
                {
                    erti = new ERTICaptionInfoQB(colName, lbl, true, qfp.getFieldQRI().getFormatter(), 0, qfp.getStringId());
                }
                erti.setColClass(qfp.getFieldQRI().getDataClass());
                result.add(erti);
            }
        }
        return result;
    }
    
    /**
     * @param queryName
     * @param fixLabels
     * @return ERTICaptionInfo for the visible columns returned by query queryName.
     */
    public static List<ERTICaptionInfo> getColumnInfo(final String queryName, final boolean fixLabels)
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance()
        .createSession();
        try
        {
            SpQuery fndQuery = session.getData(SpQuery.class, "name", queryName,
                    DataProviderSessionIFace.CompareType.Equals);
            if (fndQuery == null)
            {
                throw new Exception("Unable to load query " + queryName);
            }
            return getColumnInfoSp(fndQuery.getFields(), fixLabels);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally
        {
            session.close();
        }
    }
    
    /**
     * @param queryFields
     * @param fixLabels
     * @return ERTICaptionInfo for the visible columns represented in an SpQuery's queryFields.
     */
    public static List<ERTICaptionInfo> getColumnInfoSp(final Set<SpQueryField> queryFields, final boolean fixLabels)
    {
        List<ERTICaptionInfo> result = new Vector<ERTICaptionInfo>();
        for (SpQueryField qf : queryFields)
        {
            if (qf.getContextTableIdent() != null)
            {
                DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(qf.getContextTableIdent());
                DBFieldInfo fi = ti.getFieldByColumnName(qf.getFieldName());
                String colName = ti.getAbbrev() + '.' + qf.getFieldName();
                if (qf.getIsDisplay())
                {
                    String lbl = qf.getColumnAliasTitle();
                    if (fixLabels)
                    {
                        lbl = lbl.replaceAll(" ", "_");
                        lbl = lbl.replaceAll("/", "_");
                    }
                    ERTICaptionInfo erti;
                    if (fi != null)
                    {
                        erti = new ERTICaptionInfoQB(colName, lbl, true, fi.getFormatter(), 0, qf.getStringId());
                        erti.setColClass(fi.getDataClass());
                    }
                    else
                    {
                        erti = new ERTICaptionInfoQB(colName, lbl, true, null, 0, qf.getStringId());
                        erti.setColClass(String.class);
                    }
                    result.add(erti);
                }
            }
            else log.error("null contextTableIdent for " + qf.getFieldName());
        }
        return result;
    }

    /**
     * @return list reports that use this.query as a data source.
     */
    protected List<SpReport> getReports()
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            //currently experiencing hibernate weirdness with loading of query's reports set so...
            //Object id = query.getId();
            return session.getDataList(SpReport.class, "query", query);
        }
        finally
        {
            session.close();
        }
    }
    
    /**
     * @param report
     * 
     * Loads and runs the query that acts as data source for report. Then runs report.
     */
    public static void runReport(final SpReport report, final String title, final RecordSetIFace rs)
    {
        final TableTree tblTree = readTables();
        final Hashtable<String, TableTree> ttHash = QueryBldrPane.buildTableTreeHash(tblTree);
        boolean go = true;
        QueryParameterPanel qpp = new QueryParameterPanel();
        qpp.setQuery(report.getQuery(), tblTree, ttHash);
        if (rs == null && qpp.getHasPrompts())
        {
            CustomDialog cd = new CustomDialog((Frame) UIRegistry.getTopWindow(), UIRegistry
                    .getResourceString("QB_GET_REPORT_CONTENTS_TITLE"), true, qpp);
            UIHelper.centerAndShow(cd);
            go = !cd.isCancelled(); // XXX what about x box?
            cd.dispose();
        }
        if (go)
        {
            TableQRI rootQRI = null;
            int cId = report.getQuery().getContextTableId();
            for (TableTree tt : ttHash.values())
            {
                if (cId == tt.getTableInfo().getTableId())
                {
                    rootQRI = tt.getTableQRI();
                    break;
                }
            }
            Vector<QueryFieldPanel> qfps = new Vector<QueryFieldPanel>(qpp.getFields());
            for (int f=0; f<qpp.getFields(); f++)
            {
                qfps.add(qpp.getField(f));
            }
            
            Pair<String, List<Pair<String, Object>>> sql = null;
            
            // XXX need to allow modification of SelectDistinct(etc) ???
            boolean includeRecordIds = true;
            
            try
            {
                sql = QueryBldrPane.buildHQL(rootQRI, !includeRecordIds, qfps, tblTree, rs);
            }
            catch (Exception ex)
            {
                UIRegistry.getStatusBar().setErrorMessage(ex.getLocalizedMessage(), ex);
                return;
            }
            QBJRDataSource src = new QBJRDataSource(sql.getFirst(), sql.getSecond(), getColumnInfo(qfps, true), includeRecordIds, report.getRepeats());
            
            final CommandAction cmd = new CommandAction(ReportsBaseTask.REPORTS,
                    ReportsBaseTask.PRINT_REPORT, src);
            cmd.setProperty("title", title); 
            cmd.setProperty("file", report.getName());
            CommandDispatcher.dispatch(cmd);
        }
    }
    
    /**
     * called when retrieved results have been displayed.
     */
    public void resultsComplete()
    {
        completedResults.set(runningResults.get());
        runningResults.set(null);
        new SwingWorker()
        {
            @Override
            public Object construct()
            {
                QueryBldrPane.this.searchBtn.setText(UIRegistry.getResourceString("QB_SEARCH")); 
                UIRegistry.getStatusBar().setText("");
                UIRegistry.getStatusBar().setIndeterminate(query.getName(), false);
                return null;
            }
        }.start();
    }
    
    /**
     * @return countOnly
     */
    public boolean isCountOnly()
    {
        return countOnly;
    }
    
    /**
     * Called when the db record retrieval task has completed.
     */
    public void queryTaskDone()
    {
        doneTime.set(System.nanoTime());
        if (runningResults.get() != null && runningResults.get().getQuery() != null)
        {
            if (!countOnly && !runningResults.get().getQuery().isCancelled() && !runningResults.get().getQuery().isInError())
            {
                final int results = runningResults.get().getQuery().getDataObjects().size();
                new SwingWorker()
                {
                    @Override
                    public Object construct()
                    {
                        String msg = String.format(UIRegistry
                                .getResourceString("QB_DISPLAYING_RETRIEVED_RESULTS"), String
                                .valueOf(results), String
                                .valueOf((doneTime.get() - startTime.get()) / 1000000000D));
                        UIRegistry.getStatusBar().setText(msg);
                        UIRegistry.getStatusBar().setIndeterminate(query.getName(), true);
                        return null;
                    }

                }.start();
            }
            if (countOnly || runningResults.get().getQuery().isCancelled() || runningResults.get().getQuery().isInError())
            {
                new SwingWorker()
                {
                    @Override
                    public Object construct()
                    {
                        QueryBldrPane.this.searchBtn.setText(UIRegistry.getResourceString("QB_SEARCH")); 
                        UIRegistry.getStatusBar().setText("");
                        UIRegistry.getStatusBar().setIndeterminate(query.getName(), false);
                        return null;
                    }
                }.start();            
            }
            if (countOnly && !runningResults.get().getQuery().isCancelled() && !runningResults.get().getQuery().isInError())
            {
                UIRegistry.showLocalizedMsg("QB_COUNT_TITLE", "QB_COUNT_MSG", String.valueOf(runningResults.get().getQuery().getDataObjects().size()));
            }
        }
        
    }
    
    /**
     * @param queryFieldItemsArg
     * @param sql
     * @param params
     * @param rootTable
     * @param distinct
     */
    protected void processSQL(final Vector<QueryFieldPanel>    queryFieldItemsArg, 
                              final String                     sql, 
                              final List<Pair<String, Object>> params, 
                              final DBTableInfo                rootTable, 
                              final boolean                    distinct)
    {
        List<? extends ERTICaptionInfo> captions = getColumnInfo(queryFieldItemsArg, false);
        List<Integer> list = new Vector<Integer>();
        
        String iconName = distinct ? "BlankIcon" : rootTable.getClassObj().getSimpleName();
        int tblId = distinct ? -1 : rootTable.getTableId();
        QBQueryForIdResultsHQL qri = new QBQueryForIdResultsHQL(new Color(144, 30, 255),
                getResourceString("QB_SEARCH_RESULTS"),
                //rootTable.getTableInfo().getClassObj().getSimpleName(), // Icon Name
                iconName,
                //rootTable.getTableInfo().getTableId(), // table id
                tblId,
                "", // search term
                list,
                this);
        
        qri.setSQL(sql);
        qri.setParams(params);
        //XXX check generics reference book.
        qri.setCaptions((List<ERTICaptionInfo> )captions);
        qri.setExpanded(true);
        runningResults.set(qri);
        doneTime.set(-1);
        new SwingWorker(){
            @Override
            public Object construct()
            {
                if (runningResults.get() != null && !runningResults.get().getCancelled())
                {
                    searchBtn.setText(UIRegistry.getResourceString("QB_CANCEL")); 
                    UIRegistry.getStatusBar().setText("Searching..."); //XXX i18n
                    UIRegistry.getStatusBar().setIndeterminate(query.getName(), true);
                }
                //else the query got cancelled or crashed before this thread was executed
                return null;
            }
        }.start();
        
        if (esrp == null)
        {
            CommandAction cmdAction = new CommandAction("Express_Search", "HQL", qri);
            cmdAction.setProperty("reuse_panel", true); 
            CommandDispatcher.dispatch(cmdAction);
        } else
        {
            esrp.addSearchResults(qri);
        }
        //cancelSearch();
        startTime.set(System.nanoTime());
    }

    /**
     * @param pn
     * @param lvl
     */
    protected static void printTree(ProcessNode pn, int lvl)
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
    
    protected SpQuery cloneTheQuery()
    {
        SpQuery result = new SpQuery();
        result.initialize();
        result.setSpecifyUser(AppContextMgr.getInstance().getClassObject(SpecifyUser.class));
        result.setName(query.getName());
        result.setContextTableId(query.getContextTableId());
        result.setContextName(query.getContextName());
        //result.setCreatedByAgent(query.getCreatedByAgent());
        for (QueryFieldPanel qfp : queryFieldItems)
        {
            SpQueryField qf = qfp.getQueryField();
            if (qf == null) { throw new RuntimeException("Shouldn't get here!"); }
            SpQueryField newQf = new SpQueryField();
            newQf.initialize();
            newQf.setFieldName(qf.getFieldName());
            newQf.setPosition(qf.getPosition());
            qfp.updateQueryField(newQf);
            result.addReference(newQf, "fields");
        }
        return result;
    }

    /**
     * 
     */
    protected boolean saveQuery(final boolean saveAs)
    {
        if (!query.isNamed() || saveAs)
        {
            if (!getQueryNameFromUser(saveAs))
            {
                return false;
            }
        }
        
        TableQRI tableQRI = (TableQRI) tableList.getSelectedValue();
        if (tableQRI != null)
        {
            short position = 0;
            
            Set<Integer> queryFldsWithoutPanels = new HashSet<Integer>();
            for (SpQueryField qf : query.getFields())
            {
            	queryFldsWithoutPanels.add(qf.getId());
            }
            
            for (QueryFieldPanel qfp : queryFieldItems)
            {
                SpQueryField qf = qfp.getQueryField();
                if (qf == null) { throw new RuntimeException("Shouldn't get here!"); }
                queryFldsWithoutPanels.remove(qf.getId());
                qf.setPosition(position);
                qfp.updateQueryField();

                position++;
            }

            //Remove query fields for which panels could be created in order to prevent
            //repeat of missing fld message in getQueryFieldPanels() whenever this query is loaded.
            for (Integer qfId : queryFldsWithoutPanels)
            {
            	//this is real lame but should hardly ever need to be executed
            	for (SpQueryField qf : query.getFields())
            	{
            		if (qfId.equals(qf.getId()))
            		{
            			query.getFields().remove(qf);
            			break;
            		}
            	}
            }
            
            if (query.getSpQueryId() == null || saveAs)
            {
                if (query.getSpQueryId() != null && saveAs)
                {
                    query = cloneTheQuery();
                    queryNavBtn.setEnabled(true);
                }
                
                queryNavBtn = ((QueryTask) task).saveNewQuery(query, false); // false tells it to
                                                                                // disable the
                                                                                // navbtn

                query.setNamed(true);
                if (query.getSpQueryId() != null && saveAs)
                {
                    this.setQueryIntoUI();
                }   
                else
                {
                    SubPaneMgr.getInstance().renamePane(this, query.getName());
                }
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
    protected boolean getQueryNameFromUser(final boolean saveAs)
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
                //if (QueryTask.askUserForInfo("Query", getResourceString("QB_DATASET_INFO"), query))
                //Using above method causes Hibernate 'Dirty Collection' issues for save as.
                //Currently the only info involved is the name so using a simple dialog box is good enuf.
                JTextField nameText = new JTextField(newQueryName);
                JLabel nameLbl = UIHelper.createLabel(UIRegistry.getResourceString("QB_Q_NAME_PROMPT"));
                PanelBuilder pane = new PanelBuilder(new FormLayout("4dlu, p, 2dlu, fill:p:grow, 4dlu", "5dlu, p, 5dlu"));
                CellConstraints cc = new CellConstraints();
                pane.add(nameLbl, cc.xy(2, 2));
                pane.add(nameText, cc.xy(4, 2));
                CustomDialog cd = new CustomDialog((Frame)UIRegistry.getTopWindow(), 
                        saveAs ? UIRegistry.getResourceString("QB_SAVE_Q_AS_TITLE") : UIRegistry.getResourceString("QB_SAVE_Q_TITLE"), 
                        true, pane.getPanel());
                UIHelper.centerAndShow(cd);
                if (!cd.isCancelled())
                {
                    //newQueryName = query.getName();
                    newQueryName = nameText.getText();
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
                            query.setName(newQueryName);
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
                if (!tblQRI.getField(f).isFieldHidden())
                {
                    model.addElement(tblQRI.getField(f));
                }
            }
            for (int k = 0; k < tblQRI.getTableTree().getKids(); k++)
            {
                boolean addIt;
                TableTree kidK = tblQRI.getTableTree().getKid(k);
                if (kidK.isAlias())
                {
                    if (!fixAliases(kidK, tableTreeHash))
                    {
                        addIt = false;
                    }
                    else
                    {
                        addIt = tblIsDisplayable(kidK, tableTreeHash.get(kidK.getName()).getTableInfo());
                    }
                }
                else
                {
                    addIt = !kidK.getTableInfo().isHidden() && tblIsDisplayable(kidK, kidK.getTableInfo());
                }
                if (addIt)
                {
                    if (kidK.getTableQRI().getRelationship() == null || !kidK.getTableQRI().getRelationship().isHidden())
                    {
                        model.addElement(tblQRI.getTableTree().getKid(k).getTableQRI());
                    }
                }
            }
        }
    }

    /**
     * @param aliasTbl
     * @param tblInfo
     * @return true if aliasTbl should be displayed in the fields list for the current context.
     */
    protected static boolean tblIsDisplayable(final TableTree aliasTbl, final DBTableInfo tblInfo)
    {
        /*
        if (aliasTbl.isAlias())
        {
            return !isCyclic(aliasTbl, tblInfo.getTableId()) || isCyclicable(aliasTbl, tblInfo);
        }
        //else
        return true;
        */
        
        return !isCyclic(aliasTbl, tblInfo.getTableId()) || isCyclicable(aliasTbl, tblInfo);
        
    }
    
    /**
     * @param alias
     * @param tblId
     * @return true if the specified alias represents a table that is already
     * present in the alias' tabletree.
     */
    protected static boolean isCyclic(final TableTree alias, final int tblId)
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
    protected static boolean isCyclicable(final TableTree alias, final DBTableInfo tblInfo)
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
                                JList list = (JList)e.getSource();
                                QryListRendererIFace qriFace = (QryListRendererIFace) list.getSelectedValue();
                                if (BaseQRI.class.isAssignableFrom(qriFace.getClass()))
                                {
                                    BaseQRI qri = (BaseQRI) qriFace;
                                    if (qri.isInUse())
                                    {
                                        //remove the field
                                        for (QueryFieldPanel qfp : QueryBldrPane.this.queryFieldItems)
                                        {
                                            FieldQRI fqri = qfp.getFieldQRI();
                                            if (fqri == qri || (fqri instanceof RelQRI && fqri.getTable() == qri))
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
                                        SpQueryField qf = new SpQueryField();
                                        qf.initialize();
                                        qf.setFieldName(fieldQRI.getFieldName());
                                        query.addReference(qf, "fields");
                                        addQueryFieldItem(fieldQRI, qf, false);
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
                String colHeaderText = "Query Fields";
                if (item instanceof TableQRI)
                {
                    colHeaderText = ((TableQRI)item).getTitle();
                }
                JLabel colHeader = UIHelper.createLabel(colHeaderText);
                colHeader.setHorizontalAlignment(SwingConstants.CENTER);
                sp.setColumnHeaderView(colHeader);
                
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
                JLabel colHeaderLbl = (JLabel)sp.getColumnHeader().getComponent(0);
                if (item instanceof TableQRI)
                {
                    colHeaderLbl.setText(((TableQRI)item).getTitle());
                }
                else
                {
                    colHeaderLbl.setText("Query Fields");
                }
            }

            if (item instanceof TableQRI)
            {
                createNewList((TableQRI)item, model);

            }
            listBoxPanel.remove(addBtn);
            listBoxPanel.add(sp);
            listBoxPanel.add(addBtn);
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

    
//    /**
//     * @param rel
//     * @param classObj
//     * @return false if rel represents a 'system' relationship.
//     */
//    protected static boolean isRelevantRel(final DBRelationshipInfo rel, final Class<?> classObj)
//    {
//        if (classObj.equals(edu.ku.brc.specify.datamodel.Agent.class))
//        {
//            return rel.getColName() == null ||
//                (!rel.getColName().equalsIgnoreCase("modifiedbyagentid") &&
//                 !rel.getColName().equalsIgnoreCase("createdbyagentid"));
//        }
//        return true;
//    }
    
    /**
     * @param qri
     * @return qri if it is already a FieldQRI, else constructs a RelQRI and returns it.
     */
    protected static FieldQRI buildFieldQRI(final BaseQRI qri)
    {
        if (qri instanceof FieldQRI) { return (FieldQRI) qri; }
        if (qri instanceof TableQRI)
        {
            DBRelationshipInfo relInfo = ((TableQRI)qri).getRelationship();
            if (relInfo != null)
            {
                return new RelQRI((TableQRI) qri, relInfo);
            }
            throw new RuntimeException(QueryBldrPane.class.getName() + ": unable to determine relationship.");
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
            BaseQRI qri = (BaseQRI) listBoxList.get(currentInx)
                    .getSelectedValue();
            addBtn.setEnabled(qri != null && !qri.isInUse());
        }
    }

    /**
     * Removes it from the List.
     * 
     * @param qfp QueryFieldPanel to be added
     */
    public void removeQueryFieldItem(final QueryFieldPanel qfp)
    {
        query.forceLoad(true);
        if (query.getReports().size() > 0)
        {
            CustomDialog cd = new CustomDialog((Frame )UIRegistry.getTopWindow(), UIRegistry.getResourceString("REP_CONFIRM_DELETE_TITLE"),
                    true, new QBReportInfoPanel(query, UIRegistry.getResourceString("QB_USED_BY_REP_FLD_DELETE_CONFIRM")));
            UIHelper.centerAndShow(cd);
            cd.dispose();
            if (cd.isCancelled())
            {
                return;
            }
        }
        qfp.getFieldQRI().setIsInUse(false);
        if (qfp.getQueryField() != null)
        {
            query.removeReference(qfp.getQueryField(), "fields");
        }
        queryFieldItems.remove(qfp);
        qualifyFieldLabels();

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
                    BaseQRI qri = qfp.getFieldQRI() instanceof RelQRI ? qfp.getFieldQRI()
                            .getTable() : qfp.getFieldQRI();
                    for (JList lb : listBoxList)
                    {
                        if (lb.isVisible())
                        {
                            for (int i = 0; i < ((DefaultListModel) lb.getModel()).getSize(); i++)
                            {
                                if (((DefaultListModel) lb.getModel()).getElementAt(i) == qri)
                                {
                                    lb.repaint();
                                }
                            }
                        }
                    }
                }
                catch (ArrayIndexOutOfBoundsException ex)
                {
                    log.error(ex);
                }
                queryFieldsPanel.repaint();
                saveBtn.setEnabled(QueryBldrPane.this.queryFieldItems.size() > 0);
                updateSearchBtn();
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
    protected static FieldQRI getFieldQRI(final TableTree tbl,
                                   final SpQueryField field,
                                   final int[] tableIds,
                                   final int level,
                                   final Hashtable<String, TableTree> ttHash)
    {
        int id = tableIds[level];
        for (int k=0; k<tbl.getKids(); k++)
        {
            TableTree kid = tbl.getKid(k);
            boolean checkKid = true;
            if (kid.isAlias()) 
            {
                checkKid = fixAliases(kid, ttHash);
            }
            if (checkKid)
            {
                if (kid.getTableInfo().getTableId() == id)
                {
                    if (level == (tableIds.length - 1))
                    {
                        if (field.getIsRelFld() == null || !field.getIsRelFld())
                        {
                            for (int f = 0; f < kid.getTableQRI().getFields(); f++)
                            {
                                if (kid.getTableQRI().getField(f).getFieldName().equals(
                                    field.getFieldName())) { return kid.getTableQRI().getField(f); }
                            }
                        }
                        else
                        {
                            return buildFieldQRI(kid.getTableQRI());
                        }
                    }
                    else
                    {
                        FieldQRI fi = getFieldQRI(kid, field, tableIds, level + 1, ttHash);
                        if (fi != null) { return fi; }
                    }
                }
            }
        }
        return null;
    }

    protected static Vector<QueryFieldPanel> getQueryFieldPanels(final QueryFieldPanelContainerIFace container, 
            final Set<SpQueryField> fields, final TableTree tblTree, 
            final Hashtable<String,TableTree> ttHash,
            final Component saveBtn)
    {
        Vector<QueryFieldPanel> result = new Vector<QueryFieldPanel>();
        List<SpQueryField> orderedFlds = new ArrayList<SpQueryField>(fields);
        Collections.sort(orderedFlds);
        result.add(bldQueryFieldPanel(container, null, null, container.getColumnDefStr(), saveBtn));
        for (SpQueryField fld : orderedFlds)
        {
            FieldQRI fieldQRI = getFieldQRI(tblTree, fld, fld.getTableIds(), 0, ttHash);
            if (fieldQRI != null)
            {
                result.add(bldQueryFieldPanel(container, fieldQRI, fld, container.getColumnDefStr(), saveBtn));
                fieldQRI.setIsInUse(true);
                if (fieldQRI.isFieldHidden() && !container.isPromptMode())
                {
                	UIRegistry.showLocalizedMsg("QB_FIELD_HIDDEN_TITLE", "QB_FIELD_HIDDEN_SHOULD_REMOVE", fieldQRI.getTitle());
                }
            }
            else
            {
            	if (!container.isPromptMode())
            	{
            		UIRegistry.showLocalizedMsg("QB_FIELD_MISSING_TITLE", "QB_FIELD_NOT_ADDED", fld.getColumnAlias());
            	}
            	log.error("Couldn't find [" + fld.getFieldName() + "] [" + fld.getTableList()
                        + "]");
            }
        }
        return result;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#getColumnDefStr()
     */
    //@Override
    public String getColumnDefStr()
    {
        return columnDefStr;
    }

    protected static QueryFieldPanel bldQueryFieldPanel(final QueryFieldPanelContainerIFace container,
                                                        final FieldQRI fieldQRI,
                                                        final SpQueryField fld,
                                                        String colDefStr,
                                                        final Component saveBtn)
    {
        if (colDefStr == null) { return new QueryFieldPanel(container, fieldQRI,
                IconManager.IconSize.Std24, colDefStr, saveBtn, null); }
        return new QueryFieldPanel(container, fieldQRI, IconManager.IconSize.Std24, colDefStr,
                saveBtn, fld);
    }
    
    /**
     * Add QueryFieldItem to the list created with a TableFieldPair.
     * 
     * @param fieldItem the TableFieldPair to be in the list
     */
    protected void addQueryFieldItem(final FieldQRI fieldQRI, final SpQueryField queryField, final boolean loading)
    {
        if (fieldQRI != null)
        {            
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
                        queryFieldsPanel.add(qfp);
                        queryFieldsPanel.validate();
                        if (fieldQRI instanceof RelQRI)
                        {
                            BaseQRI qri = fieldQRI.getTable();
                            for (JList lb : listBoxList)
                            {
                                if (lb.isVisible())
                                {
                                    if (((DefaultListModel) lb.getModel()).contains(qri))
                                    {
                                        lb.repaint();
                                    }
                                }
                            }
                        }
                        else
                        {
                            listBoxList.get(currentInx).repaint();
                        }
                        
                        updateAddBtnState();
                        selectQFP(qfp);
                        queryFieldsPanel.repaint();
                        if (!loading)
                        {
                            saveBtn.setEnabled(true);
                            updateSearchBtn();
                        }
                        //Sorry, but a new context can't be selected if any fields are selected from the current context.
                        tableList.setEnabled(queryFieldItems.size() == 0);
                    }
                }
            });
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#selectQFP(edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanel)
     * 
     * Use runSelectQFP if not calling from Swing thread.
     */
    public void selectQFP(final QueryFieldPanel qfp)
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
                selectedQFP.setSelected(false);
                selectedQFP.repaint();
            }
            selectedQFP = qfp;
            if (selectedQFP != null)
            {
                selectedQFP.setSelected(true);
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
        List<String> labels = new ArrayList<String>(queryFieldItems.size());
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
    protected static void processForTables(final Element parent,
                                    final TableTree parentTT)
    {
        String tableName = XMLHelper.getAttr(parent, "name", null);
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
                	TreeDefIface<?, ?, ?> treeDef = null;
                    DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                    try
                    {
                        DataModelObjBase tempdisc = (DataModelObjBase )AppContextMgr.getInstance().getClassObject(Discipline.class);
                        Discipline disc = (Discipline )session.get(tempdisc.getDataClass(), tempdisc.getId());
                        treeDef = disc.getTreeDef(UploadTable.capitalize(tableInfo.getClassObj().getSimpleName())
                                + "TreeDef");
                    }
                    catch (Exception ex)
                    {
                        throw new RuntimeException(ex);
                    }
                    finally
                    {
                        session.close();
                    }
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
    protected static boolean fixAliases(final TableTree tbl, final Hashtable<String, TableTree> hash)
    {
        if (tbl.isAlias())
        {
            TableTree tt = hash.get(tbl.getName());
            if (tt != null)
            {
                if (!tt.getTableInfo().isHidden() && tblIsDisplayable(tbl, tt.getTableInfo()))
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
    protected static TableTree readTables()
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

    protected static Hashtable<String, TableTree> buildTableTreeHash(final TableTree treeRoot)
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

        CommandDispatcher.unregister(ReportsBaseTask.REPORTS, this);
        
        if (saveBtn != null && saveBtn.isEnabled())
        {
            saveBtn.setEnabled(false);
        }
        query = null;
        if (queryNavBtn != null)
        {
            queryNavBtn.setEnabled(true);
        }
        
        /*NOTE: runningResults or completedResults may still be pointing to this so
        garbage collection will be hampered, but since only one QueryResult is allowed
        at any time it should not be a problem. (???)
        */
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
                saveQuery(false);
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#getFields()
     */
    public int getFields()
    {
        return queryFieldItems.size();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#getField(int)
     */
    public QueryFieldPanel getField(int index)
    {
        return queryFieldItems.get(index);
    }
    
    /**
     * Disables search button when there is nothing to search for.
     */
    protected void updateSearchBtn()
    {
        searchBtn.setEnabled(queryFieldItems.size() > 0);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#isPromptMode()
     */
    //@Override
    public boolean isPromptMode()
    {
        return false;
    }

    protected boolean canSearch()
    {
        if (runningResults.get() == null)
        {
            return true;
        }
//        if (runningResults.get().getQueryTask() == null)
//        {
//            //something has gone wrong?
//            runningResults.set(null);
//            return true;
//        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CommandListener#doCommand(edu.ku.brc.ui.CommandAction)
     */
    //@Override
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isType(ReportsBaseTask.REPORTS)) 
        {
            refreshQuery(); //currently done for all commands
            if (cmdAction.isAction(ReportsBaseTask.REFRESH))
            {
                //nothing else to do
            }
            else if (cmdAction.isAction(ReportsBaseTask.REPORT_DELETED))
            {
                if (runningResults.get() != null)
                {
                    runningResults.get().reportDeleted((Integer)cmdAction.getData());
                }
                if (completedResults.get() != null)
                {
                    completedResults.get().reportDeleted((Integer)cmdAction.getData());
                }
            }
        }
    }
    
    protected void refreshQuery()
    {
        if (query != null && query.getId() != null)
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance()
            .createSession();
            try
            {
                session.refresh(query);
                query.forceLoad(true);
            }
            finally
            {
                session.close();
            }
        }
    }
    
    public Set<SpReport> getReportsForQuery()
    {
        //assuming query.forceLoad() has been called
        return query.getReports();
    }
}


