/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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
package edu.ku.brc.specify.tasks.subpane;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.RecordSetFactory;
import edu.ku.brc.af.core.ServiceInfo;
import edu.ku.brc.af.core.ServiceProviderIFace;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.specify.tasks.RecordSetTask;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.Uploader;
import edu.ku.brc.specify.ui.db.ResultSetTableModel;
import edu.ku.brc.ui.BiColorTableCellRenderer;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.DataFlavorTableExt;
import edu.ku.brc.ui.GradiantButton;
import edu.ku.brc.ui.GradiantLabel;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.TriangleButton;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.GhostActionable;

/**
 * This is a single set of of results and is derived from a query where all the record numbers where
 * supplied as an "in" clause.
 *
 * 
 * @code_status Beta
 * 
 * @author rods
 *
 */
public class ESResultsTablePanel extends JPanel implements ESResultsTablePanelIFace
{
	private static final Logger log = Logger.getLogger(ESResultsTablePanel.class);

    protected static final Cursor handCursor    = new Cursor(Cursor.HAND_CURSOR);
    protected static final Cursor defCursor     = new Cursor(Cursor.DEFAULT_CURSOR);

    protected ExpressSearchResultsPaneIFace esrPane;
    protected ResultSetTableModel     resultSetTableModel;
    protected JTable                   table;
    protected JPanel                   tablePane;
    protected TriangleButton           expandBtn;
    protected GradiantButton           showTopNumEntriesBtn;
    protected int                      rowCount       = 0;
    protected boolean                  showingAllRows = false;
    protected boolean                  hasResults     = false;
    protected boolean                  isEditable     = false;
    protected JPopupMenu               popupMenu      = null;
    protected JButton                  delRSItems     = null;

    
    protected Hashtable<ServiceInfo, JButton> serviceBtns = null;

    protected JPanel                   morePanel      = null;
    protected Color                    bannerColor    = new Color(30, 144, 255);    // XXX PREF
    protected int                      topNumEntries  = 7;
    protected QueryForIdResultsIFace   results;
    
    protected GradiantLabel            topTitleBar;
    protected JButton                  moreBtn;
    
    protected JButton                  selectAllBtn   = UIHelper.createMiniI18NBtn("SELECTALL");
    protected JButton                  deselectAllBtn = UIHelper.createMiniI18NBtn("DESELECTALL");
    protected JButton                  moveToRSBtn    = UIHelper.createButton(IconManager.getIcon("Unmap"));
    protected RolloverCommand          moveToRSCmd;
    protected JPanel                   botBtnPanel;
    
    protected PropertyChangeListener   propChangeListener = null;

    /**
     * Constructor of a results "table" which is really a panel
     * @param esrPane the parent
     * @param erTableInfo the info describing the results
     * @param installServices indicates whether services should be installed
     * @param isExpandedAtStartUp enough said
     */
    public ESResultsTablePanel(final ExpressSearchResultsPaneIFace esrPane,
                               final QueryForIdResultsIFace    results,
                               final boolean                   installServices,
                               final boolean                   isExpandedAtStartUp)
    {
        this(esrPane, results, installServices, isExpandedAtStartUp, true);
    }
     
    /**
     * Constructor of a results "table" which is really a panel
     * @param esrPane the parent
     * @param erTableInfo the info describing the results
     * @param installServices indicates whether services should be installed
     * @param isExpandedAtStartUp enough said
     * @param inclCloseBtn whether to include the close button on the bar
     */
    public ESResultsTablePanel(final ExpressSearchResultsPaneIFace esrPane,
                               final QueryForIdResultsIFace    results,
                               final boolean                   installServices,
                               final boolean                   isExpandedAtStartUp,
                               final boolean                   inclCloseBtn)
    {
        super(new BorderLayout());

        this.esrPane       = esrPane;
        this.results       = results;
        this.bannerColor   = results.getBannerColor();
        this.isEditable    = results.isEditingEnabled();
        
        table = new JTable();
        //BiColorTableCellRenderer cellRenderer = new BiColorTableCellRenderer();
        //table.setDefaultRenderer(String.class, cellRenderer);
        table.setShowVerticalLines(false);
        table.setRowSelectionAllowed(true);
        table.setSelectionMode(results.isMultipleSelection() ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);

        setBackground(table.getBackground());
        
        if (isEditable)
        {
            addContextMenu();
        }

        topTitleBar = new GradiantLabel(results.getTitle(), SwingConstants.LEFT);
        topTitleBar.setBGBaseColor(bannerColor);
        topTitleBar.setTextColor(Color.WHITE);
        topTitleBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                
                if (e.getClickCount() == 2)
                {
                    expandBtn.doClick();
                }
            }
        });
        
        String description = results.getDescription();
        if (StringUtils.isNotEmpty(description))
        {
            topTitleBar.setToolTipText(description);
        }

        expandBtn = new TriangleButton();
        expandBtn.setToolTipText(getResourceString("CollapseTBL"));
        expandBtn.setForeground(bannerColor);
        expandBtn.setTextColor(Color.WHITE);

        showTopNumEntriesBtn = new GradiantButton(String.format(getResourceString("ShowTopEntries"), new Object[] {topNumEntries}));
        showTopNumEntriesBtn.setForeground(bannerColor);
        showTopNumEntriesBtn.setTextColor(Color.WHITE);
        showTopNumEntriesBtn.setVisible(false);
        showTopNumEntriesBtn.setCursor(handCursor);

        List<ServiceInfo> services = installServices ? getServices() : null;

        //System.out.println("["+tableInfo.getTableId()+"]["+services.size()+"]");
        StringBuffer colDef  = new StringBuffer("p,0px,p:g,0px,p,0px,");
        int          numCols = (installServices ? services.size() : 0) + (inclCloseBtn ? 1 : 0);
        colDef.append(UIHelper.createDuplicateJGoodiesDef("p", "0px", numCols)); // add additional col defs for services
        
        PanelBuilder    builder    = new PanelBuilder(new FormLayout(colDef.toString(), "f:p:g"));
        CellConstraints cc         = new CellConstraints();

        int col = 1;
        builder.add(expandBtn, cc.xy(col,1));
        col += 2;

        builder.add(topTitleBar, cc.xy(col,1));
        col += 2;

        builder.add(showTopNumEntriesBtn, cc.xy(col,1));
        col += 2;
                
        if (installServices && services.size() > 0)
        {
            serviceBtns = new Hashtable<ServiceInfo, JButton>();
            
            //IconManager.IconSize size = IconManager.
            int iconSize = AppPreferences.getLocalPrefs().getInt("banner.icon.size", 20);
            // Install the buttons on the banner with available services
            for (ServiceInfo serviceInfo : services)
            {
                GradiantButton btn = new GradiantButton(serviceInfo.getIcon(iconSize)); // XXX PREF
                btn.setToolTipText(serviceInfo.getTooltip());
                btn.setForeground(bannerColor);
                builder.add(btn, cc.xy(col, 1));
                ESTableAction esta = new ESTableAction(serviceInfo.getCommandAction(), table, serviceInfo.getTooltip());
                esta.setProperty("gridtitle", results.getTitle());
                btn.addActionListener(esta);
                serviceBtns.put(serviceInfo, btn);
                col += 2;
            }
        }

        GradiantButton closeBtn = null;
        if (inclCloseBtn)
        {
            closeBtn = new GradiantButton(IconManager.getIcon("Close"));
            closeBtn.setToolTipText(getResourceString("ESCloseTable"));
            closeBtn.setForeground(bannerColor);
            closeBtn.setRolloverEnabled(true);
            closeBtn.setRolloverIcon(IconManager.getIcon("CloseHover"));
            closeBtn.setPressedIcon(IconManager.getIcon("CloseHover"));
            builder.add(closeBtn, cc.xy(col,1));
            col += 2;
        }

        add(builder.getPanel(), BorderLayout.NORTH);

        tablePane = new JPanel(new BorderLayout());
        tablePane.add(table.getTableHeader(), BorderLayout.PAGE_START);
        
        Component comp = AppPreferences.getLocalPrefs().getBoolean("ss.usescrollbars", false) ? UIHelper.createScrollPane(table) : table;
        tablePane.add(comp, BorderLayout.CENTER);
        //tablePane.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        if (isEditable)
        {
            //delRSItems = UIHelper.createI18NButton("RESTBL_DEL_ITEMS");
            delRSItems = UIHelper.createIconBtn("DelRec", "ESDelRowsTT", null);
            delRSItems.addActionListener(createRemoveItemAL());
            delRSItems.setEnabled(false);
            
            table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e)
                {
                    if (!e.getValueIsAdjusting())
                    {
                        delRSItems.setEnabled(table.getSelectedRowCount() > 0);
                    }
                }
            });
        }
        
        add(tablePane, BorderLayout.CENTER);
        
        moveToRSCmd = new DragSelectedRowsBtn(IconManager.getIcon("Record_Set", IconManager.IconSize.Std16));
        
        if (installServices)
        {
            PanelBuilder bottomBar = new PanelBuilder(new FormLayout("4px,p,4px,p,4px,p,"+(delRSItems != null ? "4px,p," : "")+"f:p:g", "p"));
            bottomBar.add(moveToRSCmd,    cc.xy(2,1));
            bottomBar.add(selectAllBtn,   cc.xy(4,1));
            bottomBar.add(deselectAllBtn, cc.xy(6,1));
            if (delRSItems != null)
            {
                bottomBar.add(delRSItems, cc.xy(8,1));
            }
            botBtnPanel = bottomBar.getPanel();
            
            deselectAllBtn.setEnabled(false);
            selectAllBtn.setEnabled(true);
            moveToRSCmd.setEnabled(true);
            
            deselectAllBtn.setToolTipText(getResourceString("SELALLTOOLTIP"));
            selectAllBtn.setToolTipText(getResourceString("DESELALLTOOLTIP"));
            moveToRSCmd.setToolTipText(getResourceString("MOVEROWSTOOLTIP"));
            
            selectAllBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    table.selectAll(); 
                }
            });
            
            deselectAllBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    table.clearSelection();
                }
            });
            
            moveToRSCmd.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    RecordSetIFace src = (RecordSetIFace)moveToRSCmd.getData();
                    CommandDispatcher.dispatch(new CommandAction(RecordSetTask.RECORD_SET, "AskForNewRS", src, null, null));
                }
            });
            
            add(botBtnPanel, BorderLayout.SOUTH);

        } else
        {
            botBtnPanel = null;
        }

        expandBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                expandTable(false);
            }
        });
        
        if (!isExpandedAtStartUp)
        {
            expandTable(true);
        }
        
        showTopNumEntriesBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                morePanel.setVisible(true);
                showTopNumEntriesBtn.setVisible(false);
                showingAllRows = false;
                setDisplayRows(rowCount, topNumEntries);

                // If it is collapsed then expand it
                if (!expandBtn.isDown())
                {
                    tablePane.setVisible(true);
                    expandBtn.setDown(true);
                }

                // Make sure the layout is updated
                invalidate();
                doLayout();
                esrPane.revalidateScroll();
            }
        });

        if (closeBtn != null)
        {
            closeBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            removeMe();
                        }
                      });
    
                }
            });
        }
        
        ResultSetTableModel rsm = createModel();
        rsm.setPropertyListener(this);
        resultSetTableModel = rsm;
        table.setRowSorter(new TableRowSorter<ResultSetTableModel>(resultSetTableModel));
        
        table.setRowSelectionAllowed(true);
        table.setModel(rsm);

        configColumns();
        
        rowCount = rsm.getRowCount();
        if (rowCount > topNumEntries + 2)
        {
            buildMorePanel();
            setDisplayRows(rowCount, topNumEntries);
        } else
        {
            setDisplayRows(rowCount, Integer.MAX_VALUE);
        }

        invalidate();
        doLayout();
        UIRegistry.forceTopFrameRepaint();
        
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() 
        {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    if (botBtnPanel != null)
                    {
                        deselectAllBtn.setEnabled(table.getSelectedRowCount() > 0);
                        selectAllBtn.setEnabled(table.getSelectedRowCount() != table.getRowCount());
                        moveToRSCmd.setEnabled(table.getSelectedRowCount() > 0);
                    }
                }
                if (propChangeListener != null)
                {
                    if (!e.getValueIsAdjusting())
                    {
                        propChangeListener.propertyChange(new PropertyChangeEvent(this, "selection", table.getSelectedRowCount(), 0));

                    } else
                    {
                        propChangeListener.propertyChange(new PropertyChangeEvent(this, "selection", table.getSelectedRowCount(), 0));
                    }
                }
            }});
        
        table.addMouseListener(new MouseAdapter() 
        {
            public void mouseClicked(MouseEvent e) 
            {
                //System.out.println(e.getButton());
                if (e.getClickCount() == 2 && e.getButton() == 1)
                {
                    if (propChangeListener != null) 
                    {
                        propChangeListener.propertyChange(new PropertyChangeEvent(this, "doubleClick", 2, 0));
                    }
                    
                    if (serviceBtns != null)
                    {
                        for (ServiceInfo si : serviceBtns.keySet())
                        {
                            if (si.isDefault())
                            {
                                JButton defBtn = serviceBtns.get(si);
                                if (defBtn != null)
                                {
                                    defBtn.doClick();
                                }
                            }
                        }
                    }
                }
            }
        });
        
        BiColorTableCellRenderer bi = new BiColorTableCellRenderer();
        table.setDefaultRenderer(String.class, bi);
        TableColumnModel tableColModel = table.getColumnModel();
        for (int i=0;i<tableColModel.getColumnCount();i++)
        {
            tableColModel.getColumn(i).setCellRenderer(bi);
        }
    }
    
    /**
     * Start the Acquisition of data.
     */
    public void startFilling()
    {
    	resultSetTableModel.startDataAcquisition();
    }
    
    /**
     * @return the list of Services
     */
    protected List<ServiceInfo> getServices()
    {
        List<ServiceInfo> services = new Vector<ServiceInfo>(ContextMgr.checkForServices(results.getTableId()));
        if (results instanceof ServiceProviderIFace)
        {
            List<ServiceInfo> additionalServices = ((ServiceProviderIFace)results).getServices(this);
            if (additionalServices != null)
            {
                services.addAll(additionalServices);
            }
        }
        if (Uploader.getCurrentUpload() != null)
        {
        	services = Uploader.getCurrentUpload().filterServices(services);
        }
        return services;
    }
    
    /**
     * @return ResultSetTableModel
     */
    protected ResultSetTableModel createModel()
    {
        return new ResultSetTableModel(this, results, false, false);
    }
    
    /**
     * @return creates an action listener for removing an item from the table.
     */
    protected ActionListener createRemoveItemAL()
    {
        return new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run()
                    {
                        removeRows(getSelectedRows());
                        if (popupMenu != null)
                        {
                            popupMenu.setVisible(false);
                        }
                    }
                });
            }
        };
    }
    
    /**
     * 
     */
    protected void addContextMenu()
    {
        MouseAdapter mouseAdapter = new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                super.mousePressed(e);
                
                if (e.isPopupTrigger())
                {
                    if (table.getSelectedColumnCount() > 0)
                    {
                        if (popupMenu != null && popupMenu.isVisible())
                        {
                            popupMenu.setVisible(false);
                        }
                        
                        if (popupMenu == null)
                        {
                            popupMenu = new JPopupMenu();
                            JMenuItem mi = popupMenu.add(new JMenuItem(UIRegistry.getResourceString("Remove")));
                            mi.addActionListener(createRemoveItemAL());
                        }
                        Point p = table.getLocationOnScreen();
                        popupMenu.setLocation(p.x + e.getX() + 1, p.y + e.getY() + 1);
                        popupMenu.setVisible(true); 
                    }
                }
            }
        };
        table.addMouseListener(mouseAdapter);
    }
    
    /**
     * @param rows
     */
    protected void removeRows(int[] rows)
    {
        ResultSetTableModel model = (ResultSetTableModel)table.getModel();
        Vector<Integer>     ids   = new Vector<Integer>();
        for (int i=0;i<rows.length;i++)
        {
            ids.add(model.getRowId(rows[i]-i));
            model.removeRow(rows[i]-i);
        }
        results.removeIds(ids);
        table.clearSelection();
    }
    
    /**
     * 
     */
    public void expandView()
    {
        if (!expandBtn.isDown())
        {
            expandTable(false);
        }
    }
    
    /**
     * @param isInitial
     */
    protected void expandTable(final boolean isInitial)
    {
        boolean isExpanded = !expandBtn.isDown();

        expandBtn.setDown(isExpanded);
        expandBtn.setToolTipText(isExpanded ? getResourceString("CollapseTBL") : getResourceString("ExpandTBL"));

        tablePane.setVisible(isExpanded);
        if (botBtnPanel != null)
        {
            botBtnPanel.setVisible(isExpanded);
        }

        if (!showingAllRows && morePanel != null)
        {
            morePanel.setVisible(isExpanded);
        }
        
        if (!isInitial)
        {
            invalidate();
            doLayout();
            esrPane.revalidateScroll();
        }
    } 
    
    /**
     * 
     */
    protected void setTitleBar()
    {
        topTitleBar.setText(rowCount > 0 ? String.format("%s - %d", results.getTitle(), rowCount) : results.getTitle());
        if (moreBtn != null)
        {
            moreBtn.setText(String.format(getResourceString("MoreEntries"), new Object[] {(rowCount - topNumEntries)}));
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#getResults()
     */
    public QueryForIdResultsIFace getResults()
    {
        return results;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#hasResults()
     */
    public boolean hasResults()
    {
        return esrPane.hasResults();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#cleanUp()
     */
    public void cleanUp()
    {
        ((ResultSetTableModel)table.getModel()).cleanUp();
        
        if (results != null)
        {
            results.cleanUp();
        }
        esrPane   = null;
        results   = null;
    }

    /**
     * Sets all the Columns to be center justified this COULD be set up in the table info.
     *
     */
    protected void configColumns()
    {
        UIHelper.makeTableHeadersCentered(table, true);
    }
    
    /**
     * Builds the "more" panel.
     *
     */
    protected void buildMorePanel()
    {
        FormLayout      formLayout = new FormLayout("15px,0px,p", "p");
        PanelBuilder    builder    = new PanelBuilder(formLayout);
        CellConstraints cc         = new CellConstraints();

        moreBtn = createButton(String.format(getResourceString("MoreEntries"), new Object[] {(rowCount - topNumEntries)}));//(rowCount - topNumEntries)+" more...");
        moreBtn.setCursor(handCursor);

        moreBtn.setBorderPainted(false);
        builder.add(createLabel(" "), cc.xy(1,1));
        builder.add(moreBtn, cc.xy(3,1));

        morePanel = builder.getPanel();
        Color bgColor = table.getBackground();
        bgColor = new Color(Math.max(bgColor.getRed()-10, 0), Math.max(bgColor.getGreen()-10, 0), Math.max(bgColor.getBlue()-10, 0));

        Color fgColor = new Color(Math.min(bannerColor.getRed()+10, 255), Math.min(bannerColor.getGreen()+10, 255), Math.min(bannerColor.getBlue()+10, 255));
        morePanel.setBackground(bgColor);
        moreBtn.setBackground(bgColor);
        moreBtn.setForeground(fgColor);
        add(builder.getPanel(), BorderLayout.SOUTH);

        moreBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                morePanel.setVisible(false);
                showTopNumEntriesBtn.setVisible(true);
                showingAllRows = true;
                setDisplayRows(rowCount, Integer.MAX_VALUE);
                esrPane.revalidateScroll();
            }
        });
        
        morePanel.setVisible(false);
    }

    /**
     * Asks parent to remove this table.
     */
    protected void removeMe()
    {
        esrPane.removeTable(this);
    }

    /**
     * Creates an array of indexes.
     * @param rows the number of rows to be displayed
     * @return an array of indexes
     */
    protected int[] createIndexesArray(final int rows)
    {
        int[] indexes = new int[rows];
        for (int i=0;i<rows;i++)
        {
            indexes[i] = i;
        }
        return indexes;
    }

    /**
     * Display the 'n' number of rows up to topNumEntries.
     *
     * @param numRows the desired number of rows
     * @param maxNum the maximum number of rows
     */
    protected void setDisplayRows(final int numRows, final int maxNum)
    {
        @SuppressWarnings("unused")
        int rows = Math.min(numRows, maxNum);
        //ResultSetTableModel rsm = (ResultSetTableModel)table.getModel();
        //rsm.initializeDisplayIndexes();
        //rsm.addDisplayIndexes(createIndexesArray(rows));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#getTable()
     */
    public JTable getTable()
    {
        return table;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#getRecordSet(boolean)
     */
    public RecordSetIFace getRecordSet(final boolean returnAll)
    {
        //log.debug("Indexes: "+table.getSelectedRows().length+" Index["+results.getTableId()+"]");
        //for (int v : table.getSelectedRows())
        //{
        //	log.debug("["+v+"]");
        //}

        boolean doReturnAll = returnAll;
        
        int[] rows = getSelectedRows();
        if (returnAll || rows.length == 0)
        {
            int numRows = table.getModel().getRowCount();
            rows = new int[numRows];
            for (int i=0;i<numRows;i++)
            {
                rows[i] = i;
            }
        }
        RecordSetIFace rs = getRecordSet(rows, doReturnAll);

        // Now we use the actual Table Id from the table info
        // to set it correctly in the RecordSet
        rs.setDbTableId(results.getTableId());
        
        return rs;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#getListOfIds(boolean)
     */
    public List<Integer> getListOfIds(final boolean returnAll)
    {
        List<Integer> list = new ArrayList<Integer>();
        RecordSetIFace rs = getRecordSet(returnAll);
        if (rs != null)
        {
            for (RecordSetItemIFace rsi : rs.getOrderedItems())
            {
                list.add(rsi.getRecordId());
            }
        }
        return list;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#getRecordSet(int[], boolean)
     */
    public RecordSetIFace getRecordSet(final int[] rows, final boolean returnAll)
    {
        ResultSetTableModel rsm = (ResultSetTableModel)table.getModel();

        return rsm.getRecordSet(rows, returnAll);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#getSelectedRows()
     */
    public int[] getSelectedRows()
    {
        int[] rows = table.getSelectedRows();
        for (int i = 0; i < rows.length; i++) 
        {
            rows[i] = table.convertRowIndexToModel(rows[i]);
        }
        return rows;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#getTitle()
     */
    public String getTitle()
    {
        return results.getTitle();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#getUIComponent()
     */
    public Component getUIComponent()
    {
        return this;
    }

    
    //--------------------------------------------------------------
    // Inner Classes
    //--------------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#setPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void setPropertyChangeListener(PropertyChangeListener pcl)
    {
        propChangeListener = pcl;
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(final PropertyChangeEvent evt)
    {
        // This gets called when all the results have been loaded
        rowCount = (Integer)evt.getNewValue();
        
        if (rowCount > 0)
        {
            synchronized (getTreeLock()) 
            {
                setTitleBar();
                esrPane.addTable(ESResultsTablePanel.this);
            }
            
            if (propChangeListener != null) 
            {
                propChangeListener.propertyChange(new PropertyChangeEvent(this, "loaded", rowCount, rowCount));
            }                
            
            BiColorTableCellRenderer bi = new BiColorTableCellRenderer();
            table.setDefaultRenderer(String.class, bi);
            TableColumnModel tableColModel = table.getColumnModel();
            for (int i=0;i<tableColModel.getColumnCount();i++)
            {
                tableColModel.getColumn(i).setCellRenderer(bi);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#initialize(edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace, edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace)
     */
    public void initialize(ExpressSearchResultsPaneIFace esrPaneArg, QueryForIdResultsIFace resultsArg)
    {
        // NO OP
    }

    /**
     * 
     */
    class ESTableAction implements ActionListener
    {
        protected CommandAction   cmd;
        protected RecordSetIFace  recordSet;
        protected JTable          estTable;
        protected Properties      props = new Properties();
        protected String          msg;
        
        public ESTableAction(final CommandAction cmd,
                             final JTable        estTable,
                             final String        msg)
        {
            this.cmd          = cmd;
            this.estTable     = estTable;
            this.msg          = msg;
            this.props.put("jtable", estTable);
        }

        public void setProperty(final String key, final Object value)
        {
            props.put(key, value);
        }
        
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            UIRegistry.getStatusBar().setText(msg);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    boolean setCmdData = cmd.getData() == null;
                    if (setCmdData)
                    {
                        cmd.setData(getRecordSet(false));
                    }
                    cmd.addProperties(props);
                    CommandDispatcher.dispatch(cmd);

                    // always reset the consumed flag and set the data to null
                    // so the command can be used again
                    cmd.setConsumed(false);
                    if (setCmdData)
                    {
                        cmd.setData(null);
                    }
                }
            });
        }
    }

    
    //-------------------------------------------------------------
    // New RolloverCommand to enable DnD of rows using Ghosting.
    //-------------------------------------------------------------
    
    class DragSelectedRowsBtn extends RolloverCommand implements GhostActionable
    {
        /**
         * 
         */
        public DragSelectedRowsBtn(final ImageIcon imgIcon)
        {
            super(null, imgIcon);
            
            dragFlavors.add(new DataFlavorTableExt(RecordSetTask.class, "Record_Set", results.getTableId()));
            createMouseInputAdapter();
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.dnd.GhostActionable#getData()
         */
        public Object getData()
        {
            if (results.getRecIds() != null && results.getRecIds().size() > 0)
            {
                DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(results.getTableId());
                if (tableInfo != null)
                {
                    RecordSetIFace rs = RecordSetFactory.getInstance().createRecordSet();
                    rs.setDbTableId(results.getTableId());
                    
                    if (table.getSelectedRowCount() > 0)
                    {
                        for (int inx : getSelectedRows())
                        {
                            int id = results.getRecIds().get(inx);
                            rs.addItem(id);
                        }
                    } else
                    {
                        for (int i=0;i<results.size();i++)
                        {
                            rs.addItem(results.getRecIds().get(i));
                        }
                    }
                    return rs;
                }
            } else
            {
                log.error("results doesn't have any ids.");
            }
            return null;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.dnd.GhostActionable#getDataForClass(java.lang.Class)
         */
        public Object getDataForClass(Class<?> classObj)
        {
            DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(results.getTableId());
            if (tableInfo != null)
            {
                return tableInfo.getClassObj();
            }
            return null;
        }
    }
    
}
