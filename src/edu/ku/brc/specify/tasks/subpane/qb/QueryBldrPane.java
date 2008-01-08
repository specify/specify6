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
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpQueryField;
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
    protected JButton                        saveBtn;
    
    
    protected Hashtable<String, Boolean>     fieldsToSkipHash = new Hashtable<String, Boolean>();
    protected QryListRenderer                qryRenderer      = new QryListRenderer(IconManager.IconSize.Std16);
    protected int                            listCellHeight;
    
    protected Vector<TableTree>              tableTreeList;
    protected boolean                        processingLists = false;
    
    protected RolloverCommand                queryNavBtn     = null;
    
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
        
        saveBtn = new JButton("Save");
        saveBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                saveQuery();
            }
        });
        
        listBoxPanel = new JPanel(new HorzLayoutManager(2,2));
        
        Vector<TableQRI> list = new Vector<TableQRI>();
        for (TableTree tt : tableTreeList)
        {

            list.add((TableQRI)tt.getBaseQRI()); 
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
                    
                    int inx = tableList.getSelectedIndex();
                    if (inx > -1)
                    {
                        fillNextList(tableList);
                        
                        TableTree node = tableTreeList.get(inx);
                        query.setContextTableId((short)node.getTableInfo().getTableId());
                        query.setContextName(node.getName());
                        
                    } else
                    {
                        listBoxPanel.removeAll();
                    }
                }
            }
        });

        
        addBtn = new JButton(IconManager.getImage("PlusSign", IconManager.IconSize.Std16));
        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                FieldQRI fieldQRI = (FieldQRI)listBoxList.get(currentInx).getSelectedValue();
                
                SpQueryField qf = new SpQueryField();
                qf.initialize();
                qf.setFieldName(fieldQRI.getFieldInfo().getName());
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
        queryFieldsPanel.setLayout(new NavBoxLayoutManager(0,2));
        JScrollPane sp = new JScrollPane(queryFieldsPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null);
        add(sp, BorderLayout.CENTER);
        
        JButton         searchBtn = new JButton("Search");
        
        PanelBuilder    outer     = new PanelBuilder(new FormLayout("f:p:g,p", "p"));
        PanelBuilder    builder   = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "p"));
        CellConstraints cc        = new CellConstraints();
        builder.add(searchBtn, cc.xy(2, 1));
        
        outer.add(builder.getPanel(), cc.xy(1, 1));
        outer.add(saveBtn,            cc.xy(2, 1));
        add(outer.getPanel(), BorderLayout.SOUTH);
        
        searchBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                doSearch();
            }
        });
        
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        
    }
    
    public int getCurrentContextTableId()
    {
        TableQRI currentTableQRI = (TableQRI)tableList.getSelectedValue();
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
            DefaultListModel model = (DefaultListModel)list.getModel();
            for (int i=0;i<model.size();i++)
            {
                BaseQRI bq = (BaseQRI)model.get(i);
                bq.setIsInUse(false);
            }
        }
        
        tableList.clearSelection();
        
        if (query != null)
        {
            Short tblId = query.getContextTableId();
            if (tblId != null)
            {
                for (int i=0;i<tableList.getModel().getSize();i++)
                {
                    TableQRI qri = (TableQRI)tableList.getModel().getElementAt(i);
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
            for (QueryFieldPanel qfp : queryFieldItems)
            {
                if (qfp.isForDisplay())
                {
                    if (fieldsStr.length() > 0) fieldsStr.append(", ");
                }
                fieldsStr.append(qfp.getFieldInfo().getTableInfo().getAbbrev());
                fieldsStr.append('.');
                fieldsStr.append(qfp.getFieldInfo().getName());
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
            
            if (criteriaStr.length() > 0)
            {
                sqlStr.append(" where ");
                sqlStr.append(criteriaStr);
            }
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
    
    /**
     * @param pn
     * @param lvl
     */
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
    
    /**
     * 
     */
    protected void saveQuery()
    {
        TableQRI tableQRI = (TableQRI)tableList.getSelectedValue();
        if (tableQRI != null)
        {
            short position = 0;
            for (QueryFieldPanel qfp : queryFieldItems)
            {
                SpQueryField qf = qfp.getQueryField();
                if (qf == null)
                {
                    throw new RuntimeException("Shouldn't get here!");
                }
                qf.setPosition(position);
                qfp.updateQueryField();
                
                position++;
            }
            
            if (query.getSpQueryId() == null)
            {
                queryNavBtn = ((QueryTask)task).saveNewQuery(query, false); // false tells it to disable the navbtn
                
            } else
            {
                try
                {
                    DataProviderSessionIFace session    = DataProviderFactory.getInstance().createSession();
                    session.beginTransaction();
                    session.saveOrUpdate(query);
                    session.commit();
                    session.close();
                    
                } catch (Exception ex)
                {
                    log.error(ex);
                    ex.printStackTrace();
                }
            }
            
        } else
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
        for (TableTree tt : parentTT.getKids())
        {
            if (tt.getName().equals(nameArg))
            {
                return tt;
            }
        }
        return null;
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

     /**
     * @param parentQRI
     * @param tableTree
     * @param model
     */
    protected void createNewList(final BaseQRI   parentQRI, 
                                 final TableTree tableTree, 
                                 final DefaultListModel model)
    {
        model.clear();
        if (parentQRI != null)
        {
            TableQRI tblQRI = (TableQRI)parentQRI;
            for (BaseQRI baseQRI : tblQRI.getKids())
            {
                model.addElement(baseQRI);
            }
        }
    }
    
    /**
     * @param parentList
     */
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
    
    /**
     * @param kids
     * @param field
     * @param tableIds
     * @param level
     * @return
     */
    protected FieldQRI getFieldQRI(final Vector<TableTree> kids, final SpQueryField field, final int[] tableIds, final int level)
    {
        int id = tableIds[level];
        System.out.println("getFieldQRI id["+id+"] level["+level+"]");
        for (TableTree kid : kids)
        {
            System.out.println("checking id["+id+"] ["+kid.getTableInfo().getTableId()+"]");
            if (kid.getTableInfo().getTableId() == id)
            {
                if (level == (tableIds.length-1))
                {
                    TableQRI tblQRI = (TableQRI)kid.getBaseQRI();
                    for (BaseQRI baseQRI : tblQRI.getKids())
                    {
                        if (baseQRI instanceof FieldQRI)
                        {
                            FieldQRI fqri = (FieldQRI)baseQRI;
                            if (fqri.getFieldInfo().getName().equals(field.getFieldName()))
                            {
                                return fqri;
                            }
                        }
                    }
                } else
                {
                    FieldQRI fi = getFieldQRI(kid.getKids(), field, tableIds, level+1);
                    if (fi != null)
                    {
                        return fi;
                    }
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
            System.out.println(field.getTableList());
            FieldQRI fieldQRI = getFieldQRI(tableTreeList, field, field.getTableIds(), 0);
            if (fieldQRI != null)
            {
                addQueryFieldItem(fieldQRI, field);
            } else
            {
                log.error("Couldn't find ["+field.getFieldName()+"] ["+field.getTableList()+"]");
            }
        }
    }
    
    /**
     * Add QueryFieldItem to the list created with a TableFieldPair.
     * @param fieldItem the TableFieldPair to be in the list
     */
    protected void addQueryFieldItem(final FieldQRI fieldQRI, final SpQueryField queryField)
    {
        if (fieldQRI != null)
        {
            if (queryFieldItems.size() == 0 && queryFieldsPanel.getComponentCount() == 0)
            {
                QueryFieldPanel qfp = new QueryFieldPanel(this, fieldQRI, IconManager.IconSize.Std24, columnDefStr, saveBtn, null);
                queryFieldsPanel.add(qfp);                
            }
            
            final QueryFieldPanel qfp = new QueryFieldPanel(this, fieldQRI, IconManager.IconSize.Std24, columnDefStr, saveBtn, queryField);
            queryFieldsPanel.add(qfp);
            queryFieldItems.add(qfp);
            fieldQRI.setIsInUse(true);
            queryFieldsPanel.validate();
            
            SwingUtilities.invokeLater(new Runnable() {
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
    
    /**
     * @param parent
     * @param parentTT
     * @param ttKids
     * @param parentQRI
     */
    protected void processForTables(final Element           parent, 
                                    final TableTree         parentTT,
                                    final Vector<TableTree> ttKids,
                                    final TableQRI          parentQRI)
    {
        String tableName = XMLHelper.getAttr(parent, "name", null);
        if (tableName.equals("CollectionObject"))
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
            
            TableTree newTreeNode = new TableTree(parentTT, tableName, fieldName, tableInfo);
            ttKids.add(newTreeNode);
            
            TableQRI tableQRI = new TableQRI(parentQRI, newTreeNode);
            newTreeNode.setBaseQRI(tableQRI);
            
            for (DBFieldInfo fi : tableInfo.getFields())
            {
                FieldQRI  fqri = new FieldQRI(tableQRI, fi);
                tableQRI.addKid(fqri);
            }
            
            if (parentQRI != null)
            {
                parentQRI.addKid(tableQRI);
            }
            
            for (Object kidObj : parent.selectNodes("table"))
            {
                Element kidElement = (Element)kidObj;
                processForTables(kidElement, newTreeNode, newTreeNode.getKids(), tableQRI);
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
    
    /**
     * @param tableTree
     * @param hash
     */
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
    
    /**
     * @return
     */
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
                processForTables(tableElement, null, tables, null);
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

    /* (non-Javadoc)
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
