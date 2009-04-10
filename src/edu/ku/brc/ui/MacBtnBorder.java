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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.border.Border;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 26, 2008
 *
 */
public class MacBtnBorder implements Border
{
    private Insets insets = new Insets(4, 4, 4, 4);
    
    private static Color c5 = new Color(196, 206, 226);
    private static Color c4 = new Color(153, 174, 213);
    private static Color c3 = new Color(112, 143, 202);
    private static Color c2 = new Color(170, 191, 230);
    private static Color c1 = new Color(211, 221, 241);
    private static Color[] colors;
    
    private Dimension arcSize = new Dimension(10, 10);
    
    static 
    {
        colors = new Color[] {c5, c4, c3, c2, c1 } ;
    }
    
    /**
     * 
     */
    public MacBtnBorder()
    {
        super();
    }
    
    public void setArc(final int arcWidth, final int arcHeight)
    {
        arcSize.setSize(arcWidth, arcHeight);
    }

    /* (non-Javadoc)
     * @see javax.swing.border.Border#getBorderInsets(java.awt.Component)
     */
    @Override
    public Insets getBorderInsets(Component c)
    {
        return new Insets(insets.left, insets.top, insets.right, insets.bottom);
    }

    /* (non-Javadoc)
     * @see javax.swing.border.Border#isBorderOpaque()
     */
    @Override
    public boolean isBorderOpaque()
    {
        return false;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.border.Border#paintBorder(java.awt.Component, java.awt.Graphics, int, int, int, int)
     */
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
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
            g2d.drawRoundRect(r.x, r.y, r.width, r.height, arcSize.width, arcSize.height);
            r.grow(-1, -1);
            cnt++;
        }
    }
    
}
