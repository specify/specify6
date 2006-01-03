/* Filename:    $RCSfile: ExpressTableResultsBase.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.ku.brc.specify.tasks.subpane;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.util.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.core.*;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.tasks.ExpressResultsTableInfo;
import edu.ku.brc.specify.helpers.*;
import edu.ku.brc.specify.ui.*;
import edu.ku.brc.specify.ui.db.*;

/**
 * This is a single set of of results and is derived from a query where all the record numbers where 
 * supplied as an "in" clause.
 * 
 * @author rods
 *
 */
abstract class ExpressTableResultsBase extends JPanel
{
    protected static final Cursor handCursor    = new Cursor(Cursor.HAND_CURSOR);
    protected static final Cursor defCursor     = new Cursor(Cursor.DEFAULT_CURSOR);

    protected ExpressSearchResultsPane esrPane;
    protected JTable                   table;
    protected JPanel                   tablePane;
    protected TriangleButton           expandBtn;
    protected GradiantButton           showTopNumEntriesBtn;
    protected int                      rowCount       = 0;
    protected boolean                  showingAllRows = false;
   
    protected JPanel                   morePanel      = null;       
    protected Color                    bannerColor    = new Color(30, 144, 255);    // XXX PREF
    protected int                      topNumEntries  = 7;
    protected String[]                 colNames;
    protected ExpressResultsTableInfo  tableInfo;
    
    /**
     * Constructor of a results "table" which is really a panel
     * @param esrPane the parent 
     * @param esrPane the parent 
     * @param tableInfo the info describing the results
     * @param bannerColor the color of the banner (or bar)
     */
    public ExpressTableResultsBase(final ExpressSearchResultsPane esrPane, 
                                   final ExpressResultsTableInfo tableInfo)
    {
        super(new BorderLayout());
        
        this.esrPane     = esrPane;
        this.tableInfo   = tableInfo;
        this.bannerColor = tableInfo.getColor();
        
        table = new JTable();
        table.setShowVerticalLines(false);
        table.setRowSelectionAllowed(true);
        setBackground(table.getBackground());
        
        GradiantLabel vl = new GradiantLabel(tableInfo.getTitle(), JLabel.LEFT);
        vl.setForeground(bannerColor);
        vl.setTextColor(Color.WHITE);
        
        expandBtn = new TriangleButton();
        expandBtn.setToolTipText(getResourceString("CollapseTBL"));
        expandBtn.setForeground(bannerColor);
        expandBtn.setTextColor(Color.WHITE);
  
        showTopNumEntriesBtn = new GradiantButton(String.format(getResourceString("ShowTopEntries"), new Object[] {topNumEntries}));
        showTopNumEntriesBtn.setForeground(bannerColor);
        showTopNumEntriesBtn.setTextColor(Color.WHITE);
        showTopNumEntriesBtn.setVisible(false);
        showTopNumEntriesBtn.setCursor(handCursor);
        
        boolean newWay = true;
        if (newWay)
        {
            List<ServiceInfo> services = ContextMgr.checkForServices(Integer.parseInt(tableInfo.getTableId()));
            
            StringBuffer colDef = new StringBuffer("p,0px,p:g,0px,p,0px,p,0px,");
            colDef.append(UIHelper.createDuplicateJGoodiesDef("p", "0px", services.size())); // add additional col defs for services
            
            FormLayout      formLayout = new FormLayout(colDef.toString(), "center:p");
            PanelBuilder    builder    = new PanelBuilder(formLayout);
            CellConstraints cc         = new CellConstraints();
            
            int col = 1;
            builder.add(expandBtn, cc.xy(col,1));
            col += 2;
            
            builder.add(vl, cc.xy(col,1));
            col += 2;
            
            builder.add(showTopNumEntriesBtn, cc.xy(col,1));
            col += 2;
            
            // install the btns on the banner with available services
            for (ServiceInfo serviceInfo : services)
            {
                GradiantButton btn = new GradiantButton(serviceInfo.getIcon(IconManager.IconSize.Std16));
                btn.setToolTipText(serviceInfo.getTooltip());
                btn.setForeground(bannerColor);
                builder.add(btn, cc.xy(col,1));
                
                btn.addActionListener(new ESTableAction(serviceInfo.getCommandAction(), table, tableInfo));

                col += 2;

            }
            
            CloseButton closeBtn = new CloseButton();
            closeBtn.setToolTipText(getResourceString("ESCloseTable"));
            closeBtn.setForeground(bannerColor);
            closeBtn.setCloseColor(new Color(255,255,255, 90));
            builder.add(closeBtn, cc.xy(col,1));
            col += 2;
            
            add(builder.getPanel(), BorderLayout.NORTH);
            
            tablePane = new JPanel(new BorderLayout());
            tablePane.setLayout(new BorderLayout());
            tablePane.add(table.getTableHeader(), BorderLayout.PAGE_START);
            tablePane.add(table, BorderLayout.CENTER);
    
            add(tablePane, BorderLayout.CENTER);
            
            expandBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) 
                {
                    boolean isExpanded = !expandBtn.isDown();
                    
                    expandBtn.setDown(isExpanded);
                    expandBtn.setToolTipText(isExpanded ? getResourceString("CollapseTBL") : getResourceString("ExpandTBL"));
    
                    tablePane.setVisible(isExpanded);               
                    
                    if (!showingAllRows && morePanel != null)
                    {
                        morePanel.setVisible(isExpanded);
                    }
                    invalidate();
                    doLayout();
                    esrPane.revalidateScroll();
                }
            });
            
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
        
        
        } else
        {
            StringBuffer colDef = new StringBuffer("p,0px,p:g,0px,p,0px,p,0px,p");
            
            boolean isCollectionTable = tableInfo.getTableId().equals("1");
            if (isCollectionTable)
            {
                colDef.append(",0px,p"); // Labels, Save To RecordSet, Data Entry
            }
            colDef.append(",0px,p"); // close Button
            
            FormLayout      formLayout = new FormLayout(colDef.toString(), "center:p");
            PanelBuilder    builder    = new PanelBuilder(formLayout);
            CellConstraints cc         = new CellConstraints();
    
            int col = 1;
            builder.add(expandBtn, cc.xy(col,1));
            col += 2;
            
            builder.add(vl, cc.xy(col,1));
            col += 2;
            
            builder.add(showTopNumEntriesBtn, cc.xy(col,1));
            col += 2;
            
            GradiantButton labelsBtn = null;
            GradiantButton rsBtn     = null;
            GradiantButton deBtn     = null;
            
            if (isCollectionTable)
            {
                labelsBtn = new GradiantButton(IconManager.getImage("Labels", IconManager.IconSize.Std16));
                labelsBtn.setToolTipText(getResourceString("CreateLabelTT"));
                labelsBtn.setForeground(bannerColor);
                builder.add(labelsBtn, cc.xy(col,1));
                col += 2;
            }
            
            rsBtn = new GradiantButton(IconManager.getImage("Record_Set", IconManager.IconSize.Std16));
            rsBtn.setToolTipText(getResourceString("CreateRecordSetTT"));
            rsBtn.setForeground(bannerColor);
            builder.add(rsBtn, cc.xy(col,1));
            col += 2;
            
            deBtn = new GradiantButton(IconManager.getImage("Data_Entry", IconManager.IconSize.Std16));
            deBtn.setToolTipText(getResourceString("EditRecordSetTT"));
            deBtn.setForeground(bannerColor);
            builder.add(deBtn, cc.xy(col,1));
            col += 2;
            
            CloseButton closeBtn = new CloseButton();
            closeBtn.setToolTipText(getResourceString("ESCloseTable"));
            closeBtn.setForeground(bannerColor);
            closeBtn.setCloseColor(new Color(255,255,255, 90));
            builder.add(closeBtn, cc.xy(col,1));
            col += 2;
            
            add(builder.getPanel(), BorderLayout.NORTH);
            
            tablePane = new JPanel(new BorderLayout());
            tablePane.setLayout(new BorderLayout());
            tablePane.add(table.getTableHeader(), BorderLayout.PAGE_START);
            tablePane.add(table, BorderLayout.CENTER);
    
            add(tablePane, BorderLayout.CENTER);
            
            expandBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) 
                {
                    boolean isExpanded = !expandBtn.isDown();
                    
                    expandBtn.setDown(isExpanded);
                    expandBtn.setToolTipText(isExpanded ? getResourceString("CollapseTBL") : getResourceString("ExpandTBL"));
    
                    tablePane.setVisible(isExpanded);               
                    
                    if (!showingAllRows && morePanel != null)
                    {
                        morePanel.setVisible(isExpanded);
                    }
                    invalidate();
                    doLayout();
                    esrPane.revalidateScroll();
                }
            });
            
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
            
            /*
            if (isCollectionTable)
            {
                labelsBtn.addActionListener(new ESTableAction("Labels", "DoLabels", table, tableInfo));
            }
            rsBtn.addActionListener(new ESTableAction("Record_Set", "Save", table, tableInfo));
            deBtn.addActionListener(new ESTableAction("Data_Entry", "Edit", table, tableInfo));
            */
        }
        
        
    }
    
    /**
     * 
     *
     */
    protected void configColumnNames()
    {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
        
        int[] colMappings = null;//tableInfo.getDisplayColIndexes();
        colNames = tableInfo.getColNames();

        TableColumnModel tableColModel = table.getColumnModel();
        for (int i=0;i<tableColModel.getColumnCount();i++) 
        {
            tableColModel.getColumn(i).setCellRenderer(renderer);
            if (colNames != null)
            {
                String label = (String)tableColModel.getColumn(i).getHeaderValue();
                if (label != null )
                {
                    tableColModel.getColumn(i).setHeaderValue(colMappings != null ? colNames[colMappings[i]] : colNames[i]);
                }
            }
        }
    }
    
    /**
     * 
     *
     */
    protected void buildMorePanel()
    {
        FormLayout      formLayout = new FormLayout("15px,0px,p", "p");
        PanelBuilder    builder    = new PanelBuilder(formLayout);
        CellConstraints cc         = new CellConstraints();
        
        JButton btn = new JButton(String.format(getResourceString("MoreEntries"), new Object[] {(rowCount - topNumEntries)}));//(rowCount - topNumEntries)+" more...");
        btn.setCursor(handCursor);

        btn.setBorderPainted(false);
        builder.add(new JLabel(" "), cc.xy(1,1));
        builder.add(btn, cc.xy(3,1));
        
        morePanel = builder.getPanel();
        Color bgColor = table.getBackground();
        bgColor = new Color(Math.max(bgColor.getRed()-10, 0), Math.max(bgColor.getGreen()-10, 0), Math.max(bgColor.getBlue()-10, 0));
        
        Color fgColor = new Color(Math.min(bannerColor.getRed()+10, 255), Math.min(bannerColor.getGreen()+10, 255), Math.min(bannerColor.getBlue()+10, 255));
        morePanel.setBackground(bgColor);
        btn.setBackground(bgColor);
        btn.setForeground(fgColor);
        add(builder.getPanel(), BorderLayout.SOUTH);
        
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) 
            {
                morePanel.setVisible(false);
                showTopNumEntriesBtn.setVisible(true);
                showingAllRows = true;
                setDisplayRows(rowCount, Integer.MAX_VALUE);
                esrPane.revalidateScroll();
            }
        });
        
    }
    
    /**
     * Aks parent to remove this table
     */
    protected void removeMe()
    {
        esrPane.removeTable(this);
    }
    
    /**
     * Creates an array of indexes
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
     * Display the 'n' number of rows up to topNumEntries
     * 
     * @param numRows the desired number of rows
     */
    protected void setDisplayRows(final int numRows, final int maxNum)
    {
        int rows = Math.min(numRows, maxNum);
        ResultSetTableModelDM rsm = (ResultSetTableModelDM)table.getModel();
        rsm.initializeDisplayIndexes();
        rsm.addDisplayIndexes(createIndexesArray(rows));
    }
    
    /**
     * Returns a RecordSet object from the table
     * @param true - allRecords all the records regardless of selection, false - only the selected records
     * @return Returns a RecordSet object from the table
     */
    public abstract RecordSet getRecordSet(final int[] rows, final int column);

    //--------------------------------------------------------------
    // Inner Classes
    //--------------------------------------------------------------
 
     /**
     * 
     * @author rods
     *
     */
    class ESTableAction implements ActionListener 
    {
        protected CommandAction cmd;
        protected RecordSet     recordSet;
        protected JTable        table;
        protected ExpressResultsTableInfo tableInfo;
        
        public ESTableAction(final CommandAction cmd, 
                             final JTable table,
                             final ExpressResultsTableInfo tableInfo)
        {
            this.cmd       = cmd;
            this.table     = table;
            this.tableInfo = tableInfo;
        }
        
        public void actionPerformed(ActionEvent e) 
        {
            RecordSet rs = getRecordSet(table.getSelectedRows(), tableInfo.getRecordSetColumnInx());
            rs.setTableId(Integer.parseInt(tableInfo.getTableId()));
            cmd.setData(rs);
            CommandDispatcher.dispatch(cmd);
            
            // always reset the consumed flag and set the data to null
            // so the command can be used again
            cmd.setConsumed(false);
            cmd.setData(null);
        }
    }

}
