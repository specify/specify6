package edu.ku.brc.specify.tasks.subpane.security;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class GeneralPermissionTableCellRenderer extends
        DefaultTableCellRenderer
{
    public Font normalFont;
    public Font boldFont;

    private JLabel adminLabel;
    private JCheckBox checkbox;
    
    /**
     * 
     */
    public GeneralPermissionTableCellRenderer()
    {
        super();
        setHorizontalAlignment(SwingConstants.CENTER);
        
        normalFont = getFont();
        boldFont   = normalFont.deriveFont(Font.BOLD);

        adminLabel = new JLabel("Always (Admin)");
        adminLabel.setHorizontalAlignment(SwingConstants.CENTER);
        adminLabel.setForeground(Color.LIGHT_GRAY);
        adminLabel.setFont(normalFont);
        
        checkbox = new JCheckBox();
        checkbox.setForeground(Color.BLACK);
        checkbox.setBackground(Color.WHITE);
        checkbox.setHorizontalAlignment(SwingConstants.CENTER);
        adminLabel.setFont(normalFont);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column)
    {
        if (value instanceof GeneralPermissionTableCellValueWrapper)
        {
            // it's a wrapper object that supports displaying of overriding
            // permissions
            // JLabel label = (JLabel)super.getTableCellRendererComponent(table,
            // new Boolean(true), isSelected, hasFocus, row, column);
            GeneralPermissionTableCellValueWrapper wrapper = (GeneralPermissionTableCellValueWrapper) value;
            
            if (wrapper.isAdmin()) 
            {
                return adminLabel;
            }

            String text = "";
            if (wrapper.isOverriden())
            {
                text = "(" + wrapper.getOverrulingPermissionText() + ")";
            }
            checkbox.setText(text);
            checkbox.setSelected(wrapper.getPermissionActionValue());
            return checkbox;
        }
        return null;
    }
}
