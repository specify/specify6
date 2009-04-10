/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.qb;

import static edu.ku.brc.ui.UIHelper.createLabel;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.IconManager;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 18, 2007
 *
 */
public class QryListRenderer implements ListCellRenderer
{
    protected IconManager.IconSize iconSize;
    protected JPanel               panel;
    protected ImageIcon            kidIcon = null;
    protected ImageIcon            blankIcon;
    protected JLabel               iconLabel;
    protected JLabel               label;
    protected JLabel               kidLabel;
    protected boolean              displayKidIndicator = true;
    
    public QryListRenderer(final IconManager.IconSize iconSize) 
    {
        this.iconSize  = iconSize;
        this.blankIcon = IconManager.getIcon("BlankIcon", iconSize);
        
        this.iconLabel = new JLabel(blankIcon);
        this.label     = createLabel("  ");
        this.kidLabel  = new JLabel(blankIcon);

        if (true)
        {
            panel = new JPanel(new BorderLayout());
            panel.add(iconLabel, BorderLayout.WEST);
            panel.add(label, BorderLayout.CENTER);
            panel.add(kidLabel, BorderLayout.EAST);
        } else
        {
            PanelBuilder pb = new PanelBuilder(new FormLayout("p,2px,f:p:g,1px,p", "p"));
            CellConstraints cc = new CellConstraints();
            pb.add(iconLabel, cc.xy(1,1));
            pb.add(label, cc.xy(3,1));
            pb.add(kidLabel, cc.xy(5,1));
            panel = pb.getPanel();
        }
        panel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        iconLabel.setOpaque(false);
        label.setOpaque(false);
        kidLabel.setOpaque(false);
    }

    /**
     * @param displayKidIndicator the displayKidIndicator to set
     */
    public void setDisplayKidIndicator(boolean displayKidIndicator)
    {
        this.displayKidIndicator = displayKidIndicator;
    }

    public Component getListCellRendererComponent(JList list, Object value, // value to display
                                                  int index, // cell index
                                                  boolean iss, // is the cell selected
                                                  boolean chf) // the list and the cell have the
                                                                // focus
    {
        QryListRendererIFace qri = (QryListRendererIFace) value;
        ImageIcon icon;
        if (qri == null) 
        {
            icon = blankIcon;
        }
        else
        {
            icon = qri.getIsInUse() == null ? IconManager.getIcon(qri.getIconName(), iconSize) :
                   (qri.getIsInUse() ? IconManager.getIcon("Checkmark", IconManager.IconSize.Std16) : blankIcon);
        }
        iconLabel.setIcon(icon != null ? icon : blankIcon);
        
        ImageIcon childIcon = blankIcon;
        if (displayKidIndicator)
        {
            if (qri != null)
            {
                if (qri.hasChildren())
                {
                    childIcon = IconManager.getIcon(qri.hasMultiChildren() ? "MultiKid" : "SingleKid", iconSize);
                }
            }
        }
        kidLabel.setIcon(childIcon);
        
        if (iss)
        {
            // setOpaque(true);
            panel.setBackground(list.getSelectionBackground());
            label.setForeground(list.getSelectionForeground());
            list.setSelectedIndex(index);
        }
        else
        {
            // this.setOpaque(false);
            panel.setBackground(list.getBackground());
            label.setForeground(list.getForeground());
        }
        label.setText(" " + (qri != null ? qri.getTitle() : "?"));
        panel.doLayout();
        return panel;
    }
}
