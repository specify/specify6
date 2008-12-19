package edu.ku.brc.specify.tasks.subpane.security;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

public class GeneralPermissionTableCellEditor extends AbstractCellEditor
        implements TableCellEditor, ActionListener

{
    private GeneralPermissionTableCellValueWrapper currentWrapper;
    private Font                                   normalFont;
    //private Font                                   boldFont;
    private JLabel                                 adminLabel;
    private JCheckBox                              checkbox;

    /**
     * 
     */
    public GeneralPermissionTableCellEditor()
    {
        super();

        adminLabel = new JLabel("Always (Admin)");
        adminLabel.setHorizontalAlignment(SwingConstants.CENTER);
        adminLabel.setForeground(Color.LIGHT_GRAY);
        adminLabel.setFont(normalFont);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column)
    {
        currentWrapper = (GeneralPermissionTableCellValueWrapper) value;

        if (currentWrapper.isAdmin()) 
        {
            adminLabel.setText("Always (Admin)");
            return adminLabel;
        }
        else if (currentWrapper.isOverriden())
        {
            adminLabel.setText("(" + currentWrapper.getOverrulingPermissionText() + ")");
            return adminLabel;
        }

        checkbox = new JCheckBox();
        checkbox.setBackground(Color.WHITE);
        checkbox.setHorizontalAlignment(SwingConstants.CENTER);
        checkbox.setSelected(currentWrapper.getPermissionActionValue());
        return checkbox;
    }

    @Override
    public Object getCellEditorValue()
    {
        if (currentWrapper == null)
        {
            return null;
        }
        currentWrapper.setPermissionActionValue(checkbox.isSelected());
        return currentWrapper;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        fireEditingStopped();
    }
}
