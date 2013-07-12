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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *  A simple vector button that draw a close on its face.
 * 
 * @code_status Beta
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class CloseButton extends GradiantButton
{
    protected Color   closeColor      = Color.WHITE;
    protected Color   closeHoverColor = new Color(200, 102, 102);
    protected boolean isHovering      = false; 

    /**
     * Default Constructor
     */
    public CloseButton() 
    {
        super("");
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                isHovering = true;
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e)
            {
                isHovering = false;
                repaint();
            }
            
        });
    }  
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g) 
    {       
        super.paint(g);
        
        FontMetrics fm = this.getFontMetrics(getFont());
        int         w  = (int)(fm.getAscent() * 0.8);
        Rectangle   r  = new Rectangle((getWidth() - w) / 2, (getHeight() - w) / 2, w, w);
        
        if (pressed) 
        {
            g.translate(1, 1);
        }
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
        g.setColor(isHovering ? closeHoverColor : closeColor);
        g.fillOval(r.x, r.y, r.width, r.height);

        g.setColor(isHovering ? new Color(64,64,64) : getForeground());
        r.grow(-3, -3);
        g.drawLine(r.x, r.y,          r.x+r.width-1,   r.y+r.height);
        g.drawLine(r.x, r.y+r.height, r.x+r.width-1, r.y);
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
