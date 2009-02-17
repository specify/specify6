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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;

import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;

/**
 * This Border class draws a curved border
 */
/*
 * @code_status Beta
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class CurvedBorder extends AbstractBorder
{
    protected Color     borderInnerColor = Color.LIGHT_GRAY;
    protected Color     borderOuterColor = Color.DARK_GRAY;
    protected int       borderWidth      = 2;
    protected int       arcSize          = 10;
    protected Dimension size             = new Dimension(1,1);

    /**
     * Default Constructor
     *
     */
    public CurvedBorder()
    {
        // do nothing
    }

    /**
     * 
     * @param borderWidth the border width
     */
    public CurvedBorder(int borderWidth)
    {
        this.borderWidth = borderWidth;
    }

    /**
     * 
     * @param borderColor the border color
     */
    public CurvedBorder(Color borderColor)
    {
        this.borderOuterColor = borderColor;
    }

    /**
     * Constructor
     * @param borderWidth the border width
     * @param borderColor the border color
     */
    public CurvedBorder(int borderWidth, Color borderColor)
    {
        this.borderWidth = borderWidth;
        this.borderOuterColor = borderColor;
    }

    /**
     * Constructor
     * @param borderWidth the border width
     * @param borderOuterColor the border color
     */
    public CurvedBorder(int borderWidth, Color borderInnerColor, Color borderOuterColor)
    {
        this.borderWidth = borderWidth;
        this.borderOuterColor = borderOuterColor;
    }

    /* (non-Javadoc)
     * @see javax.swing.border.Border#paintBorder(java.awt.Component, java.awt.Graphics, int, int, int, int)
     */
    @Override
    public void paintBorder(final Component c, final Graphics g, final int xc, final int yc, final int width, final int height)
    {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        BasicStroke    stdLineStroke    = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2d.setStroke(stdLineStroke);
        //g2d.setStroke(UIHelper.getStdLineStroke());
        
        g2d.setColor(new Color(64, 64, 64));
        if (false)
        {

            Color color = UIManager.getColor("CheckBox.background");
            Color grad_top = color;
            Color grad_bot = UIHelper.changeColorBrightness(color, 0.80);
            
            GradientPaint bg = new GradientPaint(new Point(0,0), grad_top,
                                                 new Point(0,height/2), grad_bot);
            g2d.setPaint(bg);
            g2d.fillArc(xc, yc, width-1, height-1, 0, 360);
            Shape clip = g2d.getClip();
            
            g2d.setClip(0,height/2,width,height);
            
            bg = new GradientPaint(new Point(0,height/2), grad_bot,
                                   new Point(0,height),   grad_top);
            
            g2d.setPaint(bg);
            g2d.fillArc(xc, yc, width-1, height-1, 0, 360);
            
            g2d.setClip(clip);
            
            g2d.setPaint(null);
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawArc(xc, yc, width-1, height-1, 0, 360);
        }
        
        if (true)
        {
            borderInnerColor = Color.WHITE;
            borderOuterColor = UIManager.getColor("Button.shadow");
    
            borderInnerColor = UIManager.getColor("Button.shadow");
            borderOuterColor = Color.WHITE;
    
            arcSize          = 16;
            
            int x = xc;
            int y = yc;
            int w = width;
            int h = height;
            for (int i=0;i<1;i++)
            {
                g2d.setColor(borderOuterColor);
                paintUpperLeftBorder(g2d, x-i, y-i, w-i, h-i,  arcSize);
                //paintLowerRightBorder(g2d, x, y, w, y, arcSize);
                
                g2d.setColor(borderInnerColor);
                //paintUpperLeftBorder(g2d, x, y, w, h,  arcSize);
                paintLowerRightBorder(g2d, x-i, y-i, w-i, h-i, arcSize);
            }
            g2d.setColor(Color.BLACK);
            stdLineStroke    = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            g2d.setStroke(stdLineStroke);
            //g2d.drawArc(1, 1, w-1, h-1, 0, 360);
        }
        
    }

    protected void paintUpperLeftBorder(final Graphics2D g2d, 
                                          final int x, 
                                          final int y, 
                                          final int w, 
                                          final int h,
                                          final int arcSz)
    {
        
        int halfArc = arcSz / 2;
        int lenH    = h - (arcSz/2);
        
        g2d.drawLine(x+halfArc+1, 0,     x+w-halfArc-1, 0);  // top
        g2d.drawLine(x,     y+1+arcSz/2, x,     y+lenH);     // left
        
        g2d.drawArc(x, y, arcSz, arcSz, 90, 90);              // upper left
        g2d.drawArc(x+w-arcSz-1, y, arcSz, arcSz, 45, 45);   // upper right
        g2d.drawArc(x, y+h-arcSz-1, arcSz, arcSz, 180, 45); // bottom left
        
    }

    /**
     * @param g2d
     * @param x
     * @param y
     * @param w
     * @param h
     * @param arcSz
     */
    protected void paintLowerRightBorder(final Graphics2D g2d, 
                                          final int x, 
                                          final int y, 
                                          final int w, 
                                          final int h,
                                          final int arcSz)
    {
        
        int halfArc = arcSz / 2;
        int lenH    = h - halfArc;
        
        g2d.drawLine(x+halfArc+1, y+h-1, x+w-halfArc-1, y+h-1); // bottom
        g2d.drawLine(x+w-1, y-1+arcSz/2, x+w-1, y+lenH);        // right
    
        g2d.drawArc(x,           y+h-arcSz-1, arcSz, arcSz, 225, 45); // bottom left
        g2d.drawArc(x+w-arcSz-1, y,           arcSz, arcSz, 0, 45);   // upper right
        g2d.drawArc(x+w-arcSz-1, y+h-arcSz-1, arcSz, arcSz, 270, 90); // bottom right
    }

    /* (non-Javadoc)
     * @see javax.swing.border.Border#getBorderInsets(java.awt.Component)
     */
    @Override
    public Insets getBorderInsets(Component c)
    {
        return new Insets(borderWidth, borderWidth, borderWidth, borderWidth);
    }

    /* (non-Javadoc)
     * @see javax.swing.border.AbstractBorder#getBorderInsets(java.awt.Component, java.awt.Insets)
     */
    @Override
    public Insets getBorderInsets(Component c, Insets i)
    {
        i.left = i.right = i.bottom = i.top = borderWidth;
        return i;
    }

    /* (non-Javadoc)
     * @see javax.swing.border.Border#isBorderOpaque()
     */
    @Override
    public boolean isBorderOpaque()
    {
        return true;
    }

    /**
     * 
     * @return the border color
     */
     public Color getBorderInnerColor()
    {
        return borderInnerColor;
    }

     /**
      * 
      * @return the border color
      */
      public Color getBorderOuterColor()
     {
         return borderOuterColor;
     }


    /**
     * @param borderInnerColor the borderInnerColor to set
     */
    public void setBorderInnerColor(Color borderInnerColor)
    {
        this.borderInnerColor = borderInnerColor;
    }

    /**
     * @param borderOuterColor the borderOuterColor to set
     */
    public void setBorderOuterColor(Color borderOuterColor)
    {
        this.borderOuterColor = borderOuterColor;
    }

    /**
     * 
     * @return the border width
     */
    public int getBorderWidth()
    {
        return borderWidth;
    }

    /**
     * 
     * @param borderWidth the new width
     */
    public void setBorderWidth(int borderWidth)
    {
        this.borderWidth = borderWidth;
    }

}
