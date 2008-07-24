package edu.ku.brc.specify.tasks.subpane.security;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

@SuppressWarnings("serial")
public class TristateEditor extends AbstractCellEditor implements TableCellEditor 
{
	int row;
	int col;
	TriStateCheckBox check;
	
	TristateEditor()
	{
		check = new TriStateCheckBox();
		check.setHorizontalAlignment(JLabel.CENTER);
	}

	public Object getCellEditorValue() {
		return check.getState();
	}

	public Component getTableCellEditorComponent(JTable table, Object value, 
			boolean isSelected, int row, int column) 
	{
		if(isSelected)
		{
			check.setForeground(table.getSelectionForeground());
			check.setBackground(table.getSelectionBackground());
		}
		else
		{
			check.setForeground(table.getForeground());
			check.setBackground(table.getBackground());
		}
		this.col = column;
		this.row = row;
		return check;
	}
}
