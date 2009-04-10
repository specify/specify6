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

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

/**
 * A listener of clicks on the table column headers.
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
public class TableColumnHeaderListener extends MouseAdapter
{
    protected int columnIndex     = -1;
    protected int leftColumnIndex = -1;
    
    @Override
    public void mouseClicked(MouseEvent evt)
    {
        JTable table = ((JTableHeader) evt.getSource()).getTable();
        TableColumnModel colModel = table.getColumnModel();

        // The index of the column whose header was clicked
        columnIndex = colModel.getColumnIndexAtX(evt.getX());
        // int mColIndex = table.convertColumnIndexToModel(vColIndex);

        // Return if not clicked on any column header
        if (columnIndex == -1) 
        { 
            return; 
        }

        // Determine if mouse was clicked between column heads
        Rectangle headerRect = table.getTableHeader().getHeaderRect(columnIndex);
        if (columnIndex == 0)
        {
            headerRect.width -= 3; // Hard-coded constant
            
        } else
        {
            headerRect.grow(-3, 0); // Hard-coded constant
        }
        
        if (!headerRect.contains(evt.getX(), evt.getY()))
        {
            // Mouse was clicked between column heads
            // vColIndex is the column head closest to the click

            // vLeftColIndex is the column head to the left of the click
            leftColumnIndex = columnIndex;
            if (evt.getX() < headerRect.x)
            {
                leftColumnIndex--;
            }
        }
    }
}
