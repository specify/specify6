/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.security;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * @author Ricardo
 *
 * @code_status Alpha
 *
 * Jul 20, 2008
 *
 */
@SuppressWarnings("serial")
public class TristateEditor extends AbstractCellEditor implements TableCellEditor 
{
	int row;
	int col;
	TriStateCheckBox check;
	
	public TristateEditor()
	{
		check = new TriStateCheckBox();
		check.setHorizontalAlignment(JLabel.CENTER);
	}

	public Object getCellEditorValue() 
	{
		return check.getState();
	}

	public Component getTableCellEditorComponent(JTable table, Object value, 
			boolean isSelected, int rowArg, int column) 
	{
		if (isSelected)
		{
			check.setForeground(table.getSelectionForeground());
			check.setBackground(table.getSelectionBackground());
		} else
		{
			check.setForeground(table.getForeground());
			check.setBackground(table.getBackground());
		}
		this.col = column;
		this.row = rowArg;
		return check;
	}
}
