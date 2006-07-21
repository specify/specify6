/* This library is free software; you can redistribute it and/or
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
package edu.ku.brc.ui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 *  A simple vector button that draw a close on its face.
 *  
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class CloseButton extends GradiantButton
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
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    public void paint(Graphics g) 
    {       
        super.paint(g);
        
        FontMetrics fm = this.getFontMetrics(getFont());
        int w = (int)((double)fm.getAscent() * 0.8);
        Rectangle r = new Rectangle((getWidth() - w) / 2, (getHeight() - w) / 2, w, w);
        
        if (pressed) 
        {
            g.translate(1, 1);
        }
        //Graphics2D g2d = (Graphics2D)g;
        //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
        g.setColor(closeColor);
        g.fillOval(r.x, r.y, r.width, r.height);

        g.setColor(getForeground());
        r.grow(-3, -3);
        g.drawLine(r.x, r.y, r.x+r.width, r.y+r.height);
        g.drawLine(r.x, r.y+r.height, r.x+r.width, r.y);
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
