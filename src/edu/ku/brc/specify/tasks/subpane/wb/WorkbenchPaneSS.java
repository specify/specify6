/* This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.tasks.subpane.wb;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.util.Collections;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.ui.tmanfe.SpreadSheet;

public class WorkbenchPaneSS extends BaseSubPane
{
    private static final Logger log = Logger.getLogger(WorkbenchPaneSS.class);

    protected JTable     table;
    protected Workbench  workbench;
    protected String[]   columns;
    protected Vector<WorkbenchTemplateMappingItem> headers = new Vector<WorkbenchTemplateMappingItem>();

    public WorkbenchPaneSS(final String name,
                           final Taskable task,
                           final Workbench workbench)
    {
        super(name, task);
        
        this.workbench = workbench;
        
        removeAll();
        
        headers.addAll(workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems());
        Collections.sort(headers);
        
        FormLayout      formLayout = new FormLayout("f:max(100px;p):g", "top:p:g, p, center:p:g");
        PanelBuilder    builder    = new PanelBuilder(formLayout, this);
        CellConstraints cc         = new CellConstraints();

        SpreadSheet spreadsheet = new SpreadSheet(new GridTableModel());
        //spreadsheet.setBackground(Color.WHITE);
        initColumnSizes(spreadsheet);
        
        builder.add(spreadsheet.getScrollPane(), cc.xy(1,1));

    }
    
    /**
     * Adjust all the column width for the data in the column, this may be handles with JDK 1.6 (6.)
     * @param tableArg the table that should have it's columns adjusted
     */
    private void initColumnSizes(final JTable tableArg) 
    {
        TableModel  tblModel    = tableArg.getModel();
        TableColumn column      = null;
        Component   comp        = null;
        int         headerWidth = 0;
        int         cellWidth   = 0;
        
        TableCellRenderer headerRenderer = tableArg.getTableHeader().getDefaultRenderer();

        for (int i = 0; i < tblModel.getColumnCount(); i++) 
        {
            column = tableArg.getColumnModel().getColumn(i);

            comp = headerRenderer.getTableCellRendererComponent(
                                 null, column.getHeaderValue(),
                                 false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;

            comp = tableArg.getDefaultRenderer(tblModel.getColumnClass(i)).
                                               getTableCellRendererComponent(tableArg, tblModel.getValueAt(0, i), false, false, 0, i);
            
            cellWidth = comp.getPreferredSize().width;
            
            comp.setBackground(Color.WHITE);
            
            int maxWidth = headerWidth + 10;
            TableModel m = tableArg.getModel();
            FontMetrics fm     = new JLabel().getFontMetrics(getFont());
            for (int row=0;row<tableArg.getModel().getRowCount();row++)
            {
                String text = m.getValueAt(row, i).toString();
                maxWidth = Math.max(maxWidth, fm.stringWidth(text)+10);
                //System.out.println(i+" "+maxWidth);
            }

            /*
            if (DEBUG) {
                System.out.println("Initializing width of column "
                                   + i + ". "
                                   + "headerWidth = " + headerWidth
                                   + "; cellWidth = " + cellWidth);
            }*/

            //XXX: Before Swing 1.1 Beta 2, use setMinWidth instead.
            column.setPreferredWidth(Math.max(maxWidth, cellWidth));
            
            column.setCellEditor(new GridCellEditor());
        }
        
        //tableArg.setCellEditor(new GridCellEditor());

    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#aboutToShutdown()
     */
    public boolean aboutToShutdown()
    {
        super.aboutToShutdown();
        
        return true;
    }

    
    public class GridTableModel extends AbstractTableModel
    {
        public GridTableModel()
        {
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        public int getColumnCount()
        {
            return headers.size();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnName(int)
         */
        public String getColumnName(int column)
        {
            if (headers != null)
            {
                String label = headers.get(column).getCaption();
                return label != null ? label : "";
            }
            log.error("columnList should not be null!");
            return "N/A";
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getRowCount()
         */
        public int getRowCount()
        {
            return workbench.getWorkbenchRows().size();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        public Object getValueAt(int row, int column)
        {
            if (getRowCount() >= 0)
            {
                return workbench.getWorkbenchRowsAsList().get(row).getData(column);
            }
            return null;
        }

        public boolean isCellEditable(int row, int column)
        {
            return true;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        public Class<?> getColumnClass(int columnIndex)
        {
            Object obj = getValueAt(0, columnIndex);
            if (obj != null)
            {
                return obj.getClass();
                
            } else
            {
                return String.class;
            }
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
         */
        public void setValueAt(Object value, int row, int column)
        {
            if (getRowCount() >= 0)
            {
                workbench.getWorkbenchRowsAsList().get(row).setData(value.toString(), column);
            }
        }
        
        public void fireDataChanged()
        {
            this.fireTableDataChanged();
        }
        
        //void setEditMode(int row, int column)    { cells[row][column].state=SheetCell.EDITED; }

        //void setDisplayMode(int row, int column) { cells[row][column].state=SheetCell.UPDATED; }
    }
    class GridCellEditor extends AbstractCellEditor implements TableCellEditor
    {
        protected JTextField textField = new JTextField();

        public GridCellEditor()
        {

        }

        /* (non-Javadoc)
         * @see javax.swing.CellEditor#getCellEditorValue()
         */
        public Object getCellEditorValue() 
        {
            return textField.getText();
        }

        /* (non-Javadoc)
         * @see javax.swing.AbstractCellEditor#isCellEditable(java.util.EventObject)
         */
        public boolean isCellEditable(EventObject anEvent) 
        { 
            return true; 
        }
        
        //
        //          Implementing the CellEditor Interface
        //
        /** Implements the <code>TableCellEditor</code> interface. */
        public Component getTableCellEditorComponent(JTable  tbl, 
                                                     Object  value,
                                                     boolean isSelected,
                                                     int     row, 
                                                     int     column)
        {
            textField.setText(value != null ? value.toString() : "");
            return textField;
        }
     }

}

