/* Copyright (C) 2015, University of Kansas Center for Research
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
import java.lang.reflect.Method;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;


/**
 * Provides a facility for producing components appropriate for rendering the cells
 * of a list.  If the objects being rendered have a <code>getName()</code> method that
 * returns a <code>String</code>, then that method is used to provide text for the
 * cell components.  Otherwise, the {@link Object#toString()} method is used.
 *
 * @code_status Complete
 * @author jstewart
 */
public class NameBasedListCellRenderer extends DefaultListCellRenderer
{
	/**
	 * Returns a {@link JLabel} with the text set to the value of <code>value</code>'s
	 * <code>getName()</code> method, if one exists.  The value of
	 * <code>value.toString()</code> is used otherwise. 
	 *
	 * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 * @param list the list the cells are contained in
	 * @param value the element to be rendered
	 * @param index the index of the element to be rendered
	 * @param isSelected indicator of the cell's selection state
	 * @param cellHasFocus indicator of the cell's focus state
	 * @return the paintable component
	 */
	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
	{
		JLabel l = (JLabel)super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
		
		Method getName = null;
		try
		{
			getName = value.getClass().getMethod("getName",new Class<?>[]{});
		}
		catch( NoSuchMethodException e )
		{
			// do nothing, just move on
		}
		
		if( getName!=null )
		{
			try
			{
				String name = (String) getName.invoke(value,new Object[]{});
				l.setText(name);
			}
			catch( Exception e )
			{
				l.setText(value.toString());
			}
		}

		return l;
	}

}
