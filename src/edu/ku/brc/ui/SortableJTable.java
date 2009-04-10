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
package edu.ku.brc.ui;

import java.awt.Color;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import edu.ku.brc.ui.SortableTableModel.SortableTableHeaderCellRenderer;

/**
 * Wraps the JTable so it can always provide the corrent model, which in this case is the "deleegate model".
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
public class SortableJTable extends JTable
{
    protected SortableTableModel model = null;
    
    /**
     * Constructor with the Appropriate Sortable model
     * @param model the sortable model
     */
    public SortableJTable(final SortableTableModel model) 
    {
        super();
        
        this.model = model;
        
        setModel(model);
        
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JTable#getModel()
     */
    @Override
    public TableModel getModel()
    {
        return model != null ? model.getDelegateModel() : null;
    }
    
    /**
     * Return the sortable table model
     * @return the sortable table model
     */
    public SortableTableModel getSortableTableModel()
    {
        return model;
    }
    
    
    /**
     * 
     */
    public void installColumnHeaderListeners()
    {
        class LocalTableColHeaderListener extends TableColumnHeaderListener
        {
            @Override
            public void mouseClicked(MouseEvent evt)
            {
                super.mouseClicked(evt);
                if (columnIndex > -1)
                {
                    getSortableTableModel().setSortColumn(columnIndex);
                }
            }
        }
        
        JTableHeader header = getTableHeader();
        if (header != null)
        {
            header.addMouseListener(new LocalTableColHeaderListener());
        }
        
        SortableTableModel              sortableModel = getSortableTableModel();
        SortableTableHeaderCellRenderer cellRenderer  = sortableModel.new SortableTableHeaderCellRenderer();
        cellRenderer.setTextColor(Color.WHITE);
        
        //cellRenderer.setForeground(new Color(30, 144, 255));  // XXX PREF ??? Blue
        cellRenderer.setBGBaseColor(new Color(102, 153, 153));  // XXX PREF ??? Cadet Blue
        
        TableColumnModel colModel = getColumnModel();
        for (int i=0;i<colModel.getColumnCount();i++)
        {
            TableColumn col = colModel.getColumn(i);
            col.setHeaderRenderer(cellRenderer);
        }
    }

}
