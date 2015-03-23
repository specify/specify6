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
package edu.ku.brc.specify.tasks.subpane.wb;

import static edu.ku.brc.ui.UIHelper.createLabel;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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
public class TableInfoListRenderer implements ListCellRenderer
{
    protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
    private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    
    protected IconManager.IconSize iconSize;
    
    protected JPanel               display;
    protected JLabel               icon1;
    protected JLabel               label;
    protected ImageIcon            checkmarkIcon = IconManager.getIcon("Checkmark", IconManager.IconSize.Std16);
    protected ImageIcon            blankIcon     = IconManager.getIcon("BlankIcon", IconManager.STD_ICON_SIZE);

    public TableInfoListRenderer(final IconManager.IconSize iconSize)
    {
        // Don't paint behind the component
        this.iconSize = iconSize;

        PanelBuilder builder = new PanelBuilder(new FormLayout(blankIcon.getIconWidth()+"px,2px,f:p:g", "c:p"));
        CellConstraints cc = new CellConstraints();

        builder.add(icon1 = createLabel(""), cc.xy(1, 1));
        builder.add(label = createLabel(""), cc.xy(3, 1));
        display = builder.getPanel();
        display.setBorder(BorderFactory.createEmptyBorder(2, 1, 2, 1));
        display.setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list, Object value, // value to display
                                                  int index, // cell index
                                                  boolean isSelected, // is the cell selected
                                                  boolean cellHasFocus) // the list and the cell have the
                                                                // focus
    {
        TableListItemIFace ti   = (TableListItemIFace)value;
        
        if (ti.isExpandable())
        {
            icon1.setIcon(ti.getIcon());
            
        } else
        {
            icon1.setIcon(IconManager.getIcon("BlankIcon", iconSize));
            icon1.setIcon(ti.isChecked() ? checkmarkIcon : blankIcon);
        }
        label.setText(ti.getText());
        
        if (isSelected)
        {
            display.setBackground(list.getSelectionBackground());
            label.setForeground(list.getSelectionForeground());

        } else
        {
            display.setBackground(list.getBackground());
            label.setForeground(list.getForeground());
        }
        
        display.setEnabled(list.isEnabled());
        display.setFont(list.getFont());

        Border border = null;
        if (cellHasFocus)
        {
            if (isSelected)
            {
                border = UIManager.getBorder("List.focusSelectedCellHighlightBorder");
            }
            if (border == null)
            {
                border = UIManager.getBorder("List.focusCellHighlightBorder");
            }
        } else
        {
            border = getNoFocusBorder();
        }
        display.setBorder(border);
        return display;
    }
    
    private static Border getNoFocusBorder() 
    {
        if (System.getSecurityManager() != null) {
            return SAFE_NO_FOCUS_BORDER;
        }
        return noFocusBorder;
    }
}
