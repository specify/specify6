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

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Hashtable;
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
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.NavBoxLayoutManager;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.tasks.subpane.TableNameRenderer.TableNameRendererIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.MultiStateIconButon;

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
    protected Vector<TableInfo>              tableInfoList = new Vector<TableInfo>();
    protected JList                          fieldList;
    protected JList                          tableList;
    protected Hashtable<DBTableIdMgr.TableInfo, Vector<TableFieldPair>> tableFieldList = new Hashtable<DBTableIdMgr.TableInfo, Vector<TableFieldPair>>();
    
    protected Vector<QueryFieldPanel>        queryFieldItems = new Vector<QueryFieldPanel>();
    protected int                            currentInx      = -1;
    protected JPanel                         queryFieldsPanel;

    protected JButton                        addBtn;
    
    protected ImageIcon blankIcon   = IconManager.getIcon("BlankIcon", IconManager.IconSize.Std24);
    
    protected String            columnDefStr = null;

    /**
     * Constructor.
     * @param name name of subpanel
     * @param task the owning task
     */
    public QueryBldrPane(final String name,
                         final Taskable task)
    {
        super(name, task);
     
        createUI();
    }
    
    /**
     * 
     */
    protected void createUI()
    {
        String[] skipItems = {"TimestampCreated", "LastEditedBy", "TimestampModified"};
        Hashtable<String, String> skipHash = new Hashtable<String, String>();
        for (String nameStr : skipItems)
        {
            skipHash.put(nameStr, "X");
        }
        
        Hashtable<Class, Boolean> alreadyThere = new Hashtable<Class, Boolean>();
        for (DBTableIdMgr.TableInfo ti : DBTableIdMgr.getInstance().getList())
        {
            if (ti.isForQuery() && StringUtils.isNotEmpty(ti.toString()))
            {
                if (alreadyThere.get(ti.getClassObj()) == null)
                {
                    tableInfoList.add(new TableInfo(ti)); 
                
                    Vector<TableFieldPair> fldList = new Vector<TableFieldPair>();
                    /*if (ti.getClassObj() == Geography.class)
                    {
                        addGeographyFields(ti, fldList);
                        
                    } else if (ti.getClassObj() == Taxon.class)
                    {
                        addTaxonFields(ti, fldList);
                        
                    } else if (ti.getClassObj() == Location.class)
                    {
                        addLocationFields(ti, fldList);
                        
                    } else*/
                    {
                        for (DBTableIdMgr.FieldInfo fi : ti.getFields())
                        {
                            if (fi.getColumn() != null && skipHash.get(fi.getColumn()) == null)
                            {
                                fldList.add(new TableFieldPair(ti, fi));
                            }
                        }
                    }
                    Collections.sort(fldList);
                    tableFieldList.put(ti, fldList);
                    alreadyThere.put(ti.getClassObj(), true);                    
                }
            }
        }
        
        Vector<TableInfo> tempList = new Vector<TableInfo>();
        for (TableInfo ti : tableInfoList)
        {
            if (ti.getTableInfo().getClassObj() == Determination.class)
            {
                int x = 0;
                x++;
            }
            for (DBTableIdMgr.TableRelationship tr : ti.getTableInfo().getRelationships())
            {
                if (tr.getType() == DBTableIdMgr.RelationshipType.ManyToOne)
                {
                    DBTableIdMgr.TableInfo trTI = DBTableIdMgr.getInstance().getByClassName(tr.getClassName());
                    if (alreadyThere.get(trTI.getClassObj()) == null)
                    {
                        tempList.add(new TableInfo(trTI, ti.getTitle() + " - " + fixName(tr.getName()), trTI.getClassObj().getSimpleName()));
                        //alreadyThere.put(trTI.getClassObj(), true);
                        
                        Vector<TableFieldPair> fldList = new Vector<TableFieldPair>();
                        /*if (ti.getClassObj() == Geography.class)
                        {
                            addGeographyFields(ti, fldList);
                            
                        } else if (ti.getClassObj() == Taxon.class)
                        {
                            addTaxonFields(ti, fldList);
                            
                        } else if (ti.getClassObj() == Location.class)
                        {
                            addLocationFields(ti, fldList);
                            
                        } else*/
                        {
                            for (DBTableIdMgr.FieldInfo fi : trTI.getFields())
                            {
                                if (fi.getColumn() != null && skipHash.get(fi.getColumn()) == null)
                                {
                                    fldList.add(new TableFieldPair(trTI, fi));
                                }
                            }
                        }
                        Collections.sort(fldList);
                        tableFieldList.put(trTI, fldList);
                    }
                }
            }
        }

        tableInfoList.addAll(tempList);
        Collections.sort(tableInfoList);
        
        PanelBuilder    builder = new PanelBuilder(new FormLayout("f:p:g", "p,10px,f:p:g"), this);
        CellConstraints cc      = new CellConstraints();

        PanelBuilder innerBuilder = new PanelBuilder(new FormLayout("max(250px;p):g, 10px, max(250px;p):g, 2px, p, p", "p, 5px, f:p:g, p"));

        innerBuilder.add(new JLabel(getResourceString("WB_DATAOBJECTS"),     JLabel.CENTER), cc.xy(1, 1));
        innerBuilder.add(new JLabel(getResourceString("WB_DATAOBJ_FIELDS"),  JLabel.CENTER), cc.xy(3, 1));
        tableList = new JList(tableInfoList);
        tableList.setCellRenderer(new TableNameRenderer(IconManager.IconSize.Std24));
        
        JScrollPane sp = new JScrollPane(tableList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        innerBuilder.add(sp, cc.xywh(1, 3, 1, 2));
        
        tableList.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent e)
            {
                fieldList.setSelectedIndex(-1);
                fillFieldList(((TableInfo)tableList.getSelectedValue()).getTableInfo());
            }
        });
        
        fieldList = new JList(new DefaultListModel());
        fieldList.setCellRenderer(new FieldNameRenderer(IconManager.IconSize.Std16));
        
        sp = new JScrollPane(fieldList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        innerBuilder.add(sp, cc.xywh(3, 3, 1, 2));
        
        fieldList.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent e)
            {
                updateAddBtnState();
            }
        });
        
        fieldList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                TableFieldPair fieldItem = (TableFieldPair)fieldList.getSelectedValue();
                if (fieldItem != null && !fieldItem.isInUse() && e.getClickCount() == 2)
                {   
                    addQueryFieldItem(fieldItem);
                    updateAddBtnState();
                }
            }
        });
        
        addBtn = new JButton(IconManager.getImage("PlusSign", IconManager.IconSize.Std16));
        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                addQueryFieldItem((TableFieldPair)fieldList.getSelectedValue());
            }
        });
        
        innerBuilder.add(addBtn, cc.xy(5, 4));
        builder.add(innerBuilder.getPanel(), cc.xy(1, 1));
        
        queryFieldsPanel = new JPanel();
        queryFieldsPanel.setLayout(new NavBoxLayoutManager(0,2));
        sp = new JScrollPane(queryFieldsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        builder.add(sp, cc.xy(1, 3));
        
        builder.getPanel().setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        
        int initalIndex = 0;
        for (TableInfo ti : tableInfoList)
        {
            if (ti.getTableInfo().getClassObj() == CollectionObject.class)
            {
                tableList.setSelectedIndex(initalIndex);
                
                TableFieldPair tfp = tableFieldList.get(ti.getTableInfo()).get(0);
                addQueryFieldItem(tfp);
                tfp.setInUse(false);

               break; 
            }
            initalIndex++;
        }
        this.validate();
    }
    
    protected void updateAddBtnState()
    {
        TableFieldPair fieldItem = (TableFieldPair)fieldList.getSelectedValue();
        addBtn.setEnabled(fieldItem != null && !fieldItem.isInUse() );
    }

    
    /**
     * Fill in the JList's model from the list of fields.
     * @param tableInfo the table who's list we should use
     */
    protected void fillFieldList(DBTableIdMgr.TableInfo tableInfo)
    {
        DefaultListModel model = (DefaultListModel)fieldList.getModel();
        model.clear();
        //System.out.println(tableInfo+" "+tableFieldList.get(tableInfo));
        for (TableFieldPair fi : tableFieldList.get(tableInfo))
        {
            model.addElement(fi);
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
        qfp.getTableField().setInUse(false);
        queryFieldsPanel.validate();
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                fieldList.repaint();
                queryFieldsPanel.repaint();
            }
        });
    }
    
    /**
     * Add QueryFieldItem to the list created with a TableFieldPair.
     * @param fieldItem the TableFieldPair to be in the list
     */
    protected void addQueryFieldItem(final TableFieldPair fieldItem)
    {
        if (fieldItem != null)
        {
            final QueryFieldPanel qfp = new QueryFieldPanel(queryFieldItems.size(), fieldItem, IconManager.IconSize.Std24);
            queryFieldsPanel.add(qfp);
            queryFieldItems.add(qfp);
            fieldItem.setInUse(true);
            queryFieldsPanel.validate();
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    fieldList.repaint();
                    queryFieldsPanel.repaint();
                    qfp.repaint();
                }
            });
        }
    }
    protected String fixName(final String name)
    {
        StringBuilder s = new StringBuilder();
        for (int i=0;i<name.length();i++)
        {
            if (i == 0) 
            {
                s.append(Character.toUpperCase(name.charAt(i)));
            } else
            {
                char c = name.charAt(i);
                if (Character.isUpperCase(c))
                {
                    s.append(' ');
                }
                s.append(c);
            }
        }
        return s.toString();  
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
        
        
        protected TableFieldPair   tblField = null;
        
        protected QueryFieldPanel  thisItem;
        
        protected String[] labelStrs = {" ", "Field", "Not", "Operator", "Criteria", "Sort", "Display", " ", " "};
        
        
        /**
         * Constructor.
         * @param fieldName the field Name
         * @param icon the icon to use once it is mapped
         */
        public QueryFieldPanel(final int row, 
                               final TableFieldPair tableField, 
                               final IconManager.IconSize iconSize)
        {
            this.tblField = tableField;
            
            thisItem = this;
            
            int[] widths = buildControlLayout(iconSize, row == 0);
            if (row == 0)
            {
                removeAll();
                buildLabelLayout(iconSize, widths);
            }
        }
        
        protected int[] buildControlLayout(final IconManager.IconSize iconSize, final boolean returnWidths)
        {
            iconLabel     = new JLabel(icon);
            fieldLabel    = new JLabel(fixName(tblField.getTitle()));
            isNotCheckbox = new JCheckBox("");
            operatorCBX   = new JComboBox(new String[] { "Like", "Contains"});
            criteria      = new JTextField();
            sortCheckbox  = new MultiStateIconButon(new ImageIcon[] {
                                IconManager.getImage("GrayDot",   IconManager.IconSize.Std16),
                                IconManager.getImage("UpArrow",   IconManager.IconSize.Std16),
                                IconManager.getImage("DownArrow", IconManager.IconSize.Std16)});
            //sortCheckbox.setMargin(new Insets(2,2,2,2));
            //sortCheckbox.setBorder(BorderFactory.createLineBorder(new Color(225,225,225)));
            isDisplayedCkbx = new JCheckBox("");
            closeBtn = new JLabel(IconManager.getIcon("Close"));
            

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
            
            System.out.println(sb.toString());
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

            icon = IconManager.getIcon(tblField.getTableinfo().getObjTitle(), iconSize);
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

            System.out.println(sb.toString());
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
         * @return the splt apart name
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
        public TableFieldPair getTableField()
        {
            return tblField;
        }

        /**
         * Returns the field name.
         * @return the field name.
         */
        public String getFieldName()
        {
            return fieldLabel.getText();
        }
    }
    
    //------------------------------------------------------------------
    //-- 
    //------------------------------------------------------------------
    class TableInfo implements TableNameRendererIFace, Comparable<TableInfo>
    {
        protected DBTableIdMgr.TableInfo tableInfo;
        protected String                 title;
        protected String                 iconName;
        
        public TableInfo(final DBTableIdMgr.TableInfo tableInfo)
        {
            this.tableInfo = tableInfo;
            this.title     = tableInfo.toString();
            this.iconName  = tableInfo.getClassObj().getSimpleName();
        }

        public TableInfo(final DBTableIdMgr.TableInfo tableInfo,
                         final String                 title,
                         final String                 iconName)
        {
            this.tableInfo = tableInfo;
            this.title     = title;
            this.iconName  = iconName;
        }

        /**
         * @return the tableInfo
         */
        public DBTableIdMgr.TableInfo getTableInfo()
        {
            return tableInfo;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.specify.tasks.subpane.TableNameRenderer.TableNameRendererIFace#getIconName()
         */
        public String getIconName()
        {
            return iconName;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.specify.tasks.subpane.TableNameRenderer.TableNameRendererIFace#getTitle()
         */
        public String getTitle()
        {
            return title;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(TableInfo obj)
        {
            return title.compareTo(obj.title);
        }
    }
}
