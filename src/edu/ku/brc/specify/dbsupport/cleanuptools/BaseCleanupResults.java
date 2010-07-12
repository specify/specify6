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
package edu.ku.brc.specify.dbsupport.cleanuptools;

import static edu.ku.brc.ui.UIRegistry.getTopWindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * May 4, 2010
 *
 */
public abstract class BaseCleanupResults extends CustomDialog
{
    protected static final Logger  log = Logger.getLogger(BaseCleanupResults.class);
    
    protected DataObjTableModel model;
    protected DataObjTableModel newModel;
    
    protected BaseFindCleanupItems.ItemInfo itemInfo;
    
    protected JTable  topTable;
    protected JTable  botTable;
    
    protected JButton upBtn;
    protected JButton dwnBtn;
    protected JButton mvRecBtn;
    
    
    /**
     * 
     */
    public BaseCleanupResults(final String title, final BaseFindCleanupItems.ItemInfo itemInfo) throws HeadlessException
    {
        super((Frame) getTopWindow(), title, true, null);
        
        this.itemInfo = itemInfo;
    }

    /**
     * 
     */
    protected abstract void createAndFillModels();

    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        setOkLabel(UIRegistry.getResourceString("CLNUP.DOCLEANUP"));
        
        super.createUI();
        
        createAndFillModels();
        
        topTable = new JTable(model);
        botTable = new JTable(newModel);
        
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
        
        /*upBtn = UIHelper.createIconBtn("UpArrow", "", new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                moveValueUp();
            }
        });*/
        
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
                mvRecBtn.setEnabled(topTable.getSelectedRow() > -1);
                dwnBtn.setEnabled(topTable.getSelectedRow() > -1);
                //upBtn.setEnabled(botTable.getSelectedRow() > -1);
            }
        };
        topTable.getSelectionModel().addListSelectionListener(lsl);
        botTable.getSelectionModel().addListSelectionListener(lsl);
        
        PanelBuilder arrowPanel = new PanelBuilder(new FormLayout("f:p:g, p, 30px, p, f:p:g", "p"));
        arrowPanel.add(mvRecBtn, cc.xy(2, 1));
        //arrowPanel.add(upBtn,    cc.xy(4, 1));
        arrowPanel.add(dwnBtn,   cc.xy(4, 1));
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g,10px,p,10px,p:g"));
        
        UIHelper.calcColumnWidths(topTable);
        UIHelper.calcColumnWidths(botTable, 3);
        pb.add(UIHelper.createScrollPane(topTable, true), cc.xy(1,1));
        pb.add(arrowPanel.getPanel(),                     cc.xy(1,3));
        pb.add(UIHelper.createScrollPane(botTable, true), cc.xy(1,5));
        pb.setDefaultDialogBorder();
        
        contentPanel =  pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        pack();
        
        getOkBtn().addActionListener(new ActionListener() 
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        doFixDuplicates();
                    }
                });
            }
        });
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
        }
    }
    
    /**
     * 
     */
    private void moveRecord()
    {
        int row = topTable.getSelectedRow();
        
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
        }
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
    protected DefaultTableCellRenderer getTableCellRenderer(final List<DataObjTableModel.RowInfo> rowInfoList)
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
                DataObjTableModel.RowInfo rowInfo = rowInfoList.get(row);
                
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
    
    @SuppressWarnings("unused")
    protected void doFixDuplicates()
    {
        
    }
    
}
