package edu.ku.brc.specify.tasks.subpane.security;

import javax.swing.JCheckBox;

/**
 * A custom JCheckBox that holds a permission cell wrapper inside it.
 * It's used to control edits inside a GeneralPermissionEditor.
 * 
 * Note that we store the cell value inside the wrapper, besides the boolean flag 
 * that comes with the JCheckBox. That's because I preferred not to mess with the
 * internals of JCheckBox to conciliate both values. Instead, the client should
 * call synchPermissionValue() to get the permission boolean value to be sync'ed
 * with the inner JCheckBox flag that may have been updated during cell editing.
 * 
 * @author Ricardo
 *
 */
@SuppressWarnings("serial")
public class GeneralPermissionTableCheckBox extends JCheckBox
{

    private GeneralPermissionTableCellValueWrapper cellValue;

    public GeneralPermissionTableCheckBox(
            GeneralPermissionTableCellValueWrapper cellValue)
    {
        this.cellValue = cellValue;
    }

    public GeneralPermissionTableCellValueWrapper getCellValue()
    {
        return cellValue;
    }

    public void setCellValue(GeneralPermissionTableCellValueWrapper cellValue)
    {
        this.cellValue = cellValue;
        setSelected(cellValue.getPermissionActionValue());
    }
    
    /**
     * Synchronizes the boolean value in the wrapper with the check box flag.
     */
    public void synchPermissionValue()
    {
        cellValue.setPermissionActionValue(isSelected());
    }
}
