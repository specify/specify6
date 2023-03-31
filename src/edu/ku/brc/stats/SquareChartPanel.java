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
package edu.ku.brc.stats;

import java.awt.Rectangle;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 * Simple derived class from ChartPanel that enables the Chart to always be a square instead of being stretched.
 * At this time it always sets the width to the height.
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class SquareChartPanel extends ChartPanel
{

    public SquareChartPanel(JFreeChart arg0)
    {
        super(arg0);
   }

    public SquareChartPanel(JFreeChart arg0, boolean arg1)
    {
        super(arg0, arg1);
    }

    public SquareChartPanel(JFreeChart arg0, boolean arg1, boolean arg2,
            boolean arg3, boolean arg4, boolean arg5)
    {
        super(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    public SquareChartPanel(JFreeChart arg0, int arg1, int arg2, int arg3,
            int arg4, int arg5, int arg6, boolean arg7, boolean arg8,
            boolean arg9, boolean arg10, boolean arg11, boolean arg12)
    {
        super(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9,
                arg10, arg11, arg12);
    }

    public void setBounds(Rectangle r)
    {
        //System.out.println(r);
        setBounds(r.x, r.y, r.width, r.height);
    }

    public void setBounds(int x, int y, int width, int height)
    {
        super.setBounds(x, y, width, height);
    }

}
