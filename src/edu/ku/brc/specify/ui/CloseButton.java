/* Filename:    $RCSfile: CloseButton.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.ui;

import java.awt.*;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;

/**
 *  A simple vector button that draw a close on its face.
 *  
 * @author rods
 *
 */
public class CloseButton extends VectorButton
{
    /**
     * 
     */
    protected Color   closeColor = Color.WHITE;

    /**
     * Default Constructor
     *
     */
    public CloseButton() 
    {
        super("");
    }
    
    /* (non-Javadoc)
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize() 
    {
        FontMetrics fm = this.getFontMetrics(getFont());
        float scale = (50f/30f)*this.getFont().getSize2D();
        int h = fm.getHeight();
        h += (int)(scale*.3f);
        return new Dimension(h, h);
    }  
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    public void paintComponent(Graphics g) 
    {       
        super.paintComponent(g);
        
        Rectangle r = getBounds();
        r.x = 0;
        r.y = 0;
        int delta = (int)((double)r.width * 0.28);
        r.grow(-delta, -delta);

        //Graphics2D g2d = (Graphics2D)g;
        //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
        g.setColor(closeColor);
        g.fillOval(r.x, r.y, r.width, r.height);

        g.setColor(getForeground());
        r.grow(-2, -2);
        g.drawLine(r.x, r.y, r.x+r.width-1, r.y+r.height-1);
        g.drawLine(r.x, r.y+r.height-1, r.x+r.width-1, r.y);
    }

    /**
     * @return Returns the color of the close
     */
    public Color getCloseColor()
    {
        return closeColor;
    }

    /**
     * Sets the close's color on the button and calls repaint.
     * @param closeColor the new color for the close
     */
    public void setCloseColor(Color closeColor)
    {
        this.closeColor = closeColor;
        repaint();
    }
    
}