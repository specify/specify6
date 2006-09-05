/**
 * 
 */
package edu.ku.brc.ui.renderers;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;

import edu.ku.brc.util.Nameable;

/**
 * A {@link ListCellRenderer} implementation for use when displaying a model of
 * {@link Nameable} objects.  If the renderer is passed an object that does not
 * implement {@link Nameable}, the result is the same as that of passing the
 * object to a {@link DefaultListCellRenderer}.
 *
 * @code_status Complete
 * @author jstewart
 */
public class NameableListItemCellRenderer extends DefaultListCellRenderer
{
	/**
	 * Returns a {@link Component} used to 'paint' the list cell representing the given
	 * value.
	 *
	 * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 * @param list the list being rendered
	 * @param value the object being rendered
	 * @param index the index of the rendered object in the {@link ListModel}
	 * @param isSelected whether or not the list item is currently selected
	 * @param cellHasFocus whether or not the list item currently has focus
	 * @return the {@link Component} used to 'paint' the list cell
	 */
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
