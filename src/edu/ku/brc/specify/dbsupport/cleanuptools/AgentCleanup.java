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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * May 4, 2010
 *
 */
public class AgentCleanup
{
    protected static final Logger  log = Logger.getLogger(AgentCleanup.class);
    
    protected DataObjTableModel model;
    protected DataObjTableModel newModel;
    
    protected JTable topTable;
    protected JTable botTable;
    
    protected JButton upBtn;
    protected JButton dwnBtn;
    
    
    /**
     * 
     */
    public AgentCleanup()
    {
        super();
    }

    
    /**
     * 
     */
    public void loadData()
    {

        model    = new DataObjTableModel(Agent.getClassTableId(), "LastName", "Jahr%", true);
        newModel = new DataObjTableModel(Agent.getClassTableId(), model.getItems(), model.getHasDataList(), model.getSameValues(), model.getMapInx(), model.getIndexHash());
        
        topTable = new JTable(model);
        botTable = new JTable(newModel);
        
        DefaultTableCellRenderer tcr = getTableCellRenderer();
        for (int i=0;i<model.getColumnCount();i++)
        {
            topTable.setDefaultRenderer(model.getColumnClass(i), tcr);
        }
        
        topTable.setCellSelectionEnabled(true);
        
        CellConstraints cc = new CellConstraints();
        
        dwnBtn = UIHelper.createIconBtn("DownArrow", "", new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                moveValueDown();
            }
            
        });
        
        upBtn = UIHelper.createIconBtn("UpArrow", "", new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                moveValueUp();
            }
            
        });
        
        ListSelectionListener lsl = new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                dwnBtn.setEnabled(topTable.getSelectedRow() > -1);
                upBtn.setEnabled(botTable.getSelectedRow() > -1);
            }
        };
        topTable.getSelectionModel().addListSelectionListener(lsl);
        botTable.getSelectionModel().addListSelectionListener(lsl);
        
        PanelBuilder arrowPanel = new PanelBuilder(new FormLayout("f:p:g, p, 2px, p, f:p:g", "p"));
        arrowPanel.add(upBtn, cc.xy(2, 1));
        arrowPanel.add(dwnBtn, cc.xy(4, 1));
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g,10px,p,10px,f:p:g"));
        
        pb.add(UIHelper.createScrollPane(topTable), cc.xy(1,1));
        pb.add(arrowPanel.getPanel(),               cc.xy(1,3));
        pb.add(UIHelper.createScrollPane(botTable), cc.xy(1,5));
        pb.setDefaultDialogBorder();
        
        CustomDialog dlg = new CustomDialog(null, "Agent Duplicates", true, pb.getPanel());
        dlg.setVisible(true);
        if (!dlg.isCancelled())
        {
            doFixDuplicates();
        }
    }
    
    private void moveValueDown()
    {
        int row = topTable.getSelectedRow();
        int col = topTable.getSelectedColumn();
        
        if (row != -1 && col != -1)
        {
            Object data = topTable.getValueAt(row, col);
            newModel.setValueAt(data, 0, col);
        }
    }
    
    private void moveValueUp()
    {
        int row = topTable.getSelectedRow();
        int col = topTable.getSelectedColumn();
        
        if (row != -1 && col != -1)
        {
            newModel.setValueAt(null, row, col);
        }
    }
    
    protected DefaultTableCellRenderer getTableCellRenderer()
    {
        final Color sameColor = new Color(0,192,0);
        
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table,
                                                           Object value,
                                                           boolean isSelected,
                                                           boolean hasFocus,
                                                           int row,
                                                           int column)
            {
                JLabel lbl = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setForeground(model.isSame(column) ? sameColor : Color.BLACK);
                return lbl;
            }
        };
    }
    
    private void doFixDuplicates()
    {
        
    }
    
}
