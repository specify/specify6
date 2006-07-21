package edu.ku.brc.ui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class JustifiedTableCellRenderer extends DefaultTableCellRenderer
{
	protected int	alignment;

	public JustifiedTableCellRenderer(final int alignment)
	{
		this.alignment = alignment;
	}

	// This method is called each time a cell in a column
	// using this renderer needs to be rendered.
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex)
	{
		JLabel l = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, vColIndex);
		l.setHorizontalTextPosition(alignment);
		l.setHorizontalAlignment(alignment);
		return l;
	}
}
