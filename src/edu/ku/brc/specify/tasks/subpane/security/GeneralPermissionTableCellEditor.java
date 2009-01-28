package edu.ku.brc.specify.tasks.subpane.security;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

/**
 * Table cell editor for the table created by the GeneralPermissionEditor class.
 * 
 * @author Ricardo
 *
 */
@SuppressWarnings("serial")
public class GeneralPermissionTableCellEditor extends AbstractCellEditor
        implements TableCellEditor, ActionListener

{
    private GeneralPermissionTableCheckBox customCheckbox;

    /**
     * Constructor.
     */
    public GeneralPermissionTableCellEditor()
    {
        super();
        
        customCheckbox = new GeneralPermissionTableCheckBox(null);
        customCheckbox.setBackground(Color.WHITE);
        customCheckbox.setHorizontalAlignment(SwingConstants.CENTER);
        customCheckbox.addActionListener(this);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column)
    {
        GeneralPermissionTableCellValueWrapper wrapper = (GeneralPermissionTableCellValueWrapper) value;
        wrapper.prepareComponent(customCheckbox);
        return customCheckbox;
    }

    @Override
    public Object getCellEditorValue()
    {
        return customCheckbox.getCellValue();
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        // synchronizes the permission action boolean inside the customCheckbox with its JCheckBox flag value.
        customCheckbox.synchPermissionValue();
        fireEditingStopped();
    }
}
