package edu.ku.brc.specify.ui.treetables;

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
		
		// only process customizations if the passed in value is the correct class
		if( value instanceof TreeDefinitionIface )
		{
			TreeDefinitionIface treeDef = (TreeDefinitionIface)value;
			l.setText(treeDef.getName());			
		}
		
		return l;
	}
}
