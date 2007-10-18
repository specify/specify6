/*
     * Copyright (C) 2007  The University of Kansas
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
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
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
import edu.ku.brc.dbsupport.DBRelationshipInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpQueryField;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
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
    protected static final Logger log = Logger.getLogger(QueryBldrPane.class);
    
    protected JList                          tableList;
    protected Hashtable<DBTableInfo, Vector<TableFieldPair>> tableFieldList = new Hashtable<DBTableInfo, Vector<TableFieldPair>>();
    
    protected Vector<QueryFieldPanel>        queryFieldItems  = new Vector<QueryFieldPanel>();
    protected int                            currentInx       = -1;
    protected JPanel                         queryFieldsPanel;
    
    protected SpQuery                        query            = null;

    protected JButton                        addBtn;
    
    protected ImageIcon                      blankIcon        = IconManager.getIcon("BlankIcon", IconManager.IconSize.Std24);
    
    protected String                         columnDefStr     = null;
    
    protected JPanel                         listBoxPanel;
    protected Vector<JList>                  listBoxList      = new Vector<JList>();
    protected Vector<TableTree>              nodeList         = new Vector<TableTree>();
    protected JScrollPane                    scrollPane;
    protected Vector<JScrollPane>            spList           = new Vector<JScrollPane>();
    
    protected Hashtable<String, Boolean>     fieldsToSkipHash = new Hashtable<String, Boolean>();
    protected QryListRenderer                qryRenderer      = new QryListRenderer(IconManager.IconSize.Std16);
    protected int                            listCellHeight;
    
    protected Vector<TableTree>              tableTreeList;
    protected boolean                        processingLists = false;
    
    /**
     * Constructor.
     * @param name name of subpanel
     * @param task the owning task
     */
    public QueryBldrPane(final String   name,
                         final Taskable task,
                         final SpQuery  query)
    {
        super(name, task);
        
        this.query = query;
        
        String[] skipItems = {"TimestampCreated", "LastEditedBy", "TimestampModified"};
        for (String nameStr : skipItems)
        {
            fieldsToSkipHash.put(nameStr, true);
        }
        
        tableTreeList = readTables();
     
        createUI();
        
        setQueryIntoUI();
    }
    
    /**
     * 
     */
    protected void createUI()
    {
        removeAll();
        
        listBoxPanel = new JPanel(new HorzLayoutManager(2,2));
        
        Vector<TableQRI> list = new Vector<TableQRI>();
        for (TableTree tt : tableTreeList)
        {
            list.add(new TableQRI(null, tt)); 
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
        JScrollPane spt = new JScrollPane(tableList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        Dimension   pSize = spt.getPreferredSize();
        pSize.height = 200;
        spt.setPreferredSize(pSize);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        
        scrollPane = new JScrollPane(listBoxPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        tableList.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent e)
            {
                //fieldList.setSelectedIndex(-1);
                if (!e.getValueIsAdjusting())
                {
                    nodeList.clear();
                    fillNextList(tableList);
                }
            }
        });

        
        addBtn = new JButton(IconManager.getImage("PlusSign", IconManager.IconSize.Std16));
        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                FieldQRI fieldQRI = (FieldQRI)listBoxList.get(currentInx).getSelectedValue();
                addQueryFieldItem(fieldQRI);
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
        queryFieldsPanel.setLayout(new NavBoxLayoutManager(0,2));
        JScrollPane sp = new JScrollPane(queryFieldsPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null);
        add(sp, BorderLayout.CENTER);
        
        JButton         searchBtn = new JButton("Search");
        PanelBuilder    builder   = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "p"));
        CellConstraints cc        = new CellConstraints();
        builder.add(searchBtn, cc.xy(2, 1));
        add(builder.getPanel(), BorderLayout.SOUTH);
        
        searchBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                doSearch();
            }
        });
        
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        
        this.validate();
    }
    
    /**
     * @param fieldName
     * @return
     */
    public static String fixFieldName(final String fieldName)
    {
        return fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
    }
    
    protected void setQueryIntoUI()
    {
        queryFieldsPanel.removeAll();
        queryFieldItems.clear();
        queryFieldsPanel.validate();
        
        if (query == null)
        {
            tableList.setSelectedIndex(-1);
            
        } else
        {
            int tblId = query.getContextTableId();
            for (int i=0;i<tableList.getModel().getSize();i++)
            {
                TableQRI qri = (TableQRI)tableList.getModel().getElementAt(i);
                if (qri.getTableInfo().getTableId() == tblId)
                {
                    tableList.setSelectedIndex(i);
                    for (SpQueryField field : query.getFields())
                    {
                        addQueryFieldItem(field);
                    }
                    break;
                }
            }
        }
    }
    
    /**
     * @param queryArg
     */
    public void setQuery(final SpQuery queryArg)
    {
        query = queryArg;
        setQueryIntoUI();
    }
    
    /**
     * Performs the Search by building the HQL String.
     */
    protected void doSearch()
    {
        if (queryFieldItems.size() > 0)
        {
            StringBuilder fieldsStr = new StringBuilder();
            for (QueryFieldPanel qfi : queryFieldItems)
            {
                if (qfi.isForDisplay())
                {
                    if (fieldsStr.length() > 0) fieldsStr.append(", ");
                }
                fieldsStr.append(qfi.getFieldInfo().getTableInfo().getAbbrev());
                fieldsStr.append('.');
                fieldsStr.append(qfi.getFieldInfo().getName());
            }
            
            Vector<BaseQRI> list = new Vector<BaseQRI>();

            StringBuilder criteriaStr = new StringBuilder();
            
            boolean     debug = true;
            ProcessNode root  = new ProcessNode(null);
            for (QueryFieldPanel qfi : queryFieldItems)
            {
                FieldQRI pqri = qfi.getFieldQRI();
                if (debug)
                {
                    System.out.println("\nNode: "+qfi.getFieldName());
                }
                String  criteria      = qfi.getCriteriaFormula();
                boolean isDisplayOnly = StringUtils.isEmpty(criteria);
                
                if (!isDisplayOnly)
                {
                    if (!isDisplayOnly && criteriaStr.length() > 0)
                    {
                        criteriaStr.append(" AND ");
                    }
                    criteriaStr.append(criteria);
                }
                //criteriaStr.append(' ');

                // Create a Stack (list) of parent from 
                // the current node up to the top
                // basically we are creating a path of nodes
                // to determine if we need to create a new node in the tree
                BaseQRI parent = qfi.getFieldQRI();
                list.clear();
                list.insertElementAt(pqri, 0);
                while (parent.getParent() != null)
                {
                    parent = parent.getParent();
                    list.insertElementAt(parent, 0);
                }
                
                if (debug)
                {
                    System.out.println("Path From Top Down:");
                    for (BaseQRI qri : list)
                    {
                        System.out.println("  "+qri.getTitle());
                    }
                }

                // Now walk the stack top (the top most parent)
                // down and if the path form the top down doesn't
                // exist then add a new node
                System.out.println("\nBuilding Tree:");
                ProcessNode parentNode = root;
                for (BaseQRI qri : list)
                {
                    if (debug)
                    {
                        System.out.println("ProcessNode["+qri.getTitle()+"]");
                    }
                    if (!parentNode.contains(qri))
                    {
                        ProcessNode newNode = new ProcessNode(qri);
                        parentNode.getKids().add(newNode);
                        if (debug)
                        {
                            System.out.println("Adding new node["+newNode.getQri().getTitle()+"] to Node["+(parentNode.getQri() == null ? "root" : parentNode.getQri().getTitle())+"]");
                        }
                        parentNode = newNode;
                    } else
                    {
                        for (ProcessNode kidNode : parentNode.getKids())
                        {
                            if (!kidNode.contains(qri))
                            {
                                parentNode = kidNode;
                            }
                        }
                    }
                    //System.out.println(stack.get(i).getTitle());
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
            sqlStr.append(fieldsStr);
            sqlStr.append(" from ");
            
            processTree(root, sqlStr, 0);
            
            sqlStr.append(" where ");
            sqlStr.append(criteriaStr);
            System.out.println(sqlStr.toString());
            
            processSQL(queryFieldItems, sqlStr.toString());
            
        }
        //processSQL("select CO.catalogNumber, DE.determinedDate from CollectionObject as CO JOIN CO.determinations DE");
    }
    
    /**
     * @param parent
     * @param sqlStr
     * @param level
     */
    protected void processTree(final ProcessNode   parent, 
                               final StringBuilder sqlStr,
                               final int           level)
    {
        BaseQRI qri = parent.getQri();
        if (qri != null)
        {
            TableTree tt = qri.getTableTree();
            if (tt != null)
            {
                System.out.println("processTree "+tt.getName());
                if (level == 1)
                {
                    sqlStr.append(tt.getName());
                    sqlStr.append(' ');
                    sqlStr.append(tt.getAbbrev());
                    sqlStr.append(' ');
                   
                } else
                {
                    sqlStr.append(" join ");
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
            processTree(kid, sqlStr, level+1);
        }
    }
    
    /**
     * @param queryFieldItemsArg
     * @param sql
     */
    protected void processSQL(final Vector<QueryFieldPanel> queryFieldItemsArg, final String sql)
    {
        List<ERTICaptionInfo> captions = new Vector<ERTICaptionInfo>();
        for (QueryFieldPanel qfp : queryFieldItemsArg)
        {
            DBFieldInfo fi      = qfp.getFieldInfo();
            DBTableInfo ti      = fi.getTableInfo();
            String      colName = ti.getAbbrev() +'.' + fi.getColumn();
            
            if (qfp.isDisplayable())
            {
                captions.add(new ERTICaptionInfo(colName, fi.getTitle(), true, null, 0));
            }
        }
        
        List<Integer>          list = new Vector<Integer>();
        QBQueryForIdResultsHQL qri = new QBQueryForIdResultsHQL(new Color(144, 30, 255), 
                                                                "Search Results",                       // XXX I18N
                                                                CollectionObject.class.getSimpleName(), // Icon Name
                                                                1,                                      // table id
                                                                "",                                     // search term
                                                                list);
        qri.setSQL(sql);
        qri.setCaptions(captions);
        qri.setExpanded(true);
        
        CommandDispatcher.dispatch(new CommandAction("Express_Search", "HQL", qri));
    }
    
    protected void printTree(ProcessNode pn, int lvl)
    {
        for (int i=0;i<lvl;i++)
        {
            System.out.print(" ");
        }
        System.out.println(pn.getQri() == null ? "Root" : pn.getQri().getTitle());
        for (ProcessNode kid : pn.getKids())
        {
            printTree(kid, lvl+1);
        }
    }
    
    //-----------------------------------------------------------
    //-- Inner Classes
    //-----------------------------------------------------------
    
    class ProcessNode 
    {
        protected Vector<ProcessNode> kids = new Vector<ProcessNode>();
        protected BaseQRI qri;
        
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
                if (pn.getQri() == qriArg)
                {
                    return true;
                }
            }
            return false;
        }
    }
    
    protected TableTree findTableTree(final TableTree parentTT, final String nameArg)
    {
        for (TableTree tt : parentTT.getKids())
        {
            if (tt.getName().equals(nameArg))
            {
                return tt;
            }
        }
        return null;
    }
    
    protected void createNewList(final BaseQRI parentQRI, final TableTree tableTree, final DefaultListModel model)
    {
        model.clear();
        if (tableTree != null)
        {
            Vector<QryListRendererIFace> fldList = new Vector<QryListRendererIFace>();

            for (DBFieldInfo fi : tableTree.getTableInfo().getFields())
            {
                if (fi.getColumn() != null && fieldsToSkipHash.get(fi.getColumn()) == null && !fi.isHidden())
                {
                    fldList.add(new FieldQRI(parentQRI, fi));
                }
            }
            for (TableTree tt : tableTree.getKids())
            {
                for (DBRelationshipInfo ri : tableTree.getTableInfo().getRelationships())
                {
                    if (!ri.isHidden())
                    {
                        String clsName = StringUtils.substringAfterLast(ri.getClassName(), ".");
                        if (clsName.equals(tt.getName()))
                        {
                            TableTree riTT = findTableTree(parentQRI.getTableTree(), clsName);
                            if (riTT == null)
                            {
                                riTT = tt;
                            }
                            fldList.add(new RelQRI(parentQRI, riTT, ri));
                            break;
                        }
                    }
                }
            }
            Collections.sort(fldList, new Comparator<QryListRendererIFace>() {
                public int compare(QryListRendererIFace o1, QryListRendererIFace o2)
                {
                    return o1.getTitle().compareTo(o2.getTitle());
                }
            });
            
            for (QryListRendererIFace qri : fldList)
            {
                model.addElement(qri);
            }
        }
    }
    
    protected void fillNextList(final JList parentList)
    {
        if (processingLists)
        {
            return;
        }
        
        processingLists = true;
        
        final int curInx = listBoxList.indexOf(parentList);
        if (curInx > -1)
        {
            for (int i=curInx+1;i<listBoxList.size();i++)
            {
                listBoxPanel.remove(spList.get(i));
            }
            
        } else
        {
            listBoxPanel.removeAll();
        }
        //System.out.println("cur "+curInx+"  cnt "+listBoxPanel.getComponentCount()+"  size "+listBoxList.size());
        
        QryListRendererIFace item = (QryListRendererIFace)parentList.getSelectedValue();
        if (!(item instanceof FieldQRI))
        {
            JList            newList;
            DefaultListModel model;
            JScrollPane      sp;
            
            if (curInx == listBoxList.size()-1)
            {
                newList = new JList(model = new DefaultListModel());
                newList.setCellRenderer(qryRenderer);
                listBoxList.add(newList);
                sp = new JScrollPane(newList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                spList.add(sp);
                
                newList.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
                    public void valueChanged(ListSelectionEvent e)
                    {
                        if (!e.getValueIsAdjusting())
                        {
                            fillNextList(listBoxList.get(curInx+1));
                        }
                    }
                });
                
            } else
            {
                newList = listBoxList.get(curInx+1);
                model   = (DefaultListModel)newList.getModel();
                sp      = spList.get(curInx+1);
            }
            
            if (item instanceof TableQRI)
            {
                model.clear();
                TableQRI table = (TableQRI)item;
                
                createNewList((BaseQRI)item, table.getTableTree(), model);
                
            } else if (item instanceof RelQRI)
            {
                RelQRI rel = (RelQRI)item;
                
                createNewList((BaseQRI)item, rel.getTableTree(), model);
            }
            
            listBoxPanel.add(sp);
            listBoxPanel.remove(addBtn);
            currentInx = -1;
            
        } else
        {
            listBoxPanel.add(addBtn);
        }
        
        SwingUtilities.invokeLater(new Runnable() {
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
            QryListRendererIFace qri = (QryListRendererIFace)listBoxList.get(currentInx).getSelectedValue();
            if (qri instanceof FieldQRI)
            {
                FieldQRI fieldQRI = (FieldQRI)qri; 
                addBtn.setEnabled(!fieldQRI.isInUse());
            }
        }
    }
    
    /**
     * Removes it from the List.
     * @param qfp QueryFieldPanel to be added
     */
    public void removeQueryFieldItem(final QueryFieldPanel qfp)
    {
        queryFieldItems.remove(qfp);
        queryFieldsPanel.getLayout().removeLayoutComponent(qfp);
        queryFieldsPanel.remove(qfp);
        qfp.getFieldQRI().setIsInUse(false);
        queryFieldsPanel.validate();
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                listBoxList.get(currentInx).repaint();
                queryFieldsPanel.repaint();
            }
        });
    }
    
    protected TableQRI getTableQRI(final JList list, final int tblId)
    {
        for (int i=0;i<list.getModel().getSize();i++)
        {
            BaseQRI qri = (BaseQRI)list.getModel().getElementAt(i);
            if (qri instanceof TableQRI)
            {
                TableQRI tblQRI =  (TableQRI)qri;
                if (tblQRI.getTableInfo().getTableId() == tblId)
                {
                    list.setSelectedIndex(i);
                    return tblQRI;
                }
            }
        }
        return null;
    }
    
    /**
     * Add QueryFieldItem to the list created with a TableFieldPair.
     * @param fieldItem the TableFieldPair to be in the list
     */
    protected void addQueryFieldItem(final SpQueryField field)
    {
        if (field != null)
        {
            currentInx = 0;
            //TableQRI tableQRI = null;
            int      level    = 0;
            int[]    ids      = field.getTableIds();
            for (int id : ids)
            {
                if (level == ids.length-1)
                {
                    ListModel model = listBoxList.get(level-1).getModel();
                    for (int i=0;i<model.getSize();i++)
                    {
                        BaseQRI qri = (BaseQRI)model.getElementAt(i);
                        if (qri instanceof FieldQRI)
                        {
                            FieldQRI fieldQRI = (FieldQRI)qri;
                            if (fieldQRI.getFieldInfo().getName().equals(field.getFieldName()))
                            {
                                addQueryFieldItem(fieldQRI);
                                currentInx = level-1;
                                return;
                            }
                        }
                    }
                } else
                {
                    getTableQRI(level == 0 ? tableList : listBoxList.get(level-1), id);
                }
                level++;
            }
        }
    }
    
    /**
     * Add QueryFieldItem to the list created with a TableFieldPair.
     * @param fieldItem the TableFieldPair to be in the list
     */
    protected void addQueryFieldItem(final FieldQRI fieldQRI)
    {
        if (fieldQRI != null)
        {
            if (queryFieldItems.size() == 0 && queryFieldsPanel.getComponentCount() == 0)
            {
                QueryFieldPanel qfp = new QueryFieldPanel(this, true, fieldQRI, IconManager.IconSize.Std24, columnDefStr);
                queryFieldsPanel.add(qfp);                
            }
            final QueryFieldPanel qfp = new QueryFieldPanel(this, false, fieldQRI, IconManager.IconSize.Std24, columnDefStr);
            queryFieldsPanel.add(qfp);
            queryFieldItems.add(qfp);
            fieldQRI.setIsInUse(true);
            queryFieldsPanel.validate();
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    listBoxList.get(currentInx).repaint();
                    queryFieldsPanel.repaint();
                    qfp.repaint();
                    updateAddBtnState();
                }
            });
        }
    }
    
    protected void processForAliases(final Element parent, 
                                     final Vector<TableTree> treeNodes)
    {
        @SuppressWarnings("unused")
        TableTree treeNode = null;
        String    nameStr  = XMLHelper.getAttr(parent, "name", null);
        for (TableTree tt : treeNodes)
        {
            if (tt.getName().equals(nameStr))
            {
                treeNode = tt;
                break;
            }
        }
        
        //if (treeNode != null)
        //{
            for (Object obj : parent.selectNodes("alias"))
            {
                Element kidElement = (Element)obj;
                processForAliases(kidElement, treeNodes);
            }
        //}
    }
    
    protected void processForTables(final Element           parent, 
                                    final TableTree         parentTT,
                                    final Vector<TableTree> kids)
    {
        String      tableName = XMLHelper.getAttr(parent, "name", null);
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByShortClassName(tableName);
        if (!tableInfo.isHidden())
        {
            String fieldName = XMLHelper.getAttr(parent, "field", null);
            if (StringUtils.isEmpty(fieldName))
            {
                fieldName = tableName.substring(0, 1).toLowerCase() + tableName.substring(1);
            }
            
            TableTree newTreeNode = new TableTree(parentTT, tableName, fieldName, tableInfo);
            kids.add(newTreeNode);
            
            for (Object kidObj : parent.selectNodes("table"))
            {
                Element kidElement = (Element)kidObj;
                processForTables(kidElement, newTreeNode, newTreeNode.getKids());
            }
            
            for (Object obj : parent.selectNodes("alias"))
            {
                Element kidElement = (Element)obj;
                String  kidClassName = XMLHelper.getAttr(kidElement, "name", null);
                tableInfo = DBTableIdMgr.getInstance().getByShortClassName(kidClassName);
                if (!tableInfo.isHidden())
                {
                    TableTree aliasTreeNode = new TableTree(newTreeNode, kidClassName);
                    aliasTreeNode.setAlias(true);
                    newTreeNode.getKids().add(aliasTreeNode);
                    aliasTreeNode.setParent(newTreeNode);
                    //System.out.println(XMLHelper.getAttr(kidElement, "name", null)+"  "+parentTT);
                    //hash.put(XMLHelper.getAttr(kidElement, "name", null), new Pair<TableTree, TableTree>(newTreeNode, newTreeNode));
                }
            }
        }
    }
    
    protected void fixAliases(final TableTree tableTree, Hashtable<String, TableTree> hash)
    {
        if (tableTree.isAlias())
        {
            TableTree tt = hash.get(tableTree.getName());
            if (tt != null)
            {
                tableTree.setField(tt.getField());
                tableTree.getKids().addAll(tt.getKids());
                tableTree.setTableInfo(tt.getTableInfo());
                tableTree.setAlias(false);
                
            } else
            {
                log.error("Couldn't find ["+tableTree.getName()+"] in the hash.");
            }
        } else
        {
            for (TableTree kidTable : tableTree.getKids())
            {
                fixAliases(kidTable, hash);
            }
        }
        
    }
    
    protected Vector<TableTree> readTables()
    {
        Vector<TableTree> tables = null;
        try
        {
            Element root       = XMLHelper.readDOMFromConfigDir("querybuilder.xml");
            List<?> tableNodes = root.selectNodes("/database/table");
            tables = new Vector<TableTree>(tableNodes.size());
            for (Object obj : tableNodes)
            {
                Element tableElement = (Element)obj;
                processForTables(tableElement, null, tables);
            }
            
            Hashtable<String, TableTree> hash = new Hashtable<String, TableTree>();
            for (TableTree tt : tables)
            {
               hash.put(tt.getName(), tt);
               log.debug("Adding["+tt.getName()+"] to hash");
            }
            
            for (TableTree tt : tables)
            {
                fixAliases(tt, hash);
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return tables;
    }

    /**
     * @param columnDefStr the columnDefStr to set
     */
    public void setColumnDefStr(String columnDefStr)
    {
        this.columnDefStr = columnDefStr;
    }


}
