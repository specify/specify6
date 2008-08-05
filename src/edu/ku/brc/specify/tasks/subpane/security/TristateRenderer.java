/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.security;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * @author Ricardo
 *
 * @code_status Alpha
 *
 * Jul 20, 2008
 *
 */
@SuppressWarnings("serial")
public class TristateRenderer extends TriStateCheckBox implements TableCellRenderer
{
	TristateRenderer()
	{
		this.setHorizontalAlignment(JLabel.CENTER);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, 
			boolean isSelected, boolean hasFocus, int row, int column) 
	{
		if(isSelected)
		{
			setForeground(table.getSelectionForeground());
			setBackground(table.getSelectionBackground());
		}
		else
		{
			setForeground(table.getForeground());
			setBackground(table.getBackground());
		}

		return this;
	}
}

