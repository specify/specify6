/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.af.core.expresssearch;

import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createI18NLabel;
import static edu.ku.brc.ui.UIHelper.createIconBtn;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ToggleButtonChooserPanel;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * Enables a user to configure what fields they want to search and display for ExpressSearch.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Sep 6, 2007
 *
 */
public class ExpressSearchConfigDlg extends CustomDialog
{
    public static String NONE       = null;
    public static String ASCENDING  = null;
    public static String DESCENDING = null;
    
    protected static final String[] TAB_HELP_CONTEXT = {"ESConfigSearchFields", "ESConfigRelatedTables", "ESConfigResultsOrdering"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    
    protected JTabbedPane                    tabbedPane;
    protected JList                          tableList;
    protected JList                          toBeSearchedList;
    protected Vector<SearchFieldConfig>      toBeSearchedVect      = new Vector<SearchFieldConfig>();
    protected ESTableOrderPanel              orderPanel            = null;
    protected Component                      currTabComp           = null;
    
    protected ToggleButtonChooserPanel<DisplayFieldConfig> displayList;
    
    protected Hashtable<Integer, SearchTableConfig> tiRenderHash = new Hashtable<Integer, SearchTableConfig> ();
    protected Vector<SearchTableConfig>             tiRenderList = new Vector<SearchTableConfig>();
    
    protected JTable                         searchFieldsTable;
    protected SearchFieldsTableModel         searchFieldsTableModel;
    protected JButton                        orderUpBtn;
    protected JButton                        orderDwnBtn;
    
    protected Comparator<TableNameRendererIFace> toBeSearchedComparator = getTableFieldNameComparator();
    
    protected SearchConfigService            searchConfigService = SearchConfigService.getInstance();
    protected SearchConfig                   config              = new SearchConfig();
    
    static
    {
        NONE       = getI18NStr("ES_NONE"); //$NON-NLS-1$
        ASCENDING  = getI18NStr("ES_ASCENDING"); //$NON-NLS-1$
        DESCENDING = getI18NStr("ES_DESCENDING"); //$NON-NLS-1$
    }
    
    /**
     * Default Constructor.
     */
    public ExpressSearchConfigDlg()
    {
        super((Frame)UIRegistry.getTopWindow(), getI18NStr("ES_DLG_TITLE"), true, OKHELP, null); //$NON-NLS-1$
        
        setOkLabel(getI18NStr("CLOSE")); //$NON-NLS-1$
        
        /*
        Locale german = new Locale("de", "", "");
        SchemaI18NService.setCurrentLocale(german);
        SchemaI18NService.getInstance().loadWithLocale((byte)0, 1, DBTableIdMgr.getInstance(), german); 
        */

        config = searchConfigService.getSearchConfig();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        HelpMgr.registerComponent(helpBtn, TAB_HELP_CONTEXT[0]);
        
        int maxDisplayCnt = 0;
        int maxSearchCnt  = 0;
        
        Hashtable<String, List<ExpressResultsTableInfo>> joinHash = ExpressSearchConfigCache.getJoinIdToTableInfoHash();
        Vector<DBTableInfo>                   tableListInfoWithJoins = new Vector<DBTableInfo>();

        for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
        {
            if (ti.isSearchable())
            {
                List<DBFieldInfo> fieldList = searchConfigService.getPruncedFieldList(ti);
                
                int     notSortedIndex   = 1000;
                boolean hasIndexedFields = false;
                for (DBFieldInfo fi : fieldList)
                {
                    if (fi.isIndexed())
                    {
                        hasIndexedFields = true;
                        break;
                    }
                }
                
                if (!hasIndexedFields)
                {
                    continue;
                }
                
                SearchTableConfig stc = config.findTableOrCreate(ti.getClassObj().getSimpleName());
                stc.setTableInfo(ti);
                
                int displayCnt = 0;
                int searchCnt  = 0;
                
                for (DBFieldInfo fi : fieldList)
                {
                    if (fi.isHidden())
                    {
                        if (config.findSearchField(stc, fi.getName(), true) != null)
                        {
                            config.removeSearchField(stc.getTableName(), fi.getName());
                        }
                        if (config.findDisplayField(stc, fi.getName(), true) != null)
                        {
                            config.removeDisplayField(stc.getTableName(), fi.getName());
                        }
                        continue;
                    }
                    
                    if (fi.isIndexed())
                    {
                        // If found it sets inUse to true, otherwise it is false when created
                        SearchFieldConfig sfc = config.findSearchField(stc, fi.getName(), true);
                        sfc.setFieldInfo(fi);
                        sfc.setStc(stc);
                        if (sfc.getOrder() == null)
                        {
                            sfc.setOrder(notSortedIndex++);
                        }
                        searchCnt++;
                    }
                    
                    if (!fi.isHidden())
                    {
                        // If found it sets inUse to true, otherwise it is false when created
                        DisplayFieldConfig dfc = config.findDisplayField(stc, fi.getName(), true);
                        dfc.setFieldInfo(fi);
                        dfc.setStc(stc);
                    }
                    
                    displayCnt++;
                }
                
                // Remove hidden table after processing the fields so any shown/searched fields can be removed.
                if (ti.isHidden())
                {
                    config.removeTable(stc);
                }
                
                // Do this after the hideden table has been removed
                List<ExpressResultsTableInfo> joinList = joinHash.get(Integer.toString(ti.getTableId()));
                if (joinList != null)
                {
                    tableListInfoWithJoins.add(ti);
                }
                
                maxDisplayCnt = Math.max(maxDisplayCnt, displayCnt);
                maxSearchCnt  = Math.max(maxSearchCnt, searchCnt);
                
                Collections.sort(stc.getSearchFields());
                Collections.sort(stc.getDisplayFields());
            }
        } // For
        
        Collections.sort(config.getTables());
        for (SearchTableConfig stc : config.getTables())
        {
            if (AppContextMgr.isSecurityOn())
            {
                PermissionSettings perm = stc.getTableInfo().getPermissions(); 
                if (!perm.canView())
                {
                    continue;
                }
            }
            
            tiRenderList.add(stc);
            tiRenderHash.put(stc.getTableInfo().getTableId(), stc);
        }
        Collections.sort(tiRenderList);
        
        tableList = new JList(tiRenderList);
        TableNameRenderer nameRender = new TableNameRenderer(IconManager.IconSize.Std24);
        nameRender.setUseIcon("PlaceHolder"); //$NON-NLS-1$
        tableList.setCellRenderer(nameRender);
        
        //----------------------------------------
        //-- Search Fields Table
        //----------------------------------------

        searchFieldsTableModel = new SearchFieldsTableModel();
        
        searchFieldsTable = new JTable(searchFieldsTableModel);
        searchFieldsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            //@Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    updateEnabledState();
                }
            }
        });
        UIHelper.makeTableHeadersCentered(searchFieldsTable, false);
        
        String[] values = new String[] {NONE, ASCENDING, DESCENDING};
        
        // Set the combobox editor on the 1st visible column
        TableColumn col = searchFieldsTable.getColumnModel().getColumn(2);
        MyComboBoxEditor myCBXEditor = new MyComboBoxEditor(values);
        col.setCellEditor(myCBXEditor);
        searchFieldsTable.setRowHeight(createComboBox().getPreferredSize().height);
        col.setCellRenderer(new MyComboBoxRenderer(values));
        
        orderUpBtn = createIconBtn("ReorderUp", "WB_MOVE_UP", new ActionListener() //$NON-NLS-1$ //$NON-NLS-2$
        {
            public void actionPerformed(ActionEvent ae)
            {
                int inx = searchFieldsTable.getSelectedRow();
                searchFieldsTableModel.moveRowUp(inx);
                searchFieldsTable.setRowSelectionInterval(inx-1, inx-1);
                updateEnabledState();
            }
        });
        orderDwnBtn = createIconBtn("ReorderDown", "WB_MOVE_DOWN", new ActionListener() //$NON-NLS-1$ //$NON-NLS-2$
        {
            public void actionPerformed(ActionEvent ae)
            {
                int inx = searchFieldsTable.getSelectedRow();
                searchFieldsTableModel.moveRowDown(inx);
                searchFieldsTable.setRowSelectionInterval(inx+1, inx+1);
                updateEnabledState();
            }
        });
        
        // Build Layout

        PanelBuilder    outer   = new PanelBuilder(new FormLayout("p,6px,f:max(250px;p):g", "p,2px,f:p:g")); //$NON-NLS-1$ //$NON-NLS-2$
        PanelBuilder    builder = new PanelBuilder(new FormLayout("f:p:g", "p,10px,f:p:g")); //$NON-NLS-1$ //$NON-NLS-2$
        CellConstraints cc      = new CellConstraints();

        PanelBuilder innerBuilder = new PanelBuilder(new FormLayout("max(250px;p):g, 2px, p, 10px, max(250px;p):g", "p,2px,f:min(250px;p):g")); //$NON-NLS-1$ //$NON-NLS-2$

        innerBuilder.add(createI18NLabel("ExpressSearchConfigDlg.ES_SEARCHFIELDS",   SwingConstants.CENTER), cc.xy(1, 1)); //$NON-NLS-1$
        innerBuilder.add(createI18NLabel("ExpressSearchConfigDlg.ES_DISPLAYFIELDS",  SwingConstants.CENTER), cc.xy(5, 1)); //$NON-NLS-1$
        
        PanelBuilder upDownPanel = new PanelBuilder(new FormLayout("p", "f:p:g, p, 2px, p, f:p:g"));         //$NON-NLS-1$ //$NON-NLS-2$
        upDownPanel.add(orderUpBtn,       cc.xy(1, 2));
        upDownPanel.add(orderDwnBtn,      cc.xy(1, 4));
        innerBuilder.add(upDownPanel.getPanel(), cc.xy(3, 3));

        displayList = new ToggleButtonChooserPanel<DisplayFieldConfig>(maxDisplayCnt, ToggleButtonChooserPanel.Type.Checkbox);
        displayList.setUseScrollPane(true);
        displayList.setOkBtn(okBtn);
        displayList.setActionListener(new ActionListener() 
        {
            //@Override
            public void actionPerformed(ActionEvent e)
            {
                final JToggleButton tb = (JToggleButton)e.getSource();
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        itemDisplayChecked(tb);
                    }
                });
            }
        });        
        displayList.createUI();
        
        //innerBuilder.add(searchList.getUIComponent(), cc.xy(1, 3));
        JScrollPane sp = new JScrollPane(searchFieldsTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        innerBuilder.add(sp, cc.xy(1, 3));
        innerBuilder.add(displayList.getUIComponent(), cc.xy(5, 3));
        
        tableList.setVisibleRowCount(10);
        sp = new JScrollPane(tableList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        builder.add(sp, cc.xy(1,1));
        builder.add(innerBuilder.getPanel(), cc.xy(1,3));
        
        tableList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            //@Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    tableSelected();
                }
            }
        });
        
        toBeSearchedList = new JList();
        toBeSearchedList.setCellRenderer(nameRender);
        toBeSearchedList.setModel(new VecModel());
        sp = new JScrollPane(toBeSearchedList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        toBeSearchedList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            //@Override
            public void valueChanged(ListSelectionEvent e)
            {
                toBeSearchedSelected();
            }
        });

        outer.add(createI18NLabel("ExpressSearchConfigDlg.ES_AVAIL_TABLES", SwingConstants.CENTER), cc.xy(1,1)); //$NON-NLS-1$
        outer.add(builder.getPanel(), cc.xy(1,3));
        outer.add(createI18NLabel("ExpressSearchConfigDlg.ES_FLDS_TO_SEARCH", SwingConstants.CENTER), cc.xy(3,1)); //$NON-NLS-1$
        outer.add(sp, cc.xy(3,3));
        
        orderPanel = new ESTableOrderPanel(config);
         
        // Preload the right-hand list
        if (config != null)
        {
            for (SearchTableConfig stc : config.getTables())
            {
                for (SearchFieldConfig sfc : stc.getSearchFields())
                {
                    if (sfc.isInUse())
                    {
                        toBeSearchedVect.add(sfc);
                    }
                }
            }
            Collections.sort(toBeSearchedVect, toBeSearchedComparator);
        }
        
        orderPanel.loadOrderList(toBeSearchedVect);
        
        // Create TabbedPane and add tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.add(getI18NStr("ES_SEARCH_FIELDS"), outer.getPanel()); //$NON-NLS-1$
        tabbedPane.add(getI18NStr("ES_RELATED_TABLES"), new RelatedTableInfoPanel(config)); //$NON-NLS-1$
        tabbedPane.add(getI18NStr("ES_RESULTS_ORDERING"), orderPanel); //$NON-NLS-1$

        tabbedPane.addChangeListener(new ChangeListener() {
            //@Override
            public void stateChanged(ChangeEvent e)
            {
                tabChanged();
            }
        });
        
        JPanel tPanel = new JPanel(new BorderLayout());
        tPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        tPanel.add(createI18NLabel("ExpressSearchConfigDlg.ES_EXPLAIN", SwingConstants.CENTER), BorderLayout.NORTH); //$NON-NLS-1$
        tPanel.add(tabbedPane, BorderLayout.CENTER);
        
        contentPanel = tPanel;
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        pack();
    }
    
    /**
     * @param subKey
     * @return
     */
    private static String getI18NStr(final String subKey)
    {
        return getResourceString("ExpressSearchConfigDlg." + subKey);
    }
    
    /**
     * @return 0, 1, -1
     */
    public static Comparator<TableNameRendererIFace> getTableFieldNameComparator()
    {
        return new Comparator<TableNameRendererIFace>() {
            //@Override
            public int compare(TableNameRendererIFace o1, TableNameRendererIFace o2)
            {
                return makeName(o1).compareTo(makeName(o2));
            }
        };
    }
    
    /**
     * Helper for constructing name of table/field needed for sorting.
     * @param tnr the renderable
     * @return the name
     */
    protected static String makeName(final TableNameRendererIFace tnr)
    {
        SearchFieldConfig sfc = (SearchFieldConfig)tnr;
        return sfc.getFieldInfo().getTableInfo().getName() + sfc.getFieldInfo().getColumn();
    }
    
    /**
     * Update the order up/down btns enabled state.
     */
    protected void updateEnabledState()
    {
        int inx = searchFieldsTable.getSelectedRow();
        orderUpBtn.setEnabled(inx > 0);
        orderDwnBtn.setEnabled(inx > -1 && inx < searchFieldsTableModel.getRowCount()-1);
    }
    
    /**
     * Watches for Tab changes so the Order of the searches panel can be
     * initialized or the order retrieved.
     */
    protected void tabChanged()
    {
        int index = tabbedPane.getSelectedIndex();
        HelpMgr.registerComponent(helpBtn, TAB_HELP_CONTEXT[index]);
        
        Component comp = tabbedPane.getComponent(index);
        if (comp == orderPanel)
        {
            orderPanel.loadOrderList(toBeSearchedVect);
            
        } else if (currTabComp == orderPanel)
        {
            orderPanel.grabOrderInList();
        }
        currTabComp = comp;
    }
    
    /**
     * 
     */
    protected void toBeSearchedSelected()
    {
        SearchFieldConfig str = (SearchFieldConfig)toBeSearchedList.getSelectedValue();
        if (str != null)
        {
            int i = 0;
            for (SearchTableConfig stc : tiRenderList)
            {
                if (str.getStc().getTableInfo() == stc.getTableInfo())
                {
                    tableList.setSelectedIndex(i);
                    tableList.ensureIndexIsVisible(i);
                    break;
                }
                i++;
            }
        }
    }
    
    /**
     * @return the number of search fields checked on
     */
    protected int getCountSelectedSearchFields()
    {
        int cnt = 0;
        for (int i=0;i<searchFieldsTableModel.getRowCount();i++)
        {
            SearchFieldConfig sfc = (SearchFieldConfig)searchFieldsTableModel.getFields().get(i);
            if (sfc.isInUse())
            {
                cnt++;
            }
        }
        return cnt;
    }
    
    /**
     * Sets all the display btns enabled or not.
     * @param enable true enable, false not
     * @param doClearCheck true indicates it should uncheck the display field
     */
    protected void enableSearchBtns(final boolean enable, final boolean doClearCheck)
    {
        for (JToggleButton btn : displayList.getButtons())
        {
            DisplayFieldConfig dfc = displayList.getItemForBtn(btn);
            if (dfc != null)
            {
                if (doClearCheck)
                {
                    dfc.setInUse(false);
                }
                if (!enable)
                {
                    btn.setSelected(false);
                }
                btn.setEnabled(enable);
            }
        }
    }
    
    /**
     * @param btn
     */
    protected void itemDisplayChecked(final JToggleButton btn)
    {
        DisplayFieldConfig dfc = displayList.getItemForBtn(btn);
        if (btn.isSelected())
        {
            dfc.setInUse(true);
            //config.addDisplayField(dfc.getStc().getTableInfo().getShortClassName(), dfc);
        } else
        {
            dfc.setInUse(false);
            //config.removeDisplayField(dfc.getStc().getTableInfo().getShortClassName(), dfc.getFieldName());
        }
    }
    
    
    /**
     * @param index
     */
    protected void tableSelected()
    {
        SearchTableConfig stc = (SearchTableConfig)tableList.getSelectedValue();
        if (stc != null)
        {
            Collections.sort(stc.getDisplayFields());
            
            displayList.setItems(stc.getDisplayFields());
            
            searchFieldsTableModel.add(stc.getSearchFields());
            searchFieldsTable.getSelectionModel().clearSelection();
            
            for (SearchFieldConfig sfc : stc.getSearchFields())
            {
                if (sfc.isInUse())
                {
                    autoSelectDisplayListField(sfc, true);
                }
            }
           
            for (DisplayFieldConfig dfc : stc.getDisplayFields())
            {
                if (dfc.isInUse())
                {
                    displayList.setSelectedObj(dfc);
                }
            }
        } else
        {
            displayList.setItems(null);
            searchFieldsTableModel.add(null);
            searchFieldsTable.getSelectionModel().clearSelection();
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#cancelButtonPressed()
     */
    @Override
    protected void cancelButtonPressed()
    {
        pruneTree();
        
        super.cancelButtonPressed();
    }
    
    /**
     * 
     */
    protected void pruneTree()
    {
        // Now Prune out anything that is not being used
        for (SearchTableConfig stc : new Vector<SearchTableConfig>(config.getTables()))
        {
            if (stc.hasConfiguredSearchFields())
            {
                for (SearchFieldConfig sfc : new Vector<SearchFieldConfig>(stc.getSearchFields()))
                {
                    if (!sfc.isInUse())
                    {
                        stc.getSearchFields().remove(sfc);
                    }
                }
                
                for (DisplayFieldConfig dfc : new Vector<DisplayFieldConfig>(stc.getDisplayFields()))
                {
                    if (!dfc.isInUse())
                    {
                        stc.getDisplayFields().remove(dfc);
                    }
                }
                
                if (!stc.hasConfiguredSearchFields())
                {
                    config.removeTable(stc);
                }
                
                // They need to be sorted because the UI doesn't do that
                // the UI only sorts visually an din the UI models
                Collections.sort(stc.getSearchFields());
                Collections.sort(stc.getDisplayFields());
                
            } else
            {
                config.removeTable(stc);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
    @Override
    protected void okButtonPressed()
    {
        pruneTree();
        
        orderPanel.saveChanges(toBeSearchedVect);
        
        XStream xstream = new XStream();
        SearchConfig.configXStream(xstream);
        
        // This is for testing only RELEASE
        /*try
        {
            FileUtils.writeStringToFile(new File("esconfig.xml"), xstream.toXML(config)); //$NON-NLS-1$
            //System.out.println(xstream.toXML(config));
            
        } catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ExpressSearchConfigDlg.class, ex);
            ex.printStackTrace();
        }*/
        
        searchConfigService.saveConfig();
        
        super.okButtonPressed();
    }
    
    // I can't believe I have to make my own class just to have the fire happen
    class VecModel extends AbstractListModel 
    {
        public int getSize() { return toBeSearchedVect.size(); }
        public Object getElementAt(int index) { return toBeSearchedVect.get(index); }
        public void fireChange(@SuppressWarnings("unused")final DBFieldInfo field) //$NON-NLS-1$
        {
            fireContentsChanged(this, 0, toBeSearchedVect.size());
        }
    }
    
    //------------------------------------------------------------------------------
    //--
    //------------------------------------------------------------------------------
    class SearchFieldsTableModel extends AbstractTableModel
    {
        protected Vector<String>             headings  = new Vector<String>();
        protected Vector<SearchFieldConfig>  fields    = new Vector<SearchFieldConfig>();
        protected Class<?>[]                 classes   = {Boolean.class, String.class, String.class};
        
        /**
         * 
         */
        public SearchFieldsTableModel()
        {
            String[] heads = {"ExpressSearchConfigDlg.SEARCH","ExpressSearchConfigDlg.ES_FIELDNAME", "ExpressSearchConfigDlg.ES_SORTING"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            for (String key : heads)
            {
                headings.add(getResourceString(key));
            }
        }
        
        public void add(final Vector<SearchFieldConfig> flds)
        {
            fields = flds;
            
            if (fields != null)
            {
                
                int i = 0;
                for (SearchFieldConfig sfc : fields)
                {
                    sfc.setOrder(i++);
                }
                fireTableRowsInserted(0, flds.size());
            } else
            {
                fireTableDataChanged();
            }
            
        }
        
        public void moveRowUp(final int index)
        {
            SearchFieldConfig sfc = fields.get(index);
            fields.remove(index);
            fields.insertElementAt(sfc, index-1);
            fields.get(index-1).setOrder(index-1);
            fields.get(index).setOrder(index);
            fireTableRowsUpdated(index-1, index);
        }
        
        public void moveRowDown(final int index)
        {
            SearchFieldConfig sfc = fields.get(index);
            fields.remove(index);
            fields.insertElementAt(sfc, index+1);
            fields.get(index).setOrder(index);
            fields.get(index+1).setOrder(index+1);
            fireTableRowsUpdated(index, index+1);
        }
        
        public Vector<SearchFieldConfig> getFields()
        {
            return fields;
        }
        
        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return classes[columnIndex];
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        //@Override
        public int getColumnCount()
        {
            return headings.size();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int columnIndex)
        {
            return headings.get(columnIndex);
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getRowCount()
         */
        //@Override
        public int getRowCount()
        {
            return fields == null ? 0 : fields.size();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        //@Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            if (fields != null)
            {
                SearchFieldConfig sfc = fields.get(rowIndex);
                if (sfc != null)
                {
                    switch (columnIndex)
                    {
                        case 0 : return sfc.isInUse();
                        case 1 : return sfc.toString();
                        case 2 : 
                        {
                            if (!sfc.getIsSortable())
                            {
                                return NONE;
                            }
                            return sfc.getIsAscending() ? ASCENDING : DESCENDING;
                        }
                    }
                }
            }
            return ""; //$NON-NLS-1$
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            SearchFieldConfig sfc = fields.get(rowIndex);
            if (sfc != null)
            {
                switch (columnIndex)
                {
                    case 0 : return true;
                    case 1 : return false;
                    case 2 : return sfc.isInUse();
                }
            }
            return false;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
         */
        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex)
        {
            SearchFieldConfig sfc = fields.get(rowIndex);
            if (sfc != null)
            {
                switch (columnIndex)
                {
                    case 0 : 
                    {
                        int cnt = getCountSelectedSearchFields();
                        
                        sfc.setInUse((Boolean)value);
                        
                        if (cnt == 0 && ((Boolean)value))
                        {
                            enableSearchBtns(true, false);
                        }
                        
                        fireTableRowsUpdated(rowIndex, rowIndex);
                        
                        if (sfc.isInUse())
                        {
                            toBeSearchedVect.add(sfc);
                            Collections.sort(toBeSearchedVect, toBeSearchedComparator);
                            ((VecModel)toBeSearchedList.getModel()).fireChange(null);
                            autoSelectDisplayListField(sfc, true);
                            
                        } else
                        {
                            toBeSearchedVect.remove(sfc);
                            ((VecModel)toBeSearchedList.getModel()).fireChange(null);
                            autoSelectDisplayListField(sfc, false);
                        }
                        
                        if (cnt == 1 && !((Boolean)value))
                        {
                            enableSearchBtns(false, true);
                        }
                    }
                    break;
                        
                    case 1 : 
                        sfc.setFieldName((String)value);
                        break;
                        
                    case 2 : 
                        {
                            String valStr = (String)value;
                            if (valStr.equals(NONE))
                            {
                                sfc.setIsSortable(false);
                                
                            } else if (valStr.equals(ASCENDING))
                            {
                                sfc.setIsSortable(true);
                                sfc.setIsAscending(true);
                                
                            } else if (valStr.equals(DESCENDING))
                            {
                                sfc.setIsSortable(true);
                                sfc.setIsAscending(false);
                            }
                            fireTableRowsUpdated(rowIndex, rowIndex);
                        }
                        break;
                }
            }
        }
    }
    
    /**
     * Automatically selects/deselects the display field when the searchable field is choosen or unchoosen/
     * @param sfc the choosen search field
     * @param wasAdded whether it was added or removed.
     */
    protected void autoSelectDisplayListField(final SearchFieldConfig sfc, final boolean wasAdded)
    {
        //List<DisplayFieldConfig> displayItems = displayList.getItems();
        int index = 0;
        for (DisplayFieldConfig dfc : displayList.getItems())
        {
            if (dfc.getFieldInfo() == sfc.getFieldInfo())
            {
                JToggleButton tb = displayList.getButtons().get(index);
                tb.setSelected(wasAdded);
                if (wasAdded)
                {
                    tb.setEnabled(!wasAdded);
                } else
                {
                    tb.setEnabled(true);
                }
                dfc.setInUse(wasAdded);
               return; 
            }
            index++;
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        if (btnPressed == NONE_BTN)
        {
            pruneTree();
        }
    }

    //------------------------------------------------------------------------------
    //-- Renderer for the Combobox in the table
    //------------------------------------------------------------------------------
    public class MyComboBoxRenderer extends JComboBox implements TableCellRenderer 
    {
        public MyComboBoxRenderer(String[] items) 
        {
            super(items);
        }
    
        public Component getTableCellRendererComponent(JTable table, 
                                                       Object value,
                                                       boolean isSelected, 
                                                       boolean hasFocus, 
                                                       int row, 
                                                       int column) 
        {
            SearchFieldConfig sfc = ((SearchFieldsTableModel)table.getModel()).getFields().get(row);
            if (isSelected) 
            {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else 
            {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            
            setEnabled(sfc.isInUse());
    
            // Select the current value
            setSelectedItem(value);
            return this;
        }
    }
    
    public class MyComboBoxEditor extends DefaultCellEditor {
        public MyComboBoxEditor(String[] items) {
            super(new JComboBox(items));
        }
    }
}
