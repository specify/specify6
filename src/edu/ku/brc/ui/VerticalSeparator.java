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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 28, 2007
 *
 */
public class VerticalSeparator extends JComponent
{
    protected Color bgColor;
    protected Color fgColor;
    protected int   preferredWidth = 10;
    
    protected static BasicStroke lineStroke = new BasicStroke(1.5f, BasicStroke.CAP_SQUARE, BasicStroke.CAP_SQUARE);

    
    /**
     * @param bgColor
     * @param fgColor
     */
    public VerticalSeparator(final Color fgColor, final Color bgColor)
    {
        this.bgColor = bgColor;
        this.fgColor = fgColor;
    }

    /**
     * @param bgColor
     * @param fgColor
     */
    public VerticalSeparator(final Color fgColor, final Color bgColor, final int preferredWidth)
    {
        this.bgColor = bgColor;
        this.fgColor = fgColor;
        this.preferredWidth = preferredWidth;
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D)g;
        
        g2d.setRenderingHints(UIHelper.createTextRenderingHints());
        g2d.setStroke(lineStroke);
        
        Dimension size = getSize();
        
        int x = size.width / 2;
        
        g.setColor(bgColor);
        g.drawLine(x, 2, x, size.height-1);
        //g.drawLine(x+1, 2, x+1, size.height-1);
        
        g.setColor(fgColor);
        g.drawLine(x-2, 0, x-2, size.height-3);
        //g.drawLine(x-1, 0, x-1, size.height-3);
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize()
    {
        Dimension size = super.getPreferredSize();
        size.width = Math.max(size.width, 4);
        size.width = preferredWidth;
        return size;
    }

}
