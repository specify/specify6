package edu.ku.brc.specify.ui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import edu.ku.brc.specify.datamodel.TreeDefinitionIface;

public class TreeDefListCellRenderer extends DefaultListCellRenderer
{
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
	{
		JLabel l = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);
		
		TreeDefinitionIface treeDef = (TreeDefinitionIface)value;
		l.setText(treeDef.getName());
		
		return l;
	}
}
