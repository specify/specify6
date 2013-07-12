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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.border.AbstractBorder;

import edu.ku.brc.af.ui.SearchBox;

/**
 * @author rod
 *
 * @code_status Beta
 *
 * Sep 9, 2007
 *
 */
public class SearchBorderMac extends AbstractBorder
{
    protected Color topColor1     = new Color(119,119,119);
    protected Color topColor2     = new Color(199,199,199);
    protected Color topColor3     = new Color(241,241,241);
    
    protected Color botEndsColor  = new Color(169,169,169);
    
    protected int   iconWidth     = 0;
    
    // Focus Border Colors
    private int extra = 5;
    private static Color c5 = new Color(196, 206, 226);
    private static Color c4 = new Color(153, 174, 213);
    private static Color c3 = new Color(112, 143, 202);
    private static Color c2 = new Color(170, 191, 230);
    //private static Color c1 = new Color(211, 221, 241);
    private static Color[] colors;
    
    //private Dimension arcSize = new Dimension(10, 10);
    
    static 
    {
        colors = new Color[] {c5, c4, c3, c2} ;
    }
    
    /**
     * @param iconWidth the width of the search icon
     */
    public SearchBorderMac(int iconWidth)
    {
        this.iconWidth = iconWidth;
    }

    /* (non-Javadoc)
     * @see javax.swing.border.AbstractBorder#paintBorder(java.awt.Component, java.awt.Graphics, int, int, int, int)
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
    {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int half = height / 2;

        // First Line
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
        g.setColor(botEndsColor);
        g.drawArc(x+0, y+0, height, height-1, 180, 90);              // Bottom Left
        g.drawArc(x+width-height-1, y+0, height, height-1, 270, 90); // Bottom Right

        g.setColor(new Color(160,160,160));
        g.drawArc(x+0, y+0, height, height-1, 170, 20);              // Bottom Left
        g.drawArc(x+width-height-1, y+0, height, height-1, 350, 20); // Bottom Right
        
        g.setColor(botEndsColor);
        g.drawLine(x+half, y+height-1, x+width-half-1, y+height-1);      // Bottom Line
        
        // Inside Ends
        g.setColor(topColor3);
        g.drawArc(x+1, y+1, hgt, hgt-1, 180, 90);                // Bottom Left
        g.drawArc(x+width-height-2, y+0, height-1, height-2, 270, 90); // Bottom Right
        g.drawLine(x+half, y+height-2, x+width-half-1, y+height-2);      // Bottom Line
        
        if (((SearchBox)c).getSearchText().hasFocus())
        {
            Rectangle r = new Rectangle(x, y, width-1, height-1);
            int cnt = 0;
            for (Color clr : colors)
            {
                g.setColor(clr);
                //g.drawRect(r.x, r.y, r.width, r.height);
                Graphics2D g2d = (Graphics2D)g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setStroke(UIHelper.getStdLineStroke());
                g2d.drawRoundRect(r.x, r.y, r.width, r.height, height, height);//arcSize.width, arcSize.height);
                r.grow(-1, -1);
                cnt++;
            } 
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.border.AbstractBorder#getBorderInsets(java.awt.Component)
     */
    public Insets getBorderInsets(Component c)
    {
        return new Insets(6 + extra, 7+iconWidth + extra, 3 + extra, 7 + extra);
    }

    /* (non-Javadoc)
     * @see javax.swing.border.AbstractBorder#getBorderInsets(java.awt.Component, java.awt.Insets)
     */
    public Insets getBorderInsets(Component c, Insets i)
    {
        i.top    = 6 + extra;
        i.left   = 7 + iconWidth + extra;
        i.bottom = 3 + extra;
        i.right  = 7 + extra;
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
