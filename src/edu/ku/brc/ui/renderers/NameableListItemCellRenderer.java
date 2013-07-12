/* Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
