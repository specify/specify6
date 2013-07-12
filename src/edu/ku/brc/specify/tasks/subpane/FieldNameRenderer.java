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
package edu.ku.brc.specify.tasks.subpane;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;

import edu.ku.brc.af.core.expresssearch.TableFieldPair;
import edu.ku.brc.ui.IconManager;

/**
 * Renderer for the Field List.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 23, 2007
 *
 */
public class FieldNameRenderer extends DefaultListCellRenderer 
{
    protected ImageIcon checkMark;
    protected ImageIcon blankIcon;
    
    public FieldNameRenderer(IconManager.IconSize iconSize) 
    {
        // Don't paint behind the component
        this.setOpaque(false);
        checkMark   = IconManager.getIcon("Checkmark", iconSize);
        blankIcon   = IconManager.getIcon("BlankIcon", iconSize);
    }

    public Component getListCellRendererComponent(JList list,
                                                  Object value,   // value to display
                                                  int index,      // cell index
                                                  boolean iss,    // is the cell selected
                                                  boolean chf)    // the list and the cell have the focus
    {
        super.getListCellRendererComponent(list, value, index, iss, chf);

        TableFieldPair tblField = (TableFieldPair)value;
        setIcon(tblField.isInUse() ? checkMark : blankIcon);
        
        if (iss) {
            setOpaque(true);
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
            list.setSelectedIndex(index);

        } else {
            this.setOpaque(false);
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        setText(tblField.getTitle());
        return this;
    }

}
