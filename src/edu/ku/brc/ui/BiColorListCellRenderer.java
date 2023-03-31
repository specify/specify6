/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 28, 2009
 *
 */
public class BiColorListCellRenderer extends DefaultListCellRenderer
{
    protected static Color[] lineColor;
    
    static 
    {
        lineColor    = new Color[2];
        lineColor[0] = (new JList()).getBackground();
        lineColor[1] = UIHelper.getAltLineColor();
    }

    /**
     * 
     */
    public BiColorListCellRenderer()
    {
        super();
        setOpaque(true);
    }

    /* (non-Javadoc)
     * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    @Override
    public Component getListCellRendererComponent(final JList   list,
                                                  final Object  value,
                                                  final int     index,
                                                  final boolean isSelected,
                                                  final boolean cellHasFocus)
    {
        JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (!isSelected) 
        {
            setBackground(lineColor[index % 2]);
        }
        return label;
    }
}
