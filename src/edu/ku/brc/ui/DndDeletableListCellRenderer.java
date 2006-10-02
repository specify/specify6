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
package edu.ku.brc.ui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import edu.ku.brc.ui.dnd.DndDeletable;

/**
 * Renders a list of DndDeletable Items
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class DndDeletableListCellRenderer extends JLabel implements ListCellRenderer 
{
    /**
     * Constructor with icon
     * @param icon the icon to be displayed (can be null)
     */
    public DndDeletableListCellRenderer()
    {
        // do nothing
    }
    
    // This is the only method defined by ListCellRenderer.
    // We just reconfigure the JLabel each time we're called.

    public Component getListCellRendererComponent(JList list,
                                                  Object value,            // value to display
                                                  int index,               // cell index
                                                  boolean isSelected,      // is the cell selected
                                                  boolean cellHasFocus)    // the list and the cell have the focus
    {
        if (value instanceof DndDeletable)
        {
            DndDeletable delObj = (DndDeletable)value;
            setText(delObj.getName());
            setIcon(delObj.getIcon());
            if (isSelected) 
            {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else 
            {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
        } else
        {
            setText(value.toString());
        }
        return this;
    }
}
