package edu.ku.brc.specify.tasks.subpane.security;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class YesNoCellRenderer extends DefaultTableCellRenderer
{
    public final String YES = getResourceString("YES");
    public final String NO  = getResourceString("NO");
    
    public Font boldFont  = null;
    public Font normalFont = null;
    
    /**
     * 
     */
    public YesNoCellRenderer()
    {
        super();
        setHorizontalAlignment(SwingConstants.CENTER);
        
        normalFont = getFont();
        boldFont   = normalFont.deriveFont(Font.BOLD);
    }


    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column)
    {
        JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value instanceof Boolean)
        {
            label.setForeground(((Boolean)value) ? Color.BLACK : Color.LIGHT_GRAY);
            label.setFont(((Boolean)value) ? boldFont : normalFont);
            label.setText(((Boolean)value) ? YES : NO);
        }
        else if (value instanceof GeneralPermissionTableCellValueWrapper)
        {
            // it's a wrapper object that supports displaying of overriding permissions
            GeneralPermissionTableCellValueWrapper wrapper = (GeneralPermissionTableCellValueWrapper) value;
            if (!wrapper.isOverriden()) 
            {
                // not overriden, so display regular label
                label.setForeground(wrapper.getPermissionActionValue() ? Color.BLACK : Color.LIGHT_GRAY);
                label.setFont(wrapper.getPermissionActionValue() ? boldFont : normalFont);
                label.setText(wrapper.getPermissionActionValue() ? YES : NO);
            }
            else
            {
                label.setForeground(Color.DARK_GRAY);
                label.setFont(boldFont);
                label.setText("<html><center><font size=\"-2\">" + YES + "</font><br>" + 
                        "<font size=\"-3\">(" + wrapper.getOverrulingPermissionText() + 
                        ")</font><center></html>");
            }
        }
        return label;
    }
    
}
