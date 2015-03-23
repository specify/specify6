/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.qb;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.specify.tasks.subpane.ESResultsTablePanel;
import edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace;
import edu.ku.brc.specify.ui.db.ResultSetTableModel;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
@SuppressWarnings("serial")
public class QBResultsTablePanel extends ESResultsTablePanel
{
    private static final int MAX_COL_STR_WIDTH = 50;
    
    public QBResultsTablePanel(final ExpressSearchResultsPaneIFace esrPane,
                               final QueryForIdResultsIFace    results,
                               final boolean                   installServices,
                               final boolean                   isExpandedAtStartUp)
    {
        super(esrPane, results, installServices, isExpandedAtStartUp);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanel#createModel()
     */
    @Override
    protected ResultSetTableModel createModel()
    {
        return new QBResultSetTableModel(this, results);
    }
    
    /**
     * @return the tableModel
     */
    public QBResultSetTableModel getTableModel()
    {
    	return (QBResultSetTableModel )resultSetTableModel;
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanel#setupTablePane()
	 */
	@Override
	protected void setupTablePane() {
        Component comp = new JScrollPane(table);
        tablePane.add(comp, BorderLayout.CENTER);
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanel#autoResizeColWidth(javax.swing.JTable, javax.swing.table.DefaultTableModel)
	 */
	@Override
	protected void autoResizeColWidth(JTable table, DefaultTableModel model) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        //table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.setModel(model);

        int margin = 5;
        
        DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
        
        int   preferredWidthTotal = 0;
        int   renderedWidthTotal  = 0;
        int[] colWidths           = new int[table.getColumnCount()];
        int[] strWidths          = new int[table.getColumnCount()];
        for (int i = 0; i < table.getColumnCount(); i++)
        {
            int                     vColIndex = i;
            TableColumn             col       = colModel.getColumn(vColIndex);
            int                     width     = 0;

            TableCellRenderer headerRenderer = col.getHeaderRenderer();
            if (headerRenderer instanceof JLabel)
            {
                ((JLabel)headerRenderer).setHorizontalAlignment(SwingConstants.CENTER);
            }

            // Get width of column header
            TableCellRenderer renderer = col.getCellRenderer();
            if (renderer == null)
            {
                renderer = table.getTableHeader().getDefaultRenderer();
            }

            Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(),
                                                                    false, false, 0, 0);

            width = comp.getPreferredSize().width;

            // Get maximum width of column data
            int     strWidth = 0;
            boolean isString = model.getColumnClass(i) == String.class;
            for (int r=0;r<table.getRowCount();r++)
            {
                renderer = table.getCellRenderer(r, vColIndex);
                Object objVal = table.getValueAt(r, vColIndex);
                if (isString && objVal != null)
                {
                    strWidth = Math.max(strWidth, ((String)objVal).length());
                }
                comp = renderer.getTableCellRendererComponent(table, objVal, false, false, r, vColIndex);
                width = Math.max(width, comp.getPreferredSize().width);
            }

            // Add margin
            width += 2 * margin;

            preferredWidthTotal += col.getPreferredWidth();
            colWidths[i] = width;
            strWidths[i] = strWidth;
            
            renderedWidthTotal += width;
        }
        
        String maxWidthStr = "";
        for (int i = 0; i < MAX_COL_STR_WIDTH; i++) {
        	maxWidthStr += "x";
        }
        
        int sumWidths = 0;
		for (int i = 0; i < table.getColumnCount(); i++) {
			TableColumn col = colModel.getColumn(i);
			TableCellRenderer renderer = col.getCellRenderer();
			if (renderer != null) {
				((JLabel) renderer).setHorizontalAlignment(SwingConstants.LEFT);
			}

			if (renderedWidthTotal > preferredWidthTotal) {
				Component comp = renderer.getTableCellRendererComponent(table,
						maxWidthStr, false, false, 1, i);
				col.setPreferredWidth(Math.min(colWidths[i],
						comp.getPreferredSize().width));
				col.setWidth(Math.min(colWidths[i],
						comp.getPreferredSize().width));
			}
			sumWidths += col.getPreferredWidth();
		}

		//Attempt to stretch rightmost col to fill empty space to right
//		if (renderedWidthTotal <= preferredWidthTotal) {
//	        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);			
//		}
		//System.out.println(sumWidths + " < " + tablePane.getWidth() + "?");
        if (sumWidths  < tablePane.getWidth()) {
        	int addWidth = tablePane.getWidth() - sumWidths;
        	TableColumn col = colModel.getColumn(colWidths.length - 1);
        	col.setPreferredWidth(col.getPreferredWidth() + addWidth);
            //table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        }
        

        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        // table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
	}
    
    
}
