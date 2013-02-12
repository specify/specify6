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
package edu.ku.brc.specify.tasks.subpane.collab;

import java.awt.Component;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import sun.swing.table.DefaultTableCellHeaderRenderer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Nov 29, 2011
 *
 */
public class ReviewLogPane extends BaseSubPane
{
    /**
     * @param name
     * @param task
     * @param buildProgressUI
     * @param includeProgressCancelBtn
     */
    public ReviewLogPane(String name, Taskable task, boolean buildProgressUI,
            boolean includeProgressCancelBtn)
    {
        super(name, task, buildProgressUI, includeProgressCancelBtn);
        createUI();
    }

    /**
     * @param name
     * @param task
     * @param buildProgressUI
     */
    public ReviewLogPane(String name, Taskable task, boolean buildProgressUI)
    {
        super(name, task, buildProgressUI);
        createUI();
    }

    /**
     * @param name
     * @param task
     */
    public ReviewLogPane(String name, Taskable task)
    {
        super(name, task);
        createUI();
    }
    
    private void createUI()
    {
        ReviewLogTblModel model = new ReviewLogTblModel();
        
        JTable table = new JTable(model);
        TableColumn tcol = table.getColumnModel().getColumn(0);
        tcol.setCellRenderer(new DescTableCellRenderer(model.getData()));
        
        table.setRowHeight(24);
        //UIHelper.calcColumnWidths(table, 15);
        
        tcol = table.getColumnModel().getColumn(3);
        tcol.setCellRenderer(new TableCellCenterRenderer());
        
        tcol = table.getColumnModel().getColumn(4);
        tcol.setCellRenderer(new TableCellCenterRenderer());
        
        table.getTableHeader().setDefaultRenderer(new TableCellHeaderRenderer());
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder pb1 = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"), this);
        pb1.add(UIHelper.createScrollPane(table, true), cc.xy(1,1));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#shutdown()
     */
    @Override
    public void shutdown()
    {
        super.shutdown();
    }

    public class DescTableCellRenderer extends DefaultTableCellRenderer
    {
        private final Vector<NotiLogInfo> list;
        
        public DescTableCellRenderer(final Vector<NotiLogInfo> list)
        {
            this.list = list;
        }
        
        public Component getTableCellRendererComponent(JTable table,
                                                       Object obj,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column)
        {
            JLabel cell = (JLabel)super.getTableCellRendererComponent(table, obj, isSelected, hasFocus, row, column);
            cell.setIcon(null);
            cell.setText("Bad");
            if (obj != null)
            {
                NotiLogInfo ni = list.get(row);
                cell.setIcon(ni.getIcon());
                cell.setText(ni.getDesc());
            }
            return cell;
        }
    }
    
    public class TableCellCenterRenderer extends DefaultTableCellRenderer
    {
        public TableCellCenterRenderer()
        {
            super();
            setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

    public class TableCellHeaderRenderer extends DefaultTableCellHeaderRenderer
    {
        public TableCellHeaderRenderer()
        {
            super();
            setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

}
