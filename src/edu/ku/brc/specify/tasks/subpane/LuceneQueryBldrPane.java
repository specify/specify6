/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */

package edu.ku.brc.specify.tasks.subpane;

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.Color;
import java.awt.Component;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
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
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.ERTICaptionInfo;
import edu.ku.brc.af.core.ERTIColInfo;
import edu.ku.brc.af.core.ExpressResultsTableInfo;
import edu.ku.brc.af.core.NavBoxLayoutManager;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.tasks.ExpressSearchTask;
import edu.ku.brc.specify.tasks.subpane.TableNameRenderer.TableNameRendererIFace;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.MultiStateIconButon;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 23, 2007
 *
 */
public class LuceneQueryBldrPane extends BaseSubPane
{
    protected Vector<TableInfo>              tableInfoList = null;
    protected JList                          fieldList;
    protected JList                          tableList;
    protected Hashtable<DBTableIdMgr.TableInfo, Vector<KeyColInfo>> tableFieldList = new Hashtable<DBTableIdMgr.TableInfo, Vector<KeyColInfo>>();
    
    protected Vector<QueryFieldPanel>        queryFieldItems = new Vector<QueryFieldPanel>();
    protected int                            currentInx      = -1;
    protected JPanel                         queryFieldsPanel;

    protected JButton                        addBtn;
    protected JButton                        searchBtn;
    
    protected ImageIcon blankIcon   = IconManager.getIcon("BlankIcon", IconManager.IconSize.Std24);
    
    protected String                         columnDefStr  = null;
    protected Element                        esDOM         = null;
    protected List<ExpressResultsTableInfo>  ertiList      = null;

    /**
     * Constructor.
     * @param name name of subpanel
     * @param task the owning task
     */
    public LuceneQueryBldrPane(final String name,
                               final Taskable task)
    {
        super(name, task);
        
        if (esDOM == null)
        {
            esDOM = XMLHelper.readDOMFromConfigDir("backstop/search_config.xml");         // Describes the definitions of the full text search
        }

        List<?> tables = esDOM.selectNodes("/searches/express/table");
        ertiList = new ArrayList<ExpressResultsTableInfo>(tables.size());
        for (Object obj : tables)
        {
            Element tableElement = (Element)obj;
            ExpressResultsTableInfo tableInfo = new ExpressResultsTableInfo(tableElement, true);
            if (isNotEmpty(tableInfo.getBuildSql()))
            {
                ertiList.add(tableInfo);
            }
        }
     
        createUI();
    }
    
    /**
     * 
     */
    protected void createUI()
    {
        Vector<TableInfo> tempList = new Vector<TableInfo>();
        for (ExpressResultsTableInfo erti : ertiList)
        {
            Vector<KeyColInfo> fldList = new Vector<KeyColInfo>();
            for (ERTIColInfo colInfo : erti.getColInfo())
            {
                if (StringUtils.isNotEmpty(colInfo.getSecondaryKey()))
                {
                    fldList.add(new KeyColInfo(erti, colInfo));
                }
            }
            if (fldList.size() > 0)
            {
                DBTableIdMgr.TableInfo tblInfo = DBTableIdMgr.getInstance().getInfoById(Integer.parseInt(erti.getTableId()));
                
                String title = erti.getTitle();// + " - " + UIHelper.makeNamePretty(colInfo.getColName());
                
                tempList.add(new TableInfo(tblInfo, title, tblInfo.getClassObj().getSimpleName()));
                Collections.sort(fldList);
                tableFieldList.put(tblInfo, fldList);
            }
        }

        tableInfoList = new Vector<TableInfo>();
        tableInfoList.addAll(tempList);
        Collections.sort(tableInfoList);
        
        PanelBuilder    builder = new PanelBuilder(new FormLayout("f:p:g", "p,10px,f:p:g,2px,p"), this);
        CellConstraints cc      = new CellConstraints();

        PanelBuilder innerBuilder = new PanelBuilder(new FormLayout("max(250px;p):g, 10px, max(250px;p):g, 2px, p, p", "p, 5px, f:p:g, p"));

        innerBuilder.add(new JLabel(getResourceString("WB_DATAOBJECTS"),     SwingConstants.CENTER), cc.xy(1, 1));
        innerBuilder.add(new JLabel(getResourceString("WB_DATAOBJ_FIELDS"),  SwingConstants.CENTER), cc.xy(3, 1));
        tableList = new JList(tableInfoList);
        tableList.setCellRenderer(new TableNameRenderer(IconManager.IconSize.Std24));
        
        JScrollPane sp = new JScrollPane(tableList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        innerBuilder.add(sp, cc.xywh(1, 3, 1, 2));
        
        tableList.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent e)
            {
                fieldList.setSelectedIndex(-1);
                fillFieldList(((TableInfo)tableList.getSelectedValue()).getTableInfo());
            }
        });
        
        fieldList = new JList(new DefaultListModel());
        fieldList.setCellRenderer(new ERTIFieldNameRenderer(IconManager.IconSize.Std16));
        
        sp = new JScrollPane(fieldList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        innerBuilder.add(sp, cc.xywh(3, 3, 1, 2));
        
        fieldList.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent e)
            {
                updateAddBtnState();
            }
        });
        
        fieldList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                KeyColInfo fieldItem = (KeyColInfo)fieldList.getSelectedValue();
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
                addQueryFieldItem((KeyColInfo)fieldList.getSelectedValue());
            }
        });
        
        innerBuilder.add(addBtn, cc.xy(5, 4));
        builder.add(innerBuilder.getPanel(), cc.xy(1, 1));
        
        queryFieldsPanel = new JPanel();
        queryFieldsPanel.setLayout(new NavBoxLayoutManager(0,2));
        sp = new JScrollPane(queryFieldsPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        builder.add(sp, cc.xy(1, 3));
        
        builder.getPanel().setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        
        int initalIndex = 0;
        for (TableInfo ti : tableInfoList)
        {
            if (ti.getTableInfo().getClassObj() == CollectionObject.class)
            {
                tableList.setSelectedIndex(initalIndex);
                
                KeyColInfo erti = tableFieldList.get(ti.getTableInfo()).get(0);
                addQueryFieldItem(erti);
                erti.setInUse(false);

               break; 
            }
            initalIndex++;
        }
        
        searchBtn = new JButton("Search");
        PanelBuilder searchPanel = new PanelBuilder(new FormLayout("r:p", "p"));
        searchPanel.add(searchBtn, cc.xy(1,1));
        
        builder.add(searchPanel.getPanel(), cc.xy(1, 5));
        
        searchBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                CommandDispatcher.dispatch(new CommandAction(ExpressSearchTask.EXPRESSSEARCH, "Search", "101"));
            }
        });
        this.validate();
    }
    
    protected void updateAddBtnState()
    {
        KeyColInfo fieldItem = (KeyColInfo)fieldList.getSelectedValue();
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
        for (KeyColInfo fi : tableFieldList.get(tableInfo))
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
     * Add QueryFieldItem to the list created with a KeyColInfo.
     * @param fieldItem the KeyColInfo to be in the list
     */
    protected void addQueryFieldItem(final KeyColInfo fieldItem)
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
        protected JTextField       criteria;
        
        protected KeyColInfo       tblField = null;
        
        protected QueryFieldPanel  thisItem;
        
        protected String[] labelStrs = {" ", "Field", "Criteria", " ", " "};
        
        
        /**
         * Constructor.
         * @param fieldName the field Name
         * @param icon the icon to use once it is mapped
         */
        public QueryFieldPanel(final int row, 
                               final KeyColInfo tableField, 
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
            fieldLabel    = new JLabel(fixName(tblField.getColInfo().getColName()));
            criteria      = new JTextField();
            closeBtn = new JLabel(IconManager.getIcon("Close"));

            JComponent[] comps = {iconLabel, fieldLabel, criteria, closeBtn, null};

            StringBuilder sb = new StringBuilder();
            if (columnDefStr == null)
            {
                for (int i=0;i<comps.length;i++)
                {
                    sb.append(i == 0 ? "" : ",");
                    sb.append("p");
                    if (i == 2) sb.append(":g");
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

            icon = IconManager.getIcon(tblField.getErti().getName(), iconSize);
            setIcon(icon);
            
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
                labels[i] = new JLabel(labelStrs[i], SwingConstants.CENTER);
                labelWidths[i] = Math.max(widths[i], labels[i].getPreferredSize().width);
            }
            
            for (int i=0;i<labels.length;i++)
            {
                sb.append(i == 0 ? "" : ",");
                sb.append("max(");
                sb.append(labelWidths[i]);
                sb.append(";p)");
                if (i == 2) sb.append(":g");
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
        public KeyColInfo getTableField()
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
    
    class KeyColInfo implements Comparable<KeyColInfo>
    {
        protected ExpressResultsTableInfo erti;
        protected ERTIColInfo             colInfo;
        protected boolean                 isInUse = false;
        
        public KeyColInfo(ExpressResultsTableInfo erti, ERTIColInfo colInfo)
        {
            super();
            this.erti = erti;
            this.colInfo = colInfo;
        }
        /**
         * @return the colInfo
         */
        public ERTIColInfo getColInfo()
        {
            return colInfo;
        }
        /**
         * @return the erti
         */
        public ExpressResultsTableInfo getErti()
        {
            return erti;
        }
        
        /**
         * @return the isInUse
         */
        public boolean isInUse()
        {
            return isInUse;
        }
        /**
         * @param isInUse the isInUse to set
         */
        public void setInUse(boolean isInUse)
        {
            this.isInUse = isInUse;
        }
        
        //----------------------------------------------------------------------
        //-- Comparable Interface
        //----------------------------------------------------------------------

        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(KeyColInfo obj)
        {
            return colInfo.getColName().compareTo(obj.colInfo.getColName());
        } 
    }
    
    public class ERTIFieldNameRenderer extends DefaultListCellRenderer 
    {
        protected ImageIcon checkMark;
        protected ImageIcon blankIcon;
        
        public ERTIFieldNameRenderer(IconManager.IconSize iconSize) 
        {
            // Don't paint behind the component
            this.setOpaque(false);
            checkMark   = IconManager.getIcon("Checkmark", iconSize);
            blankIcon   = IconManager.getIcon("BlankIcon", iconSize);
        }

        public Component getListCellRendererComponent(JList list,
                                                      Object value,   // value to display
                                                      int index,      // cell index
                                                      boolean iss,    // is the cell selected
                                                      boolean chf)    // the list and the cell have the focus
        {
            super.getListCellRendererComponent(list, value, index, iss, chf);

            KeyColInfo tblField = (KeyColInfo)value;
            setIcon(tblField.isInUse() ? checkMark : blankIcon);
            
            if (iss) {
                setOpaque(true);
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                list.setSelectedIndex(index);

            } else {
                this.setOpaque(false);
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            setText(tblField.getColInfo().getColName());
            return this;
        }

    }
}
