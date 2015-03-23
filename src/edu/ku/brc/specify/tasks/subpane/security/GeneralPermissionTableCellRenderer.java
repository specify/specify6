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

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Table cell renderer for the table created by the GeneralPermissionEditor class.
 * 
 * @author Ricardo
 *
 */
@SuppressWarnings("serial")
public class GeneralPermissionTableCellRenderer extends
        DefaultTableCellRenderer
{
    private GeneralPermissionTableCheckBox customCheckbox;
    
    /**
     * Default constructor.
     */
    public GeneralPermissionTableCellRenderer()
    {
        super();
        setHorizontalAlignment(SwingConstants.CENTER);
        customCheckbox = new GeneralPermissionTableCheckBox(null);
        customCheckbox.setBackground(Color.WHITE);
        customCheckbox.setHorizontalAlignment(SwingConstants.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column)
    {
        if (value instanceof GeneralPermissionTableCellValueWrapper)
        {
            // it's a wrapper object that supports displaying of overriding permissions
            GeneralPermissionTableCellValueWrapper wrapper = (GeneralPermissionTableCellValueWrapper) value;
            wrapper.prepareComponent(customCheckbox);
            return customCheckbox;
        }

        return null;
    }
}
