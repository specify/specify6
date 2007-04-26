/*
 * Copyright (C) 2007 The University of Kansas
 * 
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

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
    protected IconManager.IconSize iconSize;
    
    protected JPanel               display;
    protected JLabel               icon1;
    protected JLabel               icon2;
    protected JLabel               label;
    protected ImageIcon            arrowRight = IconManager.getIcon("move_right");
    protected ImageIcon            arrowDown  = IconManager.getIcon("DownArrow");

    public TableInfoListRenderer(final IconManager.IconSize iconSize)
    {
        // Don't paint behind the component
        this.iconSize = iconSize;

        int size = iconSize.size();
        PanelBuilder builder = new PanelBuilder(new FormLayout(arrowRight.getIconWidth()+"px,2px,"+size+"px,2px,f:p:g", "c:p"));
        CellConstraints cc = new CellConstraints();

        builder.add(icon1 = new JLabel(), cc.xy(1, 1));
        builder.add(icon2 = new JLabel(), cc.xy(3, 1));
        builder.add(label = new JLabel(), cc.xy(5, 1));
        display = builder.getPanel();
        display.setBorder(BorderFactory.createEmptyBorder(2, 1, 2, 1));
    }
    
    public int getTextOffset()
    {
        return arrowRight.getIconWidth() + 2;
    }

    public Component getListCellRendererComponent(JList list, Object value, // value to display
                                                  int index, // cell index
                                                  boolean isSelected, // is the cell selected
                                                  boolean chf) // the list and the cell have the
                                                                // focus
    {
        TableListItemIFace ti   = (TableListItemIFace)value;
        
        ImageIcon icon = ti.getIcon();
        if (ti.isExpandable())
        {
            icon1.setIcon(ti.isExpanded() ? arrowDown : arrowRight);
            icon2.setIcon(icon);
            
        } else
        {
            icon1.setIcon(IconManager.getIcon("BlankIcon", iconSize));
            icon2.setIcon(ti.isChecked() ? icon : null);
        }
        label.setText(ti.getText());
        
        if (isSelected)
        {
            display.setOpaque(true);
            display.setBackground(list.getSelectionBackground());
            display.setForeground(list.getSelectionForeground());

        } else
        {
            display.setOpaque(false);
            display.setBackground(list.getBackground());
            display.setForeground(list.getForeground());
        }

        return display;
    }
}