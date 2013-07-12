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
package edu.ku.brc.af.core.expresssearch;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;

import edu.ku.brc.ui.IconManager;

/**
 * Renderer for the Table List from DBTableIdMgr.
 *  
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 23, 2007
 *
 */
public class TableNameRenderer extends DefaultListCellRenderer 
{
    protected IconManager.IconSize iconSize;
    protected ImageIcon            blankIcon;
    
    public TableNameRenderer() 
    {
        // Don't paint behind the component
        this.setOpaque(false);
        this.iconSize  = null;
        this.blankIcon = null;
    }

    public TableNameRenderer(final IconManager.IconSize iconSize) 
    {
        // Don't paint behind the component
        this.setOpaque(false);
        this.iconSize  = iconSize;
        this.blankIcon = IconManager.getIcon("Blank", iconSize); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    @Override
    public Component getListCellRendererComponent(JList list,
                                                  Object value,   // value to display
                                                  int index,      // cell index
                                                  boolean iss,    // is the cell selected
                                                  boolean chf)    // the list and the cell have the focus
    {
        super.getListCellRendererComponent(list, value, index, iss, chf);

        TableNameRendererIFace ti   = (TableNameRendererIFace)value;
        if (iconSize != null)
        {
            ImageIcon              icon = IconManager.getIcon(ti.getIconName(), iconSize);
            setIcon(icon != null ? icon : blankIcon);
        }
        
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

        setText(ti.getTitle());
        return this;
    }

    /**
     * @param useIconName the icon to use instead for blank icons
     */
    public void setUseIcon(final String useIconName)
    {
        blankIcon = IconManager.getIcon(useIconName, iconSize);
    }
}
