/* Copyright (C) 2013, University of Kansas Center for Research
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

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 *
 */
@SuppressWarnings("serial")
public class YesNoCellRenderer extends DefaultTableCellRenderer
{
    public final String YES = getResourceString("YES");
    public final String NO  = getResourceString("NO");
    
    public Font boldFont  = null;
    public Font normalFont = null;
    
    /**
     * 
     */
    public YesNoCellRenderer()
    {
        super();
        setHorizontalAlignment(SwingConstants.CENTER);
        
        normalFont = getFont();
        boldFont   = normalFont.deriveFont(Font.BOLD);
    }


    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column)
    {
        JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value instanceof Boolean)
        {
            label.setForeground(((Boolean)value) ? Color.BLACK : Color.LIGHT_GRAY);
            label.setFont(((Boolean)value) ? boldFont : normalFont);
            label.setText(((Boolean)value) ? YES : NO);
        }
        else if (value instanceof GeneralPermissionTableCellValueWrapper)
        {
            // it's a wrapper object that supports displaying of overriding permissions
            GeneralPermissionTableCellValueWrapper wrapper = (GeneralPermissionTableCellValueWrapper) value;
            if (!wrapper.isOverriden()) 
            {
                // not overriden, so display regular label
                label.setForeground(wrapper.getPermissionActionValue() ? Color.BLACK : Color.LIGHT_GRAY);
                label.setFont(wrapper.getPermissionActionValue() ? boldFont : normalFont);
                label.setText(wrapper.getPermissionActionValue() ? YES : NO);
            }
            else
            {
                label.setForeground(Color.DARK_GRAY);
                label.setFont(boldFont);
                label.setText("<html><center><font size=\"-2\">" + YES + "</font><br>" + 
                        "<font size=\"-3\">(" + wrapper.getOverrulingPermissionText() + 
                        ")</font><center></html>");
            }
        }
        return label;
    }
    
}
