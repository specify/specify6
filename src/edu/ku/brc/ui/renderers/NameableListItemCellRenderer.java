/**
 * 
 */
package edu.ku.brc.ui.renderers;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import edu.ku.brc.util.Nameable;

/**
 *
 *
 * @author jstewart
 * @version %I% %G%
 */
public class NameableListItemCellRenderer extends DefaultListCellRenderer
{
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
	{
		JLabel l = (JLabel)super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
		
		if(value instanceof Nameable)
		{
			l.setText(((Nameable)value).getName());
		}
		
		return l;
	}

}
