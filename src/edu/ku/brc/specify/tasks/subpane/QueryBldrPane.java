/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.LayoutManager2;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.hibernate.Session;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.NavBoxLayoutManager;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.expresssearch.ERTICaptionInfo;
import edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL;
import edu.ku.brc.af.core.expresssearch.TableFieldPair;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.MultiStateIconButon;
import edu.ku.brc.ui.UIHelper;
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
    protected JList                          tableList;
    protected Hashtable<DBTableIdMgr.TableInfo, Vector<TableFieldPair>> tableFieldList = new Hashtable<DBTableIdMgr.TableInfo, Vector<TableFieldPair>>();
    
    protected Vector<QueryFieldPanel>        queryFieldItems = new Vector<QueryFieldPanel>();
    protected int                            currentInx      = -1;
    protected JPanel                         queryFieldsPanel;

    protected JButton                        addBtn;
    
    protected ImageIcon                      blankIcon   = IconManager.getIcon("BlankIcon", IconManager.IconSize.Std24);
    
    protected String                         columnDefStr = null;
    
    protected JPanel                         listBoxPanel;
    protected Vector<JList>                  listBoxList      = new Vector<JList>();
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
    public QueryBldrPane(final String name,
                         final Taskable task)
    {
        super(name, task);
        
        String[] skipItems = {"TimestampCreated", "LastEditedBy", "TimestampModified"};
        for (String nameStr : skipItems)
        {
            fieldsToSkipHash.put(nameStr, true);
        }
        
        tableTreeList = readTables();
     
        createUI();
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
    
    public static String fixFieldName(final String fieldName)
    {
        return fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
    }
    
    /**
     * 
     */
    protected void doSearch()
    {
        if (queryFieldItems.size() > 0)
        {
            QueryFieldPanel panel  = queryFieldItems.get(0);
            FieldQRI        fqri   = panel.getFieldQRI();
            
            StringBuilder fieldsStr = new StringBuilder();
            for (QueryFieldPanel qfi : queryFieldItems)
            {
                if (qfi.isForDisplay())
                {
                    if (fieldsStr.length() > 0) fieldsStr.append(", ");
                }
                fieldsStr.append(qfi.getFieldQRI().getParent().getTableTree().getAbbrev());
                fieldsStr.append('.');
                fieldsStr.append(qfi.getFieldInfo().getName());
            }
            
            Stack<BaseQRI>  stack = new Stack<BaseQRI>();

            StringBuilder criteriaStr = new StringBuilder();
            
            ProcessNode root = new ProcessNode(null);
            for (QueryFieldPanel qfi : queryFieldItems)
            {
                String criteria = qfi.getCriteriaFormula();
                if (StringUtils.isEmpty(criteria))
                {
                    continue;
                }
                if (criteriaStr.length() > 0)
                {
                    criteriaStr.append(" AND ");
                }
                criteriaStr.append(criteria);
                //criteriaStr.append(' ');

                BaseQRI parent = qfi.getFieldQRI();
                stack.clear();
                stack.push(fqri);
                while (parent.getParent() != null)
                {
                    parent = parent.getParent();
                    stack.push(parent);
                }
                
                ProcessNode parentNode = root;
                for (int i=stack.size()-1;i>-1;i--)
                {
                    BaseQRI qri = stack.get(i);
                    if (!parentNode.contains(qri))
                    {
                        ProcessNode newNode = new ProcessNode(qri);
                        parentNode.getKids().add(newNode);
                        parentNode = newNode;
                    }
                    //System.out.println(stack.get(i).getTitle());
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
            
            processSQL(sqlStr.toString());
            
        }
        //processSQL("select CO.catalogNumber, DE.determinedDate from CollectionObject as CO JOIN CO.determinations DE");
    }
    
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
    
    protected void processSQL(final String sql)
    {
        
        class MyQueryForIdResultsHQL extends QueryForIdResultsHQL
        {
        
            public MyQueryForIdResultsHQL(final Color             bannerColor,
                                          final String            searchTerm,
                                          final List<?>           listOfIds)
            {
                super(null, bannerColor, searchTerm, listOfIds);
            }
            
            public void setCaptions(List<ERTICaptionInfo> list)
            {
                this.captions = list;
            }

            /* (non-Javadoc)
             * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#getDisplayOrder()
             */
            @Override
            public Integer getDisplayOrder()
            {
                return 0;
            }

            /* (non-Javadoc)
             * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#getIconName()
             */
            @Override
            public String getIconName()
            {
                return CollectionObject.class.getSimpleName();
            }

            /* (non-Javadoc)
             * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#buildCaptions()
             */
            @Override
            protected void buildCaptions()
            {
            }
            
            public String getTitle()
            {
                return "Hello";
            }
            
            public int getTableId()
            {
                return 1;
            }
            
        };
        
        List<ERTICaptionInfo> captions = new Vector<ERTICaptionInfo>();
        captions.add(new ERTICaptionInfo("de.remarks", "Remarks", true, null, 0));
        captions.add(new ERTICaptionInfo("co.catalogNumber", "CatalogNumber", true, null, 0));
        
        List<Integer> list = new Vector<Integer>();
        //list.add(1);
        //list.add(1);
        MyQueryForIdResultsHQL qri = new MyQueryForIdResultsHQL(new Color(144, 30, 255), "XXX", list);
        qri.setSQL(sql);
        qri.setCaptions(captions);
        
        CommandDispatcher.dispatch(new CommandAction("Express_Search", "HQL", qri));
        
        /*
        Session session = HibernateUtil.getNewSession();
        try
        {
            List<?> list = session.createQuery(sql).list();
            for (Object obj : list)
            {
                System.out.println(obj);
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            session.close();
        }*/
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
        
        public boolean contains(BaseQRI qri)
        {
            for (ProcessNode pn : kids)
            {
                if (pn.getQri() == qri)
                {
                    return true;
                }
            }
            return false;
        }
    }
    
    protected TableTree findTableTree(final String name)
    {
        for (TableTree tt : tableTreeList)
        {
            if (tt.getName().equals(name))
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

            for (DBTableIdMgr.FieldInfo fi : tableTree.getTableInfo().getFields())
            {
                if (fi.getColumn() != null && fieldsToSkipHash.get(fi.getColumn()) == null)
                {
                    fldList.add(new FieldQRI(parentQRI, fi));
                }
            }
            for (TableTree tt : tableTree.getKids())
            {
                for (DBTableIdMgr.TableRelationship ri : tableTree.getTableInfo().getRelationships())
                {
                    String clsName = StringUtils.substringAfterLast(ri.getClassName(), ".");
                    if (clsName.equals(tt.getName()))
                    {
                        TableTree riTT = findTableTree(clsName);
                        if (riTT == null)
                        {
                            riTT = tt;
                        }
                        fldList.add(new RelQRI(parentQRI, riTT, ri));
                        break;
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
        System.out.println("cur "+curInx+"  cnt "+listBoxPanel.getComponentCount()+"  size "+listBoxList.size());
        
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
    protected void removeQueryFieldItem(final QueryFieldPanel qfp)
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
     * Add QueryFieldItem to the list created with a TableFieldPair.
     * @param fieldItem the TableFieldPair to be in the list
     */
    protected void addQueryFieldItem(final FieldQRI fieldQRI)
    {
        if (fieldQRI != null)
        {
            if (queryFieldItems.size() == 0)
            {
                QueryFieldPanel qfp = new QueryFieldPanel(true, fieldQRI, IconManager.IconSize.Std24);
                queryFieldsPanel.add(qfp);                
            }
            final QueryFieldPanel qfp = new QueryFieldPanel(false, fieldQRI, IconManager.IconSize.Std24);
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
        TableTree treeNode = null;
        String name = XMLHelper.getAttr(parent, "name", null);
        for (TableTree tt : treeNodes)
        {
            if (tt.getName().equals(name))
            {
                treeNode = tt;
                break;
            }
        }
        if (treeNode == null)
        {
            treeNode = null;
        }
        
        for (Object obj : parent.selectNodes("alias"))
        {
            Element kidElement = (Element)obj;
            processForAliases(kidElement, treeNodes);
        }
    }
    
    protected void processForTables(final Element           parent, 
                                    final TableTree         parentTT,
                                    final Vector<TableTree> kids,
                                    final Hashtable<Element, TableTree> hash)
    {
        String nm = XMLHelper.getAttr(parent, "name", null).toLowerCase();
        if (nm.indexOf("collectingeventatt") > -1)
        {
            int x = 0;
            x++;
        }
        TableTree newTreeNode = new TableTree(parentTT, 
                                              XMLHelper.getAttr(parent, "name", null),
                                              XMLHelper.getAttr(parent, "abbrev", null),
                                              XMLHelper.getAttr(parent, "field", null));
        kids.add(newTreeNode);
        
        for (Object obj : parent.selectNodes("table"))
        {
            Element kidElement = (Element)obj;
            processForTables(kidElement, newTreeNode, newTreeNode.getKids(), hash);
        }
        for (Object obj : parent.selectNodes("alias"))
        {
            Element kidElement = (Element)obj;
            hash.put(kidElement, newTreeNode);
        }
    }
    
    protected Vector<TableTree> readTables()
    {
        Vector<TableTree> tables = null;
        try
        {
            Hashtable<Element, TableTree> hash = new Hashtable<Element, TableTree>();
            
            Element root       = XMLHelper.readDOMFromConfigDir("querybuilder.xml");
            List<?> tableNodes = root.selectNodes("/database/table");
            tables = new Vector<TableTree>(tableNodes.size());
            for (Object obj : tableNodes)
            {
                Element tableElement = (Element)obj;
                processForTables(tableElement, null, tables, hash);
            }
            
            for (Element ele : hash.keySet())
            {
                TableTree tableTree = hash.get(ele);
                
                String name = XMLHelper.getAttr(ele, "name", null);
                for (TableTree tt : tables)
                {
                    if (tt.getName().equals(name))
                    {
                        tableTree.getKids().add(tt);
                        break;
                    }
                }
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return tables;
    }


    //------------------------------------------------------------
    // One of the Criteria for the search
    //------------------------------------------------------------
    class QueryFieldPanel extends JPanel
    {
        protected String    noMappingStr = getResourceString("WB_NO_MAPPING");

        protected boolean          hasFocus      = false;
        protected Color            bgColor       = null;
        protected JLabel           fieldLabel;
        protected JLabel           closeBtn;
        protected JLabel           iconLabel;
        protected ImageIcon        icon;
        protected JCheckBox        isNotCheckbox;
        protected JComboBox        operatorCBX;
        protected JTextField       criteria;
        protected MultiStateIconButon sortCheckbox;
        protected JCheckBox        isDisplayedCkbx;
        protected FieldQRI         fieldQRI;
        
        protected DBTableIdMgr.FieldInfo  field = null;
        
        protected QueryFieldPanel  thisItem;
        
        protected String[] labelStrs   = {" ", "Field", "Not", "Operator", "Criteria", "Sort", "Display", " ", " "};
        protected String[] comparators = {"Like", "=", ">", "<"};
        
        /**
         * Constructor.
         * @param fieldName the field Name
         * @param icon the icon to use once it is mapped
         */
        public QueryFieldPanel(final boolean createAsHeader, 
                               final FieldQRI fieldQRI, 
                               final IconManager.IconSize iconSize)
        {
            this.fieldQRI = fieldQRI;
            this.field    = fieldQRI != null ? fieldQRI.getFieldInfo() : null;
            
            thisItem = this;
            
            int[] widths = buildControlLayout(iconSize, createAsHeader);
            if (createAsHeader)
            {
                removeAll();
                buildLabelLayout(iconSize, widths);
            }
        }
        
        protected String[] getComparatorListForClass(final Class<?> classObj)
        {
            if (classObj == String.class)
            {
                return new String[] {"Like", "="};
            }
            // else
            return new String[] {"=", ">", "<", ">=", "<="};
        }
        
        public String getCriteriaFormula()
        {
            String criteriaStr = criteria.getText();
            if (StringUtils.isNotEmpty(criteriaStr))
            {
                StringBuilder str  = new StringBuilder();
                String operStr     = operatorCBX.getSelectedItem().toString();
                
                System.out.println(fieldQRI.getFieldInfo().getDataClass().getSimpleName());
                if (fieldQRI.getFieldInfo().getDataClass() == String.class)
                {
                    if (operStr.equals("Like"))
                    {
                        criteriaStr = "'%" + criteriaStr + "%'";
                    } else
                    {
                        criteriaStr = "'" + criteriaStr + "'";
                    }
                }
                if (criteriaStr.length() > 0)
                {
                    TableTree parentTree = fieldQRI.getParent().getTableTree();
                    str.append(parentTree.getAbbrev() + '.');
                    str.append(QueryBldrPane.fixFieldName(getFieldName()));
                    str.append(' ');
                    str.append(isNotCheckbox.isSelected() ? "NOT" : "");
                    str.append(' ');
                    str.append(operStr);
                    str.append(' ');
                    str.append(criteriaStr);
                    return str.toString();
                }
            }
            return null;
        }
        
        /**
         * @return the fieldQRI
         */
        public FieldQRI getFieldQRI()
        {
            return fieldQRI;
        }

        protected int[] buildControlLayout(final IconManager.IconSize iconSize, final boolean returnWidths)
        {
            iconLabel     = new JLabel(icon);
            fieldLabel    = new JLabel(UIHelper.makeNamePretty(field.getColumn()));
            isNotCheckbox = new JCheckBox("");
            operatorCBX   = new JComboBox(comparators);
            criteria      = new JTextField();
            sortCheckbox  = new MultiStateIconButon(new ImageIcon[] {
                                IconManager.getImage("GrayDot",   IconManager.IconSize.Std16),
                                IconManager.getImage("UpArrow",   IconManager.IconSize.Std16),
                                IconManager.getImage("DownArrow", IconManager.IconSize.Std16)});
            //sortCheckbox.setMargin(new Insets(2,2,2,2));
            //sortCheckbox.setBorder(BorderFactory.createLineBorder(new Color(225,225,225)));
            isDisplayedCkbx = new JCheckBox("");
            closeBtn        = new JLabel(IconManager.getIcon("Close"));
            
            //                       0           1           2              3           4           5             6              7
            JComponent[] comps = {iconLabel, fieldLabel, isNotCheckbox, operatorCBX, criteria, sortCheckbox, isDisplayedCkbx, closeBtn, null};

            StringBuilder sb = new StringBuilder();
            if (columnDefStr == null)
            {
                for (int i=0;i<comps.length;i++)
                {
                    sb.append(i == 0 ? "" : ",");
                    if (i == 2 || i == 3 || i == 6) sb.append("c:");
                    sb.append("p");
                    if (i == 4) sb.append(":g");
                    sb.append(",4px");
                }
            } else
            {
                sb.append(columnDefStr);
            }
            
            PanelBuilder    builder = new PanelBuilder(new FormLayout(sb.toString(), "p"), this);
            CellConstraints cc      = new CellConstraints();

            int col = 1;
            for (JComponent comp : comps)
            {
                if (comp != null)
                {
                    builder.add(comp, cc.xy(col, 1));
                }
                col += 2;
            }

            icon = IconManager.getIcon(field.getTableInfo().getObjTitle(), iconSize);
            setIcon(icon);
            isDisplayedCkbx.setSelected(true);
            
            closeBtn.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    removeQueryFieldItem((QueryFieldPanel)((JComponent)e.getSource()).getParent());
                }
            });
            
            validate();
            doLayout();
            
            int[] widths = null;
            if (returnWidths)
            {
                widths = new int[comps.length];
                for (int i=0;i<comps.length;i++)
                {
                    widths[i] = comps[i] != null ? comps[i].getSize().width : 0;
                }
                widths[0] = iconSize.size();
                widths[1] = 200;
            }
            return widths;
        }
        
        protected void buildLabelLayout(final IconManager.IconSize iconSize,
                                        final int[] widths)
        {
            
            StringBuilder sb     = new StringBuilder();
            JLabel[] labels      = new JLabel[labelStrs.length];
            int[]    labelWidths = new int[labelStrs.length];
            for (int i=0;i<labels.length;i++)
            {
                labels[i] = new JLabel(labelStrs[i], JLabel.CENTER);
                labelWidths[i] = Math.max(widths[i], labels[i].getPreferredSize().width);
            }
            
            for (int i=0;i<labels.length;i++)
            {
                sb.append(i == 0 ? "" : ",");
                if (i == 2 || i == 3 || i == 6) sb.append("c:");
                sb.append("max(");
                sb.append(labelWidths[i]);
                sb.append(";p)");
                if (i == 4) sb.append(":g");
                sb.append(",4px");
            }

            //System.out.println(sb.toString());
            columnDefStr = sb.toString();
            
            PanelBuilder    builder = new PanelBuilder(new FormLayout(sb.toString(), "p"), this);
            CellConstraints cc      = new CellConstraints();


            int x = 1;
            for (JLabel label : labels)
            {
                builder.add(label, cc.xy(x, 1));
                x += 2;
            }          
        }
        
        /**
         * Split apart the name keying on upper case
         * @param nameToFix the name of the field
         * @return the split apart name
         */
        protected String fixName(final String nameToFix)
        {
            StringBuilder s = new StringBuilder();
            for (int i=0;i<nameToFix.length();i++)
            {
                if (i == 0) 
                {
                    s.append(Character.toUpperCase(nameToFix.charAt(i)));
                } else
                {
                    char c = nameToFix.charAt(i);
                    if (Character.isUpperCase(c))
                    {
                        s.append(' ');
                    }
                    s.append(c);
                }
            }
            return s.toString();  
        }


        public void setIcon(ImageIcon icon)
        {
            this.icon = icon == null ? blankIcon : icon;
            iconLabel.setIcon(this.icon);
        }

        /**
         * @return the TableInfo object
         */
        public DBTableIdMgr.FieldInfo getFieldInfo()
        {
            return field;
        }

        /**
         * Returns the field name.
         * @return the field name.
         */
        public String getFieldName()
        {
            return fieldLabel.getText();
        }
        
        public boolean isForDisplay()
        {
            return isDisplayedCkbx.isSelected();
        }
    }
    
    //------------------------------------------------------------------
    //-- 
    //------------------------------------------------------------------
    
    public class QryListRenderer implements ListCellRenderer
    {
        protected IconManager.IconSize iconSize;
        protected JPanel               panel;
        protected ImageIcon            kidIcon = null;
        protected ImageIcon            blankIcon;
        protected JLabel               iconLabel;
        protected JLabel               label;
        protected JLabel               kidLabel;
        protected boolean              displayKidIndicator = true;
        
        public QryListRenderer(final IconManager.IconSize iconSize) 
        {
            this.iconSize  = iconSize;
            this.blankIcon = IconManager.getIcon("BlankIcon", iconSize);
            
            this.iconLabel = new JLabel(blankIcon);
            this.label     = new JLabel("  ");
            this.kidLabel  = new JLabel(blankIcon);
            
            panel = new JPanel(new BorderLayout());
            panel.add(iconLabel, BorderLayout.WEST);
            panel.add(label, BorderLayout.CENTER);
            panel.add(kidLabel, BorderLayout.EAST);
            iconLabel.setOpaque(false);
            label.setOpaque(false);
            kidLabel.setOpaque(false);
        }

        /**
         * @param displayKidIndicator the displayKidIndicator to set
         */
        public void setDisplayKidIndicator(boolean displayKidIndicator)
        {
            this.displayKidIndicator = displayKidIndicator;
        }

        public Component getListCellRendererComponent(JList   list,
                                                      Object  value,   // value to display
                                                      int     index,      // cell index
                                                      boolean iss,    // is the cell selected
                                                      boolean chf)    // the list and the cell have the focus
        {
            QryListRendererIFace qri = (QryListRendererIFace)value;
            ImageIcon icon = qri.getIsInUse() == null ? IconManager.getIcon(qri.getIconName(), iconSize) : (qri.getIsInUse() ? IconManager.getIcon("Checkmark", iconSize) : blankIcon);
            iconLabel.setIcon(icon != null ? icon : blankIcon);
            kidLabel.setIcon(displayKidIndicator ? qri.hasChildren() ? IconManager.getIcon("Forward", iconSize) : blankIcon : blankIcon);
            
            if (iss) {
                //setOpaque(true);
                panel.setBackground(list.getSelectionBackground());
                panel.setForeground(list.getSelectionForeground());
                list.setSelectedIndex(index);

            } else {
                //this.setOpaque(false);
                panel.setBackground(list.getBackground());
                panel.setForeground(list.getForeground());
            }

            label.setText(qri.getTitle());
            panel.doLayout();
            return panel;
        }
    }
    
    public interface QryListRendererIFace
    {
        public String getIconName();
        public String getTitle();
        public boolean hasChildren();
        
        public Boolean getIsInUse();
        public void setIsInUse(Boolean isInUse);
        
    }
    
    class BaseQRI implements QryListRendererIFace, Comparable<QryListRendererIFace>
    {
        protected BaseQRI   parent;
        protected TableTree tableTree;
        protected String    iconName;
        protected String    title;
        protected Boolean   isInUse = null;
        
        public BaseQRI(final BaseQRI parent, final TableTree tableTree)
        {
            this.parent    = parent;
            this.tableTree = tableTree;
        }

        public boolean isInUse()
        {
            return isInUse != null && isInUse;
        }
        
        /**
         * @return the parent
         */
        public BaseQRI getParent()
        {
            return parent;
        }

        /**
         * @return the isInUse
         */
        public Boolean getIsInUse()
        {
            return isInUse;
        }

        /**
         * @param isInUse the isInUse to set
         */
        public void setIsInUse(Boolean isInUse)
        {
            this.isInUse = isInUse;
        }

        /**
         * @return the tableTree
         */
        public TableTree getTableTree()
        {
            return tableTree;
        }

        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(QryListRendererIFace qri)
        {
            //System.out.println(qri);
            //System.out.println(title+"]["+qri.getTitle());
            return title.compareTo(qri.getTitle());
        }
        
        public String getIconName()
        {
            return iconName;
        }
        
        public String getTitle()
        {
            return title;
        }
        public boolean hasChildren()
        {
            return true;
        }
    }
    
    class TableQRI extends BaseQRI
    {
        protected DBTableIdMgr.TableInfo ti;
        
        public TableQRI(final BaseQRI parent, final TableTree tableTree)
        {
            super(parent, tableTree);
            this.ti  = tableTree.getTableInfo();
            iconName = ti.getClassObj().getSimpleName();
            title    = UIHelper.makeNamePretty(iconName);
        }
        
        public DBTableIdMgr.TableInfo getTableInfo()
        {
            return ti;
        }
    }
    
    class FieldQRI extends BaseQRI
    {
        protected DBTableIdMgr.FieldInfo fi;
        
        public FieldQRI(final BaseQRI parent, final DBTableIdMgr.FieldInfo fi)
        {
            super(parent, null);
            this.fi  = fi;
            title    = UIHelper.makeNamePretty(fi.getColumn());
            iconName = "BlankIcon";
        }
        
        public DBTableIdMgr.FieldInfo getFieldInfo()
        {
            return fi;
        }
        public boolean hasChildren()
        {
            return false;
        }

    }
    
    class RelQRI extends BaseQRI
    {
        protected DBTableIdMgr.TableRelationship ri;
        
        public RelQRI(final BaseQRI parent, final TableTree tableTree, final DBTableIdMgr.TableRelationship ri)
        {
            super(parent, tableTree);
            this.ri = ri;
            
            try
            {
                iconName = Class.forName(ri.getClassName()).getSimpleName();
                title    = UIHelper.makeNamePretty(iconName);
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                iconName = "BlankIcon";
                title    = "????";
            }
        }
        public DBTableIdMgr.TableRelationship getTableRelationship()
        {
            return ri;
        }        
        public boolean hasChildren()
        {
            return true;//ri.getType() == DBTableIdMgr.RelationshipType.OneToMany || ri.getType() == DBTableIdMgr.RelationshipType.ManyToMany;
        }
    }
    
    public class HorzLayoutManager implements LayoutManager2
    {

        private Vector<Component> comps         = new Vector<Component>();
        private Dimension         preferredSize = new Dimension();
        private Dimension         minimumSize   = new Dimension();
        private int               borderPadding = 2;
        private int               xSeparation   = 5;

        /**
         * Constructs a layout manager for laying out NavBoxes. It lays out all the NavBoxes vertically
         * and uses the 'ySeparator' as the spacing in between the boxes. It uses borderPadding as a 'margin'
         * around all the boxes.
         * @param borderPadding the margin around the boxes
         * @param ySeparation the vertical separation in between the boxes.
         */
        public HorzLayoutManager(final int borderPadding, final int xSeparation)
        {
            this.borderPadding = borderPadding;
            this.xSeparation   = xSeparation;
        }

        /* (non-Javadoc)
         * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String, java.awt.Component)
         */
        public void addLayoutComponent(String arg0, Component arg1)
        {
            if (arg1 == null)
            {
                throw new NullPointerException("Null component in addLayoutComponent");
            }
            comps.addElement(arg1);

        }

        /* (non-Javadoc)
         * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
         */
        public void removeLayoutComponent(Component arg0)
        {
            if (arg0 == null)
            {
                throw new NullPointerException("Null component in removeLayoutComponent");
            }
          comps.removeElement(arg0);

        }

        /* (non-Javadoc)
         * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
         */
        public Dimension preferredLayoutSize(Container arg0)
        {
            return new Dimension(preferredSize);
        }

        /* (non-Javadoc)
         * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
         */
        public Dimension minimumLayoutSize(Container arg0)
        {
             return new Dimension(minimumSize);
        }

        /* (non-Javadoc)
         * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
         */
        public void layoutContainer(Container arg0)
        {
            Dimension parentSize =  arg0.getSize();
            parentSize.width  -= 2 * borderPadding;
            parentSize.height -= 2 * borderPadding;

            int x = borderPadding;
            int y = borderPadding;

            for (Component comp: comps)
            {
                Dimension size = comp.getPreferredSize();
                if (comp instanceof JButton)
                {
                    comp.setBounds(x, y+parentSize.height-size.height, size.width, size.height);
                    
                } else
                {
                    comp.setBounds(x, y, size.width, parentSize.height);
                }
                x += size.width + xSeparation;
            }
        }

        /**
         * Calculates the preferred size of the contain. It lays out all the NavBoxes vertically
         * and uses the 'ySeparator' as the spacing in between the boxes. It uses borderPadding as a 'margin'
         * around all the boxes.
         *
         */
        protected void calcPreferredSize()
        {
            preferredSize.setSize(borderPadding, borderPadding*2);

            for (Component comp : comps)
            {
                Dimension size = comp.getPreferredSize();
                //System.out.println(size);
                preferredSize.height = Math.max(preferredSize.height, size.height + (2 * borderPadding));
                preferredSize.width += size.width + xSeparation;
                
                minimumSize.height = Math.max(minimumSize.height, comp.getMinimumSize().height + (2 * borderPadding));
            }
            preferredSize.width -= xSeparation;
        }

        /**
         * Return the list of all the components that have been added to the alyout manager.
         * @return the list of all the components that have been added to the alyout manager
         */
        public List<Component> getComponentList()
        {
            return comps;
        }

        /**
         * Remove all the componets that have been added to the layout manager.
         */
        public void removeAll()
        {
            comps.clear();
            preferredSize.setSize(0, 0);
        }

        //----------------------
        // LayoutManager2
        //----------------------
        
        /* (non-Javadoc)
         * @see java.awt.LayoutManager2#addLayoutComponent(java.awt.Component, java.lang.Object)
         */
        public void  addLayoutComponent(Component comp, Object constraints)
        {
            if (comp == null)
            {
                throw new NullPointerException("Null component in addLayoutComponent");
            }
            comps.addElement(comp);
        }
        
        /* (non-Javadoc)
         * @see java.awt.LayoutManager2#getLayoutAlignmentX(java.awt.Container)
         */
        public float   getLayoutAlignmentX(Container target)
        {
            return (float)0.0;
        }
        
        /* (non-Javadoc)
         * @see java.awt.LayoutManager2#getLayoutAlignmentY(java.awt.Container)
         */
        public float   getLayoutAlignmentY(Container target)
        {
            return (float)0.0;
        }
        
        /* (non-Javadoc)
         * @see java.awt.LayoutManager2#invalidateLayout(java.awt.Container)
         */
        public void invalidateLayout(Container target)
        {
            preferredSize.setSize(0, 0);
            calcPreferredSize();
        }
        
        /* (non-Javadoc)
         * @see java.awt.LayoutManager2#maximumLayoutSize(java.awt.Container)
         */
        public Dimension maximumLayoutSize(Container target)
        {
            calcPreferredSize();
            return new Dimension(minimumSize);
        }
    }
    
    class TableTree implements Comparable<TableTree>
    {
        protected String            name;
        protected String            abbrev;
        protected String            field;
        protected TableTree         parent;
        protected Vector<TableTree> kids     = new Vector<TableTree>();
        protected DBTableIdMgr.TableInfo tableInfo = null;
        
        public TableTree(final TableTree parent, 
                         final String name,
                         final String abbrev,
                         final String field)
        {
            this.parent = parent;
            this.name   = name;
            this.abbrev = abbrev;
            this.field  = field;
            
            try
            {
                //Class<?> classObj = Class.forName("edu.ku.brc.specify.datamodel."+name);
                tableInfo = DBTableIdMgr.getInstance().getInfoByTableName(name.toLowerCase());
                if (tableInfo == null)
                {
                    System.err.println("Can't find tableInfo with table name["+name.toLowerCase()+"]");
                }
            } catch (Exception ex)
            {
                if (tableInfo == null)
                {
                    System.err.println("Can't find tableInfo with table name["+name.toLowerCase()+"]");
                }
                ex.printStackTrace();
            }
        }

        /**
         * @return the name
         */
        public String getName()
        {
            return name;
        }

        /**
         * @return the abbrev
         */
        public String getAbbrev()
        {
            return abbrev;
        }

        /**
         * @return the field
         */
        public String getField()
        {
            return field;
        }

        /**
         * @return the parent
         */
        public TableTree getParent()
        {
            return parent;
        }

        /**
         * @return the tableInfo
         */
        public DBTableIdMgr.TableInfo getTableInfo()
        {
            return tableInfo;
        }

        /**
         * @return the kids
         */
        public Vector<TableTree> getKids()
        {
            return kids;
        }

        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(TableTree o)
        {
            return name.compareTo(o.name);
        }
    }
    
}
