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
        
        panel = new JPanel(new BorderLayout());
        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(label, BorderLayout.CENTER);
        panel.add(kidLabel, BorderLayout.EAST);
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

    public Component getListCellRendererComponent(JList   list,
                                                  Object  value,   // value to display
                                                  int     index,      // cell index
                                                  boolean iss,    // is the cell selected
                                                  boolean chf)    // the list and the cell have the focus
    {
        QryListRendererIFace qri = (QryListRendererIFace)value;
        ImageIcon icon = qri.getIsInUse() == null ? IconManager.getIcon(qri.getIconName(), iconSize) : (qri.getIsInUse() ? IconManager.getIcon("Checkmark", iconSize) : blankIcon);
        iconLabel.setIcon(icon != null ? icon : blankIcon);
        kidLabel.setIcon(displayKidIndicator ? qri.hasChildren() ? IconManager.getIcon("Forward", iconSize) : blankIcon : blankIcon);
        
        if (iss) {
            //setOpaque(true);
            panel.setBackground(list.getSelectionBackground());
            panel.setForeground(list.getSelectionForeground());
            list.setSelectedIndex(index);

        } else {
            //this.setOpaque(false);
            panel.setBackground(list.getBackground());
            panel.setForeground(list.getForeground());
        }

        label.setText(qri.getTitle());
        panel.doLayout();
        return panel;
    }
}