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
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

/**
 *  A simple vector button that draw a triangle on its face.
 * 
 * @code_status Unknown (auto-generated)
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class TriangleButton extends GradiantButton
{
    /**
     * 
     */
    protected boolean isDown        = true; 
    protected Polygon poly          = new Polygon();
    protected Color   triangleColor = Color.WHITE;

    /**
     * Default Constructor
     *
     */
    public TriangleButton() 
    {
        super("");
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    public void paint(Graphics g) 
    {       
        super.paint(g);
        
        if (pressed) 
        {
            g.translate(1, 1);
        }
        poly.reset();
        Rectangle r = getBounds();
        int delta = (int)((double)r.width * 0.28);
        r.grow(-delta, -delta);
        
        if (isDown)
        {
            poly.addPoint(r.x, r.y);
            poly.addPoint(r.x+r.width, r.y);
            poly.addPoint(r.x+(r.width / 2), r.y+r.height);
            poly.addPoint(r.x, r.y);
        } else
        {
            poly.addPoint(r.x, r.y);
            poly.addPoint(r.x, r.y+r.height);
            poly.addPoint(r.x+r.width, r.y+(r.height / 2));
            poly.addPoint(r.x, r.y);            
        }
        
        g.setColor(triangleColor);
        g.fillPolygon(poly);
    }

    /**
     * Returns whether the arrow is down indicating things are expanded
     * @return Returns whether the arrow is down indicating things are expanded
     */
    public boolean isDown()
    {
        return isDown;
    }

    /**
     * Sets whether the button should the arrow pointing down. True means expanded, false means collapsed
     * and calls repaint
     * @param isDown the direction (true - down, false - up)
     */
    public void setDown(boolean isDown)
    {
        this.isDown = isDown;
        repaint();
    }

    /**
     * @return Returns the color of the triangle
     */
    public Color getTriangleColor()
    {
        return triangleColor;
    }

    /**
     * Sets the triangle's color on the button and calls repaint.
     * @param triangleColor the new color for the triangle
     */
    public void setTriangleColor(Color triangleColor)
    {
        this.triangleColor = triangleColor;
        repaint();
    }
    
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent evt) 
    { 
        //isDown = !isDown;
        repaint();
    }

    
}
