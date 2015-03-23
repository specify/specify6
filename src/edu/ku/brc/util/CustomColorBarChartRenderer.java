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
package edu.ku.brc.util;

import java.awt.Color;
import java.awt.Paint;

import org.jfree.chart.renderer.category.BarRenderer3D;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 29, 2008
 *
 */
public class CustomColorBarChartRenderer extends BarRenderer3D
{
    private Paint[] colors;
    
    int[] rgb = {142, 32, 166,
                    87,  32, 166,
                    32,  32, 166,
                    32, 87, 166,
                    32, 142, 166,
                    32, 166, 32,
                    87, 166, 32,
                    142, 166, 32,
                    166, 142, 32,
                    166, 87, 32,
                    166, 32, 32};
        
    /**
     * 
     */
    public CustomColorBarChartRenderer()
    {
        colors = new Paint[rgb.length];
        for (int i = 0; i < rgb.length; i++)
        {
            colors[i / 3] = new Color(rgb[i], rgb[i + 1], rgb[i + 2]);
            i += 2;
        }
    }

    /* (non-Javadoc)
     * @see org.jfree.chart.renderer.AbstractRenderer#getItemPaint(int, int)
     */
    public Paint getItemPaint(final int row, final int column)
    {
        return (this.colors[row % colors.length]);
    }
}
