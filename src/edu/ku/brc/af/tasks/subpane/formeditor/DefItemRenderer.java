/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.af.tasks.subpane.formeditor;

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
        this.blankIcon = IconManager.getIcon("BlankIcon", iconSize);
        
        this.iconLabel = new JLabel(blankIcon);
        this.label     = new JLabel("  ");
        
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
        ImageIcon icon = item.isInUse() ? IconManager.getIcon("Checkmark", iconSize) : blankIcon;
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