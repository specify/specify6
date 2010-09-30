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
package edu.ku.brc.specify.plugins.sgr;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.dbsupport.cleanuptools.DataObjTableModel;
import edu.ku.brc.specify.dbsupport.cleanuptools.DataObjTableModelRowInfo;
import edu.ku.brc.specify.dbsupport.cleanuptools.FindItemInfo;
import edu.ku.brc.ui.BiColorTableCellRenderer;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.VerticalSeparator;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 27, 2010
 *
 */
public abstract class BaseResultsDisplay extends JPanel
{
    protected static final Logger  log = Logger.getLogger(BaseResultsDisplay.class);
    
    protected ArrayList<Color[]> colorGrid  = null;
    protected Color             shadedColor = new Color(235, 235, 255);
    protected Connection        connection;
    
    protected DataObjTableModel model;
    protected DataObjTableModel newModel;
    
    protected FindItemInfo      itemInfo;
    
    protected JTable            topTable;
    protected JTable            botTable;
    
    protected JButton           upBtn;
    protected JButton           dwnBtn;
    protected JButton           mvRecBtn;
    
    protected boolean           hasData   = false;

    protected JButton           acceptBtn = null;
    
    
    /**
     * 
     */
    public BaseResultsDisplay(final Connection connection)
    {
        super();
        this.connection = connection;
    }
    
    /**
     * 
     */
    protected abstract void createAndFillModels();
    
    /**
     * @return
     */
    protected abstract Object getDataRow();
    
    /**
     * @return
     */
    protected abstract boolean hasTools();
    
    /**
     * @return
     */
    protected abstract JPanel getToolsPanel();
    
    /**
     * 
     */
    protected void rowSelected(final int row)
    {
        mvRecBtn.setEnabled(row > -1);
        dwnBtn.setEnabled(row > -1);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    public void createUI()
    {
        createAndFillModels();
        
        topTable = new JTable(model)
        {
            public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int colIndex)
            {
                Component c          = super.prepareRenderer(renderer, rowIndex, colIndex);
                boolean   isSelected = isCellSelected(rowIndex, colIndex);
                Color     bgColor    = rowIndex % 2 == 0 && !isSelected ? shadedColor : isSelected ? getSelectionBackground() : getBackground();
                
                Color[] colorRow = colorGrid != null ? colorGrid.get(rowIndex) : null;
                if (colorRow != null)
                {
                    Color fgColor = isSelected ? getSelectionForeground() : colorRow[colIndex] == null ? Color.BLACK : colorRow[colIndex];
                    c.setForeground(fgColor);
                    //System.out.println(rowIndex+"  "+colIndex +"  "+fgColor);
                }
                
                c.setBackground(bgColor);
                return c;
            }
        };
        
        botTable = new JTable(newModel);
        
        //UIHelper.calcColumnWidths(topTable, null, 200);
        
        DefaultTableCellRenderer tcr = getTableCellRenderer(model.getRowInfoList());
        for (int i=0;i<model.getColumnCount();i++)
        {
            if (model.getColumnClass(i) != Boolean.class)
            {
                topTable.setDefaultRenderer(model.getColumnClass(i), tcr);
            }
        }
        
        topTable.setCellSelectionEnabled(true);
        
        CellConstraints cc = new CellConstraints();
        
        dwnBtn = UIHelper.createButton("Move Field Value");
        dwnBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                moveValueDown();
            }
        });
        
        mvRecBtn = UIHelper.createButton("Choose Base Record");
        mvRecBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                moveRecord();
            }
        });
        
        ListSelectionListener lsl = new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                rowSelected(topTable.getSelectedRow());
            }
        };
        topTable.getSelectionModel().addListSelectionListener(lsl);
        botTable.getSelectionModel().addListSelectionListener(lsl);
        
       
        
        String colsDef = "f:p:g, p";
        if (hasTools())
        {
            colsDef += ", f:p:g, p, f:p:g, p";
        }
        colsDef += ", f:p:g";
        
        PanelBuilder outerMidPB = new PanelBuilder(new FormLayout(colsDef, "f:p:g"));
        
        PanelBuilder midPanel = new PanelBuilder(new FormLayout("f:p:g, p, 30px, p, f:p:g", "f:p:g, p, f:p:g"));
        midPanel.add(mvRecBtn, cc.xy(2, 2));
        midPanel.add(dwnBtn,   cc.xy(4, 2));
        
        outerMidPB.add(midPanel.getPanel(), cc.xy(2, 1));
        
        if (hasTools())
        {
            Color fgColor = new Color(224, 224, 224);
            Color bgColor = new Color(124, 124, 124);
            outerMidPB.add(new VerticalSeparator(fgColor, bgColor), cc.xy(4, 1));
            outerMidPB.add(getToolsPanel(),                         cc.xy(6, 1));
        }
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g", "f:MAX(400px;p):g,10px,p,10px,200px"), this);
        
        topTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        botTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        UIHelper.makeTableHeadersCentered(topTable, false);
        topTable.setDefaultRenderer(String.class, new BiColorTableCellRenderer(false));

        UIHelper.makeTableHeadersCentered(botTable, false);

        //final JScrollPane botScrollPane = UIHelper.createScrollPane(botTable, true);
        final JScrollPane topScrollPane = new JScrollPane(topTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        final JScrollPane botScrollPane = new JScrollPane(botTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        //UIHelper.calcColumnWidths(botTable, 3);
        pb.add(topScrollPane, cc.xy(1,1));
        pb.add(outerMidPB.getPanel(),                     cc.xy(1,3));
        pb.add(botScrollPane,                             cc.xy(1,5));
        pb.setDefaultDialogBorder();
        
        /*SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                int numRows = 4;
                JViewport viewPort = botScrollPane.getViewport();
                Dimension dim = viewPort.getSize();
                dim.height = botTable.getRowHeight() * numRows;
                viewPort.setViewSize(dim);
                botScrollPane.setRowHeader(viewPort);
            }
        });*/
    }

    /**
     * 
     */
    private void moveValueDown()
    {
        int   row      = topTable.getSelectedRow();
        int[] colsInxs = topTable.getSelectedColumns();
        
        if (colsInxs != null && colsInxs.length > 0)
        {
            for (int c : colsInxs)
            {
                Object data = topTable.getValueAt(row, c);
                newModel.setValueAt(data, 0, c);
            }
            hasData = true;
            acceptBtn.setEnabled(true);
        }
    }
    
    /**
     * 
     */
    private void moveRecord()
    {
        int row = topTable.getSelectedRow();
        if (row == -1 && topTable.getRowCount() == 1)
        {
            row = 0;
        }
        
        if (row != -1)
        {
            for (int i=0;i<topTable.getColumnCount();i++)
            {
                Object data = topTable.getValueAt(row, i);
                newModel.setValueAt(data, 0, i);
            }
            
            for (int i=0;i<model.getRowCount();i++)
            {
                model.getRowInfoList().get(i).setMainRecord(row == i);
            }
            hasData = true;
            acceptBtn.setEnabled(true);
        }
    }
    
    /**
     * @return the hasData
     */
    public boolean hasData()
    {
        return hasData;
    }

    /**
     * @param hasData the hasData to set
     */
    public void setHasData(boolean hasData)
    {
        this.hasData = hasData;
    }
    
    /*private void moveValueUp()
    {
        int row = topTable.getSelectedRow();
        int col = topTable.getSelectedColumn();
        
        if (row != -1 && col != -1)
        {
            newModel.setValueAt(null, row, col);
        }
    }*/
    
    /**
     * @return
     */
    protected DefaultTableCellRenderer getTableCellRenderer(final List<DataObjTableModelRowInfo> rowInfoList)
    {
        final Color sameColor = new Color(0,128,0);
        
        return new DefaultTableCellRenderer() {
            @SuppressWarnings("unchecked")
            @Override
            public Component getTableCellRendererComponent(JTable table,
                                                           Object value,
                                                           boolean isSelected,
                                                           boolean hasFocus,
                                                           int row,
                                                           int column)
            {
                DataObjTableModelRowInfo rowInfo = rowInfoList.get(row);
                
                boolean doCenter = false;
                Object  val      = value;
                if (value instanceof Pair<?, ?>)
                {
                    Pair<Object, Object> pair = (Pair<Object, Object>)value;
                    val      = pair.first;
                    doCenter = true;
                }
                
                JLabel lbl = (JLabel)super.getTableCellRendererComponent(table, val, isSelected, hasFocus, row, column);
                if (rowInfo.isMainRecord())
                {
                    lbl.setForeground(Color.WHITE);
                    lbl.setBackground(Color.BLUE.brighter().brighter());
                    
                } else
                {
                    lbl.setForeground(model.isSame(column) ? sameColor : Color.BLACK);
                    lbl.setBackground(Color.WHITE);
                }
                lbl.setHorizontalTextPosition(doCenter ? SwingConstants.CENTER : SwingConstants.LEFT);
                
                return lbl;
            }
        };
    }
    
    /**
     * Can be called right before the panel is displayed.
     */
    public void beforeDisplay()
    {
    }
    
    public void setAcceptBtn(final JButton btn)
    {
        acceptBtn = btn;
    }
    
    /**
     * 
     */
    public void shutdown()
    {
        
    }
}
