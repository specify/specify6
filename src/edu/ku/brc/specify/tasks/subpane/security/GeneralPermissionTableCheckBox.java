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
package edu.ku.brc.specify.tasks.subpane.security;

import javax.swing.JCheckBox;

/**
 * A custom JCheckBox that holds a permission cell wrapper inside it.
 * It's used to control edits inside a GeneralPermissionEditor.
 * 
 * Note that we store the cell value inside the wrapper, besides the boolean flag 
 * that comes with the JCheckBox. That's because I preferred not to mess with the
 * internals of JCheckBox to conciliate both values. Instead, the client should
 * call synchPermissionValue() to get the permission boolean value to be sync'ed
 * with the inner JCheckBox flag that may have been updated during cell editing.
 * 
 * @author Ricardo
 *
 */
@SuppressWarnings("serial")
public class GeneralPermissionTableCheckBox extends JCheckBox
{
    private GeneralPermissionTableCellValueWrapper cellValue;

    /**
     * @param cellValue
     */
    public GeneralPermissionTableCheckBox(final GeneralPermissionTableCellValueWrapper cellValue)
    {
        this.cellValue = cellValue;
    }

    /**
     * @return
     */
    public GeneralPermissionTableCellValueWrapper getCellValue()
    {
        return cellValue;
    }

    /**
     * @param cellValue
     */
    public void setCellValue(GeneralPermissionTableCellValueWrapper cellValue)
    {
        this.cellValue = cellValue;
        setSelected(cellValue.getPermissionActionValue());
    }
    
    /**
     * Synchronizes the boolean value in the wrapper with the check box flag.
     */
    public void synchPermissionValue()
    {
        cellValue.setPermissionActionValue(isSelected());
    }
}
