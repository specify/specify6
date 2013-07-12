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
package edu.ku.brc.ui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/*
 * @code_status Beta
 **
 * @author rods
 *
 */
public class JustifiedTableCellRenderer extends DefaultTableCellRenderer
{
	protected int	alignment;

	public JustifiedTableCellRenderer(final int alignment)
	{
		this.alignment = alignment;
	}

	// This method is called each time a cell in a column
	// using this renderer needs to be rendered.
	@Override
    public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex)
	{
		JLabel l = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, vColIndex);
		l.setHorizontalTextPosition(alignment);
		l.setHorizontalAlignment(alignment);
		return l;
	}
}
