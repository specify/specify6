/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

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
        this.label     = new JLabel("  ");
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
            icon = qri.getIsInUse() == null ? IconManager
                .getIcon(qri.getIconName(), iconSize) : (qri.getIsInUse() ? IconManager.getIcon(
                "Checkmark", iconSize) : blankIcon);
        }
        iconLabel.setIcon(icon != null ? icon : blankIcon);
        kidLabel.setIcon(displayKidIndicator ? qri != null && qri.hasChildren() ? IconManager.getIcon("Forward",
                iconSize) : blankIcon : blankIcon);
        if (iss)
        {
            // setOpaque(true);
            panel.setBackground(list.getSelectionBackground());
            panel.setForeground(list.getSelectionForeground());
            list.setSelectedIndex(index);
        }
        else
        {
            // this.setOpaque(false);
            panel.setBackground(list.getBackground());
            panel.setForeground(list.getForeground());
        }
        label.setText(" " + (qri != null ? qri.getTitle() : "WTF"));
        panel.doLayout();
        return panel;
    }
}