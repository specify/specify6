/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * A "liquid" Label that default to a gradeient filled square Label. 
 * A renderer need to be created so VectorLabel and GradiantLabel can share all the code.
* 
 * (Adapted from the book SwingHacks)
 *
 * @code_status Beta
 * 
 * @author rods
 * @author Chris Adamson
 * @author Joshua Marinacci
 *
 */
@SuppressWarnings("serial")
public class GradiantLabel extends JLabel
{
    protected Color   textColor        = null;
    protected Color   textColorShadow  = null;
    protected Color   bgBaseColor      = null;
    
    protected Color   gradiantTop      = null;
    protected Color   gradiantBot      = null;
    
    /**
     * Defaults to a gradiant square Label
     * @param text the label on the Label
     */
    public GradiantLabel(String text) 
    {
        super(text);
        setTextColor(Color.BLACK);
        setBorder(new EmptyBorder(0,0,0,0));
        bgBaseColor = (new JLabel()).getBackground();
    }
    
    /**
     * Defaults to a gradiant square button with JLabel alignments (XXX right align is not implemented)
     * @param text the lable text
     * @param align alignment from JLabel
     */
    public GradiantLabel(String text, int align) 
    {
        super(text, align);
        setTextColor(Color.BLACK);
        bgBaseColor = (new JLabel()).getBackground();
    }
    
    /* (non-Javadoc)
     * @see java.awt.Component#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize() 
    {
        String text = getText();
        FontMetrics fm = this.getFontMetrics(getFont());
        float scale = (50f/40f)*this.getFont().getSize2D();
        int w = fm.stringWidth(text);
        w += (int)(scale*1.4f);
        int h = fm.getHeight();
        h += (int)(scale*.3f);
        
        Icon icon = getIcon();
        if (icon != null)
        {
            h = Math.max(icon.getIconHeight(), h);
            w += icon.getIconWidth() + this.getIconTextGap();
        }
        return new Dimension(w, h);
    }
    
    /* (non-Javadoc)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g) 
    {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        
        String text      = getText();
        int    textWidth = g2.getFontMetrics().stringWidth(text);
        
        int w = this.getWidth();
        int h = this.getHeight();
        g2.fillRect(0,0, w,h);
 
        drawLabelBody(w,h, bgBaseColor, g2);

        Icon icon = getIcon();
        if (icon == null)
        {
            drawText(w, h, text, textWidth, g2);
            
        } else if (this.getHorizontalTextPosition() == SwingConstants.LEFT)
        {
            int offsetX = drawText(w, h, text, textWidth, g2);
            icon.paintIcon(this, g2, offsetX+textWidth+this.getIconTextGap(), h > icon.getIconHeight() ? (h - icon.getIconHeight())/2 : 0);
            
        } else
        {
            icon.paintIcon(this, g2, 0, h > icon.getIconHeight() ? (h - icon.getIconHeight())/2 : 0);
            drawText(this.getIconTextGap()+icon.getIconWidth(), h, text, g2);
            
        }
    }
    
    protected void drawLabelBody(int w, int h, Color base, Graphics2D g2) 
    {
        // draw the Label body
        Color grad_top = gradiantTop != null ? gradiantTop : base.brighter();
        Color grad_bot = gradiantBot != null ? gradiantBot : base.darker();
        
        GradientPaint bg = new GradientPaint(new Point(0,0), grad_top,
                                             new Point(0,h), grad_bot);
        g2.setPaint(bg);
        g2.fillRect(0, 0, w, h);
        
    }
    
    protected int drawText(int w, int h, String text, int textWidth, Graphics2D g2) 
    {
        // calculate the width and height
        int textx = this.getHorizontalAlignment() == SwingConstants.LEFT ? Math.max(getInsets().left, 2) : (w-textWidth)/2;
        drawText(textx, h, text, g2);
        return textx;
    }
    
    protected void drawText(int x, int h, String text, Graphics2D g2) 
    {
        // calculate the width and height
        int fh = g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent();
        int texty = h/2 + fh/2;
        
        // draw the text
        g2.setColor(textColorShadow);
        g2.drawString(text, x, texty);
        g2.setColor(textColor);
        g2.drawString(text, x, texty);

    }
    
     // generate the alpha version of this color
    protected static Color alphaColor(Color color, int alpha) 
    {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
    
    /**
     * @param gradiantBot the gradiantBot to set
     */
    public void setGradiants(Color gradiantTop, Color gradiantBot)
    {
        this.gradiantTop = gradiantTop;
        this.gradiantBot = gradiantBot;
    }

    /**
     * @return the textColorShadow
     */
    public Color getTextColorShadow()
    {
        return textColorShadow;
    }

    /**
     * @param textColorShadow the textColorShadow to set
     */
    public void setTextColorShadow(Color textColorShadow)
    {
        this.textColorShadow = textColorShadow;
    }

    /**
     * @return the bgBaseColor
     */
    public Color getBGBaseColor()
    {
        return bgBaseColor;
    }

    /**
     * @param bgBaseColor the bgBaseColor to set
     */
    public void setBGBaseColor(Color bgBaseColor)
    {
        this.bgBaseColor = bgBaseColor;
    }

    public Color getTextColor()
    {
        return textColor;
    }

    public void setTextColor(Color textColor)
    {
        this.textColor = textColor;
        textColorShadow = new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 70);
    }

    
}
