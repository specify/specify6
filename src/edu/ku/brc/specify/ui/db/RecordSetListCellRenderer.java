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
package edu.ku.brc.specify.ui.db;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.ui.IconManager;

/**
 * Renders a list item with an icon to the left of the text
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class RecordSetListCellRenderer extends JLabel implements ListCellRenderer 
{
    /**
     * Constructor with icon
     * @param icon the icon to be displayed (can be null)
     */
    public RecordSetListCellRenderer()
    {
        super();
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    }
    
    // This is the only method defined by ListCellRenderer.
    // We just reconfigure the JLabel each time we're called.

    public Component getListCellRendererComponent(JList list,
                                                  Object value,            // value to display
                                                  int index,               // cell index
                                                  boolean isSelected,      // is the cell selected
                                                  boolean cellHasFocus)    // the list and the cell have the focus
    {
        if (value instanceof RecordSetIFace)
        {
            RecordSetIFace rs = (RecordSetIFace)value;
            DBTableInfo    ti = DBTableIdMgr.getInstance().getInfoById(rs.getDbTableId());
            if (ti != null)
            {
                setIcon(ti.getIcon(IconManager.IconSize.Std16));
            }
            setText(rs.getName());
            
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
            setText("N/A");
            setIcon(null);
        }
        return this;
    }
}
