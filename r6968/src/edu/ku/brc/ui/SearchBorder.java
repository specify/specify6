/* Copyright (C) 2009, University of Kansas Center for Research
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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.border.AbstractBorder;

/**
 * Parially rounded border with space of an icon.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Sep 9, 2007
 *
 */
public class SearchBorder extends AbstractBorder
{
    protected Color topColor1     = new Color(119,119,119);
    protected Color topColor2     = new Color(199,199,199);
    protected Color topColor3     = new Color(241,241,241);
    
    protected Color botEndsColor  = new Color(169,169,169);
    protected int   iconWidth     = 0;
    
    /**
     * @param iconWidth the width of the search icon
     */
    public SearchBorder(int iconWidth)
    {
        this.iconWidth = iconWidth;
    }

    /* (non-Javadoc)
     * @see javax.swing.border.AbstractBorder#paintBorder(java.awt.Component, java.awt.Graphics, int, int, int, int)
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int heightArg)
    {
        int height = heightArg / 2;
        
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int half = height / 2;
        
        // First Line
        g.setColor(Color.WHITE);
        g.fillArc(x, y, height, height, 90, 90);              // Top Left
        g.fillArc(x+width-height-1, y, height, height, 0, 90); // Top Right
        
        g.setColor(topColor1);
        g.drawArc(x, y, height, height, 90, 90);              // Top Left
        g.drawArc(x+width-height-1, y, height, height, 0, 90); // Top Right
        g.drawLine(x+half, y, x+width-half-1, y);                // Top
        
        // Second Line
        g.setColor(topColor2);
        int hgt = height - 1;
        g.drawArc(x+1, y+1, hgt, hgt, 90, 90);              // Top Left
        g.drawArc(x+width-height-1, y+1, hgt, hgt, 0, 90); // Top Right
        g.drawLine(x+half+1, y+1, x+width-half-2, y+1);       // Top
        
        // Third Line
        g.setColor(topColor3);
        hgt = height - 2;
        g.drawArc(x+2, y+2, hgt, hgt, 90, 90);              // Top Left
        g.drawArc(x+width-height-1, y+2, hgt, hgt, 0, 90); // Top Right
        g.drawLine(x+half+2, y+2, x+width-half-3, y+2);        // Top
        
        // Bottom Line
        // Outside Ends
        g.setColor(Color.WHITE);
        g.fillArc(x+0, y+heightArg-height, height, height-1, 180, 90);              // Bottom Left
        g.fillArc(x+width-height-1, y+heightArg-height, height, height-1, 270, 90); // Bottom Right

        g.setColor(botEndsColor);
        g.drawArc(x+0, y+heightArg-height, height, height-1, 180, 90);              // Bottom Left
        g.drawArc(x+width-height-1, y+heightArg-height, height, height-1, 270, 90); // Bottom Right
        
        g.setColor(new Color(160,160,160));
        g.drawLine(x,         y+half, x, y+heightArg-half-1);      // Bottom Line
        g.drawLine(x+width-1, y+half-1, x+width-1, y+heightArg-half-1);      // Bottom Line
        
        g.setColor(botEndsColor);
        g.drawLine(x+half, y+heightArg-1, x+width-half-1, y+heightArg-1);      // Bottom Line
        
        // Inside Ends
        g.setColor(topColor3);
        g.drawArc(x+1, y+heightArg-height+1, hgt, hgt-1, 180, 90);                // Bottom Left
        g.drawArc(x+width-height-2, y+heightArg-height, height-1, height-2, 270, 90); // Bottom Right
       
    }

    /* (non-Javadoc)
     * @see javax.swing.border.AbstractBorder#getBorderInsets(java.awt.Component)
     */
    public Insets getBorderInsets(Component c)
    {
        return new Insets(6, 7+iconWidth, 3, 7);
    }

    /* (non-Javadoc)
     * @see javax.swing.border.AbstractBorder#getBorderInsets(java.awt.Component, java.awt.Insets)
     */
    public Insets getBorderInsets(Component c, Insets i)
    {
        i.top    = 6;
        i.left   = 7+iconWidth;
        i.bottom = 3;
        i.right  = 7;
        return i;
    }

    /* (non-Javadoc)
     * @see javax.swing.border.AbstractBorder#isBorderOpaque()
     */
    public boolean isBorderOpaque()
    {
        return true;
    }
}
