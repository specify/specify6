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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;

/**
 *  A simple vector button that draw a triangle on its face.
 * 
 * @code_status Beta
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class TriangleButton extends GradiantButton
{
    
protected static RenderingHints hints;
    
    static
    {
        hints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }
    
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
    @Override
    public void paint(Graphics g) 
    {       
        super.paint(g);
        
        Graphics2D g2d = (Graphics2D)g;
        g2d.addRenderingHints(hints);
        
        if (pressed) 
        {
            g.translate(1, 1);
        }
        poly.reset();
        Rectangle r = getBounds();
        int delta = (int)(r.width * 0.28);
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
    @Override
    public void mouseClicked(MouseEvent evt) 
    { 
        //isDown = !isDown;
        repaint();
    }

    
}
