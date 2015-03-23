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
package edu.ku.brc.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;


/**
 * Renders every other line a white and a shaded color depending on the platform.
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class BiColorTableCellRenderer extends DefaultTableCellRenderer
{
    protected static Color[] lineColor;
    
    static 
    {
        lineColor    = new Color[2];
        lineColor[0] = (new JTable()).getBackground();
        lineColor[1] = UIHelper.getAltLineColor();
    }
    
    /**
     * Constructor.
     */
    public BiColorTableCellRenderer()
    {
        this(true);
    }

    /**
     * Constructor.
     */
    public BiColorTableCellRenderer(final boolean isCellCentered)
    {
        super();
        setOpaque(true);
        if (isCellCentered)
        {
            setHorizontalAlignment(SwingConstants.CENTER);
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(final JTable  table,
                                                   final Object  value,
                                                   final boolean isSelected,
                                                   final boolean hasFocus,
                                                   final int     row,
                                                   final int     column)
    {
        JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (!isSelected) 
        {
            setBackground(lineColor[row % 2]);
        }
        return label;

    }
}
