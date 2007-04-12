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
package edu.ku.brc.specify.extras;

import java.util.List;
import java.util.Vector;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * This was quickly implemented for the SPNHC Demo
 *
 * This is a special table model for the BioGeomancer Table in the dialog
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
public class BGMRecordTableModel implements TableModel
{
    protected Vector<TableModelListener> listeners = new Vector<TableModelListener>();
    protected List<String[]> rowData;
    protected Vector<String> methods;
    protected String[]       header = {"ID", "Country", "Adm1", "Adm2", "Feat. Name", "Feat. Type",
                                       "Gazetteer", "Coords", "Offset", "Bounding Box", "Locality"};

    /**
     * @param rowData
     */
    public BGMRecordTableModel(final List<String[]> rowData)
    {
        this.rowData = rowData;
    }

    public int getColumnCount()
    {
        return header.length;
    }

    public String getColumnName(int column)
    {
        return header[column];
    }

    public int getRowCount()
    {
        return rowData.size();
    }

    public Object getValueAt(int row, int column)
    {
        return rowData.get(row)[column];
    }

    public boolean isCellEditable(int row, int column)
    {
        return false;
    }

    public Class<?> getColumnClass(int columnIndex)
    {
        return String.class;
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        return;
    }

    public void addTableModelListener(TableModelListener l)
    {
        listeners.add(l);
    }

    public void removeTableModelListener(TableModelListener l)
    {
        listeners.remove(l);
    }
}
