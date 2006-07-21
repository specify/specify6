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

package edu.ku.brc.specify.ui;

import java.awt.Color;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import edu.ku.brc.specify.ui.SortableTableModel.SortableTableHeaderCellRenderer;

/**
 * Wraps the JTable so it can always provide the corrent model, which in this case is the "deleegate model".
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
            public void mouseClicked(MouseEvent evt)
            {
                super.mouseClicked(evt);
                if (columnIndex > -1)
                {
                    getSortableTableModel().setSortColumn(columnIndex);
                }
            }
        };
        
        JTableHeader header = getTableHeader();
        if (header != null)
        {
            header.addMouseListener(new LocalTableColHeaderListener());
        }
        
        SortableTableModel              sortableModel = getSortableTableModel();
        SortableTableHeaderCellRenderer cellRenderer  = sortableModel.new SortableTableHeaderCellRenderer();
        cellRenderer.setTextColor(Color.WHITE);
        
        //cellRenderer.setForeground(new Color(30, 144, 255));  // XXX PREF ??? Blue
        cellRenderer.setForeground(new Color(102, 153, 153));  // XXX PREF ??? Cadet Blue
        
        TableColumnModel colModel = getColumnModel();
        for (int i=0;i<colModel.getColumnCount();i++)
        {
            TableColumn col = colModel.getColumn(i);
            col.setHeaderRenderer(cellRenderer);
        }
    }

}
