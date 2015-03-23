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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

/**
 * Table cell editor for the table created by the GeneralPermissionEditor class.
 * 
 * @author Ricardo
 *
 */
@SuppressWarnings("serial")
public class GeneralPermissionTableCellEditor extends AbstractCellEditor
        implements TableCellEditor, ActionListener

{
    private GeneralPermissionTableCheckBox customCheckbox;

    /**
     * Constructor.
     */
    public GeneralPermissionTableCellEditor()
    {
        super();
        
        customCheckbox = new GeneralPermissionTableCheckBox(null);
        customCheckbox.setBackground(Color.WHITE);
        customCheckbox.setHorizontalAlignment(SwingConstants.CENTER);
        customCheckbox.addActionListener(this);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, 
                                                 Object value,
                                                 boolean isSelected, 
                                                 int row, 
                                                 int column)
    {
        GeneralPermissionTableCellValueWrapper wrapper = (GeneralPermissionTableCellValueWrapper) value;
        wrapper.prepareComponent(customCheckbox);
        return customCheckbox;
    }

    @Override
    public Object getCellEditorValue()
    {
        return customCheckbox.getCellValue();
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        // synchronizes the permission action boolean inside the customCheckbox with its JCheckBox flag value.
        customCheckbox.synchPermissionValue();
        fireEditingStopped();
    }
}
