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
        display.setOpaque(true);
    }
    
    public int getTextOffset()
    {
        return arrowRight.getIconWidth() + 2;
    }

    public Component getListCellRendererComponent(JList list, Object value, // value to display
                                                  int index, // cell index
                                                  boolean isSelected, // is the cell selected
                                                  boolean cellHasFocus) // the list and the cell have the
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
            if (cellHasFocus) {
                if (isSelected) {
                    border = UIManager.getBorder("List.focusSelectedCellHighlightBorder");
                }
                if (border == null) {
                    border = UIManager.getBorder("List.focusCellHighlightBorder");
                }
            } else {
                border = getNoFocusBorder();
            }
            display.setBorder(border);
        return display;
    }
    
    private static Border getNoFocusBorder() {
        if (System.getSecurityManager() != null) {
            return SAFE_NO_FOCUS_BORDER;
        } else {
            return noFocusBorder;
        }
    }
}