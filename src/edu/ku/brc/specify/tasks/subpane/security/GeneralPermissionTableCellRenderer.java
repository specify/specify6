package edu.ku.brc.specify.tasks.subpane.security;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Table cell renderer for the table created by the GeneralPermissionEditor class.
 * 
 * @author Ricardo
 *
 */
@SuppressWarnings("serial")
public class GeneralPermissionTableCellRenderer extends
        DefaultTableCellRenderer
{
    private GeneralPermissionTableCheckBox customCheckbox;
    
    /**
     * Default constructor.
     */
    public GeneralPermissionTableCellRenderer()
    {
        super();
        setHorizontalAlignment(SwingConstants.CENTER);
        customCheckbox = new GeneralPermissionTableCheckBox(null);
        customCheckbox.setBackground(Color.WHITE);
        customCheckbox.setHorizontalAlignment(SwingConstants.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column)
    {
        if (value instanceof GeneralPermissionTableCellValueWrapper)
        {
            // it's a wrapper object that supports displaying of overriding permissions
            GeneralPermissionTableCellValueWrapper wrapper = (GeneralPermissionTableCellValueWrapper) value;
            wrapper.prepareComponent(customCheckbox);
            return customCheckbox;
        }

        return null;
    }
}
