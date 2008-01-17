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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.NavBoxLayoutManager;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.expresssearch.ERTICaptionInfo;
import edu.ku.brc.af.core.expresssearch.TableFieldPair;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpQueryField;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.tasks.QueryTask;
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
    protected Hashtable<DBTableInfo, Vector<TableFieldPair>> tableFieldList   = new Hashtable<DBTableInfo, Vector<TableFieldPair>>();

    protected Vector<QueryFieldPanel>                        queryFieldItems  = new Vector<QueryFieldPanel>();
    protected int                                            currentInx       = -1;
    protected JPanel                                         queryFieldsPanel;

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
    protected JButton                                        saveBtn;

    protected Hashtable<String, Boolean>                     fieldsToSkipHash = new Hashtable<String, Boolean>();
    protected QryListRenderer                                qryRenderer      = new QryListRenderer(
                                                                                      IconManager.IconSize.Std16);
    protected int                                            listCellHeight;

    protected TableTree                                      tableTree;
    protected boolean                                        processingLists  = false;

    protected RolloverCommand                                queryNavBtn      = null;

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
                saveQuery();
                saveBtn.setEnabled(false);
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
                // fieldList.setSelectedIndex(-1);
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

        JPanel contextPanel = new JPanel(new BorderLayout());
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
        JScrollPane sp = new JScrollPane(queryFieldsPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null);
        add(sp, BorderLayout.CENTER);

        JButton searchBtn = new JButton("Search");
        final JCheckBox distinctChk = new JCheckBox("distinct", false);
        PanelBuilder outer = new PanelBuilder(new FormLayout("f:p:g,p", "p"));
        PanelBuilder builder = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g,f:p:g", "p"));
        CellConstraints cc = new CellConstraints();
        builder.add(searchBtn, cc.xy(2, 1));
        builder.add(distinctChk, cc.xy(3, 1));

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

    public int getCurrentContextTableId()
    {
        TableQRI currentTableQRI = (TableQRI) tableList.getSelectedValue();
        return currentTableQRI.getTableInfo().getTableId();
    }

    /**
     * @param fieldName
     * @return
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
        queryFieldsPanel.removeAll();
        queryFieldItems.clear();
        queryFieldsPanel.validate();
        columnDefStr = null;

        for (JList list : listBoxList)
        {
            DefaultListModel model = (DefaultListModel) list.getModel();
            for (int i = 0; i < model.size(); i++)
            {
                BaseQRI bq = (BaseQRI) model.get(i);
                bq.setIsInUse(false);
            }
        }

        tableList.clearSelection();

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
                        Vector<SpQueryField> fields = new Vector<SpQueryField>(query.getFields());
                        Collections.sort(fields);
                        for (SpQueryField field : fields)
                        {
                            addQueryFieldItem(field);
                        }
                        break;
                    }
                }
            }
        }

        for (QueryFieldPanel qfp : queryFieldItems)
        {
            qfp.resetValidator();
        }
        saveBtn.setEnabled(false);

        this.validate();
    }

    protected boolean includeObjKey()
    {
        return false;
    }
    
    public int getFldPosition(final QueryFieldPanel qfp)
    {
        for (int p=0; p<queryFieldItems.size(); p++)
        {
            if (queryFieldItems.get(p) == qfp)
            {
                return includeObjKey() ? p+1 : p;
            }
        }
        return -1; //oops
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

    /**
     * Performs the Search by building the HQL String.
     */
    protected void doSearch(final TableQRI rootTable, boolean distinct)
    {
        if (queryFieldItems.size() > 0)
        {
            StringBuilder fieldsStr = new StringBuilder();
            if (!distinct)
            {
                fieldsStr.append(rootTable.getTableTree().getAbbrev());
                fieldsStr.append(".");
                fieldsStr.append(rootTable.getTableInfo().getIdFieldName());
            }
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
                    if (fieldsStr.length() > 0)
                        fieldsStr.append(", ");
                    fieldsStr.append(qfi.getFieldQRI().getSQLFldSpec());
                    fldPosition++;
                }
                
                FieldQRI pqri = qfi.getFieldQRI();
                if (debug)
                {
                    System.out.println("\nNode: " + qfi.getFieldName());
                }
                String criteria = qfi.getCriteriaFormula();
                boolean isDisplayOnly = StringUtils.isEmpty(criteria);
                if (!isDisplayOnly)
                {
                    if (!isDisplayOnly && criteriaStr.length() > 0)
                    {
                        criteriaStr.append(" AND ");
                    }
                    criteriaStr.append(criteria);
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
                TableTree parent = qfi.getFieldQRI().getTableTree();
                list.clear();
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
                    if (!parentNode.contains(qri))
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
                            if (!kidNode.contains(qri))
                            {
                                parentNode = kidNode;
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

            StringBuilder sqlStr = new StringBuilder();
            sqlStr.append("select ");
            if (distinct)
            {
                sqlStr.append(" distinct ");
            }
            sqlStr.append(fieldsStr);
            sqlStr.append(" from ");

            processTree(root, sqlStr, 0);

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

            System.out.println(sqlStr.toString());

            processSQL(queryFieldItems, sqlStr.toString(), rootTable.getTableInfo(), distinct);

        }
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
                System.out.println("processTree " + tt.getName());
                if (level == 1)
                {
                    sqlStr.append(tt.getName());
                    sqlStr.append(' ');
                    sqlStr.append(tt.getAbbrev());
                    sqlStr.append(' ');

                }
                else
                {
                    // really should only use left join when necessary...
                    sqlStr.append(" left join ");

                    sqlStr.append(tt.getParent().getAbbrev());
                    sqlStr.append('.');
                    sqlStr.append(tt.getField());
                    sqlStr.append(' ');
                    sqlStr.append(tt.getAbbrev());
                    sqlStr.append(' ');
                }
            }
        }
        for (ProcessNode kid : parent.getKids())
        {
            processTree(kid, sqlStr, level + 1);
        }
    }

    /**
     * @param queryFieldItemsArg
     * @param sql
     */
    protected void processSQL(final Vector<QueryFieldPanel> queryFieldItemsArg, final String sql, final DBTableInfo rootTable, final boolean distinct)
    {
        List<ERTICaptionInfo> captions = new Vector<ERTICaptionInfo>();
        for (QueryFieldPanel qfp : queryFieldItemsArg)
        {
            DBFieldInfo fi = qfp.getFieldInfo();
            DBTableInfo ti = null;
            if (fi != null)
               ti = fi.getTableInfo();
            String colName = qfp.getFieldName();
            if (ti != null && fi != null)
            {
                colName = ti.getAbbrev() + '.' + fi.getColumn();
            }
            if (qfp.isForDisplay() && qfp.isDisplayable())
            {
                ERTICaptionInfo erti = new ERTICaptionInfo(colName, qfp.getFieldQRI().getTitle(), true, qfp.getFieldQRI().getFormatter(), 0);
                erti.setColClass(qfp.getFieldQRI().getDataClass());
                captions.add(erti);
            }
        }
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
    protected void saveQuery()
    {
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

            }
            else
            {
                try
                {
                    DataProviderSessionIFace session = DataProviderFactory.getInstance()
                            .createSession();
                    session.beginTransaction();
                    session.saveOrUpdate(query);
                    session.commit();
                    session.close();

                }
                catch (Exception ex)
                {
                    log.error(ex);
                    ex.printStackTrace();
                }
            }
            
        }
        else
        {
            log.error("No Context selected!");
        }
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
                if (pn.getQri() == qriArg) { return true; }
            }
            return false;
        }
    }

    /**
     * @param parentQRI
     * @param tableTree
     * @param model
     */
    protected void createNewList(final TableQRI tblQRI,
                                 final DefaultListModel model)
    {
        model.clear();
        if (tblQRI != null)
        {
            for (int f=0; f<tblQRI.getFields(); f++)
            {
                model.addElement(tblQRI.getField(f));
            }
            for (int k=0; k<tblQRI.getTableTree().getKids(); k++)
            {
                if (!tblQRI.getTableTree().getKid(k).isAlias())
                    model.addElement(tblQRI.getTableTree().getKid(k).getTableQRI());
            }
        }
    }

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
        queryFieldItems.remove(qfp);
        queryFieldsPanel.getLayout().removeLayoutComponent(qfp);
        queryFieldsPanel.remove(qfp);
        qfp.getFieldQRI().setIsInUse(false);
        queryFieldsPanel.validate();

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    listBoxList.get(currentInx).repaint();
                } catch (ArrayIndexOutOfBoundsException ex)
                {
                    log.error(ex);
                }
                queryFieldsPanel.repaint();
                saveBtn.setEnabled(true);
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
        System.out.println("getFieldQRI id[" + id + "] level[" + level + "]");
        for (int k=0; k<tbl.getKids(); k++)
        {
            TableTree kid = tbl.getKid(k);
            System.out.println("checking id[" + id + "] [" + kid.getTableInfo().getTableId() + "]");
            if (kid.getTableInfo().getTableId() == id)
            {
                if (level == (tableIds.length - 1))
                {
                    for (int f=0; f<kid.getTableQRI().getFields(); f++)
                    {
                        if (kid.getTableQRI().getField(f).getFieldName().equals(field.getFieldName())) 
                        { 
                            return kid.getTableQRI().getField(f); 
                        }
                    }
                }
                else
                {
                    FieldQRI fi = getFieldQRI(kid, field, tableIds, level + 1);
                    if (fi != null) { return fi; }
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
            System.out.println(field.getTableList());
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
            if (queryFieldItems.size() == 0 && queryFieldsPanel.getComponentCount() == 0)
            {
                QueryFieldPanel qfp = new QueryFieldPanel(this, fieldQRI,
                        IconManager.IconSize.Std24, columnDefStr, saveBtn, null);
                queryFieldsPanel.add(qfp);
            }

            final QueryFieldPanel qfp = new QueryFieldPanel(this, fieldQRI,
                    IconManager.IconSize.Std24, columnDefStr, saveBtn, queryField);
            queryFieldsPanel.add(qfp);
            queryFieldItems.add(qfp);
            fieldQRI.setIsInUse(true);
            queryFieldsPanel.validate();

            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    if (currentInx > -1)
                    {
                        listBoxList.get(currentInx).repaint();
                        queryFieldsPanel.repaint();
                        qfp.repaint();
                        updateAddBtnState();
                    }
                }
            });
        }
    }

    /**
     * @param parent
     * @param treeNodes
     */
    protected void processForAliases(final Element parent, final Vector<TableTree> treeNodes)
    {
        @SuppressWarnings("unused")
        TableTree treeNode = null;
        String nameStr = XMLHelper.getAttr(parent, "name", null);
        for (TableTree tt : treeNodes)
        {
            if (tt.getName().equals(nameStr))
            {
                treeNode = tt;
                break;
            }
        }

        // if (treeNode != null)
        // {
        for (Object obj : parent.selectNodes("alias"))
        {
            Element kidElement = (Element) obj;
            processForAliases(kidElement, treeNodes);
        }
        // }
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
            

            List<?> treeLevels = parent.selectNodes("treelevel");
            if (treeLevels.size() > 0 && !Treeable.class.isAssignableFrom(tableInfo.getClassObj()))
            {
                log.error("ignoring treelevel specified for non-Treeable table");
            }
            else
            {
                for (Object levelObj : treeLevels)
                {
                    try
                    {
                        newTreeNode.getTableQRI().addField(new TreeLevelQRI(newTreeNode.getTableQRI(), null, Integer.valueOf(XMLHelper
                            .getAttr((Element) levelObj, "rank", "0"))));
                    }
                    catch (Exception ex)
                    {
                        //if there is no TreeDefItem for the rank then just skip it.
                        if (ex instanceof TreeLevelQRI.NoTreeDefItemException)
                        {
                            log.error(ex);
                        }
                        //else something is really messed up
                        else
                        {
                            throw new RuntimeException(ex);
                        }
                    }
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
                    newTreeNode.addKid(new TableTree(kidClassName, true));
                }
            }
        }
    }

    /**
     * @param tbl
     * @param hash
     */
    protected void fixAliases(final TableTree tbl, Hashtable<String, TableTree> hash)
    {
        if (tbl.isAlias())
        {
            TableTree tt = hash.get(tbl.getName());
            if (tt != null)
            {
                try
                {
                    tbl.setField(tt.getField());
                    for (int k = 0; k < tt.getKids(); k++)
                    {
                        tbl.addKid((TableTree) tt.getKid(k).clone());
                    }
                    tbl.setTableInfo(tt.getTableInfo());
                    tbl.setTableQRIClone(tt.getTableQRI());
                    tbl.setAlias(false);
                }
                catch (CloneNotSupportedException ex)
                {
                    throw new RuntimeException(ex);
                }
            }
            else
            {
                log.error("Couldn't find [" + tbl.getName() + "] in the hash.");
            }
        }
        else
        {
            for (int k = 0; k < tbl.getKids(); k++)
            {
                fixAliases(tbl.getKid(k), hash);
            }
        }
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

            Hashtable<String, TableTree> hash = new Hashtable<String, TableTree>();
            for (int t=0; t<treeRoot.getKids(); t++)
            {
                TableTree tt = treeRoot.getKid(t);
                hash.put(tt.getName(), tt);
                log.debug("Adding[" + tt.getName() + "] to hash");
            }

            for (int t=0; t<treeRoot.getKids(); t++)
            {
                fixAliases(treeRoot.getKid(t), hash);
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

        if (queryNavBtn != null)
        {
            queryNavBtn.setEnabled(true);
        }
    }

}
