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
package edu.ku.brc.af.tasks.subpane.formeditor;

import static edu.ku.brc.ui.UIHelper.createLabel;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import edu.ku.brc.ui.IconManager;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 18, 2007
 *
 */
public class DefItemRenderer implements ListCellRenderer
{
    protected IconManager.IconSize iconSize;
    protected JPanel               panel;
    protected ImageIcon            blankIcon;
    protected JLabel               iconLabel;
    protected JLabel               label;
    
    public DefItemRenderer(final IconManager.IconSize iconSize) 
    {
        this.iconSize  = iconSize;
        this.blankIcon = IconManager.getIcon("BlankIcon", iconSize); //$NON-NLS-1$

        this.iconLabel = new JLabel(blankIcon);
        this.label     = createLabel("  "); //$NON-NLS-1$
        
        panel = new JPanel(new BorderLayout());
        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(label, BorderLayout.CENTER);
        iconLabel.setOpaque(false);
        label.setOpaque(false);
    }

    public Component getListCellRendererComponent(JList   list,
                                                  Object  value,   // value to display
                                                  int     index,      // cell index
                                                  boolean iss,    // is the cell selected
                                                  boolean chf)    // the list and the cell have the focus
    {
        JGoodiesDefItem item = (JGoodiesDefItem)value;
        ImageIcon icon = item.isInUse() ? IconManager.getIcon("Checkmark", iconSize) : blankIcon; //$NON-NLS-1$
        iconLabel.setIcon(icon != null ? icon : blankIcon);
        
        if (iss) {
            panel.setBackground(list.getSelectionBackground());
            panel.setForeground(list.getSelectionForeground());
            list.setSelectedIndex(index);

        } else {
            panel.setBackground(list.getBackground());
            panel.setForeground(list.getForeground());
        }

        label.setText(item.toString());
        panel.doLayout();
        return panel;
    }
}
