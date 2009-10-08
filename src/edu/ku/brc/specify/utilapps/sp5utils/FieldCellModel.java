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
package edu.ku.brc.specify.utilapps.sp5utils;

import javax.swing.table.DefaultTableModel;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Oct 8, 2009
 *
 */
public class FieldCellModel extends DefaultTableModel
{
    protected String[] headers = {"Field Name", "Sp6 Name", "Caption", "Control Type", "Data Type", "Related", "Top", "Left", "Width", "Height"};
    
    protected FormInfo selectedForm = null;
    
    /**
     * 
     */
    public FieldCellModel()
    {
        super();
    }

    /**
     * @param selectedForm the selectedForm to set
     */
    public void setSelectedForm(FormInfo selectedForm)
    {
        this.selectedForm = selectedForm;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(int column)
    {
        return headers[column];
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    @Override
    public int getColumnCount()
    {
        return headers == null ? 0 : headers.length;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    @Override
    public int getRowCount()
    {
        return selectedForm != null ? selectedForm.getFields().size() : 0;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int row, int column)
    {
        return false;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        if (selectedForm != null)
        {
            FormFieldInfo fi = selectedForm.getFields().get(rowIndex);
            
            switch (columnIndex)
            {
                case 0: return fi.getSp5FieldName();
                case 1: return fi.getSp6FieldName();
                case 2: return fi.getCaption();
                case 3: return fi.getControlType();
                case 4: return fi.getDataType();
                case 5: return fi.getRelatedTableName() == null ? "" : fi.getRelatedTableName();
                case 6: return fi.getTop();
                case 7: return fi.getLeft();
                case 8: return fi.getWidth();
                case 9: return fi.getHeight();
            }
        }
        return "x";
    }
}